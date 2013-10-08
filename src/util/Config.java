package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.Set;

public class Config {
	private Properties table;
	private String fileName;

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	
	public Config( String fileName ) throws FileNotFoundException, IOException{

	//	this.PropertiesTest();
		this.fileName = fileName;
		table = new Properties();
		
		loadProperties();	
	}
	
	public String getProperty( String key ){
		return table.getProperty( key );
	}
	
	public void setProperty( String key, String value) {
		table.setProperty( key, value);
		//save properties to project root folder
		try {
			table.store(new FileOutputStream( fileName ), null);
	
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	 public void listProperties()
	 {
		 Set< Object > keys = table.keySet(); // get property names
	  
	     // output name/value pairs
	     for ( Object key : keys )
	     {
	         System.out.printf(
	         "%s\t%s\n", key, table.getProperty( ( String ) key ) );
	     } // end for
	     System.out.println();
	 } // end method listProperties

	 
	  public void PropertiesTest()
	        {
	           table = new Properties(); // create Properties table
	   
	           // set properties                    
	           table.setProperty( "color", "blue" );
	           table.setProperty( "width", "200" ); 
	   
	           System.out.println( "After setting properties" );
	           listProperties(); // display property values
	   
	           // replace property value           
	           table.setProperty( "color", "red" );
	   
	           System.out.println( "After replacing properties" );
	           listProperties(); // display property values
	   
	           saveProperties(  ); // save properties
	   
	           table.clear(); // empty table
	   
	           System.out.println( "After clearing properties" );
	           listProperties(); // display property values
	   
	           loadProperties(  ); // load properties
	   
	           // get value of property color              
	           Object value = table.getProperty( "color" );
	   
	           // check if value is in table
	           if ( value != null )
	              System.out.printf( "Property color's value is %s\n", value );
	           else
	              System.out.println( "Property color is not in table" );
	        } // end PropertiesTest constructor

	  
	// save properties to a file
	  public void saveProperties(  )
	  {
		  // save contents of table
	      try {
	         FileOutputStream output = new FileOutputStream( fileName );
	         table.store( output, "Sample Properties" ); // save properties
	         output.close();
	         System.out.println( "After saving properties" );
	         listProperties();
	      }  catch ( IOException ioException ) {
	              ioException.printStackTrace();
          } //
	  }
	 
	        
	        public void loadProperties(  )
	              {
	                 // load contents of table
	                 try
	                 {
	                    FileInputStream input = new FileInputStream( fileName );
	                    table.load( input ); // load properties
	                    input.close();
//	                    System.out.println( "After loading properties" );
//	                    listProperties(); // display property values
	                 } // end try
	                 catch ( IOException ioException )
	                 {
	                    ioException.printStackTrace();
	                 } // end catch
	              } // end method loadProperties

	public static void main(String args[]){
		try {
			Config c = new Config("config.dat");
//			c.listProperties();
			//System.out.println(c.fileName + " res: " );
			System.out.println(c.getProperty("positiveLabel"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

}
