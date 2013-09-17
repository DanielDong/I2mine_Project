package geo.excel;


import java.io.File;
import java.io.IOException;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import jxl.write.Number;

/**
 * This class provides utility functions for writing data to excel files.
 * 
 * @author Dong
 * @version 1.0
 */
public class ExcelWriter {
	
	private WritableWorkbook workBook = null;
	private WritableSheet sheet = null;
	
	private boolean createNewWorkbook(String workbookName){
		File file = new File(workbookName);
		try {
			workBook = Workbook.createWorkbook(file);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean createSheet(String sheetName, int position){
		sheet = workBook.createSheet(sheetName, position);
		return true;
	}
	
	public boolean createSheet(String workbookName, String sheetName, int position){
		boolean isWorkbookCreated = createNewWorkbook(workbookName);
		if(isWorkbookCreated == false){
			// workbook creation failed
			return false;
		}else{
			// workbook creation succeeds
			boolean isSheetCreated = createSheet(sheetName, position);
			if(isSheetCreated == false){
				// sheet creation failed
				return false;
			}else{
				// sheet creation succeeds
				return true;
			}
		}
	}
	
	public boolean addLabelToSheet(int col, int row, String value){
		
		boolean isAdd = false;
		Label label = new Label(col, row, value);
		if(sheet != null){
			try {
				sheet.addCell(label);
				isAdd = true;
			} catch (RowsExceededException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				isAdd = false;
			} catch (WriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				isAdd = false;
			}
		}
		return isAdd;
	}
	
	public boolean addNumberToSheet(int col, int row, double value){
		
		boolean isAdd = false;
		Number num = new Number(col, row, value);
		if(sheet != null){
			try {
				sheet.addCell(num);
				isAdd = true;
			} catch (RowsExceededException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				isAdd = false;
			} catch (WriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				isAdd = false;
			}
			
		}
		return isAdd;
	}
	
	public boolean closeWorkbook(){
		if(workBook == null){
			return false;
		}else{
			try {
				workBook.write();
				workBook.close();
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} catch (WriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
	}
	
	public boolean isWorkbookClosed(){
		return true;
	}
	
}
