package geo.core;

import java.util.ArrayList;

public class WorkfaceState {
	
	private ArrayList<Integer> workfaceState = null;
	private int numOfWorkface = 0;
	/**
	 * @param workfaceNum the number of workfaces
	 */
	public WorkfaceState(int workfaceNum){
		
		this.numOfWorkface = workfaceNum;
		this.workfaceState = new ArrayList<Integer> (this.numOfWorkface);
	}
	
	public boolean addWorkfaceState(int state){
		
		if(this.workfaceState.size() < this.numOfWorkface){
			this.workfaceState.add(state);
		}else
			return false;
		return true;
	}
}
