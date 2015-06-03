package semgen.annotation.workbench;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.tuple.Triple;

import semgen.SemGen;
import semsim.SemSimConstants;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.annotation.SemSimRelation;
import semsim.annotation.StructuralRelation;
import semsim.model.SemSimModel;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.PhysicalPropertyinComposite;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.utilities.ReferenceOntologies.ReferenceOntology;
import semsim.writing.CaseInsensitiveComparator;

public class SemSimTermLibrary {
	
	private ReferenceOntology lastont;
	ArrayList<Integer> cpepps = new ArrayList<Integer>();
	ArrayList<Integer> pps = new ArrayList<Integer>();
	ArrayList<Integer> rpes = new ArrayList<Integer>();
	ArrayList<Integer> cupes = new ArrayList<Integer>();
	ArrayList<Integer> cpes = new ArrayList<Integer>();
	ArrayList<Integer> procs = new ArrayList<Integer>();
	
	ArrayList<IndexCard<?>> masterlist = new ArrayList<IndexCard<?>>();
	
	public SemSimTermLibrary(SemSimModel model) {
		for (PhysicalPropertyinComposite pp : SemGen.semsimlib.getCommonProperties()) {
			addAssociatePhysicalProperty(pp);
		}
		addTermsinModel(model);
	}
	
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
	
	public int addAssociatePhysicalProperty(PhysicalPropertyinComposite pp) {
		int i = getPhysicalPropertyIndex(pp);
		if (i!=-1) return i; 
		
		IndexCard<PhysicalPropertyinComposite> ppic = new IndexCard<PhysicalPropertyinComposite>(pp);
		masterlist.add(ppic);
		
		i = masterlist.indexOf(ppic);
		cpepps.add(i);
		return i;
	}
	
	public int addPhysicalProperty(PhysicalProperty pp) {
		int i = getPhysicalPropertyIndex(pp);
		if (i!=-1) return i; 
		
		IndexCard<PhysicalProperty> ppic = new IndexCard<PhysicalProperty>(pp);
		masterlist.add(ppic);
		
		i = masterlist.indexOf(ppic);
		pps.add(i);
		return i;
	}
	
	public int addReferencePhysicalEntity(ReferencePhysicalEntity rpe) {
		int i = this.getIndexofReferencePhysicalEntity(rpe);
		if (i!=-1) return i; 
		
		IndexCard<ReferencePhysicalEntity> ic = new IndexCard<ReferencePhysicalEntity>(rpe);
		masterlist.add(ic);
		
		i = masterlist.indexOf(ic);
		rpes.add(i);
		return i;
	}
	
	public int addCustomPhysicalEntity(CustomPhysicalEntity cupe) {
		int i = getIndexofCustomPhysicalEntity(cupe);
		if (i!=-1) return i; 
		
		IndexCard<CustomPhysicalEntity> ic = new IndexCard<CustomPhysicalEntity>(cupe);
		masterlist.add(ic);
		
		i = masterlist.indexOf(ic);
		cupes.add(i);
		return i;
	}
	
	public int addCompositePhysicalEntity(CompositePhysicalEntity cpe) {
		int i = getIndexofCompositePhysicalEntity(cpe);
		if (i!=-1) return i; 
		
		IndexCard<CompositePhysicalEntity> ic = new IndexCard<CompositePhysicalEntity>(cpe);
		masterlist.add(ic);
		
		i = masterlist.indexOf(ic);
		cpes.add(i);
		return i;
	}
	
	public int createCompositePhysicalEntity(ArrayList<Integer> peindicies) {
		//Avoid creating a composite with a null in the entity list
		if (peindicies.contains(-1)) return -1;
		ArrayList<PhysicalEntity> pes = new ArrayList<PhysicalEntity>();
		ArrayList<StructuralRelation> rels = new ArrayList<StructuralRelation>();
		for (Integer i : peindicies) {
			if (i==-1) pes.add(null); 
			pes.add((PhysicalEntity)masterlist.get(i).getObject());
			rels.add(SemSimConstants.PART_OF_RELATION);
		}
		if (!rels.isEmpty()) {
			rels.remove(0);
		}
		
		return addCompositePhysicalEntity(new CompositePhysicalEntity(pes, rels));
	}
	
	public int addPhysicalProcess(PhysicalProcess proc) {
		int i = getPhysicalProcessIndex(proc);
		if (i!=-1) return i; 
		
		IndexCard<PhysicalProcess> ic = new IndexCard<PhysicalProcess>(proc);
		masterlist.add(ic);
		
		i = masterlist.indexOf(ic);
		procs.add(i);
		return i;
	}
	
	public Triple<ArrayList<Integer>, ArrayList<Integer>, ArrayList<Integer>> getProcessParticipants(Integer index) {
		PhysicalProcess process = (PhysicalProcess) masterlist.get(index).getObject();
		
		ArrayList<Integer> sources = new ArrayList<Integer>();
		
		for (PhysicalEntity entity : process.getSourcePhysicalEntities()) {
			 sources.add(getComponentIndex(entity));
		}
				 
		ArrayList<Integer> sinks = new ArrayList<Integer>();
		
		for (PhysicalEntity entity : process.getSourcePhysicalEntities()) {
			 sources.add(getComponentIndex(entity));
		}
		
		ArrayList<Integer> mediators = new ArrayList<Integer>();
		
		for (PhysicalEntity entity : process.getSourcePhysicalEntities()) {
			 sources.add(getComponentIndex(entity));
		}
				
		Triple<ArrayList<Integer>, ArrayList<Integer>, ArrayList<Integer>> participants =
			 Triple.of(sources, sinks, mediators);
		
		return participants;
	}
	
	public PhysicalPropertyinComposite getAssociatePhysicalProperty(Integer index) {
		return (PhysicalPropertyinComposite)masterlist.get(index).getObject();
	}

	public Integer getPhysicalPropertyIndex(PhysicalPropertyinComposite pp) {
		for (Integer i : cpepps) {
			if (masterlist.get(i).isTermEquivalent(pp)) return i; 
		}
		return -1;
	}
	
	public Integer getPhysicalPropertyIndex(PhysicalProperty pp) {
		for (Integer i : pps) {
			if (masterlist.get(i).isTermEquivalent(pp)) return i; 
		}
		return -1;
	}
	
	public ArrayList<Integer> getSortedAssociatePhysicalPropertyIndicies() {
		return sortComponentIndiciesbyName(cpepps);
	}
	
	public ArrayList<Integer> getSortedPhysicalPropertyIndicies() {
		return sortComponentIndiciesbyName(pps);
	}

	public ReferencePhysicalEntity getReferencePhysicalEntity(Integer index) {
		return (ReferencePhysicalEntity)masterlist.get(index).getObject();
	}
	
	public ArrayList<Integer> getSortedReferencePhysicalEntityIndicies() {
		return sortComponentIndiciesbyName(rpes);
	}
	
	public int getIndexofReferencePhysicalEntity(ReferencePhysicalEntity rpe) {
		for (Integer i : rpes) {
			if (masterlist.get(i).isTermEquivalent(rpe)) return i; 
		}
		return -1;
	}

	public CustomPhysicalEntity getCustomPhysicalEntity(Integer index) {
		return (CustomPhysicalEntity)masterlist.get(index).getObject();
	}

	public int getIndexofCustomPhysicalEntity(CustomPhysicalEntity cupe) {
		for (Integer i : cupes) {
			if (masterlist.get(i).isTermEquivalent(cupe)) return i; 
		}
		return -1;
	}
	
	public CustomPhysicalEntity getCompositePhysicalEntity(Integer index) {
		return (CustomPhysicalEntity)masterlist.get(index).getObject();
	}

	public int getIndexofCompositePhysicalEntity(CompositePhysicalEntity cpe) {
		for (Integer i : cpes) {
			if (masterlist.get(i).isTermEquivalent(cpe)) return i; 
		}
		return -1;
	}
	
	public PhysicalProcess getPhysicalProcess(Integer index) {
		return (PhysicalProcess)masterlist.get(index).getObject();
	}
		
	public Integer getPhysicalProcessIndex(PhysicalProcess process) {
		for (Integer i : procs) {
			if (masterlist.get(i).isTermEquivalent(process)) return i; 
		}
		return -1;
	}
	
	public ArrayList<Integer> getSortedPhysicalProcessIndicies() {
		return sortComponentIndiciesbyName(procs);
	}	
	
	public ArrayList<Integer> getSortedSingularPhysicalEntityIndicies() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.addAll(cupes);
		list.addAll(rpes);
		
		return sortComponentIndiciesbyName(list);
	}
	
	public ArrayList<Integer> getSortedCompositePhysicalEntityIndicies() {
		return sortComponentIndiciesbyName(cpes);
	}
	
	public void removePhysicalProperty(Integer index) {
		cpepps.remove(index);
	}

	public void removeReferencePhysicalEntity(Integer index) {
		rpes.remove(index);
	}

	public void removeCustomPhysicalEntities(Integer index) {
		cupes.remove(index);
	}

	public void removeCompositePhysicalEntities(Integer index) {
		cpes.remove(index);
	}

	public void removePhysicalProcesses(Integer index) {
		procs.remove(index);
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

	public Integer getComponentIndex(PhysicalModelComponent pmc) {
		for (IndexCard<?> card : masterlist) {
			if (card.isTermEquivalent(pmc)) return masterlist.indexOf(card);
		}
		return -1;
	}
	
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
		return masterlist.get(index).getDescription();
	}
	
	public PhysicalModelComponent getComponent(Integer index) {
		return masterlist.get(index).getObject();
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
	
	private ArrayList<Integer> sortComponentIndiciesbyName(ArrayList<Integer> indicies) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for (Integer i : indicies) {
			map.put(masterlist.get(i).getName(), i);
		}
		ArrayList<String> names = new ArrayList<String>(map.keySet());
		names.sort(new CaseInsensitiveComparator());
		
		ArrayList<Integer> sortedlist = new ArrayList<Integer>();
		
		for (String s : names) {
			sortedlist.add(map.get(s));
		}
		return sortedlist;
	}
	
	public boolean isReferenceTerm(Integer index) {
		return masterlist.get(index).isReferenceTerm();
	}
	
	public URI getReferenceComponentURI(Integer index) {
		return masterlist.get(index).getReferenceURI();
	}
	
	public ReferenceTerm getReferenceTermbyURI(URI uri) {
		for (Integer i : getAllReferenceTerms()) {
			ReferenceTerm term = (ReferenceTerm) masterlist.get(i);
			if (term.getReferstoURI().equals(uri)) return term;
		}
		return null;
	}
	
	public ArrayList<Integer> getIndiciesofReferenceRelations(int indexcard, SemSimRelation rel) {
		ArrayList<Integer> indicies = new ArrayList<Integer>();
		if (indexcard!=-1) {
			ArrayList<URI> uris = masterlist.get(indexcard).getAnnotationObjectsbyRelation(rel);
			
			for (URI uri : uris) {
				ReferenceTerm term = getReferenceTermbyURI(uri);
				indicies.add(masterlist.indexOf((PhysicalModelComponent)term));
			}
		}
		return indicies;
	}
	
	public void setName(int index, String name) {
		masterlist.get(index).getObject().setName(name);
	}
	
	public void setDescription(int index, String description) {
		masterlist.get(index).getObject().setName(description);
	}
	
	protected class IndexCard<T extends PhysicalModelComponent> {
		private T component;
		private Boolean reference;
		
		public IndexCard(T comp) {
			component = comp;
			reference = component.hasRefersToAnnotation();
		}
		
		public String getDescription() {
			return component.getDescription();
		}

		public T getObject() {
			return component;
		}
		
		public boolean isReferenceTerm() {
			return reference;
		}
		
		public String getName() {
			if (reference) return ((ReferenceTerm)component).getNamewithOntologyAbreviation();
			return component.getName();
		}
		
		public URI getReferenceURI() {
			if (isReferenceTerm()) {
				return ((ReferenceTerm)component).getReferstoURI();
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
		
		public Boolean isTermEquivalent(PhysicalModelComponent term) {
			return component.equals(term);
		}
	}
	
	public ReferenceOntology getLastOntology() {
		return lastont;
	}
	
	public void setLastOntology(ReferenceOntology ont) {
		lastont = ont;
	}
}
