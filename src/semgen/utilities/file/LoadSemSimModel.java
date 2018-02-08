package semgen.utilities.file;

import java.io.IOException;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.semanticweb.owlapi.model.OWLException;

import JSim.util.Xcept;
import semgen.SemGen;
import semgen.annotation.workbench.routines.AutoAnnotate;
import semgen.utilities.SemGenJob;
import semsim.fileaccessors.JSIMProjectAccessor;
import semsim.fileaccessors.ModelAccessor;
import semsim.model.collection.SemSimModel;
import semsim.reading.CASAreader;
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
	
	private void loadSemSimModelFromFile() throws JDOMException, IOException {
		
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
						|| cellmlreader.rdfreader instanceof CASAreader);
				
				// If the model wasn't previously annotated in SemGen and autoannotate is turned on,
				// perform auto-annotation
				if((semsimmodel!=null) && semsimmodel.getErrors().isEmpty() && autoannotate && ! previouslyannotated) {
					setStatus("Annotating Physical Properties");
					AutoAnnotate.autoAnnotateWithOPB(semsimmodel);
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
			ErrorLog.addError(modelaccessor.getFileName() + " was an invalid model.", true, false);
		
	}


	private SemSimModel loadMML(ModelAccessor ma) throws Xcept, IOException, InterruptedException, OWLException{
		
		String srcText = ma.getModelasString();
		Document xmmldoc = MMLtoXMMLconverter.convert(srcText, ma.getFileName());
		
		if (ErrorLog.hasErrors()) return null;
		
		XMMLreader xmmlreader = new XMMLreader(modelaccessor, xmmldoc, srcText.toString());		
		semsimmodel = xmmlreader.readFromDocument();
		
		if((semsimmodel == null) || (! semsimmodel.getErrors().isEmpty()))
			return semsimmodel;
		
		else{
			
			if (ma.getModelType().equals(ModelType.MML_MODEL_IN_PROJ)) {
				if(JSimProjectFileReader.getModelPreviouslyAnnotated(semsimmodel, (JSIMProjectAccessor)ma, SemGen.semsimlib)){
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
		AutoAnnotate.autoAnnotateWithOPB(semsimmodel);
	}
	
	private void nameOntologyTerms(){
		if(semsimmodel.getErrors().isEmpty() && ReferenceTermNamer.getModelComponentsWithUnnamedAnnotations(semsimmodel, SemGen.semsimlib).size()>0){

			setStatus("Annotating with web services");
			boolean online = BioPortalSearcher.testBioPortalWebservice();
			
			if( ! online){
				ErrorLog.addError("Could not connect to BioPortal search service", false, false);
			}
				ReferenceTermNamer.getNamesForOntologyTermsInModel(semsimmodel, SemGen.termcache.getOntTermsandNamesCache(), SemGen.semsimlib);
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
