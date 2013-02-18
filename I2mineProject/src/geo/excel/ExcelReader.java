package geo.excel;

import java.io.File;
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
	
	private Workbook workbook = null;
	private Sheet sheet = null;
	
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
	
	public void initWorkSheet(int position){
		sheet = workbook.getSheet(position);
	}
	
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
}
