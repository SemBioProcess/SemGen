package semsim.writing;

import java.io.File;
import java.net.URI;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.semanticweb.owlapi.model.OWLException;

import semsim.definitions.RDFNamespace;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
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
	public void writeToFile(File destination) throws OWLException {
		
		Document projdoc = null; 
		
		if(semsimmodel.getFunctionalSubmodels().size()==0){
			
			rdfblock = new SemSimRDFwriter(semsimmodel, null, null);
			
			// Write out model-level annotations
			rdfblock.setRDFforModelLevelAnnotations();
			
			// Write out annotations for data structures
			rdfblock.setRDFforDataStructureAnnotations();
			
			// Write out annotations for submodels
			rdfblock.setRDFforSubmodelAnnotations();
			
		}
		
		// Otherwise there are CellML-style functional submodels present
		else{}
		
		// Create the project document.
		// If the file already exists...
		if(destination.exists()){
			
			// If the file that we're writing to exists, and is different than the source location,
			// then we need to read in the target file as a document
			if(semsimmodel.getLegacyCodeLocation().equals(outputModelAccessor))
				projdoc = JSimProjectFileReader.getDocument(outputProjectFile);
			
			else
				projdoc = JSimProjectFileReader.getDocument(semsimmodel.getLegacyCodeLocation().getFileThatContainsModel());
		}
		
		//...otherwise create a new empty project file, add model element and annotations.
		else {
			projdoc = createEmptyProject();
			
			// TODO: what if trying to save a CellML model to project file?
			Document origindoc = JSimProjectFileReader.getDocument(semsimmodel.getLegacyCodeLocation().getFileThatContainsModel());
			Element modelel = JSimProjectFileReader.getModelElement(origindoc, semsimmodel.getName());
			projdoc.getRootElement().getChild("project").addContent(modelel.detach());
		}
		
		// Add the RDF metadata to the appropriate element in the JSim project file
		// TODO: check this if block
		if(rdfblock.rdf.listStatements().hasNext()){
			
			String rawrdf = SemSimRDFreader.getRDFmodelAsString(rdfblock.rdf);			
			Content newrdf = ModelWriter.makeXMLContentFromString(rawrdf);
			semsimControlElement = JSimProjectFileReader.getSemSimAnnotationControlElementForModel(projdoc, modelName);
			
			// Remove old RDF
			semsimControlElement.removeChild("RDF", RDFNamespace.RDF.createJdomNamespace());
			
			// Add the new RDF
			if(newrdf != null) semsimControlElement.addContent(newrdf);
		}
		
		if(projdoc != null){
			String outputstring =  outputter.outputString(projdoc);
			SemSimUtil.writeStringToFile(outputstring, destination);
		}
		else{} // Otherwise there were no annotations to write
		
	}

	@Override
	public void writeToFile(URI uri) throws OWLException {
		writeToFile(new File(uri));
	}

	
	private Document createEmptyProject(){
		Element jsimel = new Element("JSim");
		Document doc = new Document(jsimel);
		doc.setRootElement(jsimel);
		
		jsimel.setAttribute("version", "2.09"); // Might need to change version #
		
		Element projel = new Element("project");
		projel.setAttribute("name", "proj1");
		
		jsimel.addContent(projel);
		
		return doc;
	}
}
