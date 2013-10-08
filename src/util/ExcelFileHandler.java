package util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.IndexedColors;

/**
 * Baseado no artigo: Read / Write Excel file in Java using Apache POI
 * url: http://viralpatel.net/blogs/java-read-write-excel-file-apache-poi/
 * @author Alex
 * @version 1.1
 */
public class ExcelFileHandler {
	// Arquivo xls
	HSSFWorkbook workbook = null;
	// Planilha do arquivo xls
	HSSFSheet sheet;
	// Row in current sheet
	HSSFRow row = null;	
	//Create a new cell in current row
	HSSFCell cell = null;
	// Font of cells
	HSSFFont font = null;
	// font style
	HSSFCellStyle style = null;
	/**
	 * Construtor padrão, que começa com uma planilha em "branco", que deve ser salva 
	 * futuramente.
	 */
	public ExcelFileHandler(){
		workbook = new HSSFWorkbook();
		sheet = workbook.createSheet("Plan1");
		font = workbook.createFont(); 
		style = workbook.createCellStyle();
		// Inicia com a fonte "Consolas" como padrão
		font.setFontName( "Consolas" );
		font.setFontHeightInPoints((short)10);
		style.setFont(font);
	}
	
	/**
	 * Construtor que cria um objeto Excel e carrega a planilha de acordo com o arquivo
	 * passado como argumento. 
	 * @param fileName
	 */
	public ExcelFileHandler( String fileName ){
		try {
		    FileInputStream file = new FileInputStream(new File( fileName ));
		 
		    workbook = new HSSFWorkbook(file);
		    // Gets the first sheet
		    sheet = workbook.getSheetAt(0);
			font = workbook.createFont(); 
			style = workbook.createCellStyle();
		    file.close();		     
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	public<T> void insertData( int nRow, int nCol, T content){
		if( nRow < 0 || nCol < 0)
			return;
		//Create a new row in current sheet
//		System.out.println( sheet.getLastRowNum());
//		System.out.println(sheet.getFirstRowNum());
//		if ( nRow > sheet.getLastRowNum())
		
		row = sheet.getRow( nRow );
		if ( row == null) // row nao foi criada ainda
			row = sheet.createRow( nRow );	
		//Create a new cell in current row
//		System.	out.println( row.getLastCellNum());
		cell = row.getCell( nCol );
		if ( cell == null)
			cell = row.createCell( nCol );
		
		style.setFont(font);
		cell.setCellStyle(style);
		
		//Set value to new value
		
        if(content instanceof Date)
            cell.setCellValue((Date)content);
        else if(content instanceof Boolean)
            cell.setCellValue((Boolean)content);
        else if(content instanceof String)
            cell.setCellValue((String)content);
        else if(content instanceof Double)
            cell.setCellValue((Double)content);
		
	}
	
	
	public boolean updateData(int nRow, int nCol, String content ){
		
		row = sheet.getRow( nRow );
		if ( row == null) // row nao foi criada ainda
			return false;
		
		cell = row.getCell( nCol );
		if ( cell == null) // coluna nao existe
			return false;

	    //Update the value of cell
	    // cell = sheet.getRow( nRow ).getCell(2);
	    // cell.setCellValue(cell.getNumericCellValue() * 2);
	    cell.setCellValue( content );
		return true;
	}
	
	/**
	 * Insere uma formula. Se a linha/coluna nao existir, cria a linha, coluna e adiciona a formula.
	 * Caso contrário, substitui o conteudo da célula.
	 * @param nRow A linha
	 * @param nCol A coluna
	 * @param content A formula
	 * @return
	 */
	public boolean addFormula(int nRow, int nCol, String formula ){
		
		row = sheet.getRow( nRow );
		if ( row == null) // linha nao existe
			row = sheet.createRow( nRow ); 
		
		cell = row.getCell( nCol );
		if ( cell == null)
			cell = row.createCell( nCol );

	    //Update the value of cell
	    // cell = sheet.getRow( nRow ).getCell(2);
	    // cell.setCellValue(cell.getNumericCellValue() * 2);
	    cell.setCellFormula( formula );
		return true;
	}
	
	public String getCurrentSheetName(){
		return sheet.getSheetName();
	}
	

	
	public boolean saveFile( String fileName ){
		try {
		    FileOutputStream out =
		            new FileOutputStream(new File( fileName ));
		    workbook.write(out);
		    out.close();
		    System.out.println("Arquivo .xls (Excel) salvo com sucesso.");
		     
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		    return false;
		} catch (IOException e) {
		    e.printStackTrace();
		    return false;
		}
		return true;
	}
	
	
	public void setFont( String fontName ){
		
		font.setFontName( fontName );
		style.setFont(font);
	}
	
	public void setFont( int nRow, int nCol, String fontName ){
		row = sheet.getRow( nRow );
		if ( row == null) // linha nao existe
			return; 
		
		cell = row.getCell( nCol );
		if ( cell == null)
			return;
		
		font.setFontName( fontName );
		style.setFont(font);
		cell.setCellStyle(style);
	}
	
	public void setBold( int nRow, int nCol ){
		row = sheet.getRow( nRow );
		if ( row == null) // linha nao existe
			return; 
		
		cell = row.getCell( nCol );
		if ( cell == null)
			return;
		
		HSSFFont newFont = workbook.createFont();
		newFont.setFontName( font.getFontName() );
		
		HSSFCellStyle newStyle = workbook.createCellStyle();

//		HSSFCellStyle newStyle = cell.getCellStyle();
		newFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		newStyle.setFont( newFont);
		cell.setCellStyle(newStyle);
//		sheet.setDefaultColumnStyle( nCol, newStyle);
		
//		font.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
	}
	
	public void setCenterAlign( int nRow, int nCol){
		HSSFCellStyle my_style = workbook.createCellStyle();
		my_style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        my_style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        
		row = sheet.getRow( nRow );
		if ( row == null) // linha nao existe
			return; 
		
		cell = row.getCell( nCol );
		if ( cell == null)
			return;

		cell.setCellStyle(my_style);
	}
	
	
	public void setColor( int nRow, int nCol ){
		row = sheet.getRow( nRow );
		if ( row == null) // linha nao existe
			return; 
		
		cell = row.getCell( nCol );
		if ( cell == null)
			return;
		
		HSSFFont newFont = workbook.createFont();
//		HSSFFont newFont = font;
		newFont.setFontName( font.getFontName() );
		
		HSSFCellStyle newStyle = workbook.createCellStyle();

//		HSSFCellStyle newStyle = cell.getCellStyle();
//		newFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		newFont.setColor(IndexedColors.BLUE.getIndex());
//	    newFont.setBold(false);
//	    defaultFont.setItalic(false);
		newStyle.setFont( newFont);
		cell.setCellStyle(newStyle);
//		sheet.setDefaultColumnStyle( nCol, newStyle);
		
		//newFont.setColor(IndexedColors.BLACK.getIndex());
	}
	
	public void setItalic( int nRow, int nCol ){
		row = sheet.getRow( nRow );
		if ( row == null) // linha nao existe
			return; 
		
		cell = row.getCell( nCol );
		if ( cell == null)
			return;
		
		font.setItalic(true);
		style.setFont(font);
		cell.setCellStyle(style);
	}
	
	/**
	 * NAO FUNCIONA AINDA!
	 * @param nRow
	 * @param nCol
	 */
	public void setDecimalPlaces(int nRow, int nCol){
		HSSFDataFormat dataformat = workbook.createDataFormat(); 
//		List<String> list = dataformat.getBuiltinFormats();
		
//		for(String s: list)
//			System.out.println(s);
//		
//		System.out.println( dataformat.getBuiltinFormat("0.00") );

		
		HSSFCellStyle my_style = workbook.createCellStyle();
        my_style.setDataFormat( dataformat.getFormat("0.0000"));
		row = sheet.getRow( nRow );
		if ( row == null) // linha nao existe
			return; 
		
		cell = row.getCell( nCol );
		if ( cell == null)
			return;

		cell.setCellStyle(my_style);
	}
	
	public static void main(String args[]){
		ExcelFileHandler planilha  = new ExcelFileHandler();
	
		for(int row=0; row < 10; row++){
			
			for( int col=0; col < 10; col++){
				planilha.insertData(row, col, row*col);
			}
		}
		
		planilha.setFont(2,2,"Consolas");
		planilha.updateData(2, 2, "Deu certo");
		planilha.updateData(3, 2, "Outra linha");
		planilha.setBold(2,2);
		planilha.insertData(4, 2, "qualquer coisa");
		planilha.setColor(4,2);
		planilha.addFormula(9,9, "A1+B2+C2");
		planilha.saveFile("g:\\mySampleXls.xls");
		
		Double d =(double) 0;
		planilha.insertData(6, 2, d);
//		planilha.setDecimalPlaces(6,2);
	}
}
