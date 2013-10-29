package geo.core;

import java.util.ArrayList;
import java.util.List;

public class MachineInitialPosition {
	List<InitialPositionUnit> initPosList;
	
	public MachineInitialPosition(){
		initPosList = new ArrayList<InitialPositionUnit>();
	}
	
	public void addIniPosUnit(int machineNum, int initVal){
		InitialPositionUnit unit = new InitialPositionUnit(machineNum, initVal);
		initPosList.add(unit);
	}
	
	public List<InitialPositionUnit> getMachineInitPosList(){
		return initPosList;
	}
	
	public int getInitPosOfMachine(int machineNum){
		for(InitialPositionUnit unit: initPosList){
			if(unit.getMachineNum() == machineNum){
				return unit.getInitPosVal();
			}
		}
		return 0;
	}
	
	
	public static class InitialPositionUnit{
		private int machineNum;
		private int initPosVal;
		
		public InitialPositionUnit(){}
		public InitialPositionUnit(int machineVal, int initVal){
			machineNum = machineVal;
			initPosVal = initVal;
		}
		
		public int getMachineNum(){
			return machineNum;
		}
		
		public int getInitPosVal(){
			return initPosVal;
		}
	}
}
