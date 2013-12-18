package geo.core;

import java.util.ArrayList;

public class WorkfaceMineralCapacity {
	
	ArrayList<Float> wfCapList;
	public WorkfaceMineralCapacity(){
		wfCapList = new ArrayList<Float>(); 
	}
	
	public void addCapacity(float cap){
		wfCapList.add(cap);
	}
	
	public float getWorkfaceCapacity(int wfId){
		return wfCapList.get(wfId);
	}
}
