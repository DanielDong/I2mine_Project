package geo.core;

import java.util.ArrayList;

/**
 * Class instance stores the dump site capacity for all dump sites.
 * All the site capacity is stored in an internal list.
 * All the dump sites are indexed from 0, e.g., Value <i>val</i> at index 
 * <i>i</i> in the internal list indicates that dump site with ID <i>i</i>
 * has the capacity of <i>val</i> ton/hour.
 * 
 * @author Dong
 * @version 1.0
 */
public class DumpSiteCapacity {
	private ArrayList<Float> siteCapacityList;
	
	/**
	 * Create an empty DumpSiteCapacity instance.
	 */
	public DumpSiteCapacity(){
		siteCapacityList = new ArrayList<Float>(); 
	}
	
	/**
	 * Add dump site capacity to the internal capacity list.
	 * @param cap To be added capacity.
	 */
	public void addSiteCapacity(float cap){
		siteCapacityList.add(cap);
	}
	
	/**
	 * Get the number of dump sites.
	 * @return The total number of dump sites.
	 */
	public int getDumpSiteNum(){
		return siteCapacityList.size();
	}
	
	/**
	 * Get the dump site capacity by specifying the site ID.
	 * @param siteId The dump site ID.
	 * @return The capacity of the dump site.
	 */
	public float getDumpSiteCapacity(int siteId){
		return siteCapacityList.get(siteId);
	}
}
