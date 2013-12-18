package geo.core;

import java.util.ArrayList;

public class DumpSiteWorkfaceDistance {
	
	ArrayList<ArrayList<Float>> dumpSiteWfDist;
	public DumpSiteWorkfaceDistance(){
		dumpSiteWfDist = new ArrayList<ArrayList<Float>>();
	}
	public void addDumpSiteList(ArrayList<Float> curDumpSiteList){
		dumpSiteWfDist.add(curDumpSiteList);
	}
}
