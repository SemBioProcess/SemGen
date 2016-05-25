package semsim.writing;

import java.io.File;
import java.net.URI;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.semanticweb.owlapi.model.OWLException;

import semsim.model.collection.SemSimModel;

public class SBMLwriter extends ModelWriter {

	public SBMLwriter(SemSimModel model) {
		super(model);
	}

	@Override
	public void writeToFile(File destination) throws OWLException {
		SBMLDocument sbmldoc = new SBMLDocument();
		Model sbmlmodel = sbmldoc.createModel();
		
		addCompartments();
		addSpecies();
		addReactions();
		

	}

	private void addCompartments(){
		
	}
	
	private void addSpecies(){
		
	}
	
	private void addReactions(){
	
	}
	
	
	@Override
	public void writeToFile(URI uri) throws OWLException {
		// TODO Auto-generated method stub

	}

}
