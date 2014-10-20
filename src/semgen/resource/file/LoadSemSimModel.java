package semgen.resource.file;

import java.io.File;

import javax.swing.JOptionPane;

import semsim.model.SemSimModel;
import semsim.reading.CellMLreader;
import semsim.reading.MMLreader;
import semsim.reading.ModelClassifier;
import semsim.reading.SemSimOWLreader;

public class LoadSemSimModel {
	
	public static SemSimModel loadSemSimModelFromFile(File file) {
		SemSimModel semsimmodel = null;
		int modeltype = ModelClassifier.classify(file);

		String JSimBuildDir = "./jsimhome";
		
		try {
			switch (modeltype){
			
			case ModelClassifier.MML_MODEL:
					semsimmodel = new MMLreader(JSimBuildDir).readFromFile(file);
				break;
					
			case ModelClassifier.SBML_MODEL:// MML
					semsimmodel = new MMLreader(JSimBuildDir).readFromFile(file);
				break;
				
			case ModelClassifier.CELLML_MODEL:
				semsimmodel = new CellMLreader().readFromFile(file);

				break;		
			case ModelClassifier.SEMSIM_MODEL:
				semsimmodel = loadSemSimOWL(file);
				break;
				
			default:
				JOptionPane.showMessageDialog(null, "SemGen did not recognize the file type for " + file.getName(),
						"Error: Unrecognized model format", JOptionPane.ERROR_MESSAGE);
				break;
			}
		}
		catch(Exception e){e.printStackTrace();}
		
		if(semsimmodel!=null){
			if(!semsimmodel.getErrors().isEmpty()){
				String errormsg = "";
				for(String catstr : semsimmodel.getErrors())
					errormsg = errormsg + catstr + "\n";
				JOptionPane.showMessageDialog(null, errormsg, "ERROR", JOptionPane.ERROR_MESSAGE);
				return semsimmodel;
			}
			semsimmodel.setName(file.getName().substring(0, file.getName().lastIndexOf(".")));
			semsimmodel.setSourceModelType(modeltype);				
		}

		return semsimmodel;
	}
	
	public static SemSimModel loadSemSimOWL(File file) throws Exception {
		return new SemSimOWLreader().readFromFile(file);
	}
}
