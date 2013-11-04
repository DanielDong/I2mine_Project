package geo.core;

import java.util.ArrayList;

public class ShareMachineUnit {
	
	ArrayList<ProcedureUnit> sharedMachineList;
	
	public ShareMachineUnit(){
		sharedMachineList = new ArrayList<ProcedureUnit>();
	}
	
	public void addNewProcedureUnit(int procId, int machineNum, String name){
		ProcedureUnit pu = new ProcedureUnit(procId, machineNum, name);
		sharedMachineList.add(pu);
	}
	
	public ArrayList<ProcedureUnit> getSharedMachineList(){
		return sharedMachineList;
	}
	
	public static class ProcedureUnit{
		private int procId; // 0-indexed
		private int numOfMachine;
		private String machineName;
		
		public ProcedureUnit(){};
		public ProcedureUnit(int proc, int machineNum, String name){
			procId = proc;
			numOfMachine = machineNum;
			machineName = name;
		}
		
		public int getProcId(){
			return procId;
		}
		
		public int getMachineNum(){
			return numOfMachine;
		}
		
		public String getMachineName(){
			return machineName;
		}
		
	}
}
