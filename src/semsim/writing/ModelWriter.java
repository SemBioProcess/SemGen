package semsim.writing;

import java.io.File;
import java.net.URI;

import org.semanticweb.owlapi.model.OWLException;

import semsim.SemSimLibrary;
import semsim.model.collection.SemSimModel;

public abstract class ModelWriter {
	protected static SemSimLibrary sslib;
	protected SemSimModel semsimmodel;
	protected File srcfile;
	
	ModelWriter(SemSimModel model) {
		semsimmodel = model;
	}
	
	public static void pointtoSemSimLibrary(SemSimLibrary lib) {
		sslib = lib;
	}
	
	public abstract void writeToFile(File destination) throws OWLException;
	
	public abstract void writeToFile(URI uri) throws OWLException;
}
