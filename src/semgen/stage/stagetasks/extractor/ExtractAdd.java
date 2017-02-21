package semgen.stage.stagetasks.extractor;

import java.util.HashSet;
import java.util.Set;

import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;

public class ExtractAdd extends Extractor {
	protected Set<DataStructure> dsstoadd = new HashSet<DataStructure>();
	protected Set<Submodel> smstoadd = new HashSet<Submodel>();
	
	public ExtractAdd(SemSimModel source) {
		super(source.clone());
	}

	public void addDataStructure(DataStructure dstoimport) {
		if (dstoimport instanceof MappableVariable) {
			if (((MappableVariable)dstoimport).getMappedFrom()!=null) {
				dstoimport = ((MappableVariable)dstoimport).getMappedFrom();
			}
		}
		dsstoadd.add(dstoimport);
		for (DataStructure dstocheck : sourcemodel.getAssociatedDataStructures()) {
			if (dstocheck.equals(dstoimport)) {	
				sourcemodel.replaceDataStructure(dstocheck, dstoimport);
				break;
			}
		}
	}
	

	@Override
	public void addSubmodel(Submodel sourceobj) {
		addSubmodel(sourceobj);
	}
	
	private void collectStructures() {
		for (DataStructure ds : sourcemodel.getAssociatedDataStructures()) {
			this.includeDependency(ds);
		}
		for (Submodel sm : sourcemodel.getSubmodels()) {
			this.includeSubModel(sm);
		}
	}
	
	@Override
	public void addEntity(PhysicalEntity pe) {
		for (DataStructure dstoadd : gatherDatastructureswithPhysicalComponent(pe)) {
			includeDependency(dstoadd);
		}
	}
	
	

	@Override
	public void addProcess(PhysicalProcess proc) {
		for (DataStructure dstoadd : gatherDatastructureswithPhysicalComponent(proc)) {
			includeDependency(dstoadd);
		}

		for (PhysicalEntity participant : proc.getParticipants()) {
			addEntity(participant);
		}
	}
	
	@Override
	public SemSimModel run() {
		collectStructures();
		collectDataStructureInputs();
		replaceComputations();
		replaceSubmodelDataStructures();
		replaceSubmodels();
		buildExtraction();
		
		return null;
	}


}
