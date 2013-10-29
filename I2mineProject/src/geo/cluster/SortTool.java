package geo.cluster;

import geo.core.MachineInitialPosition;
import geo.core.MachineOpInfo;
import geo.core.WorkfaceDistance;
import geo.core.WorkfaceWorkload;
import geo.util.LogTool;

import java.util.ArrayList;

import net.sf.javaml.core.Dataset;

/**
 * This class provides functions for computing machines' operating time, sorting workfaces and workface groups, etc.
 * 
 * @author Dong
 * @version 1.0
 */
public class SortTool {
	
	// Determine if output the debug info or not
	public static int LEVEL = -1;
	
	/**
	 * Static nested class WorkfaceGroup to ease the permutation of groups of sorted workfaces.
	 * @author Dong
	 *
	 */
	static class WorkfaceGroup {
		// Sorted list of workfaces of current group
		ArrayList<Integer> groupOfSortedWorkfaces = null;
		// Total operating and moving time of current sorted list of workfaces
		double totalWorkTime = 0.0;
	}
	
	/**
	 * Given one sorted workfaces in a region (w1, w2, w3), compute the machine time interval for each machine. 
	 * @param sortedWorkfacesInOneRegion orted workfaces in one region
	 * @param machineOpInfo Machine operating information
	 * @param workload Workload for each machine in each workface
	 * @param distance Distances between each pair of workfaces
	 * @return Time interval for all machines in current region
	 */
	
	public static ArrayList<ArrayList<Double>> computeMachineTimeIntervalInOneRegion(ArrayList<Integer> sortedWorkfacesInOneRegion, MachineOpInfo machineOpInfo, WorkfaceWorkload workload, WorkfaceDistance distance){
		int numberOfMachine = machineOpInfo.getMachineNum();
		// storing time intervals for all machines e.g. w1, w1->w3, w3, w3->w2, w2
		ArrayList<ArrayList<Double>> machineTimeInterval = new ArrayList<ArrayList<Double>>(); 
		// compute time interval for each machine in all workfaces in current region
		for(int m = 0; m < numberOfMachine;m++){
			// OR and MR
			ArrayList<Double> curOpInfo = machineOpInfo.getCertainMachineOpInfo(m);
			ArrayList<Double> curWorkload = workload.getWorkloadOfMachine(m);// Contain operate info and move info
			ArrayList<Double> curTimeInterval = new ArrayList<Double>();
			ArrayList<Double> curWaitTime = new ArrayList<Double>();
			
			// time interval of previous machine in current region
			// in the format of "pro_time, mov_time [, WAIT_time], pro_time, mov_time[, WAIT_time] ..." 
//			ArrayList<Double> preMachineTimeInterval = null;
//			ArrayList<Double> preWaitTime = null;
//			if(m != 0){
//				preMachineTimeInterval = machineTimeInterval.get((m - 1) * 2);
//				preWaitTime = machineTimeInterval.get((m - 1) * 2 + 1);
//			}
			
			// Register log info
			StringBuilder msgSortedWf = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
			msgSortedWf.append("\n=============SORTED workfaces:=============\n");
			for(int sw = 0; sw < sortedWorkfacesInOneRegion.size(); sw++)
				msgSortedWf.append(sortedWorkfacesInOneRegion.get(sw)+"  ");
			msgSortedWf.append("\n");
			LogTool.log(LEVEL, msgSortedWf.toString());
			
			for(int w = 0; w < sortedWorkfacesInOneRegion.size() - 1; w++){
				
				// sortedWorkfacesInOneRegion.get(w) gets the real index for workface, e.g., 1, 2, 3, ..., 20
				// The needed index is 0 based
				double curProTime = curWorkload.get(sortedWorkfacesInOneRegion.get(w) - 1)/curOpInfo.get(0);
				double curMovTime = distance.getDistBetweenTwoWorkfaces(
						sortedWorkfacesInOneRegion.get(w) - 1, sortedWorkfacesInOneRegion.get(w + 1) - 1)/curOpInfo.get(1);
//				double timeOfCur = curProTime + curMovTime;
				// processing time from 1st procedure to last - 1 procedure
				curTimeInterval.add(curProTime);
				// moving time
				curTimeInterval.add(curMovTime);
				// test whether WAIT time needs to be added
				if(m == 0){
					curWaitTime.add(0.0);
				}else{
					// No wait time before first workface of a permutation
//					if(w != 0){
						
						// Compute previous machines' time
						double timeOfPres = 0, timeOfCurs = 0;
						for(int k = 0; k < m; k ++){
							// Only take the first 
							if(k != m - 1){
								timeOfPres += machineTimeInterval.get(2 * k).get(0); // Only add the operational time for first workface
							}else{
								ArrayList<Double> preMachineTimeInterval = machineTimeInterval.get(2 * k); // Get operational time of machine k
								ArrayList<Double> preWaitTime = machineTimeInterval.get(2 * k + 1); // Get wait time of machine k
								for(int j = 0; j <= w + 1; j ++){
									if(j != w + 1){
										timeOfPres += preMachineTimeInterval.get(j * 2); // Operational time
										
										if(j == 0){
											timeOfCurs = timeOfPres;
										}
										
										timeOfPres += preMachineTimeInterval.get(j * 2 + 1); // Moving time
										timeOfPres += preWaitTime.get(j); // Wait time
									}
									// Only add operational time, no move time, no wait time added
									else{
										timeOfPres +=  preMachineTimeInterval.get(j * 2); // Operational time
									}
								}// end for
							}
						}// end for
						
						// Compute current machines' time
						for(int k = 0; k <= w; k ++){
							if(k != w){
//								System.out.println("k:" + k + " w:" + w + " m:" + m);
								timeOfCurs += curTimeInterval.get(k * 2); // Add operational time
								timeOfCurs += curTimeInterval.get(k * 2 + 1); // Add moving time
								timeOfCurs += curWaitTime.get(k); // Add waiting time
							}
							else{
								timeOfCurs += curTimeInterval.get(k * 2); // Add operational time
								timeOfCurs += curTimeInterval.get(k * 2 + 1); // Add moving time
							}
						}
						
						//Wait time is needed
						if(timeOfPres > timeOfCurs){
							curWaitTime.add(timeOfPres - timeOfCurs);
						}
						// No wait time is needed
						else{
							curWaitTime.add(0.0);
						}
						
//					}
						
					
					
					
//					int indexOfPre = (w + 1) * 2;
//					double timeOfPre = preMachineTimeInterval.get(indexOfPre) + preMachineTimeInterval.get(indexOfPre - 1);  
//					timeOfPre += preWaitTime.get(w);
//					
//					// WAIT time is needed
//					if(timeOfPre > timeOfCur){
//						curWaitTime.add(timeOfPre - timeOfCur);
//					}
//					// No WAIT time is needed, can start process right away
//					else{
//						curWaitTime.add(0.0);
//					}
				}
				
			}// end for(w)
			// processing time for last procedure
			curTimeInterval.add(curWorkload.get(sortedWorkfacesInOneRegion.get(sortedWorkfacesInOneRegion.size() - 1) - 1)/curOpInfo.get(0));
			
			// add time interval for current machine into machineTimeInterval
			machineTimeInterval.add(curTimeInterval);
			machineTimeInterval.add(curWaitTime);
		}// end for(m)
		
		// Register log info -- print out machineTimeInterval in the format of "time_interval, wait_time [, time_interval, wait_time]"
		StringBuilder msgMachineTimeInterval = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
		for(int n = 0; n < machineTimeInterval.size(); n++){
			
			if(n % 2 == 0){
				msgMachineTimeInterval.append("MACHINE: "+ n / 2 +"\nTime_interval: \n");
			}else{
				msgMachineTimeInterval.append("Wait_time:\n");
			}
			msgMachineTimeInterval.append(machineTimeInterval.get(n) + "\n");
		}
		LogTool.log(LEVEL, msgMachineTimeInterval.toString());
		return machineTimeInterval;
	}
	
	
	/**
	 * Sort groups of sorted workfaces to determine the operating order of workface groups in terms of minimum operating time.
	 * @param sortedWorkfaces  A list of workface groups. Workfaces in Each group has already been sorted.
	 * @param machineOpInfo Machine operation information including machine operating rate and moving rate.
	 * @param workload Workload for all machines on all workfaces.
	 * @param distance Distance matrix of all workfaces.
	 * @return The list of sorted groups of workfaces with the minimum operating time.
	 */
	public static ArrayList<ArrayList<Integer>> sortGroups_new(ArrayList<ArrayList<Integer>> sortedWorkfaces, MachineOpInfo machineOpInfo, WorkfaceWorkload workload, WorkfaceDistance distance){
		
		ArrayList<WorkfaceGroup> wgList = new ArrayList<WorkfaceGroup>();
		for(int i = 0; i < sortedWorkfaces.size(); i ++){
			WorkfaceGroup wg = new WorkfaceGroup();
			wg.groupOfSortedWorkfaces = sortedWorkfaces.get(i);
			wgList.add(wg);
		}
		
		// Get all the permutations of all the groups
		ArrayList<ArrayList<WorkfaceGroup>> retList = new ArrayList<ArrayList<WorkfaceGroup>>();
		permuteGroup(wgList, 0, wgList.size() - 1, retList);
//		System.out.println("=========permutated group of workface=======");
//		for(int i = 0; i < retList.size(); i ++){
//			for(int j = 0; j < retList.get(i).size(); j ++){
//				for(int k = 0; k < retList.get(i).get(j).groupOfSortedWorkfaces.size(); k ++){
//					System.out.print(retList.get(i).get(j).groupOfSortedWorkfaces.get(k) + " ");
//				}
//				System.out.println();
//			}
//		}
		
		// Compute all the operating time (optionally with waiting time) for each permutation
		double minTotalTime = Double.MAX_VALUE;
		ArrayList<ArrayList<Integer>> minGroupList = new ArrayList<ArrayList<Integer>>();
		//Iterate over each group permutation
		for(int i = 0; i < retList.size(); i ++){
			// Process current group permutation
			ArrayList<WorkfaceGroup> curGroupPerm = retList.get(i);
			ArrayList<Integer> curWorkfaceList = new ArrayList<Integer>(); 
			// Total time for all machines to finish all sorted workfaces in current permutation
			double curTotalTime = 0.0;
			for(int j = 0; j < curGroupPerm.size(); j ++){
				curWorkfaceList.addAll(curGroupPerm.get(j).groupOfSortedWorkfaces);
			}
			
			// regionTimeInfo in the form of "M1TimeInterval, M1WaitTime, M2TimeInterval, M2WaitTime,...."
			ArrayList<ArrayList<Double>> curRegionTime = computeMachineTimeIntervalInOneRegion
					(curWorkfaceList, machineOpInfo, workload, distance);
			

			curTotalTime = computeOperatingTimeOfWorkfaceList(curRegionTime);
			
			//System.out.println();			
			//Log information
			StringBuilder msgMachineTimeInterval = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
			msgMachineTimeInterval.append("<sortGroups_new>cur total time:" + curTotalTime + "\n<sortGroups_new>cur sort group:");
			for(int k = 0; k < curWorkfaceList.size(); k ++){
				msgMachineTimeInterval.append(curWorkfaceList.get(k) + " ");
			}
			LogTool.log(LEVEL, msgMachineTimeInterval.toString());
			
			
			if(curTotalTime < minTotalTime){
				minTotalTime = curTotalTime;
				minGroupList.clear();
				for(int l = 0; l < curGroupPerm.size(); l ++){
					
					minGroupList.add(curGroupPerm.get(l).groupOfSortedWorkfaces);
				}
			}
		}
		
//		System.out.println("============minimum time=============\n" + minTotalTime + "\n");
//		for(int i = 0; i < minGroupList.size(); i ++){
//			for(int j =0; j < minGroupList.get(i).size(); j ++){
//				System.out.print(minGroupList.get(i).get(j) + " ");
//			}
//			System.out.println();
//		}
		
		return minGroupList;
	}
	
	/**
	 * 
	 * @param curRegionTime
	 * @return
	 */
	public static double computeOperatingTimeOfWorkfaceList(ArrayList<ArrayList<Double>> curRegionTime){
		double curTotalTime = 0.0;
		//============================================================================
//		if(curWorkfaceList.size() == 20){
//			System.out.println("20 workfaces:");
//			for(int kk = 0; kk < curWorkfaceList.size(); kk ++){
//				System.out.print(curWorkfaceList.get(kk) + " ");
//			}
//		}System.out.println();
		
		
		// Sum each machine's first operating time on each workface
		for(int k = 0; k * 2 < curRegionTime.size() - 2; k ++){
			curTotalTime += curRegionTime.get(k * 2).get(0);
			
//			//============================================================================
//			if(curWorkfaceList.size() == 20){
//				if(k == 0)
//					System.out.print("op time: ");
//				System.out.print(curRegionTime.get(k * 2).get(0) + " ");
//			}
		}
		//System.out.println();
		
		// Sum last machine's operating time
		ArrayList<Double> lastMachineTimeInterval = curRegionTime.get(curRegionTime.size() - 2);
		for(int m = 0; m < lastMachineTimeInterval.size(); m ++){
			curTotalTime += lastMachineTimeInterval.get(m);
			
//			//============================================================================
//			if(curWorkfaceList.size() == 20){
//				if(m == 0)
//					System.out.print("last op/mv time: ");
//				System.out.print(lastMachineTimeInterval.get(m) + " ");
//			}
		}
		//System.out.println();
		
		// Sum last machine's waiting time
		ArrayList<Double> lastMachineWaitTime = curRegionTime.get(curRegionTime.size() - 1);
		for(int n = 0; n < lastMachineWaitTime.size(); n ++){
			curTotalTime += lastMachineWaitTime.get(n);
		}
		return curTotalTime;
	}
		
	
	private static void swapGroup(ArrayList<WorkfaceGroup> wgList, int from, int to){
		WorkfaceGroup tmp = wgList.get(from);
		wgList.set(from, wgList.get(to));
		wgList.set(to, tmp);
	}
	
	/**
	 * Permutate a list of groups of workfaces to get all possible group permutations and store them into <i>retList</i>
	 * @param wgList A list of groups of workfaces to be sorted.
	 * @param left The starting position of the list of groups of workfaces to be permutated.
	 * @param right The end position of the list of groups of workfaces to be permutated.
	 * @param retList A list which stores all the possible group permutations.
	 */
	private static void permuteGroup(ArrayList<WorkfaceGroup> wgList, int left, int right, ArrayList<ArrayList<WorkfaceGroup>> retList){
		if(left == right){
			ArrayList<WorkfaceGroup> tmpList = new ArrayList<WorkfaceGroup>(wgList);
			retList.add(tmpList);
		}
		
		for(int i = left; i <= right; i ++){
			swapGroup(wgList, left, i);
			permuteGroup(wgList, left + 1, right, retList);
			swapGroup(wgList, left, i);
		}
	}
	
	/**
	 * Sort groups of sorted workfaces to determine the operating order of workface groups in terms of minimum operating time.
	 * @param sortedWorkfaces A list of workface groups. Workfaces in Each group has already been sorted.
	 * @param machineOpInfo Machine operation information including machine operating rate and moving rate.
	 * @param workload Workload for all machines on all workfaces.
	 * @param distance Distance matrix of all workfaces.
	 * @return The list of sorted groups of workfaces with the minimum operating time.
	 */
	@Deprecated
	public static ArrayList<ArrayList<Integer>> sortGroups(ArrayList<ArrayList<Integer>> sortedWorkfaces, MachineOpInfo machineOpInfo, WorkfaceWorkload workload, WorkfaceDistance distance){
		
		ArrayList<ArrayList<Integer>> sortGroups = new ArrayList<ArrayList<Integer>>();
		int numberOfMachine = machineOpInfo.getMachineNum();
		
		// iterate through each pair of regions
		for(int region11 = 0; region11 < sortedWorkfaces.size() - 1; region11 ++){
			// regionTimeInfo in the form of "M1TimeInterval, M1WaitTime, M2TimeInterval, M2WaitTime,...."
			ArrayList<ArrayList<Double>> regionTimeInfo11 = computeMachineTimeIntervalInOneRegion
					(sortedWorkfaces.get(region11), machineOpInfo, workload, distance);
			
			System.out.println("=======region time info======");
			System.out.println(regionTimeInfo11.size());
			
			// Register log info
			StringBuilder msgCurSortWf1 = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
			msgCurSortWf1.append("==========current sorted workfaces 1 in <sortGroups>==========\n");
			msgCurSortWf1.append("workfaces(ArrayList<Integer>) -- region11 value: " + region11 + ": " + sortedWorkfaces.get(region11) + "\n");
			LogTool.log(LEVEL, msgCurSortWf1.toString());
			
			// ***************compute MACHINE TIME for region11***************
			ArrayList<Double> regionTime11 = new ArrayList<Double>(numberOfMachine);
			for(int r11 = 0; r11 < numberOfMachine; r11 ++){
				double tmpMachineTime = 0;
				// compute processing time and moving time for machine r11 for all the procedures
				for(int i = 0; i < regionTimeInfo11.get(r11 * 2).size(); i++){
					tmpMachineTime += regionTimeInfo11.get(r11 * 2).get(i);
				}
				// compute wait time for machine r11 for all the procedures
				for(int j = 0; j < regionTimeInfo11.get(r11 * 2 + 1).size(); j++){
					tmpMachineTime += regionTimeInfo11.get(r11 * 2 + 1).get(j);
				}
				regionTime11.add(tmpMachineTime);
			}
			
			
			// Register log info
			StringBuilder msgCurRegionTime1 = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString()); 
			msgCurRegionTime1.append("=================current regionTime 1 ==============\n");
			msgCurRegionTime1.append("Original region time number:"+regionTimeInfo11.size() + "\n");
			msgCurRegionTime1.append(regionTime11 + "\n");
			LogTool.log(LEVEL, msgCurRegionTime1.toString());
			
			for(int region12 = region11 + 1; region12 < sortedWorkfaces.size(); region12 ++){
				// regionTimeInfo in the form of "M1TimeInterval, M1WaitTime, M2TimeInterval, M2WaitTime,...."
				ArrayList<ArrayList<Double>> regionTimeInfo12 = computeMachineTimeIntervalInOneRegion
						(sortedWorkfaces.get(region12), machineOpInfo, workload, distance);
				
				// Register log info
				StringBuilder msgCurSortedWf2 = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
				msgCurSortedWf2.append("==========current sorted workfaces 2 in <sortGroups>==========\n");
				msgCurSortedWf2.append("Original region time number:"+regionTimeInfo12.size() + "\n");
				msgCurSortedWf2.append(sortedWorkfaces.get(region12) + "\n");
				LogTool.log(LEVEL, msgCurSortedWf2.toString());
				
				// ***************compute MACHINE TIME for region12***************
				ArrayList<Double> regionTime12 = new ArrayList<Double>(numberOfMachine);
				for (int r12 = 0; r12 < numberOfMachine; r12 ++){
					double tmpMachineTime = 0;
					// compute processing time and moving time for machine r12 for all the procedures
					for(int i = 0; i < regionTimeInfo12.get(r12 * 2).size(); i++){
						tmpMachineTime += regionTimeInfo12.get(r12 * 2).get(i);
					}
					// compute wait time for machine r12 for all the procedures
					for(int j = 0; j < regionTimeInfo12.get(r12 * 2 + 1).size(); j++){
						tmpMachineTime += regionTimeInfo12.get(r12 * 2 + 1).get(j);
					}
					regionTime12.add(tmpMachineTime);
				}
				
				// Register log info
				StringBuilder msgCurRegionTime2 = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
				msgCurRegionTime2.append("=================current regionTime 2 ==============\n");
				msgCurRegionTime2.append(regionTime12 + "\n");
				LogTool.log(LEVEL, msgCurRegionTime2.toString());
				
				// ************** start processing region1 -> region2********************
				int lastWorkfaceOfRegion11 = sortedWorkfaces.get(region11).get(sortedWorkfaces.get(region11).size() - 1);
				int firstWorkfaceOfRegion12 = sortedWorkfaces.get(region12).get(0);
				double dist11_12 = distance.getDistBetweenTwoWorkfaces(lastWorkfaceOfRegion11, firstWorkfaceOfRegion12);
				
				// Register log info
				StringBuilder msgDistRegionTime1 = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
				msgDistRegionTime1.append("DISTANCE between(1->2): "+lastWorkfaceOfRegion11+"------->"+firstWorkfaceOfRegion12 + "\n");
				msgDistRegionTime1.append("Distance is(1->2): "+ dist11_12 + "\n");
				msgDistRegionTime1.append("Region11 time: "+regionTime11 + "\n");
				
				msgDistRegionTime1.append("tmpTotalTime11_12 value: ");
				msgDistRegionTime1.append("\nEach step of each tmpTotalTime11_12: " + "\n");
				LogTool.log(LEVEL, msgDistRegionTime1.toString());
				
				double maxTotalTime11_12 = 0, tmpTotalTime11_12 = 0;
				for(int sep1 = 0; sep1 < numberOfMachine; sep1 ++){
					
					tmpTotalTime11_12 += regionTime11.get(0);
					for(int m1 = 1; m1 <= sep1; m1 ++){
						tmpTotalTime11_12 += regionTimeInfo11.get(m1 * 2).get(regionTimeInfo11.get(m1 * 2).size() - 1);
					}
					
					tmpTotalTime11_12 += regionTime11.get(sep1);
					for(int m2 = sep1 + 1; m2 < numberOfMachine; m2++){
						tmpTotalTime11_12 += regionTimeInfo11.get(m2 * 2).get(regionTimeInfo11.get(m2 * 2).size() - 1);
					}

					tmpTotalTime11_12 += dist11_12 / machineOpInfo.getCertainMachineOpInfo(sep1).get(1);
					
					maxTotalTime11_12 = (maxTotalTime11_12 > tmpTotalTime11_12) ? maxTotalTime11_12 : tmpTotalTime11_12; 
					tmpTotalTime11_12 = 0;
				}// sep1
				System.out.println();
				
				// ************** start processing region2 -> region1********************
				int lastWorkfaceOfRegion21 = sortedWorkfaces.get(region12).get(sortedWorkfaces.get(region12).size() - 1);
				int firstWorkfaceOfRegion22 = sortedWorkfaces.get(region11).get(0);
				double dist21_22 = distance.getDistBetweenTwoWorkfaces(lastWorkfaceOfRegion21, firstWorkfaceOfRegion22);
				
				// Register log info
				StringBuilder msgDistRegionTime2 = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
				msgDistRegionTime2.append("\nDISTANCE between(2->1): "+lastWorkfaceOfRegion21+"------->"+firstWorkfaceOfRegion22 + "\n");
				msgDistRegionTime2.append("Distance is(2->1): "+ dist21_22 + "\n");
				msgDistRegionTime2.append("Region12 time: "+regionTime12 + "\n");
				
				msgDistRegionTime2.append("tmpTotalTime12_11 value: ");
				double maxTotalTime12_11 = 0, tmpTotalTime12_11 = 0;
				msgDistRegionTime2.append("\nEach step of each tmpTotalTime12_11: \n");
				LogTool.log(LEVEL, msgDistRegionTime2.toString());
				
				for(int sep2 = 0; sep2 < numberOfMachine; sep2 ++){
					
					tmpTotalTime12_11 += regionTime12.get(0);
					for(int m1 = 1; m1 <= sep2; m1 ++){
						tmpTotalTime12_11 += regionTimeInfo12.get(m1 * 2).get(regionTimeInfo12.get(m1 * 2).size() - 1);
					}
					tmpTotalTime12_11 += regionTime12.get(sep2);
					for(int m2 = sep2 + 1; m2 < numberOfMachine; m2 ++){
						tmpTotalTime12_11 += regionTimeInfo12.get(m2 * 2).get(regionTimeInfo12.get(m2 * 2).size() - 1);
					}
					
					tmpTotalTime12_11 += dist21_22 / machineOpInfo.getCertainMachineOpInfo(sep2).get(1);
					maxTotalTime12_11 = (maxTotalTime12_11 > tmpTotalTime12_11) ? maxTotalTime12_11 : tmpTotalTime12_11;
					tmpTotalTime12_11 = 0;
				}
				System.out.println();
				
				StringBuilder msgCurRegionPair = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
				msgCurRegionPair.append("\n===============================current region pair===============================\n");
				msgCurRegionPair.append("WORKFACE: "+sortedWorkfaces.get(region11)+"------>"+sortedWorkfaces.get(region12) + "\n");
				msgCurRegionPair.append("TIME DURATION: "+maxTotalTime11_12+"------->"+maxTotalTime12_11 + "\n");
				
				// region2 should be ahead of region1
				boolean containsRegion11 = sortGroups.contains(sortedWorkfaces.get(region11));
				boolean containsRegion12 = sortGroups.contains(sortedWorkfaces.get(region12));
				msgCurRegionPair.append("containsRegion11: " + containsRegion11 + " containsRegion12: " + containsRegion12 + "\n");
				msgCurRegionPair.append("indexOf region11: " + sortGroups.indexOf(sortedWorkfaces.get(region11)) + 
						" indexOf region12: " + sortGroups.indexOf(sortedWorkfaces.get(region12)) + "\n");
				LogTool.log(LEVEL, msgCurRegionPair.toString());
				
				if(maxTotalTime12_11 < maxTotalTime11_12){
					if(containsRegion11 == true){
						if(containsRegion12 == true){
							int indexOf1 = sortGroups.indexOf(sortedWorkfaces.get(region11));
							int indexOf2 = sortGroups.indexOf(sortedWorkfaces.get(region12));
							if(indexOf1 < indexOf2){
								sortGroups.remove(sortedWorkfaces.get(region12));
								sortGroups.add(sortGroups.indexOf(sortedWorkfaces.get(region11)), sortedWorkfaces.get(region12));
							}
						}else{
							sortGroups.add(sortGroups.indexOf(sortedWorkfaces.get(region11)), sortedWorkfaces.get(region12));
						}
					}else{
						if(containsRegion12 == true){
							sortGroups.add(sortedWorkfaces.get(region11));
						}else{
							sortGroups.add(sortedWorkfaces.get(region12));
							sortGroups.add(sortedWorkfaces.get(region11));
						}
					}
				}
				// region1 should be ahead of region2
				else{
					if(containsRegion11 == true){
						if(containsRegion12 == true){
							int indexOf1 = sortGroups.indexOf(sortedWorkfaces.get(region11));
							int indexOf2 = sortGroups.indexOf(sortedWorkfaces.get(region12));
							if(indexOf2 < indexOf1){
								sortGroups.remove(sortedWorkfaces.get(region11));
								sortGroups.add(sortGroups.indexOf(sortedWorkfaces.get(region12)), sortedWorkfaces.get(region11));
							}
						}else{
							sortGroups.add(sortedWorkfaces.get(region12));
						}
					}else{
						if(containsRegion12 == true){
							sortGroups.add(sortGroups.indexOf(sortedWorkfaces.get(region12)), sortedWorkfaces.get(region11));
						}else{
							sortGroups.add(sortedWorkfaces.get(region11));
							sortGroups.add(sortedWorkfaces.get(region12));
						}
					}
				}// for - region12
			}
		}// for - region11
		
		// Register log info
		StringBuilder msgPrintSortGroups = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
		msgPrintSortGroups.append("\n=============print out sort groups===============\n");
		for(int sg = 0; sg < sortGroups.size(); sg ++){
			msgPrintSortGroups.append(sortGroups.get(sg) + "\n");
		}
		LogTool.log(LEVEL, msgPrintSortGroups.toString());
		
		
		return sortGroups;
	}
	
	
	/**
	 * A utility function to swap two workfaces in a list of workfaces.
	 * @param arr A list of workfaces.
	 * @param i One workface to be changed into j-th workface in the original workface list <i>arr</i>.
	 * @param j One workface to be changed into i-th workface in the original workface list <i>arr</i>.
	 */
	private static void swap (ArrayList<Integer> arr, int i, int j){
		int tmp = arr.get(j);
		arr.set(j, arr.get(i));
		arr.set(i, tmp);
		
	}
	
	/**
	 * Permutate a list of workface to get all possible workface permutations and store them into <i>permList</i>
	 * @param arr A list of workfaces to be permutated.
	 * @param left The starting position of the workface list to be permutated.
	 * @param right The end position of the workface list to be permutated.
	 * @param permList A list which stores all the possible workface permutations.
	 */
	private static void permute(ArrayList<Integer> arr, int left, int right, ArrayList<ArrayList<Integer>> permList){
		if(left == right){
			ArrayList<Integer> tmpList = new ArrayList<Integer>(arr);
			permList.add(tmpList);
		}
		
		for(int i = left; i <= right; i ++){
			swap(arr, left, i);
			permute(arr, left + 1, right, permList);
			swap(arr, left, i);
		}
	}
	/**
	 * Get the path with the maximum time for one workface permutation
	 * @param matrix Matrix recording operating and moving time for each machine (column) on each workface (row).
	 * @param col Current column to be calculated.
	 * @param row Current row to be calculated.
	 * @return The maximum time for one workface permutation.
	 */
	private static Double getMaxTime(ArrayList<ArrayList<Double>> matrix, int col, int row, int flag){
		if(col == 0 && row == 0){
			return matrix.get(row).get(col);
		}else{
			double sum1 = 0;
			if(col - 1 >= 0) sum1 = getMaxTime(matrix, col - 1, row, flag);
			double sum2 = 0;
			if(row - 1 >= 0) sum2 = getMaxTime(matrix, col, row - 1, flag);
//			if(flag == 1){
//				if(sum1 > sum2)
//					System.out.println((matrix.get(row).get(col - 1) + " " + matrix.get(row).get(col)));
//				else
//					System.out.println((matrix.get(row - 1).get(col) + " " + matrix.get(row).get(col)));
//			}
			
			return (Math.max(sum1,  sum2) + matrix.get(row).get(col));
		}
	}
	
	/**
	 * Compute the maximum operating and moving time for one workface permutation.
	 * @param matrix Matrix recording operating and moving time for each machine (column) on each workface (row).
	 * @return The maximum time for one workface permutation.
	 */
	private static double computeMaxWfSeqTime(ArrayList<ArrayList<Double>> matrix, int flag){
		return getMaxTime(matrix, matrix.get(0).size() - 1, matrix.size() - 1, 0);
	}
	
	/**
	 *  Sort workfaces in traditional manner.
	 * @param data
	 * @param clusterGroups1
	 * @param machineOpInfo
	 * @param workload
	 * @param distance
	 * @return
	 */
	public static ArrayList<ArrayList<Integer>> sortWorkfaces_new1 (Dataset[] data, ArrayList<ArrayList<Integer>> clusterGroups1, MachineOpInfo machineOpInfo, WorkfaceWorkload workload, WorkfaceDistance distance){
		ArrayList<ArrayList<Integer>> sortGroups = new ArrayList<ArrayList<Integer>>();
		
		// The number of groups after clustering
		int len = clusterGroups1.size();
		
		// Iterate through each cluster group, e.g. (w1, w2, w3), (w4, w6), (w5, w7)
		for(int i = 0; i < len; i++){
			
			ArrayList<ArrayList<Integer>> permList = new ArrayList<ArrayList<Integer>>();
			ArrayList<Integer> curWfList = clusterGroups1.get(i);
			permute(curWfList, 0, curWfList.size() - 1, permList);
			// For each permutation of the workface sequence, e.g. w1 w2 w3, w1 w3 w2, w2 w1 w3, w2 w3 w1...(in total 6 permutations for 3 workfaces)
			// Compute the total time used for operating and moving
			double minTotalTime = Double.MAX_VALUE;
			ArrayList<Integer> minPermList = new ArrayList<Integer>();
			
			for(int j = 0; j < permList.size(); j ++){
				ArrayList<Integer> curPermList = permList.get(j);
				double curTotalTime = 0;
				// regionTimeInfo in the form of "M1TimeInterval, M1WaitTime, M2TimeInterval, M2WaitTime,...."
				ArrayList<ArrayList<Double>> curRegionTime = computeMachineTimeIntervalInOneRegion
						(curPermList, machineOpInfo, workload, distance);
				
				// Sum each machine's first operating time on each workface
				for(int k = 0; k * 2 < curRegionTime.size() - 2; k ++){
					curTotalTime += curRegionTime.get(k * 2).get(0);
				}
				// Sum last machine's operating time
				ArrayList<Double> lastMachineTimeInterval = curRegionTime.get(curRegionTime.size() - 2);
				for(int m = 0; m < lastMachineTimeInterval.size(); m ++){
					curTotalTime += lastMachineTimeInterval.get(m);
				}
				// Sum last machine's waiting time
				ArrayList<Double> lastMachineWaitTime = curRegionTime.get(curRegionTime.size() - 1);
				for(int n = 0; n < lastMachineWaitTime.size(); n ++){
					curTotalTime += lastMachineWaitTime.get(n);
				}
				
//				if(curPermList.contains(17) && curPermList.contains(18) && curPermList.contains(19) && curPermList.contains(20)){
//					System.out.println("------------&&&&&-------------");
//					for(int k = 0; k < curPermList.size(); k ++)
//						System.out.print(curPermList.get(k) + " ");
//					System.out.println("\n cur total time: " + curTotalTime);
//				}
//				if(curPermList.contains(12) && curPermList.contains(10) && curPermList.contains(11)){
//					System.out.println("------------&&&&&-------------");
//					for(int k = 0; k < curPermList.size(); k ++)
//						System.out.print(curPermList.get(k) + " ");
//					System.out.println("\n cur total time: " + curTotalTime);
//				}
//				if(curPermList.contains(12) && curPermList.contains(10) && curPermList.contains(11) && curPermList.contains(9) && curPermList.contains(8) && curPermList.contains(7) && curPermList.contains(6) && curPermList.contains(5) && curPermList.contains(4)){
////					System.out.println("------------&&&&&-------------");
//					StringBuilder sb = new StringBuilder();
//					for(int k = 0; k < curPermList.size(); k ++)
//						//System.out.print(curPermList.get(k) + " ");
//						sb.append(curPermList.get(k) + " ");
//					if(sb.toString().equals("5 6 7 4 8 9 12 10 11 ") || sb.toString().equals("6 5 4 9 8 7 12 10 11 ") || sb.toString().equals("4 6 5 7 8 9 10 11 12 ") || sb.toString().equals("4 6 5 7 8 9 11 10 12 ")){
//						System.out.println(sb.toString() + "\n cur total time: " + curTotalTime);
//					}
//					
//				}
				
				if(curTotalTime < minTotalTime){
					minTotalTime = curTotalTime;
					minPermList.clear();
					minPermList.addAll(curPermList);
				}
			}
			sortGroups.add(minPermList);
		}
		return sortGroups;
	}
	
	/**
	 * Sort workfaces using matrix.
	 * @param data
	 * @param clusterGroups1
	 * @param machineOpInfo
	 * @param workload
	 * @return
	 */
	public static ArrayList<ArrayList<Integer>> sortWorkfaces_new (Dataset[] data, ArrayList<ArrayList<Integer>> clusterGroups1, MachineOpInfo machineOpInfo, WorkfaceWorkload workload, MachineInitialPosition machineInitPos){
		
		ArrayList<ArrayList<Integer>> sortGroups = new ArrayList<ArrayList<Integer>>();
		
		// The number of groups after clustering
		int len = clusterGroups1.size();
		
		// The number of procedures in each work face
		int numOfProcedure = machineOpInfo.getMachineNum();
		
		// Iterate through each cluster group, e.g. (w1, w2, w3), (w4, w6), (w5, w7)
		for(int i = 0; i < len; i++){
			
			ArrayList<ArrayList<Integer>> permList = new ArrayList<ArrayList<Integer>>();
			ArrayList<Integer> curWfList = clusterGroups1.get(i);
			permute(curWfList, 0, curWfList.size() - 1, permList);
			// For each permutation of the workface sequence, e.g. w1 w2 w3, w1 w3 w2, w2 w1 w3, w2 w3 w1...(in total 6 permutations for 3 workfaces)
			// Compute the total time used for operating and moving
			double minTime = Double.MAX_VALUE;
			ArrayList<Integer> minPermList = null;
			
			for(int j = 0; j < permList.size(); j ++){
				ArrayList<Integer> curPermList = permList.get(j);
				
				// Initiate operating time and moving time matrix
				ArrayList<ArrayList<Double>> matrix = new ArrayList<ArrayList<Double>>();
				
				for(int k = 0; k < curPermList.size(); k ++){
					ArrayList<Double> curWfTotalTime = new ArrayList<Double>();
					for(int m = 0; m < numOfProcedure; m ++){
						
						// Get workload of machine m on workface k
						double curMachineWorkload = workload.getWorkloadOfMachine(m).get(curPermList.get(k) - 1);
						if(curMachineWorkload == 0){
							curWfTotalTime.add(0.0);
						}else{
							double dist = 0;
							if(k > 0){
								int tmpi = k - 1; 
								for(; tmpi >=0; tmpi -- ){
									if(matrix.get(tmpi).get(m) == 0){
										continue;
									}else
										break;
								}
								if(tmpi == -1){
									dist = data[0].get(curPermList.get(k) - 1).get(machineInitPos.getInitPosOfMachine(m));
									//dist = 0;
								}
								else
									dist = data[0].get(curPermList.get(tmpi) - 1).get(curPermList.get(k) - 1);
							}
							
							double opRate = machineOpInfo.getCertainMachineOpInfo(m).get(0);
							double moveRate = machineOpInfo.getCertainMachineOpInfo(m).get(1);
							double curTotalTime = curMachineWorkload / opRate + dist / moveRate ;
							
							curWfTotalTime.add(curTotalTime);
						}
						
					}
					matrix.add(curWfTotalTime);
				}
				
				int flag = 0;
//				if(curPermList.contains(20) && curPermList.contains(19) && curPermList.contains(18) && curPermList.contains(17))
//					flag = 1;
				double curMaxTime = computeMaxWfSeqTime(matrix, flag);
				
				if(curMaxTime < minTime){
					minTime = curMaxTime;
					minPermList = curPermList;
				}
				
				//Register log info -- Print out each workface permutation time info and cur maxtime and permutation
				StringBuilder msgPerm = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
				msgPerm.append("\n===================Print out workface time info for each permutation================\n");
				msgPerm.append("cur matrix: \n");
				for(int jj = 0; jj < matrix.size(); jj ++){
					for(int kk = 0; kk < matrix.get(jj).size(); kk ++){
						msgPerm.append(matrix.get(jj).get(kk) + "\t");
					}
					msgPerm.append("\n");
				}
				msgPerm.append("curPermList: \n");
				for(int ii = 0; ii < curPermList.size(); ii ++){
					msgPerm.append(curPermList.get(ii) + " ");
				}
				msgPerm.append("\n");
				msgPerm.append("cur maxtime: " + curMaxTime + "\ncur mintime: " + minTime + "\n");
				msgPerm.append("curMinList: \n");
				for(int ii = 0; ii < minPermList.size(); ii ++){
					msgPerm.append(minPermList.get(ii) + " ");
				}
				msgPerm.append("\n");
				LogTool.log(LEVEL, msgPerm.toString());
				if(curPermList.contains(20) && curPermList.contains(19) && curPermList.contains(18) && curPermList.contains(17)){
					System.out.println(msgPerm);
				}
				
//				if(curPermList.contains(8)){
//					msgPerm.append("\n===================Print out workface time info for each permutation================\n");
//					for(int ii = 0; ii < curPermList.size(); ii ++){
//						System.out.print(curPermList.get(ii) + " ");
//					}
//					System.out.println("cur maxtime: " + curMaxTime);
//				}
				
			}
			// Print out minimum workface time
//			System.out.println("\n===================minimum workface time permutation================\n" + minTime);
//			for(int k = 0; k < minPermList.size(); k ++){
//				System.out.print(minPermList.get(k) + " ");
//			}
//			System.out.println();
			
			sortGroups.add(minPermList);
		}
		
		//Register log info -- Print out sorted workfaces
		StringBuilder msgSortedGroups = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
		msgSortedGroups.append("\n===================Print out sorted workfaces================\n");
		for (int i = 0; i < sortGroups.size(); i ++)
			msgSortedGroups.append(sortGroups.get(i) + "\n");
		LogTool.log(LEVEL, msgSortedGroups.toString());
		
		return sortGroups;
	}
	
	/**
	 * Sort workfaces to determin the operating order of all machines on these workfaces.
	 * @param data Distance matrix between all workfaces
	 * @param clusterGroups A group of workfaces which are to be sorted, e.g. (w1, w2, w3), (w4, w6), (w5, w7)
	 * @param machineOpInfo Machine operation information including machine operating rate and moving rate.
	 * @param workload Workload for all machines on all workfaces.
	 * @return A list of sorted workfaces 
	 */
	@Deprecated 
 	public static ArrayList<ArrayList<Integer>> sortWorkfaces (Dataset[] data, ArrayList<ArrayList<Integer>> clusterGroups1, MachineOpInfo machineOpInfo, WorkfaceWorkload workload){
 		
		ArrayList<ArrayList<Integer>> sortGroups = new ArrayList<ArrayList<Integer>>();
		// The number of procedures in each work face
		int numOfProcedure = machineOpInfo.getMachineNum();
		// Iterate through each cluster group, e.g. (w1, w2, w3), (w4, w6), (w5, w7)
		for(int i = 0; i < clusterGroups1.size(); i++){
			
			ArrayList<Integer> workfaceSeq  =new ArrayList<Integer>();
			for(int w1 = 0; w1 < clusterGroups1.get(i).size() - 1; w1++){
				for(int w2 = w1 + 1; w2 < clusterGroups1.get(i).size(); w2 ++ ){
					
					// id of first workface (real value minus 1)
					int id1 = clusterGroups1.get(i).get(w1) - 1; 
					// id of second workface (real value minus 1)
					int id2 = clusterGroups1.get(i).get(w2) - 1;
					// distance between workface 1 and 2
					double dist12 = data[i].get(id1).value(id2);
					
					
					// ********* compute workface id1-id2******************
					double tmpTotalTime1 = 0, maxTotalTime1 = 0;
					// sorting algorithm
					for(int sep1 = 0; sep1 < numOfProcedure; sep1 ++){
						// all procedures (all machines)
						for(int pro11 = 0 ; pro11 <= sep1; pro11 ++){
							// machine pro's workload at workface id1 / machine pro's operating rate
							tmpTotalTime1 += workload.getWorkloadOfMachine(pro11).get(id1)/
									machineOpInfo.getCertainMachineOpInfo(pro11).get(0);
						}
						for(int pro12 = sep1; pro12 < numOfProcedure; pro12 ++){
							tmpTotalTime1 += workload.getWorkloadOfMachine(pro12).get(id2)/
									machineOpInfo.getCertainMachineOpInfo(pro12).get(0);
						}
						// moving time from work face id1 to work face id2
						tmpTotalTime1 += dist12/machineOpInfo.getCertainMachineOpInfo(sep1).get(1);
						maxTotalTime1 = (tmpTotalTime1 > maxTotalTime1) ? tmpTotalTime1 : maxTotalTime1;
						tmpTotalTime1 = 0;
					}// end sep1
					
					// ********* compute workface id2-id1******************
					double tmpTotalTime2 = 0, maxTotalTime2 = 0;
					for(int sep2 = 0; sep2 <numOfProcedure; sep2 ++){
						// all procedures (all machines)
						for(int pro21 = 0 ; pro21 <= sep2; pro21 ++){
							// machine pro's workload at workface id1 / machine pro's operating rate
							tmpTotalTime2 += workload.getWorkloadOfMachine(pro21).get(id2)/
									machineOpInfo.getCertainMachineOpInfo(pro21).get(0);
						}
						for(int pro22 = sep2; pro22 < numOfProcedure; pro22 ++){
							tmpTotalTime1 += workload.getWorkloadOfMachine(pro22).get(id1)/
									machineOpInfo.getCertainMachineOpInfo(pro22).get(0);
						}
						// moving time from work face id1 to work face id2
						tmpTotalTime2 += dist12/machineOpInfo.getCertainMachineOpInfo(sep2).get(1);
						maxTotalTime2 = (tmpTotalTime2 > maxTotalTime2) ? tmpTotalTime2 : maxTotalTime2;
						tmpTotalTime2 = 0;
					}// end sep2
					
					// maxTotalTime1 indidates (w-id1, w-id2)
					// now sequecnce should be (w-id2, w-id1)
					
					boolean containId1 = workfaceSeq.contains(id1);
					boolean containId2 = workfaceSeq.contains(id2);
					
					if(maxTotalTime1 > maxTotalTime2){
						if(containId1 == true){
							if(containId2 == true){
								int indexOf1 = workfaceSeq.indexOf(id1);
								int indexOf2 = workfaceSeq.indexOf(id2);
								if(indexOf1 < indexOf2){
									workfaceSeq.remove(new Integer(id2));
									workfaceSeq.add(workfaceSeq.indexOf(id1), id2);
								}
							}
							//first-time emergence of id2
							else{
								workfaceSeq.add(workfaceSeq.indexOf(id1), id2);
							}
						}
						// first-time emergence of id1 
						else{
							if(containId2 == true){
								workfaceSeq.add(workfaceSeq.indexOf(id2)+1, id1);
							}
							//first-time emergence of id2
							else{
								workfaceSeq.add(id2);
								workfaceSeq.add(id1);
							}
						}
					}
					// maxTotalTime12 indidates (w-id2, w-id1)
					// now sequence should be (w-id1, w-id2)
					else{
						if(containId1 == true){
							if(containId2 == true){
								workfaceSeq.remove(new Integer(id1));
								workfaceSeq.add(workfaceSeq.indexOf(id2), id1);
							}else{
								workfaceSeq.add(workfaceSeq.indexOf(id1)+1, id2);
							}
						}
						else{
							if(containId2 == true){
								workfaceSeq.add(workfaceSeq.indexOf(id2), id1);
							}else{
								workfaceSeq.add(id1);
								workfaceSeq.add(id2);
							}
						}
					}
					//Register log info
					StringBuilder msgCurSortGroups = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
					msgCurSortGroups.append("\ncurrent sort groups: " + workfaceSeq + "\n");
					LogTool.log(LEVEL, msgCurSortGroups.toString());
				}// end w2
			}// end w1
			sortGroups.add(workfaceSeq);
		}
		
		//Register log info -- Print out sorted workfaces
		StringBuilder msgSortedGroups = new StringBuilder(Thread.currentThread().getStackTrace()[1].toString());
		msgSortedGroups.append("\n===================Print out sorted workfaces================\n");
		for (int i = 0; i < sortGroups.size(); i ++)
			msgSortedGroups.append(sortGroups.get(i) + "\n");
		LogTool.log(LEVEL, msgSortedGroups.toString());
		
		for(int i = 0; i < sortGroups.size(); i ++){
			for(int j = 0; j < sortGroups.get(i).size(); j ++){
				int tmpV = sortGroups.get(i).get(j);
				sortGroups.get(i).set(j, tmpV + 1);
			}
		}
		return sortGroups;
	}
 	
	/**
	 *  Sort workfaces to determin the operating order of all machines on these workfaces.
	 * @param clusterGroups A group of workfaces which are to be sorted, e.g. (w1, w2, w3), (w4, w6), (w5, w7)
	 * @param machineOpInfo Machine operation information including machine operating rate and moving rate.
	 * @param workload Workload for all machines on all workfaces.
	 * @return  A list of sorted workfaces 
	 */
 	@Deprecated 
 	public static ArrayList<ArrayList<Integer>> sortWorkfaces (Dataset[] clusterGroups, MachineOpInfo machineOpInfo, WorkfaceWorkload workload){
 		
		ArrayList<ArrayList<Integer>> sortGroups = new ArrayList<ArrayList<Integer>>();
		// The number of procedures in each work face
		int numOfProcedure = machineOpInfo.getMachineNum();
		// Iterate through each cluster group, e.g. (w1, w2, w3), (w4, w6), (w5, w7)
		for(int i = 0; i < clusterGroups.length; i++){
			
			ArrayList<Integer> workfaceSeq  =new ArrayList<Integer>();
			for(int w1 = 0; w1 < clusterGroups[i].size() - 1; w1++){
				for(int w2 = w1 + 1; w2 < clusterGroups[i].size(); w2 ++ ){
					
					// id of first workface
					int id1 = clusterGroups[i].get(w1).getID();
					// id of second workface
					int id2 = clusterGroups[i].get(w2).getID();
					// distance between workface 1 and 2
					double dist12 = clusterGroups[i].get(w1).value(id2);
					
					
					// ********* compute workface id1-id2******************
					double tmpTotalTime1 = 0, maxTotalTime1 = 0;
					// sorting algorithm
					for(int sep1 = 0; sep1 < numOfProcedure; sep1 ++){
						// all procedures (all machines)
						for(int pro11 = 0 ; pro11 <= sep1; pro11 ++){
							// machine pro's workload at workface id1 / machine pro's operating rate
							tmpTotalTime1 += workload.getWorkloadOfMachine(pro11).get(id1)/
									machineOpInfo.getCertainMachineOpInfo(pro11).get(0);
						}
						for(int pro12 = sep1; pro12 < numOfProcedure; pro12 ++){
							tmpTotalTime1 += workload.getWorkloadOfMachine(pro12).get(id2)/
									machineOpInfo.getCertainMachineOpInfo(pro12).get(0);
						}
						// moving time from work face id1 to work face id2
						tmpTotalTime1 += dist12/machineOpInfo.getCertainMachineOpInfo(sep1).get(1);
						maxTotalTime1 = (tmpTotalTime1 > maxTotalTime1) ? tmpTotalTime1 : maxTotalTime1;
						tmpTotalTime1 = 0;
					}// end sep1
					
					// ********* compute workface id2-id1******************
					double tmpTotalTime2 = 0, maxTotalTime2 = 0;
					for(int sep2 = 0; sep2 <numOfProcedure; sep2 ++){
						// all procedures (all machines)
						for(int pro21 = 0 ; pro21 <= sep2; pro21 ++){
							// machine pro's workload at workface id1 / machine pro's operating rate
							tmpTotalTime2 += workload.getWorkloadOfMachine(pro21).get(id2)/
									machineOpInfo.getCertainMachineOpInfo(pro21).get(0);
						}
						for(int pro22 = sep2; pro22 < numOfProcedure; pro22 ++){
							tmpTotalTime1 += workload.getWorkloadOfMachine(pro22).get(id1)/
									machineOpInfo.getCertainMachineOpInfo(pro22).get(0);
						}
						// moving time from work face id1 to work face id2
						tmpTotalTime2 += dist12/machineOpInfo.getCertainMachineOpInfo(sep2).get(1);
						maxTotalTime2 = (tmpTotalTime2 > maxTotalTime2) ? tmpTotalTime2 : maxTotalTime2;
						tmpTotalTime2 = 0;
					}// end sep2
					
					// maxTotalTime1 indidates (w-id1, w-id2)
					// now sequecnce should be (w-id2, w-id1)
					
					boolean containId1 = workfaceSeq.contains(id1);
					boolean containId2 = workfaceSeq.contains(id2);
					
					if(maxTotalTime1 > maxTotalTime2){
						if(containId1 == true){
							if(containId2 == true){
								int indexOf1 = workfaceSeq.indexOf(id1);
								int indexOf2 = workfaceSeq.indexOf(id2);
								if(indexOf1 < indexOf2){
									workfaceSeq.remove(new Integer(id2));
									workfaceSeq.add(workfaceSeq.indexOf(id1), id2);
								}
							}
							//first-time emergence of id2
							else{
								workfaceSeq.add(workfaceSeq.indexOf(id1), id2);
							}
						}
						// first-time emergence of id1 
						else{
							if(containId2 == true){
								workfaceSeq.add(workfaceSeq.indexOf(id2)+1, id1);
							}
							//first-time emergence of id2
							else{
								workfaceSeq.add(id2);
								workfaceSeq.add(id1);
							}
						}
					}
					// maxTotalTime12 indidates (w-id2, w-id1)
					// now sequence should be (w-id1, w-id2)
					else{
						if(containId1 == true){
							if(containId2 == true){
								workfaceSeq.remove(new Integer(id1));
								workfaceSeq.add(workfaceSeq.indexOf(id2), id1);
							}else{
								workfaceSeq.add(workfaceSeq.indexOf(id1)+1, id2);
							}
						}
						else{
							if(containId2 == true){
								workfaceSeq.add(workfaceSeq.indexOf(id2), id1);
							}else{
								workfaceSeq.add(id1);
								workfaceSeq.add(id2);
							}
						}
					}
				   System.out.println("current sort groups: "+workfaceSeq);
				}// end w2
			}// end w1
			sortGroups.add(workfaceSeq);
		}
		return sortGroups;
	}
}
