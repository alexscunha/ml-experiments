package evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;



import util.Config;

/**
 * Classe que realiza a avaliação dos baselines usados como atributos no trabalho de
 * Aprendizado de Máquina, baseado na metodologia do arquivo publicado no IADIS 2012.
 * @author Alex
 * @version 1.0
 * Data da Ultima Atualização: 15/08/2013
 *
 */
public class Baselines {

	private static Config config = null;
	
	/**
	 * inicializa as configurações internas, encontradas no arquivo "config.da"
	 */
	public static void init(){
		loadConfig("config.dat");

	}

	/**
	 * Realiza a carga das configurações existentes em um arquivo de propriedades java.
	 * @param fileName O nome do arquivo de propriedade.
	 */
	public static void loadConfig( String fileName ){
		try {
			config = new Config( fileName );
		
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	
	
	/*
	 * 
	 * Caso positivo, retorna o indice da medida ou -1 em caso de erro.
	 */
	/**
	 * Recebe um BufferedReader para verificar se o argumento "measure" se encontra no cabecalho
	 * extraído pelo BufferedReader "br". Caso encontrado, retorna o indice da medida de
	 * acordo com a sequencia de atributos no cabeçalho.
	 * @param br O leitor bufferizado referente ao arquivo que contém o cabeçalho.
	 * @param measure A medida cuja existência será procurada no cabeçalho
	 * @return O índice da medida (0 a n-1). O valor -1 indica que a medida não foi encontrada
	 * @throws IOException A exceção lançada caso seja encontrado problemas na manipulação
	 * do aquivo.
	 */
	private static int indexOfMeasure( BufferedReader br, String measure ) throws IOException{
		StringBuilder linha = new StringBuilder();
		String[] token = null;
		int iMeasure = -1;		
		
		if( br.ready()){
			linha.append(br.readLine());		
			token = linha.toString().split("\t");
			if( !token[0].equals("#")){
				System.err.println("Arquivo de cabecalho nao encontrado");
				return iMeasure;
			} else {
				for(int i =0; i< token.length; i++ ){
					if( token[i].equalsIgnoreCase( measure )) {
						iMeasure = i;
						break;
					}
				}
				if ( iMeasure < 0 ) {
					System.err.println("Medida nao encontrada no arquivo de cabecalho" );
					return iMeasure;
				}
			}		
		} else {
			System.err.println("Problema na abertura do arquivo" );
			return iMeasure;
		}
		return iMeasure;
	}
	
	/**
	 * Calcula e avalia o F-Measure para qualquer medida utilizada como baseline. Internamente,
	 * o método executa uma variação de threshold no intervalo [0.0,1.0[, com incrementos de 0.1.
	 * São consideradas todas as instâncias cujo valor da medida >= que o threshold corrente.
	 * @param inputFile O arquivo de instancias BASIC FORMAT o qual será utilizado para
	 * fins de avaliação
	 * @param measure A medida que se deseja fazer a avaliação. A medida deve estar contida
	 * @param desiredThreshold Um array com os valores de threshold desejados.
	 * no cabeçalho do arquivo "inputFile".
	 * OBS:
	 * O método gera um arquivo de saída denominado "[xx-xx]-measure-evaluation", com o resultado
	 * da avaliacao da medida.
	 */
	public static void showEvaluation(String inputFile, String measure, float desiredThreshold[]){
		/* Declaracoes para arquivo de entrada e saida */
		BufferedReader brOne = null;
		File fileOne = new File( config.getProperty("drive") + config.getProperty("inputDir") + inputFile);
		
		/* variaves de apoio */
//		float range[][]=null;		
//		float range[] = { 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f};
//		float range[] = { 0.02f, 0.04f, 0.07f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f,0.8f, 0.9f};
		float range[] = desiredThreshold;
		StringBuilder linha = new StringBuilder();
		String[] token = null;
		int iMeasure;    /* indice da medida no cabecalho */
		double valor;    /* Valor da medida convertido de String para double */
		float threshold; /* O threshold incremental */
		DecimalFormat decimal = new DecimalFormat( "0.000" );	
		DecimalFormat integer = new DecimalFormat("0000");
		
		NumberFormat percentFormat = NumberFormat.getPercentInstance();
		percentFormat.setMaximumFractionDigits(1);
		percentFormat.setMaximumFractionDigits(1);
		
		StringBuilder content = new StringBuilder();
		StringBuilder confusionMatrix = new StringBuilder();
		
		float bestF1,bestThreshold;
	
		
		try {
			/* Verificando se a medida se encontra no cabecalho do arquivo */
			brOne = new BufferedReader(new InputStreamReader(new FileInputStream( fileOne ), "UTF8"));
			iMeasure = indexOfMeasure( brOne, measure );
			
			if( iMeasure < 0 ){
				System.out.println("ERRO em showEvaluation(): medida \"" + measure + "\" nao foi encontrada no cabecalho.");
				brOne.close();
				return;
			}
			brOne.close();
			
	
			content.append("Measure: [" + measure + "]\tFile: " + inputFile + "\n");
			content.append("threshold\tTP\tFP\tFN\t#Positives\tPrecision\tRecall\tFMeasure\tSel/Descart(total)\n");
			content.append(new String(new char[120]).replace("\0", "-")+"\n");
			System.out.print( content.toString());

			bestF1=bestThreshold=0;
			for(int i=0; i<range.length; i++) {
				
				threshold = range[i];
				
				int TP=0, FP=0, TN=0, FN=0, selected=0;
				
				int descarte=0; /* Contador de instancias descartadas, cujo valor da medida é menor que o threshold*/
				@SuppressWarnings("unused")
				double precision, recall, fmeasure;
				
				brOne = new BufferedReader(new InputStreamReader(new FileInputStream( fileOne ), "UTF8"));
			
				while (brOne.ready()){
	
					linha.delete(0, linha.length());
					linha.append(brOne.readLine());		
					token = linha.toString().split("\t");
				
					if ( token[0].equals("#") || token[0].equals("%"))
						continue;
				
					valor = Double.parseDouble( token[iMeasure]);
						
					if ( valor < threshold ) {
						descarte++;
						if (token[token.length-1].equalsIgnoreCase(config.getProperty("positiveLabel"))){
							FN++;
							continue;
						} else if (token[token.length-1].equalsIgnoreCase(config.getProperty("negativeLabel"))) {
							TN++;
							continue;
						}
						
					}
					selected++;
					
					if (token[token.length-1].equalsIgnoreCase(config.getProperty("positiveLabel")))
						TP++;
					else if (token[token.length-1].equalsIgnoreCase(config.getProperty("negativeLabel")))
						FP++;
				} // fim while-br.ready()
				
				brOne.close();
				
				
				
				precision = (double)TP/(TP+FP);
				recall = (double)TP/(TP+FN);
				fmeasure = (double)2 * ((precision * recall)/(precision+recall));
				
				if( fmeasure > bestF1){
					bestF1 = (float)fmeasure;
				    bestThreshold = threshold;
					// Preparando a matriz de confusão
					confusionMatrix.delete(0, confusionMatrix.length());
					confusionMatrix.append("\n=== Confusion Matrix for " + threshold + " threshold ===\n");
					confusionMatrix.append("   a       b   |  <-- Classified as\n");
					confusionMatrix.append(" " + integer.format(TP) + "     " + integer.format(FN) + " | a = " + config.getProperty("positiveLabel") + "\n");
					confusionMatrix.append(" " + integer.format(FP) + "     " + integer.format(TN) + " | b = " + config.getProperty("negativeLabel") + "\n");
					confusionMatrix.append("-----------------------------------------------\n");
					confusionMatrix.append("Total de Instancias = " + (TP+FP+TN+FN) + "\n");
					confusionMatrix.append("Total de Positivas = " + (TP+FN) + " (" + percentFormat.format((double)(TP+FN)/(TP+FP+TN+FN)) + ")\n");
					confusionMatrix.append("Total de Negativas = " + (TN+FP) + " (" + percentFormat.format((double)(TN+FP)/(TP+FP+TN+FN)) + ")\n");
				}
				
				// Para o arquivo texto
				content.append(threshold + "\t" + TP + "\t" + FP + "\t" + FN + "\t" + (TP+FN) + "\t" +
						       (Double.isNaN(precision)?decimal.format(0):decimal.format(precision)) + "\t" +
						       decimal.format(recall) + "\t" +
						       (Double.isNaN(fmeasure)?decimal.format(0):decimal.format(fmeasure)) + "\t" +
						       selected +"/"+ descarte + "(" + (TP+TN+FP+FN) + ")\n");
				// Saída na tela
				System.out.print(threshold + "\t\t");
				System.out.print(TP + "\t");
				System.out.print(FP + "\t");
				System.out.print(FN + "\t");
				System.out.print(TP+FN + "\t\t");
				System.out.print((Double.isNaN(precision)?decimal.format(0):decimal.format(precision)) + "\t\t");
				System.out.print(decimal.format(recall) + "\t");
//				if( Double.isNaN(fmeasure) )
//					fmeasure = 0.0f;
				System.out.print( (Double.isNaN(fmeasure)?decimal.format(0):decimal.format(fmeasure)) + "\t\t");
				System.out.print(selected +"/"+ descarte + "(" + (TP+TN+FP+FN) + ")\n");
												
			} // end-for
			System.out.println();
			System.out.println( confusionMatrix.toString());
			
			String outputFile = inputFile.substring(0,7)+"-"+measure+"-evaluation";
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputFile) )));
			bw.write(content.toString());
			bw.write(confusionMatrix.toString());
			bw.flush();
			bw.close();
			System.out.println("File successfuly saved: " + outputFile + "\n");
			
		} catch (IOException ioe ){
			ioe.printStackTrace();
		} // try-catch

	} // end- showevaluation()
			
	
	public static void groupByRange( String inputFile, String measure, String label ){
		/* Declaracoes para arquivo de entrada e saida */
		BufferedReader brOne = null;
		File fileOne = new File( config.getProperty("drive") + config.getProperty("inputDir") + inputFile);
		
		/* variaves de apoio */
		float range[] = { 0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f};
		int count[] = new int[range.length];
		StringBuilder linha = new StringBuilder();
		String[] token = null;
		double valor;    /* Valor da medida convertido de String para double */
		int iMeasure;    /* indice da medida no cabecalho */
						
		try {
			/* Verificando se a medida se encontra no cabecalho do arquivo */
			brOne = new BufferedReader(new InputStreamReader(new FileInputStream( fileOne ), "UTF8"));
			
			iMeasure = indexOfMeasure( brOne, measure );
			
			if( iMeasure < 0 ){
				System.out.println("ERRO em groupByRange(): medida \"" + measure + "\" nao foi encontrada no cabecalho.");
				brOne.close();
				return;
			}
			System.out.println("Analise para o arquivo " + inputFile);
			System.out.print("\nLendo registros...");
			int i, descarte=0, selected=0;			
			while (brOne.ready()){	
				linha.delete(0, linha.length());
				linha.append(brOne.readLine());		
				token = linha.toString().split("\t");
				
				if ( token[0].equals("#") || token[0].equals("%"))
						continue;
				
				// Descartar o label que nao corresponde ao filtro
				if( !token[token.length-1].equalsIgnoreCase(label)){
					descarte++;
					continue;
				}
					
				valor = Double.parseDouble( token[iMeasure]) * 10;

				
				i = (int) Math.floor(valor);
				count[ i ]++;
				
				selected++;
			} // while br.ready()	
			brOne.close();
			
			System.out.print("[done]\n");
			System.out.println("\tSelecionados com rotulo " + label + ": " + selected);
			System.out.println("\tDescartados  = " + descarte);
			System.out.println("======= Summary =========");
			for( i=0; i < range.length-1; i++ ){
				System.out.printf("[%3.1f|%3.1f[ ",range[i], range[i+1]);
			}			
			System.out.println();
			for( i=0; i < range.length-1; i++ )
				System.out.print("+-------+ ");
			System.out.println();
			for( i=0; i < range.length-1; i++ ){
				System.out.printf("  %04d    ", count[i]);
			}			
			System.out.println();
			System.out.print("Validação: Soma = ");
			int soma = 0;
			for( i=0; i < range.length; i++ ){
				soma += count[i];
			}		
			System.out.print( soma);

		} catch (IOException ioe ){
			ioe.printStackTrace();
		} // try-catch

	} // end-groupByRange()
	
	

	public static void main(String[] args) {
		
		Scanner in = new Scanner(System.in);
		
		loadConfig("config8.dat");
		
		
		Baselines.groupByRange("[50-50]-TESTE-1594", "cosseno", "HYPER");
		System.exit(0);
		
		// Arquivos do Experimento 8: Tem que ser o arquivo de TESTE
		String files[] = {"[10-90]-TESTE-7973","[20-80]-TESTE-3986","[30-70]-TESTE-2657",
				          "[40-60]-TESTE-1993","[50-50]-TESTE-1594"};
		float range[][]= { { 0.01f,  0.04f,  0.07f, 0.1f, 0.2f, 0.3f, 0.5f, 0.8f, 0.9f}, // jaccard
		                   {  0.1f,   0.2f,   0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f}, // cosseno
		                   { 0.02f,  0.04f,  0.07f, 0.1f, 0.2f, 0.3f, 0.4f, 0.6f, 0.9f}, // generalizacao
		                   { 0.02f,  0.04f,  0.07f, 0.1f, 0.2f, 0.3f, 0.4f, 0.6f, 0.9f}, // especialização
                           {  1.0f,   2.0f,  3.0f,  4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 0.9f}  // path
      };
		
		// Arquivos do Experimento 9: Tem que ser o arquivo de TESTE
//		String files[] = {"[10-90]-TESTE-703","[20-80]-TESTE-351","[30-70]-TESTE-234",
//				          "[40-60]-TESTE-175","[50-50]-TESTE-140"};
		// Arquivos do Experimento 10: Tem que ser o arquivo de TESTE
//				  		String files[] = {"[10-90]-TESTE-830","[20-80]-TESTE-415","[30-70]-TESTE-277",
//				  				          "[40-60]-TESTE-207","[50-50]-TESTE-166"};

						// Arquivos do Experimento 11: Tem que ser o arquivo de TESTE
//				  		String files[] = {"[10-90]-TESTE-1443","[20-80]-TESTE-721","[30-70]-TESTE-481",
//				  				          "[40-60]-TESTE-360","[50-50]-TESTE-288"};
//		float range[][]= { { 0.1f,  0.2f,  0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f},      // jaccard
//						   { 0.1f,  0.2f,  0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f}, // cosseno
//						   { 0.05f,  0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.9f}, // generalizacao
//				           { 0.5f,  1.0f,  2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f}  // path
//				         };
		
		System.out.println("=========================== BASELINES =============================");
		System.out.print("Deseja realizar a avaliação automática (todos os atributos X particoes). (S)im ou (N)ao?: ");
		String resposta = in.next();
		System.out.println();
		
		if( resposta.equalsIgnoreCase("S")) {
			String att[] = Baselines.config.getProperty("attList").split("\t");
			int index = 0; // Indice para acesso ao "range"
			for( String measure: att ){
				
				for( int i=0; i < files.length; i++){		
					showEvaluation( files[i], measure, range[index]);
				}
				index++;
			}
			
			
		} else {
			

			System.out.println("Medidas: [" + Baselines.config.getProperty("attList") + "]");
			System.out.print("Digite a medida que deseja avaliar: ");
			String inputMeasure = in.next();
			System.out.println();
			// Descobrindo o indice do range[] para a medida digitada
			int index = -1;
			String att[] = Baselines.config.getProperty("attList").split("\t");
			for( int i=0; i < att.length; i++){
				if(att[i].equalsIgnoreCase( inputMeasure )) {
					index = i;
					break;
				}
			}
			if( index < 0){
				System.err.println("Medida \"" + inputMeasure + "\" inexistente na lista de atributos");
				return;
			}

			System.out.println("Indice de threshold encontrado: " + index);
			for( int i=0; i < files.length; i++){		
				showEvaluation( files[i], inputMeasure, range[ index ]);
			}
		} // fim if-resposta
		
	}// fim main
	
}
