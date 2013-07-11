package geo.cluster;

import geo.core.MachineOpInfo;
import geo.core.WorkfaceDistance;
import geo.core.WorkfaceWorkload;
import geo.util.LogTool;

import java.util.ArrayList;

import net.sf.javaml.core.Dataset;

public class SortTool {
	
	// Determine if output the debug info or not
	public static int LEVEL = -1;
	
	/**
	 * Given one sorted workfaces in a region (w1, w2, w3), compute the machine time interval for each machine. 
	 * @param sortedWorkfacesInOneRegion
	 * 		sorted workfaces in one region
	 * @param machineOpInfo
	 * 		machine operating information
	 * @param workload
	 * 		workload for each machine in each workface
	 * @param distance
	 * 		distances between each pair of workfaces
	 * @return time interval for all machines in current region
	 */
	
	public static ArrayList<ArrayList<Double>> computeMachineTimeIntervalInOneRegion(ArrayList<Integer> sortedWorkfacesInOneRegion, MachineOpInfo machineOpInfo, WorkfaceWorkload workload, WorkfaceDistance distance){
		int numberOfMachine = machineOpInfo.getMachineNum();
		// the number of travelling times for a machine in current region
		int travelNumber = numberOfMachine - 1;
		// storing time intervals for all machines e.g. w1, w1->w3, w3, w3->w2, w2
		ArrayList<ArrayList<Double>> machineTimeInterval = new ArrayList<ArrayList<Double>>(); 
		ArrayList<ArrayList<Double>> machineWaitTime = new ArrayList<ArrayList<Double>>();
		
		// compute time interval for each machine in all workfaces in current region
		for(int m = 0; m < numberOfMachine;m++){
			// OR and MR
			ArrayList<Double> curOpInfo = machineOpInfo.getCertainMachineOpInfo(m);
			ArrayList<Double> curWorkload = workload.getWorkloadOfMachine(m);
			ArrayList<Double> curTimeInterval = new ArrayList<Double>();
			ArrayList<Double> curWaitTime = new ArrayList<Double>();
			
			// time interval of previous machine in current region
			// in the format of "pro_time, mov_time [, WAIT_time], pro_time, mov_time[, WAIT_time] ..." 
			ArrayList<Double> preMachineTimeInterval = null;
			ArrayList<Double> preWaitTime = null;
			if(m != 0){
				preMachineTimeInterval = machineTimeInterval.get((m - 1) * 2);
				preWaitTime = machineTimeInterval.get((m - 1) * 2 + 1);
			}
			
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
				double curProTime = curWorkload.get((sortedWorkfacesInOneRegion.get(w)) - 1)/curOpInfo.get(0);
				double curMovTime = distance.getDistBetweenTwoWorkfaces(
						sortedWorkfacesInOneRegion.get(w) - 1, sortedWorkfacesInOneRegion.get(w + 1) - 1)/curOpInfo.get(1);
				double timeOfCur = curProTime + curMovTime;
				// processing time from 1st procedure to last - 1 procedure
				curTimeInterval.add(curProTime);
				// moving time
				curTimeInterval.add(curMovTime);
				// test whether WAIT time needs to be added
				if(m == 0){
					curWaitTime.add(0.0);
				}else{
					int indexOfPre = (w + 1) * 2;
					double timeOfPre = preMachineTimeInterval.get(indexOfPre) + preMachineTimeInterval.get(indexOfPre - 1);  
					timeOfPre += preWaitTime.get(w);
					
					// WAIT time is needed
					if(timeOfPre > timeOfCur){
						curWaitTime.add(timeOfPre - timeOfCur);
					}
					// No WAIT time is needed, can start process right away
					else{
						curWaitTime.add(0.0);
					}
				}
				
			}// end for(w)
			// processing time for last procedure
			curTimeInterval.add(curWorkload.get(sortedWorkfacesInOneRegion.get(sortedWorkfacesInOneRegion.size() - 1))/curOpInfo.get(0));
			
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
	 * 
	 * @param sortedWorkfaces
	 * @param machineOpInfo
	 * @param workload
	 * @param distance
	 * @return
	 */
	
	public static ArrayList<ArrayList<Integer>> sortGroups(ArrayList<ArrayList<Integer>> sortedWorkfaces, MachineOpInfo machineOpInfo, WorkfaceWorkload workload, WorkfaceDistance distance){
		ArrayList<ArrayList<Integer>> sortGroups = new ArrayList<ArrayList<Integer>>();
		int numberOfMachine = machineOpInfo.getMachineNum();
		
		// iterate through each pair of regions
		for(int region11 = 0; region11 < sortedWorkfaces.size() - 1; region11 ++){
			// regionTimeInfo in the form of "M1TimeInterval, M1WaitTime, M2TimeInterval, M2WaitTime,...."
			ArrayList<ArrayList<Double>> regionTimeInfo11 = computeMachineTimeIntervalInOneRegion
					(sortedWorkfaces.get(region11), machineOpInfo, workload, distance);
			
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
					//---------------------------------------------
//					for(int m1 = 0; m1 <= sep1; m1 ++){
//						tmpTotalTime11_12 += regionTime11.get(m1);
//						System.out.print(regionTime11.get(m1) + " ");
//					}
//					for(int m2 = sep1; m2 < numberOfMachine; m2 ++){
//						tmpTotalTime11_12 += regionTime12.get(m2);
//						System.out.print(regionTime12.get(m2) + " ");
//					}
					//----------------------------------------------
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
					
					//-----------------------------------------------
//					for(int m1 = 0; m1 <= sep2; m1 ++){
//						tmpTotalTime12_11 += regionTime12.get(m1);
//						System.out.print(regionTime12.get(m1) + " ");
//					}
//					for(int m2 = sep2; m2 < numberOfMachine; m2 ++){
//						tmpTotalTime12_11 += regionTime11.get(m2);
//						System.out.print(regionTime11.get(m2) + " ");
//					}
					//-----------------------------------------------
					tmpTotalTime12_11 += dist21_22 / machineOpInfo.getCertainMachineOpInfo(sep2).get(1);
					
					maxTotalTime12_11 = (maxTotalTime12_11 > tmpTotalTime12_11) ? maxTotalTime12_11 : tmpTotalTime12_11;
					//System.out.print(tmpTotalTime12_11 + " ");
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
	 * 
	 * @param clusterGroups
	 * @param machineOpInfo
	 * @param workload
	 * @return
	 */
 	public static ArrayList<ArrayList<Integer>> sortWorkfaces (Dataset[] data, ArrayList<ArrayList<Integer>> clusterGroups1,MachineOpInfo machineOpInfo, WorkfaceWorkload workload){
 		
		ArrayList<ArrayList<Integer>> sortGroups = new ArrayList<ArrayList<Integer>>();
		// The number of groups after clustering
		int len = clusterGroups1.size();
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
		//return sortGroups;
	}
 	
	/**
	 * @deprecated this method is deprecated
	 * @param clusterGroups
	 * @param machineOpInfo
	 * @param workload
	 * @return
	 */
 	@Deprecated 
 	public static ArrayList<ArrayList<Integer>> sortWorkfaces (Dataset[] clusterGroups, MachineOpInfo machineOpInfo, WorkfaceWorkload workload){
 		
		ArrayList<ArrayList<Integer>> sortGroups = new ArrayList<ArrayList<Integer>>();
		// The number of groups after clustering
		int len = clusterGroups.length;
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
