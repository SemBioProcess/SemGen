package semgen.utilities.file;

import java.io.IOException;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.semanticweb.owlapi.model.OWLException;

import JSim.util.Xcept;
import semgen.SemGen;
import semgen.utilities.SemGenJob;
import semsim.annotation.AutoAnnotate;
import semsim.fileaccessors.JSimProjectAccessor;
import semsim.fileaccessors.ModelAccessor;
import semsim.model.collection.SemSimModel;
import semsim.reading.OMEXmetadataReader;
import semsim.reading.CellMLreader;
import semsim.reading.JSimProjectFileReader;
import semsim.reading.MMLtoXMMLconverter;
import semsim.reading.XMMLreader;
import semsim.reading.ReferenceTermNamer;
import semsim.reading.SBMLreader;
import semsim.reading.SemSimOWLreader;
import semsim.reading.ModelClassifier.ModelType;
import semsim.utilities.ErrorLog;
import semsim.utilities.SemSimUtil;
import semsim.utilities.webservices.BioPortalSearcher;

public class LoadModelJob extends SemGenJob {
	private ModelAccessor modelaccessor;
	private boolean autoannotate = false;
	public boolean onStage;
	private SemSimModel semsimmodel;
	
	public LoadModelJob(ModelAccessor modelaccessor) {
		this.modelaccessor = modelaccessor;
	}
	
	public LoadModelJob(ModelAccessor modelaccessor, boolean autoannotate) {
		this.modelaccessor = modelaccessor;
		this.autoannotate = autoannotate;
	}

	public LoadModelJob(ModelAccessor modelaccessor, boolean autoannotate, boolean loadOnStage) {
		this.modelaccessor = modelaccessor;
		this.autoannotate = autoannotate;
		this.onStage = loadOnStage;
	}
	
	public LoadModelJob(ModelAccessor modelaccessor, SemGenJob sga) {
		super(sga);
		this.modelaccessor = modelaccessor;
	}
	
	public LoadModelJob(ModelAccessor modelaccessor, boolean autoannotate, SemGenJob sga) {
		super(sga);
		this.modelaccessor = modelaccessor;
		this.autoannotate = autoannotate;
	}
	
	private void loadSemSimModelFromFile() throws JDOMException, IOException {
    	System.out.println("Reading " + modelaccessor.getShortLocation());

    	setStatus("Reading " + modelaccessor.getShortLocation());

    	ModelType modeltype = modelaccessor.getModelType();
		try {
			switch (modeltype){
			
			case SEMSIM_MODEL:
					semsimmodel = new SemSimOWLreader(modelaccessor).read();	
				break;
			
			case CELLML_MODEL:
				CellMLreader cellmlreader = new CellMLreader(modelaccessor);
				semsimmodel = cellmlreader.read();

				// If the semsim namespace is prefixed in the RDF, then we assume it was previously annotated
				// and we don't perform automatic OPB annotation based on units
				boolean previouslyannotated = (cellmlreader.rdfreader.rdf.getNsPrefixURI("semsim") != null
						|| cellmlreader.rdfreader instanceof OMEXmetadataReader);
				
				// If the model wasn't previously annotated in SemGen and autoannotate is turned on,
				// perform auto-annotation
				if((semsimmodel!=null) && semsimmodel.getErrors().isEmpty() && autoannotate && ! previouslyannotated) {
					setStatus("Annotating Physical Properties");
					AutoAnnotate.autoAnnotateWithOPB(semsimmodel, SemGen.semsimlib, SemGen.cfgreadpath);
				}
				// Otherwise collect any needed reference term names from BioPortal
				else{
					setStatus("Collecting annotations");
					nameOntologyTerms();
				}

				break;
				
			case SBML_MODEL:
				semsimmodel = new SBMLreader(modelaccessor).read();			
				nameOntologyTerms();
				break;
				
			case MML_MODEL:
				loadMML(modelaccessor);
				break;
				
			case MML_MODEL_IN_PROJ:
				loadMML(modelaccessor);
				break;
				
			default:
				ErrorLog.addError("SemGen did not recognize the model type for " + modelaccessor.getShortLocation(), true, false);
				return;
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		if(semsimmodel != null){
			
			if( ! semsimmodel.getErrors().isEmpty()){
				for (String e : semsimmodel.getErrors()) {
					ErrorLog.addError(e,true, true);
				}
				return;
			}

			semsimmodel.setName(modelaccessor.getModelName());
			semsimmodel.setSourceModelType(modeltype);
			SemSimUtil.regularizePhysicalProperties(semsimmodel, SemGen.semsimlib);
		}
		else
			ErrorLog.addError("Could not load " + modelaccessor.getFileName() 
			+ "\nCheck that model encoding is valid.", true, false);
		
	}


	private SemSimModel loadMML(ModelAccessor ma) throws Xcept, IOException, InterruptedException, OWLException{
		
		String srcText = ma.getModelAsString();
		Document xmmldoc = MMLtoXMMLconverter.convert(srcText, ma.getFileName());
		
		if (ErrorLog.hasErrors()) return null;
		
		XMMLreader xmmlreader = new XMMLreader(modelaccessor, xmmldoc, srcText.toString());		
		semsimmodel = xmmlreader.readFromDocument();
		
		if((semsimmodel == null) || (! semsimmodel.getErrors().isEmpty()))
			return semsimmodel;
		
		else{
			
			if (ma.getModelType().equals(ModelType.MML_MODEL_IN_PROJ)) {
				if(JSimProjectFileReader.getModelPreviouslyAnnotated(semsimmodel, (JSimProjectAccessor)ma, SemGen.semsimlib)){
					//If annotations present, collect names of reference terms
					setStatus("Collecting annotations");
					nameOntologyTerms();
				}
			}
			
			else if(autoannotate){
				// Otherwise auto-annotate, if turned on, 
				annotateModel();
			}
		}
		
		return semsimmodel;
	}
	
	private void annotateModel() {
		setStatus("Annotating physical properties");
		AutoAnnotate.autoAnnotateWithOPB(semsimmodel, SemGen.semsimlib, SemGen.cfgreadpath);
	}
	
	private void nameOntologyTerms(){
		if(semsimmodel.getErrors().isEmpty() && ReferenceTermNamer.getModelComponentsWithUnnamedAnnotations(semsimmodel, SemGen.semsimlib).size()>0){

			setStatus("Retrieving names for reference terms");
			boolean online = BioPortalSearcher.testBioPortalWebservice();
			
			if( ! online){
				ErrorLog.addError("Could not connect to BioPortal search service", false, false);
			}
				ReferenceTermNamer.getNamesForReferenceTermsInModel(semsimmodel, SemGen.termcache.getOntTermsandNamesCache(), SemGen.semsimlib);
//					SBMLAnnotator.setFreeTextDefinitionsForDataStructuresAndSubmodels(semsimmodel);
		}
	}
	


	@Override
	public void run() {
		try {
			loadSemSimModelFromFile();
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
				
		if (semsimmodel == null || ErrorLog.errorsAreFatal()) {
			abort();
			return;
		}
	}
	
	public SemSimModel getLoadedModel() {
		return semsimmodel;
	}
}
