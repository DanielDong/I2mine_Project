package geo.core;

import java.util.ArrayList;

public class DumpSiteCapacity {
	private ArrayList<Float> siteCapacityList;
	
	public DumpSiteCapacity(){
		siteCapacityList = new ArrayList<Float>(); 
	}
	
	public void addSiteCapacity(float cap){
		siteCapacityList.add(cap);
	}
}
