package semgen.encoding;

<<<<<<< HEAD
import java.io.File;

=======



import java.io.File;
import java.io.PrintWriter;
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

<<<<<<< HEAD
import semgen.SemGen;
import semgen.resource.file.FileFilter;
import semgen.resource.file.SemGenOpenFileChooser;

public class Coder {
	protected OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	protected OWLOntology ontology;
	
	public File outputfile;

	public Coder(File afile, String extension, File outputfile, Boolean autoencode) {
		this.outputfile = outputfile;
		try {
			ontology = manager.loadOntologyFromOntologyDocument(afile);	
			String base = ontology.getOntologyID().toString();
			base = base.substring(base.indexOf("<") + 1, base.indexOf(">")) + "#";
			System.out.println("Base: " + base);
		} catch (OWLOntologyCreationException err) {
			SemGen.logfilewriter.println("The ontology could not be created: "+ err.getMessage());
		}

=======
import semgen.FileFilter;
import semgen.ProgressFrame;
import semgen.SemGenGUI;





public class Coder {
	public OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public File file;
	public OWLOntology ontology;
	
	public String base;
	public File outputfile;
	public PrintWriter writer;
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
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
		if(outputfile==null || !autoencode){
			selectOutputFile(extension);
		}
	}

<<<<<<< HEAD
	private void selectOutputFile(String extension) {
		JFileChooser fc2 = new JFileChooser();
		Boolean saveok = false;
		while (!saveok) {
			fc2.setCurrentDirectory(SemGenOpenFileChooser.currentdirectory);
			fc2.setDialogTitle("Choose location to save output");
			fc2.addChoosableFileFilter(new FileFilter(new String[] { extension }));
			int returnVal2 = fc2.showSaveDialog(null);
=======

	public File selectOutputFile(String extension) {
		JFileChooser fc2 = new JFileChooser();
		Boolean saveok = false;
		while (!saveok) {
			fc2.setCurrentDirectory(SemGenGUI.currentdirectory);
			fc2.setDialogTitle("Choose location to save output");
			fc2.addChoosableFileFilter(new FileFilter(new String[] { extension }));
			int returnVal2 = fc2.showSaveDialog(SemGenGUI.desktop);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
			if (returnVal2 == JFileChooser.APPROVE_OPTION) {
				if(!fc2.getSelectedFile().getAbsolutePath().toLowerCase().endsWith("." + extension)){
					outputfile = new File(fc2.getSelectedFile().getAbsolutePath() + "." + extension);
				}
				else{
					outputfile = new File(fc2.getSelectedFile().getAbsolutePath());
				}
<<<<<<< HEAD
				SemGenOpenFileChooser.currentdirectory = fc2.getCurrentDirectory();
				// Prompt for overwrite
				if (outputfile.exists()) {
					int overwriteval = JOptionPane.showConfirmDialog(
							null, "Overwrite existing file?",
=======
				SemGenGUI.currentdirectory = fc2.getCurrentDirectory();
				// Prompt for overwrite
				if (outputfile.exists()) {
					int overwriteval = JOptionPane.showConfirmDialog(
							SemGenGUI.desktop, "Overwrite existing file?",
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
							outputfile.getName() + " already exists",
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (overwriteval == JOptionPane.OK_OPTION) {
						saveok = true;
<<<<<<< HEAD
					} 
=======
					} else {
						saveok = false;
					}
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
				} else {
					saveok = true;
				}
			} else if (returnVal2 == JFileChooser.CANCEL_OPTION) {
				saveok = true;
				outputfile = null;
			}
		}
<<<<<<< HEAD
	}
	
	
=======
		return outputfile;
	}
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
}
