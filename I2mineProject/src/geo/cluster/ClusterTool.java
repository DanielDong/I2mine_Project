package geo.cluster;

import java.io.File;
import java.util.ArrayList;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.clustering.evaluation.TraceScatterMatrix;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.tools.data.FileHandler;

/**
 * This tutorial shows how to use a clustering algorithm to cluster a data set.
 * 
 * 
 * @author Thomas Abeel
 * 
 */
public class ClusterTool {

	public static int CLUSTER_NUM = 3;
    /**
     * Tests the k-means algorithm with default parameter settings.
     */
    public static void main(String[] args) throws Exception {

        /* Load a dataset */
        Dataset data = FileHandler.loadDataset(new File("workphase.txt"), 5, "\t");
        
        int finalClusterNum = 0;
        boolean isBestResult = false;
        for(int i = 3 ;i < data.size(); i++){
        	
        	System.out.println("i:"+i);
        	
        	Clusterer km = new KMeans(i);
        	Dataset[] clusters = km.cluster(data);
        	
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
        			for(int jt = 0; jt < clusters[j].size(); jt++){
        				double tmpDist = data.get(clusters[j].get(it).getID()).value(jt);
        				excludeId.add(clusters[j].get(it).getID());
        				maxDist = ( tmpDist> maxDist)? tmpDist:0;
        			}
        		}
        		
        		System.out.println("max dist:"+maxDist);
        		
        		//Check if there are points outside this cluster which should be in this cluster
        		boolean isGoodCluster = false;
        		for(int in = 0; in < data.size(); in++){
        			if(excludeId.contains(in) == false){
        				//Calculate distance between points outside this cluster with points in this cluster
        				for(int jn = 0; jn< excludeId.size(); jn++){
        					double cutDist = data.get(in).value(excludeId.get(jn));
        					isGoodCluster = (cutDist > maxDist)? false: true;
        				}
        			}
        		}
        		
        		// This cluster is not a good one
        		if(isGoodCluster == true){
        			isBestResult = false;
        		}
        		// This cluster is good enough
        		else{
        			isBestResult = true;
        		}
        	}
        	
        	if(isBestResult == true){
        		finalClusterNum = i;
        		break;
        	}
        }
        
        System.out.println("best cluster num:"+finalClusterNum);
        
    }/* main */

}