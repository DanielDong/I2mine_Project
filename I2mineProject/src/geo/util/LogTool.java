package geo.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 
 * This class provides logging tool for logging debug information.
 * Debug information is stored into a file called <s>logdata.txt</s>under the project root directory. 
 * @author Dong
 * @version 1.0
 */
public class LogTool {
	
	// Do not store debug information.
	public static final int LEVEL_CLOSE = 0;
	// Store the debug information into log file.
	public static final int LEVEL_OPEN = 1;
	
	public static void log(int level, String message){
		if(level == LEVEL_CLOSE){
			return ;
		}else{
			File f = new File("logdata.txt");
			FileWriter fw;
			try {
				fw = new FileWriter(f, true);
				fw.append(message);
				fw.flush();
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	} 
}
