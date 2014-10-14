package semgen.encoding;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import semgen.FileFilter;
import semgen.SemGenGUI;
import semgen.resource.uicomponent.ProgressFrame;

public class Coder {
	public OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public File file;
	public OWLOntology ontology;
	
	public String base;
	public File outputfile;

	public String onedom = "";
	public static ProgressFrame progframe;

	public Coder(File afile, String extension, File outputfile, Boolean autoencode) {
		this.file = afile;
		this.outputfile = outputfile;
		try {
			ontology = manager.loadOntologyFromOntologyDocument(file);
		} catch (OWLOntologyCreationException err) {
			SemGenGUI.logfilewriter.println("The ontology could not be created: "+ err.getMessage());
		}
		base = ontology.getOntologyID().toString();
		base = base.substring(base.indexOf("<") + 1, base.indexOf(">")) + "#";
		System.out.println("Base: " + base);
		if(outputfile==null || !autoencode){
			selectOutputFile(extension);
		}
	}


	public File selectOutputFile(String extension) {
		JFileChooser fc2 = new JFileChooser();
		Boolean saveok = false;
		while (!saveok) {
			fc2.setCurrentDirectory(SemGenGUI.currentdirectory);
			fc2.setDialogTitle("Choose location to save output");
			fc2.addChoosableFileFilter(new FileFilter(new String[] { extension }));
			int returnVal2 = fc2.showSaveDialog(SemGenGUI.desktop);
			if (returnVal2 == JFileChooser.APPROVE_OPTION) {
				if(!fc2.getSelectedFile().getAbsolutePath().toLowerCase().endsWith("." + extension)){
					outputfile = new File(fc2.getSelectedFile().getAbsolutePath() + "." + extension);
				}
				else{
					outputfile = new File(fc2.getSelectedFile().getAbsolutePath());
				}
				SemGenGUI.currentdirectory = fc2.getCurrentDirectory();
				// Prompt for overwrite
				if (outputfile.exists()) {
					int overwriteval = JOptionPane.showConfirmDialog(
							SemGenGUI.desktop, "Overwrite existing file?",
							outputfile.getName() + " already exists",
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					saveok = (overwriteval == JOptionPane.OK_OPTION);
				} else {
					saveok = true;
				}
			} else if (returnVal2 == JFileChooser.CANCEL_OPTION) {
				saveok = true;
				outputfile = null;
			}
		}
		return outputfile;
	}
}
