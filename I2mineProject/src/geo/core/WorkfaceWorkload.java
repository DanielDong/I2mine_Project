package geo.core;

import java.util.ArrayList;

/**
 * Class instance stores workface workloads for all workfaces from file.
 * 
 * @author Dong
 * @version 1.0
 */
public class WorkfaceWorkload {
	
	private ArrayList<ArrayList<Double>> workload = null;
	private int numOfMachine = 0, numOfWorkface = 0;
	
	/**
	 * Create a WorkfaceWorkload instance by specifying the machine number and workface nubmer.
	 * @param machineNum The number of machines.
	 * @param workfaceNum The number of workfaces.
	 */
	public WorkfaceWorkload(int machineNum, int workfaceNum){
		this.numOfMachine = machineNum;
		this.numOfWorkface = workfaceNum;
		workload = new ArrayList<ArrayList<Double>>(this.numOfWorkface);
	}
	
	
	/**
	 * Set <i>wIndex</i> workface's workload for <i>mIndex</i> machine to value.
	 * @param mIndex The ID of workface.
	 * @param wIndex The ID of operaing machine.
	 * @param value The workload value of operating machine with ID of <i>mIndex</i> on workface with ID of <i>wIndex</i>.
	 */
	public void setWorkloadForMachineOnCertainWf(int mIndex, int wIndex, double value){
		workload.get(mIndex).set(wIndex, value);
	}
	
	/**
	 * Get the total machine number.
	 * @return the number of machine.
	 */
	public int getMachineNum(){
		return this.numOfMachine;
	}
	
	/**
	 * Get total workface number.
	 * @return the number of workface.
	 */
	public int getWorkfaceNum(){
		return this.numOfWorkface;
	}
	
	/**
	 * Insert workloads for all machines on one workface into machine workload array.
	 * @param curWorkload Workloads for all work machines on one workface.
	 * @return false machine's workload array is already complete; true workloads are added successfully.
	 */
	public boolean addMachineWorkload(ArrayList<Double> curWorkload){
		
		if((this.workload.size() >= this.numOfMachine) || (curWorkload.size() < this.numOfMachine)){
			return false;
		}
		else
			this.workload.add(curWorkload);
		return true;
	}
	
	/**
	 * Get the total workload from all workfaces of a machine (machine indexed from 0).
	 * @param index The index of a machine.
	 * @return The workload in all workfaces of a machine.
	 */
	public ArrayList<Double> getWorkloadOfMachine (int index){
		return this.workload.get(index);
	}
	
	/**
	 * Obtain workface workload information.
	 */
	public String OutputWorkload(){
		StringBuilder msgWorkload = new StringBuilder();
		for(int i = 0; i < workload.size(); i ++){
			msgWorkload.append(workload.get(i) + "\n");
		}
		return msgWorkload.toString();
	}
}
