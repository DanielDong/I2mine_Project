package geo.cluster;

import geo.core.MachineOpInfo;
import geo.core.WorkfaceDistance;
import geo.core.WorkfaceWorkload;
import geo.excel.ExcelReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.core.Dataset;
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

	public static Dataset[] getClustersOfWorkfaces(String fileName, int numOfWorkphases, String delimiter) throws IOException{
		
		 /* Load a dataset */
        //Dataset data = FileHandler.loadDataset(new File("workphase.txt"), 5, "\t");
		Dataset data = FileHandler.loadDataset(new File(fileName), numOfWorkphases, delimiter);
		
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

    public static void main(String[] args) throws Exception {
    	//***************start the grouping workface process********************
    	System.out.println("***************start the grouping workface process********************");
    	Dataset[] ds = ClusterTool.getClustersOfWorkfaces("workface-distance.txt", 7, "\t");
    	if(ds == null)
    		System.out.println("ds is null.");
    	else{
    		System.out.println("best cluster num:"+ds.length);
	    	for(int i=0;i<ds.length;i++)
	    		System.out.println(ds[i]);
    	}
    	
    	//***************start the sorting workface process********************
    	System.out.println("***************start the sorting workface process********************");
    	ExcelReader er = new ExcelReader();
    	MachineOpInfo moi = er.readMachineOpInfo("machine-op-info.xls");
    	WorkfaceWorkload workload = er.readWorkfaceWorkload("workface-workload.xls");
    	ArrayList<ArrayList<Integer>> sortWorkfaces = SortTool.sortWorkfaces(ds, moi, workload);
    	for(int i = 0; i < sortWorkfaces.size(); i++){
    		System.out.println(sortWorkfaces.get(i));
    	}
    	
    	//***************start the sorting region process********************
    	System.out.println("***************start the sorting region process********************");
    	WorkfaceDistance distance = er.readWorkfaceDistance("workface-distance.xls");
    	SortTool.sortGroups(sortWorkfaces, moi, workload, distance);
    	
    }/* main */

}