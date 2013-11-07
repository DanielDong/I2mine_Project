package geo.core;

import java.util.ArrayList;

public class WorkfaceProcessUnit {
	// 0-indexed
	private int wfId;
	
	public static class WorkfaceProcedureUnit{
		// 0-indexed
		private int machineId;
		private double startTime, endTime;
		
		public WorkfaceProcedureUnit(){}
		public WorkfaceProcedureUnit(int id, double start, double end){
			machineId = id;
			startTime = start;
			endTime = end;
		}
		
		public double getStartTime(){return startTime;}
		public double getEndTime(){return endTime;}
		public void setStartTime(double start){startTime = start;}
		public void setEndTime(double end){endTime = end;}
		public int getMachineId(){return machineId;}
	}
	
	private ArrayList<WorkfaceProcedureUnit> wfProcList;
	
	public WorkfaceProcessUnit(){
		wfProcList = new ArrayList<WorkfaceProcedureUnit>();
	};
	public WorkfaceProcessUnit(int id){
		wfId = id;
		wfProcList = new ArrayList<WorkfaceProcedureUnit>();
	};
	
	public void setStartTime(int machineId, double start){
		for(WorkfaceProcedureUnit wpu: wfProcList){
			if(wpu.getMachineId() == machineId){
				wpu.setStartTime(start);
			}
		}
	}
	
}
