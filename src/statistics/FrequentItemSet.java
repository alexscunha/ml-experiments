package statistics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;


public class FrequentItemSet {

	public static void  analyseSupport( String inputFile, String measure  ){
		/* Declaracoes para arquivo de entrada */
		BufferedReader br = null;		
		BufferedWriter bw = null;
		File fileIn  = new File( inputFile );
		File fileOut = new File( "out-analyseSupport");
		
		int progress=0;
		StringBuilder linha = new StringBuilder();
		String[] tokens = null;
		
		Hashtable<Integer,Integer> ht = new Hashtable<Integer,Integer>();
		int iMeasure = -1;

		try {
			System.out.println("\n============== AnalyseSupport() ===============");
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));				
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			
			if ( br.ready()) {
				linha.append(br.readLine());
				tokens = linha.toString().split("\t");
				if( !tokens[0].equals("#") ) {
					System.err.println("\nArquivo nao contem linha de CABECALHO!");
					br.close();
					bw.close();
					return;
				} else {
					for( int i=3; i< tokens.length-1; i++ )
						if( tokens[i].equalsIgnoreCase(measure))
							iMeasure = i;
					
					if( iMeasure < 0 ) {
						System.err.println("\nMedida \"" + measure + "\" nao encontrada!");
						br.close();
						bw.close();
						return;
					}
				}
			} else {
				System.err.println("\nErro na preparacao da varredura do arquivo!");
				br.close();
				bw.close();
			}
			int chave;
			while (br.ready()){
				linha.delete(0, linha.length());
				linha.append(br.readLine()); 
				tokens = linha.toString().split("\t");
				
				chave = (int) Float.parseFloat(tokens[iMeasure]);
				
				if ( tokens[0].equalsIgnoreCase("%") || tokens[0].equalsIgnoreCase("#"))
					continue;
				else if( ht.containsKey( chave )) {
					int valor = ht.get( chave );
					ht.put(chave, ++valor);
				} else {
					ht.put(chave, 0);
				}

				++progress;
			} 
			System.out.println("Progresso = " + progress );
			System.out.println("::::: RESUMO :::::");
			bw.write("::::: RESUMO :::::");
			bw.newLine();
			bw.flush();
			DecimalFormat decimal = new DecimalFormat( "00.0000" );	
			
			Integer[] keys = (Integer[]) ht.keySet().toArray(new Integer[0]);  
			Arrays.sort(keys);
			for( int key : keys ){
				System.out.println(key + "\t" + ht.get(key) + "\t= " + decimal.format((ht.get(key)*1.0/progress)*100)+"%");
				bw.write(key + ":\t" + ht.get(key) + "\t= " + decimal.format((ht.get(key)*1.0/progress)*100)+"%");
				bw.newLine();
				bw.flush();
			}
			
			
			br.close();
			bw.close();

		} catch (IOException ioe ){
				ioe.printStackTrace();
		}
	}
	
	/* Computa o suporte das tags no arquivo de transacoes */
	public static Map<String,Integer>  computeSupportTags( String inputFile ){
		/* Declaracoes para arquivo de entrada */
		BufferedReader br = null;		
		File fileIn  = new File( inputFile );
		
		Map<String,Integer> suporteTag = new HashMap<String,Integer>();
		
		int progress=0, dot=0;
		StringBuilder linha = new StringBuilder();
		String[] tokens = null;

		try {
			System.out.println("\n============== computeSupportTags() ===============");
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));				

			while (br.ready()){
				linha.delete(0, linha.length());
				linha.append(br.readLine()); 
				tokens = linha.toString().split("\t");
				
				if( suporteTag.containsKey( tokens[1] )) {
					int valor = suporteTag.get( tokens[1] );
					suporteTag.put(tokens[1], ++valor);
				} else {
					suporteTag.put(tokens[1], 0);
				}
				
				if( suporteTag.containsKey( tokens[2] )) {
					int valor = suporteTag.get( tokens[2] );
					suporteTag.put(tokens[2], ++valor);
				} else {
					suporteTag.put(tokens[2], 0);
				}
				
				if( ++progress % 500 == 0 ){
					dot++;
					if( dot % 50 == 0)
						System.out.print("\n");
					System.out.print(". ");
				}
				
			} 
		
			br.close();
			System.out.print("[done]\n");
			return suporteTag;

		} catch (IOException ioe ){
				ioe.printStackTrace();
		}
		return null;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//analyseSupport("G:\\temp\\hiponimos\\Experimentos 5\\[5-0]-SuporteConfiancaJaccardCossenoGrauInclusao-SEQ[2]-FULL_FILE","suporte");
		computeSupportTags("G:\\temp\\hiponimos\\Experimentos 5\\[5-0]-SuporteConfiancaJaccardCossenoGrauInclusao-SEQ[2]-FULL_FILE");

	}

}
