package geo.core;

import java.util.ArrayList;

/**
 * A WorkfaceMineralCapacity instance maintains a list of 
 * workface minerals internally. All the mineral values are
 * placed from workface 0, all the way to workface N - 1 (
 * N is the total number of workface), e.g., mineral value of index 
 * 0 is the mineral capacity for workface with ID 0, mineral value 
 * of index 4 is the mineral capacity for workface with ID 4.
 * 
 * @author Dong
 * @version 1.0
 */
public class WorkfaceMineralCapacity {
	
	// Workface mineral capacity list
	ArrayList<Float> wfCapList;
	
	/**
	 * Create an empty WorkfaceMineralCapacity instance.
	 */
	public WorkfaceMineralCapacity(){
		wfCapList = new ArrayList<Float>(); 
	}
	
	/** 
	 * Add mineral capacity for all the workfaces in order from 0 
	 * to N - 1(N is the total number of workfaces)
	 * @param cap The capacity value.
	 */
	public void addCapacity(float cap){
		wfCapList.add(cap);
	}
	
	/**
	 * Get the mineral capacity of workface with ID <i>wfId</i>
	 * @param wfId The workface ID
	 * @return The mineral capacity
	 */
	public float getWorkfaceCapacity(int wfId){
		return wfCapList.get(wfId);
	}
	
	/**
	 * Set the mineral capacipty of workface with ID <i>wfId</i> to <i>cap</i>.
	 * @param wfId The workface ID.
	 * @param cap The minerla capacity value.
	 */
	public void setWorkfaceCapacity(int wfId, float cap){
		wfCapList.set(wfId, cap);
	}
}
