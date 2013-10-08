/*
 * OBS: os arquivos de instancias manipulados devem estar SEQUENCIALMENTE NUMERADOS!
 */

/**
 * Provê as classes necessárias para realizar tarefas inerentes à preparação de arquivos para
 * experimentação, utilizando aprendizagem de máquina.
 * <p>
 * Tambem estão presentes classes que realizam aprendizado de forma programática, ou seja,
 * utilizando metodoloias extraídas de artigos.
 * @see util
 */
package machine.learning;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;


import util.Config;
import util.ExcelFileHandler;
import weka.WekaExperiment;

/**
 * A classe DataMining possui uma série de funcionalidades que permite ajustar uma fonte
 * de dados (arquivos de instancias) para criar arquivos de Estimacao, Validacao e Teste,
 * inclusive com a possibilidade de convertê-los em formato .ARFF (Weka framework). 
 * @author Alex
 * @version 1.3: Classificador SVG
 * 1.2 Documetação dos métodos; Adequação dos métodos de escrita do arquivo do excel.
 * 1.1 (Remocao do main() para um arquivo externo); adicao de métodos de escrita no arquivo excel.
 */
public class DataMining {
	
	/**  Constante para indicar conjunto de Treinamento	 */
	public static final int TRAINING=1;
	/**  Constante para indicar conjunto de Teste*/
	public static final int TEST=2;
	private static int numPositives = 0; /* Numero de Instancias positivas do arquivo */
	private static int numNegatives = 0; /* Numero de Instancias negativas do arquivo */
	/**  Objeto que contém as definições de configuração de uso da classe */
	public static Config config = null;
	
	/**
	 *  Carrega as configurações definidas em um arquivo de propriedades, da forma
	 *  chave/valor.
	 * @param fileName o nome do arquivo de propriedades
	 */
	public static void loadConfig( String fileName ){
		try {
			config = new Config( fileName );
		
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	
	/**
	 * Obtem valores para as chaves do arquivo de propriedades.
	 * @param property O nome da chave
	 * @return Retorna o valor referente à chave
	 */
	public static String configGetProperty( String property){
		return config.getProperty( property );
	}
	
	
	/**
	 * Método que retorna o indice de um campo fornecido como argumento, dentro da linha
	 * de cabecalho
	 * @param headLine A linha de cabecalhho
	 * @param field O nome do campo
	 * @return o indice do cabecalho ou -1 caso nao esteja presente
	 */
	public static int getIndexAttribute(String headLine, String field){
		String tokens[];
		
		// Montando a lista de atributos atraves do cabecalho
		tokens = headLine.split("\t");
		/* descarta o id, tag1, tag2 e rotulo */
		for(int i = 0; i < tokens.length; i++ ){
			if( tokens[i].equalsIgnoreCase( field ))
					return i;
		}
		return -1;
	}
	
	
	/**
	 *  Metodo que apenas pega informacoes sobre o numero de instancias POSITIVAS e NEGATIVAS do 
	 *  arquivo passado como argumento. As informacoes coletadas sao guardadas nas propriedades de classe
	 *  numPositives e numNegatives.
	 * @param fileInstance - O arquivo a ser analisado.
	 */
	private static void getStatistics( String fileInstance ){
		File fileIn  = new File( config.getProperty("drive" ) + config.getProperty("inputDir" ) + fileInstance);
		
		BufferedReader br = null;
				
		StringBuilder linha = new StringBuilder();
		String[] tokens = null;
				

		try {			
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			numNegatives = numPositives = 0;
			while( br.ready()){
				linha.delete(0, linha.length());	
				linha.append(br.readLine());
				tokens = linha.toString().split("\t");
				
				/* # = cabecalho     % = comentario */
				if( tokens[0].equals("#") || tokens[0].equals("%"))
					continue;
				else if( tokens[tokens.length-1].equals( config.getProperty("negativeLabel" )))
					numNegatives++;
				else if( tokens[tokens.length-1].equals( config.getProperty("positiveLabel" )))
					numPositives++;
			}
			br.close();
		} catch (IOException ioe ){
			ioe.printStackTrace();
		}
	}
	


	/**
	 * Metodo privado que imprime o cabecalho do formato ARFF
	 * @param typeFile: um inteiro que representa TRAINING ou TEST
	 * @param bw      : o BufferedWriter de escrita
	 * @param att     : a lista de atributos para preparacao do cabecalho
	 * @throws IOException: Excecao que pode ser gerada caso haja falha na manipulacao do arquivo
	 */
	private static void headARFF(int typeFile, BufferedWriter bw, String att[], int matrix[]) throws IOException {
		bw.write("% ARFF File for the Bibsonomy data");
		bw.newLine();
		bw.write("%");
		bw.newLine();
		
		if ( typeFile == TRAINING) {
			bw.write("@relation data-training"); bw.newLine();
		} else {
			bw.write("@relation data-test"); bw.newLine();
		}
		
		bw.newLine();
		
		for( int i=0; i< att.length; i++){
			if ( matrix[i] == 1){
				bw.write("@attribute " + att[i] + " numeric");
				bw.newLine();
			}
		}
		bw.write("@attribute label { " + config.getProperty("positiveLabel") + ", " + config.getProperty("negativeLabel") +" }");
		bw.newLine();
		bw.write("%");
		bw.newLine();
		bw.write("@data"); 
		bw.newLine();
		bw.write("%");
		bw.newLine();

		bw.flush();
	}

	/**
	 * Metodo que procura pelo cabecalho na 1a linha de um arquivo de instancias. Se encontrar, retorna
	 * a linha de cabecalho. Se nao encontrar, retorna null.
	 * @param br : o BufferedReader relativo ao arquivo pelo qual se procura o cabecalho
	 * @return   : A linha de cabecalho (sucesso) ou null (falha).
	 * @throws IOException: Excecao que pode ser gerada caso haja falha na manipulacao do arquivo
	 */
	private static String extractHeader( BufferedReader br ) throws IOException{
		StringBuffer linha = new StringBuffer();
		String[] tokens = null;
		/* extraindo a linha de cabecalho */
		if ( br.ready()) {
			linha.append(br.readLine());
			tokens = linha.toString().split("\t");
			if( !tokens[0].equals("#") ) {
				return null;
			} else
				return linha.toString();
		}
		return null;

	}
	
	/**
	 * Metodo que seleciona apenas as instancias rotuladas com um "label" especifico
	 * @param file : o arquivo que contem as instancias a serem separadas
	 * @param label: o "rotulo" pelo qual se deseja extrair do parametro "file"
	 * @return - Retorna o nome do arquivo gerado:
	 *           outSelectedPositiveInstances
	 *           outSelectedNegativeInstances
	 */
	public static String selectInstances( String file, String label  ){
		/* Declaracoes para arquivo de entrada */
		BufferedReader br = null;		
		BufferedWriter bw = null;
		File fileIn  = new File( config.getProperty("drive") + config.getProperty("inputDir") + file );
		String text = label.equalsIgnoreCase( config.getProperty("negativeLabel") )?"Negative":"Positive";
		File fileOut = new File( config.getProperty("drive") + config.getProperty("inputDir") + "out-Selected" + text + "Instances");
		
		int selected=0;
		StringBuilder linha = new StringBuilder();
		String[] tokens = null;

		try {
			System.out.println("\n============== SELECT INSTANCES ===================");
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));		
			
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			
			if ( br.ready()) {
				linha.append(br.readLine());
				tokens = linha.toString().split("\t");
				if( !tokens[0].equals("#") ) {
					System.err.println("\nArquivo nao contem linha de CABECALHO!");
					br.close();
					bw.close();
					return null;
				} else {
					bw.write( linha.toString());
					bw.newLine();
					bw.flush();
				}
			} else {
				System.err.println("\nErro na preparacao da varredura do arquivo!");
				br.close();
				bw.close();
			}
			
			while (br.ready()){
				linha.delete(0, linha.length());
				linha.append(br.readLine()); 
				tokens = linha.toString().split("\t");
				
				if ( tokens[0].equalsIgnoreCase("%") || tokens[0].equalsIgnoreCase("#"))
					continue;
				else if( tokens[tokens.length-1].equalsIgnoreCase( label )) {
					bw.write( linha.toString());
					bw.newLine();
					bw.flush();
					selected++;
				} 

			} 
			br.close();
			bw.close();
			System.out.println("\tTotal de Instancias " + text.toUpperCase() + " Selecionadas = " + selected);
			return fileOut.getName();
		} catch (IOException ioe ){
				ioe.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Metodo que seleciona apenas as instancias rotuladas com um "label" especifico
	 * @param file O arquivo que contem as instancias a serem separadas
	 * @param att O nome do atributo
	 * @param condiction A condicao de filtro. Pode ser gt (greater than), lt, g, l, eq e neq
	 * @param threshold O valor utilizado como filtro
	 * @return - Retorna o nome do arquivo gerado:
	 *           outSelectedPositiveInstances
	 *           outSelectedNegativeInstances
	 * OBS: a condicao do filtro tem que ser modificada dentro do código, pois agora só seleciona
	 *      os valores que são maiores que 0 (zero).     
	 */

	public static String filterInstancesByAttribute( String file, String att, String condiction, double threshold  ){
		/* Declaracoes para arquivo de entrada */
		BufferedReader br = null;		
		BufferedWriter bw = null;
		File fileIn  = new File( config.getProperty("drive") + config.getProperty("inputDir") + file );
		String text = "out-filterBy" + att;
		File fileOut = new File( config.getProperty("drive") + config.getProperty("inputDir") +  text );
		
		int selected=0, processed=0, positive=0, negative=0;
		int index=0; // indice do atributo selecionado
		StringBuilder linha = new StringBuilder();
		String[] tokens = null;

		try {
			System.out.println("\n============== filterInstancesByAttribute() ===================");
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));		
			
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			
			// Verificando a existência de linha de cabecalho e validade do atributo
			// passado por argumento
			if ( br.ready()) {
				linha.append(br.readLine());
				tokens = linha.toString().split("\t");
				if( !tokens[0].equals("#") ) {
					System.err.println("\nArquivo nao contem linha de CABECALHO!");
					br.close();
					bw.close();
					return null;
				} else {
					index = getIndexAttribute(linha.toString(), att);
					
					if( index < 0 ){
						System.err.println("\nAtributo nao encontrado na linha de CABECALHO!");
						br.close();
						bw.close();
						return null;
					}

					bw.write( linha.toString());
					bw.newLine();
					bw.flush();
				}
			} else {
				System.err.println("\nErro na preparacao da varredura do arquivo!");
				br.close();
				bw.close();
				return null;
			}
			
			boolean canProcess;
			while (br.ready()){
				linha.delete(0, linha.length());
				linha.append(br.readLine()); 
				tokens = linha.toString().split("\t");
				
				if ( tokens[0].equalsIgnoreCase("%") || tokens[0].equalsIgnoreCase("#"))
					continue;
				
				
				canProcess = false;
				
				if( condiction.equalsIgnoreCase("geq")) {
					if( Double.parseDouble(tokens[index]) >= threshold )
						canProcess = true;
				} else if ( condiction.equalsIgnoreCase("leq")) {
					if( Double.parseDouble(tokens[index]) <= threshold )
						canProcess = true;			
				} else if ( condiction.equalsIgnoreCase("g")) {
					if( Double.parseDouble(tokens[index]) > threshold )
						canProcess = true;			
				} else if ( condiction.equalsIgnoreCase("l")) {
					if( Double.parseDouble(tokens[index]) < threshold )
						canProcess = true;			
				} else if ( condiction.equalsIgnoreCase("eq")) {
					if( Double.parseDouble(tokens[index]) == threshold )
						canProcess = true;			
				} else if ( condiction.equalsIgnoreCase("neq")) {
					if( Double.parseDouble(tokens[index]) != threshold )
						canProcess = true;			
				} else {
					System.err.println("\nCondicao invalida!");
					br.close();
					bw.close();
					return null;
				}
					
				if( canProcess ) {
					bw.write( linha.toString());
					bw.newLine();
					bw.flush();
					selected++;
					if( tokens[tokens.length-1].equalsIgnoreCase( config.getProperty("positiveLabel"))  )
						positive++;
					else
						negative++;
				} 
				processed++;

			} 
			
			DecimalFormat decimal = new DecimalFormat("0.0000");
			bw.newLine();
			bw.write( "% Total de Instancias processadas : " + processed);
			bw.newLine();
			bw.write( "% Total de Instancias selecionadas: " + selected);
			bw.newLine();
			bw.write( "% Total de Instancias positivas selecionadas: " + positive + " = " + decimal.format( (1.0*positive/selected)*100 ) + "%");
			bw.newLine();
			bw.write( "% Total de Instancias negativas selecionadas: " + negative + " = " + decimal.format( (1.0*negative/selected)*100 ) + "%");
			bw.newLine();
			bw.flush();
			
			br.close();
			bw.close();
			System.out.println("Total de Instancias processadas : " + processed);
			System.out.println("Total de Instancias selecionadas: " + selected);
			System.out.println("Total de Instancias positivas selecionadas: " + positive + " = " + decimal.format( (1.0*positive/selected)*100 )  + "%");
			System.out.println("Total de Instancias negativas selecionadas: " + negative + " = " + decimal.format( (1.0*negative/selected)*100 )  + "%");
			return fileOut.getName();
		} catch (IOException ioe ){
				ioe.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param fileOne: nome do primeiro arquivo
	 * @param fileTwo: nome do segundo arquivo
	 * @return o nome do arquivo de saida
	 */
	/**
	 * Metodo que concatena dois arquivos.
	 * @param fileOne: nome do primeiro arquivo
	 * @param fileTwo: nome do segundo arquivo
	 * @param newNameFile: nome (prefixo) sugerido para o arquivo de saida. 
	 * @return - O nome do arquivo gerado, contendo o prefixo + quantidade de instancias
	 *           Ex: prefix= Treino-       saida= Treino-1045
	 */
	public static String joinFiles( String fileOne, String fileTwo, String newNameFile ){
			/* Declaracoes para arquivo de entrada */
			BufferedReader br = null;		
			BufferedWriter bw = null;
			
			File fileIn  = new File( config.getProperty("drive") + config.getProperty("inputDir") + fileOne );
			File fileOut = new File( config.getProperty("drive") + config.getProperty("outputDir") + newNameFile );
			
			int total=0;
			StringBuilder linha = new StringBuilder();
			String[] tokens = null;

			try {
				
				br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));			
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
				
				String cabecalho = extractHeader(br);
				if ( cabecalho != null ) {
					bw.write( cabecalho );
					bw.newLine();
					bw.flush();
				} else {
					System.err.println("\nArquivo " + fileIn.getName() + " nao contem cabecalho.");
					br.close();
					bw.close();
					return null;
				}
				
				while (br.ready()){
					linha.delete(0, linha.length());
					linha.append(br.readLine()); 
					tokens = linha.toString().split("\t");
					
					if ( tokens[0].equalsIgnoreCase("%") || tokens[0].equalsIgnoreCase("#"))
						continue;

					bw.write( linha.toString());
					bw.newLine();
					bw.flush();
					total++;
				} 
				br.close();
				
				/* juntando o segundo arquivo */
				fileIn  = new File( config.getProperty("drive") + config.getProperty("inputDir") + fileTwo );
				br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));			

				while (br.ready()){
					linha.delete(0, linha.length());
					linha.append(br.readLine()); 
					tokens = linha.toString().split("\t");
					
					if ( tokens[0].equalsIgnoreCase("%") || tokens[0].equalsIgnoreCase("#"))
						continue;

					bw.write( linha.toString());
					bw.newLine();
					bw.flush();
					total++;
				} 
				br.close();
				bw.close();
				/* renomeando o arquivo de saida */
				File destino = new File( config.getProperty("drive") + config.getProperty("outputDir") + newNameFile + total);
				
				destino.delete();
				
				if ( !fileOut.renameTo( destino ) )
						System.err.println("\nNao foi possivel renomear para o arquivo " + destino.getName() + ". Verifique o problema");
				
				System.out.println("\tTotal de Instancias unidas = " + total);

				return destino.getName();
				
			} catch (IOException ioe ){
					ioe.printStackTrace();
			}
			return null;
		}

	
	/**
	 * Metodo que recebe um arquivo de REFERENCIA, um arquivo de TREINO, e gera um arquivo contendo todas as instancias
	 * da REFERENCIA que nao foram usadas para treino, ou seja, selecionadas para TESTE. Nenhuma instancia ficará de fora. 
	 * @param sourceFile  : arquivo geral de instancias
	 * @param trainingFile: arquivo de treino
	 * @param outFileName : prefixo para o nome do arquivo de saida.
	 * @return - O nome do arquivo gerado, contendo o prefixo + quantidade de instancias
	 *           Ex: prefix= Teste-       saida= Teste-500 
	 */
	public static String extractRemainingInstancesForTest( String sourceFile , String trainingFile, String outFileName ){
			
			File fileIn  = new File( config.getProperty("drive") + config.getProperty("inputDir") + trainingFile);
			
			BufferedWriter bw = null;
			BufferedReader br = null;
			
			StringBuilder linha = new StringBuilder();
			String[] tokens = null;
					
			HashSet<String> hs = new HashSet<String>();
			int count = 0;

			try {
					
				br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
				System.out.println("\n============== EXTRACT REMAINING INSTANCES ===================");
				System.out.println("ETAPA1: Carregando as instancias de TREINO no HashSet()....");					
				// Colocando todas as instancias positivas ou negativas do training no hashset
				
				while( br.ready()){
					linha.delete(0, linha.length());	
					linha.append(br.readLine());
					tokens = linha.toString().split("\t");
					
					/* # = cabecalho     % = comentario */
					if( tokens[0].equals("#") || tokens[0].equals("%"))
						continue;
								
					hs.add( tokens[0]); /* Adiciona o id da instancia */
					count++;
				}
				br.close();
				
				System.out.println("\t# Instancias carregadas: " + count);
				
				System.out.println("ETAPA2: Selecionando as instancias restantes para TESTE....");
				
				fileIn  = new File( config.getProperty("drive") + config.getProperty("inputDir") + sourceFile );
				
				File fileOut = new File( config.getProperty("drive") + config.getProperty("inputDir") + outFileName);	
					
				br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
				bw = new BufferedWriter(new OutputStreamWriter
						  (new FileOutputStream(fileOut),"UTF8"));
				
				count = 0;
				
				String cabecalho = extractHeader(br);
				if ( cabecalho != null ) {
					bw.write( cabecalho );
					bw.newLine();
					bw.flush();
				} else {
					System.err.println("\nArquivo " + fileIn.getName() + " em  nao contem cabecalho.");
					br.close();
					bw.close();
					return null;
				}

				// Deixando apenas as instancias que nao foram para o conjunto de treinamento.
				// Colocando todas as instancias positivas ou negativas no hashset
				while( br.ready()){
					linha.delete(0, linha.length());	
					linha.append(br.readLine());
					tokens = linha.toString().split("\t");
					
					/* # = cabecalho     % = comentario */
					if( tokens[0].equals("#") || tokens[0].equals("%"))
						continue;

					if ( hs.contains( tokens[0]) ) {
						continue;
					}
					count++;
					
					if ( count % 50 == 0)
						System.out.println("\tProgresso: " + count);
					
					bw.write( linha.toString());
					bw.newLine();
					bw.flush();
				}		
				br.close();
				bw.close();

				System.out.print("[DONE]");
				System.out.print("Tamanho do arquivo gerado: " + count + "\n\n");
				
				/* renomeando o arquivo de saida */
				File newFile = new File( config.getProperty("drive") + config.getProperty("outputDir") + outFileName + count);
				
				newFile.delete();
				
				if ( !fileOut.renameTo( newFile ) )
					System.err.println("\nNao foi possivel renomear para o arquivo " + newFile.getName() + ". Verifique o problema");
				
				return newFile.getName();
		
			} catch (IOException ioe){
				ioe.printStackTrace();
			}			
			return null;
		}

	/**
	 * Converte um arquivo BASIC_FORMAT com ID de instancia para o formato ARFF
	 * @param inputFile o arquivo que contem o conjunto de instancias em BASIC FORMAT, para confersao
	 * @param typeFile A constante TRAINING or TEST (necessario para definir o cabecalho)
	 * @param validAttribute Um array contendo os atributos que terao seus valores conservados
	 *      quando o arquivo ARFF for gerado. Se um determinado atributo não for encontrado,
	 *      seu valor será "0.0000"
	 * OBS: o método gera automaticamente o arquivo "inputFile" + ".arff" no diretorio de escrita
	 * criacao..: 07/03/2012
	 * alteracao: 23/03/2013
	 */
	public static void makeARFF(String inputFile, int typeFile, String[] validAttribute){
		/* Declaracoes para arquivo de entrada e saida */
		BufferedReader brOne = null;
		
		//System.out.println( inputFile + " = " + inputFile.indexOf(':'));
		
		String auxiliarPath = "";
		if ( inputFile.indexOf(':') < 0 )
			auxiliarPath = config.getProperty("drive") + config.getProperty("inputDir");
		
		
		File fileOne = new File( auxiliarPath + inputFile);
		
		BufferedWriter bw = null;
		File fileOut = new File( fileOne.getParent() + "/" + fileOne.getName() + ".arff");
		
		/* variaves de apoio */
		int count=0;
		StringBuilder linha = new StringBuilder();
		String[] temp = null;

		// matriz que controla os itens que serao considerados
		int matrix[] = null; // {0,0,0,0,...};
		String[] measure = null;
		
		try {
			brOne = new BufferedReader(new InputStreamReader(new FileInputStream( fileOne ), "UTF8"));
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			
			String cabecalho = extractHeader(brOne);
			if ( cabecalho == null ) {
				System.err.println("\nArquivo " + fileOne.getName() + " em " + "makeARFF() nao contem cabecalho.");
				brOne.close();
				bw.close();
				System.exit(0);
			} else {
				// Montando a lista de atributos atraves do cabecalho
				temp = cabecalho.split("\t");
				measure = new String[temp.length-4];
				matrix = new int[temp.length-4];
				/* descarta o id, tag1, tag2 e rotulo */
				for(int i = 3, j=0 ; i < temp.length - 1; i++, j++ ){
					measure[j] = temp[i];
				}
			}
			
			
			
			// Verificando quais foram os atributos repassadas por parâmetro
			for (int i = 0; i < measure.length; i++) {
				for (int j = 0; j < validAttribute.length; j++) {
					if ( measure[i].equalsIgnoreCase( validAttribute[j])) {
						matrix[i] = 1;
						break;
					}
				}
			}
			
			headARFF( typeFile, bw, measure, matrix );
			
			StringBuffer instancia = new StringBuffer();
			while (brOne.ready()){
				count++;
				linha.append(brOne.readLine());		
				temp = linha.toString().split("\t");
				
				if( temp[0].equals("#") || temp[0].equals("%"))
					continue;
				
				instancia.delete(0, instancia.length());
				for( int i = 0; i < matrix.length; i++ ){
					if ( matrix[i]==1 ) {
						instancia.append( temp[i+3].trim());
					    instancia.append(", ");
					}
//					else
//						instancia.append("0.0000");
					

				}
				instancia.append( temp[temp.length-1]);
				
				bw.write( instancia.toString() );

				bw.newLine();
				bw.flush();
				
				System.out.println("Progresso: " + count);
				/* Esvaziando o StringBuffer*/
				linha.delete(0, linha.length());
			}
			brOne.close();
			bw.close();
			System.out.println("Operacao Concluida...");
		} catch (IOException ioe ){
			ioe.printStackTrace();
		}		
	}
	
	/**
	 * Para uma sequencia de atributos, informar apenas os que deseja considerar para
	 * o arquivo em questao. Os atributos sao extraidos do cabecalho do arquivo.
	 * @param inputFile O nome do arquivo de instäncias em BASIC FORMAT
	 * OBS: gera automaticamente na saída o arquivo "inputFile" + "-cuttedAtt" no diretório
	 * de escrita.
	 * Data Alteracao: 15/04/2013
	 */
	public static void cutAttribute(String inputFile){
		
		String auxiliarPath = "";
		if ( inputFile.indexOf(':') < 0 )
			auxiliarPath = config.getProperty("drive") + config.getProperty("inputDir");
		
		/* Declaracoes para arquivo de entrada e saida */
		BufferedReader brOne = null;
		File fileOne = new File( auxiliarPath + inputFile);
		
		BufferedWriter bw = null;
		File fileOut = new File( fileOne.getParent() + "/" + fileOne.getName() + "-cuttedAtt");
		
		/* variaves de apoio */
		int count=0;
		StringBuilder linha = new StringBuilder();
		String[] tokens = null;

		// matriz que controla os itens que serao considerados
		int matrix[] = null; // {0,0,0,0,...};
		String[] measure = null;
		Scanner in = new Scanner(System.in);
		try {
			brOne = new BufferedReader(new InputStreamReader(new FileInputStream( fileOne ), "UTF8"));
			
			String cabecalho = extractHeader(brOne);
			if ( cabecalho == null ) {
				System.err.println("\nArquivo " + fileOne.getName() + " nao contem cabecalho.");
				brOne.close();
				System.exit(0);
			} else {
				// Montando a lista de atributos atraves do cabecalho
				tokens = cabecalho.split("\t");
				measure = new String[tokens.length-4];
				matrix = new int[tokens.length-4];
				/* descarta o id, tag1, tag2 e rotulo */
				for(int i = 3, j=0 ; i < tokens.length - 1; i++, j++ ){
					measure[j] = tokens[i];
				}
			}
			
			/* Perguntando quais atributos deseja selecionar */
			for( int i=0; i<measure.length;i++){
				System.out.print("["+i+"]" + measure[i]+"  ");
			}
			System.out.print("\nDigite o numero de cada atributo que deseja manter, separado por \",\" (ENTER para abandonar): ");
			String sequence = in.nextLine();
			
			if ( sequence.length() == 0 )
				return;
			
			String[] validAttribute = sequence.split(",");
			
			// Verificando quais foram os atributos repassadas por parâmetro
			boolean ok = false;
			for (int j = 0; j < validAttribute.length; j++) {
			   if ( Integer.parseInt(validAttribute[j]) >=0 &&  Integer.parseInt(validAttribute[j]) < measure.length) {
			      matrix[ Integer.parseInt(validAttribute[j])] = 1;
			      ok = true;
  			   }
			}
			
			if( !ok ){
				System.err.println("Voce nao forneceu ao menos 1 unico numero valido.");
				return;
			}
			
			/* ---------------- Definindo o novo cabecalho ------------  */
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));

			bw.write("#\ttag1\ttag2");
			for( int i=0; i < matrix.length; i++ ){
				if( matrix[i]==1) {
					bw.write("\t"+measure[i]);
				}				
			}
			bw.write("\tlabel");
			bw.newLine();
			bw.flush();
			/* -------------------------------------------------------- */
			
			StringBuffer instancia = new StringBuffer();
			while (brOne.ready()){
				count++;
				linha.delete(0, linha.length());
				linha.append(brOne.readLine());		
				tokens = linha.toString().split("\t");
				
				if( tokens[0].equals("#") || tokens[0].equals("%"))
					continue;
				
				bw.write(tokens[0] + "\t" + tokens[1] + "\t" + tokens[2] + "\t");
				instancia.delete(0, instancia.length());
				for( int i = 0; i < matrix.length; i++ ){
					if ( matrix[i]==1 ) {
						instancia.append( tokens[i+3].trim());
					    instancia.append("\t");
					}
				}
				instancia.append( tokens[tokens.length-1]);
				
				bw.write( instancia.toString() );
				bw.newLine();
				bw.flush();
				
				if ( count % 100 == 0 )
					System.out.println("Progresso: " + count);
			}
			brOne.close();
			bw.close();
			System.out.println("Operacao Concluida...");
		} catch (IOException ioe ){
			ioe.printStackTrace();
		}		
	}

	
	/**
	 * Sorteia randomicamente as instancias que vao fazer parte de um conjunto de TREINO
	 * ou TESTE (mais provavel para treino)
	 * @param quant o numero de instancias que gostaria de gerar
	 * @param inputFile o arquivo que contem as instancias a serem escolhidas. Neste arquivo,
	 *      cada linha deve conter o numero do registro, tags relacionadas, atributos e classe. Pode
	 *      ser um arquivo apenas com instâncias (-), (+) ou ( + - ).
	 * @return Um string com o nome do arquivo que contem as instâncias selecionadas automaticamente
	 * OBS: gera automaticamente o arquivo inputFile+"-RandomFile-"+quant no diretorio de saída. 
	 *      Esse arquivo pode ser tanto para classificacoes "yes" quanto "no".
	 *      Esse arquivo pode ser descartado a qualquer momento, pois é muito simples
	 *      gerar outro.
	 * DATA:
	 * 	criacao..: 19/02/2012
	 * 	alteracao: 25/02/2012
	 */
	public static String createRandomFile( long quant, String inputFile ){
		
		String outputFile;
		File fileIn  = new File( config.getProperty("drive") + config.getProperty("inputDir") + inputFile);
		
		/* Nome do arquivo randomico  */
		outputFile = inputFile + "-RandomFile-" + quant;
		File fileOut = new File( config.getProperty("drive") + config.getProperty("outputDir") + outputFile );		
		
		BufferedWriter bw = null;
		BufferedReader br = null;
		
		
		StringBuilder linha = new StringBuilder();
		String[] tokens = null;
				
		HashMap<String,String> hm = new HashMap<String,String>();

		Random randomGenerator = new Random();
		int count = 0;
		String cabecalho=null;

		try {			
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			System.out.println("\n============== CREATE RANDOM FILE ===================");
			System.out.print("ETAPA1: Carregando as instancias no Hashtag()....");
			
			cabecalho = extractHeader(br);
			if ( cabecalho == null ) {
				System.err.println("\nArquivo " + fileIn.getName() + " nao contem cabecalho.");
				br.close();
				return null;
			}
			
			// Colocando todas as instancias positivas ou negativas no hashset
			while( br.ready()){
				linha.delete(0, linha.length());	
				linha.append(br.readLine());
				tokens = linha.toString().split("\t");
				
				/* # = cabecalho     % = comentario */
				if( tokens[0].equals("#") || tokens[0].equals("%"))
					continue;
				
				count++;
				hm.put( String.valueOf(count), linha.toString());

			}
			br.close();
			System.out.print(" [done]\n");
			System.out.print("\tInstancias carregadas....: " + hm.size());
			
			System.out.print("\nETAPA2: Sorteando as instancias....\n");
			bw = new BufferedWriter(new OutputStreamWriter
					  (new FileOutputStream(fileOut),"UTF8"));
			
			// gerando instancias randomicamente
			int randomInt = 0;
			int totalInstances = hm.size();
			
			/* Adicionando o cabecalho */
			bw.write(cabecalho);
			bw.newLine();
			bw.flush();
			
			for( count=1; count <= quant; count++){
				randomInt = randomGenerator.nextInt( totalInstances ) + 1;
				
				if ( !hm.containsKey( String.valueOf( randomInt ))){
					count--;
					continue;
				}
				
				bw.write( hm.remove( String.valueOf( randomInt )));
				bw.newLine();
				bw.flush();
				
				//totalInstances = hm.size();
				
				if ( count % 50 == 0)
					System.out.println("\tProgresso: " + count);
			}
			br.close();
			bw.close();
			
			System.out.println("\tTamanho do arquivo gerado: " + (--count) );
			System.out.println("\t[DONE]");
			
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
		return outputFile;
		
	}
	

	/**
	*   OBJETIVO: 
	*   Este método é responsável por criar randomicamente o arquivo de TREINO a partir de um
	*   arquivo BASIC FORMAT passado como argumento. O arquivo passado como argumento pode ter
	*   instancias POSITIVAS e NEGATIVAS (preferencialmente os dois).
 	* 	@param iFile: o arquivo BASIC FORMAT (classificacao binaria) usado como referencia pra
 	*                 criar o arquivo de treino.
 	*   OBSERVACAO:
 	*   Como o dataset é desbalanceado, leva-se em consideração a proporção exata de 2/3 de positivas
 	*   para treino e 1/3 de positivas para teste. Da mesma forma, para as instâncias negativas
 	*/
	public static String createRandomTrainingData( String iFile, String nameOutputFile ){	
	
		long numInstances;
		String returnedFileOne, returnedFileTwo;

		getStatistics( iFile );
		/* Criando o arquivo de Treino com instancias positivas */
		String filePositives = selectInstances( iFile, config.getProperty("positiveLabel"));
		numInstances = Math.round( (numPositives / 3.0)*2 );
		returnedFileOne = createRandomFile( numInstances, filePositives);
		
		/* Criando o arquivo de Treino com instancias positivas */
		String fileNegatives = selectInstances( iFile, config.getProperty("negativeLabel"));
		numInstances = Math.round( (numNegatives / 3.0)*2 );
		returnedFileTwo = createRandomFile( numInstances, fileNegatives);
		
		/* juntando os dois arquivos */
		String returnFile =  joinFiles( returnedFileOne, returnedFileTwo, nameOutputFile );
		
		makeARFF( returnFile , TRAINING, config.getProperty("attList").split("\t"));
		
		/* apagando arquivos temporarios */
		File file = new File( config.getProperty("drive") + config.getProperty("outputDir") + returnedFileOne);
		file.delete();
		file = new File( config.getProperty("drive") + config.getProperty("outputDir") + returnedFileTwo);
		file.delete();
		return returnFile;
		
		
	}
	
	/**
	 * Metodo que recebe um arquivo de TREINO e "separa" em TREINO E VALIDACAO
	 * @param inputFile: o arquivo de treino (gerado com createTrainingData() )
	 * @param prefix   : o prefix para o nome do arquivo de saida. O arquivo de saida tera o sufixo
	 *                   TR- ou VL-, e a respectiva quantidade de instancias
	 */
	public static void createValidationData( String inputFile , String prefix ){
		long numInstances;
		String returnedFileOne, returnedFileTwo;

		getStatistics( inputFile );
		/* Criando o arquivo de Treino com instancias positivas */
		String filePositives = selectInstances( inputFile, config.getProperty("positiveLabel"));
		numInstances = Math.round( (numPositives / 3.0)*2 );
		returnedFileOne = createRandomFile( numInstances, filePositives);
		
		/* Criando o arquivo de Treino com instancias negativas */
		String fileNegatives = selectInstances( inputFile, config.getProperty("negativeLabel"));
		numInstances = Math.round( (numNegatives / 3.0)*2 );
		returnedFileTwo = createRandomFile( numInstances, fileNegatives);
		
		/* juntando os dois arquivos */
		String returnFile =  joinFiles( returnedFileOne, returnedFileTwo, prefix + "EST-");
		
		makeARFF( returnFile , TRAINING, config.getProperty("attList").split("\t"));
		
		/* apagando arquivos temporarios */
		File file = new File( config.getProperty("drive") + config.getProperty("outputDir") + returnedFileOne);
		file.delete();
		file = new File( config.getProperty("drive") + config.getProperty("outputDir") + returnedFileTwo);
		file.delete();
		System.out.println("\nArquivo gerado para TREINO: " + returnFile );
		
		String validFile = extractRemainingInstancesForTest( inputFile, returnFile, prefix + "VAL-" );
		
		makeARFF( validFile , TEST, config.getProperty("attList").split("\t"));
	}


	/**
	 * Cria os arquivos de partições de forma automática. Os arquivos de partições são identificados
	 * pelos prefixos [10-90],[20-80],[30-70],[40-60] e [50-50]. O cálculo da proporção de instâncias
	 * positivas e negativas é feito automaticamente, e depende da quantidade de instâncias positivas.
	 * @param typePartition A constante de classe TRAINING ou TEST
	 * @param positiveFile Nome do arquivo que contém apenas as instâncias positivas
	 * @param negativeFile Nome do arquivo que contém apenas as instäncias negativas
	 * 
	 * Arquivos de Saida (em formato ARFF e BASIC FILE):
	 * (1)[10-90]-FULL-21040       (2) [10-90]-TREINO-14027     (3) [10-90]-TESTE-7013
	 * (4)[10-90]-EST-9351         (5) [10-90]-VAL-4676 
	 */
	public static void createPartition(int typePartition, String positiveFile, String negativeFile) {
		String nameFile[] = {"","[10-90]","[20-80]", "[30-70]", "[40-60]", "[50-50]"};
		int numInstances;
		
		getStatistics( config.getProperty("filePositive") );
				
		if ( typePartition == 1) //10-90
			numInstances = Math.round( ( numPositives * 90)/10 );
		else if ( typePartition == 2) //20-80
			numInstances = Math.round( ( numPositives * 80)/20 );		
		else if ( typePartition == 3) //30-70
			numInstances = Math.round( ( numPositives * 70)/30 );
		else if ( typePartition == 4) //40-60
			numInstances = Math.round( ( numPositives * 60)/40 );
		else  //50-50
			numInstances = Math.round( ( numPositives * 50)/50 );
		
		
		/* Criando o arquivo de Treino com instancias positivas */
		String posFile = selectInstances( positiveFile, config.getProperty("positiveLabel"));
		/* Criando o arquivo de Teste com instancias negativas */
		String negFile = createRandomFile( numInstances, negativeFile);
		
		/* juntando os dois arquivos: o arquivo retornado ja vem com a quantidade de instancias  */
		String returnFile =  joinFiles( posFile, negFile, nameFile[typePartition] + "-FULL-");
		
		/* apagando arquivos temporarios */
		File file = new File( config.getProperty("drive") + config.getProperty("outputDir") + posFile);
		file.delete();
		file = new File( config.getProperty("drive") + config.getProperty("outputDir") + negFile);
		file.delete();
		System.out.println("\nArquivo gerado: " + returnFile );
		
		/* Dividindo em TREINO e TESTE  */
		String trainingFile = createRandomTrainingData( returnFile, nameFile[typePartition] + "-TREINO-");
		System.out.println("\tArquivo de TREINO: " + trainingFile);
		String testFile = extractRemainingInstancesForTest(returnFile, trainingFile, nameFile[typePartition] + "-TESTE-" );
		System.out.println("\tArquivo de TESTE : " + testFile);
		makeARFF( testFile , TEST, config.getProperty("attList").split("\t"));

		/* Separando o arquivo de TREINO em TR (Treino) e VL (validacao) */
		createValidationData( trainingFile , nameFile[typePartition] + "-" );

	}

	/**
	 * Realiza os experimentos de DataMining de forma automatizada para todas as particoes
	 * [10-90],[20-80],[30-70],[40-60],[50-50]. O prefixo das particoes é padronizado.
	 * Todas as saídas do classificador são direcionasa para uma planilha do exel, que é
	 * manipulada internamente pelo método.
	 * @param theClassifier O classificador escolhido para o processo experimental
	 * @throws Exception Caso algum erro de manipulação de arquivo seja lançado.
	 * 	 
	 * Arquivos de Saida: Experiment-<data-hoje>.xls
	 */
	public static void doAutomaticExperiment( int theClassifier ) throws Exception{
		// Obtendo a lista de diretorios selecionados
		List<File> selectedFile = null;
		// Um objeto para fortação numérica
		DecimalFormat decimal = new DecimalFormat("0.0000");

		// array sumarizador de Fmeasure para treino e teste
		double[][] summary = new double[5][2]; // lin 0: treino  lin 1: teste
		// indice de controle de summary
		int index = 0;
		
		// row comeca na linha 0 da planilha (1a linha)
		int row = 0; 
		// contador de coluna. comeca na coluna 0 (1a coluna)
		int col = 0;
		// Criando o manipulador de planilha eletronica
		ExcelFileHandler planilha  = new ExcelFileHandler();

		// Fazer a sequencia de experimentos para cada arquivo de particao
		// Por iteracao, são selecionados todos os arquivos ARFF referentes à
		// partição corrente.
		for( int partition=10; partition <=50; partition += 10){
			selectedFile = getArffFiles( DataMining.configGetProperty("drive") +
                    DataMining.configGetProperty("inputDir"),
                    "["+partition);
			
			if( selectedFile.isEmpty() ) {
				System.err.println("doAutomaticExperiment()- Nao foi encontrado arquivos ARFF para a particao " + partition);
				continue;
			}
			
			// Inicia o modulo de Machine learning no weka
			WekaExperiment weka = new WekaExperiment();
			
			// Verifica se os arquivos de ESTIMACAO e VALIDACAO estão na pasta
			File estFile;
			File validFile;
			if( (estFile = getFile(selectedFile,"EST"))==null || (validFile = getFile(selectedFile,"VAL"))==null  ) {
				System.err.println("doAutomaticExperiment()-ausentes arquivos de ESTIMACAO e/ou VALIDACAO para a particao " + partition);
				continue;
			}
			
			// Se chegou aqui, os arquivos da particao corrente estão presentes no diretorio
			weka.loadARFFFile( estFile, WekaExperiment.TRAINING_FILE);			
			
			writeCabecExcel(row,  planilha , "["+partition+","+(100-partition)+"]", weka.getAttributes(), theClassifier);
			
			System.out.print("Processando particao [" + partition + "," + (100-partition) + "]...");
			System.out.println( "[" + getClassifierName(theClassifier) + " Classifier]");

			// Obtendo o List com a combinacao binaria de atributos
			List<String> combination = generateAttributeCombinations( weka.getAttributeIndex());
			
			// row comeca na linha 4 da planilha
			row = row + 4; 
			// contador de coluna
			col = 0;
			// Contador de combinacoes
			int count = 0;
			// Maior F1
			double maiorF1 = 0;
			// Combinacao de atributos referente ao maior F1
			String theCombination="";
			
			// Preparando para iniciar os experimentos de Data Mining para todas as combinações de atributos
			// Dentro do laço, são usados os arquivos de ESTIMACAO e VALIDACAO
			for(String attributes : combination) {
				col = 0;
				++count;
				planilha.insertData(row, col, String.valueOf(count));
				// carregando arquivos ARFF para a particao corrente
				weka.loadARFFFile( estFile, WekaExperiment.TRAINING_FILE);
				// Obtendo a lista de atributos separados por ","
				writeAttributeCombinationExcel( planilha, weka.getAttributeIndex(), row, attributes, ",");

				// Tem que pegar o numero de atributos antes do remove()
				col = weka.getAttributes().size();
				// removendo os atributos que nao estao presentes em "attributes"
				weka.removeAttribute( attributes, true, WekaExperiment.TRAINING_FILE  );
				// gerando o classificador J48
				weka.buildClassifier( theClassifier );			
				weka.evaluateData(WekaExperiment.TRAINING_FILE);
				
//				System.out.println(decimal.format(weka.getFMeasure()) );
				Double f1 = Double.valueOf( decimal.format(weka.getFMeasure()).replace(',','.') ); 
//				System.out.println(weka.eval.toClassDetailsString());
//				System.out.println(weka.eval.toSummaryString());
//				System.out.println(weka.eval.toString());
//				Double f1 = 0.0;
				planilha.insertData(row, ++col, f1);
				
				// Preparando para avaliar TEST_FILE
				weka.loadARFFFile( validFile, WekaExperiment.TEST_FILE);
				// Removendo, também, os atributos no arquivo de test.
				weka.removeAttribute( attributes, true, WekaExperiment.TEST_FILE  );
				weka.evaluateData(WekaExperiment.TEST_FILE);
				f1 = Double.valueOf( decimal.format(weka.getFMeasure()).replace(',', '.') );
				planilha.insertData(row, ++col, f1 );
				
				if( f1 > maiorF1 ){
					maiorF1 = f1;
					theCombination = attributes;
				}
				
				planilha.insertData(row, ++col, attributes );

				row++;
			} // for da combinacao de atributos
			row++;
			planilha.insertData(row, 1, "Maior F1: ");
			planilha.insertData(row, 2, maiorF1);
			planilha.insertData(row, 4, "Atts: ");
			planilha.insertData(row, 5, theCombination);
			
			// Processando Treino e Teste utilizando a melhor combinacao de atributos
			row++;
			planilha.insertData(row, 1, "Treino: ");
//			planilha.insertData(row, 2, maiorF1);
			planilha.insertData(row, 4, "Teste: ");
//			planilha.insertData(row, 5, theCombination);
			
			// Carregando os arquivos de TREINO  e TESTE
			File training;
			File test;
			if( (training = getFile(selectedFile,"TREINO"))==null || (test = getFile(selectedFile,"TESTE"))==null  ) {
				System.err.println("doAutomaticExperiment()-ausentes arquivos de TREINO e/ou TESTE para a particao " + partition);
				continue;
			}
			// Preparando e avaliando arquivo de TREINO
			weka.loadARFFFile( training, WekaExperiment.TRAINING_FILE);
			weka.removeAttribute( theCombination, true, WekaExperiment.TRAINING_FILE  );
			weka.buildClassifier( theClassifier );		
			// Salvando a arvore de Decisao do classificador
			weka.saveDecisionTree(DataMining.configGetProperty("drive") +
                                  DataMining.configGetProperty("outputDir") +
                                  "["+partition+","+(100-partition)+"]-DecisionTree");
			weka.evaluateData(WekaExperiment.TRAINING_FILE);
			Double f1 = Double.valueOf( decimal.format(weka.getFMeasure()).replace(',','.') );
			planilha.insertData(row, 2, f1);
			// guardando o f1 no array
			summary[index][0] = f1.doubleValue();
			
			// Preparando e avaliando arquivo de Teste
			weka.loadARFFFile( test, WekaExperiment.TEST_FILE);
			weka.removeAttribute( theCombination, true, WekaExperiment.TEST_FILE  );
			weka.evaluateData(WekaExperiment.TEST_FILE);
			f1 = Double.valueOf( decimal.format(weka.getFMeasure()).replace(',', '.') );
			planilha.insertData(row, 5, f1 );
			// guardando o f1 no array
			summary[index][1] = f1.doubleValue();
			index++;

			
			row+=2;
			
			Date dataHoje = new Date();
			SimpleDateFormat formataData = new SimpleDateFormat("dd-MM-yyyy");
//			System.out.println( formataData.format(dataHoje));
			formataData.format(dataHoje);
			planilha.saveFile( DataMining.configGetProperty("drive") +
                               DataMining.configGetProperty("inputDir") +					
					           "Experimento-" + formataData.format(dataHoje).replace('\\', '-') + "-" +
                               getClassifierName(theClassifier) + ".xls");

		} // fim do laço de percurso das partições
	
		

	}

	/**
	 * Gera a combinacao binaria de todos os elementos contidos no List "inputString"
	 * @param setOfIndex Um list com os indices dos atributos que se deseja efetuar a combinação.
	 * @return um List<String> com todas as combinações. Cada combinação dentro do List
	 * é separada por uma ","
	 * Fonte:
	 * (1) http://daemoniolabs.wordpress.com/2011/02/17/gerando-combinacoes-sem-repeticao-pela-contagem-binaria-em-c/
	 */
	public static List<String> generateAttributeCombinations( List<String> setOfIndex){
		StringBuilder str = new StringBuilder();
		boolean firstIteration = true;
		for( String s : setOfIndex ){
			str.append( (firstIteration==true?"":";") + s);
			firstIteration=false;
		}
		return generateAttributeCombinations( str.toString() );
	}
	/**
	 * Gera a combinacao binaria de todos os elementos contidos em inputString
	 * @param inputString A lista de atributos, separada por ";"
	 * @return um List<String> com todas as combinações. Cada combinação dentro do List
	 * é separada por uma ","
	 * Fonte:
	 * (1) http://daemoniolabs.wordpress.com/2011/02/17/gerando-combinacoes-sem-repeticao-pela-contagem-binaria-em-c/
	 */
	public static List<String> generateAttributeCombinations( String inputString){
		
		List<String> list = new ArrayList<String>();
		StringBuilder str = new StringBuilder();
		int MAX, NUM, MASK;
		@SuppressWarnings("unused")
		int i,j;
		String[] token = inputString.split(";");
		
		if( token == null)
			return null;
		
		/* Manda o bit 1 para a n-ésima posição. Os bits são invertidos para que a 
		* posição n esteja com o bit zero, a fim de marcar  o final do processo. */
	    MAX = ~(1 << token.length) ;
	    
	    /* Primeiro número é o 1. */
	    NUM = 1;
	    
	    //  Quando o número alcançar MAX, o loop será encerrado.
	    while ( (MAX & NUM) != 0 ) {
	        MASK = 1 ;
	        i = j = 0 ;
	        boolean firstTime = true;
	        
			str.delete(0, str.length());

	        while ( (MAX & MASK) != 0 ) {
	            /* Verdadeiro se NUM tem um bit 1 na posição indicada por MASK. */
	            if ( (NUM & MASK) != 0 ) {
	                /* Gera a combinação em str. */
	                str.append( (firstTime?"":",") + token[j] ) ;
	            	firstTime = false;
	                i++ ;
	            }
	            j++ ;
	            /* Desloca a máscara */
	            MASK = MASK << 1 ;
	        }
	        list.add(str.toString() ); 
//	        System.out.println(++count + " - " + str.toString());
//	        System.out.println( list.toString());
	        NUM++ ;
	    }
	        
		return list;
	}
	
	/**
	 * Seleciona uma lista de arquivos ARFF, que comeca com determinado prefixo, no 
	 * diretorio especificado.
	 * @param directory O diretorio de busca
	 * @param prefix O prefixo para os arquivos
	 * @return Um List contendo os arquivos selecionados
	 */
	public static List<File> getArffFiles( String directory, String prefix ){
		File dir = new File( directory );
	  	List<File> selectedFile = new ArrayList<File>();
		// Verificando se o diretório existe e é um diretório
        if (!dir.isDirectory()) 
        	return null;  
  
    	// O filtro de arquivos ARFF
    	FileFilter ffArff = new FileFilter() {
    		public boolean accept( File file ){
    			if( !file.isFile())
    				return false;
    			if (file.getName().toLowerCase().endsWith("arff"))
    		        return true;
    		      else
    		    	return false;
    		}
    	};
    	
        File[] children = dir.listFiles( ffArff );
        
        for( File f: children ){
        	if( f.getName().toLowerCase().startsWith( prefix ))
        		selectedFile.add( f );
        }
		return selectedFile;
	} // fim do método
	
	
	/**
	 * Obtem da lista de arquivos, aquele que possui a substring especificada
	 * @param set O conjunto de arquivos
	 * @param substr Uma subcadeia que será procurada no mome do arquivo
	 * @return O arquivo encontrado, ou null caso ntenha encontrado.
	 */
	private static File getFile(List<File> set, String substr){
		for(File f: set){
			if( f.getName().toLowerCase().indexOf( substr.toLowerCase() ) >= 0 )
				return f;
		}
		return null;
	}

	/**
	 * Retorna o nome do classificador referente à constante de classe.
	 * @param theClassifier UO classificador utilizado
	 * @return O nome do classificador correspondente ao argumento de entrada.
	 */
	private static String getClassifierName( int theClassifier ){
		switch( theClassifier ){
		case WekaExperiment.J48:
			return "J48";
		case WekaExperiment.NAYVE_BAYES:
			return "NAYVE BAYES";
		case WekaExperiment.RANDOM_FOREST:
			return "RANDOM FOREST";
		case WekaExperiment.ADABOOST:
			return "ADABOOST";			
		case WekaExperiment.LOGISTIC_REGRESSION:
			return "LOGISTIC_REGRESSION";			
		case WekaExperiment.SVM:
			return "SVM";			
		default:
			return "J48";
			
		}		
		
	}
	/**
	 * Escreve o cabecalho padrão de inicio de nova partição na planilha do exel.
	 * @param row A linha a qual será iniciado o cabeçalho
	 * @param excel O objeto que representa a planilha que está sendo gerada
	 * @param partition O prefixo da partição
	 * @param attributes Os Atributos que foram encontrados no arquivo de instäncias
	 * @param userClassifier O classificador utilizado no experimento
	 */
	private static void writeCabecExcel( int row, ExcelFileHandler excel, String partition, List<String> attributes, int usedClassifier ){
		excel.setFont("Consolas");
		excel.insertData(row, 0, "Particao:");
		excel.insertData(row, 1, partition);
		excel.insertData(row, 3, "Classificador....::");
		

		excel.insertData(row, 5, getClassifierName(usedClassifier));
		excel.setColor(row, 5);

		excel.setColor(row, 1);
		row += 2;
		excel.insertData(row, 1, "Atributos");
		excel.setBold(row, 1);

		// Escrevendo os atributos
		int col = 1;
		excel.insertData(++row, 0, "#");
		for( String att: attributes){
			excel.insertData(row, col++, att);
		}
		excel.insertData(row, col++, "Estimacao");
		excel.insertData(row, col++, "Validacao");
		excel.insertData(row, col, "Combinacao");

	}
	

	/**
	*  Escreve no arquivo excel a sequencia de atributos que esta sendo avaliada, na seguinte forma:
	 *       jaccard cosseno a  b
	 *  1       x             x  x
	 * @param excel a instancia do objeto excel aberto
	 * @param attributes A lista com os indices dos atributos do arquivo de treinamento.
	 * @param row A linha a ser inscrita no arquivo do excel
	 * @param att A combinacao de atributos corrente
	 * @param separator O separador utilizado para obter os atributos da iteracao corrente
	 */
	private static void writeAttributeCombinationExcel( ExcelFileHandler excel, List<String> attributes, int row, String att, String separator ){
		String[] token = att.split( separator );
		int col = 1;
		for( String s: attributes ){
			for(int i=0; i < token.length; i++ ) {
				if ( token[i].equalsIgnoreCase( s) ) {
					excel.insertData( row, col, "X");
					excel.setCenterAlign(row, col);
					break;		
				}

			}
			col++;
		}
		
	}
	
} // fim da classe DataMining


