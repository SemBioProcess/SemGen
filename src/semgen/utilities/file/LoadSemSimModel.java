package semgen.utilities.file;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.swing.JOptionPane;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.semanticweb.owlapi.model.OWLException;

import JSim.util.Xcept;
import semgen.SemGen;
import semgen.annotation.workbench.routines.AutoAnnotate;
import semgen.utilities.SemGenError;
import semgen.utilities.uicomponent.SemGenProgressBar;
import semsim.model.SemSimModel;
import semsim.reading.CellMLreader;
import semsim.reading.MMLParser;
import semsim.reading.MMLreader;
import semsim.reading.ModelClassifier;
import semsim.reading.ReferenceTermNamer;
import semsim.reading.SBMLAnnotator;
import semsim.reading.SemSimOWLreader;
import semsim.utilities.webservices.WebserviceTester;

public class LoadSemSimModel {
	
	public static SemSimModel loadSemSimModelFromFile(File file, boolean autoannotate) {
		SemSimModel semsimmodel = null;
		int modeltype = ModelClassifier.classify(file);
		try {
			switch (modeltype){
			
			case ModelClassifier.MML_MODEL:
				semsimmodel = createModel(file);
				if((semsimmodel==null) || semsimmodel.getErrors().isEmpty() && autoannotate)
					semsimmodel = AutoAnnotate.autoAnnotateWithOPB(semsimmodel);
				break;
					
			case ModelClassifier.SBML_MODEL:// MML
				semsimmodel = createModel(file);
				
				if((semsimmodel==null) || semsimmodel.getErrors().isEmpty() && autoannotate){
					// If it's an SBML model and we should auto-annotate
					semsimmodel = AutoAnnotate.autoAnnotateWithOPB(semsimmodel);
					SemGenProgressBar progframe = new SemGenProgressBar("Annotating with web services...",true);
					boolean online = WebserviceTester.testBioPortalWebservice("Annotation via web services failed.");
					if(!online) 
						SemGenError.showWebConnectionError("BioPortal search service");
					 Hashtable<String, String[]> cache = new  Hashtable<String, String[]>();
					cache.putAll(SemGen.termcache.getOntTermsandNamesCache());
					SBMLAnnotator.annotate(file, semsimmodel, online, cache);
					ReferenceTermNamer.getNamesForOntologyTermsInModel(semsimmodel, SemGen.termcache.getOntTermsandNamesCache(), online);
					SBMLAnnotator.setFreeTextDefinitionsForDataStructuresAndSubmodels(semsimmodel);
					progframe.dispose();
				}
				break;
				
			case ModelClassifier.CELLML_MODEL:
				semsimmodel = new CellMLreader(file).readFromFile();
				if(semsimmodel.getErrors().isEmpty()){
					if(autoannotate){
						final SemGenProgressBar progframe = new SemGenProgressBar("Annotating " + file.getName() + " with web services...",true);
						Boolean online = true;
						
							online = WebserviceTester.testBioPortalWebservice("Annotation via web services failed.");
							if(!online) SemGenError.showWebConnectionError("BioPortal search service");

						
						ReferenceTermNamer.getNamesForOntologyTermsInModel(semsimmodel,  SemGen.termcache.getOntTermsandNamesCache(), online);
						semsimmodel = AutoAnnotate.autoAnnotateWithOPB(semsimmodel);
						
						progframe.dispose();
					}
				}
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
		return new SemSimOWLreader(file).readFromFile();
	}

	
	private static SemSimModel createModel(File file) throws Xcept, IOException, InterruptedException, OWLException, JDOMException {
		Document doc = new MMLParser().readFromFile(file);
		if (SemGenError.showSemSimErrors()) return null;
		
		MMLreader xmml2 = new MMLreader(file, doc);		
		return xmml2.readFromFile();
	}
}
