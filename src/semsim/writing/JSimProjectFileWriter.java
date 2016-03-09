package semsim.writing;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.semanticweb.owlapi.model.OWLException;

import semsim.SemSimLibrary;
import semsim.definitions.RDFNamespace;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.reading.JSimProjectFileReader;
import semsim.reading.ModelAccessor;
import semsim.reading.SemSimRDFreader;
import semsim.utilities.SemSimUtil;

public class JSimProjectFileWriter extends ModelWriter{

	File outputProjectFile;
	ModelAccessor outputModelAccessor;
	String modelName;
	XMLOutputter outputter;
	SemSimRDFwriter rdfblock;
	Element semsimControlElement;
	String modelNamespace;


	public JSimProjectFileWriter(ModelAccessor modelaccessor, SemSimModel semsimmodel) {
		super(semsimmodel);
		outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat());
		modelName = modelaccessor.getModelName();
		outputProjectFile = modelaccessor.getFileThatContainsModel();
		outputModelAccessor = modelaccessor;
		modelNamespace = semsimmodel.getNamespace();
	}

	@Override
	public void writeToFile(File destination) {
		
		Document projdoc = null; 
		boolean fromannotator = (semsimmodel.getLegacyCodeLocation() != null);
		
		// Create the project document.
			
		// If the file already exists...
		if(destination.exists()){
			
			projdoc = JSimProjectFileReader.getDocument(outputProjectFile);
		
			// If the model already exists in the project file, overwrite the model code
			// and the SemSimAnnotation control element. This will preserve parsets, etc. for the 
			// model in the project file.
			if(JSimProjectFileReader.getModelElement(projdoc, modelName) != null) {
				Element srccodeel = JSimProjectFileReader.getModelSourceCodeElement(projdoc, modelName);
				srccodeel.setText(new MMLwriter(semsimmodel).writeToString());
				semsimControlElement = JSimProjectFileReader.getSemSimControlElementForModel(projdoc, modelName);
			}
			
			// ...otherwise create a new model element.
			else{
				Element modelel = createNewModelElement(modelName);
				semsimControlElement = new Element("control");
				semsimControlElement.setAttribute("name", SemSimLibrary.SemSimInJSimControlValue);
				modelel.addContent(semsimControlElement);
				projdoc.getRootElement().getChild("project").addContent(modelel.detach());
			}
		}
		
		//...otherwise create a new empty project file, add model element and annotations.
		else {
			projdoc = createEmptyProject();
						
			Element modelel = null;
			
			// If the model is to be copied from an existing file like using SaveAs in the Annotator, collect <model> element
			// from legacy code location
			if(fromannotator && semsimmodel.getLegacyCodeLocation().modelIsPartOfJSimProjectFile()){
				
				// If the model comes from a JSim project file, collect the model element
				// so we can write it to the new project file
				Document origindoc = JSimProjectFileReader.getDocument(semsimmodel.getLegacyCodeLocation().getFileThatContainsModel());
				modelel = JSimProjectFileReader.getModelElement(origindoc, modelName);
			}
			
			// Otherwise we're using SaveAs in the Annotator for a CellML, SBML or MML file, or the model was created
			// via an extraction or merging process. Create a new model element for the project file.
			else  modelel = createNewModelElement(modelName);
			
			// Add the model element
			projdoc.getRootElement().getChild("project").addContent(modelel.detach());
			semsimControlElement = JSimProjectFileReader.getSemSimControlElementForModel(projdoc, modelName);
			
			// Create a new SemSim control element if needed
			if(semsimControlElement == null){
				semsimControlElement = new Element("control");
				semsimControlElement.setAttribute("name", SemSimLibrary.SemSimInJSimControlValue);
				modelel.addContent(semsimControlElement);
			}
		}
		
		
		// If the model contains functional submodels then we need to "flatten"
		// the data structure names in the model
		if(semsimmodel.getFunctionalSubmodels().size() > 0){
			Element srccodeel = JSimProjectFileReader.getModelSourceCodeElement(projdoc, modelName);
			flattenModelForMML(srccodeel.getText());
		}
		
		rdfblock = new SemSimRDFwriter(semsimmodel, null, null);
		
		// Write out model-level annotations
		rdfblock.setRDFforModelLevelAnnotations();
		
		// Write out annotations for data structures
		rdfblock.setRDFforDataStructureAnnotations();
		
		// Write out annotations for submodels
		rdfblock.setRDFforSubmodelAnnotations();
		
		// Add the RDF metadata to the appropriate element in the JSim project file
		if(rdfblock.rdf.listStatements().hasNext()){
			
			String rawrdf = SemSimRDFreader.getRDFmodelAsString(rdfblock.rdf);			
			Content newrdf = ModelWriter.makeXMLContentFromString(rawrdf);
				
			// Remove old RDF if present
			semsimControlElement.removeChild("RDF", RDFNamespace.RDF.createJdomNamespace());
			
			// Write the new RDF
			if(newrdf != null) semsimControlElement.addContent(newrdf);
		}
		
		if(projdoc != null){
			String outputstring =  outputter.outputString(projdoc);
			SemSimUtil.writeStringToFile(outputstring, destination);
		}
	}

	@Override
	public void writeToFile(URI uri) throws OWLException {
		writeToFile(new File(uri));
	}

	
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
	
	private Element createNewModelElement(String modelname){
		Element modelel = new Element("model");
		modelel.setAttribute("name", modelname);
		Element controlelsrc = new Element("control"); 
		controlelsrc.setAttribute("name", "modelSource");
		controlelsrc.setText(new MMLwriter(semsimmodel).writeToString());
		modelel.addContent(controlelsrc);
		return modelel;
	}
	
	private void flattenModelForMML(String MMLcode){
		
		ArrayList<DataStructure> allds = new ArrayList<DataStructure>();
		allds.addAll(semsimmodel.getAssociatedDataStructures());
		
		// For each data structure in model, see if its name is declared in the MML text
		// If not, see if its local name is declared.
		// If not, remove the data structure from the model
		// If so, change the data structure's name to the flattened name
		for(DataStructure ds : allds){
			String name = ds.getName();
			
			Pattern p1 = Pattern.compile("\\n\\s*real " + name + "\\W"); // line start, some white space, real X
			Matcher m1 = p1.matcher(MMLcode);
	
			// If full name not found
			if( ! m1.find() && name.contains(".")){
				String flattenedname = name.substring(name.lastIndexOf(".") +1 );

				Pattern p2 = Pattern.compile("\\n\\s*real " + flattenedname + "\\W"); // line start, some white space, real X
				Matcher m2 = p2.matcher(MMLcode);
				
				// If flattened name not found
				if( ! m2.find()) semsimmodel.removeDataStructure(name);
				else semsimmodel.getAssociatedDataStructure(name).setName(flattenedname);
			}
		}		
	}
}
