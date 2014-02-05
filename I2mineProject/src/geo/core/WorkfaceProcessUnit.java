package geo.core;

import java.util.ArrayList;

/**
 * A WorkfaceProcessUnit instance refers to a workface. It stores the workface ID (0-indexed), final
 * ending time of a workface when all workloads on the workface is done, a list of workface
 * procedure unit instances {@link WorkfaceProcedureUnit}. The workface procedure list represents the 
 * working phases of all the operating machines on the workface.
 * 
 * @author Dong
 * @version 1.0
 */
public class WorkfaceProcessUnit{
	
	/**
	 * Class instance stores a workface procedure details on a 
	 * workface. A workface procedure is identified by the operating 
	 * machine ID and records the starting, ending and moving time(from current
	 * workface to next one) of an operating machine on a workface.
	 * 
	 * @author Dong
	 * @version 1.0
	 */
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
	
	// 0-indexed workface ID
	private int wfId;
	// The final end time when all the workload of a workface is done
	private double totalEndTime;
	// A list of ordered WorkfaceProcedureUnit instances
	private ArrayList<WorkfaceProcedureUnit> wfProcList;
	
	
	
	public WorkfaceProcessUnit(){
		wfProcList = new ArrayList<WorkfaceProcedureUnit>();
	}
	
	/**
	 * Create a workface process unit instance which refers to 
	 * a workface. Such an instance is indentified by the workface
	 * ID
	 * @param id Workface ID (0-indexed)
	 */
	public WorkfaceProcessUnit(int id){
		wfId = id;
		wfProcList = new ArrayList<WorkfaceProcedureUnit>();
	}
	
	/**
	 * Get the list of workface procedure unit instances
	 * @return The list of workface procedure unit instances
	 */
	public ArrayList<WorkfaceProcedureUnit> getWfProcList(){
		return wfProcList;
	}
	
	/**
	 * Get the final ending time of current workface 
	 * @return The final ending time
	 */
	public double getTotalEndTime(){
		return totalEndTime;
	}
	
	/**
	 * Set the final ending time when all workload of current 
	 * workface is done
	 * @param endTime The final ending time
	 */
	public void setTotalEndTime(double endTime){
		totalEndTime = endTime;
	}
	
	/**
	 * Get the moving time of an operating machine identified by machine ID from
	 * current workface to next one
	 * @param machineId Operating machine ID
	 * @return The moving time of operating machine identified by ID from current 
	 * workface to next one
	 */
	public double getMovTime(int machineId){
		for(WorkfaceProcedureUnit wpu: wfProcList){
			if(wpu.getMachineId() == machineId){
				return wpu.getMovTime();
			}
		}
		return -1;
	}
	
	/**
	 * Set the moving time of an operating machine from current workface to next one
	 * @param machineId An operating machine ID
	 * @param mov The to-be-set moving time
	 */
	public void setMovTime(int machineId, double mov){
		for(WorkfaceProcedureUnit wpu: wfProcList){
			if(wpu.getMachineId() == machineId){
				wpu.setMovTime(mov);
				break;
			}
		}
	}
	
	/**
	 * Set the starting time of a workface procedure identified by the machine ID
	 * @param machineId An operating machine ID
	 * @param start The to-be-set starting time
	 */
	public void setStartTime(int machineId, double start){
		boolean isSuccessful = false;
		for(WorkfaceProcedureUnit wpu: wfProcList){
			if(wpu.getMachineId() == machineId){
				wpu.setStartTime(start);
				isSuccessful = true;
				break;
			}
		}
		
		if(isSuccessful == false){
			WorkfaceProcedureUnit wpu = new WorkfaceProcedureUnit(machineId, start, 0, 0);
			wfProcList.add(wpu);
		}
	}
	
	/**
	 * Get the starting time of a workface procedure which is identified by the machine ID
	 * @param machineId An operating machine ID to identity the workface procedure
	 * @return The starting time of the workface procedure.
	 */
	public double getStartTime(int machineId){
		for(WorkfaceProcedureUnit wpu: wfProcList){
			if(wpu.getMachineId() == machineId){
				return wpu.getStartTime();
			}
		}
		return -1;
	}
	
	/**
	 * Get the ending time of a workface procedure which is identified by the machine ID
	 * @param machineId An operating machine ID to identity the workface procedure
	 * @return The ending time of the workface procedure.
	 */
	public double getEndTime(int machineId){
		for(WorkfaceProcedureUnit wpu: wfProcList){
			if(wpu.getMachineId() == machineId){
				return wpu.getEndTime();
			}
		}
		return -1;
	}
	
	/**
	 * Set the ending time of a workface procedure identified by the machine ID
	 * @param machineId An operating machine ID
	 * @param end The to-be-set ending time
	 */
	public void setEndTime(int machineId, double end){
		boolean isSuccessful = false;
		for(WorkfaceProcedureUnit wpu: wfProcList){
			if(wpu.getMachineId() == machineId){
				wpu.setEndTime(end);
				isSuccessful = true;
				break;
			}
		}
		if(isSuccessful == false){
			WorkfaceProcedureUnit wpu = new WorkfaceProcedureUnit(machineId, 0, end, 0);
			wfProcList.add(wpu);
		}
	}
	
	/**
	 * Get the workface ID of current workface process unit instance
	 * @return The workface ID
	 */
	public int getWfId(){return wfId;}
	
}
