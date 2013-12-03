package geo.chart;

import geo.core.WorkfaceProcessUnit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.awt.event.WindowEvent;

public class GanttRender extends ApplicationFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7404694263273528640L;

	public GanttRender(String winTitle, String charTitle, String domain, String range,  ArrayList<WorkfaceProcessUnit> wfProcList) {

        super(winTitle);

        final IntervalCategoryDataset dataset = createDataset(wfProcList);
        final JFreeChart chart = createChart(charTitle, domain, range, dataset);

        // add the chart to a panel...
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(1500, 840));
        setContentPane(chartPanel);
        System.out.println("Set content pane!!!" + chartPanel);
    }
	
	@Override
	public void windowClosing(final WindowEvent event) {
		if(event.getWindow() == this){
			dispose();
		}
	}

    /**
     * Creates a sample dataset for a Gantt chart.
     *
     * @return The dataset.
     */
    public static IntervalCategoryDataset createDataset(ArrayList<WorkfaceProcessUnit> wfProcList) {

    	if(wfProcList == null){
    		return null;
    	}
    	TaskSeriesCollection collection = new TaskSeriesCollection();
    	// The number of tasks is the same as the number of procedures in each workface
    	int numOfProcedure = wfProcList.get(0).getWfProcList().size();
    	TaskSeries[] taskSeriesArray = new TaskSeries[numOfProcedure];
    	// For each procedure
    	for(int i = 0; i < taskSeriesArray.length; i ++){
    		taskSeriesArray[i] = new TaskSeries("Procedure " + i);
    		// For each operating machine
    		for(int j = 0; j < wfProcList.size(); j ++ ){
    			WorkfaceProcessUnit wpu = wfProcList.get(j);
    			SimpleTimePeriod tp = new SimpleTimePeriod((long)wpu.getWfProcList().get(i).getStartTime(), (long)wpu.getWfProcList().get(i).getEndTime());
    			Task t = new Task("Workface " + wfProcList.get(j).getWfId(), tp);
    			taskSeriesArray[i].add(t);
    		}
    		collection.add(taskSeriesArray[i]);
    	}
    	return collection;
    }

    /**
     * Utility method for creating <code>Date</code> objects to .
     * insert into the gantchart. 
     * 
     * @param day  the date.
     * @param month  the month.
     * @param year  the year.
     *
     * @return a date.
     */
    private static Date date(final int day, final int month, final int year) {

        final Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        final Date result = calendar.getTime();
        return result;

    }
        
    /**
     * Creates a chart.
     * 
     * @param dataset  the dataset.
     * 
     * @return The chart.
     */
    private JFreeChart createChart(String chartTitle, String domain, String range, IntervalCategoryDataset dataset) {
        final JFreeChart chart = ChartFactory.createGanttChart(
            chartTitle,  // chart title
            domain,              // domain axis label
            range,              // range axis label
            dataset,             // data
            true,                // include legend
            true,                // tooltips
            false                // urls
        );    
        System.out.println("return chart!!" + chart);
        return chart;    
    }
    
    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     */
    public static void main(final String[] args) {

        GanttRender demo = new GanttRender("Gantt Chart D 1", "chart title", "domain", "range", null);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
    }

}
