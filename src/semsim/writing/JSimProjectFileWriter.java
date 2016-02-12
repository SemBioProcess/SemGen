package semsim.writing;

import java.io.File;
import java.net.URI;

import org.jdom.Content;
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

	JSimProjectFileReader projreader;
	String modelName;
	XMLOutputter outputter;
	CellMLbioRDFblock rdfblock;
	Element modelElement;
	Element semsimControlElement;


	public JSimProjectFileWriter(ModelAccessor modelaccessor, SemSimModel semsimmodel) {
		super(semsimmodel);
		projreader = new JSimProjectFileReader(modelaccessor.getFileThatContainsModel());
		outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat());
		modelName = modelaccessor.getModelName();
		modelElement = projreader.getModelElement(modelName);
	}

	@Override
	public void writeToFile(File destination) throws OWLException {
		
		if(semsimmodel.getFunctionalSubmodels().size()==0){
			
			rdfblock = new CellMLbioRDFblock(semsimmodel, null, null);
			
			for(DataStructure ds : semsimmodel.getAssociatedDataStructures()){
				
				if(ds.hasPhysicalProperty()){
					Resource dsres = rdfblock.rdf.createResource("#" + ds.getName());

					rdfblock.addPropertyAndPropertyOfAnnotationsToDataStructure(ds, dsres);
					
					//TODO:singular annotations and free-text anns
				}
			}
		}
		
		// Otherwise there are CellML-style functional submodels present
		else{
			
		}
		
		// Add the RDF metadata to the appropriate element in the JSim project file
		if( ! rdfblock.rdf.isEmpty()){
			String rawrdf = CellMLbioRDFblock.getRDFAsString(rdfblock.rdf);
			Content newrdf = CellMLwriter.makeXMLContentFromString(rawrdf);
			
			semsimControlElement = projreader.getSemSimAnnotationsControlElementForModel(modelName);
			
			if(newrdf!=null) semsimControlElement.addContent(newrdf);
		}
		
		String outputstring =  outputter.outputString(projreader.getDocument());
		SemSimUtil.writeStringToFile(outputstring, destination);
		
	}

	@Override
	public void writeToFile(URI uri) throws OWLException {
		writeToFile(new File(uri));
	}

}
