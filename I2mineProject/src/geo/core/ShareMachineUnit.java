package geo.core;

import java.util.ArrayList;

public class ShareMachineUnit {
	
	ArrayList<ProcedureUnit> sharedMachineList;
	
	public ShareMachineUnit(){
		sharedMachineList = new ArrayList<ProcedureUnit>();
	}
	
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
	
	public ArrayList<ProcedureUnit> getSharedMachineList(){
		return sharedMachineList;
	}
	
	public static class ProcedureUnit{
		private int procId; // 0-indexed
		private int numOfMachine;
		private ArrayList<String> machineName;
		
		public ProcedureUnit(){};
		public ProcedureUnit(int proc, int machineNum, String name){
			procId = proc;
			numOfMachine = machineNum;
			machineName = new ArrayList<String>();
			machineName.add(name);
		}
		
		public int getProcId(){
			return procId;
		}
		
		public int getMachineNum(){
			return numOfMachine;
		}
		
		public void addMachineNum(){
			numOfMachine ++;
		}
		
		public ArrayList<String> getMachineName(){
			return machineName;
		}
		
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
