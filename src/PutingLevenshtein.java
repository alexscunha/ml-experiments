import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.io.OutputStreamWriter;


import java.sql.Connection;
import java.sql.SQLException;
//import java.sql.Statement;
//import java.sql.PreparedStatement;
import  java.sql.CallableStatement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import util.LoadDriverDB;

/*
 * OBJETIVO:
 * Esta classe adiciona o valor computado Levenhtein no Banco de Dados, para fins de
 * atualização.
 */
public class PutingLevenshtein {
	
	private LoadDriverDB ld;
	public static final int LOCAL = 0;
	public static final int DEFAULT = 1;
	private String readDirectory;
	private String writeDirectory;
	
	public PutingLevenshtein(){
		ld = LoadDriverDB.getInstance();
		defaultDirectories();
	}
	
	private String hoje() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm");  
		return sdf.format(new Date()); 
	}
	
	private void defaultDirectories(){
		readDirectory= "d:/temp/bibsonomy";
		writeDirectory="d:/temp";	
	}

	public void addLevenshteinSimilarity( int number, int directory ){
		/* Declaracoes para conexao com o BD */
		Connection conn1;
		CallableStatement cstmt = null;
			
		/* Declaracao de variaveis de apoio */
		String diretorio;
		if ( directory == LOCAL)
			diretorio = writeDirectory = System.getProperty("user.dir");
		else {
			defaultDirectories();
			diretorio = readDirectory;
		}

		BufferedReader br = null;
		File fileIn = new File(diretorio + "/AllTagSimilarityLevenshtein-001");
		BufferedWriter bw = null;
		File fileOut = new File(writeDirectory + "/Log.txt");		
		
		StringBuilder linha = new StringBuilder();
		StringBuilder lastTag = new StringBuilder();
		String[] temp = null;
		
		int progress=0;
		
		if (number < 1)
			return;
		
		try {
			
			conn1 = ld.getConnection();	
			
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			
			bw.write("Update: addLevenshteinSimilarity(): Skip " + number + " in " + hoje() );
			bw.newLine();
			bw.flush();
			
			while (br.ready()){
				progress++;
				linha.append(br.readLine()); 
				temp = linha.toString().split("\t");
				
				if (progress <= number ) {
					linha.delete(0, linha.length());
					continue;
				}
				
				if ( ! temp[0].equals( lastTag.toString())){
					lastTag.delete(0, lastTag.length());
					lastTag.append( temp[0]);
					bw.write( lastTag.toString() );
					bw.newLine();
					bw.flush();
				}
				
				cstmt = conn1.prepareCall("{CALL spUpdateSimilarity(?,?,?)}");
				cstmt.setString(1, temp[0]);
				cstmt.setString(2, temp[1]);
				cstmt.setFloat(3, Float.parseFloat(temp[2]));
				
				cstmt.executeUpdate();
				
				System.out.println("Progresso: " + progress );				
				linha.delete(0, linha.length());
			}			
			
			br.close();
			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException sql){
			sql.printStackTrace();
		} finally {
			System.out.println("Horario da conclusão ou Interrupção: " + hoje()); 
		}
	
	}
	

	public void addTagSimilarity( int number, int directory  ){
		/* Declaracoes para conexao com o BD */
		Connection conn1;
		CallableStatement cstmt = null;
			
		/* Declaracao de variaveis de apoio */
		String diretorio;
		if ( directory == LOCAL)
			diretorio = writeDirectory = System.getProperty("user.dir");
		else {
			defaultDirectories();
			diretorio = readDirectory;
		}


		BufferedReader br = null;
		File fileIn = new File(diretorio + "/AllTagSimilarityTag-001");
		BufferedWriter bw = null;
		File fileOut = new File(writeDirectory + "/Log.txt");

		
		StringBuilder linha = new StringBuilder();
		StringBuilder lastTag = new StringBuilder();
		String[] temp = null;
		
		int progress=0;
		
		if (number < 0)
			return;
		
		try {
			
			conn1 = ld.getConnection();	
			
			
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			
			bw.write("Update: addLevenshteinSimilarity(): Skip " + number + " in " + hoje() );
			bw.newLine();
			bw.flush();

			while (br.ready()){
				progress++;
				linha.append(br.readLine()); 
				temp = linha.toString().split("\t");
				
				if (progress <= number ) {
					linha.delete(0, linha.length());
					continue;
				}
				
				if ( ! temp[0].equals( lastTag.toString())){
					lastTag.delete(0, lastTag.length());
					lastTag.append( temp[0]);
					bw.write( lastTag.toString() );
					bw.newLine();
					bw.flush();
				}

				
				cstmt = conn1.prepareCall("{CALL spUpdateSimilarityTag(?,?,?)}");
				cstmt.setString(1, temp[0]);
				cstmt.setString(2, temp[1]);
				cstmt.setFloat(3, Float.parseFloat(temp[2]));
				
				cstmt.executeUpdate();
				
				System.out.println("Progresso: " + progress );				
				linha.delete(0, linha.length());
			}			
			System.out.println("Operacao concluida....");
			br.close();
			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException sql){
			sql.printStackTrace();
		} finally {
			System.out.println("Horario da conclusão ou Interrupção: " + hoje()); 
		}

		
	}
	
	
	public void addResourceSimilarity( int number, int directory ){
		/* Declaracoes para conexao com o BD */
		Connection conn1;
		CallableStatement cstmt = null;
			
		/* Declaracao de variaveis de apoio */
		String diretorio;
		if ( directory == LOCAL)
			diretorio = writeDirectory = System.getProperty("user.dir");
		else {
			defaultDirectories();
			diretorio = readDirectory;
		}

		BufferedReader br = null;
		File fileIn = new File(diretorio + "/AllTagSimilarityResource-001");
		BufferedWriter bw = null;
		File fileOut = new File(writeDirectory + "/Log.txt");

		
		StringBuilder linha = new StringBuilder();
		StringBuilder lastTag = new StringBuilder();
		String[] temp = null;
		
		int progress=0;
		
		if (number < 0)
			return;
		
		try {
			
			conn1 = ld.getConnection();	
			
			
			br = new BufferedReader(new InputStreamReader(new FileInputStream( fileIn ), "UTF8"));
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut),"UTF8"));
			
			bw.write("Update: addLevenshteinSimilarity(): Skip " + number + " in " + hoje() );
			bw.newLine();
			bw.flush();

			System.out.println("Iniciando a atualização do Resource Context Similarity...");
			
			while (br.ready()){
				progress++;
				linha.append(br.readLine()); 
				temp = linha.toString().split("\t");
				
				if (progress <= number ) {
					linha.delete(0, linha.length());
					continue;
				}
				
				if ( ! temp[0].equals( lastTag.toString())){
					lastTag.delete(0, lastTag.length());
					lastTag.append( temp[0]);
					bw.write( lastTag.toString() );
					bw.newLine();
					bw.flush();
				}

				cstmt = conn1.prepareCall("{CALL spUpdateSimilarityResource(?,?,?)}");
				cstmt.setString(1, temp[0]);
				cstmt.setString(2, temp[1]);
				cstmt.setFloat(3, Float.parseFloat(temp[2]));
				
				cstmt.executeUpdate();
				
				System.out.println("Progresso: " + progress );				
				linha.delete(0, linha.length());
			}			
			System.out.println("Operacao concluida....");
			br.close();
			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException sql){
			sql.printStackTrace();
		} finally {
			System.out.println("Horario da conclusão ou Interrupção: " + hoje()); 
		}
		
	}

	
	public static void main(String[] args) {
	
		PutingLevenshtein pl = new PutingLevenshtein();
		
		Scanner in = new Scanner(System.in);
		int option, number, dir;
		String resposta;

		while ( true ) {
			System.out.println("Data Training: ");
//			System.out.println(System.getProperty("user.dir"));
			System.out.println("========================");
			System.out.println("1 - Update Levenshtein");
			System.out.println("2 - Update Tag Context");
			System.out.println("3 - Update Resource Context");
			System.out.println("4 - Update Coocurrence");
			System.out.println("5 - Saída\n");
			System.out.print("[Opcao]: ");
			option = in.nextInt();
			
			switch( option) {
			case 1:
				System.out.print("Skip Records: ");
				number = in.nextInt();
				System.out.print("Diretorio Local (S/N)?");
				resposta = in.next();
				
				if ( resposta.equalsIgnoreCase("S"))
					dir = PutingLevenshtein.LOCAL;
				else
					dir = PutingLevenshtein.DEFAULT;

				pl.addLevenshteinSimilarity(number, dir);
				break;
			case 2:
				System.out.print("Skip Records: ");
				number = in.nextInt();
				System.out.print("Diretorio Local (S/N)?");
				resposta = in.next();
				
				if ( resposta.equalsIgnoreCase("S"))
					dir = PutingLevenshtein.LOCAL;
				else
					dir = PutingLevenshtein.DEFAULT;
				
				pl.addTagSimilarity(number, dir );
				
				break;
			case 3:
				System.out.print("Skip Records: ");
				number = in.nextInt();
				System.out.print("Diretorio Local (S/N)?");
				resposta = in.next();
				
				if ( resposta.equalsIgnoreCase("S"))
					dir = PutingLevenshtein.LOCAL;
				else
					dir = PutingLevenshtein.DEFAULT;
				
				pl.addResourceSimilarity(number, dir);
				break;
			case 4:
				System.out.println("Falta implementar...");
				break;
			case 5:
				System.exit(0);
			}
			
			
		}

	}

}
