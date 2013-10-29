package geo.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WorkfaceDependancy {
	
	List<WorkfaceDependancyUnit> wfDependancyList;
	public WorkfaceDependancy(){
		wfDependancyList = new ArrayList<WorkfaceDependancyUnit>();
	}
	
	public void addDependancyUnit(int wfVal, int deVal){
		WorkfaceDependancyUnit unit = new WorkfaceDependancyUnit(wfVal, deVal);
		wfDependancyList.add(unit);
	} 
	
	public List<WorkfaceDependancyUnit> getDependancyUnitList(){
		return wfDependancyList;
	}
	
	public int getDependancyOfMachine(int machineNum){
		Iterator<WorkfaceDependancyUnit> iter = wfDependancyList.iterator();
		while(iter.hasNext()){
			WorkfaceDependancyUnit unit = iter.next();
			if(unit.getWfNum() == machineNum){
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
	
	public static class WorkfaceDependancyUnit{
		private int wfNum;
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
