package semgen.utilities.file;

import java.io.File;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.semanticweb.owlapi.model.OWLException;

import JSim.util.Xcept;
import semgen.SemGen;
import semgen.annotation.workbench.routines.AutoAnnotate;
import semgen.utilities.SemGenJob;
import semsim.model.collection.SemSimModel;
import semsim.reading.CellMLreader;
import semsim.reading.MMLParser;
import semsim.reading.MMLreader;
import semsim.reading.ModelClassifier;
import semsim.reading.ReferenceTermNamer;
import semsim.reading.SBMLreader;
import semsim.reading.SemSimOWLreader;
import semsim.utilities.ErrorLog;
import semsim.utilities.SemSimUtil;
import semsim.utilities.webservices.BioPortalSearcher;

public class LoadSemSimModel extends SemGenJob {
	private File file;
	private boolean autoannotate = false;
	private SemSimModel semsimmodel;
	
	public LoadSemSimModel(File file) {
		this.file = file;
	}
	
	public LoadSemSimModel(File file, boolean autoannotate) {
		this.file = file;
		this.autoannotate = autoannotate;
	}
	
	public LoadSemSimModel(File file, SemGenJob sga) {
		super(sga);
		this.file = file;
		
	}
	
	public LoadSemSimModel(File file, boolean autoannotate, SemGenJob sga) {
		super(sga);
		this.file = file;
		this.autoannotate = autoannotate;
	}
	
	private void loadSemSimModelFromFile() {
		int modeltype = ModelClassifier.classify(file);
		try {
			switch (modeltype){
			
			case ModelClassifier.MML_MODEL:
				semsimmodel = createModel(file);
				if((semsimmodel!=null) && semsimmodel.getErrors().isEmpty() && autoannotate){
					setStatus("Annotating Physical Properties");
					semsimmodel = AutoAnnotate.autoAnnotateWithOPB(semsimmodel);
				}
				break;
					
			case ModelClassifier.SBML_MODEL:
				semsimmodel = new SBMLreader(file).readFromFile();			
				nameOntologyTerms();
				break;
				
			case ModelClassifier.CELLML_MODEL:
				semsimmodel = new CellMLreader(file).readFromFile();
				nameOntologyTerms();
				if((semsimmodel!=null) && semsimmodel.getErrors().isEmpty() && autoannotate) {
					setStatus("Annotating Physical Properties");
					semsimmodel = AutoAnnotate.autoAnnotateWithOPB(semsimmodel);
				}
				break;
				
			case ModelClassifier.SEMSIM_MODEL:
				semsimmodel = loadSemSimOWL(file);	
				break;
				
			default:
				ErrorLog.addError("SemGen did not recognize the file type for " + file.getName(), true, false);
				return;
			}
			}
		catch(Exception e){
			e.printStackTrace();
		}
		
		if(semsimmodel!=null){
			if(!semsimmodel.getErrors().isEmpty()){
				for (String e : semsimmodel.getErrors()) {
					ErrorLog.addError(e,true, true);
				}
				return;
			}
			semsimmodel.setName(file.getName().substring(0, file.getName().lastIndexOf(".")));
			semsimmodel.setSourceModelType(modeltype);
			SemSimUtil.regularizePhysicalProperties(semsimmodel, SemGen.semsimlib);
		}
		else {
			ErrorLog.addError(file.getName() + " was an invalid model.", true, false);
		}
	}
	
	public static SemSimModel loadSemSimOWL(File file) throws Exception {
		return new SemSimOWLreader(file).readFromFile();
	}

	private SemSimModel createModel(File file) throws Xcept, IOException, InterruptedException, OWLException, JDOMException {
		Document doc = new MMLParser(SemGen.cfgreadpath + "jsimhome").readFromFile(file);
		if (ErrorLog.hasErrors()) return null;
		
		MMLreader xmml2 = new MMLreader(file, doc);		
		return xmml2.readFromFile();
	}
	
	private void nameOntologyTerms(){
		if(semsimmodel.getErrors().isEmpty() && ReferenceTermNamer.getModelComponentsWithUnnamedAnnotations(semsimmodel, SemGen.semsimlib).size()>0){

			setStatus("Annotating with web services...");
			boolean online = BioPortalSearcher.testBioPortalWebservice();
			
			if(!online){
				ErrorLog.addError("Could not connect to BioPortal search service", false, false);
			}
				ReferenceTermNamer.getNamesForOntologyTermsInModel(semsimmodel, SemGen.termcache.getOntTermsandNamesCache(), SemGen.semsimlib);
//					SBMLAnnotator.setFreeTextDefinitionsForDataStructuresAndSubmodels(semsimmodel);
			}
		}

	@Override
	public void run() {
		loadSemSimModelFromFile();
		if (semsimmodel == null || ErrorLog.errorsAreFatal()) {
			abort();
			return;
		}
		
	}
	
	public SemSimModel getLoadedModel() {
		return semsimmodel;
	}
}
