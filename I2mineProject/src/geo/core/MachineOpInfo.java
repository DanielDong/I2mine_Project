package geo.core;

import java.util.ArrayList;
import java.util.List;

/**
 * This class stores operating and moving rate for each machine.
 * It also provides utility functions to easily get and set machine 
 * operating information.
 * 
 * @author Dong
 * @version 1.0
 */
public class MachineOpInfo {
	private int numOfMachine;
	private List<ArrayList<Double>> opInfo = null;
	
	/**
	 * Create a MachineOpInfo instance by specifying the total number of machines.
	 * @param machineNum The number of machines
	 */
	public MachineOpInfo (int machineNum){
		this.numOfMachine = machineNum;
		opInfo = new ArrayList<ArrayList<Double>>();
	}
	
	/**
	 * Create a MachineOpInfo instance by providing the operating machines' operating information.
	 * @param opInfoList The list of operating machines' operating information.
	 */
	public MachineOpInfo(List<ArrayList<Double>> opInfoList){
		opInfo = opInfoList;
		numOfMachine = opInfoList.size();
	}
	
	/**
	 * Get the total number of operating machines.
	 * @return The total number of operating machines.
	 */
	public int getMachineNum (){
		return this.numOfMachine;
	}
	
	/**
	 * Get operation information of a certain machine specified by <i>indexofMachine</i>
	 * @param indexOfMachine The index of a machine
	 * @return The operation information (OR and MR) of a machine
	 */
	public ArrayList<Double> getCertainMachineOpInfo(int indexOfMachine){
		return this.opInfo.get(indexOfMachine);
	}
	
	/**
	 * Add machine operation information to machine operation information array
	 * @param curOpInfo Current machine operating information.
	 * @return true if machine operation information is added successfully, otherwise false
	 */
	public boolean addMachineOpInfo(ArrayList<Double> curOpInfo){
		if((opInfo.size() >= this.numOfMachine) || curOpInfo.size() < 2){
			return false;
		}
		this.opInfo.add(curOpInfo);
		return true;
	}
	
	/**
	 * Get the list of operating machines' information.
	 * @return The list of operating machines' information.
	 */
	public List<ArrayList<Double>> getOpInfoList(){
		return opInfo;
	}
	
	/**
	 * Print out machine operation information
	 */
	public String outputMachineOpInfo(){
		StringBuilder msgOp = new StringBuilder();
		for(int i = 0; i < opInfo.size(); i ++){
			msgOp.append(opInfo.get(i).get(0) + "	" + opInfo.get(i).get(1) + "\n");
		}
		return msgOp.toString();
	}
	
}
