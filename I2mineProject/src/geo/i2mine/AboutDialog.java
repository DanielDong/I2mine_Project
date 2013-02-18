package geo.i2mine;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

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
public class AboutDialog extends org.eclipse.swt.widgets.Dialog {

	private Shell dialogShell;
	private Button okButton;
	private CLabel cLabel1;
	private CTabFolder cTabFolder1;
	private CTabItem cTabItem1;
	private CTabItem cTabItem2;

	/**
	* Auto-generated main method to display this 
	* org.eclipse.swt.widgets.Dialog inside a new Shell.
	*/
	public static void main(String[] args) {
		try {
			Display display = Display.getDefault();
			Shell shell = new Shell(display);
			AboutDialog inst = new AboutDialog(shell, SWT.NULL);
			inst.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public AboutDialog(Shell parent, int style) {
		super(parent, style);
	}

	public void open() {
		try {
			Shell parent = getParent();
			dialogShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

			dialogShell.setLayout(new FormLayout());
			dialogShell.layout();
			dialogShell.pack();			
			dialogShell.setSize(386, 359);
			dialogShell.setOrientation(SWT.HORIZONTAL);
			{
				cTabFolder1 = new CTabFolder(dialogShell, SWT.NONE);
				FormData cTabFolder1LData = new FormData();
				cTabFolder1LData.width = 152;
				cTabFolder1LData.height = 69;
				cTabFolder1LData.left =  new FormAttachment(0, 1000, 89);
				cTabFolder1LData.top =  new FormAttachment(0, 1000, 100);
				cTabFolder1.setLayoutData(cTabFolder1LData);
				{
					cTabItem1 = new CTabItem(cTabFolder1, SWT.NONE);
					cTabItem1.setText("cTabItem1");
					{
						cLabel1 = new CLabel(cTabFolder1, SWT.NONE);
						cTabItem1.setControl(cLabel1);
						cLabel1.setText("A simple SWT example");
					}
				}
				{
					cTabItem2 = new CTabItem(cTabFolder1, SWT.NONE);
					cTabItem2.setText("cTabItem2");
				}
				cTabFolder1.setSelection(0);
			}
			{
				okButton = new Button(dialogShell, SWT.PUSH | SWT.CENTER);
				FormData okButtonLData = new FormData();
				okButtonLData.width = 65;
				okButtonLData.height = 27;
				okButtonLData.bottom =  new FormAttachment(1000, 1000, -24);
				okButtonLData.right =  new FormAttachment(1000, 1000, -12);
				okButton.setLayoutData(okButtonLData);
				okButton.setText("OK");
				okButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent evt) {
						System.out.println("okButton.widgetSelected, event="+evt);
						//TODO add your code for okButton.widgetSelected
						dialogShell.dispose();
					}
				});
			}
			dialogShell.setLocation(getParent().toDisplay(100, 100));
			dialogShell.open();
			Display display = dialogShell.getDisplay();
			while (!dialogShell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
