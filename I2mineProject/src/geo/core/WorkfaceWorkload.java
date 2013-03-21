package geo.core;

import java.util.ArrayList;

public class WorkfaceWorkload {
	
	private ArrayList<ArrayList<Double>> workload = null;
	private int numOfMachine = 0, numOfWorkface = 0;
	
	/**
	 * 
	 * @param machineNum The number of machines
	 * @param workfaceNum The number of workfaces
	 */
	public WorkfaceWorkload(int machineNum, int workfaceNum){
		this.numOfMachine = machineNum;
		this.numOfWorkface = workfaceNum;
		workload = new ArrayList<ArrayList<Double>>(this.numOfWorkface);
	}
	
	public int getMachineNum(){
		return this.numOfMachine;
	}
	
	public int getWorkfaceNum(){
		return this.numOfWorkface;
	}
	
	public boolean addMachineWorkload(ArrayList<Double> curWorkload){
		
		if((this.workload.size() >= this.numOfMachine) || (curWorkload.size() < this.numOfMachine)){
			return false;
		}
		else
			this.workload.add(curWorkload);
		return true;
	}
	
	/***
	 * Get the total workload from all workfaces of a machine
	 * @param index The index of a machine
	 * @return The workload in all workfaces of a machine
	 */
	public ArrayList<Double> getWorkloadOfMachine (int index){
		return this.workload.get(index);
	}
}
