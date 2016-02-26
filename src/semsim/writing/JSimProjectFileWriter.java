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

	File projectFile;
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
		projectFile = modelaccessor.getFileThatContainsModel();
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
			for(DataStructure ds : semsimmodel.getAssociatedDataStructures())
				rdfblock.setRDFforDataStructureAnnotations(ds);
			
			// Write out annotations for submodels
			for(Submodel sub : semsimmodel.getSubmodels()) 
				rdfblock.setRDFforSubmodelAnnotations(sub);
			
		}
		
		// Otherwise there are CellML-style functional submodels present
		else{}
		
		// Add the RDF metadata to the appropriate element in the JSim project file
		if( ! rdfblock.rdf.isEmpty()){
			
			projdoc = JSimProjectFileReader.getDocument(projectFile);
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

}
