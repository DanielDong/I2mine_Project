package geo.cluster;

import geo.core.DUComparator;
import geo.core.DistanceUnit;
import geo.core.MachineInitialPosition;
import geo.core.MachineOpInfo;
import geo.core.ShareMachineUnit;
import geo.core.WorkfaceDependancy;
import geo.core.WorkfaceDistance;
import geo.core.WorkfacePriority;
import geo.core.WorkfaceWorkload;
import geo.excel.ExcelReader;
import geo.util.LogTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
	 * @param distance
	 * @return
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
			
			System.out.println("Printing out subGroup1 & 2:");
			for(int i = 0 ;i < subGroup1.size(); i ++)
				System.out.print(subGroup1.get(i) + " ");
			System.out.println();
			for(int i = 0 ;i < subGroup2.size(); i ++)
				System.out.print(subGroup2.get(i) + " ");
			System.out.println();
			
			WorkfaceDist wd1 = get2NewWfwithMaxDist(subGroup1, distance);
			WorkfaceDist wd2 = get2NewWfwithMaxDist(subGroup2, distance);
			if(wd1.dist + wd2.dist >= sum1 + sum2){
				break;
			}else{
				maxFrom = wd1.wfid;
				maxTo = wd2.wfid;
			}
			System.out.println("maxFrom: " + maxFrom + " maxTo: " + maxTo + " sum1: " + sum1 + " sum2: " + sum2 + " wd1.dist: " + wd1.dist + "  wd2.dist: " + wd2.dist);
		}
		
		
		return groups;
	}
	/**
	 * Group workfaces based on distances between them for 3 sets of machines.
	 * Note: value indexed from 0
	 * @param distance
	 * @return
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
			
			System.out.println("Printing out subGroup1 & 2 & 3:");
			for(int i = 0 ;i < subGroup1.size(); i ++)
				System.out.print(subGroup1.get(i) + " ");
			System.out.println();
			for(int i = 0 ;i < subGroup2.size(); i ++)
				System.out.print(subGroup2.get(i) + " ");
			System.out.println();
			for(int i = 0 ;i < subGroup3.size(); i ++)
				System.out.print(subGroup3.get(i) + " ");
			System.out.println();
			
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
			System.out.println("maxFrom: " + maxFrom + " maxTo: " + maxTo + " maxMiddle: " + maxMiddle +  " sum1: " + sum1 + " sum3: " + sum3 + " wd1.dist: " + wd1.dist + "  wd3.dist: " + wd3.dist + " wd2.dist: " + wd2.dist);		
		}		
		return groups;
	}
	/**
	 * Split groups into sub-groups each with workfaces.
	 * @param groups Groups obtained from {@link get2GroupsOfWorkfaces} or {@link get3GroupsOfWorkfaces}.
	 * Note: value indexed from 0 
	 * @return
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
		while(i >= 3){
			tmpRetList = new ArrayList<Integer>();
			tmpRetList.add(wdList.get(i).wfid);
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
//		System.out.println("wdList order:");
//		for(int i = 0; i < wdList.size(); i ++){
//			System.out.print(wdList.get(i).wfid + "(" + wdList.get(i).dist + ") ");
//		}
//		System.out.println();
		return wdList.get(0);
	}
	
	public static void getClustersOfWorkfacesBySharedMachine(String fileName, int numOfWorkfaces, String delimiter, MachineOpInfo opInfo, WorkfaceWorkload workload, WorkfaceDistance distance, MachineInitialPosition initPos){
		
		
	}
	
	/**
	 * 
	 * @param fileName
	 * @param numOfWorkfaces
	 * @param delimiter
	 * @param wfDependancy
	 * @param opInfo
	 * @param workload
	 * @param distance
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static ArrayList<ArrayList<Integer>> getClustersOfWorkfaces_byDependancy(String fileName, int numOfWorkfaces, String delimiter, WorkfaceDependancy wfDependancy, MachineOpInfo opInfo, WorkfaceWorkload workload, WorkfaceDistance distance, MachineInitialPosition initPos) throws IOException, URISyntaxException{
		/* Load a dataset */
		Dataset data = FileHandler.loadDataset(new File(fileName), numOfWorkfaces, delimiter);
		Dataset[] dataSets = new DefaultDataset[1];
		dataSets[0] = data;
		
		//ArrayList<ArrayList<Integer>> finalRet= new ArrayList<ArrayList<Integer>>();
		List<WorkfaceDependancy.WorkfaceDependancyUnit> WfDependancyList = wfDependancy.getDependancyUnitList();
		ArrayList<Integer> tmpRetList = new ArrayList<Integer>();
		ArrayList<Integer> tmpList = new ArrayList<Integer>();
		Iterator<WorkfaceDependancy.WorkfaceDependancyUnit> iter = WfDependancyList.iterator();
		// Get a workface list which rely on no body.
		int cnt = 0;
		while(iter.hasNext()){
			System.out.println("in while..." + (cnt ++));
			WorkfaceDependancy.WorkfaceDependancyUnit unit = iter.next();
			System.out.println(unit.getDependancyNum() + " " + unit.getWfNum());
			if(unit.getDependancyNum() == unit.getWfNum()){
				tmpRetList.add(unit.getWfNum());
				iter.remove();
			}
			System.out.println("in while end..." + (cnt));
		}
		
		// Sort workfaces which do not have dependancy (dependent on themselves)
//		ArrayList<ArrayList<Integer>> tmpSortList = new ArrayList<ArrayList<Integer>>();
//		tmpSortList.add(tmpRetList);
		// 0-index based
		for(int i = 0; i < tmpRetList.size(); i ++){
			tmpRetList.set(i, tmpRetList.get(i) - 1);
		}
		
		// Returned workfaces' index start from 1
		tmpRetList = sortWorkfacesByGroupOf4(tmpRetList, data, distance, opInfo, workload, initPos);
		
		//tmpSortList = SortTool.sortWorkfaces_new(dataSets, tmpSortList, opInfo, workload, initPos);
		//tmpRetList.clear();
//		for(int i = 0; i < tmpSortList.size(); i ++){
//			tmpRetList.addAll(tmpSortList.get(i));
//		}
		System.out.println("dependancy list size: " + wfDependancy.getDependancyUnitList().size());
		ArrayList<Integer> finalRetList = new ArrayList<Integer>();
		finalRetList.addAll(tmpRetList);
		int count = 0;
		count = tmpRetList.size();
		
		while(count < numOfWorkfaces){
			System.out.println("count: " + count);
			for(int i = 0; i < tmpRetList.size(); i ++){
				System.out.println("tmpRetList elem: " + tmpRetList.get(i));
				if(wfDependancy.getMachineNumOfDependancy(tmpRetList.get(i)) != 0){
					tmpList.add(wfDependancy.getMachineNumOfDependancy(tmpRetList.get(i)));
				}
			}
			
			// 0-index based
//			for(int i = 0; i < tmpList.size(); i ++){
//				System.out.println("tmpList elem: " + tmpList.get(i));
//				tmpList.set(i, tmpList.get(i) - 1);
//				
//			}
//			
//			tmpRetList = sortWorkfacesByGroupOf4(tmpList, data, distance, opInfo, workload, initPos);
//			count += tmpRetList.size();
			count += tmpList.size();
			finalRetList.addAll(tmpList);
			tmpRetList = tmpList;
			tmpList = new ArrayList<Integer>();
//			tmpList.clear();
		}
		
		// For all remained workfaces which have dependancies.
//		while(!WfDependancyList.isEmpty()){
//			System.out.println("in while...");
//			tmpList.clear();
//			
//			iter = WfDependancyList.iterator();
//			while(iter.hasNext()){
//				WorkfaceDependancy.WorkfaceDependancyUnit unit = iter.next();
//				if(tmpRetList.contains(unit.getDependancyNum())){
//					tmpList.add(unit.getWfNum());
//					iter.remove();
//					System.out.println("one removed...");
//				}
//			}
//			
//			//tmpSortList.clear();
//			//tmpSortList.add(tmpList);
//			tmpList = sortWorkfacesByGroupOf4(tmpList, data, distance, opInfo, workload, initPos);
//			for(int i = 0; i < tmpList.size(); i ++){
//				tmpRetList.add(tmpList.get(i));
//			}
//		}
		
		for(int i = 0; i < finalRetList.size(); i ++){
			System.out.print(finalRetList.get(i) + " ");
		}
		
		
		
		
		return null;
	}
	
	/**
	 * @param fileName
	 * @param numOfWorkfaces
	 * @param delimiter
	 * @param wfPriority
	 * @param opInfo
	 * @param workload
	 * @param distance
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static ArrayList<ArrayList<Integer>> getClustersOfWorkfaces_byPriority(String fileName, int numOfWorkfaces, String delimiter, WorkfacePriority wfPriority, MachineOpInfo opInfo, WorkfaceWorkload workload, WorkfaceDistance distance, MachineInitialPosition initPos) throws IOException, URISyntaxException{
		ArrayList<ArrayList<Integer>> finalRet = new ArrayList<ArrayList<Integer>>();
		/* Load a dataset */
		Dataset data = FileHandler.loadDataset(new File(fileName), numOfWorkfaces, delimiter);
		ArrayList<ArrayList<Integer>> prioLists = wfPriority.getSortedWfListsByPriority();
		Dataset[] dataSets = new DefaultDataset[1];
		dataSets[0] = data;
		System.out.println("priority list size: " + prioLists.size());
		System.out.println("before sort workfaces....");
		ArrayList<ArrayList<Integer>> sortedWfLists = SortTool.sortWorkfaces_new(dataSets, prioLists, opInfo, workload, initPos);
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
		return finalRet;
	}
	
	/**
	 * This method is used when there are more than one set of machines (say, two sets or three sets).
	 * @param numOfSet The number of sets of operating machines.
	 * @param fileName File which stores the distance matrix of all operating machines.
	 * @param numOfWorkfaces The total number of workfaces.
	 * @param delimiter Item delimiter in file specified by <i>fileName</i>
	 * @param opInfo Machines' operating information.
	 * @param workload All operating machines' workloads on all workfaces.
	 * @param distance1 Workface distance object which stores distance in record manner.
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static ArrayList<ArrayList<Integer>> getClustersOfWorkfaces_zhen_new2(int numOfSet, String fileName, int numOfWorkfaces, String delimiter, MachineOpInfo opInfo, WorkfaceWorkload workload, WorkfaceDistance distance, MachineInitialPosition initPos) throws IOException, URISyntaxException{
		
		ArrayList<ArrayList<Integer>> finalRet = new ArrayList<ArrayList<Integer>>();
		/* Load a dataset */
		Dataset data = FileHandler.loadDataset(new File(fileName), numOfWorkfaces, delimiter);
		
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
				finalRet.add(sortWorkfacesByGroupOf4(groups.get(i), data, distance, opInfo, workload, initPos));
			}// end for group
			
			// Start balancing out the operating time.
			// Find the two workface lists with max and min operating time.
			int min = 0, max = 0;
			double maxd = Double.MIN_VALUE, mind = Double.MAX_VALUE;
			ArrayList<Integer> minL = null, maxL = null;
			for(int i = 0; i < finalRet.size(); i ++){
				ArrayList<Integer> tmpL = finalRet.get(i);
				double tmpTime = SortTool.computeOperatingTimeOfWorkfaceList(SortTool.computeMachineTimeIntervalInOneRegion(tmpL, opInfo, workload, distance));
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
				maxd = SortTool.computeOperatingTimeOfWorkfaceList(SortTool.computeMachineTimeIntervalInOneRegion(maxL, opInfo, workload, distance));
				mind = SortTool.computeOperatingTimeOfWorkfaceList(SortTool.computeMachineTimeIntervalInOneRegion(minL, opInfo, workload, distance));
			}
			
			finalRet.add(minL);
			finalRet.add(maxL);
		}// end else
		System.out.println("final size: " + finalRet.size());
		return finalRet;
	}
	
	/**
	 * 
	 * @param wfGroup
	 * @param data
	 * @param distance
	 * @param opInfo
	 * @param workload
	 * @param initPos
	 * @return
	 */
	private static ArrayList<Integer> sortWorkfacesByGroupOf4(ArrayList<Integer> wfGroup, Dataset data, WorkfaceDistance distance, MachineOpInfo opInfo, WorkfaceWorkload workload, MachineInitialPosition initPos){

		ArrayList<ArrayList<Integer>> tmpGroups = new ArrayList<ArrayList<Integer>>(); 
		System.out.println("wfGroup size: " + wfGroup.size());
		tmpGroups = getGroupsby4Wf(wfGroup, distance);
		
		Dataset[] dataSet = new DefaultDataset[1];
		dataSet[0] = data;
		
		// Set each 0 indexed workface value plus one.
		for(int m = 0; m < tmpGroups.size(); m ++){
			for(int n = 0; n < tmpGroups.get(m).size(); n ++){
				tmpGroups.get(m).set(n, tmpGroups.get(m).get(n) + 1);
			}
		}
		
		System.out.println("tmpGroups size: " + tmpGroups.size());
		ArrayList<ArrayList<Integer>> tmpSortRet = null;		
		if(tmpGroups.size() > 1){
			ArrayList<ArrayList<Integer>> firstGroupOfWf = new  ArrayList<ArrayList<Integer>>();
			firstGroupOfWf.add(tmpGroups.get(tmpGroups.size() - 1));
			
			tmpSortRet = SortTool.sortWorkfaces_new(dataSet, firstGroupOfWf, opInfo, workload, initPos);
			for(int j = tmpGroups.size() - 2; j >= 0; j --){
				firstGroupOfWf = new  ArrayList<ArrayList<Integer>>();
				firstGroupOfWf.add(tmpSortRet.get(0));
				for(int k = 0; k < tmpGroups.get(j).size(); k ++){
					ArrayList<Integer> tmp = new ArrayList<Integer>();
					tmp.add(tmpGroups.get(j).get(k));
					firstGroupOfWf.add(tmp);
				}
				tmpSortRet = SortTool.sortGroups_new(firstGroupOfWf, opInfo, workload, distance);
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
			tmpSortRet = SortTool.sortWorkfaces_new(dataSet, tmpGroups, opInfo, workload, initPos);
		}
		
		
		ArrayList<Integer> tmpFinalRet = new ArrayList<Integer>();
		for(int j = 0; j <tmpSortRet.size(); j ++){
			for(int k = 0; k < tmpSortRet.get(j).size(); k ++){
				tmpFinalRet.add(tmpSortRet.get(j).get(k));
			}
		}

		// Print out sorted groups:
		System.out.println("Cur final sort list:");
		for(int j = 0; j < tmpFinalRet.size(); j ++){
			System.out.print(tmpFinalRet.get(j) + " ");
		}
		System.out.println();
		
		return  tmpFinalRet;
		
	}
	
	/**
	 * TO be finished.
	 * @param fileName
	 * @param numOfWorkfaces
	 * @param delimiter
	 * @param opInfo
	 * @param workload
	 * @param distance1
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Deprecated
	public static ArrayList<Integer> getClustersOfWorkfaces_zhen_new1(String fileName, int numOfWorkfaces, String delimiter, MachineOpInfo opInfo, WorkfaceWorkload workload, WorkfaceDistance distance1) throws IOException, URISyntaxException{
		
		/* Load a dataset */
		Dataset data = FileHandler.loadDataset(new File(fileName), numOfWorkfaces, delimiter);
		// Get original distance list
		ArrayList<DistanceUnit> originalDistanceList = new ArrayList<DistanceUnit>();
		for(int row = 0; row < data.size(); row ++){
			for(int col = row + 1; col < data.get(row).size(); col ++){
				 
				DistanceUnit du = new DistanceUnit();
				du.distance = data.get(row).get(col);
				du.from = row + 1; // 0 based index in data, plus 1 to be consistent with workface index in text file, 
				du.to = col + 1;
				
				originalDistanceList.add(du);
			}
		}
		
		// Sort distance list in ascending order
		DUComparator comparator = new DUComparator();
		Collections.sort(originalDistanceList, comparator);
		
		for(int i = 0; i < originalDistanceList.size(); i ++){
			System.out.print(originalDistanceList.get(i).distance + " ");
		}
		return null;
	}
	
	/**
	 * Cluster workfaces whose workface distances are stored in the <i>fileName</i> parameter.
	 * @param fileName Text file which stores workface distance matrix.
	 * @param numOfWorkfaces The total number of workfaces.
	 * @param delimiter The delimiter between two consecutive distance cells.
	 * @return Groups of workfaces determined by distances between workfaces. Workfaces which are near to each other are put together.
	 * @throws IOException
	 * @throws URISyntaxException 
	 * <p>
	 * Note: this algorithm is based on the tree structure.
	 * </p>
	 */
	@Deprecated
	public static ArrayList<ArrayList<Integer>> getClustersOfWorkfaces_zhen_new(String fileName, int numOfWorkfaces, String delimiter, MachineOpInfo opInfo, WorkfaceWorkload workload, WorkfaceDistance distance1, MachineInitialPosition initPos) throws IOException, URISyntaxException{
		
	    /* Load a dataset */
        //Dataset data = FileHandler.loadDataset(new File("workphase.txt"), 5, "\t");
		Dataset data = FileHandler.loadDataset(new File(fileName), numOfWorkfaces, delimiter);
		
		// Register log info
//		StringBuilder msgDistMatrix = new StringBuilder();
//		msgDistMatrix.append(Thread.currentThread().getStackTrace()[1].toString() + "\n=====Worface Distance Matrix======\n");
//		for(int i = 0; i < data.size(); i ++){
//			msgDistMatrix.append(data.get(i).toString() + "\n");
//		}
//		LogTool.log(LEVEL, msgDistMatrix.toString());
		
		// Get original distance list
		ArrayList<DistanceUnit> originalDistanceList = new ArrayList<DistanceUnit>();
		for(int row = 0; row < data.size(); row ++){
			for(int col = row + 1; col < data.get(row).size(); col ++){
				 
				DistanceUnit du = new DistanceUnit();
				du.distance = data.get(row).get(col);
				du.from = row + 1; // 0 based index in data, plus 1 to be consistent with workface index in text file, 
				du.to = col + 1;
				
				originalDistanceList.add(du);
			}
		}
		
		// Sort distance list in ascending order
		DUComparator comparator = new DUComparator();
		Collections.sort(originalDistanceList, comparator);
		
		// Register log info
//		StringBuilder msgSortedDistList = new StringBuilder();
//		msgSortedDistList.append(Thread.currentThread().getStackTrace()[1].toString() + "\n======Sorted Distance Unit======\n");
//		for(int i = 0; i < originalDistanceList.size(); i++){
//			msgSortedDistList.append(originalDistanceList.get(i).distance + " ");
//		}
//		msgSortedDistList.append("\n");
//		LogTool.log(LEVEL, msgSortedDistList.toString());
		
		ArrayList<ArrayList<DistanceUnit>> groupDisList = new ArrayList<ArrayList<DistanceUnit>>();
		int start = 0, end = 0;
		boolean startFixed = false, endFixed = false;
		for(int i = 0; i < originalDistanceList.size() - 1; i++){
			if(startFixed == false){
				startFixed = true;
				start = i;
			}
			
			if(originalDistanceList.get(i).distance == originalDistanceList.get(i + 1).distance){
				continue;
			}else{
				end = i;
				endFixed = true;
			}
			
			if(endFixed == true){
				ArrayList<DistanceUnit> tmpList = new ArrayList<DistanceUnit>();
				for(; start <= end; start ++){
					tmpList.add(originalDistanceList.get(start));
				}
				groupDisList.add(tmpList);
				startFixed = false;
				endFixed = false;
				start = end + 1;
			}
		}
		
		// Register log info
//		StringBuilder msgGroupDistList = new StringBuilder();
//		msgGroupDistList.append(Thread.currentThread().getStackTrace()[1].toString() + "'\n=======Display Grouped Distance Units=====\n");
//		msgGroupDistList.append("Distance Unit Size:" + originalDistanceList.size() + "\n");
//		msgGroupDistList.append("Group Size:" + groupDisList.size());
//		for(int i = 0; i < groupDisList.size(); i ++){
//			for(int j = 0; j < groupDisList.get(i).size(); j ++){
//				msgGroupDistList.append(groupDisList.get(i).get(j).distance + "(" + (groupDisList.get(i).get(j).from) + "," + 
//						(groupDisList.get(i).get(j).to) + ") ");
//			}
//			msgGroupDistList.append("\n");
//		}
//		LogTool.log(LEVEL, msgGroupDistList.toString());
		
		
		// *****************Start to create workface grouping using brackets*****************
		StringBuilder wfSeq = new StringBuilder();
		// Record if a workface has been processed
		int[] workProcessed = new int[numOfWorkfaces + 1];
		for(int i = 0; i <= numOfWorkfaces; i++){
			workProcessed[i] = -1;
		}
		int[] groupOfWf = new int[numOfWorkfaces + 1];
		for(int i = 0; i <= numOfWorkfaces; i++){
			groupOfWf[i] = -1;
		}
		int groupId =-1;
		
		// Iterate through sorted distance list
		// For each distance list
		boolean isGroupingOver = false;
		for(int i = 0; i < groupDisList.size(); i ++){
			// For each distance unit in a specific distance list
			for(int j = 0; j < groupDisList.get(i).size(); j ++){
				
				int wf1 = groupDisList.get(i).get(j).from;
				int wf2 = groupDisList.get(i).get(j).to;
				
				// Both workfaces are un-processed
				if(workProcessed[wf1] == -1 && workProcessed[wf2] == -1){
					if(wfSeq.length() == 0){
						wfSeq.append("(");
						wfSeq.append(wf1);
						wfSeq.append(",");
						wfSeq.append(wf2);
						wfSeq.append(")");
					}else{
						wfSeq.append(",(");
						wfSeq.append(wf1);
						wfSeq.append(",");
						wfSeq.append(wf2);
						wfSeq.append(")");
					}
					
					workProcessed[wf1] = (int) groupDisList.get(i).get(j).distance;
					workProcessed[wf2] = (int) groupDisList.get(i).get(j).distance;
					
					// Record grouping information of workfaces
					groupId ++;
					groupOfWf[wf1] = groupId;
					groupOfWf[wf2] = groupId;
					
					// Register log info
					StringBuilder wf12 = new StringBuilder();
					wf12.append(Thread.currentThread().getStackTrace()[1].toString() + "\n");
					wf12.append("WF1:" + wf1 + " WF2:" + wf2 + "\n");
					wf12.append(wfSeq + "\n");
					LogTool.log(LEVEL, wf12.toString());
					
					continue;
				}
				
				// One workface has been processed, the other is un-processed
				int proWf = 0, unProWf = 0;
				if(workProcessed[wf1] == -1 && workProcessed[wf2] != -1){
					proWf = wf2;
					unProWf = wf1;
				}
				
				if(workProcessed[wf1] != -1 && workProcessed[wf2] == -1){
					proWf = wf1;
					unProWf = wf2;
				}
				
				if(proWf != 0 && unProWf != 0){
					int distance = (int) groupDisList.get(i).get(j).distance;
					// Insert workface on the same level (same distance)
					if(distance == workProcessed[proWf]){
						wfSeq.insert(getWorkfaceIndex(wfSeq, String.valueOf(proWf)), String.valueOf(unProWf) + ",");
					}
					// Insert workface on the different level (different distance)
					else{
						// Determine left bound of current workface group including 'proWf'
						int curGroupId = groupOfWf[proWf];
						int smallestLeft = proWf, biggestRight = proWf;
						for(int si = 1; si <= numOfWorkfaces; si++){
							if(groupOfWf[si] == curGroupId){
								if(getWorkfaceIndex(wfSeq, String.valueOf(si)) < getWorkfaceIndex(wfSeq, String.valueOf(smallestLeft))){
									smallestLeft = si;
								}
								
								if(getWorkfaceIndex(wfSeq, String.valueOf(si)) > getWorkfaceIndex(wfSeq, String.valueOf(biggestRight))){
									biggestRight = si;
								}
							}
						}
						int iStart = getWorkfaceIndex(wfSeq, String.valueOf(smallestLeft));
						
						// Count the number of left - brackets
						while(iStart >= 0){
							
							if((wfSeq.charAt(iStart) == '(' && iStart == 0) || (wfSeq.charAt(iStart) == '(' && wfSeq.charAt(iStart - 1) == ',')){
								break;
							}
							
							iStart --;
						}
						
						int iEnd = getWorkfaceIndex(wfSeq, String.valueOf(biggestRight));
						
						while(iEnd < wfSeq.length()){
							if((wfSeq.charAt(iEnd) == ')' && iEnd == wfSeq.length() -1) || (wfSeq.charAt(iEnd) == ')' && wfSeq.charAt(iEnd + 1) == ',')){
								break;
							}								
							
							iEnd ++;
						}
						
						wfSeq.insert(iStart, "(");
						if((iEnd + 2) == wfSeq.length()){
							wfSeq.append("," + unProWf + ")");
						}else{
							wfSeq.insert(iEnd + 2, "," + unProWf + ")");
						}
						
					}
					workProcessed[unProWf] = (int) groupDisList.get(i).get(j).distance;
					
					// Record grouping information of workfaces
					groupId ++;
					groupOfWf[unProWf] = groupId;
					for(int tmpIndex = 1; tmpIndex <= numOfWorkfaces; tmpIndex ++){
						if(tmpIndex != proWf && groupOfWf[tmpIndex] == groupOfWf[proWf]){
							groupOfWf[tmpIndex] = groupOfWf[unProWf];
						}
					}
					groupOfWf[proWf] = groupOfWf[unProWf];
					
					//Register log info
//					StringBuilder msgTwoWfProcessed = new StringBuilder();
//					msgTwoWfProcessed.append(Thread.currentThread().getStackTrace()[1].toString() + "\n======Processed Two More Workfaces=======\n");
//					msgTwoWfProcessed.append("WF1:" + wf1 + " WF2:" + wf2 + "\n");
//					msgTwoWfProcessed.append(wfSeq + "\n");
//					LogTool.log(LEVEL, msgTwoWfProcessed.toString());
					
					continue;
				}
				
				// Both are processed already
				
				// Register log info
//				LogTool.log(LEVEL, Thread.currentThread().getStackTrace()[1].toString() + " Both WFs are processed - WF1 : " + wf1 + " WF2 : " + wf2);
				
				if(groupOfWf[wf1] == groupOfWf[wf2]){
					continue;
				}
				
				ArrayList<Integer> ret1 = getGroupStartEnd(wfSeq, String.valueOf(wf1), groupOfWf);
				ArrayList<Integer> ret2 = getGroupStartEnd(wfSeq, String.valueOf(wf2), groupOfWf);
				int startOf1 = ret1.get(0), endOf1 = ret1.get(1);
				int startOf2 = ret2.get(0), endOf2 = ret2.get(1);
				
				
				String subStr1 = wfSeq.substring(startOf1, endOf1 + 1);
				
				// Register log info
//				LogTool.log(LEVEL, Thread.currentThread().getStackTrace()[1].toString() + " Sub WF Seq for WF1 ( " + wf1 + " ) " + subStr1);
				
				String subStr2 = wfSeq.substring(startOf2, endOf2 + 1);
				
				// Register log info
//				LogTool.log(LEVEL, Thread.currentThread().getStackTrace()[1].toString() + " Sub WF Seq for WF2 ( " + wf2 + " ) " + subStr2);
				
				// Get sub workface sequence to decide whether subStr1 and subStr2 on the same level or not
				boolean isChanged = false;
				
				for(int tmpJ = 0; tmpJ < j; tmpJ ++){
					
					int tmpWorkface = groupDisList.get(i).get(tmpJ).from;
					
					// Register log info
//					LogTool.log(LEVEL, Thread.currentThread().getStackTrace()[1].toString() + " Temp WF(from) : " + tmpWorkface);
					
					if(getWorkfaceIndex(new StringBuilder(subStr1), String.valueOf(tmpWorkface)) != -1){
						isChanged = true;
						if(subStr1.charAt(0) =='(' && subStr1.charAt(1) == '('){
							subStr1 = wfSeq.substring(startOf1 + 1, endOf1);
							
							// Register log info
//							LogTool.log(LEVEL, Thread.currentThread().getStackTrace()[1].toString() + " Sub(cutted-from) WF Seq for WF1 ( " + wf1 + " ) " + subStr1);
						}
					}
					if(isChanged == true){
						break;
					}
					tmpWorkface = groupDisList.get(i).get(tmpJ).to;
					
					// Register log info
//					LogTool.log(LEVEL, Thread.currentThread().getStackTrace()[1].toString() + " Temp WF(to) : " + tmpWorkface);
					
					if(getWorkfaceIndex(new StringBuilder(subStr1), String.valueOf(tmpWorkface)) != -1){
						isChanged = true;
						if(subStr1.charAt(0) =='(' && subStr1.charAt(1) == '('){
							subStr1 = wfSeq.substring(startOf1 + 1, endOf1);
							
							// Register log info
//							LogTool.log(LEVEL, Thread.currentThread().getStackTrace()[1].toString() + " Sub(cutted-to) WF Seq for WF1 ( " + wf1 + " ) " + subStr1);
						}
					}
					if(isChanged == true){
						break;
					}
				}
				
				isChanged = false;
				for(int tmpJ = 0; tmpJ < j; tmpJ ++){
					int tmpWorkface = groupDisList.get(i).get(tmpJ).from;
					if(getWorkfaceIndex(new StringBuilder(subStr2), String.valueOf(tmpWorkface)) != -1){
						isChanged = true;
						if(subStr2.charAt(0) == '(' && subStr2.charAt(1) == '('){
							subStr2 = wfSeq.substring(startOf2 + 1, endOf2);
							
							// Register log info
//							LogTool.log(LEVEL, Thread.currentThread().getStackTrace()[1].toString() + " Sub(cutted-from) WF Seq for WF2 ( " + wf2 + " ) " + subStr2);
						}
					}
					if(isChanged == true){
						break;
					}
					tmpWorkface = groupDisList.get(i).get(tmpJ).to;
					if(getWorkfaceIndex(new StringBuilder(subStr2), String.valueOf(tmpWorkface)) != -1){
						isChanged = true;
						if(subStr2.charAt(0) == '(' && subStr2.charAt(1) == '('){
							subStr2 = wfSeq.substring(startOf2 + 1, endOf2);
							
							// Register log info
//							LogTool.log(LEVEL, Thread.currentThread().getStackTrace()[1].toString() + " Sub(cutted-to) WF Seq for WF2 ( " + wf2 + " ) " + subStr2);
						}
					}
					if(isChanged == true){
						break;
					}
				}
				
				// Delete useless sub workfaces
				if(startOf1 > startOf2){
					if(endOf1 + 1 < wfSeq.length()){
						if(startOf1 > 0){
							// Delete extra comma so startOf -1 instead of startOf
							wfSeq = wfSeq.delete(startOf1 - 1, endOf1 + 1);
						}else{
							wfSeq = wfSeq.delete(startOf1, endOf1 + 2);
						}
					}
					else{
						if(startOf1 > 0){
							wfSeq = wfSeq.delete(startOf1 - 1, wfSeq.length());
						}else{
							wfSeq = wfSeq.delete(startOf1, wfSeq.length());
						}
					}
						
					if(endOf2 + 1 < wfSeq.length()){
						if(startOf2 > 0){
							wfSeq = wfSeq.delete(startOf2 - 1, endOf2 + 1);
						}else{
							wfSeq = wfSeq.delete(startOf2, endOf2 + 2);
						}
					}
					else{
						if(startOf2 > 0){
							wfSeq = wfSeq.delete(startOf2 - 1, wfSeq.length());
						}else{
							wfSeq = wfSeq.delete(startOf2, wfSeq.length());
						}
					}
				}else{
					if(endOf2 + 1 < wfSeq.length()){
						if(startOf2 > 0){
							wfSeq = wfSeq.delete(startOf2 - 1, endOf2 + 1);
						}else{
							wfSeq = wfSeq.delete(startOf2, endOf2 + 2);
						}
					}
					else{
						if(startOf2 > 0){
							wfSeq = wfSeq.delete(startOf2 - 1, wfSeq.length());
						}else{
							wfSeq = wfSeq.delete(startOf2, wfSeq.length());
						}
					}
					
					if(endOf1 + 1 < wfSeq.length()){
						if(startOf1 > 0){
							wfSeq = wfSeq.delete(startOf1 - 1, endOf1 + 1);
						}else{
							wfSeq = wfSeq.delete(startOf1, endOf1 + 2);
						}
					}
					else{
						if(startOf1 > 0){
							wfSeq = wfSeq.delete(startOf1 - 1, wfSeq.length());
						}else{
							wfSeq = wfSeq.delete(startOf1, wfSeq.length());
						}
					}
				}
				if(wfSeq.length() > 0){
					wfSeq.append(",(");
					wfSeq.append(subStr1);
					wfSeq.append(",");
					wfSeq.append(subStr2);
					wfSeq.append(")");
				}else{
					
					boolean isNeededOuterBrackets = needOuterBrackets(subStr1);
					if(isNeededOuterBrackets){
						subStr1 = "(" + subStr1 + ")";
					}
					
					isNeededOuterBrackets = needOuterBrackets(subStr2);
					if(isNeededOuterBrackets){
						subStr2 = "(" + subStr2 + ")";
					}
					
					wfSeq.append("(");
					wfSeq.append(subStr1);
					wfSeq.append(",");
					wfSeq.append(subStr2);
					wfSeq.append(")");
					isGroupingOver = true;
					break;
				}
				
				groupId++;
				for(int tmpIndex = 1; tmpIndex <= numOfWorkfaces; tmpIndex ++){
					if((tmpIndex != wf1 && groupOfWf[tmpIndex] == groupOfWf[wf1]) || (tmpIndex != wf2 && groupOfWf[tmpIndex] == groupOfWf[wf2])){
						groupOfWf[tmpIndex] = groupId;
					}
				}
				groupOfWf[wf1] = groupId;
				groupOfWf[wf2] = groupId;
				
			}// end for inner for
			if(isGroupingOver == true){
				break;
			}
		}// end for outer for
		// Register log info -- print out workfaces based on distance sorting		
		StringBuilder msgWf = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
		msgWf.append("\n Workface Sequence based on distance sorting: " + wfSeq.toString() + "\n");
		LogTool.log(LEVEL, msgWf.toString());
		
		
		
		// Try to eliminate parentheses
		wfSeq = processParenLevel(wfSeq, parenClearLevel);
		
		// Register log info -- print out workfaces based on distance sorting		
		StringBuilder msgWfProcessed = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
		msgWfProcessed.append("\n Workface Sequence based on distance sorting(processed): " + wfSeq.toString() + "\n");
		LogTool.log(LEVEL, msgWfProcessed.toString());
		
		
		System.out.println("=======Workface Sequence based on distance sorting(processed-exclude parenthesis)========\n" + wfSeq.toString());

		Stack totalSortedGroup = new Stack();
		// Iterate over each char in wfSeq
		for(int ci = 0; ci < wfSeq.toString().length();){
			
			// Register log info
//			StringBuilder msgCurCharCurSortedGroup = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
//			msgCurCharCurSortedGroup.append("\nRead in next char, cur total sorted group is: " + totalSortedGroup.toString() + " cur len: " + totalSortedGroup.size() + "\n");
//			LogTool.log(LEVEL, msgCurCharCurSortedGroup.toString());
			
			if(wfSeq.charAt(ci) == '('){
				
				//bracketStack.push(ci);
				totalSortedGroup.push('(');
				ci ++;
				
			}else if(wfSeq.charAt(ci) == ')'){

				boolean isSortGroup = false;
				ArrayList<Integer> curSortList = new ArrayList<Integer>();
				ArrayList<ArrayList<Integer>> curSortGroup = new ArrayList<ArrayList<Integer>>();
				
				while(totalSortedGroup.empty() == false){
					
					Object tmpPopEle = totalSortedGroup.pop();
					if(tmpPopEle instanceof Character){
						
						// left bracket is omitted
						break;
						
					}else if(tmpPopEle instanceof Integer && isSortGroup == false){
						curSortList.add(0, (Integer)tmpPopEle);
					}else if(tmpPopEle instanceof Integer && isSortGroup == true){
						ArrayList<Integer> tmpList = new ArrayList<Integer>();
						tmpList.add((Integer)tmpPopEle);
						curSortGroup.add(tmpList);
					}
					// sort group
					else if(tmpPopEle instanceof ArrayList){
						
						isSortGroup = true;
						
						if(curSortList.size() != 0){
							
							ArrayList<Integer> tmpList = null;
							for(int i = 0; i < curSortList.size(); i ++){
								tmpList = new ArrayList<Integer>();
								tmpList.add(curSortList.get(i));
								curSortGroup.add(tmpList);
							} 
							curSortList.clear();
						}
						
						curSortGroup.add((ArrayList<Integer>)tmpPopEle);
					}// end for else if
					
				}// end for while
				
				// *****************Sort workface group *****************
				ArrayList<ArrayList<Integer>> tmpSortGroupRet = null;
				// Sort workface group
				if(isSortGroup == true){
					
					//Register log info
					StringBuilder msgBeforeAfterGroups = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
					msgBeforeAfterGroups.append("\nbefore sort groups: " + curSortGroup + "\n");
					tmpSortGroupRet = SortTool.sortGroups_new(curSortGroup, opInfo, workload, distance1);
					msgBeforeAfterGroups.append("\nafter sort groups: " + tmpSortGroupRet + "\n");
					ArrayList<Integer> tmpSortedGroupRet = new ArrayList<Integer>();
					
					// After group is sorted, all workfaces should be in the same group
					for(int iTmp = 0; iTmp < tmpSortGroupRet.size(); iTmp ++){
						for(int jTmp = 0; jTmp < tmpSortGroupRet.get(iTmp).size(); jTmp ++){
							tmpSortedGroupRet.add(tmpSortGroupRet.get(iTmp).get(jTmp));
						}
					}
					msgBeforeAfterGroups.append("before inserting the after-sorted groups,size of totalSortedGroup: " + totalSortedGroup.size() + "\n");
					LogTool.log(LEVEL, msgBeforeAfterGroups.toString());
					totalSortedGroup.push(tmpSortedGroupRet);
					
				}
				// Sort workfaces
				else{
					
					
					//Register log info
					StringBuilder msgBeforeAfterGroups = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
					msgBeforeAfterGroups.append("before sort workfaces: " + curSortList + "\n");
					Dataset[] dataSet = new DefaultDataset[1];
					dataSet[0] = data;
					ArrayList<ArrayList<Integer>> tmpPara = new ArrayList<ArrayList<Integer>>();
					tmpPara.add(curSortList);
//					tmpSortGroupRet = SortTool.sortWorkfaces_new1(dataSet, tmpPara, opInfo, workload, distance1);
					tmpSortGroupRet = SortTool.sortWorkfaces_new(dataSet, tmpPara, opInfo, workload, initPos);
					msgBeforeAfterGroups.append("after sort workfaces: " + tmpSortGroupRet + "\n");
					LogTool.log(LEVEL, msgBeforeAfterGroups.toString());
					totalSortedGroup.push(tmpSortGroupRet.get(0));
				}
				ci ++;
			}
			else if(wfSeq.charAt(ci) >= '0' && wfSeq.charAt(ci) <= '9'){
					int tmpIndex = ci + 1;
					while(wfSeq.charAt(tmpIndex) >= '0' && wfSeq.charAt(tmpIndex) <= '9'){
						tmpIndex ++;
					}
					totalSortedGroup.push(Integer.valueOf(wfSeq.subSequence(ci, tmpIndex).toString()));
					//Register log info
//					StringBuilder msgReadinInt = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
//					msgReadinInt.append("\nRead in another integer: " + totalSortedGroup + "\n");
//					LogTool.log(LEVEL, msgReadinInt.toString());
					
					ci = tmpIndex;
			}else{
				// Current character is comma, advance ci one step forward
				ci ++;
			}
		}

		ArrayList<Integer> finalSortedWorkfaceRet = new ArrayList<Integer>();
		while(totalSortedGroup.empty() == false){
			Object curTmpEle = totalSortedGroup.pop();
			if(curTmpEle instanceof ArrayList){
				for(int iFinal = ((ArrayList)curTmpEle).size() - 1; iFinal >= 0; iFinal --){
					finalSortedWorkfaceRet.add(0, (Integer)(((ArrayList)curTmpEle).get(iFinal)));
				}
			}
		}

		ArrayList<ArrayList<Integer>> finalRet = new ArrayList<ArrayList<Integer>>();
		finalRet.add(finalSortedWorkfaceRet);
		return finalRet;
	}
	/**
	 * Allocate workface workload when there are multiple groups of operating machines
	 * @param numOfMachines The number of operating machines
	 */
	@Deprecated
	public static void getClustersOfWorkfacesMultipleMachines(int numOfMachines, ArrayList<Integer> sortedWorkfaceList){
		
		// Split sorted workfaces by only one group of operating machines into "numOfMachines" segments. 
		int numOfGroups = (sortedWorkfaceList.size() % numOfMachines) == 0 ?(sortedWorkfaceList.size() / numOfMachines):((sortedWorkfaceList.size() / numOfMachines) + 1);
		ArrayList<ArrayList<Integer>> groupOfWorkface = new ArrayList<ArrayList<Integer>>();
		for(int i = 0; i < numOfGroups; i ++){
			ArrayList<Integer> curGroup = new ArrayList<Integer>();
			// The last group may contain less than "numOfMachines" workfaces
			for(int j = 0 + i * numOfMachines; j < numOfMachines + numOfMachines * i && j < sortedWorkfaceList.size(); j ++){
				curGroup.add(sortedWorkfaceList.get(j));
			}
			groupOfWorkface.add(curGroup);
		}
		
		// Compute operating time of each segment
		// TODO--This method needs to be further implemented.
	}
	
	/**
	 * Decide if outer brackets are needed to put outside the wfList
	 * @param wfList Workface list to process
	 * @return false if no brackets are needed; otherwise true
	 */
	private static boolean needOuterBrackets(String wfList){
		boolean isNeeded = true;
		Stack<ParenLevel> stack = new Stack<ParenLevel>();
		for(int i = 0; i < wfList.length(); i ++){
			if(wfList.charAt(i) == '('){
				stack.push(new ParenLevel('(', 0, i));
			}else if(wfList.charAt(i) == ')'){
				if(i == wfList.length() - 1){
					ParenLevel firstBracket = stack.pop();
					if(firstBracket.index == 0){
						isNeeded = false;
						break;
					}
				}else{
					stack.pop();
				}
			}
		}
		return isNeeded;
	} 
	
	/**
	 * Get the starting and ending index of a group of workfaces where <i>wf</i> resides in.
	 * @param wfSeq A list of workface to be examined. 
	 * @param wf Workface to be searched.
	 * @param groupOfWf Group of unprocessed workfaces.
	 * @return
	 */
	private static ArrayList<Integer> getGroupStartEnd(StringBuilder wfSeq, String wf, int[] groupOfWf){

		int start = getWorkfaceIndex(wfSeq, wf), end = start;
		int smallestLeft = start, biggestRight = end;
		
		for(int i = 1; i <= groupOfWf.length - 1; i++){
			if(groupOfWf[Integer.valueOf(wf)] == groupOfWf[i]){
				if(getWorkfaceIndex(wfSeq, String.valueOf(i)) <= start){
					start = getWorkfaceIndex(wfSeq, String.valueOf(i));
					smallestLeft = i;
				}
				if(getWorkfaceIndex(wfSeq, String.valueOf(i)) >= end){
					end = getWorkfaceIndex(wfSeq, String.valueOf(i));
					biggestRight = i;
				}
			}
		}
		
		start = getWorkfaceIndex(wfSeq, String.valueOf(smallestLeft));
		end = getWorkfaceIndex(wfSeq, String.valueOf(biggestRight));
		while(start >= 0){
			
			if((wfSeq.charAt(start) == '(' && start == 0) || ((wfSeq.charAt(start) == '(') && (wfSeq.charAt(start - 1) == ','))){
				break;
			}
			start --;
		}
		
		while(end < wfSeq.length()){
			if((wfSeq.charAt(end) == ')' && end == wfSeq.length() - 1) || (wfSeq.charAt(end) == ')' && wfSeq.charAt(end + 1) == ',')){
				break;
			}
			end ++;
		}
		
		ArrayList<Integer> indexList = new ArrayList<Integer>();
		indexList.add(start);
		indexList.add(end);
		return indexList;
	}
	
	/**
	 * Get the index of one workface in a workface sequence. Both workface sequence and workface are represented in string values.
	 * @param sb Workface sequence
	 * @param substr Workface to be searched
	 * @return The index of seached workface in the workface sequence
	 */
	private  static int getWorkfaceIndex(StringBuilder sb, String substr){
        
		int tmpIndex = sb.indexOf(substr);
		if(tmpIndex == -1){
			return -1;
		}
		
		int tmp = tmpIndex + 1;
		while(tmp < sb.length()){
			
			// the left neighboring character of current workface should be non-number
			while(tmpIndex > 0 && (sb.charAt(tmpIndex - 1) >= '0' && sb.charAt(tmpIndex - 1) <= '9')){
				tmpIndex = sb.indexOf(substr, tmpIndex + 1);
			}
			
			// No substr in sb
			if(tmpIndex == -1){
				return -1;
			}
			
			// the right neighboring character of current workface should be non-number
			tmp = tmpIndex + 1;
			if(tmp > 0){
				while(sb.charAt(tmp) >= '0' && sb.charAt(tmp) <= '9'){
					tmp ++;
				}
				
				if(substr.compareTo(sb.substring(tmpIndex, tmp)) == 0){
					// Register log info
//					String message = Thread.currentThread().getStackTrace()[1].toString() + "Workface Seq: " + sb.toString() + " Workface: " + substr + " Workface index: " + tmpIndex;   
//					LogTool.log(LEVEL, message);
					
					return tmpIndex;
				}else{
					//tmpIndex = sb.indexOf(substr, tmpIndex + 1);
					tmpIndex = sb.indexOf(substr, tmp + 1);
					tmp = tmpIndex + 1;
				}
			}
		}
		// Register log info
//		String message = Thread.currentThread().getStackTrace()[1].toString() + "Workface Seq: " + sb.toString() + " Workface: " + substr + " Workface index: " + tmpIndex;   
//		LogTool.log(LEVEL, message);
		
		return tmpIndex;
	}
	
	
	/**
	 * @deprecated This method is currently obsolete. Please refer to {@link #getClustersOfWorkfaces_zhen_new(String fileName, int numOfWorkphases, String delimiter)}
	 * @param fileName
	 * @param numOfWorkphases
	 * @param delimiter
	 * @return
	 * @throws IOException 
	 */
	@Deprecated 
	public static ArrayList<ArrayList<Integer>> getClustersOfWorkfaces_zhen(String fileName, int numOfWorkphases, String delimiter) throws IOException{
		/* Load a dataset */
      //Dataset data = FileHandler.loadDataset(new File("workphase.txt"), 5, "\t");
		Dataset data = FileHandler.loadDataset(new File(fileName), numOfWorkphases, delimiter);
		for(int i = 0; i < data.size(); i ++){
			System.out.println(data.get(i));
		}
		
		// Get original distance list
		ArrayList<DistanceUnit> originalDistanceList = new ArrayList<DistanceUnit>();
		for(int row = 0; row < data.size(); row ++){
			//System.out.println("row size: " + data.get(row).size());
			for(int col = row + 1; col < data.get(row).size(); col ++){
				 
				DistanceUnit du = new DistanceUnit();
				du.distance = data.get(row).get(col);
				du.from = row + 1;
				du.to = col + 1;
				
				originalDistanceList.add(du);
			}
		}
		
		// Sort distance list in ascending order
		DUComparator comparator = new DUComparator();
		Collections.sort(originalDistanceList, comparator);
		System.out.println("=====================sorted distance list===================");
		for(int i = 0; i < originalDistanceList.size(); i++){
			System.out.println(originalDistanceList.get(i).distance + " ");
		}
		
		ArrayList<ArrayList<DistanceUnit>> groupDisList = new ArrayList<ArrayList<DistanceUnit>>();
		int start = 0, end = 0;
		boolean startFixed = false, endFixed = false;
		for(int i = 0; i < originalDistanceList.size() - 1; i++){
			if(startFixed == false){
				startFixed = true;
				start = i;
			}
			
			if(originalDistanceList.get(i).distance == originalDistanceList.get(i + 1).distance){
				continue;
			}else{
				end = i;
				endFixed = true;
			}
			
			if(endFixed == true){
				ArrayList<DistanceUnit> tmpList = new ArrayList<DistanceUnit>();
				for(; start <= end; start ++){
					tmpList.add(originalDistanceList.get(start));
				}
				groupDisList.add(tmpList);
				startFixed = false;
				endFixed = false;
				start = end + 1;
			}
		}
		
		System.out.println("=======Display grouped distance units=====================");
		System.out.println("distance unit size:" + originalDistanceList.size());
		System.out.println("group size:" + groupDisList.size());
		for(int i = 0; i < groupDisList.size(); i ++){
			for(int j = 0; j < groupDisList.get(i).size(); j ++){
				System.out.print(groupDisList.get(i).get(j).distance + "(" + (groupDisList.get(i).get(j).from) + "," + 
						(groupDisList.get(i).get(j).to) + ") ");
			}
			System.out.println();
		}
		
		// Zhen's algorithm starts seriously FROM HERE
		System.out.println("=======Display improved grouped distance units=====================");
		// Preperation structure 1
		boolean[] workfaceResolved = new boolean[numOfWorkphases + 1];
		for(int i = 0; i < numOfWorkphases; i ++){
			workfaceResolved[i] = false;
		}
		
		// Preperation structure 2
		String[] groupId = new String[numOfWorkphases + 1];
		for(int i = 0; i < numOfWorkphases; i++){
			groupId[i] = new String("");
		}
		
		// Preperation structure 3
		int groupIndex = 1;
		
		// Group same workfaces in different DistanceUnit together
		//ArrayList improvedGroupWorkface = new ArrayList();
		int i = 0, j = 0;// i for all distance; j for all DistanceUnits for a certain distance
		boolean isAllDuResolved = false;//, isStartGroup = false;
		for(; i < groupDisList.size(); i ++){
			for(j = 0; j < groupDisList.get(i).size(); j ++){
				
				if(workfaceResolved[groupDisList.get(i).get(j).from] == true && workfaceResolved[groupDisList.get(i).get(j).to] == true){
					// Process next DistanceUnit
					continue;
				}
				
				if(workfaceResolved[groupDisList.get(i).get(j).from] == false && workfaceResolved[groupDisList.get(i).get(j).to] == false){
					
					workfaceResolved[groupDisList.get(i).get(j).from] = true;
					workfaceResolved[groupDisList.get(i).get(j).to] = true;
					groupId[groupDisList.get(i).get(j).from] = String.valueOf(groupIndex);
					System.out.println("group Id value::::("+groupDisList.get(i).get(j).from+")" + groupId[groupDisList.get(i).get(j).from]);
					groupId[groupDisList.get(i).get(j).to] = String.valueOf(groupIndex);
					System.out.println("group Id value::::("+groupDisList.get(i).get(j).to+")" + groupId[groupDisList.get(i).get(j).to]);
					groupIndex ++;
					
					// Test if all workfaces have been resolved
					int cwf = 0;
					for(int wf = 1; wf <= numOfWorkphases; wf ++){
						if(workfaceResolved[wf] == true){
							cwf ++;
						}
					}
					if(cwf == numOfWorkphases){
						isAllDuResolved = true;
					}
					
					if(isAllDuResolved == true){
						break;
					}
					continue;
				}
				
				// workface "to" is new
				if(workfaceResolved[groupDisList.get(i).get(j).from] == true){
					
					String tmpStr = groupId[groupDisList.get(i).get(j).from];
					for(int t = 0; t < numOfWorkphases; t ++){
						if((groupId[t].startsWith(String.valueOf(tmpStr.charAt(0)))) && (groupId[t].length() >= tmpStr.length())){
							tmpStr = groupId[t];
						}
					}
					tmpStr = tmpStr + String.valueOf(tmpStr.charAt(0));
					groupId[groupDisList.get(i).get(j).to] = tmpStr;
					workfaceResolved[groupDisList.get(i).get(j).to] = true;
					System.out.println("group Id value::::("+groupDisList.get(i).get(j).to+")" + groupId[groupDisList.get(i).get(j).to]);
					
					// Test if all workfaces have been resolved
					int cwf = 0;
					for(int wf = 1; wf <= numOfWorkphases; wf ++){
						if(workfaceResolved[wf] == true){
							cwf ++;
						}
					}
					if(cwf == numOfWorkphases){
						isAllDuResolved = true;
					}
					
					if(isAllDuResolved == true){
						break;
					}
				}
				// workface "from" is new
				else{
					String tmpStr = groupId[groupDisList.get(i).get(j).to];
					for(int t = 0; t < numOfWorkphases; t ++){
						if((groupId[t].startsWith(String.valueOf(tmpStr.charAt(0)))) && (groupId[t].length() >= tmpStr.length())){
							tmpStr = groupId[t];
						}
					}
					tmpStr = tmpStr + String.valueOf(tmpStr.charAt(0));
					groupId[groupDisList.get(i).get(j).from] = tmpStr;
					workfaceResolved[groupDisList.get(i).get(j).from] = true;
					System.out.println("group Id value::::("+groupDisList.get(i).get(j).from+")" + groupId[groupDisList.get(i).get(j).from]);
					
					// Test if all workfaces have been resolved
					int cwf = 0;
					for(int wf = 1; wf <= numOfWorkphases; wf ++){
						if(workfaceResolved[wf] == true){
							cwf ++;
						}
					}
					if(cwf == numOfWorkphases){
						isAllDuResolved = true;
					}
					
					if(isAllDuResolved == true){
						break;
					}
				}
			}// end inner for - all DistanceUnits for a certain distance
			
			if(isAllDuResolved == true){
				break;
			}
		}// end outer for - each difference
		
		//*****************************Display raw group workfaces*****************************
		for(int wf = 0; wf <= numOfWorkphases; wf ++){
			System.out.print(wf + " ");
		}
		System.out.println();
		for(int wf = 0; wf <= numOfWorkphases; wf ++){
			System.out.print(groupId[wf] + " ");
		}
		System.out.println();
		for(int wfr = 0; wfr <= numOfWorkphases; wfr ++){
			System.out.print(workfaceResolved[wfr] + " ");
		}
		System.out.println("\n"+isAllDuResolved + " " + i + " " + j);
		System.out.println("groupindex:"+groupIndex);
		//**********************************************************
		
		// sort raw workfaces
		ExcelReader er = new ExcelReader();
		MachineOpInfo machineOpInfo = er.readMachineOpInfo("machine-op-info.xls");
		WorkfaceWorkload workload = er.readWorkfaceWorkload("workface-workload.xls"); 
		
		for(int indexWfR = 0; indexWfR < numOfWorkphases; indexWfR ++){
			workfaceResolved[indexWfR] = false;
		}
		
		// Process the first level workfaces
		ArrayList<ArrayList<Integer>> finalFirstLevelWorkfaceSort = new ArrayList<ArrayList<Integer>>(); 
		for(int rawGroupIndex = 1; rawGroupIndex < groupIndex; rawGroupIndex ++ ){
			
			Dataset ds[] = new DefaultDataset[1];
			int indexOfDs = 0;
			ds[indexOfDs] = new DefaultDataset();
			ArrayList<ArrayList<Integer>> retOfWorkfaceSort = new ArrayList<ArrayList<Integer>>(); 
			for(int rwf = 1; rwf <= numOfWorkphases; rwf ++){
				
				if(Integer.valueOf(groupId[rwf])== rawGroupIndex){
					System.out.println("rwf:" + rwf);
					System.out.println("add to ds result:" + ds[indexOfDs].add(data.get(rwf - 1)));
					workfaceResolved[rwf] = true; 
					System.out.println("--ds["+indexOfDs+"].size():" + ds[indexOfDs].size());
				}
				
			}
			System.out.println("ds["+indexOfDs+"].size():" + ds[indexOfDs].size() + "\n ds.length:"+ ds.length);
			// Sort current workfaces in raw group (rawGroupIndex)
			
			retOfWorkfaceSort = SortTool.sortWorkfaces(ds,  machineOpInfo, workload);
			for(int tmp = 0; tmp < retOfWorkfaceSort.size(); tmp ++){
				System.out.println("Sorted workfaces:" + retOfWorkfaceSort.get(tmp));
			}
			finalFirstLevelWorkfaceSort.add(retOfWorkfaceSort.get(0));
			//return null;
		}		
		
		for(int tin = 0; tin < finalFirstLevelWorkfaceSort.size(); tin ++){
			System.out.println("ONLY FIRST LEVEL ---- :" + finalFirstLevelWorkfaceSort.get(tin));
		}
		
		// Process the rest level workfaces
		WorkfaceDistance distance = er.readWorkfaceDistance("DistanceMatrix.xls");
		for(int level = 2; level < 4; level ++){
			for(int wfr = 1; wfr <= numOfWorkphases; wfr ++){
				if(workfaceResolved[wfr] == false && groupId[wfr].length() == level){
					workfaceResolved[wfr] = true;
					// Get the group id of un-processed 2nd level workface
					int id = Integer.valueOf(String.valueOf(groupId[wfr].charAt(0)));
					System.out.println("CURRENT 2ND LEVEL WORKFACE:" + wfr);
					ArrayList<Integer> corFirstLevelGroup = finalFirstLevelWorkfaceSort.get(id - 1);
					System.out.println("Current 1st level workface:" + corFirstLevelGroup);
					ArrayList<Integer> cor2ndLevelWf = new ArrayList<Integer>();
					cor2ndLevelWf.add(wfr - 1); // index of workface is 1 less than reall number
					ArrayList<ArrayList<Integer>> tmpSortedWorkfaceGroups = new ArrayList<ArrayList<Integer>>();
					tmpSortedWorkfaceGroups.add(corFirstLevelGroup);
					tmpSortedWorkfaceGroups.add(cor2ndLevelWf);
					ArrayList<ArrayList<Integer>> ret2ndGroups = SortTool.sortGroups_new(tmpSortedWorkfaceGroups, machineOpInfo, workload, distance);
					
					ArrayList<Integer> result1List = ret2ndGroups.get(0);
					result1List.addAll(ret2ndGroups.get(1));
					finalFirstLevelWorkfaceSort.remove(id - 1);
					finalFirstLevelWorkfaceSort.add((id - 1), result1List);				
				}
			}//end for - wfr
		}// end for - level
		
		for(int tin = 0; tin < finalFirstLevelWorkfaceSort.size(); tin ++){
			System.out.println("FINAL ---- :" + finalFirstLevelWorkfaceSort.get(tin));
		}
		
		System.out.println();
		for(int wfr = 0; wfr <= numOfWorkphases; wfr ++){
			System.out.print(workfaceResolved[wfr] + " ");
		}
		
		
		//=========== Start from i and j again to process the group sorting=======
		System.out.println("\n***************** Start from i and j again to process the group sorting********************");
		System.out.println("finalFirstLevelWorkfaceSort size: "+finalFirstLevelWorkfaceSort.size());
		// processing should refer to ArrayList<ArrayList<Integer>> - finalFirstLevelWorkfaceSort
		ArrayList<ArrayList<Integer>> finalResultList = new ArrayList<ArrayList<Integer>>();
		
		boolean isFinal = false;
		for(; i < groupDisList.size(); i ++){
			for(; j < groupDisList.get(i).size(); j ++){
				System.out.println("IMPROVED finalFirstLevelWorkfaceSort size: " + finalFirstLevelWorkfaceSort.size());
				if(finalFirstLevelWorkfaceSort.size() > 1){
					int idFor1 = 0, idFor2 = 0;
					int fromWorkface = groupDisList.get(i).get(j).from;
					int toWorkface = groupDisList.get(i).get(j).to;
					boolean isInSameGroup = false;
					
					for(int tmpGId = 0; tmpGId < finalFirstLevelWorkfaceSort.size(); tmpGId ++){
						if(finalFirstLevelWorkfaceSort.get(tmpGId).contains(fromWorkface) && finalFirstLevelWorkfaceSort.get(tmpGId).contains(toWorkface)){
							isInSameGroup = true;
							break;
						}
						
						if(finalFirstLevelWorkfaceSort.get(tmpGId).contains(fromWorkface)){
							idFor1 = tmpGId;
						}
						
						if(finalFirstLevelWorkfaceSort.get(tmpGId).contains(toWorkface)){
							idFor2 = tmpGId;
						}
					}
					
					if(isInSameGroup == true){
						continue;
					}else{
						ArrayList<Integer> workfaceGroup1 = finalFirstLevelWorkfaceSort.get(idFor1);
						ArrayList<Integer> workfaceGroup2 = finalFirstLevelWorkfaceSort.get(idFor2);
						ArrayList<ArrayList<Integer>> tmpFinalResult = new ArrayList<ArrayList<Integer>>();
						tmpFinalResult.add(workfaceGroup1);
						tmpFinalResult.add(workfaceGroup2);
						tmpFinalResult = SortTool.sortGroups_new(tmpFinalResult, machineOpInfo, workload, distance);
						
						finalFirstLevelWorkfaceSort.remove(workfaceGroup1);
						finalFirstLevelWorkfaceSort.remove(workfaceGroup2);
						
						workfaceGroup1 = tmpFinalResult.get(0);
						workfaceGroup1.addAll(tmpFinalResult.get(1));
						finalFirstLevelWorkfaceSort.add(workfaceGroup1);
					}
					
				}// end for size > 1
				else{
					isFinal = true;
					break;
				}
			}// end for - j
			
			if(isFinal == true){
				finalResultList = finalFirstLevelWorkfaceSort;
				return finalResultList;
			}
		}// end for - i
		
		// The finalFirstLevelWorkfaceSort might still have more than 1 list in the end
		System.out.println("isFinal : " + isFinal + "\n i value: " + i);
		for(int fi = 0; fi < finalFirstLevelWorkfaceSort.size(); fi ++){
			System.out.println(finalFirstLevelWorkfaceSort.get(fi));
		}
		
		if(finalFirstLevelWorkfaceSort.size() > 1){
			
			finalFirstLevelWorkfaceSort = SortTool.sortGroups_new(finalFirstLevelWorkfaceSort, machineOpInfo, workload, distance);
			ArrayList<Integer> finalWorkfaceList = finalFirstLevelWorkfaceSort.get(0);
			for(int fIndex = 1; fIndex < finalFirstLevelWorkfaceSort.size(); fIndex ++){
				finalWorkfaceList.addAll(finalFirstLevelWorkfaceSort.get(fIndex));
			}
			finalResultList.add(finalWorkfaceList);
		}
		
		//System.out.println("FINAL total workface seq:" + finalResultList.get(0));
		return finalResultList;
	}


	/**
	 * This method checks if two <i> HashSet</i> s intersects with each other.
	 * @param set1 The first HashSet
	 * @param set2 The second HashSet
	 * @return true if the two HashSets intersect, otherwise, false
	 */
	@Deprecated
	public static boolean isIntersect(HashSet<Integer> set1, HashSet<Integer> set2){
		if(set1 == null || set2 == null){
			return false;
		}
		
		Iterator<Integer> iterator = set2.iterator();
		while(iterator.hasNext()){
			int value = iterator.next();
			if(set1.contains(value)){
				return true;
			}
		}
		return false;
	}
	/**
	 * @deprecated This method is currently obsolete. Please refer to {@link #getClustersOfWorkfaces_zhen_new(String, int, String, MachineOpInfo, WorkfaceWorkload, WorkfaceDistance)}
	 * @param fileName
	 * @param numOfWorkphases
	 * @param delimiter
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	public static Dataset[] getClustersOfWorkfaces(String fileName, int numOfWorkphases, String delimiter) throws IOException{
		
		
		/* Load a dataset */
        //Dataset data = FileHandler.loadDataset(new File("workphase.txt"), 5, "\t");
		Dataset data = FileHandler.loadDataset(new File(fileName), numOfWorkphases, delimiter);
		System.out.println("data size: "+data.size());
		System.out.println("=======================dataset========================");
		for(int i = 0; i < data.size(); i++){
			System.out.println(data.get(i)+":"+data.get(i).getClass().getName());
			for(int j = 0; j< data.get(i).size(); j++)
				System.out.print(data.get(i).value(j)+"-");
			System.out.println();
		}
		System.out.println("=======================dataset========================");
        
        int finalClusterNum = 0;
        boolean isBestResult = false, flagForTwice = false;
        Dataset[] clusters = null, finalClusters = null; 
        
        System.out.println("data len:"+data.size());
        
        
        // Start with cluster number of 2 to num_of_workfaces/2 included
        for(int i = 2 ;i <= data.size()/2; i++){
        	
        	isBestResult = false;
        	System.out.println("i:"+i);
        	
        	Clusterer km = new KMeans(i);
        	clusters = km.cluster(data);
        	
        	System.out.println("=======================cluster info========================");
        	for(int tmp = 0; tmp < i; tmp++)
        		System.out.println(clusters[tmp]);
        	System.out.println("=======================cluster info========================");
        	
        	// Check each cluster to see if i clusters give best result
        	for(int j = 0; j < clusters.length; j++){
        		
        		//This cluster only has one value, ignore
        		if(clusters[j].size() == 1){
        			continue;
        		}
        		
        		//Find maximum distance in a cluster
        		double maxDist = 0;
        		ArrayList<Integer> excludeId = new ArrayList<Integer> ();
        		for(int it = 0; it < clusters[j].size() - 1; it++){
        			for(int jt = it + 1; jt < clusters[j].size(); jt++){
        				//double tmpDist = data.get(clusters[j].get(it).getID()).value(jt);
        				
        				System.out.print("row:"+clusters[j].get(it).getID() + " col:"+clusters[j].get(jt).getID());
        				double tmpDist = data.get(clusters[j].get(it).getID()).value(clusters[j].get(jt).getID());
        				System.out.println(" dist:"+tmpDist);
        				
        				excludeId.add(clusters[j].get(it).getID());
        				maxDist = ( tmpDist> maxDist)? tmpDist:maxDist;
        			}
        		}
        		excludeId.add(clusters[j].get(clusters[j].size() - 1).getID());
        		
        		System.out.println("max dist:"+maxDist);
        		
        		//Check if there are points outside this cluster which should be in this cluster
        		boolean isGoodCluster = false;
        		for(int in = 0; in < data.size(); in++){
        			if(excludeId.contains(in) == false){
        				//Calculate distance between points outside this cluster with points in this cluster
        				int jn = 0;
        				for(; jn< excludeId.size(); jn++){
        					double cutDist = data.get(in).value(excludeId.get(jn));
        					System.out.print("row: "+in+" col:"+excludeId.get(jn)+" ");
        					// false means good cluster
        					// true means bad cluster
        					isGoodCluster = (cutDist > maxDist)? false: true;
        					System.out.print("cutDist:"+cutDist + " maxDist:"+maxDist + "> false, < true:"+isGoodCluster+"\n");
        					if(isGoodCluster == true)
        						break;
        				}
        				// There are points outside which should be put inside
        				if(jn < excludeId.size())
        					break;
        			}
        		}
        		
        		// This cluster is not a good one
        		if(isGoodCluster == true){
        			isBestResult = false;
        			break;
        		}
        		// This cluster is good enough
        		else{
        			isBestResult = true;
        		}
        	}// for: check each cluster
        	
        	if(isBestResult == true){
        		finalClusterNum = i;
        		finalClusters = clusters;
        		//break;
        	}
        	
        	System.out.println("finalClusterNum:"+finalClusterNum +" i:"+i);
        	
        	// To make sure each time K-means gives best clusters for current cluster number, 
        	// Each cluster number is carried out twice
        	if(flagForTwice == false){
        		i = i - 1;
        		flagForTwice = true;
        	}
        	else{
        		flagForTwice = false; 
        	}
        }// for: try different cluster number
        return finalClusters;
	}/* getClustersOfWorkphases */

	/**
	 * Get rid of parentheses based on the <i>clearLevel</i> argument.
	 * @param workface Workfaces sorted based on distance and grouped using parentheses
	 * @param clearLevel Level of parentheses to be eliminated
	 * @return processed workface list grouped using parentheses 
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

	// Using zhen's way to cluster
	public static void main(String[] args) throws Exception {
		
		// Determine whether to output debug info or not
		ClusterTool.LEVEL = LogTool.LEVEL_OPEN;
		SortTool.LEVEL = LogTool.LEVEL_OPEN;
//		ClusterTool.LEVEL = LogTool.LEVEL_CLOSE;
//		SortTool.LEVEL = LogTool.LEVEL_CLOSE;
		
		//***************start the grouping workface process********************
		//Dataset[] dss = ClusterTool.getClustersOfWorkfaces("workface-distance.txt", 20, "\t");
		
		
		
		
		// Read in workface distance information
		WorkfaceDistance distance = new WorkfaceDistance(20);
		BufferedReader br = null;
		ArrayList<Double> singleWorkloadInfo = null;
		try{
			String curLine = null;
			br = new BufferedReader(new FileReader("workface-distance.txt"));
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
		MachineOpInfo opInfo = new MachineOpInfo(6); // there are in total 6 machines
		ArrayList<Double> singleOpInfo = null;
		try{
			String curLine = null;
			br = new BufferedReader(new FileReader("machine-op-info.txt"));
			while((curLine = br.readLine()) != null){
				
				String[] opRet = curLine.split("\t");
				singleOpInfo = new ArrayList<Double>();
				singleOpInfo.add(Double.valueOf(opRet[0]));
				singleOpInfo.add(Double.valueOf(opRet[1]));
				opInfo.addMachineOpInfo(singleOpInfo);
				singleOpInfo = null;
			}
			
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(br != null)
				br.close();
		}
		//Register log info -- Print out machine operation information
//				StringBuilder msgOpInfo = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
//				msgOpInfo.append(opInfo.outputMachineOpInfo());
//				LogTool.log(LEVEL, msgOpInfo.toString());
		
		// Read in workface workload information
		WorkfaceWorkload workload = new WorkfaceWorkload(6,20);
		ArrayList<Double> singleWorkload = null;
		try{
			String curLine = null;
			br = new BufferedReader(new FileReader("workface-workload.txt"));
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
			br = new BufferedReader(new FileReader("machine-initial-location.txt"));
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
		
		
		
		System.out.println("Compute workface cluster by priority?[YES or NO]");
		Scanner s = new Scanner(System.in);
		String ret = s.nextLine();
		if(ret.equalsIgnoreCase("yes")){
			WorkfacePriority wfPriority = new WorkfacePriority();
			BufferedReader bReader = new BufferedReader(new FileReader("workface-priority.txt"));
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
			ArrayList<ArrayList<Integer>> finalRetList = getClustersOfWorkfaces_byPriority("workface-distance.txt", 20, "\t",wfPriority, opInfo, workload, distance, machineInitPos);
			for(int i = 0; i < finalRetList.size(); i ++){
				printOutVisualInfo(finalRetList.get(i), workload, opInfo, distance);
			}
			
			s.close();
			System.exit(0);
			
		}
		
		System.out.println("Compute workface cluster by sharing machines?[YES or NO]");
		s = new Scanner(System.in);
		ret = s.nextLine();
		// Finish workload by sharing operating machines.
		if(ret.equalsIgnoreCase("yes")){
			System.out.println("Number of operating machine set?");
			int numbOfMachineSet = Integer.valueOf(s.nextLine());
			String curLine = null;
			br = new BufferedReader(new FileReader("machine-set.txt"));
			LinkedList<String> q = new LinkedList<String>();
			int cntOfLine = 0;
			while((curLine = br.readLine()) != null){
				q.add(curLine);
				cntOfLine ++;
			}
			
			int numOfProc = cntOfLine / numbOfMachineSet;
			
			
			s.close();
			System.exit(0);
		}
		
		
		
		System.out.println("Compute workface cluster by dependancy?[YES or NO]");
		s = new Scanner(System.in);
		ret = s.nextLine();
		if(ret.equalsIgnoreCase("yes")){
			// Read in workface dependancy information
			WorkfaceDependancy wfDependancy = new WorkfaceDependancy();
			br = null;
			
			try{
				String curLine = null;
				br = new BufferedReader(new FileReader("workface-dependancy.txt"));
				while((curLine = br.readLine()) != null){
					
					String[] deArr = curLine.split("\t");
					wfDependancy.addDependancyUnit(Integer.valueOf(deArr[0]), Integer.valueOf(deArr[1]));
					
				}
				
				ArrayList<ArrayList<Integer>> finalRetList = getClustersOfWorkfaces_byDependancy("workface-distance.txt", 20, "\t", wfDependancy, opInfo, workload, distance, machineInitPos);
				
			}catch(IOException e){
				e.printStackTrace();
			}finally{
				if(br != null){
					br.close();
				}
			}
			
			s.close();
			System.exit(0);
		}
		
		
		
	
		
		
		int isSortedOrNot = 1;
		System.out.println("Sort workfaces or not(1 for yes, 2 for No from 1 to 20, 3 for No from 20 to 1):");
		s = new Scanner(System.in);
		String str = s.nextLine();
		if(str.equals("1")){
			isSortedOrNot = 1;
		}else if(str.equals("2")){
			isSortedOrNot = 2;
		}
		else if(str.equals("3")){
			isSortedOrNot = 3;
		}
		else{
			System.err.println("Please choose 1, 2 or 3.");
			System.exit(-1);
		}
		
		System.out.println("Choose the level of parentheses you want to exclude:\n" +
				"	0 No parentheses excluded.\n" +
				"	1 First level parentheses excluded.\n" +
				"	2 Second level parentheses excluded.\n" +
				"	3 Third level parentheses excluded.\n");
		int level = -1;
		level = s.nextInt();
		switch(level){
			case 0:
				ClusterTool.parenClearLevel = ClusterTool.Parentheses.NONE;
				break;
			case 1:
				ClusterTool.parenClearLevel = ClusterTool.Parentheses.FIRST_LEVEL;
				break;
			case 2:
				ClusterTool.parenClearLevel = ClusterTool.Parentheses.SECOND_LEVEL;
				break;
			case 3:
				ClusterTool.parenClearLevel = ClusterTool.Parentheses.THIRD_LEVEL;
				break;
//			case 4:
//				ClusterTool.parenClearLevel = ClusterTool.Parentheses.FORTH_LEVEL;
//				break;
//			case 5:
//				ClusterTool.parenClearLevel = ClusterTool.Parentheses.FIFTH_LEVEL;
//				break;
			default:
					System.out.println("Please choose a valid level number.");
					System.exit(-1);
		}
		System.out.println("Specify the number of machine sets(1, 2 or 3):\n");
		int numOfSet = s.nextInt();
		if(numOfSet < 0 || numOfSet > 3){
			System.err.println("Possible number of machine sets are 1, 2 or 3.");
			System.exit(-1);
		}
		s.close();

		
		
		// Machine 1 and 2 has no workload on workface 5.
//		workload.setWorkloadForMachineOnCertainWf(0, 4, 0);
//		workload.setWorkloadForMachineOnCertainWf(1, 4, 0);
		
		// Machine 1 and 2 has no workload on workface 1.
//		workload.setWorkloadForMachineOnCertainWf(0, 0, 0);
//		workload.setWorkloadForMachineOnCertainWf(1, 0, 0);
		
		// Machine 1 and 2 has no workload on workface 1.
		workload.setWorkloadForMachineOnCertainWf(0, 0, 0);
		workload.setWorkloadForMachineOnCertainWf(1, 0, 0);
		// Machine 1 and 2 has no workload on workface 6.
		workload.setWorkloadForMachineOnCertainWf(0, 5, 0);
		workload.setWorkloadForMachineOnCertainWf(1, 5, 0);
		
		
		
		// Print out workface workoad information
//		StringBuilder msgOutputWorkload = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
//		msgOutputWorkload.append("\n=========Workface Workload==========\n");
//		msgOutputWorkload.append(workload.OutputWorkload());
//		LogTool.log(LEVEL, msgOutputWorkload.toString());

		// Start cluster & sort workface  *********************************************************
//		ArrayList<Integer> ds = null;
		ArrayList<ArrayList<Integer>> dss = null;
		if(isSortedOrNot == 1){
			if(numOfSet == 1){
				dss = ClusterTool.getClustersOfWorkfaces_zhen_new("workface-distance.txt", 20, "\t", opInfo, workload, distance, machineInitPos);
			}else{
				dss = ClusterTool.getClustersOfWorkfaces_zhen_new2(numOfSet, "workface-distance.txt", 20, "\t", opInfo, workload, distance, machineInitPos);
			}
			 
		}else{
//			ds = new ArrayList<Integer>();
//			if(isSortedOrNot == 3)
//				for(int iRandom = 0; iRandom < 20; iRandom ++){
//					ds.add(20 - iRandom );
//				}
//			else
//				for(int iRandom = 0; iRandom < 20; iRandom ++){
//					ds.add(iRandom + 1);
//				}
		}

		for(int i = 0; i < dss.size(); i ++){
			printOutVisualInfo(dss.get(i), workload, opInfo, distance);
		}
    	
	}// end of method main
	
	/**
	 * 
	 * @param ds
	 * @param workload
	 * @param opInfo
	 * @param distance
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
