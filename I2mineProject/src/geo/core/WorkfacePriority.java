package geo.core;

import java.util.ArrayList;
import java.util.Collections;

public class WorkfacePriority {
	private ArrayList<WorkfacePrioUnit> wfPrioUnitList = new ArrayList<WorkfacePrioUnit>();
	
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
	
	
	public static class WorkfacePrioUnit implements Comparable<WorkfacePrioUnit>{
		// indexed from 0
		private int wfNum;
		// possible values are 1, 2, 3 (1 > 2 > 3)
		private int priority;
		
		public WorkfacePrioUnit(int wf, int prio){
			wfNum = wf;
			priority = prio;
		}
		@Override
		public int compareTo(WorkfacePrioUnit o) {
			return priority - o.getPriority();
		}
		
		public void setWfNum(int wf){wfNum = wf;}
		public int getWfNum(){return wfNum;}
		public void setPriority(int prio){priority = prio;}
		public int getPriority(){return priority;}
	}
}
