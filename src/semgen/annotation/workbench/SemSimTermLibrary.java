package semgen.annotation.workbench;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Observable;

import semgen.SemGen;
import semsim.PropertyType;
import semsim.SemSimConstants;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.annotation.SemSimRelation;
import semsim.annotation.StructuralRelation;
import semsim.annotation.ReferenceOntologies.ReferenceOntology;
import semsim.model.SemSimTypes;
import semsim.model.collection.SemSimModel;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.CustomPhysicalProcess;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.PhysicalPropertyinComposite;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.model.physical.object.ReferencePhysicalProcess;
import semsim.writing.CaseInsensitiveComparator;

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
	
	private ArrayList<IndexCard<?>> masterlist = new ArrayList<IndexCard<?>>();
	public enum LibraryEvent {SINGULAR_TERM_REMOVED, SINGULAR_TERM_CREATED, SINGULAR_TERM_CHANGE, COMPOSITE_ENTITY_CHANGE, PROCESS_CHANGE, TERM_CHANGE};
	
	public SemSimTermLibrary(SemSimModel model) {
		for (PhysicalPropertyinComposite pp : SemGen.semsimlib.getCommonProperties()) {
			addAssociatePhysicalProperty(pp);
		}
		addTermsinModel(model);
	}
	
	
	/**
	 * Takes a semsimmodel and extracts all physical model components and adds them to the library if they're missing.
	 * @param model
	 */
	public void addTermsinModel(SemSimModel model) {
		for (PhysicalPropertyinComposite pp : model.getAssociatePhysicalProperties()) {
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
		int i = addAssociatePhysicalProperty(new PhysicalPropertyinComposite(name, uri));
		notifySingularAdded();
		return i;
	}
	
	private int addAssociatePhysicalProperty(PhysicalPropertyinComposite pp) {
		int i = getPhysicalPropertyIndex(pp);
		if (i!=-1) return i; 
		
		IndexCard<PhysicalPropertyinComposite> ppic = new IndexCard<PhysicalPropertyinComposite>(pp);
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
			rels.add(SemSimConstants.PART_OF_RELATION);
		}
		if (!rels.isEmpty()) {
			rels.remove(0);
		}
		return new CompositePhysicalEntity(pes, rels);
	}
	
	public int createCompositePhysicalEntity(ArrayList<Integer> peindicies) {
		//Avoid creating a composite with a null in the entity list
		if (peindicies.contains(-1)) return -1;

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
		int i = getPhysicalProcessIndex(proc);
		if (i!=-1) return i; 
		
		IndexCard<PhysicalProcess> ic = new IndexCard<PhysicalProcess>(proc);
		masterlist.add(ic);
		
		i = masterlist.indexOf(ic);
		procindexer.add(i);
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
	
	public Integer getComponentIndex(PhysicalModelComponent pmc) {
		for (IndexCard<?> card : masterlist) {
			if (card.isTermEquivalent(pmc)) return masterlist.indexOf(card);
		}
		return -1;
	}
	
		public PhysicalPropertyinComposite getAssociatePhysicalProperty(Integer index) {
			return (PhysicalPropertyinComposite)masterlist.get(index).getObject();
		}

		public Integer getPhysicalPropertyIndex(PhysicalPropertyinComposite pp) {
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
			return sortComponentIndiciesbyName(ppccompindexer);
		}
		
		public ArrayList<Integer> getSortedAssociatePhysicalPropertyIndiciesbyPropertyType(PropertyType type) {
			ArrayList<Integer> results = new ArrayList<Integer>();
			switch (type) {
			case PropertyOfPhysicalEntity:
				for (Integer i : ppccompindexer) {
					URI ppc = ((PhysicalPropertyinComposite)masterlist.get(i).getObject()).getPhysicalDefinitionURI();
					if (SemGen.semsimlib.OPBhasAmountProperty(ppc) || SemGen.semsimlib.OPBhasForceProperty(ppc)) {
						results.add(i);
					}
				}
				break;
			case PropertyOfPhysicalProcess:
				for (Integer i : ppccompindexer) {
					PhysicalPropertyinComposite ppc = (PhysicalPropertyinComposite)masterlist.get(i).getObject();
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
			return sortComponentIndiciesbyName(results);
		}
		
		public ArrayList<Integer> getSortedPhysicalPropertyIndicies() {
			return sortComponentIndiciesbyName(singppindexer);
		}

		public ReferencePhysicalEntity getReferencePhysicalEntity(Integer index) {
			return (ReferencePhysicalEntity)masterlist.get(index).getObject();
		}
		
		public ArrayList<Integer> getSortedReferencePhysicalEntityIndicies() {
			return sortComponentIndiciesbyName(rpeindexer);
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
		
		public PhysicalProcess getPhysicalProcess(Integer index) {
			return (PhysicalProcess)masterlist.get(index).getObject();
		}
			
		public Integer getPhysicalProcessIndex(PhysicalProcess process) {
			for (Integer i : procindexer) {
				if (masterlist.get(i).getName().equals(process.getName())) return i; 
			}
			return -1;
		}
		
		public ArrayList<Integer> getSortedPhysicalProcessIndicies() {
			return sortComponentIndiciesbyName(procindexer);
		}	
		
		public ArrayList<Integer> getSortedReferencePhysicalProcessIndicies() {
			ArrayList<Integer> refprocs = new ArrayList<Integer>();
			for (Integer i : procindexer) {
				if (masterlist.get(i).isReferenceTerm()) refprocs.add(i);
			}
			return sortComponentIndiciesbyName(refprocs);
		}	
		
		public ArrayList<Integer> getSortedSingularPhysicalEntityIndicies() {
			ArrayList<Integer> list = new ArrayList<Integer>();
			list.addAll(custpeindexer);
			list.addAll(rpeindexer);
			
			return sortComponentIndiciesbyName(list);
		}
		
		public ArrayList<Integer> getSortedCompositePhysicalEntityIndicies() {
			return sortComponentIndiciesbyName(cpeindexer);
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
			return sortComponentIndiciesbyName(list);
		}

	
//******************************************SHARED PROPERTY METHODS***************************//
		
		public ArrayList<String> getComponentNames(ArrayList<Integer> list) {
			ArrayList<String> namelist = new ArrayList<String>();
			for (Integer index : list) {
				namelist.add(masterlist.get(index).getName());
			}
			
			return namelist;
		}
		
		public String getComponentName(int index) {
			return masterlist.get(index).getName();
		}
		
		public String getComponentDescription(int index) {
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
		
		public void setName(int index, String name) {
			masterlist.get(index).getObject().setName(name);
			notifyTermChanged();
			
		}
		
		public void clearRelations(Integer termindex, SemSimRelation relation) {
			PhysicalModelComponent pmc = masterlist.get(termindex).getObject();
			pmc.removeReferenceAnnotationsofType(relation);
			notifyTermChanged();
		}
		
		public void addRelationship(Integer termindex, SemSimRelation relation, Integer reftermindex) {
			PhysicalModelComponent pmc = masterlist.get(termindex).getObject();
			ReferenceTerm refterm = (ReferenceTerm) masterlist.get(reftermindex).getObject();
			pmc.addReferenceOntologyAnnotation(relation, refterm.getPhysicalDefinitionURI(), refterm.getName());
			notifyTermChanged();
		}
		
		public SemSimTypes getSemSimType(int index) {
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
			return getCompositeEntityIndicies(getComponentIndex(cpe));
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
			rels.add(SemSimConstants.PART_OF_RELATION);
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
			ppmap.put(getComponentIndex(pe), pes.get(pe));
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
	
	public ArrayList<Integer> getProcessMediatorIndicies(Integer index) {
		PhysicalProcess process = getPhysicalProcess(index);
		ArrayList<Integer> mediators = new ArrayList<Integer>();
		for (PhysicalEntity entity : process.getMediators()) {
			mediators.add(getComponentIndex(entity));
		}
		return sortComponentIndiciesbyName(mediators);
	}
	
	public ArrayList<Integer> getAllProcessParticipantIndicies(Integer index) {
		ArrayList<Integer> parts = new ArrayList<Integer>();
		for (PhysicalEntity entity : getPhysicalProcess(index).getParticipants()) {
			parts.add(getComponentIndex(entity));
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
	
	public String listParticipants(Integer proc) {		
		if (proc==-1) return "";
		String pstring = "<html><body>";
		
		for(int source : getProcessSourcesIndexMultiplierMap(proc).keySet()){
			pstring = pstring + "<b>Source:</b> " + getComponentName(source) + "<br>";
		}
		for(int sink : getProcessSinksIndexMultiplierMap(proc).keySet()) {
			pstring = pstring + "<b>Sink:</b> " + getComponentName(sink) + "<br>";
		}
		for(int mediator : getProcessMediatorIndicies(proc)){
			pstring = pstring + "<b>Mediator:</b> " + getComponentName(mediator) + "<br>";
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
	
	public ArrayList<Integer> getIndiciesofReferenceRelations(int indexcard, SemSimRelation rel) {
		ArrayList<Integer> indicies = new ArrayList<Integer>();
		if (indexcard!=-1) {
			ArrayList<URI> uris = masterlist.get(indexcard).getAnnotationObjectsbyRelation(rel);
			
			for (URI uri : uris) {
				ReferenceTerm term = getReferenceTermbyURI(uri);
				indicies.add(getComponentIndex((PhysicalModelComponent)term));
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
	private ArrayList<Integer> sortComponentIndiciesbyName(ArrayList<Integer> indicies) {
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
	
	private void notifyCompositeEntityChanged() {
		setChanged();
		notifyObservers(LibraryEvent.COMPOSITE_ENTITY_CHANGE);
	}
	
	private void notifyTermChanged() {
		setChanged();
		notifyObservers(LibraryEvent.TERM_CHANGE);
	}
	
	public int countObjectofType(SemSimTypes type) {
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
		
		public ArrayList<URI> getAnnotationObjectsbyRelation(SemSimRelation rel) {
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
}
