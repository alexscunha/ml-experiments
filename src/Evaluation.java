import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;



public class Evaluation {
	public static int MY_TREE_ONE_MODIFIED = 11;
	public static int MY_TREE_TWO_MODIFIED = 21;
	public static int MY_TREE_ONE_ORIGINAL = 10;
	public static int MY_TREE_TWO_ORIGINAL = 20;
	public static int MY_TREE_FINAL_ORIGINAL = 30;
	public static int MY_TREE_FINAL_MODIFIED = 31;
	public static int MY_TREE_LAST_ORIGINAL = 40;
	public static int MY_TREE_LAST_MODIFIED = 41;
	
	/*
	 *  Este método tenta identificar, pelos atributos e classificação real, qual é o par
	 *  de tag correspondente. Isto, apenas para as tags reais igual a "yes" 
	 *  Quando o par de tag é descoberto, um novo arquivo é gerado unindo o arquivo que passou
	 *  pela predição no Weka, seus dados, e as duas tags.
	 *  OBS:
	 *  Os arquivos de entrada tem que estar com a coocorrencia normalizada.
	 *  
	 */
	public static void findTagsYes( String drive, String filePredicted ){
		/* Declaracoes para arquivo de entrada e saida */
		BufferedReader brOne = null;
		File fileOne = new File(drive + "/temp/" + filePredicted);
		BufferedReader brTwo = null;
		File fileTwo = new File(drive + "/temp/bibsonomy/Instances_Supervisioneds_Yes");
		
		BufferedWriter bwLog = null;
		File fileLog = new File(drive + "/temp/Log-evaluation-yes.txt");
		BufferedWriter bw = null;
		File fileOut = new File(drive + "/temp/" + filePredicted + "-Tags-Yes");

			
		/* variaves de apoio */
		int count=0,totFound=0, totNotFound=0;
		StringBuilder linha = new StringBuilder();
		String[] temp = null;
		double attribute[]= new double[4];

		
		/* carregando instancias "yes" para o ArrayList */
		ArrayList<Instancia> sim = new ArrayList<Instancia>();
		try {
			brTwo = new BufferedReader(new InputStreamReader(new FileInputStream( fileTwo ), "UTF8"));
			while ( brTwo.ready()){
				linha.append(brTwo.readLine());		
				temp = linha.toString().split("\t");
				
				sim.add( new Instancia(temp[0], temp[1], temp[2], temp[3], temp[4], temp[5], temp[6]));
				linha.delete(0, linha.length());
			}
			brTwo.close();
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
		System.out.println("Instancias para YES carregadas....");
		
		Instancia ins = new Instancia();
		try {
			brOne = new BufferedReader(new InputStreamReader(new FileInputStream( fileOne ), "UTF8"));		
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			bwLog = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileLog),"UTF8"));
			
			while (brOne.ready()){
				count++;
				linha.append(brOne.readLine());		
				temp = linha.toString().split("\t");
				
				if ( !temp[5].equals("yes") ){
					linha.delete(0, linha.length());
					continue;
				}
				
				/* Filling attributes */
				for( int i=0; i<attribute.length; i++ )
					attribute[i] = Double.parseDouble( temp[i] );
				
//				Iterator<Instancia> it = sim.iterator();
//				boolean flag = false;
//				while ( it.hasNext()){
//					Instancia i = it.next();
//					if (i.contem(attribute[0],attribute[1],attribute[2],attribute[3])) {
//						bw.write(i.getRecord()+"\t"+i.getTagOne()+"\t"+i.getTagTwo()+"\t"+
//					             linha.toString());
//						bw.newLine();
//						bw.flush();
//						flag = true;
//						break;
//					}
//				}
//				if (!flag){
//					bwLog.write(linha.toString());
//					bwLog.newLine();
//					bwLog.flush();
//				}
				
				/* Bloco de código para testar o contains do ArrayList()  */
				ins.mapInto( attribute);
				if ( sim.contains( ins )) {
					int index = sim.indexOf( ins );
					Instancia i = sim.get(index);
					bw.write(i.getRecord()+"\t"+i.getTagOne()+"\t"+i.getTagTwo()+"\t"+
		            linha.toString());
					bw.newLine();
					bw.flush();
					totFound++;
				} else {
					bwLog.write(linha.toString());
					bwLog.newLine();
					bwLog.flush();
					totNotFound++;
				}
				/***************** fim do bloco do contains() ****************/
				
				System.out.println("Progresso: " + count);
				/* Esvaziando o StringBuffer*/
				linha.delete(0, linha.length());
			}
			brOne.close();
			bwLog.close();
			bw.close();
			System.out.println("Operacao Concluida...");
			System.out.println("Total de Instancias encontradas......: " + totFound);
			System.out.println("Total de Instancias nao encontradas..: " + totNotFound);
			System.out.println("Total de Instancias Processadas......: " + totNotFound + totFound);
		} catch (IOException ioe ){
			ioe.printStackTrace();
		}		
		
		
	}

	/*
	 *  Este método tenta identificar, pelos atributos e classificação real, qual é o par
	 *  de tag correspondente. Isto, apenas para as tags reais igual a "no" 
	 *  Quando o par de tag é descoberto, um novo arquivo é gerado unindo o arquivo que passou
	 *  pela predição no Weka, seus dados, e as duas tags.
	 *  OBS:
	 *  Os arquivos de entrada tem que estar com a coocorrencia normalizada.
	 *  
	 */

	public static void findTagsNo( String drive, String filePredicted ){
		/* Declaracoes para arquivo de entrada e saida */
		BufferedReader brOne = null;
		File fileOne = new File(drive + "/temp/" + filePredicted);
		BufferedReader brTwo = null;
		File fileTwo = new File(drive + "/temp/bibsonomy/Instances_Supervisioneds_No");
		
		BufferedWriter bwLog = null;
		File fileLog = new File(drive + "/temp/Log-evaluation-no.txt");
		BufferedWriter bw = null;
		File fileOut = new File(drive + "/temp/" + filePredicted + "-Tags-No");

			
		/* variaves de apoio */
		int count=0, totFound=0, totNotFound=0;
		StringBuilder linha = new StringBuilder();
		String[] temp = null;
		double attribute[]= new double[4];

		
		/* carregando instancias "yes" para o ArrayList */
		ArrayList<Instancia> nao = new ArrayList<Instancia>();
		try {
			brTwo = new BufferedReader(new InputStreamReader(new FileInputStream( fileTwo ), "UTF8"));
			while ( brTwo.ready()){
				linha.append(brTwo.readLine());		
				temp = linha.toString().split("\t");
				
				nao.add( new Instancia(temp[0], temp[1], temp[2], temp[3], temp[4], temp[5], temp[6]));
				linha.delete(0, linha.length());
			}
			brTwo.close();
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
		System.out.println("Instancias para No carregadas....");
		
		Instancia ins = new Instancia();
		try {
			brOne = new BufferedReader(new InputStreamReader(new FileInputStream( fileOne ), "UTF8"));		
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			bwLog = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileLog),"UTF8"));
			
			while (brOne.ready()){
				count++;
				linha.append(brOne.readLine());		
				temp = linha.toString().split("\t");
				
				if ( !temp[5].equals("no") ){
					linha.delete(0, linha.length());
					continue;
				}
				
				/* Filling attributes */
				for( int i=0; i<attribute.length; i++ )
					attribute[i] = Double.parseDouble( temp[i] );
				
//				Iterator<Instancia> it = sim.iterator();
//				boolean flag = false;
//				while ( it.hasNext()){
//					Instancia i = it.next();
//					if (i.contem(attribute[0],attribute[1],attribute[2],attribute[3])) {
//						bw.write(i.getRecord()+"\t"+i.getTagOne()+"\t"+i.getTagTwo()+"\t"+
//					             linha.toString());
//						bw.newLine();
//						bw.flush();
//						flag = true;
//						break;
//					}
//				}
//				if (!flag){
//					bwLog.write(linha.toString());
//					bwLog.newLine();
//					bwLog.flush();
//				}
				
				/* Bloco de código para testar o contains do ArrayList()  */
				ins.mapInto( attribute);
				if ( nao.contains( ins )) {
					int index = nao.indexOf( ins );
					Instancia i = nao.get(index);
					bw.write(i.getRecord()+"\t"+i.getTagOne()+"\t"+i.getTagTwo()+"\t"+
		            linha.toString());
					bw.newLine();
					bw.flush();
					totFound++;
				} else {
					bwLog.write(linha.toString());
					bwLog.newLine();
					bwLog.flush();
					totNotFound++;
				}
				/***************** fim do bloco do contains() ****************/
				System.out.println("Progresso: " + count);
				/* Esvaziando o StringBuffer*/
				linha.delete(0, linha.length());
			}
			brOne.close();
			bwLog.close();
			bw.close();
			System.out.println("Operacao Concluida...");
			System.out.println("Total de Instancias encontradas......: " + totFound);
			System.out.println("Total de Instancias nao encontradas..: " + totNotFound);
			System.out.println("Total de Instancias Processadas......: " + (int)(totNotFound + totFound));
		} catch (IOException ioe ){
			ioe.printStackTrace();
		}		
		
		
	}
	
	/*
	 *  Este método tenta identificar, pelos atributos e classificação real, qual é o par
	 *  de tag correspondente. ele leva em consideração tanto as classificações YES quanto
	 *  as classificações NO 
	 *  
	 *  STATUS: (NÃO CONCLUÍDO)
	 *  A logica de fazer tudo em arquivo na memória causa overhead de processamento e 
	 *  também tem grande chances de causar estouro de memória.
	 */
	public static void discoverTags( String drive, String filePredicted ){
		/* Declaracoes para arquivo de entrada e saida */
		BufferedReader brOne = null;
		File fileOne = new File(drive + "/temp/" + filePredicted);
		BufferedReader brYes = null;
		File fileYes = new File(drive + "/temp/Instances_supervisioneds_yes");
		BufferedReader brNo  = null;
		File fileNo  = new File(drive + "/temp/Instances_supervisioneds_no");
		
		BufferedWriter bw = null;
		File fileOut = new File(drive + "/temp/" + filePredicted + "-with-tags");

			
		/* variaves de apoio */
		int count=0;
		StringBuilder linha = new StringBuilder();
		String[] temp = null;
		double attributes[]= new double[4];
		StringBuilder tags = new StringBuilder();
		
						
		
		try {
			brOne = new BufferedReader(new InputStreamReader(new FileInputStream( fileOne ), "UTF8"));		
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			
			while (brOne.ready()){
				count++;
				linha.append(brOne.readLine());		
				temp = linha.toString().split(",");

				if ( count < 11 ) {
					linha.delete(0, linha.length());
					continue;
				}
				
				/* Filling attributes */
				for( int i=0; i<attributes.length; i++ )
					attributes[i] = Double.parseDouble( temp[i] );
				
				
				if ( temp[5].equals("yes")) {
				
					brYes = new BufferedReader(new InputStreamReader(new FileInputStream( fileYes ), "UTF8"));
					tags.append( seekAttributes(brYes, attributes) );
					brYes.close();
				} else {
					brNo = new BufferedReader(new InputStreamReader(new FileInputStream( fileNo ), "UTF8"));
					tags.append( seekAttributes(brNo, attributes) );
					brNo.close();
					
				}
				bw.write( tags.toString() + "," + linha.toString());
				bw.newLine();
				bw.flush();
				
				System.out.println("Progresso: " + count);
				/* Esvaziando o StringBuffer*/
				linha.delete(0, linha.length());
				tags.delete(0, tags.length());
			}
			brOne.close();
			bw.close();
			System.out.println("Operacao Concluida...");
		} catch (IOException ioe ){
			ioe.printStackTrace();
		}		
		
		
	}

	/*
	 * Esta função é usada por discoverTags().
	 * Por enquanto, está inutilizada.
	 */
	private static String seekAttributes( BufferedReader br, double attributes[]) throws IOException{
		StringBuilder linha = new StringBuilder();
		String[] temp = null;
		boolean flag;		

		while( br.ready()){
			linha.append(br.readLine());		
			temp = linha.toString().split("\t");
			
			/* Testing attributes */
			flag = true;
			for( int i=3; i<7; i++ ) {
				if ( Double.parseDouble( temp[i]) != attributes[i] ) {
					flag = false;
					break;
				}
			}
			linha.delete(0, linha.length());
			if ( flag )
				return temp[1] + "," + temp[2];
				
			
		}
		
		return null;		
	}
	
	/*
	 * Este método recebe o arquivo resultante do treinamento no Weka e substitui a ","
	 * por "\t". Isto é necessário para poder carregar o arquivo no MySQL, tabela
	 * predicted.
	 */
	public static void replaceCommaByTab(String drive ){
		/* Declaracoes para arquivo de entrada e saida */
		BufferedReader br = null;
		File fileIn = new File(drive + "/temp/Weka Experiment Files/mtree-data-predicted-02-M02.arff");
		
		BufferedWriter bw = null;
		File fileOut = new File(drive + "/temp/data-test-predicted");
		
		/* variaves de apoio */
		int progress=0;
		StringBuilder linha = new StringBuilder();
		StringBuilder res = new StringBuilder();
		String[] temp = null;
		
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			
			while (br.ready()){
				progress++;
				linha.append(br.readLine()); 
				temp = linha.toString().split(",");
				
				for (int i=0; i< temp.length; i++) {
					res.append( temp[i]);
					if ( i == temp.length - 1)
						break;
					else
						res.append("\t");
				}
				
				bw.write( res.toString());
				
				bw.newLine();
				bw.flush();
				
				System.out.println("Progresso: " + progress);
				/* Esvaziando o StringBuffer*/
				linha.delete(0, linha.length());
				res.delete(0, res.length());
			}
			br.close();
			bw.close();
			System.out.println("Operacao Concluida...");
		} catch (IOException ioe ){
			ioe.printStackTrace();
		}
	}
	
	private static void confusionMatrix( String drive, String fileName ){
		/* Declaracoes para arquivo de entrada e saida */
		BufferedReader brOne = null;
		File fileOne = new File(drive + "/temp/" + fileName);
			
		/* variaves de apoio */
		int count=0;
		StringBuilder linha = new StringBuilder();
		String[] temp = null;
				
		
		int fp=0, fn=0, tp=0, tn=0;
		
		try {
			brOne = new BufferedReader(new InputStreamReader(new FileInputStream( fileOne ), "UTF8"));

			
			
			while (brOne.ready()){
				count++;
				linha.append(brOne.readLine());		
				temp = linha.toString().split(",");


				
				if ( count < 16 ) { // anterior: count < 12
					linha.delete(0, linha.length());
					continue;
				}
				

				if ( temp[4].trim().equals("yes") && temp[5].trim().equals("yes"))
					tp++;
				else if ( temp[4].trim().equals("no") && temp[5].trim().equals("no"))
					tn++;
				else if ( temp[4].trim().equals("yes") && temp[5].trim().equals("no"))
					fp++;
				else if ( temp[4].trim().equals("no") && temp[5].trim().equals("yes"))
					fn++;
				
//				System.out.println("Progresso: " + count);
				/* Esvaziando o StringBuffer*/
				linha.delete(0, linha.length());
			}
			double tpr, tnr, fpr, fnr, precision, recall,fMeasure,avgTpr=0, avgFpr=0,avgPrecision=0,
					avgRecall=0,avgFMeasure=0;
			double weightYes, weightNo;
			
			tpr = (double)tp/(tp+fn);
			tnr = (double)tn/(tn+fp);
			fpr = (double)fp/(fp+tn);
			fnr = (double)fn/(fn+tp);
			precision = (double)tp/(tp+fp);
			recall = (double)tp/(tp+fn);
			fMeasure = (double)2 * ((precision * recall)/(precision+recall));

			System.out.println("Evaluation for file " + fileName);
			System.out.println("------------------------------------------------------------------------");
			
			System.out.println("True Positive...: " + tp );
			System.out.println("True Negative...: " + tn );
			System.out.println("False Positive..: " + fp );
			System.out.println("False Negative..: " + fn );
			System.out.println("Total...........: " + (int)(tp+tn+fp+fn));
			brOne.close();
			
			weightYes = ((double)(tp + fn))/ (tp+tn+fp+fn);
			weightNo  = ((double)(tn + fp))/ (tp+tn+fp+fn);
			
			avgTpr = tpr * weightYes;
			avgFpr = fpr * weightYes;
			avgPrecision = precision * weightYes;
			avgRecall = recall * weightYes;
			avgFMeasure = fMeasure * weightYes;
			
			DecimalFormat decimal = new DecimalFormat( "0.000" );
			System.out.println("             TP Rate\tFP Rate\tPrecision\tRecall\tF-Measure\tClass");
			System.out.println("             =======\t=======\t=========\t======\t=========\t======");
			System.out.println("             " + decimal.format(tpr).replace(',','.') + "\t" +
												 decimal.format(fpr).replace(',','.') + "\t" +
												 decimal.format(precision).replace(',','.') + "\t\t" +
												 decimal.format(recall).replace(',','.') + "\t" +
												 decimal.format(fMeasure).replace(',','.') + "\t\t" +
												 "yes"
								);

			tpr = (double)tn/(tn+fp);
			tnr = (double)tn/(tn+fp);
			fpr = (double)fn/(fn+tp);
			fnr = (double)fn/(fn+tp);
			precision = (double)tn/(tn+fn);
			recall = (double)tn/(tn+fp);
			fMeasure = (double)2 * ((precision * recall)/(precision+recall));

			avgTpr = avgTpr + (tpr *  weightNo);
			avgFpr = avgFpr + (fpr *  weightNo);
			avgPrecision = avgPrecision + (precision *  weightNo);
			avgRecall = avgRecall + (recall *  weightNo);
			avgFMeasure = avgFMeasure + (fMeasure *  weightNo);

			System.out.println("             " + decimal.format(tpr).replace(',','.') + "\t" +
					 							 decimal.format(fpr).replace(',','.') + "\t" +
					 							 decimal.format(precision).replace(',','.') + "\t\t" +
					 							 decimal.format(recall).replace(',','.') + "\t" +
					 							 decimal.format(fMeasure).replace(',','.') + "\t\t" +
					 							 "no"
							);
			System.out.print("Weighted Avg");
			
			System.out.println(" " + decimal.format(avgTpr).replace(',','.') + "\t" +
					 				 decimal.format(avgFpr).replace(',','.') + "\t" +
					 				 decimal.format(avgPrecision).replace(',','.') + "\t\t" +
					 				 decimal.format(avgRecall).replace(',','.') + "\t" +
					 				 decimal.format(avgFMeasure).replace(',','.') 
					 		);
			

		} catch (IOException ioe ){
			ioe.printStackTrace();
		}		
		
		
	}

	/*
	 * Metodos:
	 * 11 - MY_TREE_ONE_MODIFIED
	 * 21 - MY_TREE_TWO_MODIFIED
	 * 10 - MY_TREE_ONE_ORIGINAL
	 * 20 - MY_TREE_ONE_ORIGINAL
	 */
	public static void myDecisionTree(String drive, String fileName, int method ){
		/* Declaracoes para arquivo de entrada e saida */
		BufferedReader br = null;
		File fileIn = new File(drive + "/temp/" + fileName);
		
		BufferedWriter bw = null;
		File fileOut = new File(drive + "/temp/data-predicted-mytree.arff");
		
		/* variaves de apoio */
		int progress=0;
		StringBuilder linha = new StringBuilder();
		StringBuilder res = new StringBuilder();
		String[] temp = null;
		
		double attLev=0, attCosTag=0, attCoocurrence=0, attCosRes=0;
		String prediction;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			
			while (br.ready()){
				progress++;
				linha.append(br.readLine()); 
				temp = linha.toString().split(",");
				
				if ( progress == 8) {
					bw.write( linha.toString());
					bw.newLine();
					
					bw.write("@attribute predictedareSynonym {yes,no}");
					bw.newLine();
					bw.flush();
					linha.delete(0, linha.length());
					continue;
					
				} else  if ( progress < 15 ) {
					bw.write( linha.toString());
					bw.newLine();
					bw.flush();
					linha.delete(0, linha.length());
					continue;
				}
				
				if ( progress == 71 )
					System.out.println("cheguei");
				
				attLev = Double.parseDouble( temp[0]);
				attCosTag = Double.parseDouble( temp[1]);
				attCosRes = Double.parseDouble( temp[2]);
				attCoocurrence = Double.parseDouble( temp[3]);
				
				if ( method == MY_TREE_ONE_MODIFIED) 
					prediction = predictionModifiedMethodOne( attLev, attCosTag, attCoocurrence);
				else if ( method == MY_TREE_TWO_MODIFIED)
					prediction = predictionModifiedMethodTwo( attLev, attCosTag, attCoocurrence);
				else if ( method == MY_TREE_ONE_ORIGINAL )
					prediction = predictionOriginalMethodOne( attLev, attCosTag, attCoocurrence);
				else if ( method == MY_TREE_TWO_ORIGINAL ) 
					prediction = predictionOriginalMethodTwo( attLev, attCosTag, attCoocurrence);
				else if ( method == MY_TREE_FINAL_MODIFIED )
					prediction = predictionModifiedFinalMethod ( attLev, attCosTag, attCosRes, attCoocurrence);
				else if ( method == MY_TREE_FINAL_ORIGINAL)
					prediction = predictionOriginalFinalMethod ( attLev, attCosTag, attCosRes, attCoocurrence);
				else 
					prediction = predictionOriginalMethodThree(attLev, attCosTag, attCosRes, attCoocurrence);
				
				bw.write( temp[0] + "," + temp[1].trim() + "," + temp[2].trim() + "," + temp[3].trim() + "," +
						  prediction + "," + temp[4].trim());
				bw.newLine();
				bw.flush();
				
				System.out.println("Progresso: " + progress);
				/* Esvaziando o StringBuffer*/
				linha.delete(0, linha.length());
				res.delete(0, res.length());
			}
			br.close();
			bw.close();
			System.out.println("Operacao Concluida...");
		} catch (IOException ioe ){
			ioe.printStackTrace();
		}

	}
	
	private static String predictionOriginalMethodOne(double attLev, double attCosTag, double attCoocurrence){
		String prediction;
		if (Double.doubleToLongBits( attCoocurrence) <= Double
			.doubleToLongBits( 0.000521d))
			prediction = "yes";
		else { 
			if (Double.doubleToLongBits( attCosTag) >  Double
					.doubleToLongBits( 0.000d))
				prediction = "no";
			else {
				if (Double.doubleToLongBits( attLev) <= Double
					.doubleToLongBits( 0.6429d))
					prediction = "yes";
				else {
					if (Double.doubleToLongBits( attCoocurrence) <= Double
							.doubleToLongBits( 0.01231d))
						prediction = "no";
					else
						prediction = "yes";								
				} // end if leve <= 0.6429
			} // end if cosTag > 0
		} // end if Lev <= 0.000521

		return prediction;
	}

	/* */
	private static String predictionModifiedMethodOne(double attLev, double attCosTag, double attCoocurrence){
		String prediction;
		/* Arvore de Decisao modificada */
		if (Double.doubleToLongBits( attLev) <= Double
				.doubleToLongBits( 0.20d)) 
			prediction = "yes";
		else {
			if (Double.doubleToLongBits( attCoocurrence) <= Double
				.doubleToLongBits( 0.000521d))
				prediction = "yes";
			else { 
				if (Double.doubleToLongBits( attCosTag) >  Double
						.doubleToLongBits( 0.000d))
					prediction = "no";
				else {
					if (Double.doubleToLongBits( attLev) <= Double
						.doubleToLongBits( 0.6429d))
						prediction = "yes";
					else {
						if (Double.doubleToLongBits( attCoocurrence) <= Double
								.doubleToLongBits( 0.01231d))
							prediction = "no";
						else
							prediction = "yes";								
					} // end if leve <= 0.6429
				} // end if cosTag > 0
				
			} // end if Lev <= 0.000521
		} // end else root
		return prediction;
	}

	private static String predictionOriginalMethodTwo(double attLev, double attCosTag, double attCoocurrence){
		String prediction;
		if (Double.doubleToLongBits( attCoocurrence) <= Double
			.doubleToLongBits( 0.00043d))
			prediction = "yes";
		else { 
			if (Double.doubleToLongBits( attCosTag) >  Double
					.doubleToLongBits( 0.000d))
				prediction = "no";
			else {
				if (Double.doubleToLongBits( attCoocurrence) <= Double
					.doubleToLongBits( 0.000754d))
					prediction = "no";
				else {
					if (Double.doubleToLongBits( attLev) <= Double
							.doubleToLongBits( 0.6923d))
						prediction = "yes";
					else {
						if (Double.doubleToLongBits(attLev) <= Double
							.doubleToLongBits(0.7619d))
							prediction = "no";
						else
							prediction = "yes";
					} // end if Lev <= 0.7619
				} // end if coocurrence <= 0.000754
			} // end if cosTag > 0
		} // end if attCoocurrence <= 0.00043

		return prediction;
	}

	private static String predictionModifiedMethodTwo(double attLev, double attCosTag, double attCoocurrence){
		String prediction;
		/* Arvore de Decisao modificada */
		if (Double.doubleToLongBits( attLev) <= Double
				.doubleToLongBits( 0.20d)) 
			prediction = "yes";
		else {
			if (Double.doubleToLongBits( attCoocurrence) <= Double
				.doubleToLongBits( 0.00043d))
				prediction = "yes";
			else { 
				if (Double.doubleToLongBits( attCosTag) >  Double
						.doubleToLongBits( 0.000d))
					prediction = "no";
				else {
					if (Double.doubleToLongBits( attCoocurrence) <= Double
						.doubleToLongBits( 0.000754d))
						prediction = "no";
					else {
						if (Double.doubleToLongBits( attLev) <= Double
								.doubleToLongBits( 0.6923d))
							prediction = "yes";
						else {
							if (Double.doubleToLongBits(attLev) <= Double
								.doubleToLongBits(0.7619d))
								prediction = "no";
							else
								prediction = "yes";
						} // end if Lev <= 0.7619
					} // end if coocurrence <= 0.000754
				} // end if cosTag > 0
			} // end if attCoocurrence <= 0.00043
		} // end else root Lev <= 0.20
		return prediction;
	}

	
	
	private static String predictionOriginalFinalMethod(double attLev, double attCosTag, double attCosRes, double attCoocurrence){
		String prediction;
		if (Double.doubleToLongBits( attCoocurrence) <= Double
			.doubleToLongBits( 0.000612d))
			if (Double.doubleToLongBits( attLev) <= Double
					.doubleToLongBits( 0.4762d))
				if (Double.doubleToLongBits( attLev) <= Double
						.doubleToLongBits( 0.375d))
					prediction = "yes";
				else {
					if (Double.doubleToLongBits( attLev) <= Double
							.doubleToLongBits( 0.404865d))
						prediction = "no";
					else {
						prediction = "yes";
					} // end lev <= 0.404865
				} // end lev <= 0.375
			else{
				if (Double.doubleToLongBits( attCosRes) <= Double
						.doubleToLongBits( 0.0082d))
					prediction = "no";
				else {
					if (Double.doubleToLongBits( attCoocurrence) > Double
							.doubleToLongBits( 0.00d))
						prediction = "yes";
					else {
						if (Double.doubleToLongBits( attLev) > Double
								.doubleToLongBits( 0.9091d))
							prediction = "yes";
						else {
							if (Double.doubleToLongBits( attCosTag) > Double
									.doubleToLongBits( 0.1763d))
								prediction = "no";
							else {
								if (Double.doubleToLongBits( attCosRes) <= Double
										.doubleToLongBits( 0.009d))
									prediction = "no";
								else
									prediction = "yes";
							} // end cosTag > 0.1763
							
						} // end lev > 0.9091
							
					} // coocurrence > 0
				} // end cosRes <= 0.0082
				
			} // end lev <= 0.4762
				
		else { 
			if (Double.doubleToLongBits( attLev) >  Double
					.doubleToLongBits( 0.535786d))
				prediction = "no";
			else {
				if (Double.doubleToLongBits( attCosRes) <= Double
					.doubleToLongBits( 0.1166d))
					prediction = "no";
				else {
					if (Double.doubleToLongBits( attCosTag) > Double
							.doubleToLongBits( 0.4895d))
						prediction = "no";
					else {
						if (Double.doubleToLongBits( attLev) <= Double
								.doubleToLongBits( 0.3333d))
							prediction = "no";
						else {
							if (Double.doubleToLongBits( attCosRes) <= Double
									.doubleToLongBits( 0.5797d))
								prediction = "yes";
							else
								prediction = "no";
						}
						
					}								
				} // end if leve <= 0.6429
			} // end if cosTag > 0
		} // end if coocur <= 0.000612

		return prediction;
	}

	private static String predictionModifiedFinalMethod(double attLev, double attCosTag, double attCosRes, double attCoocurrence){
		String prediction;
		if (Double.doubleToLongBits( attLev) <= Double
				.doubleToLongBits( 0.25d))
			prediction = "yes";
		else {
			if (Double.doubleToLongBits( attCoocurrence) <= Double
				.doubleToLongBits( 0.000612d))
				if (Double.doubleToLongBits( attLev) <= Double
						.doubleToLongBits( 0.4762d))
					if (Double.doubleToLongBits( attLev) <= Double
							.doubleToLongBits( 0.375d))
						prediction = "yes";
					else {
						if (Double.doubleToLongBits( attLev) <= Double
								.doubleToLongBits( 0.404865d))
							prediction = "no";
						else {
							prediction = "yes";
						} // end lev <= 0.404865
					} // end lev <= 0.375
				else{
					if (Double.doubleToLongBits( attCosRes) <= Double
							.doubleToLongBits( 0.0082d))
						prediction = "no";
					else {
						if (Double.doubleToLongBits( attCoocurrence) > Double
								.doubleToLongBits( 0.00d))
							prediction = "yes";
						else {
							if (Double.doubleToLongBits( attLev) > Double
									.doubleToLongBits( 0.9091d))
								prediction = "yes";
							else {
								if (Double.doubleToLongBits( attCosTag) > Double
										.doubleToLongBits( 0.1763d))
									prediction = "no";
								else {
									if (Double.doubleToLongBits( attCosRes) <= Double
											.doubleToLongBits( 0.009d))
										prediction = "no";
									else
										prediction = "yes";
								} // end cosTag > 0.1763
								
							} // end lev > 0.9091
								
						} // coocurrence > 0
					} // end cosRes <= 0.0082
					
				} // end lev <= 0.4762
					
			else { 
				if (Double.doubleToLongBits( attLev) >  Double
						.doubleToLongBits( 0.535786d))
					prediction = "no";
				else {
					if (Double.doubleToLongBits( attCosRes) <= Double
						.doubleToLongBits( 0.1166d))
						prediction = "no";
					else {
						if (Double.doubleToLongBits( attCosTag) > Double
								.doubleToLongBits( 0.4895d))
							prediction = "no";
						else {
							if (Double.doubleToLongBits( attLev) <= Double
									.doubleToLongBits( 0.3333d))
								prediction = "no";
							else {
								if (Double.doubleToLongBits( attCosRes) <= Double
										.doubleToLongBits( 0.5797d))
									prediction = "yes";
								else
									prediction = "no";
							}
							
						}								
					} // end if leve <= 0.6429
				} // end if cosTag > 0
			} // end if coocur <= 0.000612
		} // end lev <= 0.25

		return prediction;
	}
	
	
	private static String predictionOriginalMethodThree(double attLev, double attCosTag, double attcosRes, double attCoocurrence){
		String prediction;
		
		if (Double.doubleToLongBits( attLev) <= Double
			.doubleToLongBits( 0.373105d)) // 0.373105d
			prediction = "yes";
		else { 
			if (Double.doubleToLongBits( attCosTag) <=  Double
					.doubleToLongBits( 0.0102d)) {

				if (Double.doubleToLongBits( attLev) <=  Double
						.doubleToLongBits( 0.903008d)) {
					prediction = "no";
				} else {
					
					if (Double.doubleToLongBits( attCosTag) <=  Double
							.doubleToLongBits( 0.005415d)) {
						prediction = "yes";
					} else {
						prediction = "no";
					}
				} // Lev <= 0.903008
				
			} else {
				if (Double.doubleToLongBits( attCoocurrence) <=  Double
						.doubleToLongBits( 0.000437d)) {
					prediction = "yes";
				} else {
					prediction = "no";
				}
				
			} // end if cosTag <= 0.0102
		} // end if lev <= 0.25

		return prediction;
	}

	
	public static void mapAttributesNo( String drive, String fileSource ){
		/* Declaracoes para arquivo de entrada e saida */
		BufferedReader brOne = null;
		File fileOne = new File(drive + "/temp/" + fileSource);
		BufferedReader brTwo = null;
		File fileTwo = new File(drive + "/temp/bibsonomy/Instances_Supervisioneds_No");
		
		BufferedWriter bwLog = null;
		File fileLog = new File(drive + "/temp/Log-Alex-no.txt");
		BufferedWriter bw = null;
		File fileOut = new File(drive + "/temp/" + fileSource + "-Tags-No");

			
		/* variaves de apoio */
		int count=0, totFound=0, totNotFound=0;
		StringBuilder linha = new StringBuilder();
		String[] temp = null;
		double attribute[]= new double[4];

		
		/* carregando instancias "no" para o ArrayList */
		ArrayList<Instancia> nao = new ArrayList<Instancia>();
		try {
			brTwo = new BufferedReader(new InputStreamReader(new FileInputStream( fileTwo ), "UTF8"));
			while ( brTwo.ready()){
				linha.append(brTwo.readLine());		
				temp = linha.toString().split("\t");
				
				nao.add( new Instancia(temp[0], temp[1], temp[2], temp[3], temp[4], temp[5], temp[6]));
				linha.delete(0, linha.length());
			}
			brTwo.close();
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
		System.out.println("Instancias para No carregadas....");
		
		Instancia ins = new Instancia();
		try {
			brOne = new BufferedReader(new InputStreamReader(new FileInputStream( fileOne ), "UTF8"));		
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			bwLog = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileLog),"UTF8"));
			
			while (brOne.ready()){
				count++;
				linha.append(brOne.readLine());		
				temp = linha.toString().split(",");
				
				if ( count < 15 ) {
					linha.delete(0, linha.length());
					continue;
				}
					
				
				if ( !temp[4].trim().equals("no") ){
					linha.delete(0, linha.length());
					continue;
				}
				
				/* Filling attributes */
				for( int i=0; i<attribute.length; i++ )
					attribute[i] = Double.parseDouble( temp[i] );
				
				
				/* Bloco de código para testar o contains do ArrayList()  */
				ins.mapInto( attribute);
				if ( nao.contains( ins )) {
					int index = nao.indexOf( ins );
					Instancia i = nao.get(index);
					bw.write(i.getRecord()+"\t"+i.getTagOne()+"\t"+i.getTagTwo()+"\t"+
							i.getLev() + "\t" + i.getCostag() + "\t" + i.getCosres() + "t" +
							i.getCoocurrence() + "\t" + "no" );
		            bw.newLine();
					bw.flush();
					totFound++;
				} else {
					bwLog.write(linha.toString());
					bwLog.newLine();
					bwLog.flush();
					totNotFound++;
				}
				/***************** fim do bloco do contains() ****************/
				System.out.println("Progresso: " + count);
				/* Esvaziando o StringBuffer*/
				linha.delete(0, linha.length());
			}
			brOne.close();
			bwLog.close();
			bw.close();
			System.out.println("Operacao Concluida...");
			System.out.println("Total de Instancias encontradas......: " + totFound);
			System.out.println("Total de Instancias nao encontradas..: " + totNotFound);
			System.out.println("Total de Instancias Processadas......: " + (int)(totNotFound + totFound));
		} catch (IOException ioe ){
			ioe.printStackTrace();
		}		
		
		
	}

	/*
	 * OBJETIVO:
	 * 		Identificar os pares de tags que foram avaliados corretamente ou incorretamente
	 * 		a partir de um arquivo de predição no formato ARFF gerado pelo Weka
	 * SITUACAO:
	 * 		[Ativa]
	 * DATA:
	 * 		criacao..: 26/02/2012
	 * 		alteracao: 26/02/2012
	 * ENTRADA:
	 *      drive : unidade em que se encontra os diretorios de leitura e escrita
	 * 		number: Numero do arquivo do SMOTE
	 *      inputFile: o arquivo que contem o resultado da predicao no Weka
	 *      rawFile  : o arquivo que foi usado para regar o arquivo ARFF. Neste arquivo,
	 *      é possivel identificar os pares de tags que foram utilizados para o ARFF.
	 * SAIDA:
	 * 		Um arquivo .txt com a análise das predições
	 *      Este arquivo tem o seguinte padrao: "Resultado-SMOTE-??.txt"
	 */
	public static void showPredictedSynonym(String drive, String number, String inputFile, String rawFile  ){
		/* Declaracoes para arquivo de entrada e saida */
		BufferedReader brOne = null;
//		File fileOne = new File(drive + "/temp/predicted-data-test-" + number + ".arff");
		File fileOne = new File(drive + "/temp/" + inputFile );
		
		// Este arquivo pega o nome das tags
		BufferedReader brTwo = null;
		File fileTwo = new File(drive + "/temp/bibsonomy/" + rawFile );
		
		BufferedWriter bw = null;
		File fileOut = new File(drive + "/temp/Resultado-SMOTE-NO-" + number + ".txt" );
		
		StringBuilder linha = new StringBuilder();
		StringBuilder outraLinha = new StringBuilder();
	
		
		String tempOne[], tempTwo[];
		int count=0;

		ArrayList<String> truePositive = new ArrayList<String>();
		ArrayList<String> falseNegative = new ArrayList<String>();
		ArrayList<String> falsePositive = new ArrayList<String>();
		
		try {
			brOne = new BufferedReader(new InputStreamReader(new FileInputStream( fileOne ), "UTF8"));		
			brTwo = new BufferedReader(new InputStreamReader(new FileInputStream( fileTwo ), "UTF8"));
			
			
			// Pulando as linhas de cabecalho do ARFF
			while (brOne.ready() ){
				count++;
				linha.append(brOne.readLine());		
				linha.delete(0, linha.length());
				if ( count >= 10 )
					break;
			}
			
//			count = 0;
//			while (brTwo.ready() ){
//				count++;
//				linha.append(brTwo.readLine());	
//				linha.delete(0, linha.length());
//				if ( count >= 2 )
//					break;
//			}
			
			count = 0;	
			while ( brOne.ready() && brTwo.ready()){
				count++;
				linha.append(brOne.readLine());
				tempOne = linha.toString().split(",");
				
				outraLinha.append( brTwo.readLine());
				tempTwo = outraLinha.toString().split("\t");

				// Acertos para no   & no
				if (tempOne[5].trim().equalsIgnoreCase("no") && 
					tempOne[4].trim().equalsIgnoreCase("no")){
					linha.delete(0, linha.length());
					outraLinha.delete(0, outraLinha.length());
					truePositive.add( tempTwo[1] + "\t" + tempTwo[2]);
					continue;
				}
				
//				//  yes  &  yes
//				if ( tempOne[5].trim().equalsIgnoreCase(tempOne[4])) {
//					truePositive.add( tempTwo[1] + "\t" + tempTwo[2]);
//					
//				} else if ( tempOne[5].trim().equalsIgnoreCase("yes") && 
//							tempOne[4].trim().equalsIgnoreCase("no")) {
//					
//					falseNegative.add( tempTwo[1] + "\t" + tempTwo[2]);				
//				} else 
//					falsePositive.add( tempTwo[1] + "\t" + tempTwo[2]);
//				
				System.out.println("Progresso: " + count);
				/* Esvaziando o StringBuffer*/
				linha.delete(0, linha.length());
				outraLinha.delete(0, outraLinha.length());
			}
	
		
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			
			bw.write("Summary"); bw.newLine();
			bw.write("==========================="); bw.newLine();
			bw.write("TRUE POSITIVES...: " + truePositive.size()); bw.newLine();
			bw.write("FALSE NEGATIVE...: " + falseNegative.size()); bw.newLine();
			bw.write("FASE  POSITIVE...: " + falsePositive.size()); bw.newLine();
			bw.write("==========================="); 
			bw.newLine();bw.newLine();
			
			Iterator<String> it = truePositive.iterator();
			
			linha.delete(0, linha.length());
			bw.write("Classificacoes corretas - TRUE POSITIVES: " + truePositive.size());
			bw.newLine();
			bw.write("==========================================================");
			bw.flush();
			bw.newLine();
			
			while ( it.hasNext() ){
				linha.append( it.next());
				bw.write( linha.toString());
				bw.flush();
				bw.newLine();
				linha.delete(0, linha.length());
			}
			
			System.exit(0);
			
			it = falseNegative.iterator();
			
			linha.delete(0, linha.length());
			bw.newLine();
			bw.newLine();
			bw.write("Classificacoes Perdidas - FALSE NEGATIVES: " + falseNegative.size());
			bw.newLine();
			bw.write("=============================================================");
			bw.flush();
			bw.newLine();
			
			while ( it.hasNext() ){
				linha.append( it.next());
				bw.write( linha.toString());
				bw.flush();
				bw.newLine();
				linha.delete(0, linha.length());
			}
			
			
			it = falsePositive.iterator();
			
			linha.delete(0, linha.length());
			bw.newLine();
			bw.newLine();
			bw.write("Classificacoes Incorretas - FALSE POSITIVE: " + falsePositive.size());
			bw.newLine();
			bw.write("==============================================================");
			bw.flush();
			bw.newLine();
			
			while ( it.hasNext() ){
				linha.append( it.next());
				bw.write( linha.toString());
				bw.flush();
				bw.newLine();
				linha.delete(0, linha.length());
			}

			
			brOne.close();
			brTwo.close();
			bw.close();
			System.out.println("Operacao concluida....");
	
		} catch (IOException ioe ){
			ioe.printStackTrace();
		}		

	}
	
	/*
	 * Este método transforma o ARFF predicted-data-test-###.arff para o formato de arquivo
	 * em que é possível saber quais sao as tags de mapeamento e o registro em que se
	 * encontra.
	 */
	public static void mapARFFpredictedToTags(String drive, String number ){
		/* Declaracoes para arquivo de entrada e saida */
		BufferedReader brOne = null;
		File fileOne = new File(drive + "/temp/predicted-data-test-" + number + ".arff");
		BufferedReader brTwo = null;
		File fileTwo = new File(drive + "/temp/bibsonomy/S01-data-test-03");
		
		BufferedWriter bw = null;
		File fileOut = new File(drive + "/temp/S01-predicted-data-test-tags" + number );
		
		StringBuilder linha = new StringBuilder();
		StringBuilder outraLinha = new StringBuilder();
	
		
		String tempOne[], tempTwo[];
		int count=0;

				
		try {
			brOne = new BufferedReader(new InputStreamReader(new FileInputStream( fileOne ), "UTF8"));		
			
			while (brOne.ready() ){
				count++;
				linha.append(brOne.readLine());		
				linha.delete(0, linha.length());
				if ( count >= 10 )
					break;
			}
			
			brTwo = new BufferedReader(new InputStreamReader(new FileInputStream( fileTwo ), "UTF8"));
			
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			
			count = 0;	
			while ( brOne.ready() && brTwo.ready()){
				count++;
				linha.append(brOne.readLine());
				tempOne = linha.toString().split(",");
				
				outraLinha.append( brTwo.readLine());
				tempTwo = outraLinha.toString().split("\t");

				bw.write(tempTwo[0] + "\t" + tempTwo[1] + "\t" + tempTwo[2] + "\t" +
						 tempTwo[3] + "\t" + tempTwo[4] + "\t" + tempTwo[5] + "\t" +
						 tempTwo[6] + "\t" + tempOne[4] + "\t" + tempOne[5] );
				bw.newLine();
				bw.flush();
				
				System.out.println("Progresso: " + count);
				/* Esvaziando o StringBuffer*/
				linha.delete(0, linha.length());
				outraLinha.delete(0, outraLinha.length());
			}
			
			brOne.close();
			brTwo.close();
			bw.close();
			System.out.println("Operacao concluida....");
	
		} catch (IOException ioe ){
			ioe.printStackTrace();
		}		

	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		Evaluation.showTags("d:", "data-test-predicted2-dump");
//		Evaluation.doTest("d:");
//		Evaluation.findTagsYes("d:", "data-test-predicted-dump");
//		Evaluation.findTagsNo("d:", "data-test-predicted-dump");
//		Evaluation.mapAttributesNo("d:", "data-training-SMOTE-HEAD.arff");
//		Evaluation.myDecisionTree("G:", "data-test-03.arff", MY_TREE_LAST_ORIGINAL);
//		Evaluation.confusionMatrix("d:", "mtree-data-predicted-01-M02.arff");
//		Evaluation.confusionMatrix("G:", "data-predicted-mytree.arff");
//		Evaluation.replaceCommaByTab("d:");
		Evaluation.showPredictedSynonym("g:", "14", 
				                        "14-data-test-04-reClassified-predicted.arff",
				                        "14-data-test-04-reClassified");
//		Evaluation.mapARFFpredictedToTags("g:", "13");
	}

}



class Instancia {
	private double lev;
	private double costag;
	private double cosres;
	private double coocurrence;
	private String tagOne;
	private String tagTwo;
	private int record;
	
	public Instancia(int record, String tagOne, String tagTwo, double lev, double costag, double cosres, double coocurrence ){
		this.record = record;
		this.tagOne = tagOne;
		this.tagTwo = tagTwo;
		this.lev = lev;
		this.costag = costag;
		this.cosres = cosres;
		this.coocurrence = coocurrence;
	}

	public Instancia(String record, String tagOne, String tagTwo, String lev, String costag, String cosres, String coocurrence ){
		this.record = Integer.parseInt(record);
		this.tagOne = tagOne;
		this.tagTwo = tagTwo;
		this.lev = Double.parseDouble(lev);
		this.costag = Double.parseDouble(costag);
		this.cosres = Double.parseDouble(cosres);
		this.coocurrence = Double.parseDouble(coocurrence);
	}
	public Instancia() {
		this.lev = this.costag = this.cosres = this.coocurrence = 0;
	}

	public String getTagOne() {
		return tagOne;
	}

	public void setTagOne(String tagOne) {
		this.tagOne = tagOne;
	}

	public String getTagTwo() {
		return tagTwo;
	}

	public void setTagTwo(String tagTwo) {
		this.tagTwo = tagTwo;
	}

	public int getRecord() {
		return record;
	}

	public void setRecord(int record) {
		this.record = record;
	}

	public double getLev() {
		return lev;
	}

	public void setLev(double lev) {
		this.lev = lev;
	}

	public double getCostag() {
		return costag;
	}

	public void setCostag(double costag) {
		this.costag = costag;
	}

	public double getCosres() {
		return cosres;
	}

	public void setCosres(double cosres) {
		this.cosres = cosres;
	}

	public double getCoocurrence() {
		return coocurrence;
	}

	public void setCoocurrence(double coocurrence) {
		this.coocurrence = coocurrence;
	}
	
	public boolean contem(double a1, double a2, double a3, double a4){
		if ( this.lev    == a1 && this.costag == a2 &&
			 this.cosres == a3 && this.coocurrence == a4)
			return true;
		else
			return false;
	}

	public void mapInto(double array[]){
		if (array.length != 4)
			return;
		this.lev = array[0];
		this.costag = array[1];
		this.cosres = array[2];
		this.coocurrence = array[3];
			
	}


	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(coocurrence);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(cosres);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(costag);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(lev);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (!(obj instanceof Instancia)) {
			return false;
		}
		
		Instancia other = (Instancia) obj;
		if (Double.doubleToLongBits(coocurrence) != Double
				.doubleToLongBits(other.coocurrence)) {
			return false;
		}
		if (Double.doubleToLongBits(cosres) != Double
				.doubleToLongBits(other.cosres)) {
			return false;
		}
		if (Double.doubleToLongBits(costag) != Double
				.doubleToLongBits(other.costag)) {
			return false;
		}
		if (Double.doubleToLongBits(lev) != Double.doubleToLongBits(other.lev)) {
			return false;
		}
		return true;
	}
	
	
}