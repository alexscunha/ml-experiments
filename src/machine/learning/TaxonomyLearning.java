package machine.learning;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import util.Config;
import estruturas.grafo.*;
import statistics.FrequentItemSet;
/**
 * Classe que executa o processamento do algoritmo <b>Taxonomy Learning</b>, publicado no 
 * artigo <i>Folksonomy-based Collabulary Learning</i>, do professor Leandro Balby Marinho. 
 * @author Alex
 * @version 1.0
 */
public class TaxonomyLearning {
	// vetor de threshold de suporte
	private int m[] = null;
	// vetor de threshold de arestas
	private float e[] = null;
	// Numero de transacoes do arquivo a ser processado
	private int numTransactions;
	// Numero de iteracoes do algoritmo
	private static final int MAX_ITERATIONS = 6;
	// Objeto que armazena as configurações carregadas de um arquivo externo
	private static Config config = null;
	// referencia para a colecao de itemsets carregados do arquivo externo para a memoria
	private List<Itemset> items = null;
	// Mapa com todas as tags e seu referido suporte, de acordo com o itemset file.
	private Map<String,Integer> support = null;
	// Grafo que representa a taxonomia aprendida
	private Grafo learnedTaxonomy;
	// Verifica se já foi feita a inicialização correta das propriedades da classe
	private boolean initialized = false;
	
	/**
	 * Inicializa a instancia de um objeto TaxonomyLearning de forma adequada.
	 * Sem a sua execução, os métodos {@link #doAlgorithm()} não funcionará.
	 * @param configFile O arquivo de propriedades com as configurações para execução
	 * do algoritmo
	 */
	public void init( String configFile, float edgeThreshold ){
		m = new int[MAX_ITERATIONS];
		//e = new float[]{1.5f};
		e = new float[]{ edgeThreshold };
		
		learnedTaxonomy = null;
		
		if( !initialized)
			initialized = true;
		else
			return;
		
		System.out.println("Aguarde.... Inicializando!");
		try {
			
			config = new Config( configFile  );
		
		} catch (IOException e) {
			System.err.println("Erro de carga do Config()");
			System.exit(0);
		}
		
		items = new ArrayList<Itemset>();
		
		numTransactions = loadTransactions( config.getProperty("drive") +
											 config.getProperty("inputDir") +
				                             config.getProperty("fullFile") );
		
		for (int i = 0; i < m.length; i++) {
			m[i]=  (int) ((0.0016 * numTransactions) / ( Math.pow(2,i) ));
			System.out.println("m[" + i + "] = " + m[i]);
		}
		
		// Computa e recebe um set com o suporte de cada tag.
		support = FrequentItemSet.computeSupportTags(config.getProperty("drive") +
				config.getProperty("inputDir") +
                 config.getProperty("fullFile") );
		
	}
	

	/**
	 * Carrega na instancia do objeto TaxonomyLearning o conjunto de itemsets encontrados no
	 * arquivo externo.
	 * @param inputFile O nome do arquivo
	 * @return o número de transações carregadas
	 */
	private int loadTransactions(String inputFile){
		/* Declaracoes para arquivo de entrada */
		BufferedReader br = null;		
		File fileIn  = new File( inputFile );
		
		StringBuilder linha = new StringBuilder();
		String[] tokens = null;
		
		int count = 0, dot=0;

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));				
			
			System.out.println("\n============== loadingTransactions() ===============");
			while (br.ready()){
				linha.delete(0, linha.length());
				linha.append(br.readLine()); 
				tokens = linha.toString().split("\t");
							
				if ( tokens[0].equalsIgnoreCase("%") || tokens[0].equalsIgnoreCase("#"))
					continue;
				
				count++;
				items.add(new Itemset( Integer.parseInt(tokens[0]), tokens[1], tokens[2],
						              (int)Float.parseFloat(tokens[3]), tokens[tokens.length-1]));
				
				if( count % 500 == 0 ){
					dot++;
					if( dot % 50 == 0)
						System.out.print("\n");
					System.out.print(". ");
				}
			} 
			br.close();
			System.out.print("[done]\n");
		} catch (IOException ioe ){
				ioe.printStackTrace();
		}
		return count;
	}
	
	/**
	 * Extrai os itemsets mais frequentes, resultando no conjunto de peças que serão utilizadas
	 * para o aprendizado da taxonomia. 
	 * @param threshold O threshold de suporte que determina os itemset mais frequentes.
	 * @return Um <i>List<i> contendo os itemsets selecionados
	 */
	private List<Itemset> mineFrequentItemsets(int threshold ){


		List<Itemset> mined = new ArrayList<Itemset>();
		
		for (Iterator<Itemset> it = items.iterator(); it.hasNext();) {
			Itemset element = (Itemset) it.next();
			
		
			if ( element.getSupport() >= threshold ){
				mined.add( element );
//				System.out.println( element.toString());
			}
			
		} // end-for
		System.out.println( "# minedItems: " + mined.size());
		return mined;
		
	}
	
	/**
	 * Constroi a taxonomia em um grafo utilizando os itemsets selecionados em 
	 * {@link #mineFrequentItemsets}. Deste modo, as relações taxonômicas são
	 * aprendidadas para a iteração corrente.
	 * @param fqSets Um List com os Itemset mais frequentes da iteração.
	 * @param e O array de threshold de aresta.
	 * @return O grafo com as relações que foram aprendidas na iteração corrente.
	 */
	private Grafo buildTaxonomyPieces( List<Itemset> fqSets, float e[]){
		Grafo Si = new Grafo();
		int supTx, supTy;
		
		Iterator<Itemset>it = fqSets.iterator();
		while( it.hasNext()){
			Itemset itemset = it.next();
			supTx = support.get( itemset.getTag1());
			supTy = support.get( itemset.getTag2());
			
		
			if ( supTx >= e[0] * supTy ){
				Vertice origem = Si.addVertice( itemset.getTag1());
				Vertice destino = Si.addVertice( itemset.getTag2());
				Si.addAresta(origem, destino, 1);
				//System.out.println(Si.toString());
				
			}
			
			
		}

		
		return Si;
	}

	/**
	 * Realiza um processamento no grafo fornecido como argumento para evitar relações
	 * de múltipla-heranca, i.e., tx->ty; ty->tz; tx->tz (observe que tz tem dois pais).
	 * Neste caso, a aresta tx->tz é removida, dando preferencia aos caminhos mais
	 * longos visto que normalmente são mais informativos.
	 * <p>
	 * Depende da chamada prévia do método {@link #buildTaxonomyPieces}
	 * @param Si O grafo a ser podado.
	 * @return Um novo grafo contendo as relações que foram podadas.
	 */
	private Grafo prunePieces( Grafo Si){
		Grafo Sii = null;
		
		Sii = Si.DFS();
			
		return Sii;
	}
	
	/**
	 * As relacoes aprendidas em cada iteracao sao fundidas com as relacoes já
	 * aprendidas em iterações anteriores, 
	 * <p>
	 * Depende da chamada prévia do método {@link #prunePieces}
	 * @param S O grafo que contém as relações já aprendidas
	 * @param Sii O grafo que contém as relações aprendidas da iteração corrente
	 * @return O grafo S atualizado, com as novas relações incorporadas.
	 */
	private Grafo addPrunedPiecesToTaxonomy( Grafo S, Grafo Sii){
		
		
		if( S.isEmpty()) {

			for( Vertice v: Sii.getVertices()){
				S.addVertice( v.getTag());
			}
			
			for( Aresta a: Sii.getArestas()){
				S.addAresta(a.getOrigem().getTag(), a.getDestino().getTag(), a.getPeso());
			}
			
			return S;
		}
		Vertice origem, destino;
		
		for(Aresta arco: Sii.getArestas()){
			origem  = S.addVertice(arco.getOrigem().getTag());
			destino = S.addVertice(arco.getDestino().getTag());

			/* Apenas as novas relacoes em conformidade com as antigas serao aprendidas */
			if( S.getNumIncidentes(destino) == 0) {
				S.addAresta(origem, destino, arco.getPeso());
			}
			
		}
			
		return S;
	}
	
	
	/**
	 * Executa o algoritmo de aprendizado de relações hierarquicas para uma taxonomia.
	 */
	public void doAlgorithm( ){
		List<Itemset> fqSets = null;
		Grafo S  = new Grafo();
		Grafo Si = new Grafo();
		Grafo Sii = new Grafo();
		
		if( !initialized){
			System.out.println("Método init() ainda não foi executado.");
			return;
		}
		
		for (int i = 0; i < m.length; i++) {
			System.out.println(i+1 + "a Iteracao.");
			fqSets = mineFrequentItemsets(m[i]);
			
			Si = buildTaxonomyPieces(fqSets, e);	
			//System.out.println(Si.toString());
			
			Sii = prunePieces(Si);	
			//System.out.println(Sii.toString());
			
			S = addPrunedPiecesToTaxonomy(S, Sii);			
			System.out.println(S.toString());
		}
		System.out.println("Concluido!!!!!");
		
		learnedTaxonomy = S;
		
//		saveToFile(S,"TaxonomyLearningFile");
		
		
		
	}

	/**
	 * Salva em um arquivo externo um conjunto de regras o qual representa a taxonomia aprendida. Cada linha
	 * do arquivo é formada pelo tipo da instrução e seu respectivo conteúdo. O identificador "vertice" 
	 * vem acompanhado do respectivo nome do vértice. O identificador "aresta", é composto pelo
	 * nome dos dois vertices adjacentes e seu respectivo peso. 
	 * Este método depende da execução prévia do método {@link #doAlgorithm()}.
	 * <p>
	 * Um seguindo arquivo é gerado automaticamente, para fins de restauracao em um banco de dados. Esse
	 * Arquivo é chamado "TaxonomyLearningToDB". 
	 * </p>
	 * Ultima atualização: 27/05/2013
	 * @param outputFile Nome que deseja para o arquivo que será gerado
	 * </p>
	 * @return retorna o caminho absoluto do arquivo que foi gerado
	 */
	public String saveToFile(String outputFile){
		
		if( learnedTaxonomy == null )
			return null;
		
		/* Declaracoes para arquivo de saida*/
		BufferedWriter bw = null;
		BufferedWriter bwdb = null;
		File fileOut = new File( outputFile);
		File fileOutDB = new File( "TaxonomyLearningToDB");
		
		int sequence=0;
		
		try {
			System.out.println("\n============== saveToFile() ===============");
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			bwdb = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOutDB),"UTF8"));
					
			System.out.println("(1) Salvando os vertices...");
			for(Vertice v: learnedTaxonomy.getVertices()){
				bw.write("vertice\t" + v.getTag());
				bw.newLine();
				
	            for (Aresta e : v.getAdj() ) {
	            	
	            	bw.write("aresta\t" + e.getOrigem().getTag() + "\t" + e.getDestino().getTag() +  
	            			 "\t" + e.getPeso());
	            	bw.newLine();	            
	            	
	            	bwdb.write(++sequence + "\t" + e.getOrigem().getTag() + "\t" + e.getDestino().getTag());
	            	bwdb.newLine();
	            	
	            }
	            bw.flush();		
	            bwdb.flush();
			}
			bw.close();
			bwdb.close();
			System.out.println("Operacao concluida....");

		} catch (IOException ioe ){
				ioe.printStackTrace();
		}
		return fileOut.getAbsolutePath();
	}
	

	
	/**
	 * Método que adiciona ao arquivo de instancias um novo atributo referente ao
	 * "shortest distance" entre duas tags, de acordo com o algoritmo "Taxonomy Learning".
	 * @param feature O nome do atributo que representa "shortest distance" no grafo
	 */
	@SuppressWarnings({ "unused" })
	public void addFeature(String feature) {
		
		if( learnedTaxonomy == null ) {
			System.err.println("Taxonomia não foi aprendida.");
			return;
		}

		
		File fileIn  = new File( config.getProperty("drive") + config.getProperty("inputDir") + 
								config.getProperty("fullFile"));
		
		File fileOut = new File( config.getProperty("drive") + config.getProperty("outputDir") + 
								config.getProperty("fullFile") + "-addShortestPath");	
		
		BufferedWriter bw = null;
		BufferedReader br = null;
		
		
		StringBuilder linha = new StringBuilder();
		StringBuilder tag1 = new StringBuilder();
		StringBuilder tag2 = new StringBuilder();
		
		String[] tokens = null;
				
		int count = 0;
		String cabecalho=null;
		Grafo sub = null;
		DijkstraEngine de = null;
		int distancia = 0;
		Vertice v=null,u=null;
		int hits=0;

		try {			
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			System.out.println("\n============== addFeature() ===================");
			
			bw = new BufferedWriter(new OutputStreamWriter
					  (new FileOutputStream(fileOut),"UTF8"));

			cabecalho = br.readLine();
			tokens = cabecalho.split("\t");
			
			if ( !tokens[0].equals("#") ) {
				System.err.println("\nArquivo " + fileIn.getName() + " nao contem cabecalho.");
				br.close();
				bw.close();
				return;
			} else {
				for(int i=0; i<tokens.length-1; i++){
					bw.write(tokens[i] + "\t");		
				}
				bw.write("path" + "\t" + tokens[tokens.length-1]);
				bw.newLine();
				bw.flush();
			}
			
			while( br.ready()){
				linha.delete(0, linha.length());	
				linha.append(br.readLine());
				tokens = linha.toString().split("\t");
				
				/* # = cabecalho     % = comentario */
				if( tokens[0].equals("#") || tokens[0].equals("%"))
					continue;
				
				count++;
				
				tag1.delete(0, tag1.length());
				tag1.append(tokens[1]);
				tag2.delete(0, tag2.length());
				tag2.append(tokens[2]);
				
				sub = learnedTaxonomy.getSubGrafo(tag1.toString());
				
				if( sub == null ) {
					System.err.println("subgrafo nao encontrado para " + tag1.toString());
					distancia = 0;
				} else {
					de = new DijkstraEngine( sub );
					
					if ( (v = sub.getVertice(tag1.toString()))==null ||  (u = sub.getVertice(tag2.toString()))==null ){
						distancia = 0;
					} else {
						distancia = de.execute(sub.getVertice(tag1.toString()), sub.getVertice(tag2.toString()));
						hits++;
					}
				}
				
				for(int i=0; i<tokens.length-1; i++){
					bw.write(tokens[i] + "\t");		
				}
				bw.write(distancia + "\t" + tokens[tokens.length-1]);
				bw.newLine();
				bw.flush();
				
				if ( count % 100 == 0)
				System.out.println("\tProgresso: " + count);

			}
			
			System.out.println("\nTotal de Hits: " + hits);

			br.close();
			bw.close();
			
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
		
		
	}

	/**
	 * Verifica se as relações aprendidas pelo algoritmo TaxonomyLearning não foram
	 * rotuladas como hiperônimo pelo ConceptNet5. Esta verificação é feita verificando
	 * se a relação aprendida está presente no arquivo de rotulações do ConceptNet previamente
	 * processado. 
	 * 
	 * @param conceptnetFile O arquivo BASIC FORMAT com as relações validadas no ConceptNet
	 * @return O nome do arquivo gerado, contendo as relações aprendidas que não se encontram 
	 * no ConceptNet5
	 */
	public String checkRelationsInConceptNetFile( String conceptnetFile ){
		
		if( learnedTaxonomy == null )
			return null;
		
		/* Declaracoes para arquivo de entrada */
		BufferedReader br = null;		
		File fileIn  = new File( conceptnetFile );
		BufferedWriter bw = null;
		File fileOut = new File("out-RelationsNotInConceptNet5");
		
		StringBuilder linha = new StringBuilder();
		String[] tokens = null;
		
		Map<String,String> transactions = new HashMap<String,String>();
		List<String> found = new ArrayList<String>();
		
		
	
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));				
			
			System.out.println("\n============== loadingConceptNetFile() ===============");
			while (br.ready()){
				linha.delete(0, linha.length());
				linha.append(br.readLine()); 
				tokens = linha.toString().split("\t");
							
				if ( tokens[0].equalsIgnoreCase("%") || tokens[0].equalsIgnoreCase("#"))
					continue;
				
				transactions.put(tokens[1]+"\t"+tokens[2], tokens[tokens.length-1]);
								
			} 
			br.close();
			
			int absent=0,present=0;
			System.out.println(".....checking absent relations in ConceptNet5");

			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			bw.write("============ RELACOES TAXONOMYLEARNING AUSENTES NO CONCEPTNET5 ================");
			bw.newLine();
			StringBuffer key = new StringBuffer();
			for(Vertice v: learnedTaxonomy.getVertices()){
					
	            for (Aresta e : v.getAdj() ) {
	        		key.delete(0, key.length());
					key.append( v.getTag() + "\t"); 
					key.append(e.getDestino().getTag());
	            	
	            	if( !transactions.containsKey( key.toString() ) ){
	            		bw.write(e.getOrigem().getTag() + "\t" + e.getDestino().getTag());
	            		bw.newLine();
	            		absent++;
	            	} else {
	            		found.add(e.getOrigem().getTag() + "\t" + e.getDestino().getTag());
	            		present++;
	            	}
	            	
	            	bw.flush();	            	
	            }
			} // end for-vertices
			bw.newLine();
			bw.write("Total de Relacoes Hiperonimas AUSENTES no ConceptNet5: " + absent + "\n");
			bw.newLine();
			bw.flush();
			
			System.out.println(".....Find out Relations present in ConceptNet");

			bw.write("============ RELACOES TAXONOMYLEARNING PRESENTES NO CONCEPTNET5 ================");
			bw.newLine();
			for( String s: found ){
				bw.write( s );
				bw.newLine();
				bw.flush();
			}
			bw.newLine();
			bw.write("Total de Relacoes Hiperonimas ENCONTRADAS no ConceptNet5: " + present);
			bw.newLine();
			bw.flush();
			bw.close();
			System.out.println(".....done\n\n");
			
		} catch (IOException ioe ){
				ioe.printStackTrace();
		}
		return fileOut.getName();
	}
	
	/**
	 * Revela as relações semanticas existentes no arquivo <b>fileTwo</b> que não estão em
	 * <b>fileOne</b>. A ideia é descobrir quais relações apareceram em um taxonomia que foi
	 * gerada usando um threshold mais baixo em relação a outra que utilizou um threshold maior.
	 * @param fileOne O arquivo que contem as relações geradas com um threshold maior
	 * @param fileTwo O arquivo que contem as relações geradas com um threshold menor
	 */
	public static void displayNewRelations( String fileOne, String fileTwo ) {
		
		/* Declaracoes para arquivo de entrada */
		BufferedReader br = null;		
		File fileIn  = new File( fileOne );
		
		StringBuilder linha = new StringBuilder();
		String[] token = null;
		
		List<String> transactions = new ArrayList<String>();
	
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));				
			
			System.out.println("\n============== displayNewRelations() ===============");
			System.out.println("..... Reading instances from " + fileOne);
			while (br.ready()){
				linha.delete(0, linha.length());
				linha.append(br.readLine()); 
				token = linha.toString().split("\t");
							
				if ( token[0].equalsIgnoreCase("%") || token[0].equalsIgnoreCase("#"))
					continue;
				
				transactions.add(token[1]+"\t"+token[2]);		
			} 
			br.close();
			
			System.out.println("..... Finding new relations created in " + fileTwo );

			fileIn  = new File( fileTwo );
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));				

			while (br.ready()){
				linha.delete(0, linha.length());
				linha.append(br.readLine()); 
				token = linha.toString().split("\t");
							
				if ( token[0].equalsIgnoreCase("%") || token[0].equalsIgnoreCase("#"))
					continue;
				
				if( !transactions.contains(token[1]+"\t"+token[2]) )
					System.out.println( token[1]+"\t"+token[2]);	
			} 
			br.close();
	
			System.out.println(".....done\n\n");
			
		} catch (IOException ioe ){
				ioe.printStackTrace();
		}		
	}
	
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		TaxonomyLearning tl = new TaxonomyLearning();
		int opcao;
		String taxonomyFile = "Graph-DUMP";
		
	
		
		for(;;){
			System.out.println("1 - Executar algoritmo \"Taxonomy Learning\"");
			System.out.println("2 - Salvar Taxonomia da memória para arquivo em disco");
			System.out.println("3 - Restaurar Taxonomia do disco para memória");
			System.out.println("4 - Adicionar atributo \"shortestPath\"");
			System.out.println("5 - Exibir subgrafo");
			System.out.println("6 - Checar se as relações aprendidas estão no arquivo do ConceptNet5");
			System.out.println("7 - Diversos");
			System.out.println("8 - Sair");
			System.out.print("Opcao: ");
			opcao = Integer.parseInt(in.nextLine());
			
			switch (opcao) {
			case 1:
				System.out.print("\nThreshold: ");
				float threshold  = Float.parseFloat( in.nextLine() );

				tl.init("config.dat", threshold);
				tl.doAlgorithm();
				break;
			case 2:
				System.out.println( tl.saveToFile(taxonomyFile));
				break;
			case 3:
				try {
					tl.learnedTaxonomy.restoreFromFile( taxonomyFile );
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Erro na restauracao do arquivo.");
				}
				break;
			case 4:
				tl.addFeature("shortestPath");
				break;
			case 5:
			{
				System.out.print("\nTag: ");
				String tag = in.nextLine();
				
				System.out.println("Tag " + tag + (tl.learnedTaxonomy.exists(tl.learnedTaxonomy.getVertice(tag))==true?"Encontrada":"NAO ENCONTRADA")  + " em learnedTaxonomy()"  );
				Grafo sub = tl.learnedTaxonomy.getSubGrafo(tag);
				System.out.println( sub );
				
				break;
			}
			case 6:
			{
				tl.checkRelationsInConceptNetFile("AdditionalInstancesFoundConceptNet5");
				break;
			}	
			case 7:
			{
				/* Exibir relações que surgiram quando utilizado um threshold mais baixo */
//				TaxonomyLearning.displayNewRelations("[thr=1.5]-TaxonomyLearningToDB-1416", "[thr=1.4]-TaxonomyLearningToDB-1443");
				TaxonomyLearning.displayNewRelations("out-labelByConceptNet-threshold-1.0", "out-SelectedConceptNet5Instances");
				break;
			}
			case 8:
				System.exit(0);
			default:
				System.out.println("\n\nOpcao Invalida!");
			}
			
			
		}
		
		
		

	}
}
