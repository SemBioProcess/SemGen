package semgen.merging.workbench;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;
import org.semanticweb.owlapi.model.OWLException;

import semsim.SemSimUtil;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.physical.Submodel;
import semsim.model.physical.object.FunctionalSubmodel;
import semsim.reading.CellMLreader;
import semsim.writing.CellMLwriter;
import JSim.util.Xcept;

public class Merger {
	private SemSimModel ssm1clone, ssm2clone;
	private ModelOverlapMap overlapmap;
	protected String error;
	private ArrayList<ResolutionChoice> choicelist;
	ArrayList<Pair<Double,String>> conversionfactors;
	
	public static enum ResolutionChoice {
		noselection, first, second, ignore; 
	}
	
	public Merger(SemSimModel model1, SemSimModel model2, ModelOverlapMap modelmap, 
			ArrayList<ResolutionChoice> choices, ArrayList<Pair<Double,String>> conversions) {
		ssm1clone = model1;
		ssm2clone = model2;
		overlapmap = modelmap;
		choicelist = choices;
		conversionfactors = conversions;
	}
	
	public SemSimModel merge() throws IOException, CloneNotSupportedException, OWLException, InterruptedException, JDOMException, Xcept {
		// First collect all the data structures that aren't going to be used in the resulting merged model
		
		SemSimModel modelfordiscardedds = null;
		int i = 0;
		DataStructure discardedds = null;
		DataStructure keptds = null;
		
		// IF there is one solution domain, get it, otherwise set soldom1 to null
		DataStructure soldom1 = (ssm1clone.getSolutionDomains().size()==1) ? ssm1clone.getSolutionDomains().toArray(new DataStructure[]{})[0] : null;

		for (Pair<DataStructure, DataStructure> dsp : overlapmap.getDataStructurePairs()) {
			if (choicelist.get(i).equals(ResolutionChoice.first)) {
				discardedds = dsp.getRight();
				keptds = dsp.getLeft();
				modelfordiscardedds = ssm2clone;
			}
			else if(choicelist.get(i).equals(ResolutionChoice.second)){
				discardedds = dsp.getLeft();
				keptds = dsp.getRight();
				modelfordiscardedds = ssm1clone;
			}
			
			// If "ignore equivalency" is not selected"
			if(keptds!=null && discardedds !=null){	
				if(keptds instanceof MappableVariable && discardedds instanceof MappableVariable){
					rewireMappedVariableDependencies((MappableVariable)keptds, (MappableVariable)discardedds, modelfordiscardedds, i);
				}
				else if (! replaceCodeWords(keptds, discardedds, modelfordiscardedds, soldom1, i)) 
					return null;
			}
			i++;
		}
	
		// What if both models have a custom phys component with the same name?
		SemSimModel mergedmodel = ssm1clone;
		
		// Create submodels representing the merged components, copy over all info from model2 into model1
		Submodel sub1 = new Submodel(ssm1clone.getName());
		sub1.setAssociatedDataStructures(ssm1clone.getDataStructures());
		sub1.setSubmodels(ssm1clone.getSubmodels());
		
		Submodel sub2 = new Submodel(ssm2clone.getName());
		sub2.setAssociatedDataStructures(ssm2clone.getDataStructures());
		
		if(soldom1!=null){
			sub2.addDataStructure(soldom1);
			
			if(ssm1clone.containsDataStructure(soldom1.getName() + ".min"))
				sub2.addDataStructure(ssm1clone.getDataStructure(soldom1.getName() + ".min"));
			if(ssm1clone.containsDataStructure(soldom1.getName() + ".max"))
				sub2.addDataStructure(ssm1clone.getDataStructure(soldom1.getName() + ".max"));
			if(ssm1clone.containsDataStructure(soldom1.getName() + ".delta"))
				sub2.addDataStructure(ssm1clone.getDataStructure(soldom1.getName() + ".delta"));
		}
		
		sub2.setSubmodels(ssm2clone.getSubmodels());
		mergedmodel.addSubmodel(sub1);
		mergedmodel.addSubmodel(sub2);
		
		// Copy in all data structures
		for(DataStructure dsfrom2 : ssm2clone.getDataStructures()){
			mergedmodel.addDataStructure(dsfrom2);
		}
		
		// Copy in the units
		mergedmodel.getUnits().addAll(ssm2clone.getUnits());
		
		// Copy in the submodels
		for(Submodel subfrom2 : ssm2clone.getSubmodels()){
			mergedmodel.addSubmodel(subfrom2);
		}
		
		// WHAT TO DO ABOUT ONTOLOGY-LEVEL ANNOTATIONS?
		mergedmodel.setNamespace(mergedmodel.generateNamespaceFromDateAndTime());
		
		return mergedmodel;
	}
	
	// Changes to variables when merging two models with CellML-style mapped variables
	private void rewireMappedVariableDependencies(MappableVariable keptds, MappableVariable discardedds, 
			SemSimModel modelfordiscardedds, int index){
		
		//For the codeword that's replaced, turn it into a component input and create a mapping from the kept codeword to the discarded one.
		discardedds.setPublicInterfaceValue("in");
		keptds.addVariableMappingTo(discardedds);
		
		//Take all mappedTo values for discarded codeword and apply them to kept codeword.
		for(MappableVariable mappedtods : discardedds.getMappedTo()){
			keptds.addVariableMappingTo(mappedtods);
		}
		
		// Remove all mappedTo DataStructures for discarded codeword.
		discardedds.getMappedTo().clear();
		
		//Also remove any initial_value that the discarded DS has.
		discardedds.setCellMLinitialValue(null);
		
		// Find mathML block for the discarded codeword and remove it from the FunctionalSubmodel's computational code
		FunctionalSubmodel fs = modelfordiscardedds.getParentFunctionalSubmodelForMappableVariable(discardedds);
		String componentMathMLstring = fs.getComputation().getMathML();
		String varname = discardedds.getName().replace(fs.getName() + ".", "");

		if(componentMathMLstring!=null){
			List<Content> componentMathML = CellMLwriter.makeXMLContentFromStringForMathML(componentMathMLstring);
			Iterator<Content> compmathmlit = componentMathML.iterator();
			Element varmathmlel = CellMLreader.getElementForOutputVariableFromComponentMathML(varname, compmathmlit);
			if(varmathmlel!=null){
				componentMathML.remove(varmathmlel.detach());
				fs.getComputation().setMathML(new XMLOutputter().outputString(componentMathML));
			}
		}
	}
	
	
	
	private boolean replaceCodeWords(DataStructure keptds, DataStructure discardedds, 
			SemSimModel modelfordiscardedds, DataStructure soldom1, int index) {
		// If we need to add in a unit conversion factor
		String replacementtext = keptds.getName();
		
		Pair<Double, String> conversionfactor = conversionfactors.get(index);
		// if the two terms have different names, or a conversion factor is required
		if(!discardedds.getName().equals(keptds.getName()) || conversionfactor.getLeft()!=1){
			replacementtext = "(" + keptds.getName() + conversionfactor.getRight() + String.valueOf(conversionfactor.getLeft()) + ")";
			SemSimUtil.replaceCodewordInAllEquations(discardedds, keptds, modelfordiscardedds, 
					discardedds.getName(), replacementtext, conversionfactor.getLeft());
		}
		// What to do about sol doms that have different units?
		
		if(discardedds.isSolutionDomain()){
		  // Re-set the solution domain designations for all DataStructures in model 2
			for(DataStructure nsdds : ssm2clone.getDataStructures()){
				if(nsdds.hasSolutionDomain())
					nsdds.setSolutionDomain(soldom1);
			}
		}
		
		// Remove the discarded Data Structure
		modelfordiscardedds.removeDataStructure(discardedds.getName());
		
		// If we are removing a state variable, remove its derivative, if present
		if(discardedds.hasSolutionDomain()){
			if(modelfordiscardedds.containsDataStructure(discardedds.getName() + ":" + discardedds.getSolutionDomain().getName())){
				modelfordiscardedds.removeDataStructure(discardedds.getName() + ":" + discardedds.getSolutionDomain().getName());
			}
		}
		
		// If the semantic resolution took care of a syntactic resolution
		if(!choicelist.get(index).equals(ResolutionChoice.ignore)){
			overlapmap.getIdenticalNames().remove(discardedds.getName());
		}
		return true;
	}
}
