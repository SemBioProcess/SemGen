/** 
 * Abstract class for producing dialogs with consistent behavior.
 */

package semgen.utilities.uicomponent;

import java.awt.Rectangle;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public abstract class SemGenDialog extends JDialog{
	private static final long serialVersionUID = 1L;

	private static JFrame location;
	
	public SemGenDialog(String title) {
		super(location, title, true);
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
	
	public static void setFrame(JFrame frame) {
		frame = location;
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
