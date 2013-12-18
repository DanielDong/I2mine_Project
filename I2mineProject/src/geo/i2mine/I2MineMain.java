package geo.i2mine;
import geo.chart.GanttRender;
import geo.cluster.ClusterTool;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
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
public class I2MineMain extends Composite {
	private static final int WF_PRIORITY = 0;
	private static final int SHARE_MACHINE = 1;
	private static final int WF_DEPENDENCY = 2;
	private static final int WF_SORT = 3;
	private static int actionChosen = -1;
	private int numOfWf;
	private String wfDistancePath;
	
	//WF_PRIORITY SHARE_MACHINE WF_DEPENDENCY WF_SORT
	{
		//Register as a resource user - SWTResourceManager will
		//handle the obtaining and disposing of resources
		SWTResourceManager.registerResourceUser(this);
	}
	

	private CLabel appTitleLable;
	private TabFolder mainTabfolder;
	private TabItem tabItem1;
	private Label WfDistFileName;
	private TabItem tabItem2, tabItem3;
	private Button BtnReadFile;
	private Label LFileToRead;
	private Button RBtnBySort;
	private Button RBtnByDep;
	private Button RBtnByShare;
	private Button RBtnByPriority;
	private Label LMachineInitPos;
	private Button BtnInitPos;
	private Label MachineIniPos, LWfWorkload, LTruckInfoIcon;
	private Button BtnWfWork;
	private Label LNumofMachineSet;
	private Button BtnPerform;
	private Label WfWorkloadFile, LTotalNumOfTrucks, LPoFileName;
	private Label LMachineOpInfo;
	private Button BtnOpInfo;
	private Label MachineOpInfoFile;
	private Label LWfDistName;
	private Button BtnWfDist;
	private Composite composite1, composite2, composite3;
	private Composite parent;
	private Combo comboDropDownSet;
	private Combo comboDropDownLevel;
	private String actionFilePath;
	private MachineOpInfo opInfo;
	private Button BtnLhd;
	private Button BtnWfDumpCap;
	private Label LWfDumpCap, LNumofWf;
	private Label LDumpWfDistIcon;
	private Label LDumpSiteCapInfo;
	private Button BtnDumpWfDist, BtnTruck, BtnDumpCap;
	private Label LDumpWfDistance, LNumofProc, LWfMineralCapIcon;
	private Label LNumOfMachineSetsFull, LTruckInfo, LDumpCap;
	private Text TxtNumofProc, TxtNumofWf, TxtNumofTruck;
	private WorkfaceWorkload workload;
	private WorkfaceDistance distance;
	private MachineInitialPosition machineInitPos;
	private DumpSiteCapacity dumpSiteCapacity;
	private int numOfTotalTruck;
	private ArrayList<Truck> truckList;
	private DumpSiteWorkfaceDistance dumpSiteWfDistance;
	private WorkfaceMineralCapacity wfMineralCapacity;

	I2MineMain(org.eclipse.swt.widgets.Composite parent, int style) {
		super(parent, style);
		this.parent = parent;
		initGUI();
	}
	
	
	public void initGUI(){
		try{
//			
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
							LWfWorkloadLData.horizontalSpan = 3;
							LWfWorkloadLData.widthHint = 240;
							LWfWorkloadLData.heightHint = 15;
							LWfWorkload.setLayoutData(LWfWorkloadLData);
//							LWfWorkload.setText("label1");
							Image workIcon = new Image(parent.getDisplay(), "icon/cancel.png");
							ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
							LWfWorkload.setImage(new Image(parent.getDisplay(), imgData));
							LWfWorkload.setSize(240, 15);
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
					tabItem2.setText("Perform operations");
					{
					composite2 = new Composite(mainTabfolder, SWT.NONE);
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
							LDumpWfDistIconLData.horizontalSpan = 3;
							LDumpWfDistIconLData.widthHint = 31;
							LDumpWfDistIconLData.heightHint = 15;
							LDumpWfDistIcon.setLayoutData(LDumpWfDistIconLData);
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
			errorBox.setMessage("You must choose the workface workload file.");
			errorBox.open();
		}
	}
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
		FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.NULL);
		String path = fileDialog.open();
		if(path != null){
			Image workIcon = new Image(parent.getDisplay(), "icon/accept.png");
			ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
			LMachineOpInfo.setImage(new Image(parent.getDisplay(), imgData));
			
			// Read in machine operation information
			int numOfProc = Integer.valueOf(TxtNumofProc.getText().trim());
			opInfo = new MachineOpInfo(numOfProc); // there are in total numOfProc machines
			ArrayList<Double> singleOpInfo = null;
			BufferedReader br = null;
			try{
				String curLine = null;
				br = new BufferedReader(new FileReader(path));
				while((curLine = br.readLine()) != null){
					
					String[] opRet = curLine.split("\t");
					singleOpInfo = new ArrayList<Double>();
					singleOpInfo.add(Double.valueOf(opRet[0]));
					singleOpInfo.add(Double.valueOf(opRet[1]));
					opInfo.addMachineOpInfo(singleOpInfo);
					singleOpInfo = null;
				}
				
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
		FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.NULL);
		String path = fileDialog.open();
		if(path != null){
			Image workIcon = new Image(parent.getDisplay(), "icon/accept.png");
			ImageData imgData = workIcon.getImageData().scaledTo(15, 15);
			LWfDistName.setImage(new Image(parent.getDisplay(), imgData));
			
			wfDistancePath = path;
			// Read in distance matrix file
			numOfWf = Integer.valueOf(TxtNumofWf.getText().trim());
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
	
	private void BtnPerformMouseDown(MouseEvent evt){
		System.out.println("BtnReadFile.mouseDown, event="+evt);
		//TODO add your code for BtnReadFile.mouseDown
		//WF_PRIORITY SHARE_MACHINE WF_DEPENDENCY WF_SORT
		switch(actionChosen){
		case WF_PRIORITY:
			if(actionFilePath == null){
				MessageBox errorBox = new MessageBox(parent.getShell());
				errorBox.setMessage("You must choose the workface file.");
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
				errorBox.setMessage("You must choose the related file.");
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
				errorBox.setMessage("You must choose the related file.");
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
					ClusterTool.getClustersOfWorkfaces_zhen_new2(numbOfMachineSet, 20, "\t", opInfo, workload, distance, machineInitPos, wfProcList);
				}
				drawGanttGraph("I2Mine Operating Machine Scheduler", "Schedule by Sorting Workface", "Workface Process", "Time Period", wfProcList.get(0));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			break;
		default:
		}
	}
	
	private void drawGanttGraph(String winTitle, String charTitle, String domain, String range,  ArrayList<WorkfaceProcessUnit> wfProcList){
		// Draw the gantt chart
        GanttRender demo = new GanttRender(winTitle, charTitle, domain, range, wfProcList);
        demo.pack();
        demo.setVisible(true);
        RefineryUtilities.centerFrameOnScreen(demo);
        System.out.println("by priority - draw Gantt finished!!!");
	}
	
	
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
	
	private void RBtnBySortMouseDown(MouseEvent evt) {
		System.out.println("RBtnBySort.mouseDown, event="+evt);
		//TODO add your code for RBtnBySort.mouseDown
		LFileToRead.setText("File to be read: NULL");
		actionChosen = WF_SORT;
		actionFilePath = null;
		LNumOfMachineSetsFull.setVisible(true);
		comboDropDownLevel.setVisible(true);
	}
	
	private void BtnLhdMouseDown(MouseEvent evt) {
		System.out.println("BtnLhd.mouseDown, event="+evt);
		//TODO add your code for BtnLhd.mouseDown
		
	}
	
	private void BtnDumpCapMouseDown(MouseEvent evt) {
		System.out.println("BtnDumpCap.mouseDown, event="+evt);
		//TODO add your code for BtnDumpCap.mouseDown
		FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.NULL);
		String path = fileDialog.open();
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
			LDumpSiteCapInfo.setImage(new Image(parent.getDisplay(), imgData));
			MessageBox errorBox = new MessageBox(parent.getShell());
			errorBox.setMessage("You must choose the <dump site capacity> file.");
			errorBox.open();
		}
	}
	
	private void BtnTruckMouseDown(MouseEvent evt) {
		System.out.println("BtnTruck.mouseDown, event="+evt);
		//TODO add your code for BtnTruck.mouseDown
		try{
			numOfTotalTruck = Integer.valueOf(TxtNumofWf.getText().trim());
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
			LTruckInfoIcon.setImage(new Image(parent.getDisplay(), imgData));
			
			truckList = new ArrayList<Truck>();
			// Read in truck information file
			BufferedReader br = null;
			dumpSiteCapacity = new DumpSiteCapacity();
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
			LTruckInfoIcon.setImage(new Image(parent.getDisplay(), imgData));
			MessageBox errorBox = new MessageBox(parent.getShell());
			errorBox.setMessage("You must choose the <truck information> file.");
			errorBox.open();
		}
	}
	
	private void BtnDumpWfDistMouseDown(MouseEvent evt) {
		System.out.println("BtnDumpWfDist.mouseDown, event="+evt);
		//TODO add your code for BtnDumpWfDist.mouseDown
		FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.NULL);
		String path = fileDialog.open();
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
			LDumpWfDistIcon.setImage(new Image(parent.getDisplay(), imgData));
			MessageBox errorBox = new MessageBox(parent.getShell());
			errorBox.setMessage("You must choose the <dump site workface distance> file.");
			errorBox.open();
		}
	}
	
	private void BtnWfDumpCapMouseDown(MouseEvent evt) {
		System.out.println("BtnWfDumpCap.mouseDown, event="+evt);
		//TODO add your code for BtnWfDumpCap.mouseDown
		
		FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.NULL);
		String path = fileDialog.open();
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
			LWfMineralCapIcon.setImage(new Image(parent.getDisplay(), imgData));
			MessageBox errorBox = new MessageBox(parent.getShell());
			errorBox.setMessage("You must choose the <workface mineral capacity> file.");
			errorBox.open();
		}
	}

}
