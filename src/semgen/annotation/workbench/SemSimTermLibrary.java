package semgen.annotation.workbench;

import java.util.TreeMap;

import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.model.SemSimModel;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.writing.CaseInsensitiveComparator;

public class SemSimTermLibrary {
	SemSimModel model;
	
	public SemSimTermLibrary(SemSimModel mod) {
		model = mod;
	}
	
	public TreeMap<String, ReferencePhysicalEntity> getRefPhysEntIDMap() {
		TreeMap<String, ReferencePhysicalEntity> stringlist = new TreeMap<String, ReferencePhysicalEntity>(new CaseInsensitiveComparator());
		for (ReferencePhysicalEntity rpe : model.getReferencePhysicalEntities()) {
			ReferenceOntologyAnnotation ra = rpe.getFirstRefersToReferenceOntologyAnnotation();
			
			stringlist.put(ra.getNamewithOntologyAbreviation(), rpe);
		}
		return stringlist;
	}
	
	public TreeMap<String, CustomPhysicalEntity> getCustomPhysEntIDMap() {
		TreeMap<String, CustomPhysicalEntity> stringlist = new TreeMap<String, CustomPhysicalEntity>(new CaseInsensitiveComparator());
		for (CustomPhysicalEntity cpe : model.getCustomPhysicalEntities()) {
	
			stringlist.put(cpe.getName(), cpe);
		}
		return stringlist;
	}
	
	public TreeMap<String, PhysicalEntity> getPhysEntIDMap() {
		TreeMap<String, PhysicalEntity> stringlist = new TreeMap<String, PhysicalEntity>(new CaseInsensitiveComparator());
		stringlist.putAll(getCustomPhysEntIDMap());
		stringlist.putAll(getRefPhysEntIDMap());
		return stringlist;
	}
}
