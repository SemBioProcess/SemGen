package semgen.annotation.workbench;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import semgen.SemGen;
import semsim.annotation.ReferenceTerm;
import semsim.model.SemSimModel;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.utilities.ReferenceOntologies.ReferenceOntology;
import semsim.writing.CaseInsensitiveComparator;

public class SemSimTermLibrary {
	private ReferenceOntology lastont;
	ArrayList<Integer> pps = new ArrayList<Integer>();
	ArrayList<Integer> rpes = new ArrayList<Integer>();
	ArrayList<Integer> cupes = new ArrayList<Integer>();
	ArrayList<Integer> cpes = new ArrayList<Integer>();
	ArrayList<Integer> procs = new ArrayList<Integer>();
	
	ArrayList<IndexCard<?>> masterlist = new ArrayList<IndexCard<?>>();
	
	public SemSimTermLibrary(SemSimModel model) {
		for (PhysicalProperty pp : SemGen.semsimlib.getCommonProperties()) {
			addPhysicalProperty(pp);
		}
		addTermsinModel(model);
	}
	
	public void addTermsinModel(SemSimModel model) {
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
	
	public int addPhysicalProcess(PhysicalProcess proc) {
		int i = getPhysicalProcessIndex(proc);
		if (i!=-1) return i; 
		
		IndexCard<PhysicalProcess> ic = new IndexCard<PhysicalProcess>(proc);
		masterlist.add(ic);
		
		i = masterlist.indexOf(ic);
		procs.add(i);
		return i;
	}
	
	public PhysicalProperty getPhysicalProperty(Integer index) {
		return (PhysicalProperty)masterlist.get(index).getObject();
	}

	public Integer getPhysicalPropertyIndex(PhysicalProperty pp) {
		for (Integer i : pps) {
			if (masterlist.get(i).isTermEquivalent(pp)) return i; 
		}
		return -1;
	}
	
	public ArrayList<Integer> getSortedPhysicalPropertyIndicies() {
		return sortComponentIndiciesbyName(pps);
	}

	public ReferencePhysicalEntity getReferencePhysicalEntity(Integer index) {
		return (ReferencePhysicalEntity)masterlist.get(index).getObject();
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
	
	public void removePhysicalProperty(Integer index) {
		pps.remove(index);
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
	
	public PhysicalModelComponent getComponent(Integer index) {
		return masterlist.get(index).getObject();
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
	
	protected class IndexCard<T extends PhysicalModelComponent> {
		private T component;
		private Boolean reference;
		
		public IndexCard(T comp) {
			component = comp;
			reference = component.hasRefersToAnnotation();
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
