package geo.core;

import java.util.ArrayList;

/**
 * This class 
 * @author Dong
 * @version 1.0
 */
public class ShareMachineUnit {
	
	ArrayList<ProcedureUnit> sharedMachineList;
	
	/**
	 * Create an empty ShareMachineUnit instance.
	 */
	public ShareMachineUnit(){
		sharedMachineList = new ArrayList<ProcedureUnit>();
	}
	
	/**
	 * Add A ProcedureUnit instance by specifying procedure ID, machine total machine number and machine name.
	 * @param procId The procedure ID.
	 * @param machineNum The total number of machines allocated to this procedure.
	 * @param name The machine name.
	 */
	public void addNewProcedureUnit(int procId, int machineNum, String name){
		int i = 0;
		for(; i < sharedMachineList.size(); i ++){
			if(sharedMachineList.get(i).getProcId() == procId){
				sharedMachineList.get(i).addMachineName(name);
				sharedMachineList.get(i).addMachineNum();
				break;
			}
		}
		
		if(i == sharedMachineList.size()){
			ProcedureUnit pu = new ProcedureUnit(procId, machineNum, name);
			sharedMachineList.add(pu);
		}
		
	}
	
	/**
	 * Get the list of shared machines.
	 * @return The shared list of machines.
	 */
	public ArrayList<ProcedureUnit> getSharedMachineList(){
		return sharedMachineList;
	}
	
	/** 
	 * An instance of this Class stores the procedure ID a list of shared machines 
	 * operate on, the list of shared machines and a list of the shared machines' names.
	 * @author Dong
	 * @version 1.0
	 */
	public static class ProcedureUnit{
		// The machine ID
		private int procId; // 0-indexed
		// The total number of operating machines.
		private int numOfMachine;
		// A list storing operating machines' names.
		private ArrayList<String> machineName;
		
		/**
		 * Create an empty ProcedureUnit instance.
		 */
		public ProcedureUnit(){};
		
		/**
		 * Create a ProcedureUnit instance by specifying operating machine's ID, the
		 * total number of operating machines and machine's name.
		 * @param proc 
		 * @param machineNum
		 * @param name
		 */
		public ProcedureUnit(int proc, int machineNum, String name){
			procId = proc;
			numOfMachine = machineNum;
			machineName = new ArrayList<String>();
			machineName.add(name);
		}
		
		/**
		 * Get the procedure ID (or machine ID).
		 * @return The procedure ID.
		 */
		public int getProcId(){
			return procId;
		}
		
		/**
		 * Get the total number of machines.
		 * @return The total number of machines.
		 */
		public int getMachineNum(){
			return numOfMachine;
		}
		
		/**
		 * Increase the total number of machines by one.
		 */
		public void addMachineNum(){
			numOfMachine ++;
		}
		
		/**
		 * Get the list of machine names.
		 * @return A list of machine names.
		 */
		public ArrayList<String> getMachineName(){
			return machineName;
		}
		
		/**
		 * Add the machine name to the machine name list.
		 * @param name To be added name
		 */
		public void addMachineName(String name){
			if(machineName != null){
				machineName.add(name);
			}else{
				machineName = new ArrayList<String>();
				machineName.add(name);
			}
		}
		
	}
}
