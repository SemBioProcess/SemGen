/** 
 * Class for commonly used error messages and a method for producing messages with consistent settings
 */

package semgen.utilities;

import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import semsim.utilities.ErrorLog;

public class SemGenError {
	private static JFrame parent = null;
	
	public static void setFrame(JFrame frame) {
		parent = frame;
	}
	
	public static void showError(String errtext, String errtitle) {
		JOptionPane.showMessageDialog(parent, errtext, errtitle, JOptionPane.ERROR_MESSAGE);
	}
	
	public static void showError(JDialog target, String errtext, String errtitle) {
		JOptionPane.showMessageDialog(target, errtext, errtitle, JOptionPane.ERROR_MESSAGE);
	}
	
	public static void showWebConnectionError(String location){
		JOptionPane.showMessageDialog(parent,
				"Please make sure you are online, otherwise the website or service \n" + 
				"may be experiencing difficulties.", "Error connecting to " + location, JOptionPane.ERROR_MESSAGE);
	}
	
	public static void showFunctionalSubmodelError(String file){
		JOptionPane.showMessageDialog(parent, "Did not load " + file + 
		"\n\nSemGen does not support merging of models with CellML-type components yet.");
	}
	
	public static void showInvalidOPBpropertyError(){
		JOptionPane.showMessageDialog(parent, "That physical property is not valid for the physical entity\n or process specified in this composite annotation.",
				"Invalid annotation", JOptionPane.ERROR_MESSAGE);
	}
	
	public static void showInvalidOPBpropertyError(JDialog target){
		JOptionPane.showMessageDialog(target, "That physical property is not valid for the physical entity\n or process specified in this composite annotation.",
				"Invalid annotation", JOptionPane.ERROR_MESSAGE);
	}
	
	public static boolean showSemSimErrors() {
		if (ErrorLog.hasErrors()) {
			String message = "";	
			ArrayList<String> errors = ErrorLog.getAllErrorsandFlush();
			for (String e : errors) {
				message += e + "\r\n";
			}
			JOptionPane.showMessageDialog(parent, message,
				"SemSim Error", JOptionPane.ERROR_MESSAGE);
			return true;
		}
		return false;
	}
}
