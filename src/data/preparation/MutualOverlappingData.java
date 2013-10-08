package data.preparation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import util.Config;

public class MutualOverlappingData {
	private static Config config = null;
	/* Hash com o mapeamento de uma tag para todos os seus respectivos recursos */
	private static HashMap<String,HashSet<String>> mapTagRecurso;
	/* Hash com o mapeamento de tags para o WordNet */
	private static HashMap<String, String> setMapTags = new HashMap<String,String>();
	/* Verifica se o arquivo de anotações de recursos para uma tag foi carregado */
	private static boolean tagAnnotationLoaded = false;

	public static void init(){
		loadConfig("config.dat");
		loadTagAnnotation("TagAnnotationSet");
	}
	
	/* carrega o arquivo de configuracao */
	public static void loadConfig( String fileName ){
		try {
			config = new Config( fileName );
		
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	
	/*
	 * Carrega para mapTagRecurso o arquivo de mapeamento tag x recursos anotados.
	 */
	public static void loadTagAnnotation( String inputFile ){
		File fileIn  = new File( config.getProperty("drive") + config.getProperty("inputDir") + inputFile);
		
		BufferedReader br = null;
		
		StringBuilder linha = new StringBuilder();
		String[] tokens = null;
				
		//Mapa que guarda os recursos que foram marcados por uma tag
		mapTagRecurso = new HashMap<String, HashSet<String>>();
		try {			
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			System.out.println("\n============== LoadSetTagAnnotationFile() ===================");			
			
			// Processando
			while( br.ready()){
				linha.delete(0, linha.length());	
				linha.append(br.readLine());
				tokens = linha.toString().split("\t");
				
				/* # = cabecalho     % = comentario */
				if( tokens[0].equals("#") || tokens[0].equals("%"))
					continue;
				
				/* tokens[0] = "chave"     tokens[1]...tokens[n] = recursos */
				mapTagRecurso.put( tokens[0], new HashSet<String>() );
				for( int i=1; i < tokens.length; i++ )
					mapTagRecurso.get(tokens[0]).add( tokens[i]);
	
			}
			br.close();		
			System.out.print("\tTag x Recursos carregados....: " + mapTagRecurso.size());
			System.out.println("......[done]");
			
			tagAnnotationLoaded = true;
			
		} catch (IOException ioe){
			ioe.printStackTrace();
		}


	} // fim loadTagAnnotation()
	
	/* Preenche o hash de tags mapeadas para o wordnet */
	@SuppressWarnings("unused")
	private static void loadTagsMappedWordNet( String inputFile ){
		File fileIn  = new File( config.getProperty("drive") + config.getProperty("inputDir") + inputFile);
		
		StringBuilder linha = new StringBuilder();
		String[] temp = null;


		if (setMapTags.size() > 0 )
			return;
		
		try {
			System.out.println("\n============== loadTagsMappedWordNet() ===================");		
			BufferedReader buffer = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
					
			while(	buffer.ready() ) {
				linha.delete(0, linha.length());
				linha.append(buffer.readLine()); 
				temp = linha.toString().split("\t");
				
				if ( !setMapTags.containsKey(temp[1])){
					/* chave= tag escrita no Bibsonomy / valor=palavra mapeada no wordNet */
					setMapTags.put(temp[1], temp[0]);
				}
			}
			buffer.close();
			System.out.println("\t...[done]");
		} catch (IOException ioe){
			ioe.printStackTrace();
			return;
		}
	}
	
	/* O objetivo deste método é apenas calcular o mutual overlapping entre duas tags e exibir
	 * sua classificacao de acordo com o alfa e o beta */
	@SuppressWarnings("unchecked")
	public static void calculateRelationBetweenTags( String tag1, String tag2, double d, double e){
		// calculate mutual-overlapping measure
		
		double a,b;
		Set<String> cardG1 = mapTagRecurso.get(tag1);
		Set<String> cardG2 = mapTagRecurso.get(tag2);
		Set<String> intersectG1G2  = (Set<String>) mapTagRecurso.get(tag1).clone();
		
		/* Verificando se a tag foi encotnrada */
		if ( cardG1 == null ) {
			System.out.println("\"" + tag1 + "\" nao foi encontrada");
			return;
		} else if ( cardG2 == null ){
			System.out.println("\"" + tag2 + "\" nao foi encontrada");
			return;
		}
		
		/* Obtendo o Set de Intersecao entre os dois conjuntos */
		intersectG1G2.retainAll(cardG2);
		
		a = intersectG1G2.size()*1.0 / Math.min( cardG1.size(), cardG2.size());
		b = intersectG1G2.size()*1.0 / Math.max( cardG1.size(), cardG2.size());
		

		String rotulo;
		/* O alfa e beta são thresholds para estimar o melhor parâmetro */
	    if ( a >= d){ /* a é GRANDE o suficiente */
	    	if ( b >= d ){ /* "a" é GRANDE o suficiente e "b" é GRANDE o suficiente */
	    		rotulo = "EQUIVALENTES";
//	    		    System.out.println(tag1 + " e " + tag2 + " são EQUIVALENTES, e provavelmente não expressão uma relação de hierarquia (1)");	    		
	    	}else if (b < e	){ /* "a" é GRANDE o suficiente e "b" é PEQUENO o suficiente */
	    		rotulo = "INCORPORA";
//	    		System.out.println(tag1 + " INCORPORA (SUBSUME) " + tag2 + " (3)");
	    	} else {
	    		rotulo = "RELEVANTES";
//	    		System.out.println(tag1 + " e " + tag2 + " são RELEVANTES (2)");
	    	}
	    } else {
//	    	System.out.println(tag1 + " e " + tag2 + " são IRRELEVANTES (0)");
	    	rotulo = "IRRELEVANTES";
	    }
		
	    System.out.printf("\nTag |%s|=%4d e |%s|=%4d : a=%.4f e b=%.4f [Interception=%3d]. Classificacao=%s", tag1, cardG1.size(), tag2, cardG2.size(),  a, b, intersectG1G2.size(), rotulo);
	}

	/*
	 * Este metodo analisa o principio de mutual-overlapping entre duas tags, e classifica
	 * de acordo com o algoritmo extraido de "learning concept hierachy from folksonomies".
	 * 
	 * Ela recebe o arquivo de entrada e apenas adiciona ao final a classificacao segundo
	 * os parametros alfa, beta e mutual overlapping.
	 */
	@SuppressWarnings("unchecked")
	public static void doAnalyse( String inputFile, double alfa, double beta ){
		
		File fileIn  = new File( config.getProperty("drive") + config.getProperty("inputDir") + inputFile);
		String outputFile = "out-" + inputFile + "-mutualOverlappingAnalysis";
		File fileOut = new File( config.getProperty("drive") + config.getProperty("outputDir") + outputFile );		
		
		BufferedWriter bw = null;
		BufferedReader br = null;
		
		StringBuilder linha = new StringBuilder();
		String[] tokens = null;				
		
		int count=0;

		int cardG1, cardG2;
		double a,b;
		Set<String> projSetG1;
		Set<String> projSetG2;
		Set<String> intersectG1G2;
		try {			
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			System.out.println("\n============== doAnalyse() ===================");			
			bw = new BufferedWriter(new OutputStreamWriter
					  (new FileOutputStream(fileOut),"UTF8"));
			// Processando
			while( br.ready()){
				linha.delete(0, linha.length());	
				linha.append(br.readLine());
				tokens = linha.toString().split("\t");
				++count;
				
				/* # = cabecalho     % = comentario */
				if( tokens[0].equals("#") || tokens[0].equals("%"))
					continue;
				
				cardG1 = mapTagRecurso.get(tokens[1]).size();
				cardG2 = mapTagRecurso.get(tokens[2]).size();
				
				/* descarta os casos em que ProjSetT1 é menor que ProjSetT2 */
				if ( cardG1 < cardG2) {
					bw.write( linha.toString() + "\t" + "|RTi|<|RTj|");
					bw.newLine();
					bw.flush();
					continue;
				}
				
				projSetG1 = mapTagRecurso.get(tokens[1]);
				projSetG2 = mapTagRecurso.get(tokens[2]);
				intersectG1G2  = (Set<String>) mapTagRecurso.get(tokens[1]).clone();
				
				/* Verificando se a tag foi encontrada */
				if ( projSetG1 == null ) {
					System.out.println("\"" + tokens[1] + "\" nao foi encontrada");
					bw.write( linha.toString() + "\t" + "RTi=null");
					bw.newLine();
					bw.flush();
					continue;
				} else if ( projSetG2 == null ){
					bw.write( linha.toString() + "\t" + "RTj=null");
					bw.newLine();
					bw.flush();
					System.out.println("\"" + tokens[2] + "\" nao foi encontrada");
					continue;
				}

				
				/* Obtendo o Set de Intersecao entre os dois conjuntos */
				intersectG1G2.retainAll(projSetG2);
				
				/* Calculando o mutual-overlapping  */
				a = intersectG1G2.size()*1.0 / Math.min( cardG1, cardG2);
				b = intersectG1G2.size()*1.0 / Math.max( cardG1, cardG2);
				
				bw.write( linha.toString() + "\t");
				
				/* O alfa e beta são thresholds para estimar o melhor parâmetro
				 * O alfa é um threshold considerado grande. o beta é um threshold considerado pequeno */
			    if ( a >= alfa){ /* a é GRANDE o suficiente */
			    	if ( b >= alfa ){ /* "a" é GRANDE o suficiente e "b" é GRANDE o suficiente */
			    		bw.write("EQUIVALENTES-(1)");
//			    		    System.out.println(tag1 + " e " + tag2 + " são EQUIVALENTES, e provavelmente não expressão uma relação de hierarquia (1)");	    		
			    	}else if (b < beta	){ /* "a" é GRANDE o suficiente e "b" é PEQUENO o suficiente */
			    		bw.write("SUBSUME-(3)");
//			    		System.out.println(tag1 + " INCORPORA (SUBSUME) " + tag2 + " (3)");
			    	} else {
			    		bw.write("RELEVANTES-(2)");
//			    		System.out.println(tag1 + " e " + tag2 + " são RELEVANTES (2)");
			    	}
			    } else {
			    	/* Se "a" é pequeno, b também vai ser pequeno e não interessa */
			    	bw.write("IRRELEVANTES-(0)");
			    }
			    System.out.println("Progresso = " + count);
			    bw.newLine();
			    bw.flush();
			    

				
			}
			br.close();		
			System.out.println("......[done]");
			
		} catch (IOException ioe){
			ioe.printStackTrace();
		}

	}
	
	/* Este método "re-classifica" o status do arquivo de acordo com o novo alfa e beta 
	 * OBS:
	 * É necessario que inputFile já tenha a e b computado */
	@SuppressWarnings("unchecked")
	public static void reClassifyStatus(String inputFile, double alfa, double beta){
		File fileIn  = new File( config.getProperty("drive") + config.getProperty("inputDir") + inputFile);
		String outputFile = "out-" + inputFile + "-reClassified-alfa-"+alfa+"-beta-"+beta;
		File fileOut = new File( config.getProperty("drive") + config.getProperty("outputDir") + outputFile );		
				
		BufferedWriter bw = null;
		BufferedReader br = null;
		
		StringBuilder linha = new StringBuilder();
		String[] tokens = null;				
		
		int count=0, possivelHyper=0;
		int iStatus=-1;


		double a,b;
	
		try {			
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			System.out.println("\n============== reClassify(): alfa="+alfa+" beta="+beta+" ===================");			
			bw = new BufferedWriter(new OutputStreamWriter
					  (new FileOutputStream(fileOut),"UTF8"));
			
			if( br.ready()){
				linha.delete(0, linha.length());	
				linha.append(br.readLine());
				tokens = linha.toString().split("\t");
				if( !tokens[0].equalsIgnoreCase("#")){
					System.out.println("Arquivo de cabecalho nao encontrado!");
					br.close();
					bw.close();
					return;
				}
				for(int i=0; i< tokens.length; i++){
					if( tokens[i].equalsIgnoreCase("status")){
						iStatus = i;
						break;
					}
				}
				bw.write(linha.toString());
				bw.newLine();
				bw.flush();
			}
			
			// Processando
			while( br.ready()){
				linha.delete(0, linha.length());	
				linha.append(br.readLine());
				tokens = linha.toString().split("\t");
				++count;
				
				/* # = cabecalho     % = comentario */
				if( tokens[0].equals("#") || tokens[0].equals("%")){
					continue;
				}
					
				a = Double.parseDouble(tokens[iStatus-2]);
				b = Double.parseDouble(tokens[iStatus-1]);
				
				for( int i=0; i < iStatus; i++ )
					bw.write( tokens[i] + "\t");
				
								/* O alfa e beta são thresholds para estimar o melhor parâmetro
				 * O alfa é um threshold considerado grande. o beta é um threshold considerado pequeno */
			    if ( a >= alfa){ /* a é GRANDE o suficiente */
			    	if ( b >= alfa ){ /* "a" é GRANDE o suficiente e "b" é GRANDE o suficiente */
			    		bw.write("EQUIVALENT");
//			    		    System.out.println(tag1 + " e " + tag2 + " são EQUIVALENTES, e provavelmente não expressão uma relação de hierarquia (1)");	    		
			    	}else if (b < beta	){ /* "a" é GRANDE o suficiente e "b" é PEQUENO o suficiente */
			    		bw.write("CANDIDATE");
			    		possivelHyper++;
//			    		System.out.println(tag1 + " INCORPORA (SUBSUME) " + tag2 + " (3)");
			    	} else {
			    		bw.write("RELEVANTE");
//			    		System.out.println(tag1 + " e " + tag2 + " são RELEVANTES (2)");
			    	}
			    } else {
			    	/* Se "a" é pequeno, b também vai ser pequeno e não interessa */
			    	bw.write("IRRELEVANT");
			    }
			    bw.write("\t");
			    bw.write(tokens[tokens.length-1]);
			    bw.newLine();
			    bw.flush();
			    System.out.println("Progresso = " + count);
			}
			br.close();		
			System.out.println("......[done]");
			System.out.println("Total de HYPER pelo mutual: " + possivelHyper);
			
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
	}
	
	
	/* Este método exibe estatisticas sobre o dataset, baseado nas medidas de mutual overlapping 
	 * OBS:
	 * É necessario que inputFile já tenha a e b computado */
	@SuppressWarnings("unchecked")
	public static void viewStatistics(String inputFile, double threshold ){
		File fileIn  = new File( config.getProperty("drive") + config.getProperty("inputDir") + inputFile);
		BufferedReader br = null;
		
		StringBuilder linha = new StringBuilder();
		String[] tokens = null;				
		
	
		int ia=-1, ib=-1;
		double a,b,alfa,beta;
		
	
		try {			
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			System.out.println("\n============== viewStatistics() ===================");			
			
			if( br.ready()){
				linha.delete(0, linha.length());	
				linha.append(br.readLine());
				tokens = linha.toString().split("\t");
				if( !tokens[0].equalsIgnoreCase("#")){
					System.err.println("Arquivo de cabecalho nao encontrado!");
					br.close();
					return;
				}
				for(int i=0; i< tokens.length; i++){
					if( tokens[i].equalsIgnoreCase("a")){
						ia = i;
					} else if ( tokens[i].equalsIgnoreCase("b")) {
						ib = i;
					}
				}

			}

			// Processando
			int lostHyperbyValue=0,littleCard=0,lowValue=0,lostHyperByCard=0,remainingHyperByCard=0;
			Set<String> cardG1;
			Set<String> cardG2;
			
			while( br.ready()){
				linha.delete(0, linha.length());	
				linha.append(br.readLine());
				tokens = linha.toString().split("\t");
						
				if( tokens[0].equalsIgnoreCase("#")){
					continue;
				}

				a = Double.parseDouble(tokens[ia]);
				b = Double.parseDouble(tokens[ib]);

				cardG1 = mapTagRecurso.get(tokens[1]);
				cardG2 = mapTagRecurso.get(tokens[2]);
				
				if ( a < threshold && b < threshold){
					lowValue++;
					if (tokens[tokens.length-1].equalsIgnoreCase(config.getProperty("negativeLabel")))
						lostHyperbyValue++;
				} else if  ( cardG1.size() < cardG2.size()) {
					littleCard++;
					if( tokens[tokens.length-1].equalsIgnoreCase(config.getProperty("negativeLabel")))
						lostHyperByCard++;
	
				} else
					if( tokens[tokens.length-1].equalsIgnoreCase(config.getProperty("negativeLabel")))
						remainingHyperByCard++;
				
				
				
			} // while(br)
			br.close();
			
			System.out.println("Instancias Eliminadas (CardG1<CardG2)......: " + littleCard);
			System.out.println("Instancias Irrelevantes (a e b < threshold): " + lowValue);
			System.out.println("Rotulacoes HYPER perdidas (CardG1<CardG2)..: " + lostHyperByCard);
			System.out.println("Rotulacoes HYPER perdidas (irrelevantes)...: " + lostHyperbyValue);
			System.out.println("Total de Relações HYPER....................: " + remainingHyperByCard );
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
	}

	
	/* Este método "re-classifica" o status do arquivo de acordo com o novo alfa e beta 
	 * OBS:
	 * É necessario que inputFile já tenha a e b computado */
	@SuppressWarnings("unchecked")
	public static void combineThreshold(String inputFile){
		File fileIn  = new File( config.getProperty("drive") + config.getProperty("inputDir") + inputFile);
		String outputFile = null; 
		File fileOut = null; 		
				
		BufferedWriter bw = null;
		BufferedReader br = null;
		
		StringBuilder linha = new StringBuilder();
		String[] tokens = null;				
		
		int  possivelHyper=0;
		
		int ia=-1, ib=-1;
		double a,b,alfa,beta;
		
	
		try {			
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			System.out.println("\n============== viewStatistics() ===================");			
			
			if( br.ready()){
				linha.delete(0, linha.length());	
				linha.append(br.readLine());
				tokens = linha.toString().split("\t");
				if( !tokens[0].equalsIgnoreCase("#")){
					System.out.println("Arquivo de cabecalho nao encontrado!");
					br.close();
					return;
				}
				for(int i=0; i< tokens.length; i++){
					if( tokens[i].equalsIgnoreCase("a")){
						ia = i;
					} else if ( tokens[i].equalsIgnoreCase("b")) {
						ib = i;
					}
				}

			}
			br.close();
			// Processando
			for( int iAlfa=1; iAlfa<10; iAlfa++) {
				
				for( int iBeta=1; iBeta<10; iBeta++ ){
					
					alfa = iAlfa/10.0;
					beta = iBeta/10.0;
					
					if( beta >= alfa )
						continue;
					
					br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
					outputFile = "out-" + inputFile + "-viewStatistics-alfa="+alfa+"beta="+beta;
					fileOut = new File( config.getProperty("drive") + config.getProperty("outputDir") + outputFile );		
					
					bw = new BufferedWriter(new OutputStreamWriter
							  (new FileOutputStream(fileOut),"UTF8"));
					
					possivelHyper = 0;
					@SuppressWarnings("unused")
					int nEquivalent=0, nRelevant=0, nIrrelevant=0,nHyper=0;
					
					while( br.ready()){
						linha.delete(0, linha.length());	
						linha.append(br.readLine());
						tokens = linha.toString().split("\t");
						
						if( tokens[0].equalsIgnoreCase("#")){
							continue;
						}

						a = Double.parseDouble(tokens[ia]);
						b = Double.parseDouble(tokens[ib]);

						/* O alfa e beta são thresholds para estimar o melhor parâmetro
						 * O alfa é um threshold considerado grande. o beta é um threshold considerado pequeno */
					    if ( a >= alfa){ /* a é GRANDE o suficiente */
					    	if ( b >= alfa ){ /* "a" é GRANDE o suficiente e "b" é GRANDE o suficiente */
					    		nEquivalent++;
//					    		    System.out.println(tag1 + " e " + tag2 + " são EQUIVALENTES, e provavelmente não expressão uma relação de hierarquia (1)");	    		
					    	}else if (b < beta	){ /* "a" é GRANDE o suficiente e "b" é PEQUENO o suficiente */
								for( int i=0; i <= ib; i++ )
									bw.write( tokens[i] + "\t");
					    		bw.write("CANDIDATE" + "\t" + tokens[tokens.length-1]);
					    		bw.newLine();
					    		bw.flush();
					    		
					    		if( tokens[tokens.length-1].equalsIgnoreCase("HYPER"))
					    			nHyper++;
					    		possivelHyper++;
//					    		System.out.println(tag1 + " INCORPORA (SUBSUME) " + tag2 + " (3)");
					    	} else {
					    		nRelevant++;
//					    		System.out.println(tag1 + " e " + tag2 + " são RELEVANTES (2)");
					    	}
					    } else {
					    	/* Se "a" é pequeno, b também vai ser pequeno e não interessa */
					    	nIrrelevant++;
					    }
					}
					br.close();		

					String resultado =   "\n%alfa = " + alfa + "\tbeta = " + beta + "\n" + 
							             "% Total EQUIVALENT =  " + nEquivalent + "\n" +
					                     "% Total RELEVANT   =  " + nRelevant + "\n" +
							             "% Total IRRELEVANT =  " + nIrrelevant + "\n" +
					                     "% Total CANDIDATE  =  " + possivelHyper + "\n" +
						                 "% Total HYPER      =  " + nHyper;
					System.out.println( resultado );
					bw.write( resultado );
					bw.newLine();
					bw.flush();
					bw.close();
				} // fim for-iBeta
				br.close();
			}
			
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
	}

	
	/*
	 * Acrescenta atributos mutual overlapping como ultimos atributos do arquivo de instancias
	 */
	@SuppressWarnings("unchecked")
	public static void addMutualOverlapping(String inputFile, double alfa, double beta){
		File fileIn  = new File( config.getProperty("drive") + config.getProperty("inputDir") + inputFile);
		String outputFile = "out-" + inputFile + "-addMutualOverlapping";
		File fileOut = new File( config.getProperty("drive") + config.getProperty("outputDir") + outputFile );		
		
		if (!tagAnnotationLoaded) {
			System.err.println("PRE-REQUISITO: ja ter sido executado o metodo loadTagAnnotation()");
		    return;
		}
		
		BufferedWriter bw = null;
		BufferedReader br = null;
		
		StringBuilder linha = new StringBuilder();
		String[] tokens = null;				
		
		int count=0;
		int contagemHyper=0;
		int cardG1, cardG2;
		double a,b;
		Set<String> projSetG1;
		Set<String> projSetG2;
		Set<String> intersectG1G2;
		
		try {			
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			System.out.println("\n================ addMutualOverlapping() ===================");			
			bw = new BufferedWriter(new OutputStreamWriter
					  (new FileOutputStream(fileOut),"UTF8"));
			// Processando
			while( br.ready()){
				linha.delete(0, linha.length());	
				linha.append(br.readLine());
				tokens = linha.toString().split("\t");
				++count;
				
				/* % = comentario */
				if ( tokens[0].equals("%") ){
					bw.write( linha.toString());
					bw.flush();
					continue;					
				}
								
				/* Joga em output todos os tokens, exceto o label, que é o ultimo */
				for( int i=0; i < tokens.length - 1; i++ )
					bw.write( tokens[i] + "\t");
				
				/* # = cabecalho */
				if( tokens[0].equals("#") ){
					bw.write( "a\tb\tstatus\t" + tokens[tokens.length-1]);
					bw.newLine();
					bw.flush();
					continue;
				}
									
				cardG1 = mapTagRecurso.get(tokens[1]).size();
				cardG2 = mapTagRecurso.get(tokens[2]).size();
				
				projSetG1 = mapTagRecurso.get(tokens[1]);
				projSetG2 = mapTagRecurso.get(tokens[2]);
				intersectG1G2  = (Set<String>) mapTagRecurso.get(tokens[1]).clone();

				
				/* Obtendo o Set de Intersecao entre os dois conjuntos */
				intersectG1G2.retainAll(projSetG2);
				
				/* Calculando o mutual-overlapping  */
				a = intersectG1G2.size()*1.0 / Math.min( cardG1, cardG2);
				b = intersectG1G2.size()*1.0 / Math.max( cardG1, cardG2);

				
				bw.write( a + "\t" + b + "\t");
				
				/* Verificando se a tag foi encontrada */
				if ( projSetG1 == null ) {
					bw.write( "ERRO");
					bw.newLine();
					bw.flush();
					System.err.println("#=" + tokens[0]);
					continue;
				} else if ( projSetG2 == null ){
					bw.write( "ERRO");
					bw.newLine();
					bw.flush();
					System.err.println("#=" + tokens[0]);
					continue;
				}
				
				/* descarta os casos em que ProjSetT1 é menor que ProjSetT2 */
				if ( cardG1 < cardG2) {
					bw.write( "0"); // o size de t1 é menor que t2
				    bw.write("\t" + tokens[tokens.length-1]);
					bw.newLine();
					bw.flush();
					continue;
				}
				/* O alfa e beta são thresholds para estimar o melhor parâmetro
				 * O alfa é um threshold considerado grande. o beta é um threshold considerado pequeno */
			    if ( a >= alfa){ /* a é GRANDE o suficiente */
			    	if ( b >= alfa ){ /* "a" é GRANDE o suficiente e "b" é GRANDE o suficiente */
			    		bw.write("2");
//			    		    System.out.println(tag1 + " e " + tag2 + " são EQUIVALENTES, e provavelmente não expressão uma relação de hierarquia (1)");	    		
			    	}else if (b < beta	){ /* "a" é GRANDE o suficiente e "b" é PEQUENO o suficiente */
			    		bw.write("4");
			    		contagemHyper++;
//			    		System.out.println(tag1 + " INCORPORA (SUBSUME) " + tag2 + " (3)");
			    	} else {
			    		bw.write("3");
//			    		System.out.println(tag1 + " e " + tag2 + " são RELEVANTES (2)");
			    	}
			    } else {
			    	/* Se "a" é pequeno, b vai ser <= ainda */
			    	bw.write("1");
			    }
			    bw.write("\t" + tokens[tokens.length-1]);
			    System.out.println("Progresso = " + count);
			    bw.newLine();
			    bw.flush();
				
			}
			br.close();		
			System.out.println("Total de Hyper: " + contagemHyper);
			System.out.println("......[done]");
			
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
	}
	
	/* Faz um filtro, eliminando todas as regras em que o |tag1| é menor que |tag2| 
	 * O arquivo "inputFile" tem que ter os atributos a e b 
	 * Ultima Atualizacao: 17/04/2013
	 * */
	
	public static void filterBySizeCard( String inputFile ){
		
		File fileIn  = new File( config.getProperty("drive") + config.getProperty("inputDir") + inputFile);
		String outputFile = "out-" + inputFile + "-filtered";
		File fileOut = new File( config.getProperty("drive") + config.getProperty("outputDir") + outputFile );		
		
		BufferedWriter bw = null;
		BufferedReader br = null;
		
		StringBuilder linha = new StringBuilder();
		String[] tokens = null;				
		
		int contHyper=0, progress=0, discarted=0,contDiscartedHyper=0;

		
		Set<String> cardG1;
		Set<String> cardG2;
		try {			
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			System.out.println("\n================ filterBySizeCard() ===================");			
			bw = new BufferedWriter(new OutputStreamWriter
					  (new FileOutputStream(fileOut),"UTF8"));
			
	
			// Processando
			while( br.ready()){
				linha.delete(0, linha.length());	
				linha.append(br.readLine());
				tokens = linha.toString().split("\t");
		
				
				/* % = comentario */
				if ( tokens[0].equals("%") || tokens[0].equals("#") ){
					bw.write( linha.toString());
					bw.newLine();
					bw.flush();
					continue;					
				}
							
				progress++;
				
				cardG1 = mapTagRecurso.get(tokens[1]);
				cardG2 = mapTagRecurso.get(tokens[2]);
				
				if ( cardG1.size() < cardG2.size()) {
					discarted++;
					if( tokens[tokens.length-1].equalsIgnoreCase("HYPER"))
						contDiscartedHyper++;
					continue;
				}
				
				if( tokens[tokens.length-1].equalsIgnoreCase("HYPER"))
					contHyper++;
				
				bw.write( linha.toString());
				bw.newLine();
				bw.flush();
				
				if( progress % 100 == 0 )
					System.out.println("Progresso = " + progress);
				
			}
			br.close();		
			bw.close();
			System.out.println("Total de Instancias Descartadas: " + discarted);
			System.out.println("Total de Hyper Descartados     : " + contDiscartedHyper);
			System.out.println("Total de Hyper Remanescente    : " + contHyper);
			System.out.println("......[done]");
			
		} catch (IOException ioe){
			ioe.printStackTrace();
		}

	}// fim filterBySizeCard()
	

	/* Faz um filtro, eliminando todas as regras em que o "a" e "b" de (tag1,tag2) é suficientemente
	 * pequento. 
	 * O arquivo "inputFile" tem que ter os atributos a e b.
	 * O valor de beta deve ser fornecido */
	
	public static void filterByIrrelevantRelations( String inputFile, double beta ){
		
		File fileIn  = new File( config.getProperty("drive") + config.getProperty("inputDir") + inputFile);
		String outputFile = "out-" + inputFile + "-descardedIrrelevants";
		File fileOut = new File( config.getProperty("drive") + config.getProperty("outputDir") + outputFile );		
		
		BufferedWriter bw = null;
		BufferedReader br = null;
		
		StringBuilder linha = new StringBuilder();
		String[] tokens = null;				
		
		int contHyper=0, progress=0, descarte=0;
		int ia=-1, ib=-1;
		double a, b;
		boolean foundAtt = false;
		

		try {			
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			System.out.println("\n================ filterByIrrelevantRelations() ===================");			
			bw = new BufferedWriter(new OutputStreamWriter
					  (new FileOutputStream(fileOut),"UTF8"));
			
			if( br.ready()){
				linha.append(br.readLine());
				tokens = linha.toString().split("\t");
				
				/* # = comentario */
				if ( tokens[0].equals("#") ){
					for( int i=3; i< tokens.length-1; i++){
						if ( tokens[i].equalsIgnoreCase("a"))
							ia = i;
						else if ( tokens[i].equalsIgnoreCase("b"))
							ib = i;
					}
					foundAtt = (ia>=0?true:false) && (ib>=0?true:false);
					if ( !foundAtt){
						System.err.println("atributo a ou b nao encontrado!");
						br.close();
						bw.close();
						return;
					}
					bw.write(linha.toString());
					bw.newLine();
					bw.flush();
				} else {
					System.err.println("Arquivo de cabecalho não encontrado!!!");
					bw.close();
					br.close();
					return;
				}
			} else{
				System.err.println("Problema na varredura do arquivo!");
				bw.close();
				br.close();
			}
	
			// Processando
			while( br.ready()){
				linha.delete(0, linha.length());	
				linha.append(br.readLine());
				tokens = linha.toString().split("\t");
						
				/* % = comentario */
				if ( tokens[0].equals("%") || tokens[0].equals("#") ){
					bw.write( linha.toString());
					bw.newLine();
					bw.flush();
					continue;					
				}
							
				progress++;
				
				a = Double.parseDouble( tokens[ia]);
				b = Double.parseDouble( tokens[ib]);
				
				if( a < beta && b < beta) {
					descarte++;
					continue;
				}
				
				if( tokens[tokens.length-1].equalsIgnoreCase("HYPER"))
					contHyper++;
				
				bw.write( linha.toString());
				bw.newLine();
				bw.flush();
				
				if( progress % 100 == 0 )
					System.out.println("Progresso = " + progress);
				
			}
			br.close();		
			bw.close();
			System.out.println("Total de Hyper: " + contHyper + "   Descartes: " + descarte) ;
			System.out.println("......[done]");
			
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
	}
		/* Prepara um arquivo de saida com as tags candidatas a hyperonimo, de acordo com o alfa e o beta.
		 * IMPORTANTE: 
		 * O arquivo "inputFile" tem que ter os atributos a e b.
		 * O valor de alfa e beta deve ser fornecido */
		
		public static void selectHyperCandidateRelations( String inputFile, double alfa, double beta ){
			
			File fileIn  = new File( config.getProperty("drive") + config.getProperty("inputDir") + inputFile);
			String outputFile = "out-" + inputFile + "-candidate";
			File fileOut = new File( config.getProperty("drive") + config.getProperty("outputDir") + outputFile );		
			
			BufferedWriter bw = null;
			BufferedReader br = null;
			
			StringBuilder linha = new StringBuilder();
			String[] tokens = null;				
			
			int contHyper=0, progress=0;
			int ia=-1, ib=-1;
			double a, b;
			boolean foundAtt = false;
			
			try {			
				br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
				System.out.println("\n================ selectHyperCandidateRelations() ===================");			
				bw = new BufferedWriter(new OutputStreamWriter
						  (new FileOutputStream(fileOut),"UTF8"));
				
				if( br.ready()){
					linha.append(br.readLine());
					tokens = linha.toString().split("\t");
					
					/* # = comentario */
					if ( tokens[0].equals("#") ){
						for( int i=3; i< tokens.length-1; i++){
							if ( tokens[i].equalsIgnoreCase("a"))
								ia = i;
							else if ( tokens[i].equalsIgnoreCase("b"))
								ib = i;
						}
						foundAtt = (ia>=0?true:false) && (ib>=0?true:false);
						if ( !foundAtt){
							System.err.println("atributo a ou b nao encontrado!!!");
							br.close();
							bw.close();
							return;
						}
						bw.write(linha.toString());
						bw.newLine();
						bw.flush();
					} else {
						System.err.println("Arquivo de cabecalho não encontrado!!!");
						bw.close();
						br.close();
						return;
					}
				} else{
					System.err.println("Problema na varredura do arquivo!");
					bw.close();
					br.close();
				}
		
				// Processando
				while( br.ready()){
					linha.delete(0, linha.length());	
					linha.append(br.readLine());
					tokens = linha.toString().split("\t");
							
					/* % = comentario */
					if ( tokens[0].equals("%") || tokens[0].equals("#") ){
						continue;					
					}
								
					progress++;
					
					a = Double.parseDouble( tokens[ia]);
					b = Double.parseDouble( tokens[ib]);
					
					/* O alfa e beta são thresholds para estimar o melhor parâmetro
					 * O alfa é um threshold considerado grande. o beta é um threshold considerado pequeno */
				    if ( a >= alfa){ /* a é GRANDE o suficiente */
				    	if (b < beta	){ /* "a" é GRANDE o suficiente e "b" é PEQUENO o suficiente */
				    		bw.write( linha.toString());
				    		bw.newLine();
				    		bw.flush();
				    		contHyper++;
				    	} 
				    } 
				    
				    System.out.println("Progresso = " + progress);
								
					if( progress % 100 == 0 )
						System.out.println("Progresso = " + progress);
					
				}
				br.close();		
				bw.close();
				System.out.println("Total de Hyper: " + contHyper ) ;
				System.out.println("......[done]");
				
			} catch (IOException ioe){
				ioe.printStackTrace();
			}

	} // fim filterBySmallIntersection();

	
	public static void main(String[] args) {
		init(); /* OBRIGATORIO, senao nada funciona */
//		doAnalyse("IN-Supervised_Instances_NO[02]", 0.3,0.1);
//		calculateRelationBetweenTags("protocol","tcp", 0.3, 0.1);
//		calculateRelationBetweenTags("protocol","http", 0.3, 0.1);
//		calculateRelationBetweenTags("system","calendar", 0.4, 0.2);
//		addMutualOverlapping("[4-0]-SuporteConfiancaJaccardCossenoGrauInclussao-SEQ[2]-FULL_FILE", 0.4, 0.3);
//		filterBySizeCard("SuporteConfiancaJaccardCossenoGrauInclussao-SEQ[2]-mutual");
//		filterByIrrelevantRelations("[2]-SuporteConfiancaJaccardCossenoGrauInclussao-SEQ[2]-filteredByCard", 0.1);
//		selectHyperCandidateRelations( "[3]-SuporteConfiancaJaccardCossenoGrauInclussao-SEQ[2]-descardedIrrelevants", 0.5, 0.3 );
//		reClassifyStatus("[4-3]-BASE_FILE-descardedIrrelevants", 0.6,0.5);
//		combineThreshold("[4-3]-BASE_FILE-descardedIrrelevants");
		viewStatistics("[4-1]-FULL_FILE-addedMutual",0.1);
	}

}
