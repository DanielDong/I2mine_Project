package geo.core;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A WorkfacePriority instance maintains a list of {@link WorkfacePrioUnit} instances internally.
 * 
 * @author Dong
 * @version 1.0
 */
public class WorkfacePriority {
	private ArrayList<WorkfacePrioUnit> wfPrioUnitList = new ArrayList<WorkfacePrioUnit>();
	
	/**
	 * Add a WorkfacePrioUnit instance to the internal priority list. 
	 * @param newUnit A WorkfacePrioUnit instance storing a mapping between a workface its corresponding priority value.
	 */
	public void addWfPrioUnit(WorkfacePrioUnit newUnit){
		wfPrioUnitList.add(newUnit);
	}
	/**
	 * Get workface list which is sorted by their priority values in ascending order.
	 * @return returned sorted workface list.
	 */
	public ArrayList<Integer> getSortedWfByPriority(){
		Collections.sort(wfPrioUnitList);
		ArrayList<Integer> retList = new ArrayList<Integer>();
		for(WorkfacePrioUnit unit: wfPrioUnitList){
			retList.add(unit.getWfNum());
		}
		return retList;
	}
	
	/**
	 * Get workface lists where each list groups workfaces with the same priority level together.
	 * @return returned workface lists.
	 */
	public ArrayList<ArrayList<Integer>> getSortedWfListsByPriority(){
		Collections.sort(wfPrioUnitList);
		ArrayList<ArrayList<Integer>> retLists = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> curList = new ArrayList<Integer>();
		int prioLevel = 1;
		for(int i = 0; i < wfPrioUnitList.size(); i ++){
			WorkfacePrioUnit unit = wfPrioUnitList.get(i);
			if(unit.getPriority() == prioLevel){
				curList.add(unit.getWfNum());
			}else{
				prioLevel ++;
				retLists.add(curList);
				curList = new ArrayList<Integer>();
				curList.add(unit.getWfNum());
			}
		}
		retLists.add(curList);
		return retLists;
	}
	
	
	/**
	 * A WorkfacePrioUnit instance stores a mapping between the ID of a workface and 
	 * its priority.
	 * 
	 * @author Dong
	 * @version 1.0
	 */
	public static class WorkfacePrioUnit implements Comparable<WorkfacePrioUnit>{
		// 0-indexed workface ID
		private int wfNum;
		// Priority level, possible values are 1, 2, 3 (1 > 2 > 3)
		private int priority;
		
		/**
		 * Create a WorkfacePrioUnit instance by specifying the Workface ID and its corresponding priority value.
		 * @param wf Workface ID.
		 * @param prio Priority value of the workface.
		 */
		public WorkfacePrioUnit(int wf, int prio){
			wfNum = wf;
			priority = prio;
		}
		
		/**
		 * Compare two WorkfacePrioUnit instances based on their priority values.
		 * Workfaces with smaller priority values come before worfaces with bigger priority values.
		 */
		@Override
		public int compareTo(WorkfacePrioUnit o) {
			return priority - o.getPriority();
		}
		
		/**
		 * Set the workface ID.
		 * @param wf The ID of the workface.
		 */
		public void setWfNum(int wf){wfNum = wf;}
		
		/**
		 * Get the total of workfaces.
		 * @return The number of workfaces.
		 */
		public int getWfNum(){return wfNum;}
		
		/**
		 * Set the priority value of current workface.
		 * @param prio The priority value.
		 */
		public void setPriority(int prio){priority = prio;}
		
		/**
		 * Get the priority value of current workface.
		 * @return The priority value.
		 */
		public int getPriority(){return priority;}
	}
}
