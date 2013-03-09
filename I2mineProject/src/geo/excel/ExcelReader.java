package geo.excel;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import jxl.Cell;
import jxl.CellType;
import jxl.DateCell;
import jxl.LabelCell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class ExcelReader {
	
	public Workbook workbook = null;
	public Sheet sheet = null;
	
	/**
	 * Load the workbook into memory.
	 * @param workbookName File name for the workbook to be loaded
	 * @return true if loading successfully, otherwise false
	 */
	public boolean getWorkbook(String workbookName){
		
		boolean isWorkbookExist = false;
		File file = new File(workbookName);
		try {
			workbook = Workbook.getWorkbook(file);
			isWorkbookExist = true;
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			isWorkbookExist = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			isWorkbookExist = false;
		}
		
		return isWorkbookExist;
	}
	
	/**
	 * Specify which sheet to be processed
	 * @param position The position of sheet in the workbook
	 */
	public void initWorkSheet(int position){
		sheet = workbook.getSheet(position);
	}
	
	/**
	 * Read and return data from excel sheet
	 * @param col Column where data resides
	 * @param row Row where data resides
	 * @param type	Data type
	 * @return Object The data in cell (row, col)
	 */
	public Object getCellValue(int col, int row, CellType type){
		
		if(sheet != null){
			Cell cell = sheet.getCell(col, row);
			
			if(CellType.BOOLEAN == type){
				
			}else if(CellType.DATE == type){
				DateCell dc = (DateCell)cell;
				return dc.getDate();
			}else if(CellType.EMPTY == type){
				
			}else if(CellType.ERROR == type){
				
			}else if(CellType.LABEL == type){
				LabelCell lc = (LabelCell)cell;
				return lc.getString();
			}else if(CellType.NUMBER == type){
				NumberCell nc = (NumberCell)cell;
				return nc.getValue(); 
			}
			
			return cell.getContents();
		}
		
		return null;
	}
	
	/**
	 * This function converts excel file to txt file
	 * @param inFileName File name of the excel file
	 * @param outFileName File name of the text file
	 * @return true if convert successfully, otherwise false
	 * @throws IOException 
	 */
	public boolean convertExcelToText(String inFileName, String outFileName) throws IOException{
		boolean isLoadSuccessul = getWorkbook(inFileName);
		if(isLoadSuccessul == true){
			initWorkSheet(0);
			
			File outFile = new File(outFileName);
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(outFileName));
			for(int i = 0; i < sheet.getRows(); i++){
				for(int j = 0; j < sheet.getColumns(); j++){
					dos.write((getCellValue(j, i, CellType.NUMBER).toString()+"\t").getBytes());
				}
				dos.write("\n".getBytes());
			}
		}
		
		return true;
	}
	
	// Test code for ExcelReader
	public static void main(String[] args) throws IOException{
		ExcelReader er = new ExcelReader();
//		boolean isLoadWorkbookSuccessful = er.getWorkbook("workphase-distance.xls");
//		if(isLoadWorkbookSuccessful == true){
//			er.initWorkSheet(0);
//			for(int i = 0; i < er.sheet.getRows(); i++){
//				for(int j = 0; j < er.sheet.getColumns(); j++){
//					System.out.print(er.getCellValue(j, i, CellType.NUMBER)+" ");
//				}
//				System.out.println();
//			}
//		}
		er.convertExcelToText("workphase-distance.xls", "workphase-distance.txt");
	}
	
}
