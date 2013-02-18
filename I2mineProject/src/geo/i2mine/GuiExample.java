package geo.i2mine;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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
public class GuiExample extends org.eclipse.swt.widgets.Composite {
	private Button goButton;
	private CCombo addressCombo;
	private Browser browser;
	private CLabel statusLable;
	private Button aboutButton;
	private CLabel statusClabel;

	/**
	* Auto-generated main method to display this 
	* org.eclipse.swt.widgets.Composite inside a new Shell.
	*/
	public static void main(String[] args) {
		showGUI();
	}
	
	/**
	* Overriding checkSubclass allows this class to extend org.eclipse.swt.widgets.Composite
	*/	
	protected void checkSubclass() {
	}
	
	/**
	* Auto-generated method to display this 
	* org.eclipse.swt.widgets.Composite inside a new Shell.
	*/
	public static void showGUI() {
		Display display = Display.getDefault();
		Shell shell = new Shell(display);
		GuiExample inst = new GuiExample(shell, SWT.NULL);
		Point size = inst.getSize();
		shell.setLayout(new FillLayout());
		shell.layout();
		if(size.x == 0 && size.y == 0) {
			inst.pack();
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

	public GuiExample(org.eclipse.swt.widgets.Composite parent, int style) {
		super(parent, style);
		initGUI();
	}

	private void initGUI() {
		try {
			FormLayout thisLayout = new FormLayout();
			this.setLayout(thisLayout);
			this.setSize(421, 290);
			this.setOrientation(SWT.HORIZONTAL);
			{
				aboutButton = new Button(this, SWT.PUSH | SWT.CENTER);
				FormData aboutButtonLData = new FormData();
				aboutButtonLData.width = 47;
				aboutButtonLData.height = 27;
				aboutButtonLData.right =  new FormAttachment(1000, 1000, -12);
				aboutButtonLData.bottom =  new FormAttachment(1000, 1000, -12);
				aboutButton.setLayoutData(aboutButtonLData);
				aboutButton.setText("About");
				aboutButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent evt) {
						aboutButtonWidgetSelected(evt);
					}
				});
			}
			{
				FormData statusLableLData = new FormData();
				statusLableLData.width = 295;
				statusLableLData.height = 23;
				statusLableLData.bottom =  new FormAttachment(1000, 1000, -12);
				statusLableLData.left =  new FormAttachment(0, 1000, 62);
				statusLableLData.right =  new FormAttachment(1000, 1000, -64);
				statusLable = new CLabel(this, SWT.SHADOW_IN);
				statusLable.setLayoutData(statusLableLData);
			}
			{
				statusClabel = new CLabel(this, SWT.NONE);
				FormData statusClabelLData = new FormData();
				statusClabelLData.left =  new FormAttachment(0, 1000, 9);
				statusClabelLData.width = 41;
				statusClabelLData.height = 23;
				statusClabelLData.bottom =  new FormAttachment(1000, 1000, -12);
				statusClabel.setLayoutData(statusClabelLData);
				statusClabel.setText("Status");
			}
			{
				browser = new Browser(this, SWT.NONE);
				FormData browserLData = new FormData();
				browserLData.top =  new FormAttachment(0, 1000, 55);
				browserLData.width = 397;
				browserLData.height = 183;
				browserLData.right =  new FormAttachment(1000, 1000, -15);
				browserLData.left =  new FormAttachment(0, 1000, 9);
				browserLData.bottom =  new FormAttachment(1000, 1000, -52);
				browser.setLayoutData(browserLData);
				browser.addStatusTextListener(new StatusTextListener() {
					public void changed(StatusTextEvent evt) {
						browserChanged(evt);
					}
				});
			}
			{
				addressCombo = new CCombo(this, SWT.BORDER);
				FormData addressComboLData = new FormData();
				addressComboLData.width = 363;
				addressComboLData.height = 27;
				addressComboLData.left =  new FormAttachment(0, 1000, 9);
				addressComboLData.top =  new FormAttachment(0, 1000, 12);
				addressComboLData.right =  new FormAttachment(1000, 1000, -49);
				addressCombo.setLayoutData(addressComboLData);
				addressCombo.setText("www.youtube.com");
				addressCombo.setBounds(9, 12, 363, 27);
				addressCombo.addKeyListener(new KeyAdapter() {
					public void keyPressed(KeyEvent evt) {
						addressComboKeyPressed(evt);
					}
				});
			}
			{
				goButton = new Button(this, SWT.PUSH | SWT.CENTER);
				FormData goButtonLData = new FormData();
				goButtonLData.top =  new FormAttachment(0, 1000, 12);
				goButtonLData.width = 31;
				goButtonLData.height = 31;
				goButtonLData.right =  new FormAttachment(1000, 1000, -12);
				goButton.setLayoutData(goButtonLData);
				goButton.setText("GO");
				goButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent evt) {
						goButtonWidgetSelected(evt);
					}
				});
			}
			this.layout();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void aboutButtonWidgetSelected(SelectionEvent evt) {
		System.out.println("aboutButton.widgetSelected, event="+evt);
		//TODO add your code for aboutButton.widgetSelected
		AboutDialog about = new AboutDialog(getShell(), SWT.DIALOG_TRIM);
		about.open();
	}
	
	private void browserChanged(StatusTextEvent evt) {
		System.out.println("browser.changed, event="+evt);
		//TODO add your code for browser.changed
		statusLable.setText(evt.text);
	}
	
	private void go() {
	    String url = addressCombo.getText();
	    if(addressCombo.indexOf(url) < 0)
	        addressCombo.add(url);
	    // go to website with this url
	    browser.setUrl(url);
	}
	
	private void addressComboKeyPressed(KeyEvent evt) {
		System.out.println("addressCombo.keyPressed, event="+evt);
		//TODO add your code for addressCombo.keyPressed
		if(evt.keyCode == SWT.CR)
	         go();
	}
	
	private void goButtonWidgetSelected(SelectionEvent evt) {
		System.out.println("goButton.widgetSelected, event="+evt);
		//TODO add your code for goButton.widgetSelected
		go();
	}

}
