/* 
 * ========================================================================================
 * ARQUIVO ULTRAPASSADO!!!! - Tudo ja foi transferido para DataMining.java 
 * ========================================================================================
 * */
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
import java.util.Random;

/*
 * OBJETIVO:
 * 		Identificar instancias de um conjunto de treinamento e retira-las do conjunto
 * 		de teste.
 * SITUACAO:
 * 		[Ativa]
 * DATA:
 * 		criacao..: 19/02/2012
 * 		alteracao: 19/02/2012
 */
public class Miscelaneous {
	private static String drive = "g:";
	private static String readDirectory = drive + "/temp/bibsonomy/";
	private static String writeDirectory = drive + "/temp/";
	/*
	 * 
	 */
	
	public static void selectDrive(String newDrive){
		drive = newDrive;
		readDirectory = drive + "/temp/bibsonomy/";
		writeDirectory = drive + "/temp/";
	}
	
	/*
	 * OBJETIVO:
	 * 		Identificar as instancias que foram usadas para treino e retira-las do arquivo geral
	 * 		de instancia, para que o arquivo de teste nao tenha instancias que foram para treino
	 * SITUACAO:
	 * 		[Ativa]
	 * DATA:
	 * 		criacao..: 19/02/2012
	 * 		alteracao: 19/02/2012
	 * ENTRADA:
	 * 		filter: retirar do arquivo de treino (cru) as  instancias: "yes" or "no"
	 *      inputFile: o arquivo que contem o conjunto de treinamento. Neste arquivo,
	 *      cada linha deve conter o numero do registro, tags relacionadas, atributos e classe.
	 * SAIDA:
	 * 		o arquivo "14-InstancesLeftForTest" para "yes" or "no"
	 */
	
	public static String separateRecordTraining(String trainingFile, String filter ){
		/* file: "ListOfSynonynsWordnet"  */
		File fileIn  = new File( readDirectory + trainingFile);

		String output = "InstanceLeftFor-" + filter;
		File fileOut = new File( readDirectory + output);		
		
		BufferedWriter bw = null;
		BufferedReader br = null;
		
		StringBuilder linha = new StringBuilder();
		String[] words = null;
				
		HashSet<String> hs = new HashSet<String>();
		int count = 0;
		int retiradas=0, selecionadas= 0;
		try {
				
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
								
			// Colocando todas as instancias positivas ou negativas do training no hashset
			while( br.ready()){
				linha.delete(0, linha.length());	
				linha.append(br.readLine());
				words = linha.toString().split("\t");
				
				// desprezar comentarios
				if (words.length == 1)
					continue;
				
				if ( !filter.equalsIgnoreCase("yes_no")) {
					
					if (!words[7].equalsIgnoreCase( filter ))
						continue;
				} 
				
				hs.add( words[0]);
				count++;
			}
			br.close();
			
			System.out.println("Numero de Instancias " + filter.toUpperCase() + " no arquivo de Treino: " + count);
			
			fileIn  = new File( readDirectory + (filter.equalsIgnoreCase("yes")?
					                             "S01-Instances_supervisioneds_yes": 
					                            (filter.equalsIgnoreCase("no"))?
					                             "S01-Instances_supervisioneds_no":
					                             "S01-Instances_supervisioneds_yes_no"));
				
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			bw = new BufferedWriter(new OutputStreamWriter
					  (new FileOutputStream(fileOut),"UTF8"));
			
			count = 0;
			// Deixando apenas as instancias que nao foram para o conjunto de treinamento.
			// Colocando todas as instancias positivas ou negativas no hashset
			while( br.ready()){
				linha.delete(0, linha.length());	
				linha.append(br.readLine());
				words = linha.toString().split("\t");
				count++;
				
				// desprezar comentarios
				if (words.length == 1)
					continue;

				if ( hs.contains( words[0]) ) {
					retiradas++;
					continue;
				}
				selecionadas++;
				bw.write( linha.toString());
				bw.newLine();
				bw.flush();
			}		
			br.close();
			bw.close();
		    
			System.out.println("Numero de Instancias " + filter.toUpperCase() + " do SET: " + count);
			System.out.println("Numero de Instancias " + filter.toUpperCase() + " Removidas do SET: " + retiradas);
			System.out.println("Numero de Instancias " + filter.toUpperCase() + " restantes do SET: " + selecionadas);

			bw.close();
			System.out.println("Operacao Concluida.....");
	
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
		
		return output;
	
	}
	
	/*
	 * OBJETIVO:
	 * 		Sortear randomicamente as instancias que vao fazer parte de um conjunto de treino
	 * 		ou teste (provavelmente para teste).
	 * SITUACAO:
	 * 		[Ativa]
	 * DATA:
	 * 		criacao..: 19/02/2012
	 * 		alteracao: 25/02/2012
	 * ENTRADA:
	 * 		quant: o numero de instancias que gostaria de gerar
	 *      inputFile: o arquivo que contem as instancias a serem escolhidas. Neste arquivo,
	 *      cada linha deve conter o numero do registro, tags relacionadas, atributos e classe.
	 * SAIDA:
	 * 		o arquivo "S01-RandomFile", gravado em writeDirectory. Esse arquivo pode ser
	 *      tanto para classificacoes "yes" quanto "no".
	 *      Esse arquivo pode ser descartado a qualquer momento, pois é muito simples
	 *      gerar outro.
	 */
	public static String createRandomFile( int quant, String inputFile ){
		
		String outputFile;
		File fileIn  = new File( readDirectory + inputFile);
		/* file: "ListOfSynonynsWordnet" */
		outputFile = inputFile + "-RandomFile-" + quant;
		File fileOut = new File( readDirectory + outputFile );		
		
		BufferedWriter bw = null;
		BufferedReader br = null;
		
		StringBuilder linha = new StringBuilder();
		String[] words = null;
				
		HashMap<String,String> hm = new HashMap<String,String>();

		
		Random randomGenerator = new Random();
		int count = 0;

		try {			
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			
			System.out.println("Aguarde carga das instancias.....");
			// Colocando todas as instancias positivas ou negativas no hashset
			while( br.ready()){
				linha.delete(0, linha.length());	
				linha.append(br.readLine());
				words = linha.toString().split("\t");
				
				// desprezar comentarios
				if (words.length != 8)
					continue;
				
				count++;
				hm.put( String.valueOf(count), linha.toString());

			}
			br.close();
			System.out.println("Passo1 CONCLUIDO = Instancias carregadas....: " + hm.size());
			
			System.out.println("\nPasso2: Sorteando as instancias....");
			bw = new BufferedWriter(new OutputStreamWriter
					  (new FileOutputStream(fileOut),"UTF8"));
			// gerando instancias randomicamente
			int randomInt = 0;
			int totalInstances = hm.size();
			count = 0;
			for(int i=1; i<= quant; i++){
				randomInt = randomGenerator.nextInt( totalInstances ) + 1;
				
				if ( !hm.containsKey( String.valueOf( randomInt ))){
					i--;
					continue;
				}
				
				bw.write( hm.remove( String.valueOf( randomInt )));
				bw.newLine();
				bw.flush();
				
				totalInstances = hm.size();
				count++;
				if ( i % 10 == 0)
					System.out.println("Progresso: " + i);
			}
			br.close();
			bw.close();
			
			System.out.println("Operacao concluida!");
			System.out.println("Tamanho do arquivo gerado: " + (count));
			
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
		return outputFile;
		
	}

	
	/*
	 * OBJETIVO:
	 * 		Sortear randomicamente as instancias que vao fazer parte de um conjunto de treino
	 * 		ou teste (provavelmente para teste).
	 * SITUACAO:
	 * 		[Ativa]
	 * DATA:
	 * 		criacao..: 19/02/2012
	 * 		alteracao: 25/02/2012
	 * ENTRADA:
	 * 		quant: o numero de instancias que gostaria de gerar
	 *      inputFile: o arquivo que contem as instancias a serem escolhidas. Neste arquivo,
	 *      cada linha deve conter o numero do registro, tags relacionadas, atributos e classe.
	 * SAIDA:
	 * 		o arquivo "S01-RandomFile", gravado em writeDirectory. Esse arquivo pode ser
	 *      tanto para classificacoes "yes" quanto "no".
	 *      Esse arquivo pode ser descartado a qualquer momento, pois é muito simples
	 *      gerar outro.
	 */
	public static String createRandomFileWithReplacement( int quant, String inputFile ){
		
		String outputFile;
		File fileIn  = new File( readDirectory + inputFile);
		/* file: "ListOfSynonynsWordnet" */
		outputFile = inputFile + "-RandomFile-" + quant;
		File fileOut = new File( readDirectory + outputFile );		
		
		BufferedWriter bw = null;
		BufferedReader br = null;
		
		StringBuilder linha = new StringBuilder();
		String[] words = null;
				
		HashMap<String,String> hm = new HashMap<String,String>();

		
		Random randomGenerator = new Random();
		int count = 0;

		try {			
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			
			System.out.println("Aguarde carga das instancias.....");
			// Colocando todas as instancias positivas ou negativas no hashset
			while( br.ready()){
				linha.delete(0, linha.length());	
				linha.append(br.readLine());
				words = linha.toString().split("\t");
				
				// desprezar comentarios
				if (words.length != 8)
					continue;
				
				count++;
				hm.put( String.valueOf(count), linha.toString());

			}
			br.close();
			System.out.println("Passo1 CONCLUIDO = Instancias carregadas....: " + hm.size());
			
			System.out.println("\nPasso2: Sorteando as instancias....");
			bw = new BufferedWriter(new OutputStreamWriter
					  (new FileOutputStream(fileOut),"UTF8"));
			// gerando instancias randomicamente
			int randomInt = 0;
			int totalInstances = hm.size();
			count = 0;
			for(int i=1; i<= quant; i++){
				randomInt = randomGenerator.nextInt( totalInstances ) + 1;
								
				bw.write( hm.get( String.valueOf( randomInt )));
				bw.newLine();
				bw.flush();
				
				count++;
				if ( i % 10 == 0)
					System.out.println("Progresso: " + i);
			}
			br.close();
			bw.close();
			
			System.out.println("Operacao concluida!");
			System.out.println("Tamanho do arquivo gerado: " + (count));
			
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
		return outputFile;
		
	}

	
	/*
	 * OBJETIVO:
	 * 		Exibir numeros acerca dos diferentes casos de sinonimia que cada medida é capaz de
	 *      pegar. Por exemplo, levenhstein pega muito plural e singular, etc.
	 * SITUACAO:
	 * 		[Ativa]
	 * DATA:
	 * 		criacao..: 15/03/2012
	 * 		alteracao: 15/03/2012
	 * ENTRADA:
	 * 		inputFile: arquivo a ser processado
	 * SAIDA:
	 * 		O mesmo arquivo com os casos separados:
	 * 		caso 1: Inflections
	 * 		caso 2: pequenas variacoes
	 * 		caso 3: acronimos
	 * 		caso 4: sinonimos distintos
	 */

	public static void cruzamentoAtributos( String inputFile ){
		
		File fileInput;
		
		String[] files = {"S01-[Inflections]-[0,0 a 0,5]",
				          "S01-[Slow Variations]-[0,37 a 0,7]",
				          "S01-[Acronym]-[0,57 a 1,0]",
				          "S01-[Synonymous]-[0,7 a 1,0]"
				          };
		/* file: "ListOfSynonynsWordnet" */
		

		BufferedReader br = null;
		
		StringBuilder linha = new StringBuilder();
		String[] words = null;
		
		HashMap<String,String> hmCase1, hmCase2, hmCase3, hmCase4;
		hmCase1 = new HashMap<String,String>();
		hmCase2 = new HashMap<String,String>();
		hmCase3 = new HashMap<String,String>();
		hmCase4 = new HashMap<String,String>();
				
		try {			
			
			
			System.out.println("PASSO 1: Aguarde carga das instancias.....");
			// Colocando todas as instancias positivas ou negativas no hashset
			
			for (int i = 0; i < files.length; i++) {
				fileInput  = new File( readDirectory + files[i]);
				br = new BufferedReader(new InputStreamReader(new FileInputStream( fileInput ), "UTF8"));
				
				System.out.println( fileInput );
				while( br.ready()){
					linha.delete(0, linha.length());	
					linha.append(br.readLine());
					words = linha.toString().split("\t");
					
					switch( i ){
					case 0:
						hmCase1.put( words[0], linha.toString());
						break;
					case 1:
						hmCase2.put( words[0], linha.toString());
						break;
					case 2:
						hmCase3.put( words[0], linha.toString());
						break;
					case 3:
						hmCase4.put( words[0], linha.toString());
						break;
					}
				}

				br.close();
			}
			System.out.println("Passo 1: CONCLUIDO = Instancias carregadas....: " );
			
			System.out.println("\nPasso 2: Separando os casos.....");
			
			// Abrindo arquivo externos
			File externalFile = new File( writeDirectory + inputFile);
			br = new BufferedReader(new InputStreamReader(new FileInputStream( externalFile ), "UTF8"));
			
			// Preparando arquivo de saida
			File fileOut = new File( writeDirectory + inputFile + "-cross.txt" );		
			
			BufferedWriter bw = null;
			bw = new BufferedWriter(new OutputStreamWriter
					  (new FileOutputStream(fileOut),"UTF8"));

			String number;
			int count = 0;
			int tn1=0, tn2=0, tn3=0, tn4=0;
			
			while( br.ready()){
				linha.delete(0, linha.length());	
				linha.append(br.readLine());
				words = linha.toString().split("\t");
				
				if ( hmCase1.containsKey(words[0]) ) {
					number = "1";
					tn1++;
				} else if ( hmCase2.containsKey(words[0]) ) {
					number = "2";
					tn2++;
				} else if ( hmCase3.containsKey(words[0]) ) {
					number = "3";
					tn3++;
				} else if ( hmCase4.containsKey(words[0]) ) {
					number = "4";
					tn4++;
				} else {
					number = "NaN";
				}
				
				System.out.println("Progresso: " + ++count );
				bw.write( number + "\t" + words[1] + "\t" + words[2] );
				bw.newLine();
				bw.flush();
			}
			
			System.out.println("Operacao concluida....");
			System.out.println("Numero de casos do tipo 1: " + tn1);
			System.out.println("Numero de casos do tipo 2: " + tn2);
			System.out.println("Numero de casos do tipo 3: " + tn3);
			System.out.println("Numero de casos do tipo 4: " + tn4);
			br.close();
			bw.close();
						
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
	
	}
	
	public static void main(String args[]){
//		System.out.println(createRandomFile( 737, "S01-Instances_supervisioneds_yes"));
//		System.out.println( separateRecordTraining("S01-Instances_supervisioneds_yes-RandomFile-737", "yes") );
//		separeRecordTraining("S01-Instances_supervisioneds_toSMOTE14_special", "no");
//		separeRecordTraining("S01-Instances_supervisioneds_yes_no-RandomFile05", "yes_no");
//		createRandomFile(7790, "S01-Instances_supervisioneds_no");
//		separateRecordTraining("S01-Instances_supervisioneds_no-RandomFile-16665", "no");
		
		cruzamentoAtributos( "Evaluation-Heuristic-CosineTag-0,0" );
	}
	

}
