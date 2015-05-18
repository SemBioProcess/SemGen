package semgen.annotation.workbench;

import java.util.ArrayList;

import semgen.SemGen;
import semsim.annotation.ReferenceTerm;
import semsim.model.SemSimModel;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.ReferencePhysicalEntity;

public class SemSimTermLibrary {
	ArrayList<PhysicalProperty> pps = new ArrayList<PhysicalProperty>();
	ArrayList<ReferencePhysicalEntity> rpes = new ArrayList<ReferencePhysicalEntity>();
	ArrayList<CustomPhysicalEntity> cupes = new ArrayList<CustomPhysicalEntity>();
	ArrayList<CompositePhysicalEntity> cpes = new ArrayList<CompositePhysicalEntity>();
	ArrayList<PhysicalProcess> procs = new ArrayList<PhysicalProcess>();
	
	ArrayList<IndexCard<?>> masterlist = new ArrayList<IndexCard<?>>();
	
	public SemSimTermLibrary(SemSimModel model) {
		pps.addAll(SemGen.semsimlib.getCommonProperties());
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
	
	public void addPhysicalProperty(PhysicalProperty pp) {
		for (PhysicalProperty p : pps) {
			if (p.equals(pp)) { 
				return;
			}
		}
		pps.add(pp);
		masterlist.add(new IndexCard<PhysicalProperty>(pps.indexOf(pp), pps));
		return;
	}
	
	public void addReferencePhysicalEntity(ReferencePhysicalEntity rpe) {
		for (ReferencePhysicalEntity librpe : rpes) {
			if (librpe.equals(rpe)) { 
				return;
			}
		}
		rpes.add(rpe);
		masterlist.add(new IndexCard<ReferencePhysicalEntity>(rpes.indexOf(rpe), rpes));
		return;
	}
	
	public void addCustomPhysicalEntity(CustomPhysicalEntity cupe) {
		for (CustomPhysicalEntity libcupe : cupes) {
			if (libcupe.equals(cupe)) { 
				return;
			}
		}
		cupes.add(cupe);
		masterlist.add(new IndexCard<CustomPhysicalEntity>(cupes.indexOf(cupe), cupes));
		return;
	}
	
	public void addCompositePhysicalEntity(CompositePhysicalEntity cpe) {
		for (CompositePhysicalEntity libcpe : cpes) {
			if (libcpe.equals(cpe)) { 
				return;
			}
		}
		cpes.add(cpe);
		masterlist.add(new IndexCard<CompositePhysicalEntity>(cpes.indexOf(cpe), cpes));
		return;
	}
	
	public void addPhysicalProcess(PhysicalProcess proc) {
		for (PhysicalProcess libproc : procs) {
			if (libproc.equals(proc)) { 
				return;
			}
		}
		procs.add(proc);
		masterlist.add(new IndexCard<PhysicalProcess>(procs.indexOf(proc), procs));
		return;
	}
	
	public PhysicalProperty getPhysicalProperty(Integer index) {
		return pps.get(index);
	}

	public Integer getPhysicalPropertyIndex(PhysicalProperty pp) {
		return pps.indexOf(pp);
	}
	
	public ArrayList<String> getPhysicalPropertyNames() {
		ArrayList<String> names = new ArrayList<String>();
		for (PhysicalProperty pp : pps) {
			names.add(pp.getName());
		}
		return names;
	}
	
	public ReferencePhysicalEntity getReferencePhysicalEntity(Integer index) {
		return rpes.get(index);
	}

	public CustomPhysicalEntity getCustomPhysicalEntity(Integer index) {
		return cupes.get(index);
	}

	public CompositePhysicalEntity getCompositePhysicalEntity(Integer index) {
		return cpes.get(index);
	}

	public PhysicalProcess getPhysicalProcess(Integer index) {
		return procs.get(index);
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
	
	public ArrayList<Integer> getPhysicalPropertyIndicies() {
		ArrayList<Integer> indicies = new ArrayList<Integer>();
		for (Integer i=0; i < pps.size(); i++) {
			indicies.add(i);
		}
		return indicies;
	}
	
	public ArrayList<String> getPhysicalPropertyNames(ArrayList<Integer> indicies) {
		ArrayList<String> names = new ArrayList<String>();
		for (Integer i : indicies) {
			names.add(pps.get(i).getName());
		}
		return names;
	}
	
	public String getPhysicalPropertyName(Integer index) {
		return pps.get(index).getName();
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
	public Integer getComponentIndex(ReferenceTerm rt) {
		return getComponentIndex((PhysicalModelComponent)rt);
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
	
	public PhysicalModelComponent getComponent(Integer index) {
		return masterlist.get(index).getObject();
	}
	
	protected class IndexCard<T extends PhysicalModelComponent> {
		private Integer pmcindex;
		private ArrayList<T> indexedlist;
		private Boolean reference;
		
		public IndexCard(int index, ArrayList<T> list) {
			pmcindex = index;
			indexedlist = list;
			reference = list.get(index).hasRefersToAnnotation();
		}
		
		public Integer getIndex() {
			return pmcindex;
		}
		
		public T getObject() {
			return indexedlist.get(pmcindex);
		}
		
		public boolean isReferenceTerm() {
			return reference;
		}
		
		public String getName() {
			if (reference) return ((ReferenceTerm)indexedlist.get(pmcindex)).getNamewithOntologyAbreviation();
			return indexedlist.get(pmcindex).getName();
		}
		
		public Boolean isTermEquivalent(PhysicalModelComponent term) {
			return indexedlist.get(pmcindex).equals(term);
		}
	}
}
