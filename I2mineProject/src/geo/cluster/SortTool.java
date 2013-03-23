package geo.cluster;

import geo.core.MachineOpInfo;
import geo.core.WorkfaceDistance;
import geo.core.WorkfaceWorkload;

import java.util.ArrayList;

import net.sf.javaml.core.Dataset;

public class SortTool {
	
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
		System.out.println("Number of machine:" + numberOfMachine);
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
			
			System.out.println("=============SORTED workfaces:=============");
			for(int sw = 0; sw < sortedWorkfacesInOneRegion.size(); sw++)
				System.out.print(sortedWorkfacesInOneRegion.get(sw)+"  ");
			System.out.println();
			
			for(int w = 0; w < sortedWorkfacesInOneRegion.size() - 1; w++){
				
				double curProTime = curWorkload.get(sortedWorkfacesInOneRegion.get(w))/curOpInfo.get(0);
				double curMovTime = distance.getDistBetweenTwoWorkfaces(
						sortedWorkfacesInOneRegion.get(w), sortedWorkfacesInOneRegion.get(w + 1))/curOpInfo.get(1);
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
				
			}// end w
			// processing time for last procedure
			curTimeInterval.add(curWorkload.get(sortedWorkfacesInOneRegion.get(sortedWorkfacesInOneRegion.size() - 1))/curOpInfo.get(0));
			
			// add time interval for current machine into machineTimeInterval
			machineTimeInterval.add(curTimeInterval);
			machineTimeInterval.add(curWaitTime);
		}// end m
		
		// print out machineTimeInterval in the format of "time_interval, wait_time [, time_interval, wait_time]"
		for(int n = 0; n < machineTimeInterval.size(); n++){
			
			if(n % 2 == 0){
				System.out.println("MACHINE: "+ n / 2 +"\nTime_interval:");
			}else{
				System.out.println("Wait_time:");
			}
			System.out.println(machineTimeInterval.get(n));
		}
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
	public static ArrayList<ArrayList<Integer>> sortGroups (ArrayList<ArrayList<Integer>> sortedWorkfaces, MachineOpInfo machineOpInfo, WorkfaceWorkload workload, WorkfaceDistance distance){
		ArrayList<ArrayList<Integer>> sortGroups = new ArrayList<ArrayList<Integer>>();
		
		ArrayList<ArrayList<Double>> regionTimeInfo = computeMachineTimeIntervalInOneRegion(sortedWorkfaces.get(0), machineOpInfo, workload, distance);
		
		// iterate through each pair of regions
		for(int region1 = 0; region1 < sortedWorkfaces.size() - 1; region1 ++){
			for(int region2 = 0; region2 < sortedWorkfaces.size(); region2 ++){
				
			}
		}
		
		return sortGroups;
	}
	/**
	 * 
	 * @param clusterGroups
	 * @param machineOpInfo
	 * @param workload
	 * @return
	 */
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
					
					
					// ********* computer workface id1-id2******************
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
					}// end sep1
					
					// ********* computer workface id2-id1******************
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
					}// end sep2
					
					// maxTotalTime1 indidates (w-id1, w-id2)
					// now sequecnce should be (w-id2, w-id1)
					boolean containId1 = workfaceSeq.contains(id1);
					boolean containId2 = workfaceSeq.contains(id2);
					
					if(maxTotalTime1 > maxTotalTime2){
						if(containId1 == true){
							if(containId2 == true){
								workfaceSeq.remove(new Integer(id2));
								workfaceSeq.add(workfaceSeq.indexOf(id1), id2);
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
