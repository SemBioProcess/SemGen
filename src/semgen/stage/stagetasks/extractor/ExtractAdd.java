package semgen.stage.stagetasks.extractor;

import java.util.HashSet;
import java.util.Set;

import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;

public class ExtractAdd extends Extractor {
	protected Set<DataStructure> dsstoadd = new HashSet<DataStructure>();
	protected Set<Submodel> smstoadd = new HashSet<Submodel>();
	
	public ExtractAdd(SemSimModel source) {
		super(source.clone());
	}

	public void addDataStructure(DataStructure dstoimport) {
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
