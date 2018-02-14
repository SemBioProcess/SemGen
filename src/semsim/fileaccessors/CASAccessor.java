package semsim.fileaccessors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.jdom.JDOMException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Resource;

import semsim.SemSimLibrary;
import semsim.model.collection.SemSimModel;
import semsim.reading.AbstractRDFreader;
import semsim.reading.CASAreader;
import semsim.reading.ModelClassifier.ModelType;
import semsim.writing.AbstractRDFwriter;

public class CASAccessor extends ModelAccessor {

	protected CASAccessor(File standAloneFile) {
		super(standAloneFile, ModelType.CASA_FILE);
		
	}
	
	protected CASAccessor(CASAccessor accessor) {
		super(accessor);
	}

	public CASAreader getAssociatedCASAFile(SemSimModel semsimmodel, SemSimLibrary sslib) throws ZipException, IOException, JDOMException {
		//Get the CASA file if this is an SBML model and it hasn't been retrieved yet.

		
			ZipFile archive = new ZipFile(file);
			ZipEntry entry = archive.getEntry(getFileName());
	        InputStream stream = archive.getInputStream(entry);
	        
	        Model casardf = ModelFactory.createDefaultModel();
	        Model cellmlrdf = ModelFactory.createDefaultModel();
	        
	        RDFReader casardfreader = casardf.getReader();
			casardfreader.setProperty("relativeURIs","same-document,relative");
			casardfreader.read(casardf, stream, "");
							
//			        if(rdfincellml.isEmpty()){
//			        	RDFReader cellmlrdfreader = cellmlrdf.getReader();
//			        	cellmlrdfreader.setProperty("relativeURIs","same-document,relative");
//			        	InputStream cellmlstream = new ByteArrayInputStream(rdfincellml.getBytes());
//			        	cellmlrdfreader.read(casardf, cellmlstream, AbstractRDFreader.TEMP_NAMESPACE);
//			        }
	        
			stream.close();
	        // If we find a reference to the model in the RDF resources, we use the file as the source of the model's annotations
	        for(Resource subj : casardf.listSubjects().toSet()){
	        			        	
	        	if (subj.getNameSpace().equals(semsimmodel.getLegacyCodeLocation().getFileName() + "#")){
	        		AbstractRDFreader.stripSemSimRelatedContentFromRDFblock(cellmlrdf, semsimmodel); // when read in rdfreader in CellML file there may be annotations that we want to ignore
					casardf.add(cellmlrdf.listStatements()); // Add curatorial statements to rdf model. When instantiate CASA reader, need to provide all RDF statements as string.
					
					String combinedrdf = AbstractRDFwriter.getRDFmodelAsString(casardf,"RDF/XML");
					
					archive.close();
					return new CASAreader(this, semsimmodel, sslib, combinedrdf);
	        	}
	        }	
	       archive.close();
		return null;
	}
	
}
