package hypernym;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Scanner;
import java.math.*;

import measures.similarity.*;



public class CleanInstances {

	public static final int LOCAL = 0;
	public static final int DEFAULT = 1;
	private String readDirectory;
	private String writeDirectory;
	
	public CleanInstances(){
		readDirectory= "d:/temp/bibsonomy";
		writeDirectory = "d:/temp";
	}
	
	/*
	 * Este método analisa todos os pares de tags gerados pelo Dump final e verifica se 
	 * existem instancias em que levenshtein nao foi calculado. Para os que nao foram
	 * calculados, é gerado o valor levenshtein e atualizado em um arquivo de saída, 
	 * que tem a mesma quantidade de registros mas com todos os valores levenshtein.
	 */
	public void completeLevenshtein( int number, int directory){
		
		/* Declaracao de variaveis de apoio */
		String diretorio;
		if ( directory == LOCAL)
			diretorio = System.getProperty("user.dir");
		else
			diretorio = readDirectory;

		BufferedReader br = null;
		BufferedWriter bw = null;
		File fileIn  = new File(diretorio + "/Instances_Dump_final");
		File fileOut = new File(writeDirectory + "/Instances_dump_Lev_16122011");
		
			
		StringBuilder linha = new StringBuilder();
		String[] temp = null;
			
		int progress=0;
		double levenshtein;
		DecimalFormat decimal = new DecimalFormat( "0.0000" );
			
		if (number < 0)
			return;
			
		try {
				
			
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
				
			System.out.println("Iniciando a limpeza do arquivo...");
				
			while (br.ready()){
				progress++;
				linha.append(br.readLine()); 
				temp = linha.toString().split("\t");
					
				if (progress <= number ) {
					linha.delete(0, linha.length());
					continue;
				}
				
					
				if ( Double.parseDouble(temp[2]) == 0 ) {
					levenshtein = LevenshteinDistance.getLevenshteinDistanceNormalized(temp[0], temp[1]);
					bw.write(temp[0] + "\t" + temp[1] + "\t" + decimal.format( levenshtein).replace(',','.') + "\t" + temp[3] + "\t" +
							 temp[4] + "\t" + temp[5]);
					
				} else {
					bw.write(linha.toString());
					
				}
				bw.newLine();
				bw.flush();
				
				System.out.println("Progresso: " + progress );				
				linha.delete(0, linha.length());
			}			
			System.out.println("Operacao concluida....");
			br.close();
			bw.close();
				
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
		
	}
	
	public static void doTest(){
		DecimalFormat decimal = new DecimalFormat( "0.0000" );  
		System.out.println( decimal.format( 3.7691931233 ).replace(',', '.') );
		Double.parseDouble(decimal.format(3.7691941).replace(',', '.'));
		
//		System.out.println(Math.ceil(1.25698));
//		System.out.println(Math.floor(1.12365));
//		System.out.println(Math.round(12.654455));
	}
	
	public static void uiCountPositives( String file ){
		/* Declaracoes para arquivo de entrada */
		BufferedReader br = null;		
		File fileIn  = new File( file );
		
		int count=0, selected=0;
		StringBuilder linha = new StringBuilder();
		String[] temp = null;

		try {
			System.out.println("\nETAPA1: Contanto instâncias positivas em \"" + fileIn.getName() + "\"");
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));		
			
			/* Recuperando a 1a linha para extrair o cabecalho */
			while (br.ready()){
				linha.delete(0, linha.length());
				linha.append(br.readLine()); 
				temp = linha.toString().split("\t");
				
				if ( temp[0].equalsIgnoreCase("%") || temp[0].equalsIgnoreCase("#"))
					continue;
				else if( !temp[temp.length-1].equalsIgnoreCase("NO")) {
					selected++;
				} 
				count++;
			} 
			System.out.println("# Processados: " + count + ".  # Positives: " + selected);
			br.close();
			System.out.println("Operacao Concluida...");
		} catch (IOException ioe ){
				ioe.printStackTrace();
		}
	}

	public static void uiPutNumber( String file ){
		/* Declaracoes para arquivo de entrada */
		BufferedReader br = null;		
		File fileIn  = new File( file );
		BufferedWriter bw = null;
		File fileOut = new File( file + "-SEQ");
		
		int count=0;
		StringBuilder linha = new StringBuilder();
		String[] temp = null;

		try {
			System.out.println("\nETAPA1: Processando o arquivo \"" + fileIn.getName() + "\"");
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			
			/* Recuperando a 1a linha para extrair o cabecalho */
			while (br.ready()){
				linha.delete(0, linha.length());
				linha.append(br.readLine()); 
				temp = linha.toString().split("\t");
				
				if ( temp[0].equalsIgnoreCase("%") || temp[0].equalsIgnoreCase("#"))
					bw.write( linha.toString());
				else {
					count++;
					bw.write(count + "\t" + linha.toString());
				}
				bw.newLine();
				bw.flush();
				
			} 
			System.out.println("# Processados: " + count );
			br.close();
			bw.close();
			System.out.println("Operacao Concluida...");
		} catch (IOException ioe ){
				ioe.printStackTrace();
		}
	}
	
	/*
	 * Dado um arquivo de entrada, seleciona as instancias HYPER ou NO, de acordo com o 
	 * parametro "label". 
	 */

	public static void uiSelectInstances( String file, String label  ){
	
			    
		/* Declaracoes para arquivo de entrada */
		BufferedReader br = null;		
		BufferedWriter bw = null;
		File fileIn  = new File( file );
		String text = label.equalsIgnoreCase("HYPER")?"Positive":"Negative";
		File fileOut = new File( "g:\\temp\\hiponimos\\out-Selected" + text + "Instances");
		
		int count=0, selected=0;
		StringBuilder linha = new StringBuilder();
		String[] temp = null;

		try {
			System.out.println("\nETAPA1: Selecionando instâncias positivas em \"" + fileIn.getName() + "\"");
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));		
			
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			/* Recuperando a 1a linha para extrair o cabecalho */
			while (br.ready()){
				linha.delete(0, linha.length());
				linha.append(br.readLine()); 
				temp = linha.toString().split("\t");
				
				if ( temp[0].equalsIgnoreCase("%") || temp[0].equalsIgnoreCase("#"))
					continue;
				else if( temp[temp.length-1].equalsIgnoreCase( label )) {
					bw.write( linha.toString());
					bw.newLine();
					bw.flush();
					selected++;
				} 
				count++;
				if (selected == 2749)
					System.out.println("parou...");

			} 
			System.out.println("# Processados: " + count + ".  # Selecionados: " + selected);
			br.close();
			bw.close();
			System.out.println("Operacao Concluida...");
		} catch (IOException ioe ){
				ioe.printStackTrace();
		}
	}
	
	public static void uiFindNewRecords( String fileA, String fileB) {

		System.out.println("Identificando instancias (+) no Arquivo B que não estão em A.");
		System.out.println("Os arquivos devem estar no Basic Format.");
		System.out.println("Arquivo A (referencia):" + fileA);
		System.out.println("Arquivo B (analisado) :" + fileB);
			    
		/* Declaracoes para arquivo de entrada */
		BufferedReader br = null;		
		File fileIn  = new File( fileA );
		
						
		/* variaves de apoio */
		Hashtable<String,String> ht = new Hashtable<String,String>();
		
		int count=0;
		StringBuilder linha = new StringBuilder();
		String[] temp = null;

		try {
			System.out.print("\nETAPA1: carregando as instâncias do arquivo A: ");
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			/* Recuperando a 1a linha para extrair o cabecalho */
			while (br.ready()){
				linha.delete(0, linha.length());
				linha.append(br.readLine()); 
				temp = linha.toString().split("\t");
				
				ht.put(temp[0]+temp[1], linha.toString());					
				count++;
			} 
			br.close();
			
			System.out.print( count + "\n");
			System.out.print("ETAPA2: Veficicando os registros em B ausentes em A: ");
			fileIn  = new File( fileB );
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			/* Arquivo de Saida */
			BufferedWriter bw = null;
			File fileOut = new File( "g:\\temp\\hiponimos\\out-AbsentRecords");
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			count = 0;
			
			while (br.ready()){
				linha.delete(0, linha.length());
				linha.append(br.readLine()); 
				temp = linha.toString().split("\t");
				
				if( !ht.containsKey(temp[0]+temp[1])) {
					bw.write( linha.toString());
					bw.newLine();
					bw.flush();
					count++;
				}
			} 
			System.out.print(count + "\n");
			br.close();
			bw.close();

			System.out.println("Operacao Concluida...");
		} catch (IOException ioe ){
				ioe.printStackTrace();
		}

		
	}

	/*
	 *  Método que Filtra dados de acordo com o cabecalho dos arquivos de entrada
	 *  Referente à Opcao 1 do menu;
	 */
	public static void uiFilterData( String fileFilter ){
		Scanner in = new Scanner(System.in);

		System.out.println("\nETAPA 1 - Lendo informações do cabeçalho do arquivo...");
			    
		/* Declaracoes para arquivo de entrada */
		BufferedReader br = null;		
		File fileIn  = new File( fileFilter );
					
		/* variaves de apoio */
		int progress=0;
		StringBuilder linha = new StringBuilder();
		String[] temp = null;
		String cabecalho = "";
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			/* Recuperando a 1a linha para extrair o cabecalho */
			if (br.ready()){
				linha.delete(0, linha.length());
				linha.append(br.readLine()); 
				temp = linha.toString().split("\t");
				
				cabecalho = linha.toString();
				// Verificando a existencia da linha de cabecalho
				if ( temp[0].equals("#") ) {
					for( int i=1; i < temp.length; i++) {
						System.out.print( "[" + i + "]=" + temp[i] + " | " );
						
					}
						
				} else {
					System.err.println("ERRO 0: O arquivo não tem cabeçalho");
					br.close();
					return;
				}
					// fim-if
					
			} else {
				System.err.println("ERRO 1: Arquivo não contem linhas para extracao.");
				br.close();
				return;
			} // fim-if
			
			System.out.println("\nDigite o Filtro (Ex: suporte > 1.0): ");
			String expressao = in.nextLine();	
			
			/* Verificando a validação dos componentes da expressao */
			String atributo = getAttribute(expressao);
			
			boolean validAttribute = false;
			boolean deuErro = false;
			int index = -1;
			for( int i=1; i < temp.length; i++) {
				if ( temp[i].equalsIgnoreCase( atributo ) ) {
					validAttribute = true;
					index = i;
					break;
				}
			}

			if (!validAttribute){
				System.err.println("\nERRO 2: ====== ATRIBUTO INVALIDO ==============");
				deuErro = true;
			}
			
			String operacao = getOperacao(expressao);
			if ( operacao == null){
				System.err.println("\nERRO 3: ====== OPERADOR INVALIDO ==============");
				deuErro = true;	
			}
			
			double valor = getValue(expressao);
			if ( valor < 0 ){
				System.err.println("\nERRO 4: ========= VALOR INVALIDO ==============");
				deuErro = true;				
			}
			
			if ( deuErro){
				in.nextInt();
				System.err.println("Pressione uma tecla para retornar ao menu principal...");
				br.close();
				return;
			}
			
			BufferedWriter bw = null;
			File fileOut = new File( "g:\\temp\\hiponimos\\out-HipoHiperFilteredBy" + atributo);
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			boolean selected = false;
			int hits = 0; // quantidade de instancias selecionadas
			
			/* Adicionando o cabecalho no arquivo de escrita */
			
			if ( br.ready()){
				bw.write( cabecalho );
				bw.newLine();
				bw.flush();
			}
			
			int tHiperSel = 0, tHiperOut = 0, tNoSel = 0, tNoOut = 0; 
			while (br.ready()){
				
				linha.delete(0, linha.length());
				linha.append(br.readLine()); 
				temp = linha.toString().split("\t");
				
				if ( temp[0].equalsIgnoreCase("%") || temp[0].equalsIgnoreCase("#"))
					continue;
				
				progress++;
				selected = false;			
				
				if ( operacao.equals(">")) {
					if ( Double.parseDouble(temp[index]) > valor)
						selected = true;
				} else if ( operacao.equals(">=")){
					if ( Double.parseDouble(temp[index]) >= valor)
						selected = true;
				}else if ( operacao.equals("<")){
					if ( Double.parseDouble(temp[index]) < valor)
						selected = true;	
				}else if ( operacao.equals("<=")){
					if ( Double.parseDouble(temp[index]) <= valor)
						selected = true;	
				} else if ( operacao.equals("==")){
					if ( Double.parseDouble(temp[index]) == valor)
						selected = true;	
				}else if ( operacao.equals("!=")){
					if ( Double.parseDouble(temp[index]) != valor)
						selected = true;
				}
				if (selected){		
					bw.write( linha.toString() );
					bw.newLine();
					bw.flush();
					hits++;
					if ( temp[temp.length-1].equals("HYPER"))
						tHiperSel++;
					else
						tNoSel++;
					
				} else {
					if ( temp[temp.length-1].equals("HYPER"))
						tHiperOut++;
					else
						tNoOut++;	
				}
				
				System.out.println("Progresso: " + progress + ".    Selecionados: " + hits);
				/* Esvaziando o StringBuffer*/
				if ( progress == 115231)
					System.out.println("parou");
				
			}
			DecimalFormat decimal = new DecimalFormat( "0.0" );
			StringBuffer sb = new StringBuffer();
			sb.append("%\t=========== ESTATISTICAS ===========\n");
			sb.append("%\t# Instancias filtradas   = " + hits + "\n");
			sb.append("%\t         Total HYPER (+) = " + tHiperSel + " /" + decimal.format( ((tHiperSel/(float)hits)*100)) + "\n");
			sb.append("%\t            Total NO (-) = " + tNoSel + " /" + decimal.format(((tNoSel/(float)hits)*100)) + "\n");
			sb.append("%\t# Instancias descartadas = " + (progress - hits) + "\n");
			sb.append("%\t         Total HYPER (+) = " + tHiperOut + " /" + decimal.format(((tHiperOut/(progress-(float)hits))*100)) + "\n");
			sb.append("%\t            Total NO (-) = " + tNoOut + " /" + decimal.format(((tNoOut/(progress-(float)hits))*100)) + "\n");
			sb.append("%\t====================================\n");
			sb.append("%\tGeral:\n");
			sb.append("%\tInstancias Processadas = " + progress + "\n");
			sb.append("%\t           Hiperonimos = " + (tHiperSel + tHiperOut) + "\n");
			sb.append("%\t                    No = " + (tNoSel + tNoOut) + "\n");
			System.out.println( sb.toString());
			bw.write(sb.toString());
			bw.flush();
			br.close();
			bw.close();
			System.out.println("Operacao Concluida...");
		} catch (IOException ioe ){
				ioe.printStackTrace();
		}

	    

	    // Show save dialog
//	    fc.showSaveDialog(null);
//	    selFile = fc.getSelectedFile();
	
		
/*		System.out.print("Skip Records: ");
		number = in.nextInt();
		System.out.print("Diretorio Local (S/N)?");
		resposta = in.next();
		
		if ( resposta.equalsIgnoreCase("S"))
			dir = PutingLevenshtein.LOCAL;
		else
			dir = PutingLevenshtein.DEFAULT;

		pl.addLevenshteinSimilarity(number, dir);
		break;
*/
	} // fim-uiFilterData
	
	private static String getOperacao(String expressao){	
		
		if ( expressao.indexOf(">=") >= 0 )
			return ">=";
		else if ( expressao.indexOf("<=") >= 0 )
			return "<=";
		else if ( expressao.indexOf("!=") >= 0 )
			return "!=";
		else if ( expressao.indexOf("<") >= 0 )
			return "<";
		else if ( expressao.indexOf(">") >= 0 )
			return ">";
		else if ( expressao.indexOf("=") >= 0 )
			return "=";
		else
			return null;
	}
	
	/*
	 * Extrai o atributo de uma expressao "atributo > valor"
	 * Se houver algum problema com a expressao, o metodo retorna null
	 */
	private static String getAttribute(String expressao){
		
		for(int i=0; i< expressao.length(); i++){
			if ( !Character.isLetterOrDigit( expressao.charAt(i) ))
				return expressao.substring(0, i);
		}
		
		return null;
	}
	
	private static double getValue(String expressao){
		int i;
		char dig;
		for(i=expressao.length()-1; i>=0; i--){
			dig = expressao.charAt(i);
			if ( Character.isWhitespace( dig ) )
				continue;
			if ( Character.isLetter(dig))
				return -1;
			if ( !Character.isDigit( dig ) ){
				if ( "=<>!".indexOf(dig) < 0 ) {
					if ( !(dig == '.'))
						return -1;
				} else {
					break;
				}
			}
		}
		
		
		return Double.parseDouble( expressao.substring(i+1));
	}


	public static void main(String[] args) {
				
		Scanner in = new Scanner(System.in);
		int option;

		while ( true ) {
			System.out.println("Clean Data: ");
			System.out.println("========================");
			System.out.println("1 - Filtrar dados ");
			System.out.println("2 - Listar instãncias (+) em B que não estão em A");
			System.out.println("3 - Selecionar instâncias positivas (+)");
			System.out.println("4 - Contar instancias positivas (+)");
			System.out.println("5 - Numeração de Instancias");
			System.out.println("6 - Saída\n");
			System.out.print("[Opcao]: ");
			option = Integer.parseInt(in.nextLine());
			
			switch( option) {
			case 1:
				uiFilterData("G:\\temp\\hiponimos\\out-HipoHiperFilteredBysuporte[g2]");
				/* Saida:  out-HipoHiperFilteredBy[suporte][confianca][jaccard] */
				break;
			case 2:
				uiFindNewRecords("G:\\temp\\hiponimos\\out-SelectedPositiveInstances","G:\\temp\\hiponimos\\out-SelectedPositiveInstances [filtro]");
				/* Saida: out-AbsentRecords */
				break;
			case 3:
//				uiSelectInstances("G:\\temp\\hiponimos\\out-HipoHiperFilteredBysuporte[maior2]confianca[ge015]","HYPER");
				uiSelectInstances("G:\\temp\\hiponimos\\out-labelByConceptNet_complete","CONCEPTNET5");
				/* Saida: out-SelectedPositiveInstances */
				break;
			case 4:
				uiCountPositives("G:\\temp\\hiponimos\\TagSuporteConfiancaJaccardHiperonimoNivelUmAtualizado");
				/* Saida: */
				break; 
			case 5:
				uiPutNumber("G:\\temp\\hiponimos\\SuporteConfiancaJaccardCossenoGrauInclussaoSinonimo");
				/* Saida: */
				break; 
			case 6:
				System.exit(0);
			} // fim switch()
			
			
		} // fim while


		
		
//		CleanInstances ci = new CleanInstances();
//		ci.completeLevenshtein(0, DEFAULT);
//		doTest();

	}


}
