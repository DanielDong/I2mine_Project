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
	
	/***
	 * Get the workface state of a certain workface specified by <i>index</i>
	 * @param index The index of a workface
	 * @return the state of the workface specified by <i>index</i>
	 */
	public int getCerntainWorkfaceState(int index){
		return this.workfaceState.get(index);
	}
}