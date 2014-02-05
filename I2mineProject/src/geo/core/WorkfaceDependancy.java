package geo.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A workfaceDependancy instance maintains a list of {@link WorkfaceDependancyUnit} instances 
 * internally. 
 * 
 * @author Dong
 * @version 1.0
 */
public class WorkfaceDependancy {
	
	List<WorkfaceDependancyUnit> wfDependancyList;
	
	public WorkfaceDependancy(){
		wfDependancyList = new ArrayList<WorkfaceDependancyUnit>();
	}
	
	/** 
	 * Add a mapping of workface ID and dependent workface ID to the internal list.
	 * @param wfVal Workface ID
	 * @param deVal Dependent workface ID
	 */
	public void addDependancyUnit(int wfVal, int deVal){
		WorkfaceDependancyUnit unit = new WorkfaceDependancyUnit(wfVal, deVal);
		wfDependancyList.add(unit);
	} 
	
	/**
	 * Get the internal list of workface ID and dependent workface ID mappings.
	 * @return The internal list of mappings of workface ID and dependent workface ID.
	 */
	public List<WorkfaceDependancyUnit> getDependancyUnitList(){
		return wfDependancyList;
	}
	
	/**
	 * Get the dependent workface ID of workface identified by <i>machineNum</i>.
	 * 
	 * @param wfNum Workface ID.
	 * @return The dependent workface ID of workface <i>machineNum</i>
	 */
	public int getDependancyOfMachine(int wfNum){
		Iterator<WorkfaceDependancyUnit> iter = wfDependancyList.iterator();
		while(iter.hasNext()){
			WorkfaceDependancyUnit unit = iter.next();
			if(unit.getWfNum() == wfNum){
				return unit.getDependancyNum();
			}
		}
		return 0;
	}
	
	public int getMachineNumOfDependancy(int depVal){
		Iterator<WorkfaceDependancyUnit> iter = wfDependancyList.iterator();
		while(iter.hasNext()){
			WorkfaceDependancyUnit unit = iter.next();
			if(unit.getDependancyNum() == depVal){
				return unit.getWfNum();
			}
		}
		return 0;
	}
	
	/**
	 * A WorkfaceDependancyUnit instance stores a mapping between the ID of workface
	 * and its dependent workface ID.
	 * 
	 * @author Dong
	 * @version 1.0
	 */
	public static class WorkfaceDependancyUnit{
		// 0-indexed workface ID
		private int wfNum;
		// The dependent workface ID
		private int dependancyNum;
		
		public WorkfaceDependancyUnit(){}
		public WorkfaceDependancyUnit(int wfVal, int deVal){
			wfNum = wfVal;
			dependancyNum = deVal;
		}
		public int getWfNum(){return wfNum;}
		public void setWfNum(int val){wfNum = val;}
		public int getDependancyNum(){return dependancyNum;}
		public void setDenpendancyNum(int val){dependancyNum = val;}
	}
}
