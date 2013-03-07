package geo.cluster;

import java.io.File;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.clustering.evaluation.AICScore;
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

	public static int CLUSTER_NUM = 4;
    /**
     * Tests the k-means algorithm with default parameter settings.
     */
    public static void main(String[] args) throws Exception {

        /* Load a dataset */
        Dataset data = FileHandler.loadDataset(new File("workphase.txt"), 5, "\t");
        //Dataset data = FileHandler.loadDataset(new File("iris.data"), 4, ",");
        /*
         * Create a new instance of the KMeans algorithm, with no options
         * specified. By default this will generate 4 clusters.
         */
        Clusterer km = new KMeans(CLUSTER_NUM);
        AICScore aic = new AICScore();
        //SumOfAveragePairwiseSimilarities aic = new SumOfAveragePairwiseSimilarities();
        
        
//        AICScore aic = new AICScore();
        //Clusterer km = new IterativeKMeans(1, 5, aic);
        //IterativeMultiKMeans km = new IterativeMultiKMeans(1, 5, aic);
        //Clusterer km = new KMedoids();
        
        /*
         * Cluster the data, it will be returned as an array of data sets, with
         * each dataset representing a cluster
         */
        Dataset[] clusters = km.cluster(data);
        System.out.println("Cluster count: " + clusters.length);
        for(int j = 0; j < CLUSTER_NUM; j++){
        	for(int i=0; i<clusters[j].size();i++){
        		System.out.println(clusters[j].get(i));
        	}
        	System.out.println("====="+clusters[j].size());

        }
        System.out.println(aic.score(clusters));
    }

}