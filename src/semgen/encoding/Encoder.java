package semgen.encoding;

import java.io.File;

import javax.swing.JOptionPane;

import org.semanticweb.owlapi.model.OWLException;

import semgen.utilities.SemGenError;
import semgen.utilities.SemGenTask;
import semgen.utilities.file.LoadSemSimModel;
import semgen.utilities.file.SemGenOpenFileChooser;
import semgen.utilities.file.SemGenSaveFileChooser;
import semgen.utilities.uicomponent.SemGenProgressBar;
import semsim.model.SemSimModel;
import semsim.writing.BioModelWriter;
import semsim.writing.CellMLwriter;
import semsim.writing.MMLwriter;

public class Encoder {	
	public Encoder() {
		SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select SemSim model to encode", 
				new String[] {"owl"});
		File inputfile = sgc.getSelectedFile();
		if (inputfile != null) {
			String filenamesuggestion = inputfile.getName();
			if(filenamesuggestion.contains(".")) {
				filenamesuggestion = filenamesuggestion.substring(0, filenamesuggestion.lastIndexOf("."));
			}
			startEncoding(inputfile, filenamesuggestion);
		}
	}
	
	public Encoder(File afile, String filenamesuggestion) {
		startEncoding(afile, filenamesuggestion);
	}
	
	public Encoder(SemSimModel model, String filenamesuggestion) {
		startEncoding(model, filenamesuggestion);
	}
	
	public void startEncoding(File afile, String filenamesuggestion){
		SemSimModel model = LoadSemSimModel.loadSemSimModelFromFile(afile, false);
		if(!model.getErrors().isEmpty()){
			SemGenError.showError("Selected model had errors:", "Could not encode model");
			return;
		}
		startEncoding(model, filenamesuggestion);
	}
	
	// Automatically apply OPB annotations to the physical properties associated
	// with the model's data structures					
	public void startEncoding(SemSimModel model, String filenamesuggestion){
		Object[] optionsarray = new Object[] {"CellML", "MML (JSim)"};
		
		Object selection = JOptionPane.showInputDialog(null, "Select output format", "SemGen coder", JOptionPane.PLAIN_MESSAGE, null, optionsarray, "CellML");
		
		BioModelWriter outwriter = null;
		SemGenSaveFileChooser fc = new SemGenSaveFileChooser("Choose Destination");
		if(selection == optionsarray[0]){
			fc.addFilters(new String[]{"cellml"});
			outwriter = new CellMLwriter(model);
		}
		
		if(selection == optionsarray[1]){
			fc.addFilters(new String[]{"mml"});
			outwriter = new MMLwriter(model);
		}
		File outputfile = fc.SaveAsAction();
		if (outputfile != null) {
			CoderTask task = new CoderTask(outwriter, outputfile);
		
			task.execute();
			SemGenError.showSemSimErrors();
		}
	}
	
	public class CoderTask extends SemGenTask {
			private BioModelWriter writer;
			private File output;
	        
			public CoderTask(BioModelWriter writer, File dest){
	        	this.writer = writer;
	        	output = dest;
			}
	        @Override
	        public Void doInBackground() {
	        	progframe = new SemGenProgressBar("Encoding...", true);
	        	while (!isCancelled()) {
	        		try {
						writer.writeToFile(output);
					} catch (OWLException e) {
						e.printStackTrace();
					}	
	        		break;
	        	}
	            return null;
	        }
	}
}
