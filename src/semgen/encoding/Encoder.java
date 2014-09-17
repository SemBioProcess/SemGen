package semgen.encoding;

import java.io.File;

import javax.swing.JOptionPane;

import semgen.resource.SemGenTask;
import semgen.resource.file.LoadSemSimModel;
import semgen.resource.file.SemGenOpenFileChooser;
import semgen.resource.file.SemGenSaveFileChooser;
import semgen.resource.uicomponents.ProgressBar;
import semsim.SemSimUtil;
import semsim.model.SemSimModel;
import semsim.writing.CellMLwriter;
import semsim.writing.MMLwriter;
import semsim.writing.Writer;

public class Encoder {
	File outputfile = null;
	
	public Encoder() {
		startEncoding(null, null);
	}
	
	public Encoder(File afile, String filenamesuggestion) {
		startEncoding(afile, filenamesuggestion);
	}
	
	public Encoder(SemSimModel model, String filenamesuggestion) {
		startEncoding(model, filenamesuggestion);
	}
	
	// Automatically apply OPB annotations to the physical properties associated
	// with the model's data structures					
	public void startEncoding(Object inputfileormodel, String filenamesuggestion){
		Object[] optionsarray = new Object[] {"CellML", "MML (JSim)"};
		
		Object selection = JOptionPane.showInputDialog(null, "Select output format", "SemGen coder", JOptionPane.PLAIN_MESSAGE, null, optionsarray, "CellML");
		if(filenamesuggestion.contains(".")) {
			filenamesuggestion = filenamesuggestion.substring(0, filenamesuggestion.lastIndexOf("."));
		}
		
		Writer outwriter = null;
		SemGenSaveFileChooser fc = new SemGenSaveFileChooser("Choose Destination");
		if(selection == optionsarray[0]){
			fc.addFilters(new String[]{"cellml"});
			outputfile = fc.SaveAsAction();
			outwriter = new CellMLwriter();
		}
		
		if(selection == optionsarray[1]){
			fc.addFilters(new String[]{"mml"});
			outputfile = fc.SaveAsAction();
			outwriter = new MMLwriter();
		}
		if(outputfile!=null){
			CoderTask task;
			if(inputfileormodel == null){
				task = new CoderTask(outwriter);
			}
			else {
				task = new CoderTask((SemSimModel)inputfileormodel, outwriter);
			}
			task.execute();
		}
	}
		
		public class CoderTask extends SemGenTask {
			public File inputfile;
			public Writer writer;
			public SemSimModel model;
	        
			public CoderTask(Writer writer){
				SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select SemSim model to encode");
				inputfile = sgc.getSelectedFile();
	        	this.writer = writer;
	        }
			public CoderTask(SemSimModel model, Writer writer){
				this.model = model;
	        	this.writer = writer;
			}
	        @Override
	        public Void doInBackground() {
	        	progframe = new ProgressBar("Encoding...", true);
	    		if(model == null){
	        		model = LoadSemSimModel.loadSemSimModelFromFile(inputfile);
	    			if(!model.getErrors().isEmpty()){
	    				JOptionPane.showMessageDialog(null, "Selected model had errors:", "Could not encode model", JOptionPane.ERROR_MESSAGE);
	    				return null;
	    			}
	    		}
				CoderAction(model, writer);
	            return null;
	        }
	        
	    	public void CoderAction(SemSimModel model, Writer writer){
	    		String content = writer.writeToString(model);
	    		if(content!=null)
	    			SemSimUtil.writeStringToFile(content, outputfile);
	    		else
	    			JOptionPane.showMessageDialog(null, "Sorry. There was a problem encoding " + model.getName() + 
	    					"\nThe JSim API threw an exception.",  
	    					"Error", JOptionPane.ERROR_MESSAGE);
	    	}
	    }
}
