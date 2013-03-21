package geo.core;

import java.util.ArrayList;

public class WorkfaceDistance {
	
	private int numOfWorkface;
	
	private ArrayList<ArrayList<Double>> distance = null;
	
	public WorkfaceDistance(int workfaceNum){
		this.numOfWorkface = workfaceNum;
		this.distance = new ArrayList<ArrayList<Double>> (this.numOfWorkface);
	}
	
	/**
	 * Add distances between one workface to the rest to the distance matrix.
	 * @param singleWorkfaceToAll 
	 * 		  The distance ArrayList representing distances between one workface and the rest of workfaces.
	 * @return true if added successfully, otherwise false.
	 * <p>
	 * NOTE: if the distance matrix is full (num_of_workface X num_of_workface matrix), then false is returned also.
	 * </p>
	 */
	public boolean addDistance(ArrayList<Double> singleWorkfaceToAll){
		if((singleWorkfaceToAll.size() < this.numOfWorkface + 1) ||
				(this.distance.size() >= this.numOfWorkface)){
			return false;
		}
		
		this.distance.add(singleWorkfaceToAll);
		return true;
	}
	
	/**
	 * Get the number of workfaces.
	 * @return The number of the workfaces
	 */
	public int getNumOfWorkface(){
		return this.numOfWorkface;
	}
	
	/**
	 * Get the distance between two workfaces
	 * @param workface1 The index of one workface
	 * @param workface2 The index of the other workface
	 * @return The distance between workface1 and workface2
	 */
	public double getDistBetweenTwoWorkfaces(int workface1, int workface2){
		return this.distance.get(workface1).get(workface2);
	}
}
