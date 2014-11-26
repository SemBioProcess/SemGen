package semgen.utilities;

import java.io.File;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import semsim.model.computational.datastructures.DataStructure;

public class SemGenError {
	private static JFrame parent = null;
	
	public static void setFrame(JFrame frame) {
		parent = frame;
	}
	
	public static void showError(String errtext, String errtitle) {
		JOptionPane.showMessageDialog(parent, errtext, errtitle, JOptionPane.ERROR_MESSAGE);
	}
	
	public static void showWebConnectionError(String location){
		JOptionPane.showMessageDialog(parent,
				"Please make sure you are online, otherwise the website or service \n" + 
				"may be experiencing difficulties.", "Error connecting to " + location, JOptionPane.ERROR_MESSAGE);
	}
	
	public static void showUnspecifiedAnnotationError(Set<DataStructure> unspecds){
		String listofds = "";
		for(DataStructure ds : unspecds){
			listofds = listofds + ds.getName() + "\n";
		}
		JOptionPane.showMessageDialog(parent, "Please first remove unspecified annotations for the following codewords:\n" + listofds);
	}
	
	public static void showFunctionalSubmodelError(File file){
		JOptionPane.showMessageDialog(parent, "Did not load " + file.getName() + 
		"\n\nSemGen does not support merging of models with CellML-type components yet.");
	}
	
	public static void showInvalidOPBpropertyError(){
		JOptionPane.showMessageDialog(parent, "That physical property is not valid for the physical entity\n or process specified in this composite annotation.",
				"Invalid annotation", JOptionPane.ERROR_MESSAGE);
	}
}
