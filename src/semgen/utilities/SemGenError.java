/** 
 * Class for commonly used error messages and a method for producing messages with consistent settings
 */

package semgen.utilities;

import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import semgen.SemGen;
import semsim.utilities.ErrorEntry;
import semsim.utilities.ErrorLog;

public class SemGenError {
	private static JFrame parent = null;
	
	public static void setFrame(JFrame frame) {
		parent = frame;
	}
	
	public static void showWarning(String warntext, String warntitle){
		JOptionPane.showMessageDialog(parent, warntext, warntitle, JOptionPane.WARNING_MESSAGE);
	}
	
	public static void showError(String errtext, String errtitle) {
		JOptionPane.showMessageDialog(parent, errtext, errtitle, JOptionPane.ERROR_MESSAGE);
	}
	
	public static void showError(JDialog target, String errtext, String errtitle) {
		JOptionPane.showMessageDialog(target, errtext, errtitle, JOptionPane.ERROR_MESSAGE);
	}
	
	public static void showWebConnectionWarning(String location){
		JOptionPane.showMessageDialog(parent,
				"Please make sure you are online, otherwise the website or service \n" + 
				"may be experiencing difficulties.", "Error connecting to " + location, JOptionPane.WARNING_MESSAGE);
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
	
	public static void showModelsWithImportsNotViewableInProjectTabError(boolean suggestannotator) {
		String msg = "SemGen cannot load models with imported components into a Project Tab yet. ";
		
		if(suggestannotator) msg = msg + "\nPlease try opening in the Annotator.";
		
		JOptionPane.showMessageDialog(SemGen.getSemGenGUI(), msg, null, JOptionPane.WARNING_MESSAGE);
	}
	
	public static boolean showSemSimErrors() {
		if (ErrorLog.hasErrors()) {
			String message = "";
			boolean fatalerror = false;
			ArrayList<ErrorEntry> errors = ErrorLog.getAllErrorsandFlush();
			
			for (ErrorEntry e : errors) {
				
				if(e != null){
					message += e.errmsg + "\r\n";
					if (e.isfatal) fatalerror = true;
				}
			}
			
			System.err.println(message);
			JOptionPane.showMessageDialog(parent, message,
				"SemSim Error", JOptionPane.ERROR_MESSAGE);
			return fatalerror;
		}
		return false;
	}
}
