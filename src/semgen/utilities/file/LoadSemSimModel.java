package semgen.utilities.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.semanticweb.owlapi.model.OWLException;

import JSim.util.Xcept;
import semgen.SemGen;
import semgen.annotation.workbench.routines.AutoAnnotate;
import semgen.utilities.SemGenJob;
import semsim.model.collection.SemSimModel;
import semsim.reading.CASAreader;
import semsim.reading.CellMLreader;
import semsim.reading.JSimProjectFileReader;
import semsim.reading.MMLtoXMMLconverter;
import semsim.reading.ModelAccessor;
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

    	ModelType modeltype = modelaccessor.getFileType();
		try {
			switch (modeltype){
			
			case SEMSIM_MODEL:
					semsimmodel = new SemSimOWLreader(modelaccessor).read();	
				break;
			
			case CELLML_MODEL:
				CellMLreader reader = new CellMLreader(modelaccessor);
				semsimmodel = reader.read();
				
				// If the semsim namespace is prefixed in the RDF, then we assume it was previously annotated
				// and we don't perform automatic OPB annotation based on units
				boolean previouslyannotated = (reader.rdfblock.rdf.getNsPrefixURI("semsim") != null
						|| reader.rdfblock instanceof CASAreader);
				
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
			semsimmodel.setName(modelaccessor.getFileName());
			semsimmodel.setSourceModelType(modeltype);
			SemSimUtil.regularizePhysicalProperties(semsimmodel, SemGen.semsimlib);
		}
		else
			ErrorLog.addError(modelaccessor.getFileName() + " was an invalid model.", true, false);
		
	}


	private SemSimModel loadMML(ModelAccessor ma) throws Xcept, IOException, InterruptedException, OWLException{
		
		InputStream stream = ma.modelInStream();
		String srcText = IOUtils.toString(stream, StandardCharsets.UTF_8.name());
		Document xmmldoc = MMLtoXMMLconverter.convert(srcText, ma.getFileName());
		
		if (ErrorLog.hasErrors()) return null;
		
		XMMLreader xmmlreader = new XMMLreader(modelaccessor, xmmldoc, srcText.toString());		
		semsimmodel = xmmlreader.readFromDocument();
		
		if((semsimmodel == null) || (! semsimmodel.getErrors().isEmpty()))
			return semsimmodel;
		
		else{
			
			if(JSimProjectFileReader.getModelPreviouslyAnnotated(semsimmodel, ma, SemGen.semsimlib)){
				//If annotations present, collect names of reference terms
				setStatus("Collecting annotations");
				nameOntologyTerms();
			}
			else if(autoannotate){
				// Otherwise auto-annotate, if turned on, 
				setStatus("Annotating physical properties");
				AutoAnnotate.autoAnnotateWithOPB(semsimmodel);
			}
		}
		
		return semsimmodel;
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
