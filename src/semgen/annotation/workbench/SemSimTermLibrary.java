package semgen.annotation.workbench;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Observable;
import java.util.Set;

import semgen.SemGen;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.annotation.Relation;
import semsim.definitions.PropertyType;
import semsim.definitions.SemSimRelations.StructuralRelation;
import semsim.definitions.ReferenceOntologies.ReferenceOntology;
import semsim.definitions.SemSimTypes;
import semsim.model.collection.SemSimModel;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalForce;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.CustomPhysicalForce;
import semsim.model.physical.object.CustomPhysicalProcess;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.PhysicalPropertyInComposite;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.model.physical.object.ReferencePhysicalProcess;
import semsim.utilities.CaseInsensitiveComparator;

/**
 * This class is the central repository for all SemSim annotations in an annotator tab. Each annotation is wrapped in an
 * instance of the IndexCard class which is stored in a single ordered list (masterlist). The indicies of each instance of a 
 * particular SemSim type are stored in seperate ArrayLists. Durring run time, items in the masterlist are not removed. Instead,
 * its index is removed from its corresponding type index list and it's flagged as deleted.
 * @author Christopher
 *
 */
public class SemSimTermLibrary extends Observable {
	
	private ReferenceOntology lastont;
	private ArrayList<Integer> ppccompindexer = new ArrayList<Integer>();
	private ArrayList<Integer> singppindexer = new ArrayList<Integer>();
	private ArrayList<Integer> rpeindexer = new ArrayList<Integer>();
	private ArrayList<Integer> custpeindexer = new ArrayList<Integer>();
	private ArrayList<Integer> cpeindexer = new ArrayList<Integer>();
	private ArrayList<Integer> procindexer = new ArrayList<Integer>();
	private ArrayList<Integer> forceindexer = new ArrayList<Integer>();
	
	private int refsearchtype = 0;
	private ArrayList<IndexCard<?>> masterlist = new ArrayList<IndexCard<?>>();
	public enum LibraryEvent {SINGULAR_TERM_REMOVED, SINGULAR_TERM_CREATED, SINGULAR_TERM_CHANGE, COMPOSITE_ENTITY_CHANGE,
		PROCESS_CHANGE,
		FORCE_CHANGE,
		TERM_CHANGE};
	
	public SemSimTermLibrary(SemSimModel model) {
		for (PhysicalPropertyInComposite pp : SemGen.semsimlib.getCommonProperties()) {
			addAssociatePhysicalProperty(pp);
		}
		addTermsinModel(model);
	}
	
	
	/**
	 * Takes a semsimmodel and extracts all physical model components and adds them to the library if they're missing.
	 * @param model
	 */
	public void addTermsinModel(SemSimModel model) {
		for (PhysicalPropertyInComposite pp : model.getAssociatePhysicalProperties()) {
			addAssociatePhysicalProperty(pp);
		}
		for (PhysicalProperty pp : model.getPhysicalProperties()) {
			addPhysicalProperty(pp);
		}
		for (ReferencePhysicalEntity rpe : model.getReferencePhysicalEntities()) {
			addReferencePhysicalEntity(rpe);
		}
		for (CustomPhysicalEntity cupe : model.getCustomPhysicalEntities()) {
			addCustomPhysicalEntity(cupe);
		}
		for (CompositePhysicalEntity cpe : model.getCompositePhysicalEntities()) {
			addCompositePhysicalEntity(cpe);
		}
		for (PhysicalProcess proc : model.getPhysicalProcesses()) {
			addPhysicalProcess(proc);
		}
	}
	
//**************************************TERM ADDITION METHODS***************************//
	
	public int createAssociatedPhysicalProperty(String name, URI uri) {
		int i = addAssociatePhysicalProperty(new PhysicalPropertyInComposite(name, uri));
		notifySingularAdded();
		return i;
	}
	
	private int addAssociatePhysicalProperty(PhysicalPropertyInComposite pp) {
		int i = getPhysicalPropertyIndex(pp);
		if (i!=-1) return i; 
		
		IndexCard<PhysicalPropertyInComposite> ppic = new IndexCard<PhysicalPropertyInComposite>(pp);
		masterlist.add(ppic);
		
		i = masterlist.indexOf(ppic);
		ppccompindexer.add(i);
		return i;
	}
	
	public int createPhysicalProperty(String name, URI uri) {
		int i = addPhysicalProperty(new PhysicalProperty(name, uri));
		notifySingularAdded();
		return i;
	}
	
	private int addPhysicalProperty(PhysicalProperty pp) {
		int i = getPhysicalPropertyIndex(pp);
		if (i!=-1) return i; 
		
		IndexCard<PhysicalProperty> ppic = new IndexCard<PhysicalProperty>(pp);
		masterlist.add(ppic);
		
		i = masterlist.indexOf(ppic);
		singppindexer.add(i);
		return i;
	}
	
	public int createReferencePhysicalEntity(String name, URI uri) {
		int i = addReferencePhysicalEntity(new ReferencePhysicalEntity(uri, name));
		notifySingularAdded();
		return i;
	}
	
	private int addReferencePhysicalEntity(ReferencePhysicalEntity rpe) {
		int i = this.getIndexofReferencePhysicalEntity(rpe);
		if (i!=-1) return i; 
		
		IndexCard<ReferencePhysicalEntity> ic = new IndexCard<ReferencePhysicalEntity>(rpe);
		masterlist.add(ic);
		
		i = masterlist.indexOf(ic);
		rpeindexer.add(i);
		return i;
	}
	
	private int addCustomPhysicalEntity(CustomPhysicalEntity cupe) {
		int i = getIndexofCustomPhysicalEntity(cupe);
		if (i!=-1) return i; 
		
		IndexCard<CustomPhysicalEntity> ic = new IndexCard<CustomPhysicalEntity>(cupe);
		masterlist.add(ic);
		
		i = masterlist.indexOf(ic);
		custpeindexer.add(i);
		return i;
	}
	
	public int createCustomPhysicalEntity(String name, String description) {
		int in = addCustomPhysicalEntity(new CustomPhysicalEntity(name, description)); 
		notifySingularAdded();
		return in;
	}

	private int addCompositePhysicalEntity(CompositePhysicalEntity cpe) {
		int i = getIndexofCompositePhysicalEntity(cpe);
		if (i!=-1) return i; 
		
		IndexCard<CompositePhysicalEntity> ic = new IndexCard<CompositePhysicalEntity>(cpe);
		masterlist.add(ic);
		
		i = masterlist.indexOf(ic);
		cpeindexer.add(i);
		return i;
	}
	
	private CompositePhysicalEntity makeCPE(ArrayList<Integer> peindicies) {
		ArrayList<PhysicalEntity> pes = new ArrayList<PhysicalEntity>();
		ArrayList<StructuralRelation> rels = new ArrayList<StructuralRelation>();
		for (Integer i : peindicies) {
			pes.add((PhysicalEntity)masterlist.get(i).getObject());
			rels.add(StructuralRelation.PART_OF);
		}
		if (!rels.isEmpty()) {
			rels.remove(0);
		}
		return new CompositePhysicalEntity(pes, rels);
	}
	
	public int createCompositePhysicalEntity(ArrayList<Integer> peindicies) {
		//Avoid creating a composite with a null in the entity list
		//Return negative index if list is empty
		if (peindicies.contains(-1) || peindicies.isEmpty()) return -1;

		return addCompositePhysicalEntity(makeCPE(peindicies));	
	}
	
	public boolean containsCompositeEntitywithTerms(ArrayList<Integer> peindicies) {
		return getIndexofCompositePhysicalEntity(makeCPE(peindicies))!=-1;
	}
	
	public int createReferencePhysicalProcess(String name, URI uri) {
		int i = addPhysicalProcess(new ReferencePhysicalProcess(uri, name));
		notifyProcessChanged();
		return i;
	}
	
	public int createProcess(String name, String desc) {
		int in = addPhysicalProcess(new CustomPhysicalProcess(name, desc));
		notifyProcessChanged();
		return in;
	}
	
	private int addPhysicalProcess(PhysicalProcess proc) {
		int i = getPhysicalProcessIndexByName(proc);
		if (i!=-1) return i; 
		
		IndexCard<PhysicalProcess> ic = new IndexCard<PhysicalProcess>(proc);
		masterlist.add(ic);
		
		i = masterlist.indexOf(ic);
		procindexer.add(i);
		return i;
	}
	
	private int addPhysicalForce(PhysicalForce force) {
		int i = getComponentIndex(force, false);
		if (i != -1) return i; // do not add if already in master list
		
		IndexCard<PhysicalForce> ic = new IndexCard<PhysicalForce>(force);
		masterlist.add(ic);
		
		i = masterlist.indexOf(ic);
		forceindexer.add(i);
		return i;
	}
	
	public void modifyCustomPhysicalEntity(int index, String name, String description) {
		CustomPhysicalEntity cpe = getCustomPhysicalEntity(index);
		cpe.setName(name);
		cpe.setDescription(description);
		notifySingularChanged();
	}
	
//*************************************************OBJECT RETRIEVAL METHODS***********************//
	
	public PhysicalModelComponent getComponent(Integer index) {
		return masterlist.get(index).getObject();
	}
	
	public Integer getComponentIndex(PhysicalModelComponent pmc, boolean assumeuniqueprocesses) {
		
		if(pmc==null) return -1;
		
		// For cases where we're looking up a physical model component and we're assuming that all processes are unique
		// (i.e. not equivalent even if they have same participants)
		if((pmc.getSemSimType()==SemSimTypes.CUSTOM_PHYSICAL_PROCESS) && assumeuniqueprocesses){
			for (Integer index : procindexer) {
				IndexCard<?> card = masterlist.get(index);
				if (card.component==pmc && !card.removed) return masterlist.indexOf(card);
			}
		}
		// Otherwise...
		else {
			for (IndexCard<?> card : masterlist) {
				if (card.isTermEquivalent(pmc)) return masterlist.indexOf(card);
			}
		}
		return -1;
	}
	
	// Retrieving physical properties
	public PhysicalPropertyInComposite getAssociatePhysicalProperty(Integer index) {
		return (PhysicalPropertyInComposite)masterlist.get(index).getObject();
	}
	
	public Integer getPhysicalPropertyIndex(PhysicalPropertyInComposite pp) {
		for (Integer i : ppccompindexer) {
			if (masterlist.get(i).isTermEquivalent(pp)) return i; 
		}
		return -1;
	}
	
	public Integer getPhysicalPropertyIndex(PhysicalProperty pp) {
		for (Integer i : singppindexer) {
			if (masterlist.get(i).isTermEquivalent(pp)) return i; 
		}
		return -1;
	}
	
	public ArrayList<Integer> getSortedAssociatePhysicalPropertyIndicies() {
		return sortComponentIndicesbyName(ppccompindexer);
	}
	
	public ArrayList<Integer> getSortedAssociatePhysicalPropertyIndiciesbyPropertyType(PropertyType type) {
		ArrayList<Integer> results = new ArrayList<Integer>();
		switch (type) {
		case PropertyOfPhysicalEntity:
			for (Integer i : ppccompindexer) {
				URI ppc = ((PhysicalPropertyInComposite)masterlist.get(i).getObject()).getPhysicalDefinitionURI();
				if (SemGen.semsimlib.OPBhasAmountProperty(ppc) || SemGen.semsimlib.OPBhasForceProperty(ppc)) {
					results.add(i);
				}
			}
			break;
		case PropertyOfPhysicalProcess:
			for (Integer i : ppccompindexer) {
				PhysicalPropertyInComposite ppc = (PhysicalPropertyInComposite)masterlist.get(i).getObject();
				if (SemGen.semsimlib.OPBhasFlowProperty(ppc.getPhysicalDefinitionURI())) {
					results.add(i);
				}
			}
			break;
		case Unknown:
			break;
		default:
			break;
		}
		return sortComponentIndicesbyName(results);
	}
	
	public ArrayList<Integer> getSortedPhysicalPropertyIndicies() {
		return sortComponentIndicesbyName(singppindexer);
	}

	public ReferencePhysicalEntity getReferencePhysicalEntity(Integer index) {
		return (ReferencePhysicalEntity)masterlist.get(index).getObject();
	}
	
	public ArrayList<Integer> getSortedReferencePhysicalEntityIndicies() {
		return sortComponentIndicesbyName(rpeindexer);
	}
	
	public int getIndexofReferencePhysicalEntity(ReferencePhysicalEntity rpe) {
		for (Integer i : rpeindexer) {
			if (masterlist.get(i).isTermEquivalent(rpe)) return i; 
		}
		return -1;
	}

	public CustomPhysicalEntity getCustomPhysicalEntity(Integer index) {
		return (CustomPhysicalEntity)masterlist.get(index).getObject();
	}

	public int getIndexofCustomPhysicalEntity(CustomPhysicalEntity cupe) {
		for (Integer i : custpeindexer) {
			if (masterlist.get(i).isTermEquivalent(cupe)) {
				return i; 
			}
		}
		return -1;
	}
	
	public CompositePhysicalEntity getCompositePhysicalEntity(Integer index) {
		return (CompositePhysicalEntity)masterlist.get(index).getObject();
	}

	public int getIndexofCompositePhysicalEntity(CompositePhysicalEntity cpe) {
		for (Integer i : cpeindexer) {
			if (masterlist.get(i).isTermEquivalent(cpe)) return i; 
		}
		return -1;
	}
	
	public ArrayList<Integer> getSortedSingularPhysicalEntityIndicies() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.addAll(custpeindexer);
		list.addAll(rpeindexer);
		
		return sortComponentIndicesbyName(list);
	}
	
	public ArrayList<Integer> getSortedCompositePhysicalEntityIndicies() {
		return sortComponentIndicesbyName(cpeindexer);
	}
	
	// Retrieving physical processes
	
	public PhysicalProcess getPhysicalProcess(Integer index) {
		return (PhysicalProcess)masterlist.get(index).getObject();
	}
	
	public PhysicalForce getPhysicalForce(Integer index) {
		return (PhysicalForce)masterlist.get(index).getObject();
	}
		
	public Integer getPhysicalProcessIndexByName(PhysicalProcess process) {
		for (Integer i : procindexer) {
			if (masterlist.get(i).getName().equals(process.getName())) return i; 
		}
		return -1;
	}
	
	public ArrayList<Integer> getSortedPhysicalProcessIndicies() {
		return sortComponentIndicesbyName(procindexer);
	}	
	
	public ArrayList<Integer> getSortedReferencePhysicalProcessIndicies() {
		ArrayList<Integer> refprocs = new ArrayList<Integer>();
		for (Integer i : procindexer) {
			if (masterlist.get(i).isReferenceTerm()) refprocs.add(i);
		}
		return sortComponentIndicesbyName(refprocs);
	}	
	
	public ArrayList<Integer> getAllReferenceTerms() {
		ArrayList<Integer> refterms = new ArrayList<Integer>();
		for (IndexCard<?> card : masterlist) {
			if (card.isReferenceTerm()) {
				refterms.add(masterlist.indexOf(card));
			}
		}
		return refterms;
	}
	
	public ArrayList<Integer> getRequestedTypes(SemSimTypes[] types) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (SemSimTypes type : types) {
			switch (type) {
			case COMPOSITE_PHYSICAL_ENTITY:
				list.addAll(cpeindexer);
				break;
			case CUSTOM_PHYSICAL_ENTITY:
				list.addAll(custpeindexer);
				break;
			case PHYSICAL_PROCESS:
				list.addAll(procindexer);
				break;
			case CUSTOM_PHYSICAL_FORCE:
				list.addAll(forceindexer);
				break;
			case PHYSICAL_PROPERTY:
				list.addAll(singppindexer);
				break;
			case PHYSICAL_PROPERTY_IN_COMPOSITE:
				list.addAll(ppccompindexer);
				break;
			case REFERENCE_PHYSICAL_ENTITY:
				list.addAll(rpeindexer);
				break;
			default:
				break;
			}
		}
		return sortComponentIndicesbyName(list);
	}

	
//******************************************SHARED PROPERTY METHODS***************************//
		
	public ArrayList<String> getComponentNames(ArrayList<Integer> list) {
		ArrayList<String> namelist = new ArrayList<String>();
		for (Integer index : list) {
			namelist.add(masterlist.get(index).getName());
		}
		
		return namelist;
	}
	
	public String getComponentName(Integer index) {
		return masterlist.get(index).getName();
	}
	
	public String getComponentDescription(Integer index) {
		String desc = masterlist.get(index).getDescription();
		if (desc==null) desc="";
		return desc;
	}
	
	/** Check if a component in the library already has a given name
	 * 
	 * @param nametocheck
	 * @return
	 */
	public int libraryHasName(String nametocheck) {
		for (IndexCard<?> card : masterlist) {
			if (nametocheck.equalsIgnoreCase(card.getName().trim())) return masterlist.indexOf(card);
		}
		return -1;
	}
	
	public void setName(Integer index, String name) {
		masterlist.get(index).getObject().setName(name);
		notifyTermChanged();
		
	}
	
	public void clearRelations(Integer termindex, Relation relation) {
		PhysicalModelComponent pmc = masterlist.get(termindex).getObject();
		pmc.removeReferenceAnnotationsWithRelation(relation);
		notifyTermChanged();
	}
	
	public void addRelationship(Integer termindex, Relation relation, Integer reftermindex) {
		PhysicalModelComponent pmc = masterlist.get(termindex).getObject();
		ReferenceTerm refterm = (ReferenceTerm) masterlist.get(reftermindex).getObject();
		pmc.addReferenceOntologyAnnotation(relation, refterm.getPhysicalDefinitionURI(), refterm.getName(), SemGen.semsimlib);
		notifyTermChanged();
	}
	
	public SemSimTypes getSemSimType(Integer index) {
		return masterlist.get(index).getType();
	}
		
//**************************************REFERENCE TERM DATA RETRIEVAL METHODS *********************//
	public String getOntologyName(Integer index) {
		return ((ReferenceTerm)masterlist.get(index).getObject()).getOntologyName(SemGen.semsimlib);
	}
	
	public String getReferenceID(Integer index) {
		return ((ReferenceTerm)masterlist.get(index).getObject()).getTermID();
	}
		
//**************************************COMPOSITE ENTITY DATA RETRIEVAL METHODS *********************//
	public ArrayList<Integer> getCompositeEntityIndicies(CompositePhysicalEntity cpe) {
		return getCompositeEntityIndicies(getIndexofCompositePhysicalEntity(cpe));
	}
	
	public ArrayList<Integer> getCompositeEntityIndicies(Integer index) {
		ArrayList<Integer> indexlist = new ArrayList<Integer>();
		CompositePhysicalEntity cpe = getCompositePhysicalEntity(index);
		
		for (PhysicalEntity pe : cpe.getArrayListOfEntities()) {
			int i;
			if (pe.hasPhysicalDefinitionAnnotation()) {
				i = getIndexofReferencePhysicalEntity((ReferencePhysicalEntity)pe);
			}
			else {
				i = getIndexofCustomPhysicalEntity((CustomPhysicalEntity)pe);
			}
			indexlist.add(i);
		}
		return indexlist;
	}
	
	public boolean compositeEntityContainsSingular(int compindex, int singindex) {
		return getCompositeEntityIndicies(compindex).contains(singindex);
	}
		
//**************************************COMPOSITE ENTITY MODIFICATION METHODS *********************//
		
	public void setCompositeEntityComponents(Integer index, ArrayList<Integer> peindicies) {
		ArrayList<PhysicalEntity> pes = new ArrayList<PhysicalEntity>();
		ArrayList<StructuralRelation> rels = new ArrayList<StructuralRelation>();
		for (Integer i : peindicies) {
			pes.add((PhysicalEntity)masterlist.get(i).getObject());
			rels.add(StructuralRelation.PART_OF);
		}
		if (!rels.isEmpty()) {
			rels.remove(0);
		}
		CompositePhysicalEntity cpetoedit = (CompositePhysicalEntity)masterlist.get(index).getObject();
		cpetoedit.setArrayListOfEntities(pes);
		cpetoedit.setArrayListOfStructuralRelations(rels);
		notifyCompositeEntityChanged();
	}
		
//******************************************PROCESS DATA RETRIEVAL METHODS*********************************//
	
	private LinkedHashMap<Integer, Double> getIndexMultiplierMap(LinkedHashMap<PhysicalEntity, Double> pes) {
		LinkedHashMap<Integer, Double>  ppmap = new LinkedHashMap<Integer, Double>();
		
		for (PhysicalEntity pe : pes.keySet()) {
			Integer peindex = getComponentIndex(pe, false);
			ppmap.put(peindex, pes.get(pe));
		}
		
		return ppmap;
	}
	
	public Double getMultiplier(Integer procindex, Integer partindex) {
		LinkedHashMap<Integer, Double> map = new LinkedHashMap<Integer, Double>();
		map.putAll(getProcessSourcesIndexMultiplierMap(procindex));
		map.putAll(getProcessSinksIndexMultiplierMap(procindex));
		return map.get(partindex);
	}
	
	public LinkedHashMap<Integer, Double> getProcessSourcesIndexMultiplierMap(Integer index) {
		PhysicalProcess process = getPhysicalProcess(index);
		return getIndexMultiplierMap(process.getSources());
	}
	
	public LinkedHashMap<Integer, Double> getProcessSinksIndexMultiplierMap(Integer index) {
		PhysicalProcess process = getPhysicalProcess(index);
		return getIndexMultiplierMap(process.getSinks());
	}
	
	public Set<Integer> getIndiciesOfForceSources(Integer forceindex){
		PhysicalForce force = getPhysicalForce(forceindex);
		
		Set<Integer> tempset = new HashSet<Integer>();
		
		for(PhysicalEntity source : force.getSources()) 
			tempset.add(this.getComponentIndex(source, false));
		
		return tempset;
	}
	
	public Set<Integer> getIndiciesOfForceSinks(Integer forceindex){
		PhysicalForce force = getPhysicalForce(forceindex);
		
		Set<Integer> tempset = new HashSet<Integer>();
		
		for(PhysicalEntity source : force.getSinks()) 
			tempset.add(this.getComponentIndex(source, false));
		
		return tempset;
	}
	
	public ArrayList<Integer> getProcessMediatorIndicies(Integer index) {
		PhysicalProcess process = getPhysicalProcess(index);
		ArrayList<Integer> mediators = new ArrayList<Integer>();
		for (PhysicalEntity entity : process.getMediators()) {
			mediators.add(getComponentIndex(entity, false));
		}
		return sortComponentIndicesbyName(mediators);
	}
	
	public ArrayList<Integer> getAllProcessParticipantIndicies(Integer index) {
		ArrayList<Integer> parts = new ArrayList<Integer>();
		for (PhysicalEntity entity : getPhysicalProcess(index).getParticipants()) {
			parts.add(getComponentIndex(entity, false));
		}
		return parts;
	}
	
	public Double getSourceMultiplier(Integer procindex, Integer partindex) {
		LinkedHashMap<Integer, Double> map = getProcessSourcesIndexMultiplierMap(procindex);
		return map.get(partindex);
	}
	
	public Double getSinkMultiplier(Integer procindex, Integer partindex) {
		LinkedHashMap<Integer, Double> map = getProcessSinksIndexMultiplierMap(procindex);
		return map.get(partindex);
	}
	
	public String listProcessParticipants(Integer proc) {		
		if (proc==-1) return "";
		String pstring = "<html><body>";
		
		for(int source : getProcessSourcesIndexMultiplierMap(proc).keySet()){
			Double mult = getProcessSourcesIndexMultiplierMap(proc).get(source);
			String multstring = (mult==1.0) ? "" : " (X" + Double.toString(mult) + ")";
			pstring = pstring + "<b>Source:</b> " + getComponentName(source) + multstring + "<br>";
		}
		for(int sink : getProcessSinksIndexMultiplierMap(proc).keySet()) {
			Double mult = getProcessSinksIndexMultiplierMap(proc).get(sink);
			String multstring = (mult==1.0) ? "" : " (X" + Double.toString(mult) + ")";
			pstring = pstring + "<b>Sink:</b> " + getComponentName(sink) + multstring + "<br>";
		}
		for(int mediator : getProcessMediatorIndicies(proc)){
			pstring = pstring + "<b>Mediator:</b> " + getComponentName(mediator) + "<br>";
		}
		
		return pstring + "</body></html>";
	}
	
	
	public String listForceParticipants(Integer force) {		
		if (force==-1) return "";
		String pstring = "<html><body>";
		
		for(int source : getIndiciesOfForceSources(force)){
			pstring = pstring + "<b>Source:</b> " + getComponentName(source) + "<br>";
		}
		for(int sink : getIndiciesOfForceSinks(force)) {
			pstring = pstring + "<b>Sink:</b> " + getComponentName(sink) + "<br>";
		}
		
		return pstring + "</body></html>";
	}
	
//*******************************************PROCESS MODIFICATION METHODS**************************************//
	
	public void editProcess(Integer procindex, String name, String desc) {
		PhysicalProcess proc = getPhysicalProcess(procindex);
		proc.setName(name);
		proc.setDescription(desc);
		notifyProcessChanged();
	}
	
	
	// Store participant info for processes
	public void setProcessSources(Integer procindex, ArrayList<Integer> sources, ArrayList<Double> mults) {
		LinkedHashMap<PhysicalEntity, Double> map = new LinkedHashMap<PhysicalEntity, Double>();
		for (int i=0; i<sources.size(); i++) {
			map.put(getCompositePhysicalEntity(sources.get(i)), mults.get(i));
		}
		getPhysicalProcess(procindex).setSources(map);
	}
	
	public void setProcessSinks(Integer procindex, ArrayList<Integer> sinks, ArrayList<Double> mults) {
		LinkedHashMap<PhysicalEntity, Double> map = new LinkedHashMap<PhysicalEntity, Double>();
		for (int i=0; i<sinks.size(); i++) {
			map.put(getCompositePhysicalEntity(sinks.get(i)), mults.get(i));
		}
		getPhysicalProcess(procindex).setSinks(map);
	}
	
	public void setProcessMediators(Integer procindex, ArrayList<Integer> mediators) {
		HashSet<PhysicalEntity> map = new HashSet<PhysicalEntity>();
		for (Integer mediator : mediators) {
			map.add(this.getCompositePhysicalEntity(mediator));
		}
		getPhysicalProcess(procindex).setMediators(map);
	}
	
	
	
	// Store info for forces
	public int createForce() {
		int in = addPhysicalForce(new CustomPhysicalForce());
		notifyForceChanged();
		return in;
	}
	
	public void editForce(Integer forceindex) {
		notifyForceChanged();
	}
	
	public void setForceSources(Integer forceindex, ArrayList<Integer> sources) {
		System.out.println("Setting sources");
		Set<PhysicalEntity> entset = new HashSet<PhysicalEntity>();
		for (int i=0; i<sources.size(); i++) {
			entset.add(getCompositePhysicalEntity(sources.get(i)));
		}
		getPhysicalForce(forceindex).setSources(entset);
	}
	
	public void setForceSinks(Integer forceindex, ArrayList<Integer> sinks) {
		Set<PhysicalEntity> entset = new HashSet<PhysicalEntity>();
		for (int i=0; i<sinks.size(); i++) {
			entset.add(getCompositePhysicalEntity(sinks.get(i)));
		}
		getPhysicalForce(forceindex).setSinks(entset);
	}
	

	public PropertyType getPropertyinCompositeType(int index) {
		return SemGen.semsimlib.getPropertyinCompositeType(getAssociatePhysicalProperty(index));
	}
	
	public boolean isReferenceTerm(Integer index) {
		if (index!=-1) {
			return masterlist.get(index).isReferenceTerm();
		}
		return false;
	}
	
	public URI getReferenceComponentURI(Integer index) {
		return masterlist.get(index).getReferenceURI();
	}
	
	public ReferenceTerm getReferenceTermbyURI(URI uri) {
		for (Integer i : getAllReferenceTerms()) {
			ReferenceTerm term = (ReferenceTerm) masterlist.get(i).getObject();
			if (term.getPhysicalDefinitionURI().equals(uri)) return term;
		}
		return null;
	}
	
	public ArrayList<Integer> getIndiciesofReferenceRelations(int indexcard, Relation rel) {
		ArrayList<Integer> indicies = new ArrayList<Integer>();
		if (indexcard!=-1) {
			ArrayList<URI> uris = masterlist.get(indexcard).getAnnotationObjectsbyRelation(rel);
			
			for (URI uri : uris) {
				ReferenceTerm term = getReferenceTermbyURI(uri);
				indicies.add(getComponentIndex((PhysicalModelComponent)term, true));
			}
		}
		return indicies;
	}

//******************************OBJECT REMOVAL METHODS********************************//
	public void removePhysicalProperty(Integer index) {
		ppccompindexer.remove(index);
		masterlist.get(index).setRemoved(true);
		notifySingularChanged();
	}
	
	public void removeSingularPhysicalProperty(Integer index) {
		singppindexer.remove(index);
		masterlist.get(index).setRemoved(true);
		notifySingularChanged();
	}
	
	public void removeReferencePhysicalEntity(Integer index) {
		rpeindexer.remove(index);
		masterlist.get(index).setRemoved(true);
		notifySingularRemoved();
	}

	public void removeCustomPhysicalEntity(Integer index) {
		custpeindexer.remove(index);
		masterlist.get(index).setRemoved(true);
		notifySingularRemoved();
	}

	public void removeCompositePhysicalEntity(Integer index) {
		cpeindexer.remove(index);
		masterlist.get(index).setRemoved(true);
		notifyCompositeEntityChanged();
	}

	public void removePhysicalProcesses(Integer index) {
		procindexer.remove(index);
		masterlist.get(index).setRemoved(true);
		notifyProcessChanged();
	}

//*************************************HELPER METHODS*************************************//
	private ArrayList<Integer> sortComponentIndicesbyName(ArrayList<Integer> indicies) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for (Integer i : indicies) {
			map.put(masterlist.get(i).getName(), i);
		}
		ArrayList<String> names = new ArrayList<String>(map.keySet());
		Collections.sort(names, new CaseInsensitiveComparator());
		
		ArrayList<Integer> sortedlist = new ArrayList<Integer>();
		
		for (String s : names) {
			sortedlist.add(map.get(s));
		}
		return sortedlist;
	}
	
	public ReferenceOntology getLastOntology() {
		return lastont;
	}
	
	public void setLastOntology(ReferenceOntology ont) {
		lastont = ont;
	}
		
	public boolean isTerm(int index) {
		return !masterlist.get(index).removed;
	}
	
	private void notifySingularAdded() {
		setChanged();
		notifyObservers(LibraryEvent.SINGULAR_TERM_CREATED);
	}
	
	private void notifySingularRemoved() {
		setChanged();
		notifyObservers(LibraryEvent.SINGULAR_TERM_REMOVED);
	}
	
	private void notifySingularChanged() {
		setChanged();
		notifyObservers(LibraryEvent.SINGULAR_TERM_CHANGE);
	}
	
	private void notifyProcessChanged() {
		setChanged();
		notifyObservers(LibraryEvent.PROCESS_CHANGE);
	}
	
	private void notifyForceChanged(){
		setChanged();
		notifyObservers(LibraryEvent.FORCE_CHANGE);
	}
	
	private void notifyCompositeEntityChanged() {
		setChanged();
		notifyObservers(LibraryEvent.COMPOSITE_ENTITY_CHANGE);
	}
	
	private void notifyTermChanged() {
		setChanged();
		notifyObservers(LibraryEvent.TERM_CHANGE);
	}
	
	public int countObjectOfType(SemSimTypes type) {
		int cnt = 0;
		switch (type) {
		case COMPOSITE_PHYSICAL_ENTITY:
			cnt = cpeindexer.size();
			break;
		case CUSTOM_PHYSICAL_ENTITY:
			cnt = custpeindexer.size();
			break;
		case PHYSICAL_PROCESS:
			cnt = procindexer.size();
			break;
		case CUSTOM_PHYSICAL_FORCE:
			cnt = forceindexer.size();
			break;
		case PHYSICAL_PROPERTY:
			cnt = singppindexer.size();
			break;
		case PHYSICAL_PROPERTY_IN_COMPOSITE:
			cnt = ppccompindexer.size();
			break;
		case REFERENCE_PHYSICAL_ENTITY:
			cnt = rpeindexer.size();
			break;
		default:
			break;
		
		}
		return cnt;
	}
	

	//*********************************INDEX CARD DEFINITION************************************//
	protected class IndexCard<T extends PhysicalModelComponent> {
		private T component;
		private Boolean reference;
		private Boolean removed = false;
		
		public IndexCard(T comp) {
			component = comp;
			reference = component.hasPhysicalDefinitionAnnotation();
		}
		
		public String getDescription() {
			return component.getDescription();
		}

		public T getObject() {
			return component;
		}
		
		public boolean isReferenceTerm() {
			if (removed) return false;
			return reference;
		}
		
		public String getName() {
			if (reference) return ((ReferenceTerm)component).getNamewithOntologyAbreviation(SemGen.semsimlib);
			return component.getName();
		}
		
		public URI getReferenceURI() {
			if (isReferenceTerm()) {
				return ((ReferenceTerm)component).getPhysicalDefinitionURI();
			}
			return null;
		}
		
		public ArrayList<URI> getAnnotationObjectsbyRelation(Relation rel) {
			ArrayList<URI> uris = new ArrayList<URI>();
			
			for (ReferenceOntologyAnnotation ann : component.getReferenceOntologyAnnotations(rel)) {
				uris.add(ann.getReferenceURI());
			}
			
			return uris;
		}
		
		public void setRemoved(Boolean remove) {
			removed = remove;
		}
		
		public Boolean isTermEquivalent(PhysicalModelComponent term) {
			if (removed) return false;
			return component.equals(term);
		}
		
		public SemSimTypes getType() {
			return component.getSemSimType();
		}
	}
	
	/**
	 * @return the refsearchtype
	 */
	public int getReferenceSearchType() {
		return refsearchtype;
	}


	/**
	 * @param refsearchtype the refsearchtype to set
	 */
	public void setReferenceSearchType(int refsearchtype) {
		this.refsearchtype = refsearchtype;
	}

}
