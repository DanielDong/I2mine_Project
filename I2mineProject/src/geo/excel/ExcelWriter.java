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
	
	/**
	 * Create an excel workbook.
	 * @param workbookName Workbook name for the new execl workbook.
	 * @param sheetName Sheetname for the working sheet in the <i>workbookName</i> workbook.
	 * @param position Indicates which sheet to work on.
	 * @return
	 */
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
	
	/**
	 * Add label(String) to current working sheet.
	 * @param col Specify the cell column.
	 * @param row Specify the cell row.
	 * @param value Specify the label value.
	 * @return True if adding succeeds, otherwise false.
	 */
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
	
	/**
	 * Add number value to current working sheet.
	 * @param col Specify the cell column.
	 * @param row Specify the cell row.
	 * @param value Specify the label value.
	 * @return True if adding succeeds, otherwise false.
	 */
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
	
	/**
	 * Close current working workbook.
	 * @return True if closing succeeds, otherwise false.
	 */
	public boolean closeWorkbook(){
		if(workBook == null){
			return false;
		}else{
			try {
				workBook.write();
				workBook.close();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			} catch (WriteException e) {
				e.printStackTrace();
				return false;
			}
		}
	}
	
	/**
	 * To see if current workbook is closed or not.
	 * @return True if workbook state is closed, otherwise false.
	 */
	public boolean isWorkbookClosed(){
		return true;
	}
	
}
