package geo.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogTool {
	
	public static final int LEVEL_CLOSE = 0;
	public static final int LEVEL_OPEN = 1;
	
	public static void log(int level, String message){
		if(level == LEVEL_CLOSE){
			return ;
		}else{
//			System.out.println(message);
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
