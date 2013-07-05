package geo.cluster;

import geo.core.DUComparator;
import geo.core.DistanceUnit;
import geo.core.MachineOpInfo;
import geo.core.WorkfaceDistance;
import geo.core.WorkfaceWorkload;
import geo.excel.ExcelReader;
import geo.util.LogTool;

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
 * This class provides methods to cluster a data set.
 * Once one cluster number satisfies requirements. Algorithm stops.
 * 
 * @author Shichao Dong
 * 
 * @version 1.0
 */
public class ClusterTool {
	
	public static int LEVEL = -1;
	
	/**
	 * This method clusters workfaces whose workface distances are stored in the <i>fileName</i> parameter.
	 * @param fileName Text file which stores workface distance matrix
	 * @param numOfWorkfaces The total number of workfaces
	 * @param delimiter The delimiter between two consecutive distance cells.
	 * @return 
	 * @throws IOException
	 */
	public static ArrayList<ArrayList<Integer>> getClustersOfWorkfaces_zhen_new(String fileName, int numOfWorkfaces, String delimiter) throws IOException{
		
		  /* Load a dataset */
	      //Dataset data = FileHandler.loadDataset(new File("workphase.txt"), 5, "\t");
			Dataset data = FileHandler.loadDataset(new File(fileName), numOfWorkfaces, delimiter);
			
			// Register log info
			StringBuilder msgDistMatrix = new StringBuilder();
			msgDistMatrix.append(Thread.currentThread().getStackTrace()[1].toString() + "\n=====Worface Distance Matrix======\n");
			for(int i = 0; i < data.size(); i ++){
				msgDistMatrix.append(data.get(i).toString() + "\n");
			}
			LogTool.log(LEVEL, msgDistMatrix.toString());
			
			
			
			// Get original distance list
			ArrayList<DistanceUnit> originalDistanceList = new ArrayList<DistanceUnit>();
			for(int row = 0; row < data.size(); row ++){
				//System.out.println("row size: " + data.get(row).size());
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
			StringBuilder msgSortedDistList = new StringBuilder();
			msgSortedDistList.append(Thread.currentThread().getStackTrace()[1].toString() + "\n======Sorted Distance Unit======\n");
			for(int i = 0; i < originalDistanceList.size(); i++){
				msgSortedDistList.append(originalDistanceList.get(i).distance + " ");
			}
			msgSortedDistList.append("\n");
			LogTool.log(LEVEL, msgSortedDistList.toString());
			
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
			StringBuilder msgGroupDistList = new StringBuilder();
			msgGroupDistList.append(Thread.currentThread().getStackTrace()[1].toString() + "'\n=======Display Grouped Distance Units=====\n");
			msgGroupDistList.append("Distance Unit Size:" + originalDistanceList.size() + "\n");
			msgGroupDistList.append("Group Size:" + groupDisList.size());
			for(int i = 0; i < groupDisList.size(); i ++){
				for(int j = 0; j < groupDisList.get(i).size(); j ++){
					msgGroupDistList.append(groupDisList.get(i).get(j).distance + "(" + (groupDisList.get(i).get(j).from) + "," + 
							(groupDisList.get(i).get(j).to) + ") ");
				}
				msgGroupDistList.append("\n");
			}
			LogTool.log(LEVEL, msgGroupDistList.toString());
			
			
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
						StringBuilder msgTwoWfProcessed = new StringBuilder();
						msgTwoWfProcessed.append(Thread.currentThread().getStackTrace()[1].toString() + "\n======Processed Two More Workfaces=======\n");
						msgTwoWfProcessed.append("WF1:" + wf1 + " WF2:" + wf2 + "\n");
						msgTwoWfProcessed.append(wfSeq + "\n");
						LogTool.log(LEVEL, msgTwoWfProcessed.toString());
						
						continue;
					}
					
					// Both are processed already
					
					// Register log info
					LogTool.log(LEVEL, Thread.currentThread().getStackTrace()[1].toString() + " Both WFs are processed - WF1 : " + wf1 + " WF2 : " + wf2);
					
					if(groupOfWf[wf1] == groupOfWf[wf2]){
						continue;
					}
					
					ArrayList<Integer> ret1 = getGroupStartEnd(wfSeq, String.valueOf(wf1), groupOfWf);
					ArrayList<Integer> ret2 = getGroupStartEnd(wfSeq, String.valueOf(wf2), groupOfWf);
					int startOf1 = ret1.get(0), endOf1 = ret1.get(1);
					int startOf2 = ret2.get(0), endOf2 = ret2.get(1);
					
					
					String subStr1 = wfSeq.substring(startOf1, endOf1 + 1);
					
					// Register log info
					LogTool.log(LEVEL, Thread.currentThread().getStackTrace()[1].toString() + " Sub WF Seq for WF1 ( " + wf1 + " ) " + subStr1);
					
					String subStr2 = wfSeq.substring(startOf2, endOf2 + 1);
					
					// Register log info
					LogTool.log(LEVEL, Thread.currentThread().getStackTrace()[1].toString() + " Sub WF Seq for WF2 ( " + wf2 + " ) " + subStr2);
					
					// Get sub workface sequence to decide whether subStr1 and subStr2 on the same level or not
					boolean isChanged = false;
//					for(int tmpJ = 0; tmpJ < j; tmpJ ++){
//						int tmpWorkface = groupDisList.get(i).get(tmpJ).from;
//						int tmpWorkface1 = groupDisList.get(i).get(tmpJ).to;
//						
//						if(getWorkfaceIndex(new StringBuilder(subStr1), String.valueOf(tmpWorkface)) != -1 && 
//								getWorkfaceIndex(new StringBuilder(subStr2), String.valueOf(tmpWorkface)) != -1){
//							isChanged = true;
//							if(subStr1.charAt(0) =='(' && subStr1.charAt(1) == '('){
//								subStr1 = wfSeq.substring(startOf1 + 1, endOf1);
//							}
//							
//							if(subStr2.charAt(0) == '(' && subStr2.charAt(1) == '('){
//								subStr2 = wfSeq.substring(startOf2 + 1, endOf2);
//							}
//						}
//						
//						if(isChanged == true)
//							break;
//						
//						if(getWorkfaceIndex(new StringBuilder(subStr1), String.valueOf(tmpWorkface1)) != -1 && 
//								getWorkfaceIndex(new StringBuilder(subStr2), String.valueOf(tmpWorkface1)) != -1){
//							isChanged = true;
//							if(subStr1.charAt(0) =='(' && subStr1.charAt(1) == '('){
//								subStr1 = wfSeq.substring(startOf1 + 1, endOf1);
//							}
//							
//							if(subStr2.charAt(0) == '(' && subStr2.charAt(1) == '('){
//								subStr2 = wfSeq.substring(startOf2 + 1, endOf2);
//							}
//						}
//						
//						if(isChanged == true)
//							break;
//					}
					
					
					
					
					
					for(int tmpJ = 0; tmpJ < j; tmpJ ++){
						
						int tmpWorkface = groupDisList.get(i).get(tmpJ).from;
						
						// Register log info
						LogTool.log(LEVEL, Thread.currentThread().getStackTrace()[1].toString() + " Temp WF(from) : " + tmpWorkface);
						
						if(getWorkfaceIndex(new StringBuilder(subStr1), String.valueOf(tmpWorkface)) != -1){
							isChanged = true;
							if(subStr1.charAt(0) =='(' && subStr1.charAt(1) == '('){
								subStr1 = wfSeq.substring(startOf1 + 1, endOf1);
								
								// Register log info
								LogTool.log(LEVEL, Thread.currentThread().getStackTrace()[1].toString() + " Sub(cutted-from) WF Seq for WF1 ( " + wf1 + " ) " + subStr1);
							}
						}
						if(isChanged == true){
							break;
						}
						tmpWorkface = groupDisList.get(i).get(tmpJ).to;
						
						// Register log info
						LogTool.log(LEVEL, Thread.currentThread().getStackTrace()[1].toString() + " Temp WF(to) : " + tmpWorkface);
						
						if(getWorkfaceIndex(new StringBuilder(subStr1), String.valueOf(tmpWorkface)) != -1){
							isChanged = true;
							if(subStr1.charAt(0) =='(' && subStr1.charAt(1) == '('){
								subStr1 = wfSeq.substring(startOf1 + 1, endOf1);
								
								// Register log info
								LogTool.log(LEVEL, Thread.currentThread().getStackTrace()[1].toString() + " Sub(cutted-to) WF Seq for WF1 ( " + wf1 + " ) " + subStr1);
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
								LogTool.log(LEVEL, Thread.currentThread().getStackTrace()[1].toString() + " Sub(cutted-from) WF Seq for WF2 ( " + wf2 + " ) " + subStr2);
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
								LogTool.log(LEVEL, Thread.currentThread().getStackTrace()[1].toString() + " Sub(cutted-to) WF Seq for WF2 ( " + wf2 + " ) " + subStr2);
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
		System.out.println(wfSeq.toString());
		return null;
	}
	
	/**
	 * 
	 * @param wfSeq 
	 * @param wf
	 * @param groupOfWf
	 * @return
	 */
	private static ArrayList<Integer> getGroupStartEnd(StringBuilder wfSeq, String wf, int[] groupOfWf){

		int start = getWorkfaceIndex(wfSeq, wf), end = start;
		int smallestLeft = start, biggestRight = end;
		
		System.out.println("First time start(wf:"+wf+"):"+start);
		
		for(int i = 1; i <= groupOfWf.length - 1; i++){
			if(groupOfWf[Integer.valueOf(wf)] == groupOfWf[i]){
				if(getWorkfaceIndex(wfSeq, String.valueOf(i)) <= start){
					start = getWorkfaceIndex(wfSeq, String.valueOf(i));
					smallestLeft = i;
//					System.out.println("small-i:"+i);
				}
				if(getWorkfaceIndex(wfSeq, String.valueOf(i)) >= end){
					end = getWorkfaceIndex(wfSeq, String.valueOf(i));
					biggestRight = i;
//					System.out.println("big-i:"+i);
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
	 * @param sb wokface sequence
	 * @param substr workface to be searched
	 * @return the index of seached workface in the workface sequence
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
					String message = Thread.currentThread().getStackTrace()[1].toString() + "Workface Seq: " + sb.toString() + " Workface: " + substr + " Workface index: " + tmpIndex;   
					LogTool.log(LEVEL, message);
					
					return tmpIndex;
				}else{
					//tmpIndex = sb.indexOf(substr, tmpIndex + 1);
					tmpIndex = sb.indexOf(substr, tmp + 1);
					tmp = tmpIndex + 1;
				}
			}
		}

		// Register log info
		String message = Thread.currentThread().getStackTrace()[1].toString() + "Workface Seq: " + sb.toString() + " Workface: " + substr + " Workface index: " + tmpIndex;   
		LogTool.log(LEVEL, message);
		
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
	 * @deprecated this method checks if two <i> HashSet</i> s intersects with each other.
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
	 * @deprecated This method is currently obsolete. Please refer to {@link #getClustersOfWorkfaces_zhen_new(String fileName, int numOfWorkphases, String delimiter)}
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
        
        System.out.println("best cluster num:"+finalClusterNum);
        return finalClusters;
	}/* getClustersOfWorkphases */

	
	// Using zhen's way to cluster
	public static void main(String[] args) throws Exception {
		
		// Determine whether to output debug info or not
		ClusterTool.LEVEL = LogTool.LEVEL_OPEN;
		//ClusterTool.LEVEL = LogTool.LEVEL_CLOSE;
		
		//***************start the grouping workface process********************
		//Dataset[] dss = ClusterTool.getClustersOfWorkfaces("workface-distance.txt", 20, "\t");
    	//System.out.println("***************start the grouping workface process********************");
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
	}// end of method main
	
	
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
//    	
//    	
//    }

}
