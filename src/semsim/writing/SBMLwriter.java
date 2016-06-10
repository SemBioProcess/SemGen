package semsim.writing;

import java.io.File;
import java.net.URI;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.semanticweb.owlapi.model.OWLException;

import semsim.SemSimLibrary;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;

public class SBMLwriter extends ModelWriter {
	
	private Model sbmlmodel;

	public SBMLwriter(SemSimModel model) {
		super(model);
	}

	@Override
	public void writeToFile(File destination) throws OWLException {
		
		SBMLDocument sbmldoc = new SBMLDocument();
		
		sbmlmodel = sbmldoc.createModel();
		
		addCompartments();
		addSpecies();
		addReactions();
		
	}

	private void addCompartments(){
		for(DataStructure ds : semsimmodel.getAssociatedDataStructures()){
			
			if(ds.hasPhysicalProperty() && ds.hasAssociatedPhysicalComponent()){
				
				URI propphysdefuri = ds.getPhysicalProperty().getPhysicalDefinitionURI();
				
				if(propphysdefuri == null) continue;
				
				if(propphysdefuri.equals(SemSimLibrary.OPB_FLUID_VOLUME_URI)){
//					sbmlmodel.createCompartment(ds.getName());
				}
			}
		}
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
