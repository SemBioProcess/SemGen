package semgen.stage.stagetasks.extractor;

import java.util.HashSet;
import java.util.Set;

import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;

public class ExtractRemove extends Extractor {
	protected Set<DataStructure> dsstoremove = new HashSet<DataStructure>();
	protected Set<Submodel> smstoremove = new HashSet<Submodel>();	
	
	public ExtractRemove(SemSimModel source, SemSimModel target) {
		super(source);
		
		extraction.setName(target.getName());
		
		collectStructures(target);
	}

	private void collectElementstoKeep() {
		for (DataStructure dstoremove : dsstoremove) {
			datastructures.remove(dstoremove);
		}
		
		for (Submodel smtoremove : smstoremove) {
			submodels.remove(smtoremove);
		}
	}
	
	@Override
	public SemSimModel run() {
		collectElementstoKeep();
		collectDataStructureInputs();
		replaceComputations();
		replaceSubmodelDataStructures();
		replaceSubmodels();
		buildExtraction();
		
		return extraction;
	}

	private void collectStructures(SemSimModel receivermodel) {
		for (Submodel sm : receivermodel.getSubmodels()) {
			includeSubModel(sourcemodel.getSubmodel(sm.getName()), new HashSet<Submodel>());
		}
		for (DataStructure ds : receivermodel.getAssociatedDataStructures()) {
			if (!ds.isExternal()) {
				DataStructure existingds = sourcemodel.getAssociatedDataStructure(ds.getName());
				this.includeDependency(existingds);
			}
		}
	}
	
	@Override
	public void addSubmodel(Submodel sourceobj) {
		sourceobj = sourcemodel.getSubmodel(sourceobj.getName());
		smstoremove.add(sourceobj);
		for (DataStructure dstoemove : sourceobj.getAssociatedDataStructures()) {
			dsstoremove.add(dstoemove);
		}
	}

	@Override
	public void addDataStructure(DataStructure sourceobj) {
		sourceobj = sourcemodel.getAssociatedDataStructure(sourceobj.getName());
			if (sourceobj instanceof MappableVariable) {
				if (((MappableVariable)sourceobj).getMappedFrom()!=null) {
					sourceobj = ((MappableVariable)sourceobj).getMappedFrom();
				}
			}
		dsstoremove.add(sourceobj);
	}

	@Override
	public void addEntity(PhysicalEntity pe) {
		for (DataStructure dstoadd : sourcemodel.gatherDatastructuresWithPhysicalComponent(pe)) {
			dstoadd = sourcemodel.getAssociatedDataStructure(dstoadd.getName());
			addDataStructure(dstoadd);
		}
	}

	@Override
	public void addProcess(PhysicalProcess proc) {
		for (DataStructure dstoadd : sourcemodel.gatherDatastructuresWithPhysicalComponent(proc)) {
			dstoadd = sourcemodel.getAssociatedDataStructure(dstoadd.getName());
			addDataStructure(dstoadd);
		}

		for (PhysicalEntity participant : proc.getParticipants()) {
			addEntity(participant);
		}
	}

}
