package semsim.writing;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import semsim.SemSimLibrary;
import semsim.definitions.RDFNamespace;
import semsim.fileaccessors.JSimProjectAccessor;
import semsim.fileaccessors.ModelAccessor;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.reading.JSimProjectFileReader;
import semsim.reading.ModelClassifier.ModelType;
import semsim.reading.ModelReader;

/**
 * Class for writing SemSim models to JSim project files
 * @author mneal
 *
 */
public class JSimProjectFileWriter extends ModelWriter{

	private Document projdoc = null; 
	String modelName;
	Element semsimControlElement;
	private JSimProjectAccessor projaccessor;
	private SemSimRDFwriter rdfwriter;


	/**
	 * @param semsimmodel The {@link SemSimModel} to write out
	 * @param modelaccessor Location of the JSim project file that will be written out
	 */
	public JSimProjectFileWriter(SemSimModel semsimmodel, JSimProjectAccessor modelaccessor) {
		super(semsimmodel);

		modelName = modelaccessor.getModelName();
		projaccessor = modelaccessor;
	}
	
	
	@Override
	public String encodeModel() {

		// Create the project document.
		Element modelel = null;

		// If the file already exists...
		if(projaccessor.isLocalFile()){
			
			projdoc = projaccessor.getJDOMDocument();
		
			// If the model already exists in the project file, overwrite the model code
			// and the SemSim annotation control element. This will preserve parsets, etc. for the 
			// model in the project file.
			modelel = JSimProjectFileReader.getModelElement(projdoc, modelName);
			
			if(modelel != null) {
				Element srccodeel = JSimProjectFileReader.getModelSourceCodeElement(projdoc, modelName);
				
				srccodeel.setText(new MMLwriter(semsimmodel).encodeModel());
				semsimControlElement = JSimProjectFileReader.getSemSimControlElementForModel(projdoc, modelName);
			}
			
			// ...otherwise create a new model element.
			else{
				modelel = createNewModelElement(modelName);
				addNewSemSimControlElementToModel(modelel);
				projdoc.getRootElement().getChild("project").addContent(modelel.detach());
			}
		}
		
		//...otherwise create a new empty project file, add model element and annotations.
		else {
			boolean fromannotator = (semsimmodel.getLegacyCodeLocation() != null);
			projdoc = createEmptyProject();
									
			// If the model is to be copied from an existing file like using SaveAs in the Annotator, collect <model> element
			// from legacy code location
			if(fromannotator && semsimmodel.getSourceModelType() == ModelType.MML_MODEL_IN_PROJ){
				
				// If the model comes from a JSim project file, collect the model element
				// so we can write it to the new project file
				Document origindoc = ModelReader.getJDOMdocumentFromFile(semsimmodel.getLegacyCodeLocation().getFile());
				modelel = JSimProjectFileReader.getModelElement(origindoc, modelName);
			}
			
			// Otherwise create a new model element for the project file.
			else  modelel = createNewModelElement(modelName);
			
			// Add the model element
			projdoc.getRootElement().getChild("project").addContent(modelel.detach());
			semsimControlElement = JSimProjectFileReader.getSemSimControlElementForModel(projdoc, modelName);
		}
		
		// Create a new SemSim control element if needed
		if(semsimControlElement == null)
			addNewSemSimControlElementToModel(modelel);
		
		// If the model contains functional submodels then we need to "flatten"
		// the data structure names in the model
		if(semsimmodel.getFunctionalSubmodels().size() > 0){
			Element srccodeel = JSimProjectFileReader.getModelSourceCodeElement(projdoc, modelName);
			flattenModelForMML(srccodeel.getText());
		}
		
		// Update the name of the model to the name used on write out (needed because 
		// the RDF resources that refer to the entire model use the model *name*, not 
		// a metaid in their URI fragments).
		semsimmodel.setName(modelName);
				
		rdfwriter = new SemSimRDFwriter(projaccessor, semsimmodel);
		
		// Write out model-level annotations
		rdfwriter.setRDFforModelLevelAnnotations();
		
		// Write out annotations for data structures
		rdfwriter.setRDFforDataStructureAnnotations();
		
		// Write out annotations for submodels
		rdfwriter.setRDFforSemSimSubmodelAnnotations();
		
		// Add the RDF metadata to the appropriate element in the JSim project file
		if( ! rdfwriter.rdf.isEmpty()){
			
			String rawrdf = AbstractRDFwriter.getRDFmodelAsString(rdfwriter.rdf,"RDF/XML-ABBREV");			
			Content newrdf = ModelWriter.makeXMLContentFromString(rawrdf);
				
			// Remove old RDF if present
			semsimControlElement.removeChild("RDF", RDFNamespace.RDF.createJdomNamespace());
			
			// Write the new RDF
			if(newrdf != null) semsimControlElement.addContent(newrdf);
		}
		
		if(projdoc != null){
			XMLOutputter outputter = new XMLOutputter();
			outputter.setFormat(Format.getPrettyFormat());
			return  outputter.outputString(projdoc);
		}
		return null;
	}
	
	@Override
	public boolean writeToStream(OutputStream stream) {
		String encodedproject = encodeModel();
		if (encodedproject != null) {
			this.commitStringtoStream(stream, encodedproject);
			return true;
		}
		return false;
	}
	

	/**
	 * Add the XML element to the JSim project file that stores SemSim annotations
	 * @param modelel The model's XML element in the project file
	 */
	private void addNewSemSimControlElementToModel(Element modelel){
		semsimControlElement = new Element("control");
		semsimControlElement.setAttribute("name", SemSimLibrary.SEMSIM_IN_JSIM_CONTROL_VALUE);
		modelel.addContent(semsimControlElement);
	}
	
	
	/** @return An empty JSim project file (as a JDOM Document) */
	private Document createEmptyProject(){
		Element jsimel = new Element("JSim");
		Document doc = new Document(jsimel);
		doc.setRootElement(jsimel);
		
		jsimel.setAttribute("version", "2.17");
		
		Element projel = new Element("project");
		projel.setAttribute("name", "proj1");
		
		jsimel.addContent(projel);
		
		return doc;
	}
	
	
	/**
	 * Add a new model element to the JSim project 
	 * @param modelname Model name
	 * @return An XML element representing a new model added to the project file
	 */
	private Element createNewModelElement(String modelname){
		Element modelel = new Element("model");
		modelel.setAttribute("name", modelname);
		Element controlelsrc = new Element("control"); 
		controlelsrc.setAttribute("name", "modelSource");
		
		// Write out MML code for model. If already in MML format, don't use
		// SemSim as intermediate step
		String modelText = null;
		ModelAccessor sourceCodeLocation = semsimmodel.getLegacyCodeLocation();
		
		if(semsimmodel.getSourceModelType()==ModelType.MML_MODEL && ! sourceCodeLocation.modelIsOnline())
			try {
				modelText = sourceCodeLocation.getModelasString();
			} catch (IOException e) {
				e.printStackTrace();
			}
		else modelText = new MMLwriter(semsimmodel).encodeModel();
		
		controlelsrc.setText(modelText);
		modelel.addContent(controlelsrc);
		return modelel;
	}
	
	
	/**
	 * Used when converting CellML models into MML code. "Flattens" names of data
	 * structures by removing prefixes followed by a ".". Removes
	 * data structures not used in the MML code that is auto-generated
	 * using the JSim API.
	 * @param MMLcode The MML code generated from converting the CellML model
	 * into a JSim model.
	 */
	private void flattenModelForMML(String MMLcode){
		
		SemSimModel modelcopy = semsimmodel.clone(); // Clone first so we don't alter the original model through flattening
		Set<String> foundnamesinMML = new HashSet<String>();
		
		ArrayList<DataStructure> allds = new ArrayList<DataStructure>();
		allds.addAll(modelcopy.getAssociatedDataStructures());
		
		// For each data structure in model, see if its name is declared in the MML text
		// If not, see if its local name is declared.
		// If not, remove the data structure from the model
		// If so, change the data structure's name to the flattened name
		for(DataStructure ds : allds){
			String name = ds.getName();
			
			Pattern p1 = Pattern.compile("\\n\\s*(real |realDomain )" + name + "\\W"); // line start, some white space, real X
			Matcher m1 = p1.matcher(MMLcode);
	
			// If full name not found
			if( ! m1.find() && name.contains(".")){
				String flattenedname = name.substring(name.lastIndexOf(".") +1 );

				Pattern p2 = Pattern.compile("\\n\\s*(real |realDomain )" + flattenedname + "\\W"); // line start, some white space, real X
				Matcher m2 = p2.matcher(MMLcode);
				
				// If flattened name not found or if we've already found a
				// codeword with the flattened name, remove from semsim model
				if( ! m2.find() || foundnamesinMML.contains(flattenedname))
					modelcopy.removeDataStructure(ds);
				else{
					ds.setName(flattenedname);
					ds.setMetadataID(flattenedname);
					foundnamesinMML.add(flattenedname);
				}
			}
			else foundnamesinMML.add(name);
		}	
		
		semsimmodel = modelcopy;
	}
	
	@Override
	public AbstractRDFwriter getRDFwriter(){
		return rdfwriter;
	}

}
