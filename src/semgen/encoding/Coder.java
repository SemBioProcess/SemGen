package semgen.encoding;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

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

		if(outputfile==null || !autoencode){
			selectOutputFile(extension);
		}
	}

	private void selectOutputFile(String extension) {
		JFileChooser fc2 = new JFileChooser();
		Boolean saveok = false;
		while (!saveok) {
			fc2.setCurrentDirectory(SemGenOpenFileChooser.currentdirectory);
			fc2.setDialogTitle("Choose location to save output");
			fc2.addChoosableFileFilter(new FileFilter(new String[] { extension }));
			int returnVal2 = fc2.showSaveDialog(null);
			if (returnVal2 == JFileChooser.APPROVE_OPTION) {
				if(!fc2.getSelectedFile().getAbsolutePath().toLowerCase().endsWith("." + extension)){
					outputfile = new File(fc2.getSelectedFile().getAbsolutePath() + "." + extension);
				}
				else{
					outputfile = new File(fc2.getSelectedFile().getAbsolutePath());
				}
				SemGenOpenFileChooser.currentdirectory = fc2.getCurrentDirectory();
				// Prompt for overwrite
				if (outputfile.exists()) {
					int overwriteval = JOptionPane.showConfirmDialog(
							null, "Overwrite existing file?",
							outputfile.getName() + " already exists",
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (overwriteval == JOptionPane.OK_OPTION) {
						saveok = true;
					} 
				} else {
					saveok = true;
				}
			} else if (returnVal2 == JFileChooser.CANCEL_OPTION) {
				saveok = true;
				outputfile = null;
			}
		}
	}
	
	
}
