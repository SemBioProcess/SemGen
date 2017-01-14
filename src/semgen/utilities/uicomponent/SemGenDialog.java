/** 
 * Abstract class for producing dialogs with consistent behavior.
 */

package semgen.utilities.uicomponent;

import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public abstract class SemGenDialog extends JDialog{
	private static final long serialVersionUID = 1L;

	protected static JFrame location;
	
	public SemGenDialog(String title) {
		super(location, title, true);
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
	
	public SemGenDialog(String title, Frame parent) {
		super(parent, title, true);
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
	
	public static void setFrame(JFrame frame) {
		location = frame;
	}
	
	/**
	 * Call when the dialog is ready for display
	 */
	protected void showDialog() {
		pack();
		setLocationRelativeTo(location);
		setVisible(true);
	}
}
