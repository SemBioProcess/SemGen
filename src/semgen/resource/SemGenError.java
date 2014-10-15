package semgen.resource;

import java.io.File;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import semsim.model.computational.DataStructure;

public class SemGenError {
	public static void showWebConnectionError(JComponent desktop, String location){
		JOptionPane.showMessageDialog(desktop,
				"Please make sure you are online, otherwise the website or service \n" + 
				"may be experiencing difficulties.", "Error connecting to " + location, JOptionPane.ERROR_MESSAGE);
	}
	
	public static void showUnspecifiedAnnotationError(JComponent desktop, Set<DataStructure> unspecds){
		String listofds = "";
		for(DataStructure ds : unspecds){
			listofds = listofds + ds.getName() + "\n";
		}
		JOptionPane.showMessageDialog(desktop, "Please first remove unspecified annotations for the following codewords:\n" + listofds);
	}
	
	public static void showFunctionalSubmodelError(JComponent desktop, File file){
		JOptionPane.showMessageDialog(desktop, "Did not load " + file.getName() + 
		"\n\nSemGen does not support merging of models with CellML-type components yet.");
	}
}
