package geo.cluster;

import geo.core.DUComparator;
import geo.core.DistanceUnit;
import geo.core.MachineOpInfo;
import geo.core.WorkfaceDistance;
import geo.core.WorkfaceWorkload;
import geo.excel.ExcelReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.tools.data.FileHandler;

/**
 * Use a clustering algorithm (k-means) to cluster a data set.
 * Once one cluster number satisfies requirements. Algorithm stops.
 * 
 * @author Shichao Dong
 * 
 * @version 1.0
 */
public class ClusterTool {
	
	public static ArrayList<ArrayList<Integer>> getClustersOfWorkfaces_zhen_new(String fileName, int numOfWorkphases, String delimiter) throws IOException{
		
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
			
			// *****************Start to create workface grouping using brackets*****************
			StringBuilder wfSeq = new StringBuilder();
			// Record if a workface has been processed
			int[] workProcessed = new int[numOfWorkphases + 1];
			for(int i = 0; i <= numOfWorkphases; i++){
				workProcessed[i] = -1;
			}
			
			// Iterate through sorted distance list
			// For each distance list
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
						System.out.println("wf1:" + wf1 + " wf2:" + wf2);
						System.out.println(wfSeq);
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
							int iStart = getWorkfaceIndex(wfSeq, String.valueOf(proWf));
							int bracketCount = 0;
							
							// Count the number of left - brackets
							while(iStart >= 0){
								if((wfSeq.charAt(iStart) == '(') || (wfSeq.charAt(iStart) == ')')){
									bracketCount ++;
								}
								if((wfSeq.charAt(iStart) == '(' && iStart == 0) || (wfSeq.charAt(iStart) == '(' && wfSeq.charAt(iStart - 1) == ',')){
									break;
								}
								
								iStart --;
							}
							
							// Find the position to insert workface 'unProWf'
							int iEnd = iStart;
							while(iEnd < wfSeq.length()){
								if(wfSeq.charAt(iEnd) == ')'){
									bracketCount --;
								}
								
								if(bracketCount == 0){
									break;
								}
								
								iEnd ++;
							}
//							System.out.println(wfSeq);
//							System.out.println(String.valueOf(unProWf));
//							System.out.println(wfSeq.length() + "*" + iEnd + "*");//+ wfSeq.charAt(iEnd));///////
							wfSeq.insert(iStart, "(");
							if((iEnd + 2) == wfSeq.length()){
								wfSeq.append("," + unProWf + ")");
							}else{
								//System.out.println(wfSeq.length());
								wfSeq.insert(iEnd + 2, "," + unProWf + ")");
							}
							
						}
						workProcessed[unProWf] = (int) groupDisList.get(i).get(j).distance;
						System.out.println("wf1:" + wf1 + " wf2:" + wf2);
						System.out.println(wfSeq);
						continue;
					}
					
					// Both are processed already
					// ..........................
					ArrayList<Integer> ret1 = getGroupStartEnd(wfSeq, String.valueOf(wf1));
					ArrayList<Integer> ret2 = getGroupStartEnd(wfSeq, String.valueOf(wf2));
					int startOf1 = ret1.get(0), endOf1 = ret1.get(1);
					int startOf2 = ret2.get(0), endOf2 = ret2.get(1);
					
					if(startOf1 == startOf2){
						continue;
					}
					
					String subStr1 = wfSeq.substring(startOf1, endOf1 + 1);
					String subStr2 = wfSeq.substring(startOf2, endOf2 + 1);
					
					// Get sub workface sequence
					if(subStr1.charAt(0) =='(' && subStr1.charAt(1) == '('){
						subStr1 = wfSeq.substring(startOf1 + 1, endOf1);
					}
					
					if(subStr2.charAt(0) == '(' && subStr2.charAt(1) == '('){
						subStr2 = wfSeq.substring(startOf2 + 1, endOf2);
					}
					System.out.println("after excluding brackets...");
					
					// Delete useless sub workfaces
					if(startOf1 > startOf2){
						// Delete extra comma so startOf -1 instead of startOf
						wfSeq = wfSeq.delete(startOf1 - 1, endOf1 + 1);
						wfSeq = wfSeq.delete(startOf2 - 1, endOf2 + 1);
					}else{
						wfSeq = wfSeq.delete(startOf2 - 1, endOf2 + 1);
						wfSeq = wfSeq.delete(startOf1 - 1, endOf1 + 1);
					}
					wfSeq.append(",(");
					wfSeq.append(subStr1);
					wfSeq.append(",");
					wfSeq.append(subStr2);
					wfSeq.append(")");
					System.out.println("after appending...");
					System.out.println("wf1:" + wf1 + " wf2:" + wf2);
					
					System.out.println(wfSeq);
					System.out.println("wf1 start:"+startOf1 + " wf1 end:"+endOf1);
					System.out.println("wf2 start:"+startOf2 + " wf2 end:"+endOf2);
					System.out.println(wfSeq);
				}						
			}
		System.out.println(wfSeq.toString());
		return null;
	}
	
	static ArrayList<Integer> getGroupStartEnd(StringBuilder wfSeq, String wf){
		int start = getWorkfaceIndex(wfSeq, wf);
		int bracketCount = 0;
		while(start >= 0){
			if((wfSeq.charAt(start) == '(') || (wfSeq.charAt(start) == ')')){
				bracketCount ++;
			}
			
			
			if((wfSeq.charAt(start) == '(' && start == 0) || ((wfSeq.charAt(start) == '(') && (wfSeq.charAt(start - 1) == ','))){
				break;
			}
			System.out.println("start...");
			start --;
		}

		int end = start + 1;
		while(end < wfSeq.length()){
			if(wfSeq.charAt(end) == ')'){
				bracketCount --;
			}
			
			if(bracketCount == 0){
				break;
			}
			System.out.println("end...");
			end ++;
		}
		ArrayList<Integer> indexList = new ArrayList<Integer>();
		indexList.add(start);
		indexList.add(end);
		System.out.println("done....");
		return indexList;
	}
	
	static int getWorkfaceIndex(StringBuilder sb, String substr){
		
		//System.out.println("getWorkfaceIndex sb:"+sb);
		//System.out.println("substr:"+substr);
		int tmpIndex = sb.indexOf(substr);
		int tmp = tmpIndex + 1;
		while(tmp < sb.length()){
			
			while((sb.charAt(tmpIndex - 1) >= '0' && sb.charAt(tmpIndex - 1) <= '9')){
				tmpIndex = sb.indexOf(substr, tmpIndex + 1);
			} 
			tmp = tmpIndex + 1;
			
			if(tmp > 0){
				while(sb.charAt(tmp) >= '0' && sb.charAt(tmp) <= '9'){
					tmp ++;
				}
				
				if(substr.compareTo(sb.substring(tmpIndex, tmp)) == 0){
					return tmpIndex;
				}else{
					tmpIndex = sb.indexOf(substr, tmpIndex + 1);
					tmp = tmpIndex + 1;
				}
			}
		}
		return -1;
	}
	
	
	
	
	
	
	
	/**
	 * 
	 * @param fileName
	 * @param numOfWorkphases
	 * @param delimiter
	 * @return
	 * @throws IOException 
	 */
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
		ArrayList improvedGroupWorkface = new ArrayList();
		int i = 0, j = 0;// i for all distance; j for all DistanceUnits for a certain distance
		boolean isAllDuResolved = false, isStartGroup = false;
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
					ArrayList<ArrayList<Integer>> ret2ndGroups = SortTool.sortGroups(tmpSortedWorkfaceGroups, machineOpInfo, workload, distance);
					
					ArrayList<Integer> result1List = ret2ndGroups.get(0);
					result1List.addAll(ret2ndGroups.get(1));
					finalFirstLevelWorkfaceSort.remove(id - 1);
					finalFirstLevelWorkfaceSort.add((id - 1), result1List);
									
					//for(int ti = 0; ti < ret2ndGroups.size(); ti ++){
						//System.out.println("" + level + " level sorted groups:" + ret2ndGroups.get(ti));
						//System.out.println("result1List: " + result1List);
					//}					
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
//		String[] groupIdCopy = new String[groupId.length];
//		for(int gindex = 0; gindex < groupId.length; gindex ++){
//			groupIdCopy[gindex] = new String(groupId[gindex]);
//		}
		
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
						tmpFinalResult = SortTool.sortGroups(tmpFinalResult, machineOpInfo, workload, distance);
						
						finalFirstLevelWorkfaceSort.remove(workfaceGroup1);
						finalFirstLevelWorkfaceSort.remove(workfaceGroup2);
						
						workfaceGroup1 = tmpFinalResult.get(0);
						workfaceGroup1.addAll(tmpFinalResult.get(1));
						finalFirstLevelWorkfaceSort.add(workfaceGroup1);
					}
					
					
					
//					int workfaceId1 = Integer.valueOf(String.valueOf(groupId[groupDisList.get(i).get(j).from].charAt(0)));
//					int workfaceId2 = Integer.valueOf(String.valueOf(groupId[groupDisList.get(i).get(j).to].charAt(0)));
//					if(workfaceId1 == workfaceId2 ){
//						continue;
//					}
//					
//					ArrayList<Integer> workfaceGroup1 = finalFirstLevelWorkfaceSort.get(workfaceId1 - 1);
//					ArrayList<Integer> workfaceGroup2 = finalFirstLevelWorkfaceSort.get(workfaceId2 - 1);
//					ArrayList<ArrayList<Integer>> tmpFinalResult = new ArrayList<ArrayList<Integer>>();
//					tmpFinalResult.add(workfaceGroup1);
//					tmpFinalResult.add(workfaceGroup2);
//					tmpFinalResult = SortTool.sortGroups(tmpFinalResult, machineOpInfo, workload, distance);
//					
//					workfaceGroup1 = tmpFinalResult.get(0);
//					workfaceGroup1.addAll(tmpFinalResult.get(1));
					
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
			
			finalFirstLevelWorkfaceSort = SortTool.sortGroups(finalFirstLevelWorkfaceSort, machineOpInfo, workload, distance);
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
	 * 
	 * @param set1
	 * @param set2
	 * @return
	 */
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
	 * 
	 * @param fileName
	 * @param numOfWorkphases
	 * @param delimiter
	 * @return
	 * @throws IOException
	 */
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
        
        System.out.println("best cluster num:"+finalClusterNum);
        return finalClusters;
	}/* getClustersOfWorkphases */

	
	// Using zhen's way to cluster
	public static void main(String[] args) throws Exception {
		//***************start the grouping workface process********************
    	System.out.println("***************start the grouping workface process********************");
    	ArrayList<ArrayList<Integer>> ds = ClusterTool.getClustersOfWorkfaces_zhen_new("workface-distance.txt", 20, "\t");
    	if(ds == null)
    		System.out.println("ds is null.");
    	else{
    		System.out.println("best cluster num:"+ds.size());
	    	for(int i=0;i<ds.size();i++)
	    		for(int j = 0; j < ds.get(i).size(); j ++){
	    			// workface number is 1 plus ds value
	    			System.out.print((ds.get(i).get(j) + 1 ) + " ");
	    		}
    	}
	}
	
	
	// Using k-means to cluster
//    public static void main(String[] args) throws Exception {
//    	//***************start the grouping workface process********************
//    	System.out.println("***************start the grouping workface process********************");
//    	Dataset[] ds = ClusterTool.getClustersOfWorkfaces("workface-distance.txt", 20, "\t");
//    	if(ds == null)
//    		System.out.println("ds is null.");
//    	else{
//    		System.out.println("best cluster num:"+ds.length);
//	    	for(int i=0;i<ds.length;i++)
//	    		System.out.println(ds[i]);
//    	}
//    	
//    	//***************start the sorting workface process********************
//    	System.out.println("***************start the sorting workface process********************");
//    	ExcelReader er = new ExcelReader();
//    	MachineOpInfo moi = er.readMachineOpInfo("machine-op-info.xls");
//    	WorkfaceWorkload workload = er.readWorkfaceWorkload("workface-workload.xls");
//    	ArrayList<ArrayList<Integer>> sortWorkfaces = SortTool.sortWorkfaces(ds, moi, workload);
//    	for(int i = 0; i < sortWorkfaces.size(); i++){
//    		System.out.println(sortWorkfaces.get(i));
//    	}
//    	
//    	//***************start the sorting region process********************
//    	System.out.println("***************start the sorting region process********************");
//    	WorkfaceDistance distance = er.readWorkfaceDistance("workface-distance.xls");
//    	distance.printDistance();
//    	SortTool.sortGroups(sortWorkfaces, moi, workload, distance);
    	
    	
//    }
    /* main */

}
