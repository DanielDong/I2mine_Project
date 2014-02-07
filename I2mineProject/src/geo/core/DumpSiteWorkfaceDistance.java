package geo.core;

import java.util.ArrayList;

/**
 * This class maintains an internal list which stores all the distance lists.
 * Each distance list indicates the distances between a workface and all possible dump sites.
 * The ID of the workface is equal to the index of the corresponding distance list in the internal list.
 * e.g. If there 10 elements in the internal list. Then all these elements have their own index starting from
 * 0. Each element, on its own, is a distance list. Element 9 (the last element) is a distance list and 
 * it records distances between workface of ID 9 and all possible dump sites. If a distance list has, say, 3 elements,
 * then that means there are in total 3 dump sites with dump site ID 0, 1, 2.
 *  
 * @author Dong
 * @version 1.0
 */
public class DumpSiteWorkfaceDistance {
	
	// A list which stores all the distance lists.
	ArrayList<ArrayList<Float>> dumpSiteWfDist;
	public DumpSiteWorkfaceDistance(){
		dumpSiteWfDist = new ArrayList<ArrayList<Float>>();
	}
	
	/**
	 * Add a distance list which indicates the distance between a workface and all possible dump sites.
	 * The ID of the workface is equal to the index of this added distance list in the internal list of distance list. 
	 * @param curDumpSiteList Added distance list
	 */
	public void addDumpSiteList(ArrayList<Float> curDumpSiteList){
		dumpSiteWfDist.add(curDumpSiteList);
	}
	
	/**
	 * Get the list of distances between workface indicated by <i>wfId</i> and possible dump sites.
	 * @param wfId Workface ID
	 * @return distance list between workface by <i>wfId</i> and all dump sites.
	 */
	public ArrayList<Float> getDumpSiteDistance(int wfId){
		return dumpSiteWfDist.get(wfId);
	}
}
