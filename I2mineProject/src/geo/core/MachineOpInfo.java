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
	
	public boolean addMachineOpInfo(ArrayList<Double> curOpInfo){
		if((opInfo.size() >= this.numOfMachine) || curOpInfo.size() < 2){
			return false;
		}
		this.opInfo.add(curOpInfo);
		return true;
	}
	
}
