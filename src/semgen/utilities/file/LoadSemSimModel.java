package semgen.utilities.file;

import java.io.IOException;

import org.jdom.Document;
import org.semanticweb.owlapi.model.OWLException;

import JSim.util.Xcept;
import semgen.SemGen;
import semgen.annotation.workbench.routines.AutoAnnotate;
import semgen.utilities.SemGenJob;
import semsim.model.collection.SemSimModel;
import semsim.reading.CellMLreader;
import semsim.reading.MMLParser;
import semsim.reading.ModelAccessor;
import semsim.reading.XMMLreader;
import semsim.reading.ModelingFileClassifier;
import semsim.reading.ReferenceTermNamer;
import semsim.reading.SBMLreader;
import semsim.reading.SemSimOWLreader;
import semsim.utilities.ErrorLog;
import semsim.utilities.SemSimUtil;
import semsim.utilities.webservices.WebserviceTester;

public class LoadSemSimModel extends SemGenJob {
	private ModelAccessor modelaccessor;
	private boolean autoannotate = false;
	private SemSimModel semsimmodel;
	
	public LoadSemSimModel(ModelAccessor modelaccessor) {
		this.modelaccessor = modelaccessor;
	}
	
	public LoadSemSimModel(ModelAccessor modelaccessor, boolean autoannotate) {
		this.modelaccessor = modelaccessor;
		this.autoannotate = autoannotate;
	}
	
	public LoadSemSimModel(ModelAccessor modelaccessor, SemGenJob sga) {
		super(sga);
		this.modelaccessor = modelaccessor;
		
	}
	
	public LoadSemSimModel(ModelAccessor modelaccessor, boolean autoannotate, SemGenJob sga) {
		super(sga);
		this.modelaccessor = modelaccessor;
		this.autoannotate = autoannotate;
	}
	
	private void loadSemSimModelFromFile() {
		
    	setStatus("Reading " + modelaccessor.toString());

		int modeltype = ModelingFileClassifier.classify(modelaccessor);
		try {
			switch (modeltype){
			
			case ModelingFileClassifier.SEMSIM_MODEL:
				if(modelaccessor.modelIsInStandAloneFile())
					semsimmodel = new SemSimOWLreader(modelaccessor.getFileThatContainsModel()).read();	
				else
					ErrorLog.addError("Cannot load a SemSim model from within an archive file.", true, false);
				break;
			
			case ModelingFileClassifier.CELLML_MODEL:
				semsimmodel = new CellMLreader(modelaccessor).read();
				nameOntologyTerms();
				if((semsimmodel!=null) && semsimmodel.getErrors().isEmpty() && autoannotate) {
					setStatus("Annotating Physical Properties");
					semsimmodel = AutoAnnotate.autoAnnotateWithOPB(semsimmodel);
				}
				break;
				
			case ModelingFileClassifier.SBML_MODEL:
				semsimmodel = new SBMLreader(modelaccessor).read();			
				nameOntologyTerms();
				break;
				
			case ModelingFileClassifier.MML_MODEL:
				semsimmodel = loadMML(modelaccessor.getModelTextAsString(), modelaccessor.getModelName());
				
				if((semsimmodel!=null) && semsimmodel.getErrors().isEmpty() && autoannotate){
					setStatus("Annotating Physical Properties");
					semsimmodel = AutoAnnotate.autoAnnotateWithOPB(semsimmodel);
				}
				break;
				
			default:
				ErrorLog.addError("SemGen did not recognize the model type for " + modelaccessor.toString(), true, false);
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
			semsimmodel.setName(modelaccessor.getModelName());
			semsimmodel.setSourceModelType(modeltype);
			SemSimUtil.regularizePhysicalProperties(semsimmodel, SemGen.semsimlib);
		}
		else {
			ErrorLog.addError(modelaccessor.getModelName() + " was an invalid model.", true, false);
		}
	}


	// Convert MML model string to SemSim model
	private SemSimModel loadMML(String srcText, String modelname) throws Xcept, IOException, InterruptedException, OWLException{
		Document doc = new MMLParser().readFromMMLstring(srcText, modelname);
		
		if (ErrorLog.hasErrors()) return null;
		
		XMMLreader xmmlreader = new XMMLreader(modelaccessor, doc, srcText);		
		return xmmlreader.readFromDocument();
	}
	
	private void nameOntologyTerms(){
		if(semsimmodel.getErrors().isEmpty() && ReferenceTermNamer.getModelComponentsWithUnnamedAnnotations(semsimmodel).size()>0){

			setStatus("Annotating with web services...");
			boolean online = WebserviceTester.testBioPortalWebservice();
			
			if(!online){
				ErrorLog.addError("Could not connect to BioPortal search service", false, false);
			}
				ReferenceTermNamer.getNamesForOntologyTermsInModel(semsimmodel, SemGen.termcache.getOntTermsandNamesCache(), online);
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
