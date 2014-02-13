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
	
	/**
	 * Create an empty MachineInitailPosition instane.
	 */
	public MachineInitialPosition(){
		initPosList = new ArrayList<InitialPositionUnit>();
		map = new HashMap<Integer, InitialPositionUnit>();
	}
	
	/**
	 * Add a record of machine ID and corresponding initial position.
	 * @param machineNum The machine ID.
	 * @param initVal The initial position.
	 */
	public void addIniPosUnit(int machineNum, int initVal){
		InitialPositionUnit unit = new InitialPositionUnit(machineNum, initVal);
		initPosList.add(unit);
		map.put(machineNum, unit);
	}
	
	/**
	 * Get the list of initial positions for all operating machines.
	 * @return The list of initial positions for all operating machines.
	 */
	public List<InitialPositionUnit> getMachineInitPosList(){
		return initPosList;
	}
	
	/**
	 * Get the initial position of a machine indicated by ID <i>machineNum</i>.
	 * @param machineNum The machine ID.
	 * @return The initial position of the machine.
	 */
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
		
		/**
		 * Create an empty InitialPositionUnit instance.
		 */
		public InitialPositionUnit(){}
		
		/**
		 * Create an InitialPositionUnit instance by specifying the machine ID  
		 * and corresponding initial position.
		 * @param machineVal Machine ID.
		 * @param initVal Initial position.
		 */
		public InitialPositionUnit(int machineVal, int initVal){
			machineNum = machineVal;
			initPosVal = initVal;
		}
		
		/**
		 * Get the machine ID.
		 * @return The machine ID.
		 */
		public int getMachineNum(){
			return machineNum;
		}
		
		/**
		 * Get the initial position.
		 * @return The initial position.
		 */
		public int getInitPosVal(){
			return initPosVal;
		}
	}
}
