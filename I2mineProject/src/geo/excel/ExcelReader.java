package geo.excel;

import geo.core.MachineOpInfo;
import geo.core.WorkfaceDistance;
import geo.core.WorkfaceState;
import geo.core.WorkfaceWorkload;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

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
			System.out.println("Get workbook succeeds!"+isWorkbookExist);
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			System.out.println("BiffException:"+e.getMessage());
			isWorkbookExist = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("IOException:"+e.getMessage());
			isWorkbookExist = false;
		}
		System.out.println("Load status: "+isWorkbookExist);
		
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
			for(int row = 0; row < sheet.getRows(); row++){
				for(int col = 0; col < sheet.getColumns(); col++){
					dos.write((getCellValue(col, row, CellType.NUMBER).toString()+"\t").getBytes());
				}
				dos.write("\n".getBytes());
			}
		}
		else
			return false;
		
		return true;
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public MachineOpInfo readMachineOpInfo(String fileName){
		
		MachineOpInfo moi = null; 
		boolean isLoadSuccessul = getWorkbook(fileName);
		if(isLoadSuccessul == true){
			
			initWorkSheet(0);
			// Row number indicates number of machines
			moi = new MachineOpInfo(sheet.getRows());
			
			for(int row = 0; row < sheet.getRows(); row++){
				
				ArrayList<Double> singMachineOpInfo = new ArrayList<Double>();
				// Read operation information of a machine
				for(int col = 0; col < 2; col++){
					singMachineOpInfo.add((Double) getCellValue(col, row, CellType.NUMBER));
				}
				// Add operation information of a machine into the MachineOpInfo
				moi.addMachineOpInfo(singMachineOpInfo);
			}// end for
		}// end if
		
		return moi;
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public WorkfaceState readWorkfaceState(String fileName){
		
		WorkfaceState wfs = null;
		boolean isLoadSuccessul = getWorkbook(fileName);
		
		if(isLoadSuccessul == true){
			initWorkSheet(0);
			// Currently, only one row and multiple columns.
			// Column number indicates the number of workfaces
			wfs = new WorkfaceState(sheet.getColumns());
			for(int col = 0; col < sheet.getColumns(); col ++){
				wfs.addWorkfaceState((Integer) getCellValue(col, 1, CellType.NUMBER));
			}
		}
		
		return wfs;
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public WorkfaceWorkload readWorkfaceWorkload(String fileName){
		
		WorkfaceWorkload ww = null;
		boolean isLoadSuccessul = getWorkbook(fileName);
		
		if(isLoadSuccessul == true){
			initWorkSheet(0);
			// The number of rows indicates the number of machines
			int numOfMachine = sheet.getRows();
			// The number of rows indicates the number of workfaces
			int numOfWorkface = sheet.getColumns(); 
			
			ww = new WorkfaceWorkload(numOfMachine, numOfWorkface);
			ArrayList<Double> singleMachineWorkload = null;
			for(int row = 0 ;row < numOfMachine; row++){
				singleMachineWorkload = new ArrayList<Double>();
				for(int col = 0; col < numOfWorkface; col++){
					singleMachineWorkload.add((Double) getCellValue(col, row, CellType.NUMBER));
				}
				ww.addMachineWorkload(singleMachineWorkload);
			}
		}
		return ww;
	}
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public WorkfaceDistance readWorkfaceDistance(String fileName){
		
		WorkfaceDistance wd = null;
		boolean isLoadSuccessful = getWorkbook(fileName);
		
		if(isLoadSuccessful == true){
			initWorkSheet(0);
			// The number of rows/columns indicates the number of workfaces
			int numberOfWorkface = sheet.getRows();
			System.out.println("number of rows in distance matrix:"+numberOfWorkface);
			
			wd = new WorkfaceDistance(numberOfWorkface);
			ArrayList<Double> distanceOfEachWorkface = new ArrayList<Double>();
			for(int row = 0; row < numberOfWorkface; row ++){
				distanceOfEachWorkface = new ArrayList<Double>();
				for(int col = 0; col < numberOfWorkface; col ++){
					distanceOfEachWorkface.add((Double) getCellValue(col, row, CellType.NUMBER));
				}
				System.out.println("Row "+ row+" in distance matrix: "+ distanceOfEachWorkface);
				wd.addDistance(distanceOfEachWorkface);
			}
		}else{
			System.out.println("Load distance file failded!");
		}
		
		return wd;
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
		er.convertExcelToText("workface-distance.xls", "workface-distance.txt");
		er.convertExcelToText("workface-state.xls", "workface-state.txt");
		er.convertExcelToText("workface-workload.xls", "workface-workload.txt");
		er.convertExcelToText("machine-op-info.xls", "machine-op-info.txt");
		
		
	}
	
}
