import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;



public class PrepareData {
	
	private static String drive = "g:";
	private static String readDirectory;
	private static String writeDirectory;
	
	public static final int TRAINING=1;
	public static final int TEST=2;
	
	public static final int YES=1;
	public static final int NO=0;
	public static final int YES_NO=2;
	
	/**
	 * @return the readDirectory
	 */
	public static String getReadDirectory() {
		return readDirectory;
	}

	/**
	 * @param readDirectory the readDirectory to set
	 */
	public static void setReadDirectory(String readDirectory) {
		PrepareData.readDirectory = readDirectory;
	}

	/**
	 * @return the writeDirectory
	 */
	public static String getWriteDirectory() {
		return writeDirectory;
	}

	/**
	 * @param writeDirectory the writeDirectory to set
	 */
	public static void setWriteDirectory(String writeDirectory) {
		PrepareData.writeDirectory = writeDirectory;
	}
	
		
	public static String getDrive(){
		return drive;
	}
	
	public static void setDrive( String newDrive){
		drive = newDrive;
	}
	
	
	public static void defaultConfig(){
		PrepareData.setDrive( "g:");
		PrepareData.setReadDirectory("/testes/[10-90]/");
		PrepareData.setWriteDirectory("/testes/[10-90]/");
		System.out.println("+-----------  Default Config  -----------------");
		System.out.println("| Drive..........: " + drive);
		System.out.println("| Read Directory.: " + readDirectory );
		System.out.println("| Write Directory: " + writeDirectory );
	}

	
	private static void headARFF(int typeFile, BufferedWriter bw) throws IOException {
		bw.write("% ARFF File for the Bibsonomy data - Synonymy Detection");
		bw.newLine();
		bw.write("%");
		bw.newLine();
		
		if ( typeFile == TRAINING) {
			bw.write("@relation data-training"); bw.newLine();
		} else {
			bw.write("@relation data-test"); bw.newLine();
		}
		
		bw.newLine();
				
		bw.write("@attribute levenshtein numeric");
		bw.newLine();
		bw.write("@attribute tagContext numeric");
		bw.newLine();
		bw.write("@attribute resourceContext numeric");
		bw.newLine();
		bw.write("@attribute coocurrence numeric");
		bw.newLine();
		bw.write("@attribute coocurByResource numeric");
		bw.newLine();
		bw.write("@attribute coocurByResource2 numeric");
		bw.newLine();
		bw.write("@attribute areSynonym { yes, no }");
		bw.newLine();
		bw.write("%");
		bw.newLine();
		bw.write("@data"); 
		bw.newLine();
		bw.write("%");
		bw.newLine();

		bw.flush();
	}


	
	


	/*
	 * OBJETIVO:
	 * 		Separa do arquivo de DUMP normalizado, as instancias YES ou NO, cada um no seu
	 *      respectivo arquivo.
	 *      O arquivo DUMP normalizado foi previamente salvo do MySQL para o formato texto,
	 *      cujos campos são separados por tabulacoes.
	 * SITUACAO:
	 * 		[Ativa]
	 * DATA:
	 * 		criacao..: 07/03/2012
	 * 		alteracao: 07/03/2012
	 * ENTRADA:
	 * 	    filter: "yes" , "no" ou "yes_no". 
	 *      inputFile: o arquivo que contem o DUMP normalizado com todas as relacoes.
	 *      este arquivo, geralmente, é o "S01-Instances_classifieds_normalized". 
	 * SAIDA:
	 * 		o arquivo DUMP_Supervised_Instances_yes ou DUMP_Supervised_Instances_no
	 *      no diretorio de escrita
	 *      
	 *      Ao arquivo de saída, é acrescentado um numero sequencial em cada registro,
	 *      para facilitar a identificação. 
	 */
	public static void convertToBasicFormat(String inputFile, String filter ){
		/* Declaracoes para arquivo de entrada e saida */
		BufferedReader br = null;
		// inputFile: "S01-Instances_classifieds_normalized"
		File fileIn = new File(drive + readDirectory + inputFile);
		
		BufferedWriter bw = null;
		File fileOut = new File(drive + writeDirectory + "Supervised_Instances_" + filter);
		
		/* variaves de apoio */
		int progress=0;
		StringBuilder linha = new StringBuilder();
		String[] temp = null;
		
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			
			while (br.ready()){
				progress++;
				linha.delete(0, linha.length());
				linha.append(br.readLine()); 
				temp = linha.toString().split("\t");
				
				if ( !filter.equalsIgnoreCase( "yes_no" ) ){
					
					if ( !temp[6].equals( filter )) {
						continue;
					}
				}
				
				
				bw.write( progress + "\t" + temp[0] + "\t" + temp[1] + "\t" + temp[2] + "\t" + temp[3] + "\t" + temp[4] + "\t" + 
						 temp[5] + "\t" + temp[6]);
				
				bw.newLine();
				bw.flush();
				
				System.out.println("Progresso: " + progress);
				/* Esvaziando o StringBuffer*/
				linha.delete(0, linha.length());
			}
			br.close();
			bw.close();
			System.out.println("Operacao Concluida...");
		} catch (IOException ioe ){
			ioe.printStackTrace();
		}
		
	}

	public void selectYesClassifiedsLevenshtein(){
		/* Declaracoes para arquivo de entrada e saida */
		BufferedReader br = null;
		File fileIn = new File(drive + "/temp/bibsonomy/Instances_classifieds_normalized");
		
		BufferedWriter bw = null;
		File fileOut = new File(drive + "/temp/Instances_supervisioneds_yes_levenshtein_0.5");
		
		/* variaves de apoio */
		int progress=0;
		StringBuilder linha = new StringBuilder();
		String[] temp = null;
		
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			
			while (br.ready()){
				progress++;
				linha.append(br.readLine()); 
				temp = linha.toString().split("\t");
				
				if ( !temp[6].equals("yes") || (Double.parseDouble(temp[2]) <= 0.25  || Double.parseDouble(temp[2]) > 0.5 ) ) {
					linha.delete(0, linha.length());
					continue;
				}
				
				bw.write( progress + "\t" + temp[0] + "\t" + temp[1] + "\t" + temp[2] + "\t" + temp[3] + "\t" + temp[4] + "\t" + 
						 temp[5] + "\t" + temp[6]);
				
				/*
				bw.write( progress + "\t" + temp[2] + "\t" + temp[3] + "\t" + temp[4] + "\t" + 
						 temp[5] + "\t" + temp[6]);
				*/
				bw.newLine();
				bw.flush();
				
				System.out.println("Progresso: " + progress);
				/* Esvaziando o StringBuffer*/
				linha.delete(0, linha.length());
			}
			br.close();
			bw.close();
			System.out.println("Operacao Concluida...");
		} catch (IOException ioe ){
			ioe.printStackTrace();
		}
		
	}

	
	
	
	
	
	/*
	 * OBJETIVO:
	 * 		Normaliza o atributo de co-ocorrencia, de um valor inteiro para um valor real
	 *      no intervalo [0,1].
	 * SITUACAO:
	 * 		[Ativa]
	 * DATA:
	 * 		criacao..: 
	 * 		alteracao: 07/03/2012
	 * ENTRADA:
	 *      inputFile: o arquivo que contem o DUMP com todas as relacoes.
	 *      este arquivo, geralmente, é o "S01-Instances_classifieds". 
	 * SAIDA:
	 * 		o arquivo input_file + "normalized" no diretorio de ESCRITA
	 */
	private static void normalizeCoocurrence(String inputFile ){
		/* Declaracoes para arquivo de entrada e saida */
		BufferedReader brOne = null;
		File fileOne = new File(drive + readDirectory + inputFile );
		
		BufferedWriter bw = null;
		File fileOut = new File(drive + readDirectory + inputFile + "_normalized");
		
		/* variaves de apoio */
		int count=0;
		StringBuilder linha = new StringBuilder();
		String[] temp = null;
				
		double maior=0,atual=0,razao = 0;
		try {
			brOne = new BufferedReader(new InputStreamReader(new FileInputStream( fileOne ), "UTF8"));

			System.out.println("Obtendo o maior valor de co-ocorrencia...");
			while (brOne.ready()){
				count++;
				linha.append(brOne.readLine());		
				temp = linha.toString().split("\t");

				atual = Double.parseDouble( temp [5]);
				
				if ( atual > maior )
					maior = atual;
				
				/* Esvaziando o StringBuffer*/
				linha.delete(0, linha.length());
			}
			System.out.println("Concluido! O maior valor encontrado foi " + maior);

			brOne.close();
			brOne = new BufferedReader(new InputStreamReader(new FileInputStream( fileOne ), "UTF8"));
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			
			DecimalFormat decimal = new DecimalFormat( "0.00000" );
			count=0;
			while ( brOne.ready()){
				count++;
				linha.append(brOne.readLine());		
				temp = linha.toString().split("\t");

				atual = Double.parseDouble( temp [5] );
				razao = atual/maior;
			
				bw.write( temp[0] + "\t" + temp[1] + "\t" + temp[2] + "\t" + temp[3] + "\t" + 
						  temp[4] + "\t" + decimal.format( razao).replace(',','.')   + "\t" + 
						  temp[6] );
				
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

	
	/*
	 * OBJETIVO:
	 * 		Converter o arquivo BASIC_FORMAT para o formato ARFF
	 * SITUACAO:
	 * 		[Ativa]
	 * DATA:
	 * 		criacao..: 07/03/2012
	 * 		alteracao: 07/03/2012
	 * ENTRADA:
	 * 	    typeFile: TRAINING or TEST
	 * 		validAttribute: Um array contendo as métricas que terao seus valores conservados
	 *      quando o arquivo ARFF for gerado. Se uma determinada métrica não for encontrada,
	 *      seu valor será "0.0000"
	 *      inputFile: o arquivo que contem o conjunto de instancias em BASIC FORMAT
	 *       
	 * SAIDA:
	 * 		o arquivo "inputFile" + ".arff" no diretorio de escrita
	 */
	public static void makeARFF(String inputFile, int typeFile, String[] validAttribute ){
		/* Declaracoes para arquivo de entrada e saida */
		BufferedReader brOne = null;
		File fileOne = new File(drive + readDirectory + inputFile);
		
		BufferedWriter bw = null;
		File fileOut = new File(drive + writeDirectory + inputFile + ".arff");
		
		/* variaves de apoio */
		int count=0;
		StringBuilder linha = new StringBuilder();
		String[] temp = null;

		// matriz que controla os itens que serao considerados
		int matrix[]={0,0,0,0,0,0};
//		String[] measure = {"levenshtein","tagContext","resourceContext","co-occurrence", "coocurByResource2"};
		String[] measure = {"levenshtein","tagContext","resourceContext","coocurrence","coocurByResource","coocurByResource2"};
		
		try {
			brOne = new BufferedReader(new InputStreamReader(new FileInputStream( fileOne ), "UTF8"));
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			
			PrepareData.headARFF( typeFile, bw);
			
			// Verificando quais foram as medidas repassadas por parâmetro
			for (int i = 0; i < measure.length; i++) {
				for (int j = 0; j < validAttribute.length; j++) {
					if ( measure[i].equalsIgnoreCase( validAttribute[j])) {
						matrix[i] = 1;
						break;
					}
				}
			}
			
			while (brOne.ready()){
				count++;
				linha.append(brOne.readLine());		
				temp = linha.toString().split("\t");
								
				bw.write( (matrix[0]==1? temp[3].trim() : "0.0000") + ", " + 
				          (matrix[1]==1? temp[4].trim() : "0.0000") + ", " + 
				          (matrix[2]==1? temp[5].trim() : "0.0000") + ", " +  
				          (matrix[3]==1? temp[6].trim() : "0.0000") + ", " +
				          (matrix[4]==1? temp[7].trim() : "0.0000") + ", " +
				          (matrix[5]==1? temp[8].trim() : "0.0000") + ", " + 
						  temp[9] );

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

	
	
	/*
	 * OBJETIVO:
	 * 		Eliminar do arquivo BASIC_FORMAT todas as instancias de uma classe especifica
	 * SITUACAO:
	 * 		[Ativa]
	 * DATA:
	 * 		criacao..: 01/03/2012
	 * 		alteracao: 01/03/2012
	 * ENTRADA:
	 * 		cleanedClass: A classe, YES or NO, que se deseja eliminar do arquivo
	 *      inputFile: o arquivo que contem o conjunto de instancias em BASIC_FORMAT
	 * SAIDA:
	 * 		o arquivo "inputFile" + "_without_yes" ou "_without_no"
	 */
	public static void deleteInstances(String inputFile, String cleanedClass ){
		/* Declaracoes para arquivo de entrada e saida */
		BufferedReader brOne = null;
		File fileOne = new File(drive + "/temp/bibsonomy/" + inputFile);
	//		File fileOne = new File(drive + "/temp/bibsonomy/" + "14-data-test-04-reClassified");
		
		BufferedWriter bw = null;
		File fileOut = new File(drive + "/temp/bibsonomy/" + inputFile + "_without_" + cleanedClass);
	//		File fileOut = new File(drive + "/temp/14-data-test-04-reClassified.arff");
		
		/* variaves de apoio */
		int count=0, eliminados=0;
		StringBuilder linha = new StringBuilder();
		String[] temp = null;
				
		
		try {
			brOne = new BufferedReader(new InputStreamReader(new FileInputStream( fileOne ), "UTF8"));
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
					
			while (brOne.ready()){
				count++;
				linha.delete(0, linha.length());
				linha.append(brOne.readLine());		
				temp = linha.toString().split("\t");
				
	
				if ( temp[7].trim().equalsIgnoreCase( cleanedClass )){
					eliminados++;
					continue;
				}
				bw.write( linha.toString());
				bw.newLine();
				bw.flush();
	
				
				System.out.println("Progresso: " + count);
			}
			brOne.close();
			bw.close();
			System.out.println("Operacao Concluida...\n\n");
			System.out.println("Total de Registro do Novo Arquivo...: " + count);
			System.out.println("Total de Registros " + cleanedClass.toUpperCase() + " eliminados...: " + eliminados);
		} catch (IOException ioe ){
			ioe.printStackTrace();
		}		
	
	} // method


	
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		int opcao;
		
		defaultConfig();
		for(;;){
			System.out.println("=================================");
			System.out.println("1- Config");
			System.out.println("2- Normalize Cooccurrence");
			System.out.println("3- Convert to BASIC FORMAT");
			System.out.println("4- Make ARFF File");
			System.out.println("5- Sair");
			System.out.println("=================================");
			System.out.println("Opcao [ ]\b\b");
			opcao = in.nextInt();
			
			switch( opcao ){
				case 1:
				{
					String drive, read, write;
					System.out.println("\nNovo Drive: ");
					drive = in.nextLine();
					setDrive( drive );
					System.out.println("Diretorio de Leitura: ");
					read = in.nextLine();
					setReadDirectory( read );
					System.out.println("Diretorio de Escrita: ");
					write = in.nextLine();
					setWriteDirectory( write );
				}
				case 2:
				case 3:
				case 4:
				{
					/* Convertendo para o formato ARFF */
//					 String[] att = {"levenshtein","tagContext","resourceContext","coocurrence","coocurByResource2"};
					 String[] att = {"levenshtein","tagContext","resourceContext","coocurrence","coocurByResource2"};
//					PrepareData.makeARFF("S01-Instances_supervisioneds_yes_no-TRAINING-6666_02", TRAINING, att);
//					PrepareData.makeARFF("S01-Instances_supervisioneds_yes_no-TEST-3334_02", TEST, att);
//					String[] att = {"support","confidence","jaccard"};
					PrepareData.makeARFF("S03-[1]-[10,90]-FileTR-9822", TRAINING, att);
					PrepareData.makeARFF("S03-[1]-[10,90]-FileVL-4912", TEST, att);

				}
				case 5:
				default:
			}
			
			System.exit(0);
			
			/* Normalizando o arquivo de classificacoes para o atributo de co-ocorrencia */
			PrepareData.normalizeCoocurrence("S01-Instances_classifieds" );
	
			/* Convertendo o DUMP das relacoes de tags para o formato BASIC_FORMAT */
			PrepareData.convertToBasicFormat("S01-Instances_classifieds_normalized", "yes_no");
			
			/* Extraindo instancias de um BASIC_FORMAT */
			PrepareData.deleteInstances("S01-Instances_supervisioneds_no-reClassified", "yes");
		}
			
	}

}
	
	