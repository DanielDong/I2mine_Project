package geo.core;

import java.util.ArrayList;

public class WorkfaceProcessUnit {
	// 0-indexed
	private int wfId;
	
	public static class WorkfaceProcedureUnit{
		// 0-indexed
		private int machineId;
		private double startTime, endTime, movTime;
		
		public WorkfaceProcedureUnit(){}
		public WorkfaceProcedureUnit(int id, double start, double end, double mov){
			machineId = id;
			startTime = start;
			endTime = end;
			movTime = mov;
		}
		
		public double getStartTime(){return startTime;}
		public double getEndTime(){return endTime;}
		public void setStartTime(double start){startTime = start;}
		public void setEndTime(double end){endTime = end;}
		public int getMachineId(){return machineId;}
		public double getMovTime(){return movTime;}
		public void setMovTime(double mov){
			movTime = mov;
		}
	}
	
	private ArrayList<WorkfaceProcedureUnit> wfProcList;
	
	public WorkfaceProcessUnit(){
		wfProcList = new ArrayList<WorkfaceProcedureUnit>();
	};
	public WorkfaceProcessUnit(int id){
		wfId = id;
		wfProcList = new ArrayList<WorkfaceProcedureUnit>();
	};
	
	public double getMovTime(int machineId){
		for(WorkfaceProcedureUnit wpu: wfProcList){
			if(wpu.getMachineId() == machineId){
				return wpu.getMovTime();
			}
		}
		return -1;
	}
	public void setMovTime(int machineId, double mov){
		for(WorkfaceProcedureUnit wpu: wfProcList){
			if(wpu.getMachineId() == machineId){
				wpu.setMovTime(mov);
				break;
			}
		}
	}
	public void setStartTime(int machineId, double start){
		for(WorkfaceProcedureUnit wpu: wfProcList){
			if(wpu.getMachineId() == machineId){
				wpu.setStartTime(start);
				break;
			}
		}
	}
	
	public double getEndTime(int machineId){
		for(WorkfaceProcedureUnit wpu: wfProcList){
			if(wpu.getMachineId() == machineId){
				return wpu.getEndTime();
			}
		}
		return -1;
	}
	
	public void setEndTime(int machineId, double end){
		for(WorkfaceProcedureUnit wpu: wfProcList){
			if(wpu.getMachineId() == machineId){
				wpu.setEndTime(end);
				break;
			}
		}
	}
	
	public int getWfId(){return wfId;}
	
}
