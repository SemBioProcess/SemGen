package semgen.annotation.workbench;

import java.util.ArrayList;
import java.util.Collections;

import semgen.SemGen;
import semsim.model.SemSimModel;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.utilities.SemSimComponentComparator;

public class SemSimTermLibrary {
	ArrayList<PhysicalProperty> pps = new ArrayList<PhysicalProperty>();
	ArrayList<ReferencePhysicalEntity> rpes = new ArrayList<ReferencePhysicalEntity>();
	ArrayList<CustomPhysicalEntity> cupes = new ArrayList<CustomPhysicalEntity>();
	ArrayList<CompositePhysicalEntity> cpes = new ArrayList<CompositePhysicalEntity>();
	ArrayList<PhysicalProcess> procs = new ArrayList<PhysicalProcess>();
	
	public SemSimTermLibrary(SemSimModel model) {
		pps.addAll(SemGen.semsimlib.getCommonProperties());
		addTermsinModel(model);
	}
	
	public void addTermsinModel(SemSimModel model) {
		for (PhysicalProperty pp : model.getPhysicalProperties()) {
			addPhysicalProperty(pp);
		}
		Collections.sort(pps, new SemSimComponentComparator());
		
		rpes.addAll(model.getReferencePhysicalEntities());
		cupes.addAll(model.getCustomPhysicalEntities());
		cpes.addAll(model.getCompositePhysicalEntities());
		procs.addAll(model.getPhysicalProcesses());
	}
	
	public void addPhysicalProperty(PhysicalProperty pp) {
		for (PhysicalProperty p : pps) {
			if (p.equals(pp)) { 
				return;
			}
		}
		pps.add(pp);
		return;
	}
	
	public void addReferencePhysicalEntity(ReferencePhysicalEntity rpe) {
		for (ReferencePhysicalEntity librpe : rpes) {
			if (librpe.equals(rpe)) { 
				return;
			}
		}
		rpes.add(rpe);
		return;
	}
	
	public void addCustomPhysicalEntity(CustomPhysicalEntity cupe) {
		for (CustomPhysicalEntity libcupe : cupes) {
			if (libcupe.equals(cupe)) { 
				return;
			}
		}
		cupes.add(cupe);
		return;
	}
	
	public void addCompositePhysicalEntity(CompositePhysicalEntity cpe) {
		for (CompositePhysicalEntity libcpe : cpes) {
			if (libcpe.equals(cpe)) { 
				return;
			}
		}
		cpes.add(cpe);
		return;
	}
	
	public void addPhysicalProcess(PhysicalProcess proc) {
		for (PhysicalProcess libproc : procs) {
			if (libproc.equals(proc)) { 
				return;
			}
		}
		procs.add(proc);
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
}
