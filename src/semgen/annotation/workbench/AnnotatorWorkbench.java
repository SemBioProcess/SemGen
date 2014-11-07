package semgen.annotation.workbench;

import java.io.File;
import java.net.URI;

import javax.swing.JOptionPane;

import semgen.SemGenGUI;
import semgen.resource.CSVExporter;
import semgen.resource.Workbench;
import semsim.model.SemSimModel;

public class AnnotatorWorkbench implements Workbench {
	private SemSimModel semsimmodel;
	public File sourcefile; //File originally loaded at start of Annotation session (could be 
							//in SBML, MML, CellML or SemSim format)

	private boolean modelsaved = true;
	private int lastsavedas = -1;
	
	//Model, modsaved and lastsaved fields are temporary, all operations on the model 
	//being annotated will be transferredto this class and its helper classes
	public AnnotatorWorkbench(File file, SemSimModel model,boolean modsaved, int lastsaved) {
		sourcefile = file;
		semsimmodel = model;
		
	}
	
	@Override
	public void loadModel() {

	}

	public boolean getModelSaved(){
		return modelsaved;
	}
	
	
	public void setModelSaved(boolean val){
		modelsaved = val;
	}
	
	public int getLastSavedAs() {
		return lastsavedas;
	}

	@Override
	public String getCurrentModelName() {
		return semsimmodel.getName();
	}

	public String getModelSourceFile() {
		return semsimmodel.getLegacyCodeLocation();
	}

	@Override
	public File saveModel() {
		return null;
	}

	@Override
	public File saveModelAs() {
		return null;
	}

	public void exportCSV() {
		try {
			new CSVExporter(semsimmodel).exportCodewords();
		} catch (Exception e1) {e1.printStackTrace();} 
	}
	
	public boolean unsavedChanges() {
		if (!getModelSaved()) {
			String title = "[unsaved file]";
			URI fileURI = sourcefile.toURI();
			if(fileURI!=null){
				title =  new File(fileURI).getName();
			}
			int returnval= JOptionPane.showConfirmDialog(null,
					"Save changes?", title + " has unsaved changes",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (returnval == JOptionPane.CANCEL_OPTION)
				return false;
			if (returnval == JOptionPane.YES_OPTION) {
				if(!SemGenGUI.SaveAction(this, lastsavedas)){
					return false;
				}
			}
		}
		return true;
	}
}
