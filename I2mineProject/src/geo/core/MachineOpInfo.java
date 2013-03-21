package geo.core;

import java.util.ArrayList;

public class MachineOpInfo {
	private int numOfMachine;
	private ArrayList<ArrayList<Double>> opInfo = null;
	
	public MachineOpInfo (int machineNum){
		this.numOfMachine = machineNum;
		opInfo = new ArrayList<ArrayList<Double>>();
	}
	
	public int getMachineNum (){
		return this.numOfMachine;
	}
	
	/***
	 * Get operation information of a certain machine specified by <i>indexofMachine</i>
	 * @param indexOfMachine The index of a machine
	 * @return The operation information (OR and MR) of a machine
	 */
	public ArrayList<Double> getCertainMachineOpInfo(int indexOfMachine){
		return this.opInfo.get(indexOfMachine);
	}
	
	public boolean addMachineOpInfo(ArrayList<Double> curOpInfo){
		if((opInfo.size() >= this.numOfMachine) || curOpInfo.size() < 2){
			return false;
		}
		this.opInfo.add(curOpInfo);
		return true;
	}
	
}
