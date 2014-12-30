package semsim.reading;

import java.io.File;
import java.io.IOException;

import org.semanticweb.owlapi.model.OWLException;

import semsim.SemSimLibrary;
import semsim.model.SemSimModel;

public abstract class ModelReader {
	protected static SemSimLibrary sslib;
	protected SemSimModel semsimmodel = new SemSimModel();
	protected File srcfile;
	
	ModelReader(File file) {
		srcfile = file;
	}
	
	public static void pointtoSemSimLibrary(SemSimLibrary lib) {
		sslib = lib;
	}
	
	public abstract SemSimModel readFromFile() throws IOException, InterruptedException, OWLException, CloneNotSupportedException;
}
