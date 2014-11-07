package semgen.encoding;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import semgen.SemGen;
import semgen.resource.file.FileFilter;
import semgen.resource.file.SemGenFileChooser;

public class Coder {
	public OWLOntology ontology;
	public File outputfile;

	public Coder(File file, String extension, File outputfile, Boolean autoencode) {
		this.outputfile = outputfile;
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		try {
			ontology = manager.loadOntologyFromOntologyDocument(file);
		} catch (OWLOntologyCreationException err) {
			SemGen.logfilewriter.println("The ontology could not be created: "+ err.getMessage());
		}
		String base = ontology.getOntologyID().toString();
		base = base.substring(base.indexOf("<") + 1, base.indexOf(">")) + "#";
		System.out.println("Base: " + base);
		if(outputfile==null || !autoencode){
			selectOutputFile(extension);
		}
	}

	public File selectOutputFile(String extension) {
		JFileChooser fc2 = new JFileChooser();
		fc2.setCurrentDirectory(SemGenFileChooser.currentdirectory);
		fc2.setDialogTitle("Choose location to save output");
		fc2.addChoosableFileFilter(new FileFilter(new String[] { extension }));
		Boolean saveok = false;
		
		while (!saveok) {
			int returnVal2 = fc2.showSaveDialog(null);
			if (returnVal2 == JFileChooser.APPROVE_OPTION) {
				if(!fc2.getSelectedFile().getAbsolutePath().toLowerCase().endsWith("." + extension)){
					outputfile = new File(fc2.getSelectedFile().getAbsolutePath() + "." + extension);
				}
				else{
					outputfile = new File(fc2.getSelectedFile().getAbsolutePath());
				}
				SemGenFileChooser.currentdirectory = fc2.getCurrentDirectory();
				// Prompt for overwrite
				if (outputfile.exists()) {
					int overwriteval = JOptionPane.showConfirmDialog(
							null, "Overwrite existing file?",
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
