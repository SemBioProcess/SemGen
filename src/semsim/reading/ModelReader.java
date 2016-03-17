package semsim.reading;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.semanticweb.owlapi.model.OWLException;

import semsim.SemSimLibrary;
import semsim.model.collection.SemSimModel;

public abstract class ModelReader {
	protected static SemSimLibrary sslib;
	protected SemSimModel semsimmodel = new SemSimModel();
	protected ModelAccessor modelaccessor;
	
	ModelReader(File file) {
		modelaccessor = new ModelAccessor(file);
	}
	
	ModelReader(ModelAccessor accessor){
		this.modelaccessor = accessor;
	}
	
	public static void pointtoSemSimLibrary(SemSimLibrary lib) {
		sslib = lib;
	}
	
	public abstract SemSimModel read() throws IOException, InterruptedException, OWLException, CloneNotSupportedException, XMLStreamException;
}
