package geo.cluster;

import geo.core.DumpSiteCapacity;
import geo.core.DumpSiteWorkfaceDistance;
import geo.core.MachineInitialPosition;
import geo.core.MachineOpInfo;
import geo.core.Truck;
import geo.core.WorkfaceDistance;
import geo.core.WorkfaceMineralCapacity;
import geo.core.WorkfaceProcessUnit;
import geo.core.WorkfaceWorkload;
import geo.i2mine.I2MineMain;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * 
 * @author Dong
 *
 */
public class LHD {
	/**
	 * Such a class instance records a time duration during which how much workload has
	 * been done on a workface, the starting time, the ending time, the number of trucks allocated
	 * to the workface during the time duration, the dump site ID where the workface mineral is sent, 
	 * 
	 * @author Dong
	 *
	 */
	public static class TimeDuration implements Comparable<TimeDuration>{
		private int wfId;
		private double startTime;
		private double duration;
		private double endTime;
		private int truckNum;
		private double workloadDone;
		private double workLeft;
		private int dumpSiteId;
		private String dumpSiteOverflowMsg;
		private int level;
		
		public TimeDuration(){};
		public void setWfId(int workfaceId){
			wfId = workfaceId;
		}
		
		public void setStartTime(double time){
			startTime = time;
		}
		
		public void setDuration(double dur){
			duration = dur;
		}
		
		public void setEndTime(double time){
			endTime = time;
		}
		
		public void setTruckNum(int number){
			truckNum = number;
		}
		
		public void setWorkloadDone(double workload){
			workloadDone = workload;
		}
		
		public void setDumpSiteId(int id){
			dumpSiteId = id;
		}
		
		public void setDumpSiteOverflowMsg(String msg){
			dumpSiteOverflowMsg = msg;
		}
		
		public void setLevel(int l){
			level = l;
		}
		
		public void setWorkLeft(double totalWorkLeft){
			workLeft = totalWorkLeft;
		}
		
		public int getWorkfaceId(){return wfId;}
		public double getStartTime(){return startTime;}
		public double getDuration(){return duration;}
		public double getEndTime(){return endTime;}
		public int getTruckNum(){return truckNum;}
		public double getWorkloadDone(){return workloadDone;}
		public String getDumpSiteOverflowMsg(){return dumpSiteOverflowMsg; }
		public int getDumpSiteId(){return dumpSiteId;}
		public int getLevel(){return level;}
		public double getWorkLeft(){return workLeft;}
		
		@Override
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append("Workface_ID: " + wfId);
			sb.append(" Start_Time: " + startTime);
			sb.append(" End_Time: " + endTime);
			sb.append(" Duration: " + duration);
			sb.append(" Truck_Number: " + truckNum);
			sb.append(" Workload_Done: " + workloadDone);
			sb.append(" Work_Left: " + workLeft);
			sb.append(" Overflow: " + dumpSiteOverflowMsg);
			sb.append(" Level: " + level + "\n");
			return  sb.toString();
		}
		
		@Override
		public int compareTo(TimeDuration t) {
			return level - t.level;
		}
	}
	
	/**
	 * Compare two WorkfaceprocessUnits based on their end time.
	 * @author Dong
	 *
	 */
	public static class WfProcUnitEndComparator implements Comparator<WorkfaceProcessUnit>{

		@Override
		public int compare(WorkfaceProcessUnit u1, WorkfaceProcessUnit u2) {
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
	 * 
	 * @param wfProcList A list of <b> WorkfaceProcessUnit</b> instances. Note: workface ID is indexed from 0.
	 * @param distance The distance between all the workfaces.
	 * @param workload The workload of all the procedures.
	 * @param wfMineralCapacity The amount of mineral for each workface.
	 * @param truckList The list of trucks used to move mineral from workface to dump site.
	 * @param opInfo Operational information of operation machines.
	 * @param initPos The initial positions for each operation machines.
	 * @param dumpWfDistance The distance between dump sites and workfaces.
	 * @param dumpCapacity The capacity of each dump site.
	 * @param totalTruckNum The total number of trucks used to move mineral from workfaces to nearest dump sites.
	 * @param numOfLoaders The total number of loaders used to load mineral into trucks.
	 * @param actionChosen The action chosen to finish all workload on all workfaces.
	 * @throws IOException Throw an Exception if the file cannot be created, or exist but a directory not a regular file.
	 * @return true if data has been written to disk; false otherwise.
	 */
	public static boolean lhd(ArrayList<WorkfaceProcessUnit> wfProcList, WorkfaceDistance distance, WorkfaceWorkload workload, 
			WorkfaceMineralCapacity wfMineralCapacity, ArrayList<Truck> truckList, MachineOpInfo opInfo, MachineInitialPosition initPos, 
			DumpSiteWorkfaceDistance dumpWfDistance, DumpSiteCapacity dumpCapacity, int totalTruckNum, int numOfLoaders, int actionChosen) throws IOException{
		
		System.out.println("wfProcList size: " + wfProcList.size() + 
						   "\ndistance wf num: " + distance.getNumOfWorkface() + 
						   "\nworkload machine num: " + workload.getMachineNum() + 
						   "\nwfmineralCapacity size: " + wfMineralCapacity.getWorkfaceCapacity(0) +
						   "\ntruckList size: " + truckList.size() + 
						   "\nopInfo machine num: " + opInfo.getMachineNum() + 
						   "\ninitPos size: " + initPos.getInitPosOfMachine(0) + 
						   "\ndumpWfDistance : " + dumpWfDistance.getDumpSiteDistance(0) + 
						   "\ndumpCapacity :" + dumpCapacity.toString() + 
						   "\ntotalTruckNum :" + totalTruckNum + 
						   "\nnumOfLoaders : " + numOfLoaders);		
		// Sort the workface by their total ending time
		WfProcUnitEndComparator cmp = new WfProcUnitEndComparator();
		Collections.sort(wfProcList, cmp);
		
		// Record the distance between each workface and its nearest dump site.
		ArrayList<Integer> truckLimit = new ArrayList<Integer>();
		ArrayList<Float> wfNearestDumpSite = new ArrayList<Float>(); 
		for(int i = 0; i < distance.getNumOfWorkface(); i ++){
			float minSiteDist = Float.MAX_VALUE;
			for(Float curDumpDist: dumpWfDistance.getDumpSiteDistance(wfProcList.get(i).getWfId())){
				if(curDumpDist < minSiteDist){
					minSiteDist = curDumpDist;
				}
			}
			wfNearestDumpSite.add(minSiteDist);
		}
		
		// Compute the truck upper limit number for each workface. 
		for(int i = 0; i < distance.getNumOfWorkface(); i ++){
			int truckNumLimit = (int) (wfNearestDumpSite.get(i) / (truckList.get(0).getLoadingTime() * truckList.get(0).getVelocity())) ;
			if(truckNumLimit == 0){
				truckNumLimit ++;
			}
			truckLimit.add(truckNumLimit);
		}
		
		System.out.println("<<<<<Workface order after performing operations:>>>>>");
		for(WorkfaceProcessUnit wfpu: wfProcList){
			System.out.print(wfpu.getWfId() + " ");
		}
		System.out.println();
		
		// Store TimeDuration instance for each workface
		ArrayList<ArrayList<TimeDuration>> timeDurList = new ArrayList<ArrayList<TimeDuration>>();
		int numOfWfs = distance.getNumOfWorkface();
		for(int i = 0; i < numOfWfs; i ++){
			timeDurList.add(new ArrayList<TimeDuration>());
		}
		
		// Queue to store workface ids
		LinkedList<Integer> queue = new LinkedList<Integer>();
		for(int i = 0; i < wfProcList.size(); i ++){
			queue.offer(wfProcList.get(i).getWfId());
		}
				
		int level = 0;
		// Start to compute the time duration for each workface
		while(!queue.isEmpty()){
			level ++;
			if(queue.size() >= numOfLoaders){
				// numOfLoaders do not change
			}
			// There are less than numOfLoaders workfaces left
			else{
				numOfLoaders = queue.size();
			}
			
			System.out.println("<<<<<WfId :");
			// Nearest dump site for numOfLoaders workfaces
			ArrayList<Float> dumpDistList = new ArrayList<Float>(); // minimum distance list
			ArrayList<Integer> dumpSiteChosenList = new ArrayList<Integer>(); // chosen dump site id list
			
			for(int i = 0; i < numOfLoaders; i ++){
				System.out.print(queue.get(i) + " ");
				ArrayList<Float> curDumpSiteDistances = dumpWfDistance.getDumpSiteDistance(queue.get(i));
				int minIdx = 0;
				float minDist = Float.MAX_VALUE;
				for(int j = 0; j < curDumpSiteDistances.size(); j ++){
					if(curDumpSiteDistances.get(j) < minDist){
						minDist = curDumpSiteDistances.get(j);
						minIdx = j;
					}
				}
				dumpDistList.add(minDist);
				dumpSiteChosenList.add(minIdx);
			}
			System.out.println();
			
			// Calculate how many trucks for each workface
			ArrayList<Integer> truckNumList = new ArrayList<Integer>();
			double sumOfDist = 0f;
			int sumOfTrucks = 0;
			for(int i = 0; i < numOfLoaders; i ++){
				sumOfDist += dumpDistList.get(i);
			}
						
			for(int i = 0; i < numOfLoaders; i ++){
				dumpDistList.set(i, (float) ((dumpDistList.get(i) / sumOfDist) * totalTruckNum));
				truckNumList.add((int)(dumpDistList.get(i).floatValue()));
				if(truckNumList.get(i) == 0){
					truckNumList.set(i, 1);
				}
				sumOfTrucks += truckNumList.get(i);
			}
			
			System.out.println("Before allocation: ");
			for(int idx = 0; idx < numOfLoaders; idx ++){
				System.out.print(truckNumList.get(idx));
			}
			System.out.println();
			
			System.out.println("\nSUM OF TRUCKS :" + sumOfTrucks + " diff: ");
			for(int idx = 0; idx < numOfLoaders; idx ++){
				System.out.print((dumpDistList.get(idx) - truckNumList.get(idx)) + " ");
			}
			System.out.println();
			
			if(sumOfTrucks < totalTruckNum){
				System.out.println("ALLOCATE---------");
				boolean[] flag = new boolean[numOfLoaders];
				for(int i = totalTruckNum - sumOfTrucks; i > 0; i --){
					int curMaxIndex = 0;
					double curMaxWf = 0f;						
					for(int j = 0; j < numOfLoaders; j ++){
						if(!flag[j] && (dumpDistList.get(j) - truckNumList.get(j)) > curMaxWf){
							curMaxIndex = j;
							curMaxWf = dumpDistList.get(j) - truckNumList.get(j);
						}
					}
					truckNumList.set(curMaxIndex, truckNumList.get(curMaxIndex) + 1);
					flag[curMaxIndex] = true;
				}
			}else if(sumOfTrucks > totalTruckNum){
				for(int i = totalTruckNum - sumOfTrucks; i > 0; i --){
					int maxIdx = 0, maxNum = 0;
					for(int j = 0; j < numOfLoaders; j ++){
						if(truckNumList.get(j) > maxNum){
							maxNum = truckNumList.get(j);
							maxIdx = j;
						}
					}
					truckNumList.set(maxIdx, truckNumList.get(maxIdx) - 1);
				}
			}
			
			// Compute the left trucks based on the truck upper limit number of each workface.
			int leftTrucks = 0;
			for(int i = 0; i < numOfLoaders; i ++){
				if(truckLimit.get(queue.get(i)) < truckNumList.get(i)){
					leftTrucks += (truckNumList.get(i) - truckLimit.get(queue.get(i)));
					truckNumList.set(i, truckLimit.get(queue.get(i)));
				}
			}
			
			System.out.println("<<<<<trucks allocated and wfId :");
			for(Integer t: truckNumList){
				System.out.print(t + " ");
			}
			System.out.println();
			
			
			// Compute time duration
			ArrayList<TimeDuration> curTimeDurList = new ArrayList<TimeDuration>(); 
			for(int i = 0; i < numOfLoaders; i ++){
				TimeDuration w1 = new TimeDuration();
				// Set workface id for this time duration
				w1.setWfId(queue.get(i));
				w1.setDumpSiteId(dumpSiteChosenList.get(i));
				// Set start time for this time duration
				// The first time duration for this workface
				if(timeDurList.get(w1.getWorkfaceId()).size() == 0){
					WorkfaceProcessUnit tmpWpu = null;
					for(WorkfaceProcessUnit wpu: wfProcList){
						if(wpu.getWfId() == w1.getWorkfaceId()){
							tmpWpu = wpu;
							break;
						}
					}
					w1.setStartTime(tmpWpu.getTotalEndTime());
				}else{
					ArrayList<TimeDuration> w1TimeList = timeDurList.get(w1.getWorkfaceId());
					int size = w1TimeList.size();
					w1.setStartTime(w1TimeList.get(size - 1).getEndTime());
				}
				
				ArrayList<Float> tmpDumpDistList = dumpWfDistance.getDumpSiteDistance(w1.getWorkfaceId());
				double dumpDist = (tmpDumpDistList.get(0) > tmpDumpDistList.get(1))?tmpDumpDistList.get(0):tmpDumpDistList.get(1);
				w1.setDuration(
						wfMineralCapacity.getWorkfaceCapacity(w1.getWorkfaceId()) * dumpDist / 
						(truckNumList.get(i) * truckList.get(0).getPayLoad() * truckList.get(0).getVelocity())
				);
				w1.setEndTime(w1.getStartTime() + w1.getDuration());
				w1.setTruckNum(truckNumList.get(i));
				
				curTimeDurList.add(w1);
			}// end for - numOfLoaders
			
			System.out.println("curTimeDurList size: " + curTimeDurList.size());
			
			
			// Get the minimum end time of all numOfLoaders time durations
			double minEndTime = Double.MAX_VALUE;
			for(int i = 0; i < numOfLoaders; i ++){
				if(curTimeDurList.get(i).getEndTime() < minEndTime){
					minEndTime = curTimeDurList.get(i).getEndTime();
				}
			}
			
			// Store total dump volume on each dump site for check purpose
			ArrayList<Float> dumpVolume = new ArrayList<Float>();
			for(int i = 0; i < dumpCapacity.getDumpSiteNum(); i ++){
				dumpVolume.add(0f);
			}
			// Process each time duration of all numOfLoaders time durations
			for(int i = 0; i < numOfLoaders; i ++){
				TimeDuration curDur = curTimeDurList.get(i);
				// The shortest time duration
				if(curDur.getEndTime() == minEndTime){					
					queue.remove(new Integer(curDur.getWorkfaceId()));
					curDur.setWorkloadDone(wfMineralCapacity.getWorkfaceCapacity(curDur.getWorkfaceId()));
					curDur.setLevel(level);
					wfMineralCapacity.setWorkfaceCapacity(curDur.getWorkfaceId(), 0);
					curDur.setWorkLeft(wfMineralCapacity.getWorkfaceCapacity(curDur.getWorkfaceId()));
					System.out.println("QUEUE removed :" + curDur.getWorkfaceId());
					timeDurList.get(curDur.getWorkfaceId()).add(curDur);
					
					dumpVolume.set(curDur.getDumpSiteId(), dumpVolume.get(curDur.getDumpSiteId()) + truckNumList.get(i) * truckList.get(0).getPayLoad());
					// Check if the dump capacity is exceeded
					if(dumpVolume.get(curDur.getDumpSiteId()) > dumpCapacity.getDumpSiteCapacity(curDur.getDumpSiteId())){
						curDur.setDumpSiteOverflowMsg("Dump Site " + i + " overflown");
					}
				}else if(minEndTime > curDur.getStartTime() && minEndTime <= curDur.getEndTime()){
					double actualTime = minEndTime - curDur.getStartTime();
					float curTotalWorkload = wfMineralCapacity.getWorkfaceCapacity(curDur.getWorkfaceId());
					float workloadDone = (float) (curTotalWorkload * actualTime / curDur.getDuration());
					wfMineralCapacity.setWorkfaceCapacity(curDur.getWorkfaceId(), curTotalWorkload - workloadDone);
					curDur.setWorkLeft(wfMineralCapacity.getWorkfaceCapacity(curDur.getWorkfaceId()));
					curDur.setDuration(actualTime);
					curDur.setWorkloadDone(workloadDone);
					curDur.setLevel(level);
					timeDurList.get(curDur.getWorkfaceId()).add(curDur);
					
					dumpVolume.set(curDur.getDumpSiteId(), dumpVolume.get(curDur.getDumpSiteId()) + truckNumList.get(i) * truckList.get(0).getPayLoad());
					// Check if the dump capacity is exceeded
					if(dumpVolume.get(curDur.getDumpSiteId()) > dumpCapacity.getDumpSiteCapacity(curDur.getDumpSiteId())){
						curDur.setDumpSiteOverflowMsg("Dump Site " + i + " overflown");
					}
				}
			}
//			if(queue.size() == 1){
//				System.exit(0);
//			}
		}// end of queue
		
		ArrayList<TimeDuration> finTimeDurList = new ArrayList<TimeDuration>();
		for(int i = 0; i < timeDurList.size(); i ++){
			for(int j = 0; j < timeDurList.get(i).size(); j ++){
				finTimeDurList.add(timeDurList.get(i).get(j));
			}
		}
		
		Collections.sort(finTimeDurList);
		
		// Dump data to persistent media
		String fileName = "LHD_";
		switch(actionChosen){
		case I2MineMain.WF_DEPENDENCY:
			fileName += "WF_DEPENDENCY";
			break;
		case I2MineMain.WF_PRIORITY:
			fileName += "WF_PRIORITY";
			break;
		case I2MineMain.WF_SORT:
			fileName += "WF_SORT";
			break;
		case I2MineMain.SHARE_MACHINE:
			fileName += "SHARE_MACHINE";
			break;
		default:
			fileName += actionChosen;
			break;
		}
		fileName += "_output.txt";
		File dumpData = new File(fileName);
		FileWriter fw = new FileWriter(dumpData);
		System.out.println("TIMEdurList size: " + timeDurList.size());
		for(TimeDuration timeDur: finTimeDurList){
			System.out.print(timeDur.toString());
			fw.write(timeDur.toString());
		}
//		for(int i = 0; i < timeDurList.size(); i ++){
//			for(int j = 0; j < timeDurList.get(i).size(); j ++){
//				System.out.print(timeDurList.get(i).get(j).toString());
//				fw.write(timeDurList.get(i).get(j).toString());
//			}
//		}
		fw.close();
		return true;
	}// end of lhd
}
