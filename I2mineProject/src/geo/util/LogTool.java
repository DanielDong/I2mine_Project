package geo.util;

public class LogTool {
	
	public static final int LEVEL_CLOSE = 0;
	public static final int LEVEL_OPEN = 1;
	
	public static void log(int level, String message){
		if(level == LEVEL_CLOSE){
			return ;
		}else{
			System.out.println(message);
		}
	} 
}
