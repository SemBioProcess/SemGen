package semsim.writing;

import java.io.File;
import java.net.URI;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.semanticweb.owlapi.model.OWLException;

import com.hp.hpl.jena.rdf.model.Resource;

import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.reading.JSimProjectFileReader;
import semsim.reading.ModelAccessor;
import semsim.utilities.SemSimUtil;

public class JSimProjectFileWriter extends ModelWriter{

	File projectFile;
	String modelName;
	XMLOutputter outputter;
	BiologicalRDFblock rdfblock;
	Element semsimControlElement;


	public JSimProjectFileWriter(ModelAccessor modelaccessor, SemSimModel semsimmodel) {
		super(semsimmodel);
		outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat());
		modelName = modelaccessor.getModelName();
		projectFile = modelaccessor.getFileThatContainsModel();
	}

	@Override
	public void writeToFile(File destination) throws OWLException {
		
		Document projdoc = null;
		
		if(semsimmodel.getFunctionalSubmodels().size()==0){
			
			rdfblock = new BiologicalRDFblock(semsimmodel, null, null);
			
			for(DataStructure ds : semsimmodel.getAssociatedDataStructures()){
				
				if(ds.hasPhysicalProperty()){
					Resource dsres = rdfblock.rdf.createResource("#" + ds.getName());
					rdfblock.setDataStructurePropertyAndPropertyOfAnnotations(ds, dsres);
					
					//TODO:singular annotations and free-text anns
				}
			}
		}
		
		// Otherwise there are CellML-style functional submodels present
		else{}
		
		// Add the RDF metadata to the appropriate element in the JSim project file
		if( ! rdfblock.rdf.isEmpty()){
			
			projdoc = JSimProjectFileReader.getDocument(projectFile);
			
			String rawrdf = BiologicalRDFblock.getRDFmodelAsString(rdfblock.rdf);
			System.out.println(rawrdf);
			
			Content newrdf = ModelWriter.makeXMLContentFromString(rawrdf);
			
			semsimControlElement = JSimProjectFileReader.getSemSimAnnotationControlElementForModel(projdoc, modelName);
			
			if(newrdf != null) semsimControlElement.addContent(newrdf);
		}
		
		if(projdoc != null){
			String outputstring =  outputter.outputString(projdoc);
			SemSimUtil.writeStringToFile(outputstring, destination);
		}
		else{} // Otherwise we didn't need to write anything out because there were no annotations
		
	}

	@Override
	public void writeToFile(URI uri) throws OWLException {
		writeToFile(new File(uri));
	}

}
