package semsim.writing;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.JSBML;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.semanticweb.owlapi.model.OWLException;

import semsim.SemSimLibrary;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.utilities.SemSimUtil;

public class SBMLwriter extends ModelWriter {
	
	private SBMLDocument sbmldoc;
	private Model sbmlmodel;
	private LinkedHashMap<CompositePhysicalEntity, Compartment> entityCompartmentMap = new LinkedHashMap<CompositePhysicalEntity, Compartment>();
	private Set<DataStructure> candidateDSsForCompartments = new HashSet<DataStructure>();
	private Set<DataStructure> candidateDSsForSpecies = new HashSet<DataStructure>();
	private Set<DataStructure> candidateDSsForReactions = new HashSet<DataStructure>();
	
	
	public SBMLwriter(SemSimModel model) {
		super(model);
	}

	@Override
	public void writeToFile(File destination) {
		
		sbmldoc = new SBMLDocument();
		
		sbmlmodel = sbmldoc.createModel();
		
		sortDataStructuresIntoSBMLgroups();
		setLevelAndVersion();
		addCompartments();
		addSpecies();
		addReactions();
		
		// Catch errors
		if(sbmldoc.getErrorCount() > 0){
			for(int i = 0; i< sbmldoc.getErrorCount(); i++)
				System.err.println(sbmldoc.getError(i));
			
			return;
		}

		// If no errors, write out
		try {
			new SBMLWriter().writeSBMLToFile(sbmldoc, destination.getAbsolutePath());
		} catch (SBMLException | FileNotFoundException | XMLStreamException e) {
			e.printStackTrace();
		}
	}

	
	// Determine which data structures potentially simulate properties of SBML compartments,
	// species, and reactions
	private void sortDataStructuresIntoSBMLgroups(){
		
		Set<String> validOPBtermsForCompartments = new HashSet<String>();
		
		validOPBtermsForCompartments.add(SemSimLibrary.OPB_FLUID_VOLUME_URI.toString());
		validOPBtermsForCompartments.add(SemSimLibrary.OPB_AREA_OF_SPATIAL_ENTITY_URI.toString());
		validOPBtermsForCompartments.add(SemSimLibrary.OPB_SPAN_OF_SPATIAL_ENTITY_URI.toString());
		
		Set<String> validOPBtermsForSpecies = new HashSet<String>();
		
		validOPBtermsForSpecies.add(SemSimLibrary.OPB_CHEMICAL_CONCENTRATION_URI.toString());
		validOPBtermsForSpecies.add(SemSimLibrary.OPB_CHEMICAL_MOLAR_AMOUNT_URI.toString());
		validOPBtermsForSpecies.add(SemSimLibrary.OPB_PARTICLE_COUNT_URI.toString());
		validOPBtermsForSpecies.add(SemSimLibrary.OPB_PARTICLE_CONCENTRATION_URI.toString());
		validOPBtermsForSpecies.add(SemSimLibrary.OPB_MASS_OF_SOLID_ENTITY_URI.toString());
		validOPBtermsForSpecies.add(SemSimLibrary.OPB_MASS_LINEAL_DENSITY_URI.toString());
		validOPBtermsForSpecies.add(SemSimLibrary.OPB_MASS_AREAL_DENSITY_URI.toString());
		validOPBtermsForSpecies.add(SemSimLibrary.OPB_MASS_VOLUMETRIC_DENSITY_URI.toString());
		
		Set<String> validOPBtermsForReactions = new HashSet<String>();
		
		validOPBtermsForReactions.add(SemSimLibrary.OPB_CHEMICAL_MOLAR_FLOW_RATE_URI.toString());
		validOPBtermsForReactions.add(SemSimLibrary.OPB_MATERIAL_FLOW_RATE_URI.toString());
		validOPBtermsForReactions.add(SemSimLibrary.OPB_PARTICLE_FLOW_RATE_URI.toString());


		for(DataStructure ds : semsimmodel.getAssociatedDataStructures()){
			
			if(ds.hasPhysicalProperty()){
				
				URI propphysdefuri = ds.getPhysicalProperty().getPhysicalDefinitionURI();
				
				if(validOPBtermsForCompartments.contains(propphysdefuri.toString()))
					candidateDSsForCompartments.add(ds);
				
				else if(validOPBtermsForSpecies.contains(propphysdefuri.toString()))
					candidateDSsForSpecies.add(ds);
				
				else if(validOPBtermsForReactions.contains(propphysdefuri.toString()))
					candidateDSsForReactions.add(ds);
			}
		}
	}
	
	private void setLevelAndVersion(){
		sbmldoc.setLevel(2);
		sbmldoc.setVersion(4);
		sbmlmodel.setLevel(2);
		sbmlmodel.setVersion(4);
	}
	
	// Collect compartments for SBML model
	private void addCompartments(){
		for(DataStructure ds : candidateDSsForCompartments){
			
			PhysicalModelComponent pmc = ds.getAssociatedPhysicalModelComponent();
			
			URI propphysdefuri = ds.getPhysicalProperty().getPhysicalDefinitionURI();
			
			// Go to next data structure if there isn't a physical property associated with the current one
			if(propphysdefuri == null) continue;
			
			Compartment comp = null;
			
			if(propphysdefuri.equals(SemSimLibrary.OPB_FLUID_VOLUME_URI))
				comp = sbmlmodel.createCompartment(ds.getName());
			
			// Only write out spatialDimensions attribute if compartment is not 3D
			else if(propphysdefuri.equals(SemSimLibrary.OPB_AREA_OF_SPATIAL_ENTITY_URI)){
				comp = sbmlmodel.createCompartment(ds.getName());
				comp.setSpatialDimensions(2);
			}
			else if(propphysdefuri.equals(SemSimLibrary.OPB_SPAN_OF_SPATIAL_ENTITY_URI)){
				comp = sbmlmodel.createCompartment(ds.getName());
				comp.setSpatialDimensions(1);
			}
			
			// Go to next data structure if we didn't find an appropriate OPB property
			// or if the associated physical entity is NOT a composite physical entity
			if(comp == null || ! (pmc instanceof CompositePhysicalEntity)) continue;
			
			entityCompartmentMap.put((CompositePhysicalEntity)pmc, comp);
			
			String mathml = ds.getComputation().getMathML();
			
			// TODO: if size of compartment is variable, need to create an assignment
			if(ds.getComputationInputs().isEmpty() && mathml != null){
				
				//TODO: if mappable variable, need to use local name of datastructure as second parameter here
				String RHS = SemSimUtil.getRHSofMathML(mathml, ds.getName());
				ASTNode node = JSBML.readMathMLFromString(RHS);

				String formula = JSBML.formulaToString(node);					
				comp.setSize(Double.parseDouble(formula));
			}
			
		}
	}
	
	// Collect chemical species for SBML model
	private void addSpecies(){
		
	}
	
	// Collect chemical reactions for SBML model
	private void addReactions(){
	
	}
	
	
	@Override
	public void writeToFile(URI uri) throws OWLException {
		writeToFile(new File(uri));

	}

}
