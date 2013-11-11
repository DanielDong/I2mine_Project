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
        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 540));
        setContentPane(chartPanel);
        System.out.println("Set content pane!!!" + chartPanel);
    }

    /**
     * Creates a sample dataset for a Gantt chart.
     *
     * @return The dataset.
     */
    public static IntervalCategoryDataset createDataset(ArrayList<WorkfaceProcessUnit> wfProcList) {

    	TaskSeriesCollection collection = new TaskSeriesCollection();
    	// The number of tasks is the same as the number of procedures in each workface
    	int numOfProcedure = wfProcList.get(0).getWfProcList().size();
    	// The number of task series is the same as the number of workfaces.
    	TaskSeries[] taskSeriesArray = new TaskSeries[wfProcList.size()];
    	// For each workface
    	for(int i = 0; i < taskSeriesArray.length; i ++){
    		WorkfaceProcessUnit wpu = wfProcList.get(i);
    		int wfId = wpu.getWfId();
    		taskSeriesArray[i] = new TaskSeries("Workface " + wfId);
    		// For each procedure
    		Task t = null;
    		for(int j = 0; j < numOfProcedure; j ++){
    			WorkfaceProcessUnit.WorkfaceProcedureUnit procUnit = wpu.getWfProcList().get(j); 
    			SimpleTimePeriod tp = new SimpleTimePeriod((long)procUnit.getStartTime(), (long)procUnit.getEndTime());
    			t = new Task("Procedure " + j, tp);
    			taskSeriesArray[i].add(t);
    		}
    		
    		collection.add(taskSeriesArray[i]);
    	}
    	System.out.println("data set generated!!!" + collection);
    	return collection;
    	
    	
//    	
//        TaskSeries s1 = new TaskSeries("Scheduled12");
//        s1.add(new Task("Write Proposal",
//               new SimpleTimePeriod(date(1, Calendar.APRIL, 2001),
//                                    date(5, Calendar.APRIL, 2001))));
//        s1.add(new Task("Obtain Approval",
//               new SimpleTimePeriod(date(6, Calendar.APRIL, 2001),
//                                    date(9, Calendar.APRIL, 2001))));
//        s1.add(new Task("Requirements Analysis",
//               new SimpleTimePeriod(date(10, Calendar.APRIL, 2001),
//                                    date(5, Calendar.MAY, 2001))));
//        s1.add(new Task("Design Phase",
//               new SimpleTimePeriod(date(6, Calendar.MAY, 2001),
//                                    date(30, Calendar.MAY, 2001))));
//        s1.add(new Task("Design Signoff",
//               new SimpleTimePeriod(date(2, Calendar.JUNE, 2001),
//                                    date(2, Calendar.JUNE, 2001))));
//        s1.add(new Task("Alpha Implementation",
//               new SimpleTimePeriod(date(3, Calendar.JUNE, 2001),
//                                    date(31, Calendar.JULY, 2001))));
//        s1.add(new Task("Design Review",
//               new SimpleTimePeriod(date(1, Calendar.AUGUST, 2001),
//                                    date(8, Calendar.AUGUST, 2001))));
//        s1.add(new Task("Revised Design Signoff",
//               new SimpleTimePeriod(date(10, Calendar.AUGUST, 2001),
//                                    date(10, Calendar.AUGUST, 2001))));
//        s1.add(new Task("Beta Implementation",
//               new SimpleTimePeriod(date(12, Calendar.AUGUST, 2001),
//                                    date(12, Calendar.SEPTEMBER, 2001))));
//        s1.add(new Task("Testing",
//               new SimpleTimePeriod(date(13, Calendar.SEPTEMBER, 2001),
//                                    date(31, Calendar.OCTOBER, 2001))));
//        s1.add(new Task("Final Implementation",
//               new SimpleTimePeriod(date(1, Calendar.NOVEMBER, 2001),
//                                    date(15, Calendar.NOVEMBER, 2001))));
//        s1.add(new Task("Signoff",
//               new SimpleTimePeriod(date(28, Calendar.NOVEMBER, 2001),
//                                    date(30, Calendar.NOVEMBER, 2001))));

//        TaskSeriesCollection collection = new TaskSeriesCollection();
//        collection.add(s1);
//        return collection;
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
//    public static void main(final String[] args) {
//
//        final GanttRender demo = new GanttRender("Gantt Chart D 1");
//        demo.pack();
//        RefineryUtilities.centerFrameOnScreen(demo);
//        demo.setVisible(true);
//    }

}
