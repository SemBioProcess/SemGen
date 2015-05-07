package semgen.annotation.workbench;

import java.util.ArrayList;
import java.util.Collections;

import semsim.model.SemSimModel;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.ReferencePhysicalProperty;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.utilities.SemSimComponentComparator;

public class SemSimTermLibrary {
	ArrayList<ReferencePhysicalProperty> pps = new ArrayList<ReferencePhysicalProperty>();
	ArrayList<ReferencePhysicalEntity> rpes = new ArrayList<ReferencePhysicalEntity>();
	ArrayList<CustomPhysicalEntity> cupes = new ArrayList<CustomPhysicalEntity>();
	ArrayList<CompositePhysicalEntity> cpes = new ArrayList<CompositePhysicalEntity>();
	ArrayList<PhysicalProcess> procs = new ArrayList<PhysicalProcess>();
	
	public SemSimTermLibrary(SemSimModel model) {
		pps.addAll(model.getPhysicalPropertyClasses());
		Collections.sort(pps, new SemSimComponentComparator());
		rpes.addAll(model.getReferencePhysicalEntities());
		cupes.addAll(model.getCustomPhysicalEntities());
		cpes.addAll(model.getCompositePhysicalEntities());
		procs.addAll(model.getPhysicalProcesses());
	}
	
	public void addPhysicalProperty(ReferencePhysicalProperty pp) {
		pps.add(pp);
	}
	
	public void addReferencePhysicalEntity(ReferencePhysicalEntity rpe) {
		rpes.add(rpe);
	}
	
	public void addCustomPhysicalEntity(CustomPhysicalEntity cupe) {
		cupes.add(cupe);
	}
	
	public void addCompositePhysicalEntity(CompositePhysicalEntity cpe) {
		cpes.add(cpe);
	}
	
	public void addPhysicalProcess(PhysicalProcess proc) {
		procs.add(proc);
	}
	
	public ReferencePhysicalProperty getPhysicalProperty(Integer index) {
		return pps.get(index);
	}

	public Integer getPhysicalPropertyIndex(ReferencePhysicalProperty pp) {
		return pps.indexOf(pp);
	}

	public Integer getPhysicalPropertyIndex(PhysicalProperty pp) {
		if (pp.hasRefersToAnnotation()) {
			for (ReferencePhysicalProperty rpe : pps) {
				if (rpe.getURI().equals(pp.getFirstRefersToReferenceOntologyAnnotation().getReferenceURI())) {
					return pps.indexOf(rpe);
				}
			}
		}
		return -1;
	}

	
	public ArrayList<String> getPhysicalPropertyNames() {
		ArrayList<String> names = new ArrayList<String>();
		for (ReferencePhysicalProperty pp : pps) {
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
