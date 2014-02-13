package geo.cluster;

import geo.core.DUComparator;
import geo.core.DistanceUnit;
import geo.core.MachineInitialPosition;
import geo.core.MachineOpInfo;
import geo.core.ShareMachineUnit;
import geo.core.WorkfaceDependancy;
import geo.core.WorkfaceDistance;
import geo.core.WorkfacePriority;
import geo.core.WorkfaceProcessUnit;
import geo.core.WorkfaceWorkload;
import geo.excel.ExcelReader;
import geo.util.LogTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.tools.data.FileHandler;

/**
 * This class provides methods to cluster a data set.
 * Once one cluster number satisfies requirements. Algorithm stops.
 * 
 * @author Dong
 * @version 1.0
 */
public class ClusterTool {
	
	public static int LEVEL = -1;
	public enum Parentheses {FIRST_LEVEL, SECOND_LEVEL, THIRD_LEVEL, FORTH_LEVEL, FIFTH_LEVEL,  NONE};
	public static Parentheses parenClearLevel;
	
	/**
	 * Class to record the parenthesis level and index of current parenthesis.
	 * @author Dong
	 */
	static class ParenLevel{
		public char val;
		public int serialNum;
		public int index;
		
		public ParenLevel(char curChar, int tmpSrlNum, int tmpIndex){
			val = curChar; serialNum = tmpSrlNum; index = tmpIndex;
		}
	}
	
	/**
	 * Group workfaces based on distances between them for 2 sets of machines.
	 * Note: value indexed from 0
	 * @param distance Distance values between all workfaces.
	 * @return 2 groups of workfaces based on geological distances.
	 */
	public static ArrayList<ArrayList<Integer>> get2GroupsOfWorkfaces(WorkfaceDistance distance){
		int numOfWf = distance.getNumOfWorkface(), maxFrom = 0, maxTo = 0;
		double maxDist = 0.0;
		for(int i = 0; i < numOfWf; i ++){
			for(int j = i + 1; j < numOfWf; j ++){
				double tmpDist = distance.getDistBetweenTwoWorkfaces(i, j);
				if(tmpDist > maxDist){
					maxDist = tmpDist;
					maxFrom = i;
					maxTo = j;
				}
			}
		}
		
		ArrayList<ArrayList<Integer>> groups = null;
		ArrayList<Integer> subGroup1 = null;
		ArrayList<Integer> subGroup2 = null;
		while(true){
			groups = new ArrayList<ArrayList<Integer>>();
			subGroup1 = new ArrayList<Integer>();
			subGroup2 = new ArrayList<Integer>();
			subGroup1.add(maxFrom);
			subGroup2.add(maxTo);
			groups.add(subGroup1);
			groups.add(subGroup2);
			
			// Initial grouping after determining the two workfaces with max distance.
			for(int i = 0; i < numOfWf; i ++){
				if(i == maxFrom || i == maxTo)
					continue;
				
				if(distance.getDistBetweenTwoWorkfaces(maxFrom, i) > distance.getDistBetweenTwoWorkfaces(maxTo, i)){
					subGroup2.add(i);
				}else{
					subGroup1.add(i);
				}
			}
			
			double sum1 = 0, sum2 = 0;
			for(int i = 1; i < subGroup1.size(); i ++){
				sum1 += distance.getDistBetweenTwoWorkfaces(maxFrom, subGroup1.get(i));
			}
			
			for(int i = 1; i < subGroup2.size(); i ++){
				sum2 += distance.getDistBetweenTwoWorkfaces(maxTo, subGroup2.get(i));
			}
			
			WorkfaceDist wd1 = get2NewWfwithMaxDist(subGroup1, distance);
			WorkfaceDist wd2 = get2NewWfwithMaxDist(subGroup2, distance);
			if(wd1.dist + wd2.dist >= sum1 + sum2){
				break;
			}else{
				maxFrom = wd1.wfid;
				maxTo = wd2.wfid;
			}
		}
		
		
		return groups;
	}
	/**
	 * Group workfaces based on distances between them for 3 sets of machines.
	 * Note: value indexed from 0
	 * @param distance Distance values between all workfaces.
	 * @return 3 groups of workfaces based on geological distances.
	 */
	public static ArrayList<ArrayList<Integer>> get3GroupsOfWorkfaces(WorkfaceDistance distance){
		int numOfWf = distance.getNumOfWorkface(), maxFrom = 0, maxMiddle = 0,  maxTo = 0;
		double maxDist = 0.0;
		for(int i = 0; i < numOfWf; i ++){
			for(int j = i + 1; j < numOfWf; j ++){
				for(int k = j + 1; k < numOfWf; k ++){
					double tmpDist = distance.getDistBetweenTwoWorkfaces(i, j) + distance.getDistBetweenTwoWorkfaces(j, k);
					if(tmpDist > maxDist){
						maxDist = tmpDist;
						maxFrom = i;
						maxMiddle = j;
						maxTo = k;
					}
				}
			}
		}
		
		ArrayList<ArrayList<Integer>> groups = null;
		ArrayList<Integer> subGroup1 = null;
		ArrayList<Integer> subGroup2 = null;
		ArrayList<Integer> subGroup3 = null;
		while(true){
			groups = new ArrayList<ArrayList<Integer>>();
			subGroup1 = new ArrayList<Integer>();
			subGroup2 = new ArrayList<Integer>();
			subGroup3 = new ArrayList<Integer>();
			subGroup1.add(maxFrom);
			subGroup2.add(maxMiddle);
			subGroup3.add(maxTo);
			groups.add(subGroup1);
			groups.add(subGroup2);
			groups.add(subGroup3);
			
			// Initial grouping after determining the two workfaces with max distance.
			for(int i = 0; i < numOfWf; i ++){
				if(i == maxFrom || i == maxTo || i == maxMiddle)
					continue;
				double tmpSum1 = distance.getDistBetweenTwoWorkfaces(maxFrom, i);
				double tmpSum2 = distance.getDistBetweenTwoWorkfaces(maxMiddle, i);
				double tmpSum3 = distance.getDistBetweenTwoWorkfaces(maxTo, i);
				
				double retSum = Math.min(Math.min(tmpSum1, tmpSum2), tmpSum3);
				if(retSum == tmpSum1){
					subGroup1.add(i);
				}else if(retSum == tmpSum2){
					subGroup2.add(i);
				}else{
					subGroup3.add(i);
				}
			}
			
			double sum1 = 0, sum2 = 0, sum3 = 0;
			for(int i = 1; i < subGroup1.size(); i ++){
				sum1 += distance.getDistBetweenTwoWorkfaces(maxFrom, subGroup1.get(i));
			}
			
			for(int i = 1; i < subGroup2.size(); i ++){
				sum2 += distance.getDistBetweenTwoWorkfaces(maxMiddle, subGroup2.get(i));
			}
			
			for(int i = 1; i < subGroup3.size(); i ++){
				sum3 += distance.getDistBetweenTwoWorkfaces(maxTo, subGroup3.get(i));
			}
			
			WorkfaceDist wd1 = get2NewWfwithMaxDist(subGroup1, distance);
			WorkfaceDist wd2 = get2NewWfwithMaxDist(subGroup2, distance);
			WorkfaceDist wd3 = get2NewWfwithMaxDist(subGroup3, distance);
			
			if(wd1.dist + wd2.dist + wd3.dist >= sum1 + sum2 + sum3){
				break;
			}else{
				maxFrom = wd1.wfid;
				maxMiddle = wd2.wfid;
				maxTo = wd3.wfid;
			}
		}		
		return groups;
	}
	/**
	 * Split groups into sub-groups each with workfaces.
	 * @param groups Groups obtained from {@link get2GroupsOfWorkfaces} or {@link get3GroupsOfWorkfaces}.
	 * Note: value indexed from 0 
	 * @return Groups of workfaces where 4 workfaces form a group.
	 */
	public static ArrayList<ArrayList<Integer>> getGroupsby4Wf(ArrayList<Integer> groups, WorkfaceDistance distance){
		ArrayList<ArrayList<Integer>> retList = new ArrayList<ArrayList<Integer>>();
		ArrayList<WorkfaceDist> wdList = new ArrayList<WorkfaceDist>();
		
		for(int i = 0; i < groups.size(); i ++){
			WorkfaceDist wd = new WorkfaceDist();
			wd.wfid = groups.get(i);
			for(int j = 0; j < groups.size(); j ++){
				if(j != i){
					wd.dist += distance.getDistBetweenTwoWorkfaces(groups.get(i), groups.get(j));
				}
			}
			wdList.add(wd);
		}
		
		WDComparator wdc= new WDComparator();
		Collections.sort(wdList, wdc);
		
		ArrayList<Integer> tmpRetList = null;
		int i = wdList.size() - 1;
		/** 4 indicates there are 3 workfaces in each sub-group*/
		while(i >= 3){
			tmpRetList = new ArrayList<Integer>();
			tmpRetList.add(wdList.get(i - 0).wfid);
			tmpRetList.add(wdList.get(i - 1).wfid);
			tmpRetList.add(wdList.get(i - 2).wfid);
			tmpRetList.add(wdList.get(i - 3).wfid);
			retList.add(tmpRetList);
			i -= 4;
		}
		
		if(i >= 0){
			tmpRetList = new ArrayList<Integer>();
			while(i >= 0){
				tmpRetList.add(wdList.get(i).wfid);
				i --;
			}
			retList.add(tmpRetList);
		}
		return retList;
	}
	
	/**
	 * Mapping between a workface identified by ID and distance.
	 * @author Dong
	 * @version 1.0
	 */
	private static class WorkfaceDist{
		public int wfid;
		public double dist;
	}
	
	// Sort increasingly
	private static class WDComparator implements Comparator<WorkfaceDist>{

		@Override
		public int compare(WorkfaceDist o1, WorkfaceDist o2) {
			return (o1.dist > o2.dist)? 1: ((o1.dist == o2.dist)? 0: -1);
		}
		
	}
	private static WorkfaceDist get2NewWfwithMaxDist(ArrayList<Integer> wfGroup, WorkfaceDistance distance){
		ArrayList<WorkfaceDist> wdList = new ArrayList<WorkfaceDist>();
		for(int i = 0; i < wfGroup.size(); i ++){
			WorkfaceDist wd = new WorkfaceDist();
			wd.wfid = wfGroup.get(i);
			for(int j = 0; j < wfGroup.size(); j ++){
				if(j != i){
					wd.dist += distance.getDistBetweenTwoWorkfaces(wfGroup.get(i), wfGroup.get(j));
				}
			}
			wdList.add(wd);
		}
		WDComparator wdc = new WDComparator();
		Collections.sort(wdList, wdc);
		return wdList.get(0);
	}
	
	/**
	 * Compare two WorkfaceprocessUnits based on their total end time so far.
	 * @author Dong
	 * @version 1.0
	 */
	public static class WfProcUnitComparator implements Comparator<WorkfaceProcessUnit>{
		@Override
		public int compare(WorkfaceProcessUnit u1, WorkfaceProcessUnit u2){
			double timeDiff = u1.getTotalEndTime() - u2.getTotalEndTime();
			if(timeDiff < 0)
				return -1;
			else if(timeDiff == 0)
				return 0;
			else
				return 1;
		}
	}
	
	/**
	 * Compare two WorkfaceprocessUnits based on their start time so far.
	 * @author Dong
	 * @version 1.0
	 */
	public static class WfProcUnitStartComparator implements Comparator<WorkfaceProcessUnit>{
		@Override
		public int compare(WorkfaceProcessUnit u1, WorkfaceProcessUnit u2){
			double timeDiff = u1.getStartTime(0) - u2.getStartTime(0);
			if(timeDiff < 0)
				return -1;
			else if(timeDiff == 0)
				return 0;
			else
				return 1;
		}
	}
	
	
	/**
	 * Sort workfaces by sets of shared operating machines.
	 * @param opInfo Operating machines' operating information.
	 * @param workload All workloads for all operating machines on all workfaces.
	 * @param distance Distance values between all workfaces.
	 * @param initPos Initial positions for all operating machines.
	 * @param shareUnit Share unit instance.
	 * @return A list of sorted {@link WorkfaceProcessUnit} instances.
	 * @throws IOException When 
	 * @throws URISyntaxException
	 */
	public static ArrayList<WorkfaceProcessUnit> getClustersOfWorkfacesBySharedMachine(MachineOpInfo opInfo, WorkfaceWorkload workload, WorkfaceDistance distance, MachineInitialPosition initPos, ShareMachineUnit shareUnit) throws IOException, URISyntaxException{
		// The number of procedures.
		int numOfProc = opInfo.getMachineNum();
		ArrayList<ShareMachineUnit.ProcedureUnit> shareMachineList = shareUnit.getSharedMachineList();
		ArrayList<WorkfaceProcessUnit> wfProcList = new ArrayList<WorkfaceProcessUnit>(); 
		for(int i = 0; i < workload.getWorkfaceNum(); i ++){
			wfProcList.add(new WorkfaceProcessUnit(i));
		}
		
		double START_TIME = System.currentTimeMillis();
		int curMachineIndex = 0;
		while(curMachineIndex < shareUnit.getSharedMachineList().size()){
			// This number is either 1, 2 or 3. ONLY 3 possbile values for this variable.
			int numOfFirstMachine = shareMachineList.get(curMachineIndex).getMachineNum();
			int toMachinewithSameNum = 0;
			for(int i = 1 + curMachineIndex; i < shareMachineList.size(); i ++){
				if(shareMachineList.get(i).getMachineNum() == numOfFirstMachine){
					toMachinewithSameNum ++;
				}else{
					break;
				}
			}
			List<ArrayList<Double>> opInfoList = opInfo.getOpInfoList();
			MachineOpInfo tmpOpInfo = new MachineOpInfo(opInfoList.subList(curMachineIndex, curMachineIndex + toMachinewithSameNum + 1));
			// Store time interval list
			ArrayList<ArrayList<ArrayList<Double>>> timeIntervalList = new ArrayList<ArrayList<ArrayList<Double>>>(); 
			//Split workfaces into "numOfFirstMachine" groups.
			ArrayList<ArrayList<Integer>> dss = null;
			if(numOfFirstMachine == 1){
				dss = ClusterTool.getClustersOfWorkfacesSortByOne(20, tmpOpInfo, workload, distance, initPos, null);
				// Store the operating, moving and waiting time for each sub-group.
				for(int i = 0; i < dss.size(); i ++){
					timeIntervalList.add(SortTool.computeMachineTimeIntervalInOneRegion(dss.get(i), tmpOpInfo, workload, distance, initPos));
				}
				
				// Get operating(moving) and waiting time of each machine in each group (in total 3)
				for(int groupIndex = 0; groupIndex < 1; groupIndex ++){
					// Current group's operating and waiting time for 
					ArrayList<ArrayList<Double>> curGroupOpWaitTimeList = timeIntervalList.get(groupIndex);
					// Get operating(moving) and waiting time of each machine
					for(int machineIndex = curMachineIndex; machineIndex <= curMachineIndex + toMachinewithSameNum; machineIndex ++){
						ArrayList<Double> curMachineOpTime = timeIntervalList.get(groupIndex).get(2 * (machineIndex - curMachineIndex));
						ArrayList<Double> curMachineWaitTime = timeIntervalList.get(groupIndex).get(2 * (machineIndex - curMachineIndex) + 1);
						ArrayList<Integer> curWfList = dss.get(groupIndex);
						// current machine's operating and moving time for each workface
						for(int wfIndex = 0; wfIndex < curWfList.size(); wfIndex ++){
							// 1-indexed
							int curWf = curWfList.get(wfIndex);
							
							WorkfaceProcessUnit curProcUnit = null;
							for(int procUnit = 0; procUnit <  wfProcList.size(); procUnit ++){
								// procedure unit's workface is 0-indexed
								if(wfProcList.get(procUnit).getWfId() == curWf - 1){
									curProcUnit = wfProcList.get(procUnit);
									break;
								}
							}
							double curWfOpTime = curMachineOpTime.get(wfIndex * 2);
							double curWfMovTime = 0;
							if(wfIndex < curWfList.size() - 1)
								curWfMovTime = curMachineOpTime.get(wfIndex * 2 + 1);
							
							double curWfWaitTime = 0;
							if(wfIndex < curWfList.size() - 1)
								curWfWaitTime = curMachineWaitTime.get(wfIndex);
							
							// For the first workface in an operating machine's operating and moving time interval list.
							if(wfIndex == 0){
								// The machine is the first machine of the current machine listl
								if(machineIndex == curMachineIndex){
									// The first operating machine group
									if(machineIndex == 0){
										double startTime = START_TIME;
										curProcUnit.setStartTime(machineIndex, startTime);
										curProcUnit.setEndTime(machineIndex, startTime + curWfOpTime);
										curProcUnit.setMovTime(machineIndex, curWfMovTime);
									}
									// second operating machine group or later ones.
									else{
										double lastProcEndTime = curProcUnit.getEndTime(machineIndex - 1);
										double distInitialWf = distance.getDistBetweenTwoWorkfaces(curWf - 1, initPos.getInitPosOfMachine(machineIndex));
										double speed = opInfo.getCertainMachineOpInfo(machineIndex).get(1);
										double movTimeFromInitialToWf = START_TIME +  distInitialWf / speed;
										if(lastProcEndTime > movTimeFromInitialToWf){
											curProcUnit.setStartTime(machineIndex, lastProcEndTime);
											curProcUnit.setEndTime(machineIndex, lastProcEndTime + curWfOpTime);
											curProcUnit.setMovTime(machineIndex, curWfMovTime);
										}else{
											curProcUnit.setStartTime(machineIndex, movTimeFromInitialToWf);
											curProcUnit.setEndTime(machineIndex, movTimeFromInitialToWf + curWfOpTime);
											curProcUnit.setMovTime(machineIndex, distInitialWf / speed);
										}										
									}
									
								}else{
									
									double startTime = curProcUnit.getEndTime(machineIndex - 1);
									double distInitialWf = distance.getDistBetweenTwoWorkfaces(curWf - 1, initPos.getInitPosOfMachine(machineIndex));
									double speed = opInfo.getCertainMachineOpInfo(machineIndex).get(1);
									double movTimeFromInitialToWf = START_TIME +  distInitialWf / speed;
									if(startTime > movTimeFromInitialToWf){
										curProcUnit.setStartTime(machineIndex, startTime);
										curProcUnit.setEndTime(machineIndex, startTime + curWfOpTime);
										curProcUnit.setMovTime(machineIndex, curWfMovTime);
									}else{
										curProcUnit.setStartTime(machineIndex, movTimeFromInitialToWf);
										curProcUnit.setEndTime(machineIndex, movTimeFromInitialToWf + curWfOpTime);
										curProcUnit.setMovTime(machineIndex, distInitialWf / speed);
									}
								}
								
							}else{
								if(machineIndex == curMachineIndex){
									// time from previous workface
									// 0-indexed workface list
									int prevWf = curWfList.get(wfIndex - 1);
									WorkfaceProcessUnit prevProcUnit = null;
									for(int procUnit = 0; procUnit <  wfProcList.size(); procUnit ++){
										// procedure unit's workface is 0-indexed
										if(wfProcList.get(procUnit).getWfId() == prevWf - 1){
											prevProcUnit = wfProcList.get(procUnit);
											break;
										}
									}
									double startTime = prevProcUnit.getEndTime(machineIndex);
									double movTime = prevProcUnit.getMovTime(machineIndex);
									
									// End time of last procedure in current workface.
									double lastProcEndTime = 0;
									if(machineIndex > 0)
										lastProcEndTime = curProcUnit.getEndTime(machineIndex - 1);
									if(startTime + movTime > lastProcEndTime){
										curProcUnit.setStartTime(machineIndex, startTime + movTime);
										curProcUnit.setEndTime(machineIndex, startTime + movTime + curWfOpTime);
										curProcUnit.setMovTime(machineIndex, curWfMovTime);
									}else{
										curProcUnit.setStartTime(machineIndex, lastProcEndTime);
										curProcUnit.setEndTime(machineIndex, lastProcEndTime + curWfOpTime);
										curProcUnit.setMovTime(machineIndex, curWfMovTime);
									}
									
									
								}else{
									double startTime1 = curProcUnit.getEndTime(machineIndex - 1);
									
									int prevWf = curWfList.get(wfIndex - 1);
									WorkfaceProcessUnit prevProcUnit = null;
									for(int procUnit = 0; procUnit <  wfProcList.size(); procUnit ++){
										// procedure unit's workface is 0-indexed
										if(wfProcList.get(procUnit).getWfId() == prevWf - 1){
											prevProcUnit = wfProcList.get(procUnit);
											break;
										}
									}
									double startTime2 = prevProcUnit.getEndTime(machineIndex);
									double movTime = prevProcUnit.getMovTime(machineIndex);
									// Need to wait
									if(startTime1 > startTime2 + movTime){
										curProcUnit.setStartTime(machineIndex, startTime1);
										curProcUnit.setEndTime(machineIndex, startTime1 + curWfOpTime);
										curProcUnit.setMovTime(machineIndex, curWfMovTime);
									}else{
										curProcUnit.setStartTime(machineIndex, startTime2 + movTime);
										curProcUnit.setEndTime(machineIndex, startTime2 + movTime + curWfOpTime);
										curProcUnit.setMovTime(machineIndex, curWfMovTime);
									}
								}//end machine
							}// end wf
							if(machineIndex == curMachineIndex + toMachinewithSameNum){
								curProcUnit.setTotalEndTime(curProcUnit.getEndTime(machineIndex));
							}
						}// end for wfIndex
					}//end for machineIndex
					
				}
				
				// Sort all the workfaces so far.
				WfProcUnitComparator wfProcUnitCom = new WfProcUnitComparator();
				Collections.sort(wfProcList, wfProcUnitCom);
				// Update current machine to next un-processed one
				curMachineIndex = curMachineIndex + toMachinewithSameNum + 1;
			}else if(numOfFirstMachine == 2){
				dss = getClustersOfWorkfacesSortByMore(2, 20, tmpOpInfo, workload, distance, initPos, null, false);
				// Store the operating, moving and waiting time for each sub-group.
				for(int i = 0; i < dss.size(); i ++){
					timeIntervalList.add(SortTool.computeMachineTimeIntervalInOneRegion(dss.get(i), tmpOpInfo, workload, distance, initPos));
				}
				
				// Get operating(moving) and waiting time of each machine in each group (in total 3)
				for(int groupIndex = 0; groupIndex < 2; groupIndex ++){
					// Current group's operating and waiting time for 
					ArrayList<ArrayList<Double>> curGroupOpWaitTimeList = timeIntervalList.get(groupIndex);
					// Get operating(moving) and waiting time of each machine
					for(int machineIndex = curMachineIndex; machineIndex <= curMachineIndex + toMachinewithSameNum; machineIndex ++){
						ArrayList<Double> curMachineOpTime = timeIntervalList.get(groupIndex).get(2 * (machineIndex - curMachineIndex));
						ArrayList<Double> curMachineWaitTime = timeIntervalList.get(groupIndex).get(2 * (machineIndex - curMachineIndex) + 1);
						ArrayList<Integer> curWfList = dss.get(groupIndex);
						// current machine's operating and moving time for each workface
						for(int wfIndex = 0; wfIndex < curWfList.size(); wfIndex ++){
							// 1-indexed
							int curWf = curWfList.get(wfIndex);
							
							WorkfaceProcessUnit curProcUnit = null;
							for(int procUnit = 0; procUnit <  wfProcList.size(); procUnit ++){
								// procedure unit's workface is 0-indexed
								if(wfProcList.get(procUnit).getWfId() == curWf - 1){
									curProcUnit = wfProcList.get(procUnit);
									break;
								}
							}
							double curWfOpTime = curMachineOpTime.get(wfIndex * 2);
							double curWfMovTime = 0;
							if(wfIndex < curWfList.size() - 1)
								curWfMovTime = curMachineOpTime.get(wfIndex * 2 + 1);
							
							double curWfWaitTime = 0;
							if(wfIndex < curWfList.size() - 1)
								curWfWaitTime = curMachineWaitTime.get(wfIndex);
							
							// For the first workface in an operating machine's operating and moving time interval list.
							if(wfIndex == 0){
								// The machine is the first machine of the current machine listl
								if(machineIndex == curMachineIndex){
									// The first operating machine group
									if(machineIndex == 0){
										double startTime = START_TIME;
										curProcUnit.setStartTime(machineIndex, startTime);
										curProcUnit.setEndTime(machineIndex, startTime + curWfOpTime);
										curProcUnit.setMovTime(machineIndex, curWfMovTime);
									}
									// second operating machine group or later ones.
									else{
										double lastProcEndTime = curProcUnit.getEndTime(machineIndex - 1);
										double distInitialWf = distance.getDistBetweenTwoWorkfaces(curWf - 1, initPos.getInitPosOfMachine(machineIndex));
										double speed = opInfo.getCertainMachineOpInfo(machineIndex).get(1);
										double movTimeFromInitialToWf = START_TIME +  distInitialWf / speed;
										if(lastProcEndTime > movTimeFromInitialToWf){
											curProcUnit.setStartTime(machineIndex, lastProcEndTime);
											curProcUnit.setEndTime(machineIndex, lastProcEndTime + curWfOpTime);
											curProcUnit.setMovTime(machineIndex, curWfMovTime);
										}else{
											curProcUnit.setStartTime(machineIndex, movTimeFromInitialToWf);
											curProcUnit.setEndTime(machineIndex, movTimeFromInitialToWf + curWfOpTime);
											curProcUnit.setMovTime(machineIndex, distInitialWf / speed);
										}										
									}
									
								}else{
									
									double startTime = curProcUnit.getEndTime(machineIndex - 1);
									double distInitialWf = distance.getDistBetweenTwoWorkfaces(curWf - 1, initPos.getInitPosOfMachine(machineIndex));
									double speed = opInfo.getCertainMachineOpInfo(machineIndex).get(1);
									double movTimeFromInitialToWf = START_TIME +  distInitialWf / speed;
									if(startTime > movTimeFromInitialToWf){
										curProcUnit.setStartTime(machineIndex, startTime);
										curProcUnit.setEndTime(machineIndex, startTime + curWfOpTime);
										curProcUnit.setMovTime(machineIndex, curWfMovTime);
									}else{
										curProcUnit.setStartTime(machineIndex, movTimeFromInitialToWf);
										curProcUnit.setEndTime(machineIndex, movTimeFromInitialToWf + curWfOpTime);
										curProcUnit.setMovTime(machineIndex, distInitialWf / speed);
									}
								}
							}else{
								if(machineIndex == curMachineIndex){
									// time from previous workface
									// 0-indexed workface list
									int prevWf = curWfList.get(wfIndex - 1);
									WorkfaceProcessUnit prevProcUnit = null;
									for(int procUnit = 0; procUnit <  wfProcList.size(); procUnit ++){
										// procedure unit's workface is 0-indexed
										if(wfProcList.get(procUnit).getWfId() == prevWf - 1){
											prevProcUnit = wfProcList.get(procUnit);
											break;
										}
									}
									double startTime = prevProcUnit.getEndTime(machineIndex);
									double movTime = prevProcUnit.getMovTime(machineIndex);
									
									// End time of last procedure in current workface.
									double lastProcEndTime = 0;
									if(machineIndex > 0)
										lastProcEndTime = curProcUnit.getEndTime(machineIndex - 1);
									if(startTime + movTime > lastProcEndTime){
										curProcUnit.setStartTime(machineIndex, startTime + movTime);
										curProcUnit.setEndTime(machineIndex, startTime + movTime + curWfOpTime);
										curProcUnit.setMovTime(machineIndex, curWfMovTime);
									}else{
										curProcUnit.setStartTime(machineIndex, lastProcEndTime);
										curProcUnit.setEndTime(machineIndex, lastProcEndTime + curWfOpTime);
										curProcUnit.setMovTime(machineIndex, curWfMovTime);
									}
								}else{
									double startTime1 = curProcUnit.getEndTime(machineIndex - 1);
									
									int prevWf = curWfList.get(wfIndex - 1);
									WorkfaceProcessUnit prevProcUnit = null;
									for(int procUnit = 0; procUnit <  wfProcList.size(); procUnit ++){
										// procedure unit's workface is 0-indexed
										if(wfProcList.get(procUnit).getWfId() == prevWf - 1){
											prevProcUnit = wfProcList.get(procUnit);
											break;
										}
									}
									double startTime2 = prevProcUnit.getEndTime(machineIndex);
									double movTime = prevProcUnit.getMovTime(machineIndex);
									// Need to wait
									if(startTime1 > startTime2 + movTime){
										curProcUnit.setStartTime(machineIndex, startTime1);
										curProcUnit.setEndTime(machineIndex, startTime1 + curWfOpTime);
										curProcUnit.setMovTime(machineIndex, curWfMovTime);
									}else{
										curProcUnit.setStartTime(machineIndex, startTime2 + movTime);
										curProcUnit.setEndTime(machineIndex, startTime2 + movTime + curWfOpTime);
										curProcUnit.setMovTime(machineIndex, curWfMovTime);
									}
								}//end machine
							}// end wf
							if(machineIndex == curMachineIndex + toMachinewithSameNum){
								curProcUnit.setTotalEndTime(curProcUnit.getEndTime(machineIndex));
							}
						}// end for wfIndex
					}//end for machineIndex
					
				}
				
				// Sort all the workfaces so far.
				WfProcUnitComparator wfProcUnitCom = new WfProcUnitComparator();
				Collections.sort(wfProcList, wfProcUnitCom);
				// Update current machine to next un-processed one
				curMachineIndex = curMachineIndex + toMachinewithSameNum + 1;
			}else if(numOfFirstMachine == 3){
				dss = getClustersOfWorkfacesSortByMore(3, 20, tmpOpInfo, workload, distance, initPos, null, false);
				// Store the operating, moving and waiting time for each sub-group.
				for(int i = 0; i < dss.size(); i ++){
					timeIntervalList.add(SortTool.computeMachineTimeIntervalInOneRegion(dss.get(i), tmpOpInfo, workload, distance, initPos));
				}
				
				// Get operating(moving) and waiting time of each machine in each group (in total 3)
				for(int groupIndex = 0; groupIndex < 3; groupIndex ++){
					// Current group's operating and waiting time for 
					ArrayList<ArrayList<Double>> curGroupOpWaitTimeList = timeIntervalList.get(groupIndex);
					// Get operating(moving) and waiting time of each machine
					for(int machineIndex = curMachineIndex; machineIndex <= curMachineIndex + toMachinewithSameNum; machineIndex ++){
						ArrayList<Double> curMachineOpTime = timeIntervalList.get(groupIndex).get(2 * (machineIndex - curMachineIndex));
						ArrayList<Double> curMachineWaitTime = timeIntervalList.get(groupIndex).get(2 * (machineIndex - curMachineIndex) + 1);
						ArrayList<Integer> curWfList = dss.get(groupIndex);
						// current machine's operating and moving time for each workface
						for(int wfIndex = 0; wfIndex < curWfList.size(); wfIndex ++){
							// 1-indexed
							int curWf = curWfList.get(wfIndex);
							
							WorkfaceProcessUnit curProcUnit = null;
							for(int procUnit = 0; procUnit <  wfProcList.size(); procUnit ++){
								// procedure unit's workface is 0-indexed
								if(wfProcList.get(procUnit).getWfId() == curWf - 1){
									curProcUnit = wfProcList.get(procUnit);
									break;
								}
							}
							double curWfOpTime = curMachineOpTime.get(wfIndex * 2);
							double curWfMovTime = 0;
							if(wfIndex < curWfList.size() - 1)
								curWfMovTime = curMachineOpTime.get(wfIndex * 2 + 1);
							
							double curWfWaitTime = 0;
							if(wfIndex < curWfList.size() - 1)
								curWfWaitTime = curMachineWaitTime.get(wfIndex);
							
							// For the first workface in an operating machine's operating and moving time interval list.
							if(wfIndex == 0){
								// The machine is the first machine of the current machine listl
								if(machineIndex == curMachineIndex){
									// The first operating machine group
									if(machineIndex == 0){
										double startTime = START_TIME;
										curProcUnit.setStartTime(machineIndex, startTime);
										curProcUnit.setEndTime(machineIndex, startTime + curWfOpTime);
										curProcUnit.setMovTime(machineIndex, curWfMovTime);
									}
									// second operating machine group or later ones.
									else{
										double lastProcEndTime = curProcUnit.getEndTime(machineIndex - 1);
										double distInitialWf = distance.getDistBetweenTwoWorkfaces(curWf - 1, initPos.getInitPosOfMachine(machineIndex));
										double speed = opInfo.getCertainMachineOpInfo(machineIndex).get(1);
										double movTimeFromInitialToWf = START_TIME +  distInitialWf / speed;
										if(lastProcEndTime > movTimeFromInitialToWf){
											curProcUnit.setStartTime(machineIndex, lastProcEndTime);
											curProcUnit.setEndTime(machineIndex, lastProcEndTime + curWfOpTime);
											curProcUnit.setMovTime(machineIndex, curWfMovTime);
										}else{
											curProcUnit.setStartTime(machineIndex, movTimeFromInitialToWf);
											curProcUnit.setEndTime(machineIndex, movTimeFromInitialToWf + curWfOpTime);
											curProcUnit.setMovTime(machineIndex, distInitialWf / speed);
										}										
									}
									
								}else{
									
									double startTime = curProcUnit.getEndTime(machineIndex - 1);
									double distInitialWf = distance.getDistBetweenTwoWorkfaces(curWf - 1, initPos.getInitPosOfMachine(machineIndex));
									double speed = opInfo.getCertainMachineOpInfo(machineIndex).get(1);
									double movTimeFromInitialToWf = START_TIME +  distInitialWf / speed;
									if(startTime > movTimeFromInitialToWf){
										curProcUnit.setStartTime(machineIndex, startTime);
										curProcUnit.setEndTime(machineIndex, startTime + curWfOpTime);
										curProcUnit.setMovTime(machineIndex, curWfMovTime);
									}else{
										curProcUnit.setStartTime(machineIndex, movTimeFromInitialToWf);
										curProcUnit.setEndTime(machineIndex, movTimeFromInitialToWf + curWfOpTime);
										curProcUnit.setMovTime(machineIndex, distInitialWf / speed);
									}
								}
								
							}else{
								if(machineIndex == curMachineIndex){
									// time from previous workface
									// 0-indexed workface list
									int prevWf = curWfList.get(wfIndex - 1);
									WorkfaceProcessUnit prevProcUnit = null;
									for(int procUnit = 0; procUnit <  wfProcList.size(); procUnit ++){
										// procedure unit's workface is 0-indexed
										if(wfProcList.get(procUnit).getWfId() == prevWf - 1){
											prevProcUnit = wfProcList.get(procUnit);
											break;
										}
									}
									double startTime = prevProcUnit.getEndTime(machineIndex);
									double movTime = prevProcUnit.getMovTime(machineIndex);
									
									// End time of last procedure in current workface.
									double lastProcEndTime = 0;
									if(machineIndex > 0)
										lastProcEndTime = curProcUnit.getEndTime(machineIndex - 1);
									if(startTime + movTime > lastProcEndTime){
										curProcUnit.setStartTime(machineIndex, startTime + movTime);
										curProcUnit.setEndTime(machineIndex, startTime + movTime + curWfOpTime);
										curProcUnit.setMovTime(machineIndex, curWfMovTime);
									}else{
										curProcUnit.setStartTime(machineIndex, lastProcEndTime);
										curProcUnit.setEndTime(machineIndex, lastProcEndTime + curWfOpTime);
										curProcUnit.setMovTime(machineIndex, curWfMovTime);
									}
								}else{
									double startTime1 = curProcUnit.getEndTime(machineIndex - 1);
									
									int prevWf = curWfList.get(wfIndex - 1);
									WorkfaceProcessUnit prevProcUnit = null;
									for(int procUnit = 0; procUnit <  wfProcList.size(); procUnit ++){
										// procedure unit's workface is 0-indexed
										if(wfProcList.get(procUnit).getWfId() == prevWf - 1){
											prevProcUnit = wfProcList.get(procUnit);
											break;
										}
									}
									double startTime2 = prevProcUnit.getEndTime(machineIndex);
									double movTime = prevProcUnit.getMovTime(machineIndex);
									// Need to wait
									if(startTime1 > startTime2 + movTime){
										curProcUnit.setStartTime(machineIndex, startTime1);
										curProcUnit.setEndTime(machineIndex, startTime1 + curWfOpTime);
										curProcUnit.setMovTime(machineIndex, curWfMovTime);
									}else{
										curProcUnit.setStartTime(machineIndex, startTime2 + movTime);
										curProcUnit.setEndTime(machineIndex, startTime2 + movTime + curWfOpTime);
										curProcUnit.setMovTime(machineIndex, curWfMovTime);
									}
								}//end machine
							}// end wf
							if(machineIndex == curMachineIndex + toMachinewithSameNum){
								curProcUnit.setTotalEndTime(curProcUnit.getEndTime(machineIndex));
							}
						}// end for wfIndex
					}//end for machineIndex
					
				}
				
				// Sort all the workfaces so far.
				WfProcUnitComparator wfProcUnitCom = new WfProcUnitComparator();
				Collections.sort(wfProcList, wfProcUnitCom);
				// Update current machine to next un-processed one
				curMachineIndex = curMachineIndex + toMachinewithSameNum + 1;
			}// end of group 3
			
		}// end while
		
		WfProcUnitStartComparator startCom = new WfProcUnitStartComparator();
		Collections.sort(wfProcList, startCom);
		// Persist ordered workface data to disk.
		File f = new File("SCHEDULE_BY_SHARING_MACHINES.txt");
		FileWriter fw = new FileWriter(f);
		StringBuilder sb = null;
		for(WorkfaceProcessUnit wfpu: wfProcList){
			sb = new StringBuilder();
			sb.append("<<<<Workface ID: " + wfpu.getWfId() + "\n");
			ArrayList<WorkfaceProcessUnit.WorkfaceProcedureUnit> procedureList = wfpu.getWfProcList();
			for(WorkfaceProcessUnit.WorkfaceProcedureUnit procedure: procedureList){
				sb.append("\tOperating Machine ID: " + procedure.getMachineId() + " Start time: " + procedure.getStartTime() + " End time: " + 
							procedure.getEndTime() + " Moving time: " + procedure.getMovTime() + "\n");
			}
			sb.append("\n");
			fw.write(sb.toString());
		}
		fw.close();
		return wfProcList;
	}
	
	/**
	 * Sort all workfaces by dependent relationships between all workfaces.
	 * @param numOfWorkfaces The total number of workfaces.
	 * @param wfDependancy {@link WorkfaceDependancy} instances storing workface dependent relationships.
	 * @param opInfo Operating machines' operating information(operating speed and moving speed).
	 * @param workload All workloads for all operating machines on all workfaces.
	 * @param distance Distance values between all workfaces.
	 * @return A list of groups of sorted workfaces.
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static ArrayList<ArrayList<Integer>> getClustersOfWorkfacesByDependancy( int numOfWorkfaces, WorkfaceDependancy wfDependancy, MachineOpInfo opInfo, WorkfaceWorkload workload, WorkfaceDistance distance, MachineInitialPosition initPos, ArrayList<ArrayList<WorkfaceProcessUnit>> finalProcList) throws IOException, URISyntaxException{
		List<WorkfaceDependancy.WorkfaceDependancyUnit> WfDependancyList = wfDependancy.getDependancyUnitList();
		ArrayList<Integer> tmpRetList = new ArrayList<Integer>();
		ArrayList<Integer> tmpList = new ArrayList<Integer>();
		Iterator<WorkfaceDependancy.WorkfaceDependancyUnit> iter = WfDependancyList.iterator();
		// Get a workface list which rely on no body.
		int cnt = 0;
		while(iter.hasNext()){
			WorkfaceDependancy.WorkfaceDependancyUnit unit = iter.next();
			System.out.println(unit.getDependancyNum() + " " + unit.getWfNum());
			if(unit.getDependancyNum() == unit.getWfNum()){
				tmpRetList.add(unit.getWfNum());
				iter.remove();
			}
		}
		
		// 0-index based
		for(int i = 0; i < tmpRetList.size(); i ++){
			tmpRetList.set(i, tmpRetList.get(i) - 1);
		}
		
		// Returned workfaces' index start from 1
		tmpRetList = sortWorkfacesByGroupOf4(tmpRetList, distance, opInfo, workload, initPos);

		System.out.println("dependancy list size: " + wfDependancy.getDependancyUnitList().size());
		ArrayList<Integer> finalRetList = new ArrayList<Integer>();
		finalRetList.addAll(tmpRetList);
		int count = 0;
		count = tmpRetList.size();
		
		while(count < numOfWorkfaces){
			for(int i = 0; i < tmpRetList.size(); i ++){
				if(wfDependancy.getMachineNumOfDependancy(tmpRetList.get(i)) != 0){
					tmpList.add(wfDependancy.getMachineNumOfDependancy(tmpRetList.get(i)));
				}
			}
			count += tmpList.size();
			finalRetList.addAll(tmpList);
			tmpRetList = tmpList;
			tmpList = new ArrayList<Integer>();
		}
		
		ArrayList<WorkfaceProcessUnit> wfProcList = new ArrayList<WorkfaceProcessUnit>();
		for(int i = 0; i < numOfWorkfaces; i ++){
			WorkfaceProcessUnit wpu = new WorkfaceProcessUnit(i);
			wfProcList.add(wpu);
		}
		
		// Store time interval list
		ArrayList<ArrayList<Double>> timeIntervalList = new ArrayList<ArrayList<Double>>();
		timeIntervalList = SortTool.computeMachineTimeIntervalInOneRegion(finalRetList, opInfo, workload, distance, initPos);
	    long START_TIME = System.currentTimeMillis();
		
	    // For each operating machine
	    for(int i = 0; i < opInfo.getMachineNum(); i ++){
	    	// Current machine's processing and moving time
	    	ArrayList<Double> proTime = timeIntervalList.get(2 * i);
	    	// Current machine's wait time
	    	ArrayList<Double> waitTime = timeIntervalList.get(2 * i + 1);
	    	
	    	// For each workface in sorted worface list
	    	for(int j = 0; j < finalRetList.size(); j ++){
	    		int curWfNum = finalRetList.get(j) - 1;
	    		double curWfMachineOpTime = proTime.get(2 * j);
	    		double curWfMachineMovTime = 0;
	    		if(j < finalRetList.size() - 1){
	    			curWfMachineMovTime = proTime.get(2 * j + 1);
	    		}
	    		
	    		double curWfMachineWaitTime = 0;
	    		if(j < finalRetList.size() - 1){
	    			curWfMachineWaitTime = waitTime.get(j);
	    		}
	    		
	    		// Current workface process unit
	    		WorkfaceProcessUnit wpu = wfProcList.get(curWfNum);
	    		// This is the first operating machine (or procedure on this workface)
	    		if(i == 0){
	    			if(j == 0){
	    				wpu.setStartTime(i, START_TIME);
		    			wpu.setEndTime(i, wpu.getStartTime(i) + curWfMachineOpTime);
		    			wpu.setMovTime(i, curWfMachineMovTime);
	    			}else{
	    				WorkfaceProcessUnit prevWpu = wfProcList.get(finalRetList.get(j - 1) - 1);
	    				wpu.setStartTime(i, prevWpu.getEndTime(i) + prevWpu.getMovTime(i));
		    			wpu.setEndTime(i, wpu.getStartTime(i) + curWfMachineOpTime);
		    			wpu.setMovTime(i, curWfMachineMovTime);
	    			}
	    			
	    		}else{
	    			if(j == 0){
	    				wpu.setStartTime(i, wpu.getEndTime(i - 1));
		    			wpu.setEndTime(i, wpu.getStartTime(i) + curWfMachineOpTime);
		    			wpu.setMovTime(i, curWfMachineMovTime);
	    			}else{
	    				WorkfaceProcessUnit prevWpu = wfProcList.get(finalRetList.get(j - 1) - 1);
	    				double endTime1 = wpu.getEndTime(i - 1);
	    				double endTime2 = prevWpu.getEndTime(i) + prevWpu.getMovTime(i);
	    				if(endTime1 > endTime2){
	    					wpu.setStartTime(i, endTime1);
	    				}else{
	    					wpu.setStartTime(i, endTime2);
	    				}
	    				wpu.setEndTime(i, wpu.getStartTime(i) + curWfMachineOpTime);
		    			wpu.setMovTime(i, curWfMachineMovTime);
	    			}
	    		}
	    		if(i == opInfo.getMachineNum() - 1){
	    			wpu.setTotalEndTime(wpu.getEndTime(i));
	    		}
	    	}// end for - workface
	    }// end for - machine
	    
	    WfProcUnitStartComparator startCom = new WfProcUnitStartComparator();
		Collections.sort(wfProcList, startCom);
		if(finalProcList != null){
			finalProcList.add(wfProcList);
		}
		
		// Persist ordered workface data to disk.
		File f = new File("SCHEDULE_BY_DEPENDENCY.txt");
		FileWriter fw = new FileWriter(f);
		StringBuilder sb = null;
		for(WorkfaceProcessUnit wfpu: wfProcList){
			sb = new StringBuilder();
			sb.append("<<<<Workface ID: " + wfpu.getWfId() + "\n");
			ArrayList<WorkfaceProcessUnit.WorkfaceProcedureUnit> procedureList = wfpu.getWfProcList();
			for(WorkfaceProcessUnit.WorkfaceProcedureUnit procedure: procedureList){
				sb.append("\tOperating Machine ID: " + procedure.getMachineId() + " Start time: " + procedure.getStartTime() + " End time: " + 
							procedure.getEndTime() + " Moving time: " + procedure.getMovTime() + "\n");
			}
			sb.append("\n");
			fw.write(sb.toString());
		}
		
		fw.close();
		ArrayList<ArrayList<Integer>> finalRet = new ArrayList<ArrayList<Integer>>();
		finalRet.add(finalRetList);
		return finalRet;
	}
	
	/**
	 * Finish workloads on working faces by working face priority.
	 * @param numOfWorkfaces The total number of working faces.
	 * @param wfPriority A {@link WorkfacePriority} instance stores working face priorities.
	 * @param opInfo Operating machines' operating information(operating speed and moving speed).
	 * @param workload All workloads of all operating machines on all workfaces.
	 * @param distance Distance values between all workfaces.
	 * @param initPos Initial positions of all operating machines.
	 * @param finalProcList Storing a list of process units for LHD.
	 * @return A list of groups of sorted workfaces. 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static ArrayList<ArrayList<Integer>> getClustersOfWorkfacesByPriority(int numOfWorkfaces, WorkfacePriority wfPriority, MachineOpInfo opInfo, WorkfaceWorkload workload, WorkfaceDistance distance, MachineInitialPosition initPos, ArrayList<ArrayList<WorkfaceProcessUnit>> finalProcList) throws IOException, URISyntaxException{
		ArrayList<ArrayList<Integer>> finalRet = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> prioLists = wfPriority.getSortedWfListsByPriority();
		
		System.out.println("priority list size: " + prioLists.size());
		System.out.println("before sort workfaces....");
		/** Sort workfaces by using time(operating time and moving time) matrix*/
//		ArrayList<ArrayList<Integer>> sortedWfLists = SortTool.sortWorkfacesByMatrix(distance, prioLists, opInfo, workload, initPos);
		/** Sort workfaces by using traditional method*/
		ArrayList<ArrayList<Integer>> sortedWfLists = SortTool.sortWorkfacesByTradition(prioLists, opInfo, workload, distance, initPos);
		System.out.println("after sort workfaces....");
		for(int i = 0; i < sortedWfLists.size(); i ++){
			for(int j = 0; j < sortedWfLists.get(i).size(); j ++){
				System.out.print(sortedWfLists.get(i).get(j) + " ");
			}
			System.out.println();
		}
		ArrayList<Integer> sortedWfList = new ArrayList<Integer>();
		for(int i = 0; i < sortedWfLists.size(); i ++){
			sortedWfList.addAll(sortedWfLists.get(i));
		}

		finalRet.add(sortedWfList);
		// List of sorted workface
		ArrayList<Integer> sortedWfSeq = new ArrayList<Integer>();
		sortedWfSeq.addAll(sortedWfList);
		ArrayList<WorkfaceProcessUnit> wfProcList = new ArrayList<WorkfaceProcessUnit>();
		for(int i = 0; i < numOfWorkfaces; i ++){
			WorkfaceProcessUnit wpu = new WorkfaceProcessUnit(i);
			wfProcList.add(wpu);
		}
		
		// Store time interval list
		ArrayList<ArrayList<Double>> timeIntervalList = new ArrayList<ArrayList<Double>>();
		timeIntervalList = SortTool.computeMachineTimeIntervalInOneRegion(sortedWfSeq, opInfo, workload, distance, initPos);
	    long START_TIME = System.currentTimeMillis();
	    
	    // For each operating machine
	    for(int i = 0; i < opInfo.getMachineNum(); i ++){
	    	// Current machine's processing and moving time
	    	ArrayList<Double> proTime = timeIntervalList.get(2 * i);
	    	// Current machine's wait time
	    	ArrayList<Double> waitTime = timeIntervalList.get(2 * i + 1);
	    	// For each workface in sorted worface list
	    	for(int j = 0; j < sortedWfList.size(); j ++){
	    		int curWfNum = sortedWfList.get(j) - 1;
	    		double curWfMachineOpTime = proTime.get(2 * j);
	    		double curWfMachineMovTime = 0;
	    		if(j < sortedWfList.size() - 1){
	    			curWfMachineMovTime = proTime.get(2 * j + 1);
	    		}
	    		
	    		double curWfMachineWaitTime = 0;
	    		if(j < sortedWfList.size() - 1){
	    			curWfMachineWaitTime = waitTime.get(j);
	    		}
	    		
	    		// Current workface process unit
	    		WorkfaceProcessUnit wpu = wfProcList.get(curWfNum);
	    		
	    		// This is the first operating machine (or procedure on this workface)
	    		if(i == 0){
	    			if(j == 0){
	    				wpu.setStartTime(i, START_TIME);
		    			wpu.setEndTime(i, wpu.getStartTime(i) + curWfMachineOpTime);
		    			wpu.setMovTime(i, curWfMachineMovTime);
	    			}else{
	    				WorkfaceProcessUnit prevWpu = wfProcList.get(sortedWfList.get(j - 1) - 1);
	    				wpu.setStartTime(i, prevWpu.getEndTime(i) + prevWpu.getMovTime(i));
		    			wpu.setEndTime(i, wpu.getStartTime(i) + curWfMachineOpTime);
		    			wpu.setMovTime(i, curWfMachineMovTime);
	    			}
	    			
	    		}else{
	    			if(j == 0){
	    				wpu.setStartTime(i, wpu.getEndTime(i - 1));
		    			wpu.setEndTime(i, wpu.getStartTime(i) + curWfMachineOpTime);
		    			wpu.setMovTime(i, curWfMachineMovTime);
	    			}else{
	    				WorkfaceProcessUnit prevWpu = wfProcList.get(sortedWfList.get(j - 1) - 1);
	    				double endTime1 = wpu.getEndTime(i - 1);
	    				double endTime2 = prevWpu.getEndTime(i) + prevWpu.getMovTime(i);
	    				if(endTime1 > endTime2){
	    					wpu.setStartTime(i, endTime1);
	    				}else{
	    					wpu.setStartTime(i, endTime2);
	    				}
	    				wpu.setEndTime(i, wpu.getStartTime(i) + curWfMachineOpTime);
		    			wpu.setMovTime(i, curWfMachineMovTime);
	    			}
	    		}
	    		if(i == opInfo.getMachineNum() - 1){
	    			wpu.setTotalEndTime(wpu.getEndTime(i));
	    		}
	    	}// end for - workface
	    }// end for - machine
	    
	    WfProcUnitStartComparator startCom = new WfProcUnitStartComparator();
		Collections.sort(wfProcList, startCom);
		
		if(finalProcList != null){
			finalProcList.add(wfProcList);
		}
		
		File f = new File("SCHEDULE_BY_PRIORITY.txt");
		FileWriter fw = new FileWriter(f);
		StringBuilder sb = null;
		for(WorkfaceProcessUnit wfpu: wfProcList){
			sb = new StringBuilder();
			sb.append("<<<<Workface ID: " + wfpu.getWfId() + "\n");
			ArrayList<WorkfaceProcessUnit.WorkfaceProcedureUnit> procedureList = wfpu.getWfProcList();
			for(WorkfaceProcessUnit.WorkfaceProcedureUnit procedure: procedureList){
				sb.append("\tOperating Machine ID: " + procedure.getMachineId() + " Start time: " + procedure.getStartTime() + " End time: " + 
							procedure.getEndTime() + " Moving time: " + procedure.getMovTime() + "\n");
			}
			sb.append("\n");
			fw.write(sb.toString());
		}
		
		fw.close();
		return finalRet;
	}
	
	/**
	 * This method is used when there are more than one set of machines (say, two sets or three sets).
	 * @param numOfSet The number of sets of operating machines.
	 * @param numOfWorkfaces The total number of workfaces.
	 * @param opInfo Machines' operating information.
	 * @param workload All operating machines' workloads on all workfaces.
	 * @param distance1 Workface distance object which stores distance in record manner.
	 * @param originCall true if this method is called from UI; false otherwise.
	 * @return A list of groups of sorted workfaces.
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static ArrayList<ArrayList<Integer>> getClustersOfWorkfacesSortByMore(int numOfSet, int numOfWorkfaces,  MachineOpInfo opInfo, WorkfaceWorkload workload, WorkfaceDistance distance, MachineInitialPosition initPos, ArrayList<ArrayList<WorkfaceProcessUnit>> wfProcListArray, boolean originCall) throws IOException, URISyntaxException{
		
		ArrayList<ArrayList<Integer>> finalRet = new ArrayList<ArrayList<Integer>>();
		/* Load a dataset */
//		Dataset data = FileHandler.loadDataset(new File(fileName), numOfWorkfaces, delimiter);
		
		// Get groups of workfaces by using new grouping method.
		ArrayList<ArrayList<Integer>> groups = null;
		if(numOfSet == 2)
			groups = get2GroupsOfWorkfaces(distance);
		else
			groups = get3GroupsOfWorkfaces(distance);
		
		if(groups == null){
			System.out.println("exit since groups == null");
			System.exit(0);
		}else{
			
			// Print out groups data
			System.out.println("Print out groups data:");
			for(int i = 0; i < groups.size(); i ++){
				for(int j = 0; j < groups.get(i).size(); j ++){
					System.out.print(groups.get(i).get(j) + " ");
				}
				System.out.println();
			}
			
			
			// For each workface group. Each workface group is for one set of operating machines.
			for(int i = 0; i < groups.size(); i ++){
				finalRet.add(sortWorkfacesByGroupOf4(groups.get(i), distance, opInfo, workload, initPos));
			}// end for group
			
			// Start balancing out the operating time.
			// Find the two workface lists with max and min operating time.
			int min = 0, max = 0;
			double maxd = Double.MIN_VALUE, mind = Double.MAX_VALUE;
			ArrayList<Integer> minL = null, maxL = null;
			for(int i = 0; i < finalRet.size(); i ++){
				ArrayList<Integer> tmpL = finalRet.get(i);
				double tmpTime = SortTool.computeOperatingTimeOfWorkfaceList(SortTool.computeMachineTimeIntervalInOneRegion(tmpL, opInfo, workload, distance, initPos));
				if(tmpTime > maxd){
					maxd = tmpTime;
					maxL = tmpL;
					max = i;
				}
				
				if(tmpTime < mind){
					mind = tmpTime;
					minL = tmpL;
					min = i;
				}			
			}
			
			ArrayList<Integer> tmpListInFinal = null;
			for(int i = 0; i < finalRet.size(); i ++){
				if(i != min && i != max){
					tmpListInFinal = finalRet.get(i);
				}
			}
			finalRet.clear();
			if(tmpListInFinal != null)
				finalRet.add(tmpListInFinal);
			
			while(maxd > mind){
				// Find the operating machine in max list which is closed to min list.
				int maxMachine = 0; 
				double tmpDisFinal = Double.MAX_VALUE;
				for(int maxi = 0; maxi < maxL.size(); maxi ++){
					double tmpDis = Double.MAX_VALUE;
					for(int mini = 0; mini < minL.size(); mini ++){
						if(distance.getDistBetweenTwoWorkfaces(minL.get(mini) - 1, maxL.get(maxi) - 1) < tmpDis){
							tmpDis = distance.getDistBetweenTwoWorkfaces(minL.get(mini) - 1, maxL.get(maxi) - 1);
						}
					}
					if(tmpDis < tmpDisFinal){
						tmpDisFinal = tmpDis;
						maxMachine = maxi;
					}
				}
				
				minL.add(maxL.remove(maxMachine));
				maxd = SortTool.computeOperatingTimeOfWorkfaceList(SortTool.computeMachineTimeIntervalInOneRegion(maxL, opInfo, workload, distance, initPos));
				mind = SortTool.computeOperatingTimeOfWorkfaceList(SortTool.computeMachineTimeIntervalInOneRegion(minL, opInfo, workload, distance, initPos));
			}
			
			finalRet.add(minL);
			finalRet.add(maxL);
		}// end else
		
		ArrayList<Integer> finalRetList = new ArrayList<Integer>();
		ArrayList<WorkfaceProcessUnit> finalWfProcList = new ArrayList<WorkfaceProcessUnit>();
		ArrayList<WorkfaceProcessUnit> wfProcList = new ArrayList<WorkfaceProcessUnit>();
		for(int i = 0; i < numOfWorkfaces; i ++){
			WorkfaceProcessUnit wpu = new WorkfaceProcessUnit(i);
			wfProcList.add(wpu);
		}
		
		long START_TIME = System.currentTimeMillis();
		ArrayList<WorkfaceProcessUnit> tmpWfProcList = new ArrayList<WorkfaceProcessUnit>(); 
		for(int k = 0 ;k < finalRet.size(); k ++){
			tmpWfProcList.clear();
			finalRetList.clear();
			finalRetList.addAll(finalRet.get(k));
			for(int j = 0; j < finalRetList.size(); j ++){
				for(WorkfaceProcessUnit wpu: wfProcList){
					if(wpu.getWfId() == finalRetList.get(j) - 1){
						tmpWfProcList.add(wpu);
					}
				}
			}
			
			// Store time interval list
			ArrayList<ArrayList<Double>> timeIntervalList = new ArrayList<ArrayList<Double>>();
			timeIntervalList = SortTool.computeMachineTimeIntervalInOneRegion(finalRetList, opInfo, workload, distance, initPos);
			System.out.println("time interval size:" + timeIntervalList.size());
			
		    // For each operating machine
		    for(int i = 0; i < opInfo.getMachineNum(); i ++){
		    	// Current machine's processing and moving time
		    	ArrayList<Double> proTime = timeIntervalList.get(2 * i);
		    	// Current machine's wait time
		    	ArrayList<Double> waitTime = timeIntervalList.get(2 * i + 1);
		    	
		    	// For each workface in sorted worface list
		    	for(int j = 0; j < finalRetList.size(); j ++){
		    		int curWfNum = finalRetList.get(j) - 1;
		    		double curWfMachineOpTime = proTime.get(2 * j);
		    		double curWfMachineMovTime = 0;
		    		if(j < finalRetList.size() - 1){
		    			curWfMachineMovTime = proTime.get(2 * j + 1);
		    		}
		    		
		    		double curWfMachineWaitTime = 0;
		    		if(j < finalRetList.size() - 1){
		    			curWfMachineWaitTime = waitTime.get(j);
		    		}
		    		
		    		// Current workface process unit
		    		WorkfaceProcessUnit wpu = null;
		    		for(WorkfaceProcessUnit tmpWpu: tmpWfProcList){
		    			if(tmpWpu.getWfId() == curWfNum){
		    				wpu = tmpWpu;
		    				break;
		    			}
		    		}
		    		
		    		// This is the first operating machine (or procedure on this workface)
		    		if(i == 0){
		    			if(j == 0){
		    				wpu.setStartTime(i, START_TIME);
			    			wpu.setEndTime(i, wpu.getStartTime(i) + curWfMachineOpTime);
			    			wpu.setMovTime(i, curWfMachineMovTime);
		    			}else{
		    				WorkfaceProcessUnit prevWpu = wfProcList.get(finalRetList.get(j - 1) - 1);
		    				wpu.setStartTime(i, prevWpu.getEndTime(i) + prevWpu.getMovTime(i));
			    			wpu.setEndTime(i, wpu.getStartTime(i) + curWfMachineOpTime);
			    			wpu.setMovTime(i, curWfMachineMovTime);
		    			}
		    		}else{
		    			if(j == 0){
		    				wpu.setStartTime(i, wpu.getEndTime(i - 1));
			    			wpu.setEndTime(i, wpu.getStartTime(i) + curWfMachineOpTime);
			    			wpu.setMovTime(i, curWfMachineMovTime);
		    			}else{
		    				WorkfaceProcessUnit prevWpu = wfProcList.get(finalRetList.get(j - 1) - 1);
		    				double endTime1 = wpu.getEndTime(i - 1);
		    				double endTime2 = prevWpu.getEndTime(i) + prevWpu.getMovTime(i);
		    				if(endTime1 > endTime2){
		    					wpu.setStartTime(i, endTime1);
		    				}else{
		    					wpu.setStartTime(i, endTime2);
		    				}
		    				wpu.setEndTime(i, wpu.getStartTime(i) + curWfMachineOpTime);
			    			wpu.setMovTime(i, curWfMachineMovTime);
		    			}
		    		}
		    		if(i == opInfo.getMachineNum() - 1){
		    			wpu.setTotalEndTime(wpu.getEndTime(i));
		    		}
		    	}// end for - workface
		    }// end for - machine
		    
		    WfProcUnitStartComparator startCom = new WfProcUnitStartComparator();
			Collections.sort(tmpWfProcList, startCom);
			finalWfProcList.addAll(tmpWfProcList);
		}
		
		if(wfProcListArray != null){
			wfProcListArray.add(finalWfProcList);
		}
		
		if(originCall){
			// Persist ordered workface data to disk.
			File f = new File("SCHEDULE_BY_SORT_2_3.txt");
			FileWriter fw = new FileWriter(f);
			StringBuilder sb = null;
			for(WorkfaceProcessUnit wfpu: wfProcList){
				sb = new StringBuilder();
				sb.append("<<<<Workface ID: " + wfpu.getWfId() + "\n");
				ArrayList<WorkfaceProcessUnit.WorkfaceProcedureUnit> procedureList = wfpu.getWfProcList();
				for(WorkfaceProcessUnit.WorkfaceProcedureUnit procedure: procedureList){
					sb.append("\tOperating Machine ID: " + procedure.getMachineId() + " Start time: " + procedure.getStartTime() + " End time: " + 
								procedure.getEndTime() + " Moving time: " + procedure.getMovTime() + "\n");
				}
				sb.append("\n");
				fw.write(sb.toString());
			}
			fw.close();
		}
		System.out.println("final size: " + finalRet.size());
		return finalRet;
	}
	
	/**
	 * Sort all workfaces in groups where each group has 4 workfaces.
	 * @param wfGroup A group of all unsorted workfaces. 
	 * @param distance Workface distances.
	 * @param opInfo Operating machine information.
	 * @param workload Workface workloads.
	 * @param initPos Operating machine initial positions.
	 * @return Sorted workfaces.
	 */
	public static ArrayList<Integer> sortWorkfacesByGroupOf4(ArrayList<Integer> wfGroup, WorkfaceDistance distance, MachineOpInfo opInfo, WorkfaceWorkload workload, MachineInitialPosition initPos){
		ArrayList<ArrayList<Integer>> tmpGroups = new ArrayList<ArrayList<Integer>>(); 
		System.out.println("wfGroup size: " + wfGroup.size());
		tmpGroups = getGroupsby4Wf(wfGroup, distance);
		
		// Set each 0 indexed workface value plus one.
		for(int m = 0; m < tmpGroups.size(); m ++){
			for(int n = 0; n < tmpGroups.get(m).size(); n ++){
				tmpGroups.get(m).set(n, tmpGroups.get(m).get(n) + 1);
			}
		}
		
//		// Print out.........................
//		System.out.println("Printing out tmpGroups..............");
//		for(int i = 0; i < tmpGroups.size(); i ++){
//			for(int j = 0; j < tmpGroups.get(i).size(); j ++){
//				System.out.print(tmpGroups.get(i).get(j) + " ");
//			}
//			System.out.println();
//		}
//		System.out.println();
//		
//		System.out.println("tmpGroups size: " + tmpGroups.size());
		ArrayList<ArrayList<Integer>> tmpSortRet = null;		
		if(tmpGroups.size() > 1){
			ArrayList<ArrayList<Integer>> firstGroupOfWf = new  ArrayList<ArrayList<Integer>>();
			firstGroupOfWf.add(tmpGroups.get(tmpGroups.size() - 1));
			/** Sort workfaces by using time(operating time and moving time) matrix*/
//			tmpSortRet = SortTool.sortWorkfacesByMatrix(distance, firstGroupOfWf, opInfo, workload, initPos);
			/** Sort workfaces by using traditional method*/
			tmpSortRet = SortTool.sortWorkfacesByTradition(firstGroupOfWf, opInfo, workload, distance, initPos);
			
//			// Print out.........................
//			System.out.println("Printing out tmpSortRet..............");
//			for(int i = 0; i < tmpSortRet.get(0).size(); i ++){
//				System.out.print(tmpSortRet.get(0).get(i) + " ");
//			}
//			System.out.println();
			
			for(int j = tmpGroups.size() - 2; j >= 0; j --){
				firstGroupOfWf = new  ArrayList<ArrayList<Integer>>();
				firstGroupOfWf.add(tmpSortRet.get(0));
				for(int k = 0; k < tmpGroups.get(j).size(); k ++){
					ArrayList<Integer> tmp = new ArrayList<Integer>();
					tmp.add(tmpGroups.get(j).get(k));
					firstGroupOfWf.add(tmp);
				}
				tmpSortRet = SortTool.sortGroups_new(firstGroupOfWf, opInfo, workload, distance, initPos);
				ArrayList<Integer> tmp = new ArrayList<Integer>();
				for(int m = 0; m < tmpSortRet.size(); m ++){
					for(int n = 0; n < tmpSortRet.get(m).size(); n++){
						tmp.add(tmpSortRet.get(m).get(n));
					}
				}
				tmpSortRet = new ArrayList<ArrayList<Integer>>();
				tmpSortRet.add(tmp);
			}
		}
		// there is only one group
		else{
			/** Sort workfaces by using time(operating time and moving time) matrix*/
//			tmpSortRet = SortTool.sortWorkfacesByMatrix(distance, tmpGroups, opInfo, workload, initPos);
			/** Sort workfaces by using traditional method*/
			tmpSortRet = SortTool.sortWorkfacesByTradition(tmpGroups, opInfo, workload, distance, initPos);
		}
		
		ArrayList<Integer> tmpFinalRet = new ArrayList<Integer>();
		for(int j = 0; j <tmpSortRet.size(); j ++){
			for(int k = 0; k < tmpSortRet.get(j).size(); k ++){
				tmpFinalRet.add(tmpSortRet.get(j).get(k));
			}
		}

		// Print out sorted groups:
//		System.out.println("Cur final sort list:");
//		for(int j = 0; j < tmpFinalRet.size(); j ++){
//			System.out.print(tmpFinalRet.get(j) + " ");
//		}
//		System.out.println();
		return  tmpFinalRet;
	}
	
	/**
	 * Sort all workfaces based on distances between all workfaces.
	 * @param numOfWorkfaces The total number of workfaces.
	 * @param opInfo Operating machines' operating information(operating speed and moving speed).
	 * @param workload All workloads of all operating machines on all workfaces.
	 * @param distance1 Distance values between all workfaces.
	 * @param initPos Initial positions of all operating machines.
	 * @param finalWfProcList A list of workface process units for LHD usage.
	 * @return A list of groups of sorted workfaces.
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static ArrayList<ArrayList<Integer>> getClustersOfWorkfacesSortByOne(int numOfWorkfaces, MachineOpInfo opInfo, WorkfaceWorkload workload, WorkfaceDistance distance, MachineInitialPosition initPos, ArrayList<ArrayList<WorkfaceProcessUnit>> finalWfProcList) throws IOException, URISyntaxException{
		ArrayList<ArrayList<Integer>> finalRet = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> groups = new ArrayList<ArrayList<Integer>>();
		groups.add(new ArrayList<Integer>());
		for(int i = 0; i < distance.getNumOfWorkface(); i ++){
			groups.get(0).add(i);
		}

		// For each workface group. Each workface group is for one set of operating machines.
		for(int i = 0; i < groups.size(); i ++){
			finalRet.add(sortWorkfacesByGroupOf4(groups.get(i), distance, opInfo, workload, initPos));
		}// end for group
		
		// Start to compute the start and end time for each workface.
		ArrayList<WorkfaceProcessUnit> wfProcList = new ArrayList<WorkfaceProcessUnit>();
		for(int i = 0; i < numOfWorkfaces; i ++){
			WorkfaceProcessUnit wpu = new WorkfaceProcessUnit(i);
			wfProcList.add(wpu);
		}
		
		// Store time interval list
		ArrayList<ArrayList<Double>> timeIntervalList = new ArrayList<ArrayList<Double>>();
		timeIntervalList = SortTool.computeMachineTimeIntervalInOneRegion(finalRet.get(0), opInfo, workload, distance, initPos);
//		System.out.println("time interval size:" + timeIntervalList.size());
	    long START_TIME = System.currentTimeMillis();
	    
	    // For each operating machine
	    for(int i = 0; i < opInfo.getMachineNum(); i ++){
	    	// Current machine's processing and moving time
	    	ArrayList<Double> proTime = timeIntervalList.get(2 * i);
	    	// Current machine's wait time
	    	ArrayList<Double> waitTime = timeIntervalList.get(2 * i + 1);
	    	
	    	// For each workface in sorted worface list
	    	for(int j = 0; j < finalRet.get(0).size(); j ++){
	    		int curWfNum = finalRet.get(0).get(j) - 1;
	    		double curWfMachineOpTime = proTime.get(2 * j);
	    		double curWfMachineMovTime = 0;
	    		if(j < finalRet.get(0).size() - 1){
	    			curWfMachineMovTime = proTime.get(2 * j + 1);
	    		}	    		
	    		double curWfMachineWaitTime = 0;
	    		if(j < finalRet.get(0).size() - 1){
	    			curWfMachineWaitTime = waitTime.get(j);
	    		}	    		
	    		// Current workface process unit
	    		WorkfaceProcessUnit wpu = wfProcList.get(curWfNum);	    		
	    		// This is the first operating machine (or procedure on this workface)
	    		if(i == 0){
	    			if(j == 0){
	    				wpu.setStartTime(i, START_TIME);
		    			wpu.setEndTime(i, wpu.getStartTime(i) + curWfMachineOpTime);
		    			wpu.setMovTime(i, curWfMachineMovTime);
	    			}else{
	    				WorkfaceProcessUnit prevWpu = wfProcList.get(finalRet.get(0).get(j - 1) - 1);
	    				wpu.setStartTime(i, prevWpu.getEndTime(i) + prevWpu.getMovTime(i));
		    			wpu.setEndTime(i, wpu.getStartTime(i) + curWfMachineOpTime);
		    			wpu.setMovTime(i, curWfMachineMovTime);
	    			}
	    			
	    		}else{
	    			if(j == 0){
	    				wpu.setStartTime(i, wpu.getEndTime(i - 1));
		    			wpu.setEndTime(i, wpu.getStartTime(i) + curWfMachineOpTime);
		    			wpu.setMovTime(i, curWfMachineMovTime);
	    			}else{
	    				WorkfaceProcessUnit prevWpu = wfProcList.get(finalRet.get(0).get(j - 1) - 1);
	    				double endTime1 = wpu.getEndTime(i - 1);
	    				double endTime2 = prevWpu.getEndTime(i) + prevWpu.getMovTime(i);
	    				if(endTime1 > endTime2){
	    					wpu.setStartTime(i, endTime1);
	    				}else{
	    					wpu.setStartTime(i, endTime2);
	    				}
	    				wpu.setEndTime(i, wpu.getStartTime(i) + curWfMachineOpTime);
		    			wpu.setMovTime(i, curWfMachineMovTime);
	    			}
	    		}
	    		if(i == opInfo.getMachineNum() - 1){
	    			wpu.setTotalEndTime(wpu.getEndTime(i));
	    		}
	    	}// end for - workface
	    }// end for - machine
		
		WfProcUnitStartComparator startCom = new WfProcUnitStartComparator();
		Collections.sort(wfProcList, startCom);
		if(finalWfProcList != null){
			finalWfProcList.add(wfProcList);
		}
		
		// Persist ordered workface data to disk.
		File f = new File("SCHEDULE_BY_SORT_1.txt");
		FileWriter fw = new FileWriter(f);
		StringBuilder sb = null;
		for(WorkfaceProcessUnit wfpu: wfProcList){
			sb = new StringBuilder();
			sb.append("<<<<Workface ID: " + wfpu.getWfId() + "\n");
			ArrayList<WorkfaceProcessUnit.WorkfaceProcedureUnit> procedureList = wfpu.getWfProcList();
			for(WorkfaceProcessUnit.WorkfaceProcedureUnit procedure: procedureList){
				sb.append("\tOperating Machine ID: " + procedure.getMachineId() + " Start time: " + procedure.getStartTime() + " End time: " + 
							procedure.getEndTime() + " Moving time: " + procedure.getMovTime() + "\n");
			}
			sb.append("\n");
			fw.write(sb.toString());
		}
		
		fw.close();
		ArrayList<ArrayList<Integer>> finalWfRet = new ArrayList<ArrayList<Integer>>();
		finalWfRet.add(finalRet.get(0));
		
		for(int i = 0; i < finalRet.size(); i ++){
			printOutVisualInfo(finalWfRet.get(i), workload, opInfo, distance);
		}
		return finalRet;
	}
	
	/**
	 * Cluster workfaces whose workface distances are stored in the <i>fileName</i> parameter.
	 * @param numOfWorkfaces The total number of workfaces.
	 * @return Groups of workfaces determined by distances between workfaces. Workfaces which are near to each other are put together.
	 * @throws IOException
	 * @throws URISyntaxException 
	 * <p>
	 * Note: this algorithm is based on the tree structure.
	 * </p>
	 */
//	@Deprecated
//	public static ArrayList<ArrayList<Integer>> getClustersOfWorkfacesSortByOneOld(int numOfWorkfaces, MachineOpInfo opInfo, WorkfaceWorkload workload, WorkfaceDistance distance1, MachineInitialPosition initPos, ArrayList<ArrayList<WorkfaceProcessUnit>> finalWfProcList) throws IOException, URISyntaxException{
//		
//		// Get original distance list
//		ArrayList<DistanceUnit> originalDistanceList = new ArrayList<DistanceUnit>();		
//		for(int row = 0; row < distance1.getNumOfWorkface(); row ++)
//			for(int col = row + 1; col < distance1.getNumOfWorkface(); col ++){
//				DistanceUnit du = new DistanceUnit();
//				du.distance = distance1.getDistBetweenTwoWorkfaces(row, col);//data.get(row).get(col);
//				du.from = row + 1; // 0 based index in data, plus 1 to be consistent with workface index in text file, 
//				du.to = col + 1;			
//				originalDistanceList.add(du);
//			}
//		
//		// Sort distance list in ascending order
//		DUComparator comparator = new DUComparator();
//		Collections.sort(originalDistanceList, comparator);
//		
//		ArrayList<ArrayList<DistanceUnit>> groupDisList = new ArrayList<ArrayList<DistanceUnit>>();
//		int start = 0, end = 0;
//		boolean startFixed = false, endFixed = false;
//		for(int i = 0; i < originalDistanceList.size() - 1; i++){
//			if(startFixed == false){
//				startFixed = true;
//				start = i;
//			}
//			
//			if(originalDistanceList.get(i).distance == originalDistanceList.get(i + 1).distance){
//				continue;
//			}else{
//				end = i;
//				endFixed = true;
//			}
//			
//			if(endFixed == true){
//				ArrayList<DistanceUnit> tmpList = new ArrayList<DistanceUnit>();
//				for(; start <= end; start ++){
//					tmpList.add(originalDistanceList.get(start));
//				}
//				groupDisList.add(tmpList);
//				startFixed = false;
//				endFixed = false;
//				start = end + 1;
//			}
//		}
//		
//		// *****************Start to create workface grouping using brackets*****************
//		StringBuilder wfSeq = new StringBuilder();
//		// Record if a workface has been processed
//		int[] workProcessed = new int[numOfWorkfaces + 1];
//		for(int i = 0; i <= numOfWorkfaces; i++){
//			workProcessed[i] = -1;
//		}
//		int[] groupOfWf = new int[numOfWorkfaces + 1];
//		for(int i = 0; i <= numOfWorkfaces; i++){
//			groupOfWf[i] = -1;
//		}
//		int groupId =-1;
//		
//		// Iterate through sorted distance list
//		// For each distance list
//		boolean isGroupingOver = false;
//		for(int i = 0; i < groupDisList.size(); i ++){
//			// For each distance unit in a specific distance list
//			for(int j = 0; j < groupDisList.get(i).size(); j ++){
//				
//				int wf1 = groupDisList.get(i).get(j).from;
//				int wf2 = groupDisList.get(i).get(j).to;
//				
//				// Both workfaces are un-processed
//				if(workProcessed[wf1] == -1 && workProcessed[wf2] == -1){
//					if(wfSeq.length() == 0){
//						wfSeq.append("(");
//						wfSeq.append(wf1);
//						wfSeq.append(",");
//						wfSeq.append(wf2);
//						wfSeq.append(")");
//					}else{
//						wfSeq.append(",(");
//						wfSeq.append(wf1);
//						wfSeq.append(",");
//						wfSeq.append(wf2);
//						wfSeq.append(")");
//					}
//					
//					workProcessed[wf1] = (int) groupDisList.get(i).get(j).distance;
//					workProcessed[wf2] = (int) groupDisList.get(i).get(j).distance;
//					
//					// Record grouping information of workfaces
//					groupId ++;
//					groupOfWf[wf1] = groupId;
//					groupOfWf[wf2] = groupId;
//					
//					// Register log info
//					StringBuilder wf12 = new StringBuilder();
//					wf12.append(Thread.currentThread().getStackTrace()[1].toString() + "\n");
//					wf12.append("WF1:" + wf1 + " WF2:" + wf2 + "\n");
//					wf12.append(wfSeq + "\n");
//					LogTool.log(LEVEL, wf12.toString());
//					
//					continue;
//				}
//				
//				// One workface has been processed, the other is un-processed
//				int proWf = 0, unProWf = 0;
//				if(workProcessed[wf1] == -1 && workProcessed[wf2] != -1){
//					proWf = wf2;
//					unProWf = wf1;
//				}
//				
//				if(workProcessed[wf1] != -1 && workProcessed[wf2] == -1){
//					proWf = wf1;
//					unProWf = wf2;
//				}
//				
//				if(proWf != 0 && unProWf != 0){
//					int distance = (int) groupDisList.get(i).get(j).distance;
//					// Insert workface on the same level (same distance)
//					if(distance == workProcessed[proWf]){
//						wfSeq.insert(getWorkfaceIndex(wfSeq, String.valueOf(proWf)), String.valueOf(unProWf) + ",");
//					}
//					// Insert workface on the different level (different distance)
//					else{
//						// Determine left bound of current workface group including 'proWf'
//						int curGroupId = groupOfWf[proWf];
//						int smallestLeft = proWf, biggestRight = proWf;
//						for(int si = 1; si <= numOfWorkfaces; si++){
//							if(groupOfWf[si] == curGroupId){
//								if(getWorkfaceIndex(wfSeq, String.valueOf(si)) < getWorkfaceIndex(wfSeq, String.valueOf(smallestLeft))){
//									smallestLeft = si;
//								}
//								
//								if(getWorkfaceIndex(wfSeq, String.valueOf(si)) > getWorkfaceIndex(wfSeq, String.valueOf(biggestRight))){
//									biggestRight = si;
//								}
//							}
//						}
//						int iStart = getWorkfaceIndex(wfSeq, String.valueOf(smallestLeft));
//						
//						// Count the number of left - brackets
//						while(iStart >= 0){
//							
//							if((wfSeq.charAt(iStart) == '(' && iStart == 0) || (wfSeq.charAt(iStart) == '(' && wfSeq.charAt(iStart - 1) == ',')){
//								break;
//							}
//							
//							iStart --;
//						}
//						
//						int iEnd = getWorkfaceIndex(wfSeq, String.valueOf(biggestRight));
//						
//						while(iEnd < wfSeq.length()){
//							if((wfSeq.charAt(iEnd) == ')' && iEnd == wfSeq.length() -1) || (wfSeq.charAt(iEnd) == ')' && wfSeq.charAt(iEnd + 1) == ',')){
//								break;
//							}								
//							
//							iEnd ++;
//						}
//						
//						wfSeq.insert(iStart, "(");
//						if((iEnd + 2) == wfSeq.length()){
//							wfSeq.append("," + unProWf + ")");
//						}else{
//							wfSeq.insert(iEnd + 2, "," + unProWf + ")");
//						}
//						
//					}
//					workProcessed[unProWf] = (int) groupDisList.get(i).get(j).distance;
//					
//					// Record grouping information of workfaces
//					groupId ++;
//					groupOfWf[unProWf] = groupId;
//					for(int tmpIndex = 1; tmpIndex <= numOfWorkfaces; tmpIndex ++){
//						if(tmpIndex != proWf && groupOfWf[tmpIndex] == groupOfWf[proWf]){
//							groupOfWf[tmpIndex] = groupOfWf[unProWf];
//						}
//					}
//					groupOfWf[proWf] = groupOfWf[unProWf];
//					continue;
//				}
//				
//				// Both are processed already
//				if(groupOfWf[wf1] == groupOfWf[wf2]){
//					continue;
//				}
//				
//				ArrayList<Integer> ret1 = getGroupStartEnd(wfSeq, String.valueOf(wf1), groupOfWf);
//				ArrayList<Integer> ret2 = getGroupStartEnd(wfSeq, String.valueOf(wf2), groupOfWf);
//				int startOf1 = ret1.get(0), endOf1 = ret1.get(1);
//				int startOf2 = ret2.get(0), endOf2 = ret2.get(1);
//				
//				
//				String subStr1 = wfSeq.substring(startOf1, endOf1 + 1);				
//				String subStr2 = wfSeq.substring(startOf2, endOf2 + 1);
//				
//				// Get sub workface sequence to decide whether subStr1 and subStr2 on the same level or not
//				boolean isChanged = false;
//				
//				for(int tmpJ = 0; tmpJ < j; tmpJ ++){
//					
//					int tmpWorkface = groupDisList.get(i).get(tmpJ).from;					
//					if(getWorkfaceIndex(new StringBuilder(subStr1), String.valueOf(tmpWorkface)) != -1){
//						isChanged = true;
//						if(subStr1.charAt(0) =='(' && subStr1.charAt(1) == '('){
//							subStr1 = wfSeq.substring(startOf1 + 1, endOf1);
//						}
//					}
//					if(isChanged == true){
//						break;
//					}
//					tmpWorkface = groupDisList.get(i).get(tmpJ).to;
//					if(getWorkfaceIndex(new StringBuilder(subStr1), String.valueOf(tmpWorkface)) != -1){
//						isChanged = true;
//						if(subStr1.charAt(0) =='(' && subStr1.charAt(1) == '('){
//							subStr1 = wfSeq.substring(startOf1 + 1, endOf1);
//						}
//					}
//					if(isChanged == true){
//						break;
//					}
//				}
//				
//				isChanged = false;
//				for(int tmpJ = 0; tmpJ < j; tmpJ ++){
//					int tmpWorkface = groupDisList.get(i).get(tmpJ).from;
//					if(getWorkfaceIndex(new StringBuilder(subStr2), String.valueOf(tmpWorkface)) != -1){
//						isChanged = true;
//						if(subStr2.charAt(0) == '(' && subStr2.charAt(1) == '('){
//							subStr2 = wfSeq.substring(startOf2 + 1, endOf2);
//						}
//					}
//					if(isChanged == true){
//						break;
//					}
//					tmpWorkface = groupDisList.get(i).get(tmpJ).to;
//					if(getWorkfaceIndex(new StringBuilder(subStr2), String.valueOf(tmpWorkface)) != -1){
//						isChanged = true;
//						if(subStr2.charAt(0) == '(' && subStr2.charAt(1) == '('){
//							subStr2 = wfSeq.substring(startOf2 + 1, endOf2);
//						}
//					}
//					if(isChanged == true){
//						break;
//					}
//				}
//				
//				// Delete useless sub workfaces
//				if(startOf1 > startOf2){
//					if(endOf1 + 1 < wfSeq.length()){
//						if(startOf1 > 0){
//							// Delete extra comma so startOf -1 instead of startOf
//							wfSeq = wfSeq.delete(startOf1 - 1, endOf1 + 1);
//						}else{
//							wfSeq = wfSeq.delete(startOf1, endOf1 + 2);
//						}
//					}
//					else{
//						if(startOf1 > 0){
//							wfSeq = wfSeq.delete(startOf1 - 1, wfSeq.length());
//						}else{
//							wfSeq = wfSeq.delete(startOf1, wfSeq.length());
//						}
//					}
//						
//					if(endOf2 + 1 < wfSeq.length()){
//						if(startOf2 > 0){
//							wfSeq = wfSeq.delete(startOf2 - 1, endOf2 + 1);
//						}else{
//							wfSeq = wfSeq.delete(startOf2, endOf2 + 2);
//						}
//					}
//					else{
//						if(startOf2 > 0){
//							wfSeq = wfSeq.delete(startOf2 - 1, wfSeq.length());
//						}else{
//							wfSeq = wfSeq.delete(startOf2, wfSeq.length());
//						}
//					}
//				}else{
//					if(endOf2 + 1 < wfSeq.length()){
//						if(startOf2 > 0){
//							wfSeq = wfSeq.delete(startOf2 - 1, endOf2 + 1);
//						}else{
//							wfSeq = wfSeq.delete(startOf2, endOf2 + 2);
//						}
//					}
//					else{
//						if(startOf2 > 0){
//							wfSeq = wfSeq.delete(startOf2 - 1, wfSeq.length());
//						}else{
//							wfSeq = wfSeq.delete(startOf2, wfSeq.length());
//						}
//					}
//					
//					if(endOf1 + 1 < wfSeq.length()){
//						if(startOf1 > 0){
//							wfSeq = wfSeq.delete(startOf1 - 1, endOf1 + 1);
//						}else{
//							wfSeq = wfSeq.delete(startOf1, endOf1 + 2);
//						}
//					}
//					else{
//						if(startOf1 > 0){
//							wfSeq = wfSeq.delete(startOf1 - 1, wfSeq.length());
//						}else{
//							wfSeq = wfSeq.delete(startOf1, wfSeq.length());
//						}
//					}
//				}
//				if(wfSeq.length() > 0){
//					wfSeq.append(",(");
//					wfSeq.append(subStr1);
//					wfSeq.append(",");
//					wfSeq.append(subStr2);
//					wfSeq.append(")");
//				}else{
//					
//					boolean isNeededOuterBrackets = needOuterBrackets(subStr1);
//					if(isNeededOuterBrackets){
//						subStr1 = "(" + subStr1 + ")";
//					}
//					
//					isNeededOuterBrackets = needOuterBrackets(subStr2);
//					if(isNeededOuterBrackets){
//						subStr2 = "(" + subStr2 + ")";
//					}
//					
//					wfSeq.append("(");
//					wfSeq.append(subStr1);
//					wfSeq.append(",");
//					wfSeq.append(subStr2);
//					wfSeq.append(")");
//					isGroupingOver = true;
//					break;
//				}
//				
//				groupId++;
//				for(int tmpIndex = 1; tmpIndex <= numOfWorkfaces; tmpIndex ++){
//					if((tmpIndex != wf1 && groupOfWf[tmpIndex] == groupOfWf[wf1]) || (tmpIndex != wf2 && groupOfWf[tmpIndex] == groupOfWf[wf2])){
//						groupOfWf[tmpIndex] = groupId;
//					}
//				}
//				groupOfWf[wf1] = groupId;
//				groupOfWf[wf2] = groupId;
//				
//			}// end for inner for
//			if(isGroupingOver == true){
//				break;
//			}
//		}// end for outer for
//		// Try to eliminate parentheses
//		wfSeq = processParenLevel(wfSeq, parenClearLevel);
//		
//		// Register log info -- print out workfaces based on distance sorting		
//		StringBuilder msgWfProcessed = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
//		msgWfProcessed.append("\n Workface Sequence based on distance sorting(processed): " + wfSeq.toString() + "\n");
//		LogTool.log(LEVEL, msgWfProcessed.toString());
//				
//		System.out.println("=======Workface Sequence based on distance sorting(processed-exclude parenthesis)========\n" + wfSeq.toString());
//		Stack totalSortedGroup = new Stack();
//		// Iterate over each char in wfSeq
//		for(int ci = 0; ci < wfSeq.toString().length();){
//			if(wfSeq.charAt(ci) == '('){				
//				//bracketStack.push(ci);
//				totalSortedGroup.push('(');
//				ci ++;				
//			}else if(wfSeq.charAt(ci) == ')'){
//				boolean isSortGroup = false;
//				ArrayList<Integer> curSortList = new ArrayList<Integer>();
//				ArrayList<ArrayList<Integer>> curSortGroup = new ArrayList<ArrayList<Integer>>();				
//				while(totalSortedGroup.empty() == false){					
//					Object tmpPopEle = totalSortedGroup.pop();
//					if(tmpPopEle instanceof Character){						
//						// left bracket is omitted
//						break;						
//					}else if(tmpPopEle instanceof Integer && isSortGroup == false){
//						curSortList.add(0, (Integer)tmpPopEle);
//					}else if(tmpPopEle instanceof Integer && isSortGroup == true){
//						ArrayList<Integer> tmpList = new ArrayList<Integer>();
//						tmpList.add((Integer)tmpPopEle);
//						curSortGroup.add(tmpList);
//					}
//					// sort group
//					else if(tmpPopEle instanceof ArrayList){						
//						isSortGroup = true;						
//						if(curSortList.size() != 0){
//							
//							ArrayList<Integer> tmpList = null;
//							for(int i = 0; i < curSortList.size(); i ++){
//								tmpList = new ArrayList<Integer>();
//								tmpList.add(curSortList.get(i));
//								curSortGroup.add(tmpList);
//							} 
//							curSortList.clear();
//						}						
//						curSortGroup.add((ArrayList<Integer>)tmpPopEle);
//					}// end for else if
//					
//				}// end for while
//				
//				// *****************Sort workface group *****************
//				ArrayList<ArrayList<Integer>> tmpSortGroupRet = null;
//				// Sort workface group
//				if(isSortGroup == true){
//
//					tmpSortGroupRet = SortTool.sortGroups_new(curSortGroup, opInfo, workload, distance1, initPos);
//					ArrayList<Integer> tmpSortedGroupRet = new ArrayList<Integer>();
//					
//					// After group is sorted, all workfaces should be in the same group
//					for(int iTmp = 0; iTmp < tmpSortGroupRet.size(); iTmp ++){
//						for(int jTmp = 0; jTmp < tmpSortGroupRet.get(iTmp).size(); jTmp ++){
//							tmpSortedGroupRet.add(tmpSortGroupRet.get(iTmp).get(jTmp));
//						}
//					}
//					totalSortedGroup.push(tmpSortedGroupRet);
//				}
//				// Sort workfaces
//				else{
//					ArrayList<ArrayList<Integer>> tmpPara = new ArrayList<ArrayList<Integer>>();
//					tmpPara.add(curSortList);
//					/** Sort workfaces by using time(operating time and moving time) matrix*/
////					tmpSortGroupRet = SortTool.sortWorkfacesByMatrix(distance1, tmpPara, opInfo, workload, initPos);
//					/** Sort workfaces by using traditional method*/
//					tmpSortGroupRet = SortTool.sortWorkfacesByTradition(tmpPara, opInfo, workload, distance1, initPos);
//					totalSortedGroup.push(tmpSortGroupRet.get(0));
//				}
//				ci ++;
//			}
//			else if(wfSeq.charAt(ci) >= '0' && wfSeq.charAt(ci) <= '9'){
//					int tmpIndex = ci + 1;
//					while(wfSeq.charAt(tmpIndex) >= '0' && wfSeq.charAt(tmpIndex) <= '9'){
//						tmpIndex ++;
//					}
//					totalSortedGroup.push(Integer.valueOf(wfSeq.subSequence(ci, tmpIndex).toString()));
//					ci = tmpIndex;
//			}else{
//				// Current character is comma, advance ci one step forward
//				ci ++;
//			}
//		}
//
//		ArrayList<Integer> finalSortedWorkfaceRet = new ArrayList<Integer>();
//		while(totalSortedGroup.empty() == false){
//			Object curTmpEle = totalSortedGroup.pop();
//			if(curTmpEle instanceof ArrayList){
//				for(int iFinal = ((ArrayList)curTmpEle).size() - 1; iFinal >= 0; iFinal --){
//					finalSortedWorkfaceRet.add(0, (Integer)(((ArrayList)curTmpEle).get(iFinal)));
//				}
//			}
//		}
//		
//		// Start to compute the start and end time for each workface.
//		ArrayList<WorkfaceProcessUnit> wfProcList = new ArrayList<WorkfaceProcessUnit>();
//		for(int i = 0; i < numOfWorkfaces; i ++){
//			WorkfaceProcessUnit wpu = new WorkfaceProcessUnit(i);
//			wfProcList.add(wpu);
//		}
//		
//		// Store time interval list
//		ArrayList<ArrayList<Double>> timeIntervalList = new ArrayList<ArrayList<Double>>();
//		timeIntervalList = SortTool.computeMachineTimeIntervalInOneRegion(finalSortedWorkfaceRet, opInfo, workload, distance1, initPos);
//		System.out.println("time interval size:" + timeIntervalList.size());
//	    long START_TIME = System.currentTimeMillis();
//	    
//	    // For each operating machine
//	    for(int i = 0; i < opInfo.getMachineNum(); i ++){
//	    	// Current machine's processing and moving time
//	    	ArrayList<Double> proTime = timeIntervalList.get(2 * i);
//	    	// Current machine's wait time
//	    	ArrayList<Double> waitTime = timeIntervalList.get(2 * i + 1);
//	    	
//	    	// For each workface in sorted worface list
//	    	for(int j = 0; j < finalSortedWorkfaceRet.size(); j ++){
//	    		int curWfNum = finalSortedWorkfaceRet.get(j) - 1;
//	    		double curWfMachineOpTime = proTime.get(2 * j);
//	    		double curWfMachineMovTime = 0;
//	    		if(j < finalSortedWorkfaceRet.size() - 1){
//	    			curWfMachineMovTime = proTime.get(2 * j + 1);
//	    		}	    		
//	    		double curWfMachineWaitTime = 0;
//	    		if(j < finalSortedWorkfaceRet.size() - 1){
//	    			curWfMachineWaitTime = waitTime.get(j);
//	    		}	    		
//	    		// Current workface process unit
//	    		WorkfaceProcessUnit wpu = wfProcList.get(curWfNum);	    		
//	    		// This is the first operating machine (or procedure on this workface)
//	    		if(i == 0){
//	    			if(j == 0){
//	    				wpu.setStartTime(i, START_TIME);
//		    			wpu.setEndTime(i, wpu.getStartTime(i) + curWfMachineOpTime);
//		    			wpu.setMovTime(i, curWfMachineMovTime);
//	    			}else{
//	    				WorkfaceProcessUnit prevWpu = wfProcList.get(finalSortedWorkfaceRet.get(j - 1) - 1);
//	    				wpu.setStartTime(i, prevWpu.getEndTime(i) + prevWpu.getMovTime(i));
//		    			wpu.setEndTime(i, wpu.getStartTime(i) + curWfMachineOpTime);
//		    			wpu.setMovTime(i, curWfMachineMovTime);
//	    			}
//	    			
//	    		}else{
//	    			if(j == 0){
//	    				wpu.setStartTime(i, wpu.getEndTime(i - 1));
//		    			wpu.setEndTime(i, wpu.getStartTime(i) + curWfMachineOpTime);
//		    			wpu.setMovTime(i, curWfMachineMovTime);
//	    			}else{
//	    				WorkfaceProcessUnit prevWpu = wfProcList.get(finalSortedWorkfaceRet.get(j - 1) - 1);
//	    				double endTime1 = wpu.getEndTime(i - 1);
//	    				double endTime2 = prevWpu.getEndTime(i) + prevWpu.getMovTime(i);
//	    				if(endTime1 > endTime2){
//	    					wpu.setStartTime(i, endTime1);
//	    				}else{
//	    					wpu.setStartTime(i, endTime2);
//	    				}
//	    				wpu.setEndTime(i, wpu.getStartTime(i) + curWfMachineOpTime);
//		    			wpu.setMovTime(i, curWfMachineMovTime);
//	    			}
//	    		}
//	    		if(i == opInfo.getMachineNum() - 1){
//	    			wpu.setTotalEndTime(wpu.getEndTime(i));
//	    		}
//	    	}// end for - workface
//	    }// end for - machine
//	    
//	    WfProcUnitStartComparator startCom = new WfProcUnitStartComparator();
//		Collections.sort(wfProcList, startCom);
//		if(finalWfProcList != null){
//			finalWfProcList.add(wfProcList);
//		}
//		
//		// Persist ordered workface data to disk.
//		File f = new File("SCHEDULE_BY_SORT_1.txt");
//		FileWriter fw = new FileWriter(f);
//		StringBuilder sb = null;
//		for(WorkfaceProcessUnit wfpu: wfProcList){
//			sb = new StringBuilder();
//			sb.append("<<<<Workface ID: " + wfpu.getWfId() + "\n");
//			ArrayList<WorkfaceProcessUnit.WorkfaceProcedureUnit> procedureList = wfpu.getWfProcList();
//			for(WorkfaceProcessUnit.WorkfaceProcedureUnit procedure: procedureList){
//				sb.append("\tOperating Machine ID: " + procedure.getMachineId() + " Start time: " + procedure.getStartTime() + " End time: " + 
//							procedure.getEndTime() + " Moving time: " + procedure.getMovTime() + "\n");
//			}
//			sb.append("\n");
//			fw.write(sb.toString());
//		}
//		
//		fw.close();
//		ArrayList<ArrayList<Integer>> finalRet = new ArrayList<ArrayList<Integer>>();
//		finalRet.add(finalSortedWorkfaceRet);
//		
//		for(int i = 0; i < finalRet.size(); i ++){
//			printOutVisualInfo(finalRet.get(i), workload, opInfo, distance1);
//		}
//		
//		return finalRet;
//	}
//	
	/**
	 * Decide if outer brackets are needed to put outside the wfList
	 * @param wfList Workface list to process
	 * @return false if no brackets are needed; otherwise true
	 */
//	private static boolean needOuterBrackets(String wfList){
//		boolean isNeeded = true;
//		Stack<ParenLevel> stack = new Stack<ParenLevel>();
//		for(int i = 0; i < wfList.length(); i ++){
//			if(wfList.charAt(i) == '('){
//				stack.push(new ParenLevel('(', 0, i));
//			}else if(wfList.charAt(i) == ')'){
//				if(i == wfList.length() - 1){
//					ParenLevel firstBracket = stack.pop();
//					if(firstBracket.index == 0){
//						isNeeded = false;
//						break;
//					}
//				}else{
//					stack.pop();
//				}
//			}
//		}
//		return isNeeded;
//	} 
	
	/**
	 * Get the starting and ending index of a group of workfaces where <i>wf</i> resides in.
	 * @param wfSeq A list of workface to be examined. 
	 * @param wf Workface to be searched.
	 * @param groupOfWf Group of unprocessed workfaces.
	 * @return the start and end workfaces of a list of workfaces surrounded by parentheses.
	 */
//	private static ArrayList<Integer> getGroupStartEnd(StringBuilder wfSeq, String wf, int[] groupOfWf){
//
//		int start = getWorkfaceIndex(wfSeq, wf), end = start;
//		int smallestLeft = start, biggestRight = end;
//		
//		for(int i = 1; i <= groupOfWf.length - 1; i++){
//			if(groupOfWf[Integer.valueOf(wf)] == groupOfWf[i]){
//				if(getWorkfaceIndex(wfSeq, String.valueOf(i)) <= start){
//					start = getWorkfaceIndex(wfSeq, String.valueOf(i));
//					smallestLeft = i;
//				}
//				if(getWorkfaceIndex(wfSeq, String.valueOf(i)) >= end){
//					end = getWorkfaceIndex(wfSeq, String.valueOf(i));
//					biggestRight = i;
//				}
//			}
//		}
//		
//		start = getWorkfaceIndex(wfSeq, String.valueOf(smallestLeft));
//		end = getWorkfaceIndex(wfSeq, String.valueOf(biggestRight));
//		while(start >= 0){
//			
//			if((wfSeq.charAt(start) == '(' && start == 0) || ((wfSeq.charAt(start) == '(') && (wfSeq.charAt(start - 1) == ','))){
//				break;
//			}
//			start --;
//		}
//		
//		while(end < wfSeq.length()){
//			if((wfSeq.charAt(end) == ')' && end == wfSeq.length() - 1) || (wfSeq.charAt(end) == ')' && wfSeq.charAt(end + 1) == ',')){
//				break;
//			}
//			end ++;
//		}
//		
//		ArrayList<Integer> indexList = new ArrayList<Integer>();
//		indexList.add(start);
//		indexList.add(end);
//		return indexList;
//	}
	
	/**
	 * Get the index of one workface in a workface sequence. Both workface sequence and workface are represented in string values.
	 * @param sb Workface sequence.
	 * @param substr Workface to be searched.
	 * @return The index of seached workface in the workface sequence.
	 */
//	private  static int getWorkfaceIndex(StringBuilder sb, String substr){
//        
//		int tmpIndex = sb.indexOf(substr);
//		if(tmpIndex == -1){
//			return -1;
//		}
//		
//		int tmp = tmpIndex + 1;
//		while(tmp < sb.length()){
//			
//			// the left neighboring character of current workface should be non-number
//			while(tmpIndex > 0 && (sb.charAt(tmpIndex - 1) >= '0' && sb.charAt(tmpIndex - 1) <= '9')){
//				tmpIndex = sb.indexOf(substr, tmpIndex + 1);
//			}
//			
//			// No substr in sb
//			if(tmpIndex == -1){
//				return -1;
//			}
//			
//			// the right neighboring character of current workface should be non-number
//			tmp = tmpIndex + 1;
//			if(tmp > 0){
//				while(sb.charAt(tmp) >= '0' && sb.charAt(tmp) <= '9'){
//					tmp ++;
//				}
//				
//				if(substr.compareTo(sb.substring(tmpIndex, tmp)) == 0){
//					// Register log info
////					String message = Thread.currentThread().getStackTrace()[1].toString() + "Workface Seq: " + sb.toString() + " Workface: " + substr + " Workface index: " + tmpIndex;   
////					LogTool.log(LEVEL, message);
//					
//					return tmpIndex;
//				}else{
//					//tmpIndex = sb.indexOf(substr, tmpIndex + 1);
//					tmpIndex = sb.indexOf(substr, tmp + 1);
//					tmp = tmpIndex + 1;
//				}
//			}
//		}
//		// Register log info
////		String message = Thread.currentThread().getStackTrace()[1].toString() + "Workface Seq: " + sb.toString() + " Workface: " + substr + " Workface index: " + tmpIndex;   
////		LogTool.log(LEVEL, message);
//		
//		return tmpIndex;
//	}

	/**
	 * Get rid of parentheses based on the <i>clearLevel</i> argument.
	 * @param workface Workfaces sorted based on distance and grouped using parentheses.
	 * @param clearLevel Level of parentheses to be eliminated.
	 * @return processed workface list grouped using parentheses. 
	 */
	public static StringBuilder processParenLevel (StringBuilder workface, Parentheses clearLevel){
		int proLoop = 0;
		if(clearLevel == Parentheses.FIRST_LEVEL)
			proLoop =1;
		else if(clearLevel == Parentheses.SECOND_LEVEL)
			proLoop = 2;
		else if(clearLevel == Parentheses.THIRD_LEVEL)
			proLoop = 3;
		else if(clearLevel == Parentheses.FORTH_LEVEL)
			proLoop = 4;
		else if(clearLevel == Parentheses.FIFTH_LEVEL)
			proLoop = 5;
		else if(clearLevel == Parentheses.NONE)
			proLoop = 0;
		
		StringBuilder tmpWfList = workface;
		while(proLoop-- > 0)
			tmpWfList = processFirstParenLevel(tmpWfList);
		return tmpWfList;
	}
	
	/**
	 * Get rid of inner-est parentheses of the current list of workfaces 
	 * @param workface A list of workfaces
	 * @return Processed list of workfaces.
	 */
	private static StringBuilder processFirstParenLevel(StringBuilder workface){
		ArrayList<ParenLevel> parenLevelList = new ArrayList<ParenLevel>(); 
		int maxLevel = -1, tmpLevel = 0;
		for(int i = 0; i < workface.length(); i ++){
			if(workface.charAt(i) == '('){
				tmpLevel ++;
				parenLevelList.add(new ParenLevel('(', tmpLevel, i));
			}else if(workface.charAt(i) == ')'){
				tmpLevel --;
				parenLevelList.add(new ParenLevel(')', tmpLevel, i));
			}
			
			if(tmpLevel > maxLevel)
				maxLevel = tmpLevel;
		}
		
		for(int i = parenLevelList.size() - 1; i >= 0; i--){
			ParenLevel tmpPL = parenLevelList.get(i);
			// Find the left parenthesis with the max level
			if(tmpPL.serialNum == maxLevel){
				// Find the matched right parenthesis
				ParenLevel tmpPL1 = parenLevelList.get(i + 1);
				workface.deleteCharAt(tmpPL1.index);
				workface.deleteCharAt(tmpPL.index);
			}
		}
		return workface;
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		
		// Determine whether to output debug info or not
//		ClusterTool.LEVEL = LogTool.LEVEL_OPEN;
//		SortTool.LEVEL = LogTool.LEVEL_OPEN;
		ClusterTool.LEVEL = LogTool.LEVEL_CLOSE;
		SortTool.LEVEL = LogTool.LEVEL_CLOSE;
		
		//***************start the grouping workface process***************		
		// Read in workface distance information
		WorkfaceDistance distance = new WorkfaceDistance(20);
		BufferedReader br = null;
		ArrayList<Double> singleWorkloadInfo = null;
		try{
			String curLine = null;
			String path = "Input_Data\\Workface_Distance.txt";
			br = new BufferedReader(new FileReader(path));			
			while((curLine = br.readLine()) != null){				
				String[] distRet = curLine.split("\t");
				singleWorkloadInfo = new ArrayList<Double>(); 
				for(int i = 0; i < distRet.length; i ++){
					singleWorkloadInfo.add(Double.valueOf(distRet[i]));
				}
				distance.addDistance(singleWorkloadInfo);
			}				
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(br != null){
				br.close();
			}
		}
		
		// Read in machine operation information
		MachineOpInfo opInfo = new MachineOpInfo(5); // there are in total 6 machines
		ArrayList<Double> singleOpInfo = null;
		try{
			String curLine = null;
			br = new BufferedReader(new FileReader("Input_Data\\Machine_Operating_Info.txt"));
			while((curLine = br.readLine()) != null){
				
				String[] opRet = curLine.split("\t");
				singleOpInfo = new ArrayList<Double>();
				singleOpInfo.add(Double.valueOf(opRet[1]));
				singleOpInfo.add(Double.valueOf(opRet[2]));
				opInfo.addMachineOpInfo(singleOpInfo);
				singleOpInfo = null;
			}			
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(br != null)
				br.close();
		}
		
		// Read in workface workload information
		WorkfaceWorkload workload = new WorkfaceWorkload(6,20);
		ArrayList<Double> singleWorkload = null;
		try{
			String curLine = null;
			br = new BufferedReader(new FileReader("Input_Data\\Workface_Workload.txt"));
			while((curLine = br.readLine()) != null){
				String[] workloadRet = curLine.split("\t");
				singleWorkload = new ArrayList<Double>();
				for(int i = 0; i < 20; i ++){
					singleWorkload.add(Double.valueOf(workloadRet[i]));
				}
				workload.addMachineWorkload(singleWorkload);
				singleWorkload = null;
			}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(br != null)
				br.close();
		}		
		
		// Read in machine initial position values
		MachineInitialPosition machineInitPos = new MachineInitialPosition();
		br = null;
		try{
			String curLine = null;
			br = new BufferedReader(new FileReader("Input_Data\\Machine_Initial_Location.txt"));
			while((curLine = br.readLine()) != null){			
				String[] deArr = curLine.split("\t");
				machineInitPos.addIniPosUnit(Integer.valueOf(deArr[0]), Integer.valueOf(deArr[1]));
			}			
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(br != null){
				br.close();
			}
		}

//		System.out.println("Compute workface cluster by priority?[YES or NO]");
		Scanner s = new Scanner(System.in);
//		String ret = s.nextLine();
		String ret = "no";
		if(ret.equalsIgnoreCase("yes")){
			WorkfacePriority wfPriority = new WorkfacePriority();
			BufferedReader bReader = new BufferedReader(new FileReader("Input_Data\\Workface_Priority.txt"));
			try{
				String curLine = null;
				while((curLine = bReader.readLine()) != null){
					String[] strArr = curLine.split("\t");
					WorkfacePriority.WorkfacePrioUnit newUnit = new WorkfacePriority.WorkfacePrioUnit(Integer.valueOf(strArr[0]), Integer.valueOf(strArr[1]));
					wfPriority.addWfPrioUnit(newUnit);
				}
			}catch(IOException e){
				System.out.println(e.getMessage());
			}
			
			ArrayList<ArrayList<Integer>> finalRetList = getClustersOfWorkfacesByPriority(20,wfPriority, opInfo, workload, distance, machineInitPos, null);
			for(int i = 0; i < finalRetList.size(); i ++){
				printOutVisualInfo(finalRetList.get(i), workload, opInfo, distance);
			}
//			s.close();
		    System.exit(0);
			
		}else{
//			System.out.println("Compute workface cluster by sharing machines?[YES or NO]");
//			s = new Scanner(System.in);
//			ret = s.nextLine();
			ret = "no";
			// Finish workload by sharing operating machines.
			if(ret.equalsIgnoreCase("yes")){
				System.out.println("Number of operating machine set?");
				int numbOfMachineSet = Integer.valueOf(s.nextLine());
				String curLine = null;
				br = new BufferedReader(new FileReader("Input_Data\\Machine_Set.txt"));
				LinkedList<String> q = new LinkedList<String>();
				int cntOfLine = 0;
				while((curLine = br.readLine()) != null){
					q.add(curLine);
					cntOfLine ++;
				}
				
				int numOfProc = cntOfLine / numbOfMachineSet;
				ShareMachineUnit shareUnit = new ShareMachineUnit();
				for(int i = 0; i < cntOfLine; i ++){
					int procId = i % numOfProc;
					String[] strArr = q.removeFirst().split("\t");
					String name = strArr[0];
					int machineNum = Integer.valueOf(strArr[1]);
					// Test
					System.out.println("ProcId: " + procId + " machineNum: " + machineNum + " machine name: " + name);
					
					shareUnit.addNewProcedureUnit(procId, machineNum, name);
				}
				//Test
				System.out.println("Share unit size: " + shareUnit.getSharedMachineList().size());
				
				getClustersOfWorkfacesBySharedMachine(opInfo, workload, distance, machineInitPos, shareUnit);
				
//				s.close();
				System.out.println("another thread to show gantt!!!");
				//System.exit(0);
			}else{
//				System.out.println("Compute workface cluster by dependancy?[YES or NO]");
//				s = new Scanner(System.in);
//				ret = s.nextLine();
				ret = "no";
				if(ret.equalsIgnoreCase("yes")){
					// Read in workface dependancy information
					WorkfaceDependancy wfDependancy = new WorkfaceDependancy();
					br = null;
					
					try{
						String curLine = null;
						br = new BufferedReader(new FileReader("Input_Data\\Workface_Dependancy.txt"));
						while((curLine = br.readLine()) != null){
							
							String[] deArr = curLine.split("\t");
							wfDependancy.addDependancyUnit(Integer.valueOf(deArr[0]), Integer.valueOf(deArr[1]));
							
						}
						
						ArrayList<ArrayList<Integer>> finalRetList = getClustersOfWorkfacesByDependancy(20, wfDependancy, opInfo, workload, distance, machineInitPos, null);
						
					}catch(IOException e){
						e.printStackTrace();
					}finally{
						if(br != null){
							br.close();
						}
					}
					
//					s.close();
					System.exit(0);
				}
				
				int isSortedOrNot = 1;
				System.out.println("Sort workfaces or not(1 for yes, 2 for self-defined sequence):");
				s = new Scanner(System.in);
				String str = s.nextLine();
				if(str.equals("1")){
					isSortedOrNot = 1;
				}else if(str.equals("2")){
					isSortedOrNot = 2;
				}else{
					System.err.println("Please choose 1 or 2.");
					System.exit(-1);
				}
				
				/** The following block of code 
				 * is to specify the parentheses level to exclude.
				 */
//				System.out.println("Choose the level of parentheses you want to exclude:\n" +
//						"	0 No parentheses excluded.\n" +
//						"	1 First level parentheses excluded.\n" +
//						"	2 Second level parentheses excluded.\n" +
//						"	3 Third level parentheses excluded.\n");
//				int level = -1;
//				level = s.nextInt();
//				switch(level){
//					case 0:
//						ClusterTool.parenClearLevel = ClusterTool.Parentheses.NONE;
//						break;
//					case 1:
//						ClusterTool.parenClearLevel = ClusterTool.Parentheses.FIRST_LEVEL;
//						break;
//					case 2:
//						ClusterTool.parenClearLevel = ClusterTool.Parentheses.SECOND_LEVEL;
//						break;
//					case 3:
//						ClusterTool.parenClearLevel = ClusterTool.Parentheses.THIRD_LEVEL;
//						break;
//					default:
//							System.out.println("Please choose a valid level number.");
//							System.exit(-1);
//				}
				System.out.println("Specify the number of machine sets(1, 2 or 3):\n");
				int numOfSet = s.nextInt();
				if(numOfSet < 0 || numOfSet > 3){
					System.err.println("Possible number of machine sets are 1, 2 or 3.");
					System.exit(-1);
				}
				s.close();

				
				/**
				 * The following block of code specifies which workface 
				 * has no workload for certain operating machines. 
				 */
				// Machine 1 and 2 has no workload on workface 5.
//				workload.setWorkloadForMachineOnCertainWf(0, 4, 0);
//				workload.setWorkloadForMachineOnCertainWf(1, 4, 0);
				
				// Machine 1 and 2 has no workload on workface 1.
//				workload.setWorkloadForMachineOnCertainWf(0, 0, 0);
//				workload.setWorkloadForMachineOnCertainWf(1, 0, 0);
				
				// Machine 1 and 2 has no workload on workface 1.
//				workload.setWorkloadForMachineOnCertainWf(0, 0, 0);
//				workload.setWorkloadForMachineOnCertainWf(1, 0, 0);
				// Machine 1 and 2 has no workload on workface 6.
//				workload.setWorkloadForMachineOnCertainWf(0, 5, 0);
//				workload.setWorkloadForMachineOnCertainWf(1, 5, 0);

				/** Start cluster & sort workface */
				ArrayList<ArrayList<Integer>> dss = null;
				if(isSortedOrNot == 1){
					if(numOfSet == 1){
						dss = ClusterTool.getClustersOfWorkfacesSortByOne(20, opInfo, workload, distance, machineInitPos, null);
					}else{
						dss = ClusterTool.getClustersOfWorkfacesSortByMore(numOfSet, 20, opInfo, workload, distance, machineInitPos, null, true);
					}					 
				}else{
					// Self-defined workface sequence is defined here
					if(numOfSet == 1){
						int[] arr = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
						dss = new ArrayList<ArrayList<Integer>>();
						dss.add(new ArrayList<Integer>());
						for(int i = 0; i < arr.length; i ++){
							dss.get(0).add(arr[i]);
						}
					}else if(numOfSet == 2){
						int[][] arr = {
								{1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
								{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}
						};
						dss = new ArrayList<ArrayList<Integer>>();
						dss.add(new ArrayList<Integer>());
						dss.add(new ArrayList<Integer>());
						for(int i = 0; i < arr[0].length; i ++){
							dss.get(0).add(arr[0][i]);
						}
						
						for(int i = 0; i < arr[1].length; i ++){
							dss.get(1).add(arr[1][i]);
						}
					}else if(numOfSet == 3){
						int[][] arr = {
								{1, 2, 3, 4, 5, 6},
								{7, 8, 9, 10, 11, 12, 13, 14},
								{15, 16, 17, 18, 19, 20}
						};
						dss = new ArrayList<ArrayList<Integer>>();
						dss.add(new ArrayList<Integer>());
						dss.add(new ArrayList<Integer>());
						dss.add(new ArrayList<Integer>());
						for(int i = 0; i < arr[0].length; i ++){
							dss.get(0).add(arr[0][i]);
						}
						
						for(int i = 0; i < arr[1].length; i ++){
							dss.get(1).add(arr[1][i]);
						}
						
						for(int i = 0; i < arr[2].length; i ++){
							dss.get(2).add(arr[2][i]);
						}
					}
				}

				for(int i = 0; i < dss.size(); i ++){
					printOutVisualInfo(dss.get(i), workload, opInfo, distance);
				}
			}
		}
	}/** end of method main */
	
	/**
	 * Print the sorted workfaces on command line.
	 * @param ds Sorted workfaces.
	 * @param workload All workload of all operating machines on all workfaces.
	 * @param opInfo Operating machines' operating information.
	 * @param distance Distance values between all workfaces.
	 */
	private static void printOutVisualInfo(ArrayList<Integer> ds, WorkfaceWorkload workload, MachineOpInfo opInfo, WorkfaceDistance distance){
    	
    	// ***************Start to print out the time table of machines and workfaces*******************
    	ArrayList<ArrayList<Double>> opMoveTimeTotal = new ArrayList<ArrayList<Double>>();
    	// Store @ to indicate how many workfaces have been finished.
    	int[] missWfNum = new int[opInfo.getMachineNum()];
    	
    	// For each machine, compute operating time and moving time for sorted workface sequence
    	for(int iMachine = 0; iMachine < opInfo.getMachineNum(); iMachine ++){
    		
    		ArrayList<Double> curMachineOpMoveTime = new ArrayList<Double>(); 
    		double curDist = 0.0, curWorkload = 0.0, curOpTime = 0.0, curMovTime = 0.0, curOpRate = 0.0, curMovRate = 0.0;
    		
    		curOpRate = opInfo.getCertainMachineOpInfo(iMachine).get(0);
			curMovRate = opInfo.getCertainMachineOpInfo(iMachine).get(1);
			
    		for(int iWorkface = 0; iWorkface < ds.size() - 1; iWorkface ++){
    			
    			curWorkload = workload.getWorkloadOfMachine(iMachine).get(ds.get(iWorkface) - 1);
    			int tmpi = iWorkface + 1;
    			while(workload.getWorkloadOfMachine(iMachine).get(ds.get(tmpi) - 1) == 0 && tmpi < ds.size()){
    				tmpi ++;
    			}
    			if(tmpi == ds.size()){
    				curDist = 0;
    			}else{
    				curDist = distance.getDistBetweenTwoWorkfaces(ds.get(iWorkface) - 1, ds.get(tmpi) - 1);
    			}
    			
    			
    			curOpTime = curWorkload / curOpRate;
    			curMovTime = curDist / curMovRate;
    			
    			curMachineOpMoveTime.add(curOpTime);
    			curMachineOpMoveTime.add(curMovTime);
    		}
    		// last machine only has operating time.
    		curWorkload = workload.getWorkloadOfMachine(iMachine).get(ds.get(ds.size() - 1) - 1);
    		curOpTime = curWorkload / curOpRate;
    		curMachineOpMoveTime.add(curOpTime);
    		opMoveTimeTotal.add(curMachineOpMoveTime);
    	}
    	
    	//Register log info -- Display the machine operating time and moving time in accordance with the sorted workfaces
//    	StringBuilder msgDisplayOpMovInfo = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
//    	for(int i = 0; i < opMoveTimeTotal.size(); i ++){
//    		msgDisplayOpMovInfo.append("Step " + i + " : ");
//    		for(int j = 0; j < opMoveTimeTotal.get(i).size(); j ++){
//    			msgDisplayOpMovInfo.append(opMoveTimeTotal.get(i).get(j) + " ");
//    		}
//    		msgDisplayOpMovInfo.append("\n");
//    	}
//    	msgDisplayOpMovInfo.append("\n\n");
//    	LogTool.log(LEVEL, msgDisplayOpMovInfo.toString());
    	
    	// Visualize the machine operating and moving movements
    	ArrayList<ArrayList<String>> visualList= new ArrayList<ArrayList<String>>(); 
    	for(int i = 0; i < opMoveTimeTotal.size(); i ++){
    		
    		ArrayList<String> curVisualList = new ArrayList<String>();
    		StringBuilder curSb = null;

    		// Visualize each machine
    		long numOfOp = 0, numOfMove = 0;
    		
    		//For each machine
    		for(int j = 0; j < opMoveTimeTotal.get(i).size(); j ++){
    			
    			curSb = new StringBuilder();
				// Print operating char
				if(j % 2 == 0){
					// machine on this workface has no workload
					if(opMoveTimeTotal.get(i).get(j) == 0){
						curSb.append("@");
						missWfNum[j % 2] ++;
					}else{
						numOfOp = (Math.round(opMoveTimeTotal.get(i).get(j) * 100) / 100) + 1;
//						numOfOp = (Math.round(opMoveTimeTotal.get(i).get(j)));
						
//						if((numOfOp / 2 ) > 0){
//							numOfOp /= 2;
//						}
						
						for(int k = 0; k < numOfOp; k ++){
							curSb.append(STR_OP);
						}
					}
				}
				// Print moving char
				else{
					// machine has no workload from previous workface, thus no need to move from previous workface to this one.
					if(opMoveTimeTotal.get(i).get(j - 1) != 0){
						numOfMove = (Math.round(opMoveTimeTotal.get(i).get(j) * 100) / 100) + 1;
//						numOfMove = (Math.round(opMoveTimeTotal.get(i).get(j)));
						
//						if((numOfMove / 2 ) > 0){
//							numOfMove /= 2;
//						}
						
						for(int k = 0; k < numOfMove; k ++){
							curSb.append(STR_MOVE);
						}
					}else{
						curSb.append("@");
						missWfNum[j % 2] ++;
					}
					
				}
				curVisualList.add(curSb.toString());

    		}// end inner for

    		visualList.add(curVisualList);
    	}// end outer for
    	
//    	System.out.println("==========visulaList.size for all machines==========");
//    	for(int i = 0; i < visualList.size(); i ++){
//    		System.out.println(visualList.get(i).size());
//    	}
    	
    	
    	//Register log info -- Print out raw visual operating and moving information
//    	StringBuilder msgRawOpMovInfo = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
//    	msgRawOpMovInfo.append("\nPrint out raw visual operating and moving information\n");
//    	
//    	for(int i = 0; i < visualList.size(); i ++){
//    		msgRawOpMovInfo.append("Machine " + i + " ");
//    		for(int j = 0; j < visualList.get(i).size(); j ++){
//    			msgRawOpMovInfo.append(visualList.get(i).get(j));
//    		}
//    		msgRawOpMovInfo.append("\n");
//    	}
//    	msgRawOpMovInfo.append("\n\n");
//    	LogTool.log(LEVEL, msgRawOpMovInfo.toString());
    	
    	// Add workface number in operating info
    	int maxLenOfWf = 0;
    	for(int i = 0; i < ds.size(); i++){
    		int curWfLen = String.valueOf(ds.get(i)).length();
    		if(maxLenOfWf < curWfLen)
    			maxLenOfWf = curWfLen;
    	}
    	
    	for(int i = 0; i < visualList.size(); i ++){
    		for(int j = 0; j < visualList.get(i).size(); j ++){
    			if(j % 2 ==0){
    				StringBuilder newOpInfo = new StringBuilder(visualList.get(i).get(j));
    				StringBuilder curWf = new StringBuilder(String.valueOf(ds.get(j/2)));
    				if(newOpInfo.charAt(0) == '@'){
    					// Nothing to do
    				}else{
    					if(curWf.length() == maxLenOfWf){
        					newOpInfo.insert(1, curWf.toString());
        				}else{
        					for(int k = 0; k <= maxLenOfWf - curWf.length(); k ++){
        						curWf.insert(0, '0');
        					}
        					newOpInfo.insert(1, curWf.toString());
        				}
        				
        				visualList.get(i).set(j, newOpInfo.toString());
    				}
    				
    			}// end if
    		}// end inner for
    	}// end outer for
    	
    	
    	//Prepare the graphical presentation of machine operating in each workface
    	for(int ii = 0; ii < visualList.size(); ii ++){
    		
    		// Compute previous machines' first operating process length
    		int prevTotalOpMoveLen = 0;
    		for(int prev = 0; prev < ii; prev ++){
    			if(visualList.get(prev).get(0).charAt(0) != '@')
    				prevTotalOpMoveLen += visualList.get(prev).get(0).length(); 
    		}
    		
    		// First machine has no wait time
    		if(ii == 0){
    			// Escape first machine
    		}
    		// Starting from 2nd machine, wait time might occur
    		else{
    			
    			for(int jj = 0; jj < visualList.get(ii).size(); jj ++){
    				// For first operating 
    				if(jj == 0){
    					int numOfIndentation = 0;
        				// Indentate
        				for(int kk = 0; kk < ii; kk ++){
        					if(visualList.get(kk).get(0).charAt(0) != '@')
        						numOfIndentation += visualList.get(kk).get(0).length();
        				}
        			}
    				// 
    				else{
    					// Only process operating time
    					if(jj % 2 == 0){
    						
    						// Operating and waiting length of previous machine
    						int preOpMoveLen = 0;
    						for(int kk = 0; kk <= jj; kk ++){
    							if(visualList.get(ii - 1).get(kk).charAt(0) != '@')
    								preOpMoveLen += visualList.get(ii - 1).get(kk).length();
    							else
    								preOpMoveLen += visualList.get(ii - 1).get(kk).substring(1).length();
    						}
    						for(int kk = 0; kk < ii - 1; kk ++){
    							if(visualList.get(kk).get(0).charAt(0) != '@')
    								preOpMoveLen += visualList.get(kk).get(0).length();
    						}
    						
    						// Operating and waiting length of current machine so far, jj exclusive
    						int curOpMoveLen = prevTotalOpMoveLen; 
    						for(int kk = 0; kk < jj; kk ++){
    							if(visualList.get(ii).get(kk).charAt(0) != '@')
    								curOpMoveLen += visualList.get(ii).get(kk).length();
    							else
    								curOpMoveLen += visualList.get(ii).get(kk).substring(1).length();
    						}
    						
    						// No need to wait
    						if(curOpMoveLen >= preOpMoveLen){
    							//System.out.print(visualList.get(ii).get(jj));
    						}
    						// Need to wait (preOpMoveLen - curOpMoveLen)
    						else{
    							StringBuilder tmpWaitSb = new StringBuilder();
    							for(int kk = 0; kk < (preOpMoveLen - curOpMoveLen); kk ++){
    								tmpWaitSb.append("*");
    							}
    							
    							String nextMove = visualList.get(ii).get(jj - 1);
    							visualList.get(ii).set(jj - 1, nextMove + tmpWaitSb.toString());
    						}
    					}
    				}
    			}// end for
    		}// end else
    	}
    	
    	// *****************Print out final graphical result********************
    	// Print out sorted workfaces
    	System.out.print("Sorted Workfaces: ");
		for(int i=0;i<ds.size();i++)
			System.out.print(ds.get(i) + " ");
		System.out.println();
    	
    	//Print the graphical presentation of machine operating in each workface
    	for(int ii = 0; ii < visualList.size(); ii ++){
    		
    		System.out.print("Machine " + ii + " ");
    		
    		// Compute previous machines' first operating process length
    		int prevTotalOpMoveLen = 0;
    		for(int prev = 0; prev < ii; prev ++){
    			if(visualList.get(prev).get(0).equals("@") == false)
    				prevTotalOpMoveLen += visualList.get(prev).get(0).length();
    			else{
    				prevTotalOpMoveLen += visualList.get(prev).get(0).substring(1).length();
    			}
    		}
    		
    		// First machine has no wait time
    		if(ii == 0){
    			for(int jj = 0; jj < visualList.get(ii).size(); jj ++){
    				if(visualList.get(ii).get(jj).equals("@") == false)
    					System.out.print(visualList.get(ii).get(jj));
        		}
    		}
    		// Starting from 2nd machine, wait time might occur
    		else{
    			
    			for(int jj = 0; jj < visualList.get(ii).size(); jj ++){
    				// For first operating 
    				if(jj == 0){
    					int numOfIndentation = 0;
        				// Indentate
        				for(int kk = 0; kk < ii; kk ++){
        					if(visualList.get(kk).get(0).startsWith("@") == false)
        						numOfIndentation += visualList.get(kk).get(0).length();
        					else{
        						numOfIndentation += visualList.get(kk).get(0).substring(1).length();
        					}
        				}
        				for(int kk = 0; kk < numOfIndentation; kk ++){
        					System.out.print(" ");
        				}
        				if(visualList.get(ii).get(jj).startsWith("@") == false)
        					System.out.print(visualList.get(ii).get(jj));
        				else{
        					System.out.print(visualList.get(ii).get(jj).substring(1));
        				}
        			}
    				// 
    				else{
    					if(visualList.get(ii).get(jj).startsWith("@") == false)
    						System.out.print(visualList.get(ii).get(jj));
    					else{
    						System.out.print(visualList.get(ii).get(jj).substring(1));
    					}
    				}
    			}// end for
    		}// end else
    		
    		System.out.println();
    	}
    	System.out.println("\nNOTE: \n= for operating.\n. for moving.\n* for waiting.");
	}
	private static final String STR_OP = "=";
	private static final String STR_MOVE = ".";
}
