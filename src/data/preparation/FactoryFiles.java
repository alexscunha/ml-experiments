package data.preparation;

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

import java.util.List;
import java.util.Map;
import java.util.Set;

import util.Config;

public class FactoryFiles {
	private static Config config = null;

	
	public static void loadConfig( String fileName ){
		try {
			config = new Config( fileName );
		
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	/* Preenche o hash de tags mapeadas para o wordnet */
	private static HashMap<String, String> buildHashTagsToWordnet( String inputFile ){
	
		File fileIn  = new File( config.getProperty("drive") + config.getProperty("inputDir") + inputFile);
		
		StringBuilder linha = new StringBuilder();
		String[] temp = null;
		
		HashMap<String, String> map = new HashMap<String,String>(); 
		
		try {
			System.out.println("\n");
			System.out.println("============== BuildHashTagsToWordnet() ===================");		
			BufferedReader buffer = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
					
			while(	buffer.ready() ) {
				linha.delete(0, linha.length());
				linha.append(buffer.readLine()); 
				temp = linha.toString().split("\t");
				
				if ( !map.containsKey(temp[1])){
					/* chave= tag escrita no Bibsonomy / valor=palavra mapeada no wordNet */
					map.put(temp[1], temp[0]);
				}
			}
			buffer.close();
			System.out.println("\t...[done]");
			return map;
		} catch (IOException ioe){
			ioe.printStackTrace();
			return null;
		}
	}
	/*
	 *  Metodo que gera um arquivo onde cada linha se tem uma TAG e o content_id de todos os
	 *  recursos que foram marcados pela tag.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	public static void createSetTagAnnotationFile(String inputFile){
		
		File fileIn  = new File( config.getProperty("drive") + config.getProperty("inputDir") + inputFile);
		
		/* Nome do arquivo randomico  */
		String outputFile = "out-TagAnnotationSet";
		File fileOut = new File( config.getProperty("drive") + config.getProperty("outputDir") + outputFile );		
		
		BufferedWriter bw = null;
		BufferedReader br = null;
		
		
		StringBuilder linha = new StringBuilder();
		String[] tokens = null;
				
		Map<String, List> hm = new HashMap<String,List>();
		
		String key;

		

		try {	
	
			String fileMapWordnet = config.getProperty("drive") + config.getProperty("outputDir") + config.getProperty("mappingTagFile") ;
			Map<String, String> setMapTags = buildHashTagsToWordnet( fileMapWordnet );

			
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			
			
			System.out.println("\n============== CreateSetTagAnnotationFile() ===================");
			System.out.println  ("ETAPA1: Preparando o set de Tags...  ");
			int count = 0;
			// Processando
			while( br.ready()){
				linha.delete(0, linha.length());	
				linha.append(br.readLine());
				tokens = linha.toString().split("\t");
				
				/* # = cabecalho     % = comentario */
				if( tokens[0].equals("#") || tokens[0].equals("%"))
					continue;			
				
				key = setMapTags.get(tokens[1]);
				
				if ( !hm.containsKey(key)) {
					List<String> novaLista = new ArrayList<String>();
					novaLista.add(tokens[2]);
					hm.put( key, novaLista );
	
				} else {
					hm.get(key).add(tokens[2]);
				}

				if ( count % 50 == 0)
					System.out.println("Progresso: " + count);
			}
			
			br.close();		
			System.out.print("\tInstancias carregadas....: " + hm.size());
			System.out.println("......[done]");
			
			System.out.print("\nETAPA2: Preparando arquivo de saida....\n");
			bw = new BufferedWriter(new OutputStreamWriter
					  (new FileOutputStream(fileOut),"UTF8"));
			
		    Set<String> keys = hm.keySet();
		    List<Integer> lista = null;
			for( String chave: keys){	
				bw.write( chave );
				
				lista = hm.get(chave);
				
		        for ( int i = 0; i < lista.size(); i++ )
		        	bw.write( "\t" + lista.get( i ) );

				bw.newLine();
				bw.flush();
			}
			bw.close();
			System.out.println("\t[DONE]");
			
		} catch (IOException ioe){
			ioe.printStackTrace();
		} // Fim try-catch

	} // Fim createSetTagAnnotationFile()
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		createSetTagAnnotationFile("tas3000_by_eclipse");
	}

}
