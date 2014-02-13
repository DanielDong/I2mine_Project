package geo.util;

import geo.cluster.SortTool;
import geo.cluster.ClusterTool.WfProcUnitStartComparator;
import geo.core.MachineInitialPosition;
import geo.core.MachineOpInfo;
import geo.core.WorkfaceDistance;
import geo.core.WorkfaceProcessUnit;
import geo.core.WorkfaceWorkload;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

/**
 * This class sort workfaces in one group using matrix or traditional method.
 * @author Dong 
 * @version 1.0
 */
public class WorkfaceTest {

	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		// Read in workface distance information
		WorkfaceDistance distance = new WorkfaceDistance(10);
		BufferedReader br = null;
		ArrayList<Double> singleWorkloadInfo = null;
		try{
			String curLine = null;
			String path = "Input_Data_OneGroup\\Workface_Distance.txt";
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
		MachineOpInfo opInfo = new MachineOpInfo(5); // there are in total 5 machines
		ArrayList<Double> singleOpInfo = null;
		try{
			String curLine = null;
			br = new BufferedReader(new FileReader("Input_Data_OneGroup\\Machine_Operating_Info.txt"));
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
		WorkfaceWorkload workload = new WorkfaceWorkload(5, 10);
		ArrayList<Double> singleWorkload = null;
		try{
			String curLine = null;
			br = new BufferedReader(new FileReader("Input_Data_OneGroup\\Workface_Workload.txt"));
			while((curLine = br.readLine()) != null){
				String[] workloadRet = curLine.split("\t");
				singleWorkload = new ArrayList<Double>();
				for(int i = 0; i < 10; i ++){
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
			br = new BufferedReader(new FileReader("Input_Data_OneGroup\\Machine_Initial_Location.txt"));
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
		
		/** Let user input flag value. 1 for matrix method. 2 for traditional method.*/
		System.out.println("Input number: 1 (matrix) or 2(tradition)");
		int flag = 2; // by default, using traditional way of computing time.
		Scanner s = new Scanner(System.in);
		flag = s.nextInt();
		s.close();
		/** If flag value is not 1 or 2, then application exits with error message. */
		if(flag != 1 && flag != 2){
			System.out.println("input number can only be 1(matrix) or 2(tradition)");
			System.exit(0);
		}
		
		/** Start cluster & sort workface. Output the total time using matrix or traditional method.*/
		try {
			long startTime = System.currentTimeMillis();
			getClustersOfWorkfacesSortByOne(10, "\t", opInfo, workload, distance, machineInitPos, flag);
			switch(flag){
			case 1:
				System.out.println("Time during using matrix(s):" + (System.currentTimeMillis() - startTime) / 1000.0);
				break;
			case 2:
				System.out.println("Time during using tradition(s):" + (System.currentTimeMillis() - startTime) / 1000.0);
				break;
			}			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static class WorkfaceDist{
		public int wfid;
		public double dist;
	}
	
	/** Sort increasingly */
	private static class WDComparator implements Comparator<WorkfaceDist>{
		@Override
		public int compare(WorkfaceDist o1, WorkfaceDist o2) {
			return (o1.dist > o2.dist)? 1: ((o1.dist == o2.dist)? 0: -1);
		}
	}
	
	/** This method is dedicated to cluster all workfaces in only one group. */
	public static ArrayList<ArrayList<Integer>> getGroupsOfOne(ArrayList<Integer> groups, WorkfaceDistance distance){
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
		while(i >= 9){
			tmpRetList = new ArrayList<Integer>();
			tmpRetList.add(wdList.get(i - 0).wfid);
			tmpRetList.add(wdList.get(i - 1).wfid);
			tmpRetList.add(wdList.get(i - 2).wfid);
			tmpRetList.add(wdList.get(i - 3).wfid);
			tmpRetList.add(wdList.get(i - 4).wfid);
			tmpRetList.add(wdList.get(i - 5).wfid);
			tmpRetList.add(wdList.get(i - 6).wfid);
			tmpRetList.add(wdList.get(i - 7).wfid);
			tmpRetList.add(wdList.get(i - 8).wfid);
			tmpRetList.add(wdList.get(i - 9).wfid);
			
			retList.add(tmpRetList);
			i -= 10;
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
	 * Sort workfaces in one group using matrix or traditional method. All workfaces are in the same group.
	 * @param wfGroup One group of workfaces to be sorted.
	 * @param distance Distace values between all workfaces.
	 * @param opInfo Operating machines' information (operating speed and moving speed).
	 * @param workload Workload of all operating machines on all workfaces.
	 * @param initPos Initial positions for all operating machines
	 * @param flag Value to indicate which sorting method to use (1 for matrix, 2 for traditional)
	 * @return Sorted groups of workfaces.
	 */
	public static ArrayList<Integer> sortWorkfacesByOneGroup(ArrayList<Integer> wfGroup, WorkfaceDistance distance, MachineOpInfo opInfo, WorkfaceWorkload workload, MachineInitialPosition initPos, int flag){
		ArrayList<ArrayList<Integer>> tmpGroups = new ArrayList<ArrayList<Integer>>(); 
		System.out.println("wfGroup size: " + wfGroup.size());
		tmpGroups = getGroupsOfOne(wfGroup, distance);
		
		// Set each 0 indexed workface value plus one.
		for(int m = 0; m < tmpGroups.size(); m ++){
			for(int n = 0; n < tmpGroups.get(m).size(); n ++){
				tmpGroups.get(m).set(n, tmpGroups.get(m).get(n) + 1);
			}
		}
		
		// Print out.........................
		System.out.println("Printing out tmpGroups..............");
		for(int i = 0; i < tmpGroups.size(); i ++){
			for(int j = 0; j < tmpGroups.get(i).size(); j ++){
				System.out.print(tmpGroups.get(i).get(j) + " ");
			}
			System.out.println();
		}
		System.out.println();
		
		System.out.println("tmpGroups size: " + tmpGroups.size());
		ArrayList<ArrayList<Integer>> tmpSortRet = null;		
		if(tmpGroups.size() > 1){
			ArrayList<ArrayList<Integer>> firstGroupOfWf = new  ArrayList<ArrayList<Integer>>();
			firstGroupOfWf.add(tmpGroups.get(tmpGroups.size() - 1));
			if(flag == 1)
				/** Sort workfaces by using time(operating time and moving time) matrix*/
				tmpSortRet = SortTool.sortWorkfacesByMatrix(distance, firstGroupOfWf, opInfo, workload, initPos);
			else if(flag == 2)
				/** Sort workfaces by using traditional method*/
				tmpSortRet = SortTool.sortWorkfacesByTradition(firstGroupOfWf, opInfo, workload, distance, initPos);
			
			// Print out.........................
			System.out.println("Printing out tmpSortRet..............");
			for(int i = 0; i < tmpSortRet.get(0).size(); i ++){
				System.out.print(tmpSortRet.get(0).get(i) + " ");
			}
			System.out.println();
			
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
			if(flag == 1)
				/** Sort workfaces by using time(operating time and moving time) matrix*/
				tmpSortRet = SortTool.sortWorkfacesByMatrix(distance, tmpGroups, opInfo, workload, initPos);
			else if(flag == 2)
				/** Sort workfaces by using traditional method*/
				tmpSortRet = SortTool.sortWorkfacesByTradition(tmpGroups, opInfo, workload, distance, initPos);
		}
		
		ArrayList<Integer> tmpFinalRet = new ArrayList<Integer>();
		for(int j = 0; j <tmpSortRet.size(); j ++){
			for(int k = 0; k < tmpSortRet.get(j).size(); k ++){
				tmpFinalRet.add(tmpSortRet.get(j).get(k));
			}
		}
		return  tmpFinalRet;
	}
	
	/**
	 * Sort workfaces in one group.
	 * @param numOfWorkfaces The number of workfaces to be sorted.
	 * @param delimiter Delimiter which splits all the values in the text file.
	 * @param opInfo Operating machines' information (operating speed and moving speed)
	 * @param workload Total workload for all operating machines on all workfaces.
	 * @param distance Distance values between all workfaces.
	 * @param initPos Initial positions for all operating machines.
	 * @param flag Flag value indicating which sorting method to use (1 for matrix method, 2 for traditional method).
	 * @return 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void getClustersOfWorkfacesSortByOne(int numOfWorkfaces, String delimiter, MachineOpInfo opInfo, WorkfaceWorkload workload, WorkfaceDistance distance, MachineInitialPosition initPos, int flag) throws IOException, URISyntaxException{
		ArrayList<ArrayList<Integer>> finalRet = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> groups = new ArrayList<ArrayList<Integer>>();
		groups.add(new ArrayList<Integer>());
		for(int i = 0; i < distance.getNumOfWorkface(); i ++){
			groups.get(0).add(i);
		}

		// For each workface group. Each workface group is for one set of operating machines.
		for(int i = 0; i < groups.size(); i ++){
			finalRet.add(sortWorkfacesByOneGroup(groups.get(i), distance, opInfo, workload, initPos, flag));
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

		ArrayList<ArrayList<Integer>> finalWfRet = new ArrayList<ArrayList<Integer>>();
		finalWfRet.add(finalRet.get(0));
		
		for(int i = 0; i < finalRet.size(); i ++){
			printOutVisualInfo(finalWfRet.get(i), workload, opInfo, distance);
		}
	}
	
	/**
	 * Print all sorted workfaces through commandline.
	 * @param ds Sorted workfaces.
	 * @param workload All workload of operating machines on all workfaces.
	 * @param opInfo Operating machines' information(operating speed and moving speed).
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
