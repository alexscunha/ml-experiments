package util;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.sql.Statement;
import java.sql.ResultSet;

/**
 * Uma classe produzida com o padrão de projeto <i>Singleton<i> que tem o objetivo
 * de conectar-se com o banco de dados espacial (PostgreeSQL).
 * @author Michael Angelo e Alex Sandro
 * @since 12/03/2003
 */
public class LoadDriverDB {
   /* informacoes necessarias para realizar a conexao com o banco*/
	private String driver = "com.mysql.jdbc.Driver";
	private String url = "jdbc:mysql://150.165.75.165/bibsonomy";
	private String user = "root";
	private String password = "2011ufcg";

    /* Interface JDBC que manipula uma conexao com o banco */
    private Connection connection = null;

	private static LoadDriverDB singleton = null;

   /**
    * Construtor padrao da classe que realiza a conexao com o banco
    */
	private LoadDriverDB() {
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			System.err.println(
				"An error ocurred while was trying to load the driver "
					+ e.getMessage());
		}
	}

   /**
    * Metodo que obtém uma instância única que representa a conexao com o banco
    * @return - uma instancia desta classe
    */
	public static LoadDriverDB getInstance() {
		if (singleton == null) {
			singleton = new LoadDriverDB();
		}
		return singleton;
	}

   /**
    * Método responsável pela obtencao da conexao com o banco
    * @return - A conexao com o banco
    * @throws SQLException - Excecao que pode ser gerada em caso de falha na operacao
    */
	public Connection getConnection() throws SQLException {
		if (connection == null || connection.isClosed()) {
			connection = DriverManager.getConnection(url, user, password);
		}
		return connection;
	}

   /**
    * Método que libera os recursos de banco de dados previamente alocados
    */
	public void leaveResources() {
		try {
			connection.close();
		} catch (SQLException e) {
			System.err.println(e);
		}
	}
	
	
	public static void main(String args[]){
		LoadDriverDB ld = LoadDriverDB.getInstance();
		
		Connection con;
		Statement st;
		ResultSet rs;
		try {
			con = ld.getConnection();	
			st = con.createStatement();
			String query = "Select tag from tagdata limit 0,10";
			rs = st.executeQuery(query);
			while( rs.next()){
				System.out.println(rs.getString(1));
			}

			rs.close();	
			st.close();
			con.close();		
		} catch (SQLException sqle) {
			System.err.println("Erro na obtencao da conexao: " + sqle.getMessage());
	
		} finally {
			ld.leaveResources();
		}
		 
	
	}

}