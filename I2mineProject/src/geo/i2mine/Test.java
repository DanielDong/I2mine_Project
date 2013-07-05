package geo.i2mine;

import geo.excel.ExcelReader;

import java.util.Date;

import jxl.CellType;

public class Test {
	
	public static void main(String[] args){
		// test code for creating a new workbook and sheet
//		ExcelWriter excelWriter = new ExcelWriter();
//		excelWriter.createSheet("output.xls", "firstSheet", 0);
//		excelWriter.addLabelToSheet(0, 0, "string value");
//		excelWriter.addNumberToSheet(1, 0, 34.567);
//		excelWriter.addLabelToSheet(2, 0, "2013/02/18");
//		excelWriter.closeWorkbook();
		
		// test code for reading data from a sheet residing a workbook
		ExcelReader er = new ExcelReader();
		er.getWorkbook("output.xls");
		er.initWorkSheet(0);
		String str = (String)er.getCellValue(0, 0, CellType.LABEL);
		double d = (Double)er.getCellValue(1, 0, CellType.NUMBER);
		Date date = (Date)er.getCellValue(2, 0, CellType.DATE);
		System.out.println("string:"+str+"\ndouble:"+d+"\ndate:"+date.toGMTString());
		
	}
}
