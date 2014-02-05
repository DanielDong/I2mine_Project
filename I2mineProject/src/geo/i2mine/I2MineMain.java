package geo.i2mine;
import geo.chart.GanttRender;
import geo.cluster.ClusterTool;
import geo.cluster.LHD;
import geo.core.DumpSiteCapacity;
import geo.core.DumpSiteWorkfaceDistance;
import geo.core.MachineInitialPosition;
import geo.core.MachineOpInfo;
import geo.core.ShareMachineUnit;
import geo.core.Truck;
import geo.core.WorkfaceDependancy;
import geo.core.WorkfaceDistance;
import geo.core.WorkfaceMineralCapacity;
import geo.core.WorkfacePriority;
import geo.core.WorkfaceProcessUnit;
import geo.core.WorkfaceWorkload;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.jfree.ui.RefineryUtilities;

import com.cloudgarden.resource.SWTResourceManager;


/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
/**
 * This UI is the entry point of the whole application.
 */
public class I2MineMain extends Composite {
	
	// 4 operations to perform on workfaces.
	public static final int WF_PRIORITY = 0;
	public static final int SHARE_MACHINE = 1;
	public static final int WF_DEPENDENCY = 2;
	public static final int WF_SORT = 3;
	// Records which operation the user chooses
	private static int actionChosen = -1, oldActionChosen = -1;
	
	// The total number of workfaces.
	private int numOfWf;
	// distance file path
	private String wfDistancePath;
	// A WorkfaceWorkload instance stores workloads for all the workfaces.
	private WorkfaceWorkload workload;
	// A WorkfaceDistance instance stores distances between each pair of workfaces.
	private WorkfaceDistance distance;
	// A MachineInitialPosition instance stores the initial positions (workfaces) for all operating machines.
	private MachineInitialPosition machineInitPos;
	// A DumpSiteCapacity instance stores dump capacities for all dump sites.
	private DumpSiteCapacity dumpSiteCapacity;
	// total number of trucks.
	private int numOfTotalTruck;
	// A list of Truck instaces.
	private ArrayList<Truck> truckList;
	// A DumpSiteWorkfaceDistance instance stores distances between workfaces and dump sites.
	private DumpSiteWorkfaceDistance dumpSiteWfDistance;
	// A WorkfaceMineralCapacity instance.
	private WorkfaceMineralCapacity wfMineralCapacity;
	// A list of WorkfaceProcessUnit instances. One WorkfaceProcessUnit instace refers to one workface.
	private ArrayList<WorkfaceProcessUnit> finalWfProcList;
	// Total number of loaders.
	private int numOfLoaders;
	private boolean isDistRead = false, isOpInfoRead = false, 
			isWorkloadRead = false, isInitPosRead = false;
	private String actionFilePath;
	// Operating machine information
	private MachineOpInfo opInfo;
	
	private boolean isDumpSiteCap = false, isTruckInfo = false,
			isDumpWfDist = false, isWfMineralCap = false;
	
	{
		//Register as a resource user - SWTResourceManager will
		//handle the obtaining and disposing of resources
		SWTResourceManager.registerResourceUser(this);
	}
	

	private CLabel appTitleLable;
	private TabFolder mainTabfolder;
	private TabItem tabItem1, tabItem2, tabItem3;
	private Button BtnOpInfo, BtnInitPos, BtnReadFile, RBtnBySort, 
				RBtnByDep, RBtnByShare, RBtnByPriority, BtnWfWork, 
				BtnPerform, BtnDumpWfDist, BtnTruck, BtnDumpCap, 
				BtnWfDist, BtnLhd, BtnWfDumpCap;  
	private Label LMachineInitPos, MachineIniPos, LWfWorkload, 
				LTruckInfoIcon, LNumofMachineSet, WfWorkloadFile, 
				LTotalNumOfTrucks, LPoFileName, LMachineOpInfo, 
				MachineOpInfoFile, LWfDistName, LWfDumpCap, LNumofWf, 
				LDumpWfDistIcon, LDumpSiteCapInfo, LNumofLoaders
				,LDumpWfDistance, LNumofProc, LWfMineralCapIcon, 
				LNumOfMachineSetsFull, LTruckInfo, LDumpCap, LFileToRead, 
				WfDistFileName;
	private Composite parent, composite1, composite2, composite3;
	private Combo comboDropDownSet, comboDropDownLevel;
	private Text TxtNumofProc, TxtNumofWf, TxtNumofTruck, TxtNumofLoaders;

	I2MineMain(org.eclipse.swt.widgets.Composite parent, int style) {
		super(parent, style);
		this.parent = parent;
		initGUI();
	}
	
	
	public void initGUI(){
		try{	
			FormLayout thisLayout = new FormLayout();
			this.setLayout(thisLayout);
			this.setSize(500, 300);
			{
				mainTabfolder = new TabFolder(this, SWT.NONE);
				{
					tabItem1 = new TabItem(mainTabfolder, SWT.NONE);
					tabItem1.setText("Read in basic files");
					{
 						composite1 = new Composite(mainTabfolder, SWT.NONE);
						GridLayout composite1Layout = new GridLayout();
						composite1Layout.numColumns = 5;
						composite1.setLayout(composite1Layout);
						tabItem1.setControl(composite1);
						{
							WfDistFileName = new Label(composite1, SWT.NONE);
							WfDistFileName.setText("Workface Distance File:");
						}
						{
							BtnWfDist = new Button(composite1, SWT.PUSH | SWT.CENTER);
							GridData BtnWfDistLData = new GridData();
							BtnWfDist.setLayoutData(BtnWfDistLData);
							BtnWfDist.setText("Browse");
							BtnWfDist.addMouseListener(new MouseAdapter() {
								public void mouseDown(MouseEvent evt) {
									BtnWfDistMouseDown(evt);
								}
							});
						}
						{
							GridData LWfDistNameLData = new GridData();
							LWfDistNameLData.widthHint = 32;
							LWfDistNameLData.heightHint = 15;
							LWfDistName = new Label(composite1, SWT.NONE);
							LWfDistName.setLayoutData(LWfDistNameLData);
//							LWfDistName.setOrientation(SWT.HORIZONTAL);
//							LWfDistName.setText("label3");
//							LWfDistName.setOrientation(SWT.HORIZONTAL);
							Image workIcon = new Image(parent.getDisplay(), "icon/cancel.png");
							ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
							LWfDistName.setImage(new Image(parent.getDisplay(), imgData));
						}
						{
							LNumofWf = new Label(composite1, SWT.NONE);
							GridData LNumofWfLData = new GridData();
							LNumofWf.setLayoutData(LNumofWfLData);
							LNumofWf.setText("Number of Workface:");
						}
						{
							TxtNumofWf = new Text(composite1, SWT.NONE);
							GridData TxtNumofWfLData = new GridData();
							TxtNumofWfLData.widthHint = 34;
							TxtNumofWfLData.heightHint = 15;
							TxtNumofWf.setLayoutData(TxtNumofWfLData);
							TxtNumofWf.setBackground(SWTResourceManager.getColor(230, 230, 230));
						}
						{
							MachineOpInfoFile = new Label(composite1, SWT.NONE);
							GridData MachineOpInfoFileLData = new GridData();
							MachineOpInfoFile.setLayoutData(MachineOpInfoFileLData);
							MachineOpInfoFile.setText("Machine Operating Info File:");
						}
						{
							BtnOpInfo = new Button(composite1, SWT.PUSH | SWT.CENTER);
							GridData BtnOpInfoLData = new GridData();
							BtnOpInfo.setLayoutData(BtnOpInfoLData);
							BtnOpInfo.setText("Browse");
							BtnOpInfo.addMouseListener(new MouseAdapter() {
								public void mouseDown(MouseEvent evt) {
									BtnOpInfoMouseDown(evt);
								}
							});
						}
						{
							GridData LMachineOpInfoLData = new GridData();
							LMachineOpInfoLData.horizontalSpan = 1;
							LMachineOpInfoLData.widthHint = 32;
							LMachineOpInfoLData.heightHint = 15;
							LMachineOpInfo = new Label(composite1, SWT.NONE);
							LMachineOpInfo.setLayoutData(LMachineOpInfoLData);
//							LMachineOpInfo.setOrientation(SWT.HORIZONTAL);
//							LMachineOpInfo.setText("label2");
							Image workIcon = new Image(parent.getDisplay(), "icon/cancel.png");
							ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
							LMachineOpInfo.setImage(new Image(parent.getDisplay(), imgData));
						}
						{
							LNumofProc = new Label(composite1, SWT.NONE);
							GridData LNumofProcLData = new GridData();
							LNumofProc.setLayoutData(LNumofProcLData);
							LNumofProc.setText("Number of Procedure:");
						}
						{
							TxtNumofProc = new Text(composite1, SWT.NONE);
							GridData TxtNumofProcLData = new GridData();
							TxtNumofProcLData.widthHint = 34;
							TxtNumofProcLData.heightHint = 15;
							TxtNumofProc.setLayoutData(TxtNumofProcLData);
							TxtNumofProc.setBackground(SWTResourceManager.getColor(230, 230, 230));
						}
						{
							WfWorkloadFile = new Label(composite1, SWT.NONE);
							GridData WfWorkloadFileLData = new GridData();
							WfWorkloadFile.setLayoutData(WfWorkloadFileLData);
							WfWorkloadFile.setText("Workface Workload File:");
						}
						{
							BtnWfWork = new Button(composite1, SWT.PUSH | SWT.CENTER);
							GridData BtnWfWorkLData = new GridData();
							BtnWfWork.setLayoutData(BtnWfWorkLData);
							BtnWfWork.setText("Browse");
							BtnWfWork.addMouseListener(new MouseAdapter() {
								public void mouseDown(MouseEvent evt) {
									BtnWfWorkMouseDown(evt);
								}
							});
						}
						{
							LWfWorkload = new Label(composite1, SWT.NONE);
							GridData LWfWorkloadLData = new GridData();
							LWfWorkloadLData.widthHint = 39;
							LWfWorkloadLData.heightHint = 15;
							LWfWorkload.setLayoutData(LWfWorkloadLData);
//							LWfWorkload.setText("label1");
							Image workIcon = new Image(parent.getDisplay(), "icon/cancel.png");
							ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
							LWfWorkload.setImage(new Image(parent.getDisplay(), imgData));
							LWfWorkloadLData.horizontalSpan = 3;
						}
						{
							MachineIniPos = new Label(composite1, SWT.NONE);
							GridData MachineIniPosLData = new GridData();
							MachineIniPos.setLayoutData(MachineIniPosLData);
							MachineIniPos.setText("Machine Initial Position File:");
						}
						{
							BtnInitPos = new Button(composite1, SWT.PUSH | SWT.CENTER);
							GridData BtnInitPosLData = new GridData();
							BtnInitPos.setLayoutData(BtnInitPosLData);
							BtnInitPos.setText("Browse");
							BtnInitPos.addMouseListener(new MouseAdapter() {
								public void mouseDown(MouseEvent evt) {
									BtnInitPosMouseDown(evt);
								}
							});
						}
						{
							LMachineInitPos = new Label(composite1, SWT.NONE);
							GridData LMachineInitPosLData = new GridData();
							LMachineInitPosLData.horizontalSpan = 3;
							LMachineInitPosLData.widthHint = 240;
							LMachineInitPosLData.heightHint = 15;
							LMachineInitPos.setLayoutData(LMachineInitPosLData);
//							LMachineInitPos.setText("label4");
							LMachineInitPos.setSize(240, 15);
							Image workIcon = new Image(parent.getDisplay(), "icon/cancel.png");
							ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
							LMachineInitPos.setImage(new Image(parent.getDisplay(), imgData));
//							LMachineInitPos.setOrientation(SWT.HORIZONTAL);
						}
					}
				}
				{
					tabItem2 = new TabItem(mainTabfolder, SWT.NONE);
//					tabItem2.addListener(SWT.MouseDoubleClick|SWT.MouseDown, new Listener(){
//						@Override
//						public void handleEvent(Event event) {
//							if(!isDistRead||!isOpInfoRead||!isWorkloadRead||!isInitPosRead){
//								MessageBox errorBox = new MessageBox(parent.getShell());
//								errorBox.setMessage("You must read in the basic files first.");
//								errorBox.open();
//								
////								composite1.forceFocus();
//							}
//						}
//					});
					
					tabItem2.setText("Perform operations");
					{
					composite2 = new Composite(mainTabfolder, SWT.NONE);
//					composite2.addFocusListener(new FocusListener(){
//
//						@Override
//						public void focusGained(FocusEvent e) {
//							System.out.println("tabItem2 focus gained: " + e);
//							// TODO Auto-generated method stub
//							if(!isDistRead||!isOpInfoRead||!isWorkloadRead||!isInitPosRead){
//								MessageBox errorBox = new MessageBox(parent.getShell());
//								errorBox.setMessage("You must read in the basic files first.");
//								errorBox.open();
//								
////								composite1.forceFocus();
//							}
//						}
//
//						@Override
//						public void focusLost(FocusEvent e) {
//							
//						}
//						
//					});
//					composite2.addMouseListener(new  MouseListener(){
//
//						@Override
//						public void mouseDoubleClick(MouseEvent e) {
//							// TODO Auto-generated method stub
//							
//						}
//
//						@Override
//						public void mouseDown(MouseEvent e) {
//							// TODO Auto-generated method stub
//							if(!isDistRead||!isOpInfoRead||!isWorkloadRead||!isInitPosRead){
//								MessageBox errorBox = new MessageBox(parent.getShell());
//								errorBox.setMessage("You must read in the basic files first.");
//								errorBox.open();
//								
//								composite1.forceFocus();
//							}
//						}
//
//						@Override
//						public void mouseUp(MouseEvent e) {
//							// TODO Auto-generated method stub
//							
//						}
//						
//					});
					
					GridLayout composite2Layout = new GridLayout();
						composite2Layout.numColumns = 4;
						composite2.setLayout(composite2Layout);
						tabItem2.setControl(composite2);
						composite2.setSize(468, 80);
//						tabItem2.setControl(table1);
						{
							RBtnByPriority = new Button(composite2, SWT.RADIO | SWT.LEFT);
							GridData RBtnByPriorityLData = new GridData();
							RBtnByPriorityLData.horizontalSpan = 4;
							RBtnByPriority.setLayoutData(RBtnByPriorityLData);
							RBtnByPriority.setText("Schedule by workface priority");
							RBtnByPriority.addMouseListener(new MouseAdapter() {
								public void mouseDown(MouseEvent evt) {
									RBtnByPriorityMouseDown(evt);
								}
							});
						}
						{
							RBtnByShare = new Button(composite2, SWT.RADIO | SWT.LEFT);
							GridData RBtnByShareLData = new GridData();
							RBtnByShareLData.horizontalSpan = 2;
							RBtnByShare.setLayoutData(RBtnByShareLData);
							RBtnByShare.setText("Schedule by sharing machines");
							RBtnByShare.addMouseListener(new MouseAdapter() {
								public void mouseDown(MouseEvent evt) {
									RBtnByShareMouseDown(evt);
								}
							});
						}
						{
							LNumofMachineSet = new Label(composite2, SWT.NONE);
							GridData LNumofMachineSetLData = new GridData();
							LNumofMachineSet.setLayoutData(LNumofMachineSetLData);
							LNumofMachineSet.setText("Number of Machine Set:");
							LNumofMachineSet.setVisible(false);
						}
						{
							comboDropDownSet = new Combo(composite2, SWT.DROP_DOWN);
							GridData comboDropDownData = new GridData();
							comboDropDownData.verticalSpan = 0;
							comboDropDownData.widthHint = 34;
							comboDropDownData.heightHint = 23;
							comboDropDownSet.setLayoutData(comboDropDownData);
							for(int i = 0; i < 3; i ++){
								comboDropDownSet.add(String.valueOf(i + 1));
							}
							comboDropDownSet.setVisible(false);
						}
						{
							RBtnByDep = new Button(composite2, SWT.RADIO | SWT.LEFT);
							GridData RBtnByDepLData = new GridData();
							RBtnByDepLData.horizontalSpan = 4;
							RBtnByDep.setLayoutData(RBtnByDepLData);
							RBtnByDep.setText("Schedule by workface dependency");
							RBtnByDep.addMouseListener(new MouseAdapter() {
								public void mouseDown(MouseEvent evt) {
									RBtnByDepMouseDown(evt);
								}
							});
						}
						{
							RBtnBySort = new Button(composite2, SWT.RADIO | SWT.LEFT);
							GridData RBtnBySortLData = new GridData();
							RBtnBySortLData.horizontalSpan = 2;
							RBtnBySort.setLayoutData(RBtnBySortLData);
							RBtnBySort.setText("Schedule after Workface Sort");
							RBtnBySort.addMouseListener(new MouseAdapter() {
								public void mouseDown(MouseEvent evt) {
									RBtnBySortMouseDown(evt);
								}
							});
						}
						{
							LNumOfMachineSetsFull = new Label(composite2, SWT.NONE);
							GridData LNumOfMachineSetsFullLData = new GridData();
							LNumOfMachineSetsFull.setLayoutData(LNumOfMachineSetsFullLData);
							LNumOfMachineSetsFull.setText("Number of Machine Set:");
							LNumOfMachineSetsFull.setVisible(false);
						}
						{
							comboDropDownLevel = new Combo(composite2, SWT.DROP_DOWN);
							GridData comboDropDownData = new GridData();
							comboDropDownData.verticalSpan = 0;
							comboDropDownData.widthHint = 34;
							comboDropDownData.heightHint = 23;
							comboDropDownLevel.setLayoutData(comboDropDownData);
							for(int i = 0; i < 3; i ++){
								comboDropDownLevel.add(String.valueOf(i + 1));
							}
							comboDropDownLevel.setVisible(false);
						}
						{
							LFileToRead = new Label(composite2, SWT.NONE);
							GridData LFileToReadLData = new GridData();
							LFileToReadLData.horizontalSpan = 4;
							LFileToReadLData.widthHint = 456;
							LFileToReadLData.heightHint = 15;
							LFileToRead.setLayoutData(LFileToReadLData);
							LFileToRead.setText("File to be read");
							LFileToRead.setVisible(true);
						}
						{
							BtnReadFile = new Button(composite2, SWT.PUSH | SWT.CENTER);
							GridData BtnReadFileLData = new GridData();
							BtnReadFile.setLayoutData(BtnReadFileLData);
							BtnReadFile.setText("Browse");
							BtnReadFile.addMouseListener(new MouseAdapter() {
								public void mouseDown(MouseEvent evt) {
									BtnReadFileMouseDown(evt);
								}
							});
						}
						{
							LPoFileName = new Label(composite2, SWT.NONE);
							GridData LPoFileNameLData = new GridData();
							LPoFileNameLData.widthHint = 172;
							LPoFileNameLData.heightHint = 15;
							LPoFileName.setLayoutData(LPoFileNameLData);
//							LPoFileName.setText("File Name");
							Image workIcon = new Image(parent.getDisplay(), "icon/cancel.png");
							ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
							LPoFileName.setImage(new Image(parent.getDisplay(), imgData));
						}
						{
							BtnPerform = new Button(composite2, SWT.PUSH | SWT.CENTER);
							GridData BtnPerformLData = new GridData();
							BtnPerformLData.horizontalSpan = 4;
							BtnPerform.setLayoutData(BtnPerformLData);
							BtnPerform.setText("Perform");
							BtnPerform.addMouseListener(new MouseAdapter() {
								public void mouseDown(MouseEvent evt) {
									BtnPerformMouseDown(evt);
								}
							});
						}
					}
				}
				{
					tabItem3 = new TabItem(mainTabfolder, SWT.NONE);
					tabItem3.setText("LHD");
					{
						composite3 = new Composite(mainTabfolder, SWT.NONE);
						GridLayout composite3Layout = new GridLayout();
						composite3Layout.numColumns = 5;
						composite3.setLayout(composite3Layout);
						tabItem3.setControl(composite3);
						{
							LDumpCap = new Label(composite3, SWT.NONE);
							LDumpCap.setText("Dump Site Capacity:");
						}
						{
							BtnDumpCap = new Button(composite3, SWT.PUSH | SWT.CENTER);
							GridData BtnDumpCapLData = new GridData();
							BtnDumpCapLData.widthHint = 49;
							BtnDumpCapLData.heightHint = 25;
							BtnDumpCap.setLayoutData(BtnDumpCapLData);
							BtnDumpCap.setText("Browse");
							BtnDumpCap.addMouseListener(new MouseAdapter() {
								public void mouseDown(MouseEvent evt) {
									BtnDumpCapMouseDown(evt);
								}
							});

						}
						{
							GridData LDumpSiteCapInfoLData = new GridData();
							LDumpSiteCapInfoLData.widthHint = 30;
							LDumpSiteCapInfoLData.heightHint = 15;
							LDumpSiteCapInfoLData.horizontalSpan = 3;
							LDumpSiteCapInfo = new Label(composite3, SWT.NONE);
							LDumpSiteCapInfo.setLayoutData(LDumpSiteCapInfoLData);
						}
						{
							LTruckInfo = new Label(composite3, SWT.NONE);
							GridData LTruckInfoLData = new GridData();
							LTruckInfo.setLayoutData(LTruckInfoLData);
							LTruckInfo.setText("Truck Information:");
						}
						{
							BtnTruck = new Button(composite3, SWT.PUSH | SWT.CENTER);
							BtnTruck.setText("Browse");
							BtnTruck.addMouseListener(new MouseAdapter() {
								public void mouseDown(MouseEvent evt) {
									BtnTruckMouseDown(evt);
								}
							});
						}
						{
							LTruckInfoIcon = new Label(composite3, SWT.NONE);
							GridData LTruckInfoIconLData = new GridData();
							LTruckInfoIconLData.widthHint = 31;
							LTruckInfoIconLData.heightHint = 15;
							LTruckInfoIcon.setLayoutData(LTruckInfoIconLData);
						}
						{
							LTotalNumOfTrucks = new Label(composite3, SWT.NONE);
							GridData LTotalNumOfTrucksLData = new GridData();
							LTotalNumOfTrucks.setLayoutData(LTotalNumOfTrucksLData);
							LTotalNumOfTrucks.setText("Total Number of Trucks:");
						}
						{
							TxtNumofTruck = new Text(composite3, SWT.NONE);
							GridData TxtNumofTruckLData = new GridData();
							TxtNumofTruckLData.widthHint = 34;
							TxtNumofTruckLData.heightHint = 15;
							TxtNumofTruck.setLayoutData(TxtNumofTruckLData);
							TxtNumofTruck.setBackground(SWTResourceManager.getColor(230, 230, 230));
						}
						{
							LDumpWfDistance = new Label(composite3, SWT.NONE);
							GridData LDumpWfDistanceLData = new GridData();
							LDumpWfDistance.setLayoutData(LDumpWfDistanceLData);
							LDumpWfDistance.setText("Dump Workface Distance:");
						}
						{
							BtnDumpWfDist = new Button(composite3, SWT.PUSH | SWT.CENTER);
							BtnDumpWfDist.setText("Browse");
							BtnDumpWfDist.addMouseListener(new MouseAdapter() {
								public void mouseDown(MouseEvent evt) {
									BtnDumpWfDistMouseDown(evt);
								}
							});
						}
						{
							LDumpWfDistIcon = new Label(composite3, SWT.NONE);
							GridData LDumpWfDistIconLData = new GridData();
//							LDumpWfDistIconLData.horizontalSpan = 3;
							LDumpWfDistIconLData.widthHint = 31;
							LDumpWfDistIconLData.heightHint = 15;
							LDumpWfDistIcon.setLayoutData(LDumpWfDistIconLData);
						}
						{
							LNumofLoaders = new Label(composite3, SWT.NONE);
							GridData LNumofLoadersLData = new GridData();
							LNumofLoaders.setLayoutData(LNumofLoadersLData);
							LNumofLoaders.setText("Number of Loaders:");
						}
						{
							TxtNumofLoaders = new Text(composite3, SWT.NONE);
							GridData TxtNumofLoadersLData = new GridData();
							TxtNumofLoadersLData.widthHint = 34;
							TxtNumofLoadersLData.heightHint = 15;
							TxtNumofLoaders.setLayoutData(TxtNumofLoadersLData);
							TxtNumofLoaders.setBackground(SWTResourceManager.getColor(230, 230, 230));
						}
						{
							LWfDumpCap = new Label(composite3, SWT.NONE);
							GridData LWfDumpCapLData = new GridData();
							LWfDumpCap.setLayoutData(LWfDumpCapLData);
							LWfDumpCap.setText("Workface Mineral Capacity:");
						}
						{
							BtnWfDumpCap = new Button(composite3, SWT.PUSH | SWT.CENTER);
							BtnWfDumpCap.setText("Browse");
							BtnWfDumpCap.addMouseListener(new MouseAdapter() {
								public void mouseDown(MouseEvent evt) {
									BtnWfDumpCapMouseDown(evt);
								}
							});
						}
						{
							LWfMineralCapIcon = new Label(composite3, SWT.NONE);
							GridData LWfMineralCapIconLData = new GridData();
							LWfMineralCapIconLData.horizontalSpan = 3;
							LWfMineralCapIconLData.widthHint = 34;
							LWfMineralCapIconLData.heightHint = 15;
							LWfMineralCapIcon.setLayoutData(LWfMineralCapIconLData);
						}
						{
							BtnLhd = new Button(composite3, SWT.PUSH | SWT.CENTER);
							GridData BtnLhdLData = new GridData();
							BtnLhdLData.horizontalSpan = 5;
							BtnLhd.setLayoutData(BtnLhdLData);
							BtnLhd.setText("Perform");
							BtnLhd.addMouseListener(new MouseAdapter() {
								public void mouseDown(MouseEvent evt) {
									BtnLhdMouseDown(evt);
								}
							});
						}
					}
				}
				FormData mainTabfolderLData = new FormData();
				mainTabfolderLData.width = 468;
				mainTabfolderLData.height = 199;
				mainTabfolderLData.bottom =  new FormAttachment(1000, 1000, -17);
				mainTabfolderLData.right =  new FormAttachment(1000, 1000, -11);
				mainTabfolderLData.left =  new FormAttachment(27, 1000, 0);
				mainTabfolderLData.top =  new FormAttachment(188, 1000, 0);
				mainTabfolder.setLayoutData(mainTabfolderLData);
				mainTabfolder.setToolTipText("Read Basic Files");
				mainTabfolder.setSelection(0);
			}
			//this.setOrientation(SWT.VERTICAL);
			

			{
				appTitleLable = new CLabel(this, SWT.NONE);
				FormData statusClabelLData = new FormData();
				statusClabelLData.left =  new FormAttachment(0, 1000, 12);
				statusClabelLData.width = 476;
				statusClabelLData.height = 35;
				statusClabelLData.bottom =  new FormAttachment(1000, 1000, -253);
				appTitleLable.setLayoutData(statusClabelLData);
				appTitleLable.setText("I2Mine Operating Machine Scheduler");
				appTitleLable.setEnabled(false);
				appTitleLable.setAlignment(SWT.CENTER);
				appTitleLable.setFont(SWTResourceManager.getFont("Segoe UI", 16, 1, false, false));
			}
			
		
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public static void showGUI(){
		Display display = Display.getDefault();
		Shell shell = new Shell(display, SWT.TITLE|SWT.CLOSE|SWT.MIN);
		I2MineMain i2mineMain = new I2MineMain(shell, SWT.NULL);
		
		Point size = i2mineMain.getSize();
		shell.setLayout(new FillLayout());
		shell.layout();
		if(size.x == 0 && size.y == 0) {
			i2mineMain.pack();
			shell.pack();
		} else {
			Rectangle shellBounds = shell.computeTrim(0, 0, size.x, size.y);
			shell.setSize(shellBounds.width, shellBounds.height);
		}
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		showGUI();
	}
	
	private void BtnInitPosMouseDown(MouseEvent evt){
		System.out.println("BtnWfDist.mouseDown, event="+evt);
		//TODO add your code for BtnWfDist.mouseDown
		FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.NULL);
		String path = fileDialog.open();
		if(path != null){
			Image workIcon = new Image(parent.getDisplay(), "icon/accept.png");
			ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
			LMachineInitPos.setImage(new Image(parent.getDisplay(), imgData));
			
			machineInitPos = new MachineInitialPosition();
			BufferedReader br = null;
			try{
				String curLine = null;
				br = new BufferedReader(new FileReader(path));
				while((curLine = br.readLine()) != null){
					String[] deArr = curLine.split("\t");
					machineInitPos.addIniPosUnit(Integer.valueOf(deArr[0]), Integer.valueOf(deArr[1]));
				}
				// Machine initial position file is read successfully.
				isInitPosRead = true;
			}catch(IOException e){
				e.printStackTrace();
			}finally{
				if(br != null){
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		}else{
			Image workIcon = new Image(parent.getDisplay(), "icon/cancel.png");
			ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
			LMachineInitPos.setImage(new Image(parent.getDisplay(), imgData));
			MessageBox errorBox = new MessageBox(parent.getShell());
			errorBox.setMessage("You must choose the machine initial position file.");
			errorBox.open();
		}
	}
	
	/**
	 * Read in the workface workload file.
	 * 
	 * @param evt
	 */
	private void BtnWfWorkMouseDown(MouseEvent evt){
		System.out.println("BtnWfDist.mouseDown, event="+evt);
		FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.NULL);
		String path = fileDialog.open();
		if(path != null){
			Image workIcon = new Image(parent.getDisplay(), "icon/accept.png");
			ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
			LWfWorkload.setImage(new Image(parent.getDisplay(), imgData));
			
			int numOfProc = Integer.valueOf(TxtNumofProc.getText().trim());
			int numOfWf = Integer.valueOf(TxtNumofWf.getText().trim());
			workload = new WorkfaceWorkload(numOfProc,numOfWf);
			ArrayList<Double> singleWorkload = null;
			BufferedReader br = null;
			try{
				String curLine = null;
				br = new BufferedReader(new FileReader(path));
				while((curLine = br.readLine()) != null){
					String[] workloadRet = curLine.split("\t");
					singleWorkload = new ArrayList<Double>();
					for(int i = 0; i < 20; i ++){
						singleWorkload.add(Double.valueOf(workloadRet[i]));
					}
					workload.addMachineWorkload(singleWorkload);
					singleWorkload = null;
				}
				// Workface workload file is read successfully
				isWorkloadRead = true;
			}catch(IOException e){
				e.printStackTrace();
			}finally{
				if(br != null)
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
			
		}else{
			Image workIcon = new Image(parent.getDisplay(), "icon/cancel.png");
			ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
			LWfWorkload.setImage(new Image(parent.getDisplay(), imgData));
			MessageBox errorBox = new MessageBox(parent.getShell());
			errorBox.setMessage("You must choose the workface workload file.");
			errorBox.open();
		}
	}
	
	private void BtnOpInfoMouseDown(MouseEvent evt){
		System.out.println("BtnWfDist.mouseDown, event="+evt);
		int numOfProc = 0;
		try{
			numOfProc = Integer.valueOf(TxtNumofProc.getText().trim());
		}catch(NumberFormatException n){
			MessageBox errorBox = new MessageBox(parent.getShell());
			errorBox.setMessage("Please specify the <total number of trucks first>.");
			errorBox.open();
			return;
		}
	
		FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.NULL);
		String path = fileDialog.open();
		if(path != null){
			Image workIcon = new Image(parent.getDisplay(), "icon/accept.png");
			ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
			LMachineOpInfo.setImage(new Image(parent.getDisplay(), imgData));
			
			// Read in machine operation information
			opInfo = new MachineOpInfo(numOfProc); // there are in total numOfProc machines
			ArrayList<Double> singleOpInfo = null;
			BufferedReader br = null;
			try{
				String curLine = null;
				br = new BufferedReader(new FileReader(path));
				while((curLine = br.readLine()) != null){
					
					String[] opRet = curLine.split("\t");
					singleOpInfo = new ArrayList<Double>();
					singleOpInfo.add(Double.valueOf(opRet[1]));
					singleOpInfo.add(Double.valueOf(opRet[2]));
					opInfo.addMachineOpInfo(singleOpInfo);
					singleOpInfo = null;
				}
				// Machine operating information is read successfully.
				isOpInfoRead = true; 
			}catch(IOException e){
				e.printStackTrace();
			}finally{
				if(br != null)
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}else{
			Image workIcon = new Image(parent.getDisplay(), "icon/cancel.png");
			ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
			LMachineOpInfo.setImage(new Image(parent.getDisplay(), imgData));
			MessageBox errorBox = new MessageBox(parent.getShell());
			errorBox.setMessage("You must choose the machine operating information file.");
			errorBox.open();
		}
	}
	
	private void BtnWfDistMouseDown(MouseEvent evt) {
		System.out.println("BtnWfDist.mouseDown, event="+evt);
		
		try{
			numOfWf = Integer.valueOf(TxtNumofWf.getText().trim());
		}catch(NumberFormatException n){
			MessageBox errorBox = new MessageBox(parent.getShell());
			errorBox.setMessage("Please specify the <total number of workfaces first>.");
			errorBox.open();
			return;
		}
		
		FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.NULL);
		String path = fileDialog.open();
		if(path != null){
			Image workIcon = new Image(parent.getDisplay(), "icon/accept.png");
			ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
			LWfDistName.setImage(new Image(parent.getDisplay(), imgData));
			
			wfDistancePath = path;
			// Read in distance matrix file
			distance = new WorkfaceDistance(numOfWf);
			BufferedReader br = null;
			ArrayList<Double> singleWorkloadInfo = null;
			try{
				String curLine = null;
				br = new BufferedReader(new FileReader(path));
				while((curLine = br.readLine()) != null){
					
					String[] distRet = curLine.split("\t");
					System.out.println("workload line size: " + distRet.length);
					singleWorkloadInfo = new ArrayList<Double>(); 
					for(int i = 0; i < distRet.length; i ++){
						singleWorkloadInfo.add(Double.valueOf(distRet[i]));
					}
					distance.addDistance(singleWorkloadInfo);
				}
				// Workface distance file is read successfully.
				isDistRead = true;
			}catch(IOException e){
				e.printStackTrace();
			}finally{
				if(br != null){
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}else{
			Image workIcon = new Image(parent.getDisplay(), "icon/cancel.png");
			ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
			LWfDistName.setImage(new Image(parent.getDisplay(), imgData));
			MessageBox errorBox = new MessageBox(parent.getShell());
			errorBox.setMessage("You must choose the distance file.");
			errorBox.open();
		}
	}
	
	private void BtnReadFileMouseDown(MouseEvent evt) {
		System.out.println("BtnReadFile.mouseDown, event="+evt);
		//TODO add your code for BtnReadFile.mouseDown
		FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.NULL);
		String path = fileDialog.open();
		if(path != null){
//			LWfDistName.setText(path);
			actionFilePath = path;
			Image workIcon = new Image(parent.getDisplay(), "icon/accept.png");
			ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
			LPoFileName.setImage(new Image(parent.getDisplay(), imgData));
		}else if(actionChosen == WF_SORT){
			// Workface sort does not need extra files.
		}else{
			Image workIcon = new Image(parent.getDisplay(), "icon/cancel.png");
			ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
			LPoFileName.setImage(new Image(parent.getDisplay(), imgData));
			MessageBox errorBox = new MessageBox(parent.getShell());
			errorBox.setMessage("You must choose the related file.");
			errorBox.open();
		}
	}
	
	/**
	 * Perform the chosen action by user (possible actions are "schedule by priority", 
	 * "schedule by dependancy", "schedule by sharing machines", "schedule by sort")
	 * 
	 * @param evt
	 */
	private void BtnPerformMouseDown(MouseEvent evt){
		System.out.println("BtnReadFile.mouseDown, event="+evt);
		// Check if all the basic files has been read
		if(!isDistRead||!isOpInfoRead||!isWorkloadRead||!isInitPosRead){
			MessageBox errorBox = new MessageBox(parent.getShell());
			errorBox.setMessage("You must read in the basic files first.");
			errorBox.open();
			return;
		}
		
		switch(actionChosen){
		case WF_PRIORITY:
			if(actionFilePath == null){
				MessageBox errorBox = new MessageBox(parent.getShell());
				errorBox.setMessage("You must choose the workface priority file.");
				errorBox.open();
			}else{
				WorkfacePriority wfPriority = new WorkfacePriority();
				BufferedReader bReader = null;
				try{
					bReader = new BufferedReader(new FileReader(actionFilePath));
					String curLine = null;
					while((curLine = bReader.readLine()) != null){
						String[] strArr = curLine.split("\t");
						WorkfacePriority.WorkfacePrioUnit newUnit = new WorkfacePriority.WorkfacePrioUnit(Integer.valueOf(strArr[0]), Integer.valueOf(strArr[1]));
						wfPriority.addWfPrioUnit(newUnit);
					}
					ArrayList<ArrayList<WorkfaceProcessUnit>> wfProcList = new ArrayList<ArrayList<WorkfaceProcessUnit>>(); 
					ClusterTool.getClustersOfWorkfaces_byPriority(numOfWf, "\t",wfPriority, opInfo, workload, distance, machineInitPos, wfProcList);
					drawGanttGraph("I2Mine Operating Machine Scheduler", "Schedule by Workface Priority", "Workface Process", "Time Period", wfProcList.get(0));
					// Set finalWfProcList for LHD usage
					finalWfProcList = wfProcList.get(0);
				} catch(IOException e){
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				
			}
			break;
		case SHARE_MACHINE: 
			if(actionFilePath == null){
				MessageBox errorBox = new MessageBox(parent.getShell());
				errorBox.setMessage("You must choose the machine set file.");
				errorBox.open();
			}else{
				BufferedReader br = null;
				try{
					String curLine = null;
					br = new BufferedReader(new FileReader(actionFilePath));
					LinkedList<String> q = new LinkedList<String>();
					int cntOfLine = 0;
					while((curLine = br.readLine()) != null){
						q.add(curLine);
						cntOfLine ++;
					}
					int numbOfMachineSet = Integer.valueOf(comboDropDownSet.getItem(comboDropDownSet.getSelectionIndex()));
					int numOfProc = cntOfLine / numbOfMachineSet;
					ShareMachineUnit shareUnit = new ShareMachineUnit();
					for(int i = 0; i < cntOfLine; i ++){
						int procId = i % numOfProc;
						String[] strArr = q.removeFirst().split("\t");
						String name = strArr[0];
						int machineNum = Integer.valueOf(strArr[1]);
						// Test
						System.out.println("ProcId: " + procId + " machineNum: " + machineNum + " machine name: " + name);
						
						shareUnit.addNewProcedureUnit(procId, machineNum, name);
					}
					ArrayList<WorkfaceProcessUnit> wfProcList = ClusterTool.getClustersOfWorkfacesBySharedMachine(opInfo, workload, distance, machineInitPos, shareUnit);
					drawGanttGraph("I2Mine Operating Machine Scheduler", "Schedule by Sharing Machines", "Workface Process", "Time Period", wfProcList);
					
					// Set finalWfProcList for LHD usage
					finalWfProcList = wfProcList;
					
					// Set label to unused state when finishing gantte drawing
					Image workIcon = new Image(parent.getDisplay(), "icon/cancel.png");
					ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
					LMachineInitPos.setImage(new Image(parent.getDisplay(), imgData));
					
				}catch(IOException e){
					MessageBox errorBox = new MessageBox(parent.getShell());
					errorBox.setMessage("Some IO exception happend when reading file: " + actionFilePath + "." + e.getMessage());
					errorBox.open();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}finally{
					if(br != null){
						try {
							br.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			break;
		case WF_DEPENDENCY: 
			if(actionFilePath == null){
				MessageBox errorBox = new MessageBox(parent.getShell());
				errorBox.setMessage("You must choose the workface dependancy file.");
				errorBox.open();
			}else{
				WorkfaceDependancy wfDependancy = new WorkfaceDependancy();
				BufferedReader br = null;
				
				try{
					String curLine = null;
					br = new BufferedReader(new FileReader(actionFilePath));
					while((curLine = br.readLine()) != null){
						
						String[] deArr = curLine.split("\t");
						wfDependancy.addDependancyUnit(Integer.valueOf(deArr[0]), Integer.valueOf(deArr[1]));
						
					}
					
					ArrayList<ArrayList<WorkfaceProcessUnit>> wfProcList = new ArrayList<ArrayList<WorkfaceProcessUnit>>();
					System.out.println("number_of_procedure:" + opInfo.getMachineNum());
					ClusterTool.getClustersOfWorkfaces_byDependancy(numOfWf, "\t", wfDependancy, opInfo, workload, distance, machineInitPos, wfProcList);
					
					// Set finalWfProcList for LHD usage
					finalWfProcList = wfProcList.get(0);
					
					drawGanttGraph("I2Mine Operating Machine Scheduler", "Schedule by Workface Dependency", "Workface Process", "Time Period", wfProcList.get(0));
				}catch(IOException e){
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}finally{
					if(br != null){
						try {
							br.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			break;
		case WF_SORT: 
			try {
				ArrayList<ArrayList<WorkfaceProcessUnit>> wfProcList = new ArrayList<ArrayList<WorkfaceProcessUnit>>(); 
				int numbOfMachineSet = Integer.valueOf(comboDropDownSet.getItem(comboDropDownLevel.getSelectionIndex()));
				if(numbOfMachineSet == 1){
					ClusterTool.getClustersOfWorkfaces_zhen_new(20, "\t", opInfo, workload, distance, machineInitPos, wfProcList);
				}else{
					ClusterTool.getClustersOfWorkfaces_zhen_new2(numbOfMachineSet, 20, "\t", 
							opInfo, 
							workload, 
							distance, 
							machineInitPos, 
							wfProcList);
				}
				drawGanttGraph("I2Mine Operating Machine Scheduler", "Schedule by Sorting Workface", "Workface Process", "Time Period", wfProcList.get(0));
				
				// Set finalWfProcList for LHD usage
				finalWfProcList = wfProcList.get(0);
				
			} catch (IllegalArgumentException e){
				MessageBox errorBox = new MessageBox(parent.getShell());
				errorBox.setMessage("You should choose a valid machine set number.");
				errorBox.open();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			break;
		default:
			MessageBox errorBox = new MessageBox(parent.getShell());
			errorBox.setMessage("You should first choose an operation, then perform it.");
			errorBox.open();
		}
		oldActionChosen = actionChosen;
		actionChosen = -1;
		RBtnByPriority.setSelection(false);
		RBtnByShare.setSelection(false);
		RBtnByDep.setSelection(false);
		RBtnBySort.setSelection(false);
	}
	
	/** 
	 * Utility function to draw Gantt graph
	 * @param winTitle Title of the window
	 * @param charTitle Title of the graph
	 * @param domain Domain - X axis range 
	 * @param range Range - Y axis range
	 * @param wfProcList Data to be showed in the Gantt graph
	 */
	private void drawGanttGraph(String winTitle, String charTitle, String domain, String range,  ArrayList<WorkfaceProcessUnit> wfProcList){
		// Draw the gantt chart
        GanttRender demo = new GanttRender(winTitle, charTitle, domain, range, wfProcList);
        demo.pack();
        demo.setVisible(true);
        RefineryUtilities.centerFrameOnScreen(demo);
//        System.out.println("by priority - draw Gantt finished!!!");
	}
	
	/**
	 * Choose the "schedule by priority" algorithm
	 * @param evt
	 */
	private void RBtnByPriorityMouseDown(MouseEvent evt){
		System.out.println("RBtnBySort.mouseDown, event="+evt);
		//TODO add your code for RBtnBySort.mouseDown
		LFileToRead.setText("File to be read: Workface Priority File");
		actionChosen = WF_PRIORITY; 
		LNumofMachineSet.setVisible(false);
		comboDropDownSet.setVisible(false);
		LNumOfMachineSetsFull.setVisible(false);
		comboDropDownLevel.setVisible(false);
		actionFilePath = null;
	}
	
	/**
	 * Choose the "schedule by sharing machines" algorithm
	 * @param evt
	 */
	private void RBtnByShareMouseDown(MouseEvent evt){
		System.out.println("RBtnBySort.mouseDown, event="+evt);
		//TODO add your code for RBtnBySort.mouseDown
		LFileToRead.setText("File to be read: Machine Set File");
		actionChosen = SHARE_MACHINE;
		LNumofMachineSet.setVisible(true);
		comboDropDownSet.setVisible(true);
		LNumOfMachineSetsFull.setVisible(false);
		comboDropDownLevel.setVisible(false);
		actionFilePath = null;
	}
	
	/**
	 * Choose the "schedule by dependency" algorithm
	 * @param evt
	 */
	private void RBtnByDepMouseDown(MouseEvent evt){
		System.out.println("RBtnBySort.mouseDown, event="+evt);
		//TODO add your code for RBtnBySort.mouseDown
		LFileToRead.setText("File to be read: Workface Dependency File");
		actionChosen = WF_DEPENDENCY;
		LNumofMachineSet.setVisible(false);
		comboDropDownSet.setVisible(false);
		LNumOfMachineSetsFull.setVisible(false);
		comboDropDownLevel.setVisible(false);
		actionFilePath = null;
	}
	
	/**
	 * Choose the "schedule by sort" algorithm
	 * @param evt
	 */
	private void RBtnBySortMouseDown(MouseEvent evt) {
		System.out.println("RBtnBySort.mouseDown, event="+evt);
		LFileToRead.setText("File to be read: NULL");
		actionChosen = WF_SORT;
		actionFilePath = null;
		LNumOfMachineSetsFull.setVisible(true);
		comboDropDownLevel.setVisible(true);
		LNumofMachineSet.setVisible(false);
		comboDropDownSet.setVisible(false);
	}
	
	/**
	 * Perform the LHD alogirthm after performing operations on all workfaces (i.e., all the workloads on 
	 * all workfaces is finished)
	 * @param evt
	 */
	private void BtnLhdMouseDown(MouseEvent evt) {
		System.out.println("BtnLhd.mouseDown, event="+evt);
		
		// Check if all the necessary files have been read
		if(finalWfProcList == null || finalWfProcList.size() == 0 ||
				distance == null || workload == null || wfMineralCapacity == null ||
				truckList == null || truckList.size() == 0 ||
				opInfo == null || opInfo.getOpInfoList().size() == 0 ||
				machineInitPos == null || machineInitPos.getMachineInitPosList().size() == 0 ||
				dumpSiteWfDistance == null || dumpSiteCapacity == null || 
				numOfTotalTruck <= 0 || numOfLoaders <= 0 || oldActionChosen == -1){
			MessageBox errorBox = new MessageBox(parent.getShell());
			errorBox.setMessage("You must read in all necessary files in tab \"Read in basic files\" and \"Perform operations\".");
			errorBox.open();
			return;
		}
		
		// Each time LHD is run, "dump site capcity" file, "truck information" file, 
		// "dump workface distance" file and "workface mineral capacity" file need to be read.
		if(!isDumpSiteCap||!isTruckInfo||!isDumpWfDist||!isWfMineralCap){
			MessageBox errorBox = new MessageBox(parent.getShell());
			errorBox.setMessage("You must read in \"dump site capcity\" file, " +
					"\"truck information\" file, " +
					"\"dump workface distance\" file" +
					"\"workface mineral capacity\" file. ");
			errorBox.open();
			return;
		}
		
		try {
			boolean res = LHD.lhd(finalWfProcList, 
					distance, 
					workload, 
					wfMineralCapacity, 
					truckList, 
					opInfo, 
					machineInitPos, 
					dumpSiteWfDistance, 
					dumpSiteCapacity, 
					numOfTotalTruck,
					numOfLoaders,
					oldActionChosen);
			isDumpSiteCap = false; 
			isTruckInfo = false;
			isDumpWfDist = false;
			isWfMineralCap = false;
			
			MessageBox errorBox = new MessageBox(parent.getShell());
			errorBox.setMessage("LHD output data has been persisted on the disk.");
			errorBox.open();
			
		} catch (IOException e) {
			e.printStackTrace();
			MessageBox errorBox = new MessageBox(parent.getShell());
			errorBox.setMessage("Encounter an exception when trying to create dump data of LHD: " + e.getMessage());
			errorBox.open();
		}
	}
	
	/**
	 * Read in dump site capacity file
	 * @param evt
	 */
	private void BtnDumpCapMouseDown(MouseEvent evt) {
		System.out.println("BtnDumpCap.mouseDown, event="+evt);
		FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.NULL);
		String path = fileDialog.open();
		// User must choose the dump site capacity file.
		if(path != null){
			Image workIcon = new Image(parent.getDisplay(), "icon/accept.png");
			ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
			LDumpSiteCapInfo.setImage(new Image(parent.getDisplay(), imgData));
			
			// Read in dump site capacity file
			BufferedReader br = null;
			dumpSiteCapacity = new DumpSiteCapacity();
			try{
				String curLine = null;
				br = new BufferedReader(new FileReader(path));
				while((curLine = br.readLine()) != null){
					
					String[] capRet = curLine.split("\t");
					dumpSiteCapacity.addSiteCapacity(Float.valueOf(capRet[1])); 
				}
				
				isDumpSiteCap = true; 
			}catch(IOException e){
				e.printStackTrace();
			}finally{
				if(br != null){
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}else{
			// Show an error if no file is chosen.
			Image workIcon = new Image(parent.getDisplay(), "icon/cancel.png");
			ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
			LDumpSiteCapInfo.setImage(new Image(parent.getDisplay(), imgData));
			MessageBox errorBox = new MessageBox(parent.getShell());
			errorBox.setMessage("You must choose the <dump site capacity> file.");
			errorBox.open();
		}
	}
	
	/**
	 * Read in the truck information file
	 * @param evt
	 */
	private void BtnTruckMouseDown(MouseEvent evt) {
		System.out.println("BtnTruck.mouseDown, event="+evt);
		// User must input the number of trucks used
		try{
			numOfTotalTruck = Integer.valueOf(TxtNumofTruck.getText().trim());
		}catch(NumberFormatException n){
			// Show an error if input is not a valid number.
			MessageBox errorBox = new MessageBox(parent.getShell());
			errorBox.setMessage("Please specify the <total number of trucks first>.");
			errorBox.open();
			return;
		}
		
		FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.NULL);
		String path = fileDialog.open();
		// User must choose the truck information file
		if(path != null){
			Image workIcon = new Image(parent.getDisplay(), "icon/accept.png");
			ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
			LTruckInfoIcon.setImage(new Image(parent.getDisplay(), imgData));
			
			truckList = new ArrayList<Truck>();
			// Read in truck information file
			BufferedReader br = null;
//			dumpSiteCapacity = new DumpSiteCapacity();
			int truckCnt = 0;
			try{
				String curLine = null;
				br = new BufferedReader(new FileReader(path));
				while((curLine = br.readLine()) != null){
					
					String[] truckStr = curLine.split("\t");
					Truck truck = new Truck(truckCnt ++, truckStr[0], 
							Float.valueOf(truckStr[1]),
							Float.valueOf(truckStr[2]),
							Float.valueOf(truckStr[3]));
					truckList.add(truck);
				}
				
				isTruckInfo = true; 
			}catch(IOException e){
				e.printStackTrace();
			}finally{
				if(br != null){
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}else{
			// Show an error if no file is chosen.
			Image workIcon = new Image(parent.getDisplay(), "icon/cancel.png");
			ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
			LTruckInfoIcon.setImage(new Image(parent.getDisplay(), imgData));
			MessageBox errorBox = new MessageBox(parent.getShell());
			errorBox.setMessage("You must choose the <truck information> file.");
			errorBox.open();
		}
	}
	/**
	 * Read in the dump site - workface distance file for later process 
	 * @param evt
	 */
	private void BtnDumpWfDistMouseDown(MouseEvent evt) {
		System.out.println("BtnDumpWfDist.mouseDown, event="+evt);
		
		// User must input the total number of loaders.
		try{
			numOfLoaders = Integer.valueOf(TxtNumofLoaders.getText().trim());
		}catch(NumberFormatException n){
			// Show an error if input is not a valid number
			MessageBox errorBox = new MessageBox(parent.getShell());
			errorBox.setMessage("Please specify the <total number of loaders first>.");
			errorBox.open();
			return;
		}
		
		FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.NULL);
		String path = fileDialog.open();
		// User must choose the dump site - workface distance file
		if(path != null){
			Image workIcon = new Image(parent.getDisplay(), "icon/accept.png");
			ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
			LDumpWfDistIcon.setImage(new Image(parent.getDisplay(), imgData));
			
			dumpSiteWfDistance = new DumpSiteWorkfaceDistance();
			// Read in truck information file
			BufferedReader br = null;
			try{
				String curLine = null;
				br = new BufferedReader(new FileReader(path));
				while((curLine = br.readLine()) != null){
					String[] dumpWfDist = curLine.split("\t");
					ArrayList<Float> curDumpWfDistList = new ArrayList<Float>();
					for(int i = 0; i < dumpWfDist.length; i ++){
						curDumpWfDistList.add(Float.valueOf(dumpWfDist[i]));
					}
					dumpSiteWfDistance.addDumpSiteList(curDumpWfDistList);
				}
				
				isDumpWfDist = true; 
			}catch(IOException e){
				e.printStackTrace();
			}finally{
				if(br != null){
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}else{
			// Show an error if no distance file is chosen.
			Image workIcon = new Image(parent.getDisplay(), "icon/cancel.png");
			ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
			LDumpWfDistIcon.setImage(new Image(parent.getDisplay(), imgData));
			MessageBox errorBox = new MessageBox(parent.getShell());
			errorBox.setMessage("You must choose the <dump site workface distance> file.");
			errorBox.open();
		}
	}
	
	/**
	 * Read in the workface mineral capacities from file to memory for later process.
	 * @param evt
	 */
	private void BtnWfDumpCapMouseDown(MouseEvent evt) {
		System.out.println("BtnWfDumpCap.mouseDown, event="+evt);
		
		FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.NULL);
		String path = fileDialog.open();
		// The user must choose the dump site capacity file.
		if(path != null){
			Image workIcon = new Image(parent.getDisplay(), "icon/accept.png");
			ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
			LWfMineralCapIcon.setImage(new Image(parent.getDisplay(), imgData));
			
			wfMineralCapacity = new WorkfaceMineralCapacity();
			// Read in truck information file
			BufferedReader br = null;
			try{
				String curLine = null;
				br = new BufferedReader(new FileReader(path));
				while((curLine = br.readLine()) != null){
					String[] wfCap = curLine.split("\t");
					wfMineralCapacity.addCapacity(Float.valueOf(wfCap[1]));
				}
				
				isWfMineralCap = true;
			}catch(IOException e){
				e.printStackTrace();
			}finally{
				if(br != null){
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}else{
			// Show an error message if no file is chosen.
			Image workIcon = new Image(parent.getDisplay(), "icon/cancel.png");
			ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
			LWfMineralCapIcon.setImage(new Image(parent.getDisplay(), imgData));
			MessageBox errorBox = new MessageBox(parent.getShell());
			errorBox.setMessage("You must choose the <workface mineral capacity> file.");
			errorBox.open();
		}
	}

}
