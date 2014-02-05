package geo.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class instance stores initial positions for all operating machines.
 * Class implementation uses a {@link HashMap} to provide constant
 * time lookup for initial position value of a given operating machine by 
 * machine ID.
 * 
 * @author Dong
 * @version 1.0
 */
public class MachineInitialPosition {
	List<InitialPositionUnit> initPosList;
	Map<Integer, InitialPositionUnit> map;
	
	public MachineInitialPosition(){
		initPosList = new ArrayList<InitialPositionUnit>();
		map = new HashMap<Integer, InitialPositionUnit>();
	}
	
	public void addIniPosUnit(int machineNum, int initVal){
		InitialPositionUnit unit = new InitialPositionUnit(machineNum, initVal);
		initPosList.add(unit);
		map.put(machineNum, unit);
	}
	
	public List<InitialPositionUnit> getMachineInitPosList(){
		return initPosList;
	}
	
	public int getInitPosOfMachine(int machineNum){
		if(map.containsKey(machineNum)){
			return map.get(machineNum).getInitPosVal();
		}
		return 0;
	}
	
	/**
	 * Class instance stores operating machine ID and 
	 * initial workface ID.
	 * 
	 * @author Dong
	 * @version 1.0
	 */
	public static class InitialPositionUnit{
		// operating machine ID
		private int machineNum;
		// initial workface ID
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
