package geo.excel;

import geo.util.LogTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import jxl.Cell;
import jxl.CellType;
import jxl.DateCell;
import jxl.LabelCell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

/**
 * This class provides utility functions({@link #getCellValue(int, int, CellType)} etc.) for processing excel files.
 * 
 * @author Dong
 * @version 1.0
 */
public class ExcelExtract {

	public static int LEVEL = LogTool.LEVEL_OPEN;
	
	private ArrayList<Integer> extractData = null;
	private Workbook workbook = null;
	private Sheet sheet = null;
	
	public ExcelExtract(){
		extractData = new ArrayList<Integer>();
	}
	
	/**
	 * Initiate the workbook for data extraction
	 * @param workbookName Workbook for data extraction
	 * @return true for successful initiation; otherwise false 
	 */
	public boolean initWorkbook(String workbookName){
		
		StringBuilder sb = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
		boolean isInitSuccessful = false;
		try{
			
			File file = new File(workbookName);
			workbook = Workbook.getWorkbook(file);
			isInitSuccessful = true;
			
			sb.append("\nSuccessfully instantiate workbook.\n");
		}catch(BiffException be){
			sb.append("\n" + be.getMessage() + "\n");
		}catch(IOException ioe){
			sb.append("\n" + ioe.getMessage() + "\n");
		}
		LogTool.log(LEVEL, sb.toString());
		
		return isInitSuccessful;
	}
	
	/**
	 * Initiate sheet.
	 * @param pos postion for sheet
	 */
	public void initSheet(int pos){
		sheet = workbook.getSheet(pos);
	}
	
	public void printSheetColRowNum(){
		int rowNum = sheet.getRows();
		int colNum = sheet.getColumns();
		
		System.out.println("row num: " + rowNum + "\ncol num: " + colNum);
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
	 * Get the column number of current working sheet in the workbook
	 * @return the column number of current working sheet in the workbook
	 */
	public int getColNum(){
		return sheet.getColumns();
	}
	
	/**
	 * Get the row number of current working sheet in the workbook
	 * @return the row number of current working sheet in the workbook
	 */
	public int getRowNum(){
		return sheet.getRows();
	}
	
	/**
	 * Print each cell type of curent work sheet of the workbook
	 * @param rowNum the total row number of current work sheet
	 * @param colNum the total column number of current work sheet
	 */
	public void printAllCells(int rowNum, int colNum){
		
		for(int i = 0; i < rowNum; i ++){
			System.out.print(i+ ":	");
			for(int j = 0; j < colNum; j ++){
				System.out.print(sheet.getCell(j, i).getType() + "	");
			}
			System.out.println();
		}
	}
	
	/**
	 * Extract number matrix from current work sheet of the workbook
	 * @deprecated
	 */
	public void extractNumber(){
		int startRow = -1, endRow = -1, startCol = -1, endCol = -1, tmpStartCol = -1, tmpEndCol = -1, nonNum = -1;
		int rowNum = getRowNum();
		int colNum = getColNum();
		
		CellType curType = null;
		for(int i = 0; i < rowNum; i ++){
			
			// For each row, nonNum should be reset to 0 at the beginning
			nonNum = 0;
			tmpStartCol = tmpEndCol = -1;
			
			for(int j = 0; j < colNum; j ++){
				curType = sheet.getCell(j, i).getType();
				if(curType == CellType.NUMBER || curType == CellType.NUMBER_FORMULA){
					
					if(startRow == -1){
						
						startRow = endRow = i;
					}else{
						endRow = i;
					}
					
					if(tmpStartCol == -1){
						tmpStartCol = tmpEndCol = j;
					}else{
						tmpEndCol = j;
					}
					
				}
			}//end inner for
			
			// After each line, check for wider start column and end column index
			if(tmpEndCol > tmpStartCol){
				// This is possibly a valid number line
				for(int k = tmpStartCol; k <= tmpEndCol; k++){
					curType = sheet.getCell(k, i).getType();
					if(curType != CellType.NUMBER && curType != CellType.NUMBER_FORMULA){
						nonNum ++;
					}
				}
				
				if((tmpEndCol - tmpStartCol)/2 > nonNum){
					// This is the first valid number line
					if(startCol == -1){
						startCol = tmpStartCol;
						endCol   = tmpEndCol;
					}
					// This is not the first valid number line and needs to be checked for wider matrix boundary
					else{
						if(startCol > tmpStartCol){
							startCol = tmpStartCol;
						}
						
						if(endCol < tmpEndCol){
							endCol = tmpEndCol;
						}
					}
					
					if(startRow == -1){
						endRow = startRow = i; 
					}else{
						// Number matrix in excel must have headings, so cannot be the first line
						endRow = i;
					}
					
				}
				// This is not a valid number line, ignore
				else{
					//do nothing
				}
			}else{
				
				// Current line does not contain valid numbers
				// Already have a number matrix to be processed
				if(startRow != -1 && endRow != startRow && startCol != -1 && endCol != startCol){
					
					// Now print out the number matrix
					for(int row = startRow; row <= endRow; row++){
						for(int col = startCol; col <= endCol; col ++){
							System.out.print(sheet.getCell(col, row).getContents() + "	");
						}
						System.out.println();
					}
					System.out.println("\n");
					
					//Reset startRow and endRow to be ready for new number matrix
					startRow = endRow = startCol = endCol = tmpStartCol = tmpEndCol = -1;
				}
			}
		}// end outter for
	}

	
	/**
	 * Extract certain attribute in a certain row
	 */
	@Deprecated
	public void extractCertainAttribute(){
		
		String instructMsg = "Choose the attribute: \n" +
				"		1. Work days per week.\n" +
				"		2. Working days per month.\n" +
				"		3. Working months per year.\n" +
				"Choose the column number:\n";
		ArrayList<String> headingList = getColHeadings(); 
		int i = 0;
		if(headingList != null)
			for(i = 0; i < headingList.size(); i ++){
				instructMsg += "		" + (i + 1) + ". " + headingList.get(i) + "\n";
			}
		
		System.out.println(instructMsg + " Enter " + (i + 1) + " to show all colume values.\n Enter -1 attribute value to exit the whole application");
		
		Scanner s = new Scanner(System.in);
		while(true){
			System.out.print("Attribute number: ");
			String attrNum = s.next();
			System.out.print("Column number: ");
			String colNum = s.next();
			int attrId = -1, colId = -1;
			ArrayList<String> ret = null;
			try{
				attrId = Integer.valueOf(attrNum);
				// Exit the whole application
				if(attrId == -1)
					break;
				
				switch(attrId){
				case 1:
					ret = searchSheet("Work days per week");
					break;
				case 2:
					ret = searchSheet("Working days per month");
					break;
				case 3:
					ret = searchSheet("Working months per year");
					break;
				default:
					System.out.println("The number you choose is not in the valid number list.");
					System.exit(-1);
				}
			}catch(NumberFormatException nfe){
				System.err.println("Please choose a valid attribute number.");
				System.exit(-1);
			}
			
			try{
				colId = Integer.valueOf(colNum);
				if(colId > headingList.size() + 1){
					System.err.println("Please choose a valid colmun number.");
					System.exit(-1);
				}
					
			}catch(NumberFormatException nfe){
				System.err.println("Please choose a valid colmun number.");
				System.exit(-1);
			}
			
			if(headingList != null && colId <= headingList.size())
				System.out.println(ret.get(0) + " " + ret.get(colId) + " " + ret.get(ret.size() - 1));
			else
				System.out.println(ret);
		}
	}
	
	/**
	 * 
	 */
	@Deprecated
	public ArrayList<String> checkForUnit(String checkVal){
		return getNumUnit(checkVal);
	}
	
	/**
	 * 
	 */
	@Deprecated
	public ArrayList<String> searchSheet(String searchStr){
		
		ArrayList<String> ret = new ArrayList<String>();
		int rowNum = getRowNum();
		int colNum = getColNum();
		int foundRow = -1, foundCol = -1, row = 0, col = 0;
		
		
		for(row = 0; row < rowNum; row ++){
			for(col = 0; col < colNum; col ++){
				if(sheet.getCell(col, row).getType() == CellType.LABEL){
					
					String value = sheet.getCell(col, row).getContents();
					if(value.contains(searchStr)){
						foundRow = row;
						foundCol = col;
						
						ret.add(value);
						continue;
					}
					
					if(foundRow != -1){
						ArrayList<String> numUnitList = checkForUnit(value); 
						if( numUnitList != null){
							for(int i = 0; i < numUnitList.size(); i ++)
							ret.add(numUnitList.get(i));
						}
					}
					
				}else if(foundRow != -1){
					
					String content = sheet.getCell(col, row).getContents();
					CellType type =	sheet.getCell(col, row).getType(); 
					if(type == CellType.NUMBER ||type == CellType.NUMBER_FORMULA ){
						ret.add(content);
					}
					else if(type == CellType.EMPTY){
						break;
					}
					else if(type == CellType.LABEL){
						ret.add(content);
						break;
					}
				}
				
			}
			if(foundRow != -1 && foundCol != -1)
				break;
		}
		
		return ret;
	}
	
	/**
	 * Read in and return a unit list from a text file named "Unit.txt"
	 * @return
	 */
	@Deprecated
	public static ArrayList<String> getUnitList(){
		ArrayList<String> unitList = new ArrayList<String>();
		
		BufferedReader br = null;
		try {
			
			br = new BufferedReader(new FileReader("Unit.txt"));
			String newLine = null;
			while((newLine = br.readLine()) != null){
				unitList.add(newLine);
			} 
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(br != null)
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return unitList;
	}
	
	@Deprecated
	public static float compareCellContent(String str1, String str2){
		
		if(str1 == null || str2 == null)
			return 0;
		
		str1 = str1.trim();
		str2 = str2.trim();
		
		float len = (str1.length() > str2.length() ? str2.length() : str1.length())
				;
		int i = 0;
		for(; i < len; i ++){
			if(str1.charAt(i) != str2.charAt(i))
				break;
		}
		
		return i / len;
	}
	
	/**
	 * If a cell contains only numerical value and unit, then 
	 * return this number and unit in an ArrayList or null otherwise
	 * @param cellVal cell contents
	 * @return an ArrayList containing only two elements(first is the number and second is the unit) or null
	 */
	@Deprecated
	public static ArrayList<String> getNumUnit(String cellVal){
		ArrayList<String> ret = null;
		ArrayList<String> unitList = getUnitList();
		int i = 0;
		for(; i < unitList.size(); i ++){
			if(cellVal.contains(unitList.get(i))){
				break;
			}
		}
		
		if(i != unitList.size()){
			ret = new ArrayList<String>();
			String numVal = cellVal.substring(0, cellVal.indexOf(unitList.get(i)));
			ret.add(numVal);
			ret.add(cellVal.substring(cellVal.indexOf(unitList.get(i))));
		}
		
		return ret;
	}
	
	@Deprecated
	public ArrayList<String> getColHeadings(){
		int rowNum = getRowNum();
		int colNum = getColNum();
		
		ArrayList<String> colHeadingList = null;
		boolean isHeadingFound = false;
		int row = 0, col = 0;
		
		for(row = 0; row < rowNum; row ++){
			
			if(isHeadingFound == true)
				break;
			
			for(col = 1; col < 2; col ++){
				Cell cell = sheet.getCell(col, row);
				// Might be heading row
				if(cell.getType() == CellType.LABEL && sheet.getCell(0, row).getType() == CellType.EMPTY){
					int colTmp = 2, resemblance100 = 0, resemblance50 = 0;
					while(sheet.getCell(colTmp, row).getType() != CellType.EMPTY && colTmp < colNum){
						String tmpContent = sheet.getCell(colTmp, row).getContents();

						float resemblance = compareCellContent(cell.getContents(), tmpContent);
						if(resemblance == 1){
							resemblance100 ++;
						}else if(resemblance >= 0.5f){
							resemblance50 ++;
						}
						colTmp ++;
					}
					
					/**
					 * This is one kind of heading format
					 * heading heading heading
					 * value1  value2  value3
					 */
					if(resemblance100 > 0){
						colHeadingList = new ArrayList<String>();
						for(int i = col; i <= col + resemblance100; i++){
							
							ArrayList<String> belowCell = getNumUnit(sheet.getCell(i, row + 1).getContents());
							if(belowCell != null)
								colHeadingList.add(cell.getContents() + " " + belowCell.get(0) + " " + belowCell.get(1));
						}
						isHeadingFound = true;
					}
					/**
					 * This is another kind of heading format
					 * heading(value1) heading(value2) heading(value3)
					 */
					else if(resemblance50 > 0){
						colHeadingList = new ArrayList<String>();
						for(int i = col; i <= col + resemblance50; i++){
							colHeadingList.add(sheet.getCell(i, row).getContents());
						}
						isHeadingFound = true;
					}
				}// end if
			}// end inner for
		}// end outter for
		return colHeadingList;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ExcelExtract ee = new ExcelExtract();
		//ee.initWorkbook("excelextract.xls");
		ee.initWorkbook("Planning.xls");
		ee.initSheet(0);
		//System.out.println(ee.getColHeadings());
		//ee.printAllCells(ee.getRowNum(), ee.getColNum());
		//System.out.println(ee.getCellValue(2, 5, CellType.LABEL));
		ee.extractCertainAttribute();
		//Code block to extract certain attribute from excel
		
		
//      // The following code block is to extract number matrix from excel file	
//		ee.printSheetColRowNum();
//		//ee.printAllCells(ee.getRowNum(), ee.getColNum());
//		System.out.println("=====================================");
//		ee.extractNumber();
	}

}
