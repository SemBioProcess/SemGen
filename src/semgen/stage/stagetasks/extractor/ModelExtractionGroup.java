package semgen.stage.stagetasks.extractor;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

import semgen.stage.serialization.ExtractionNode;
import semgen.stage.serialization.Node;
import semgen.stage.stagetasks.ModelInfo;
import semsim.fileaccessors.ModelAccessor;
import semsim.model.collection.SemSimModel;

public class ModelExtractionGroup {
	@Expose public Integer sourcemodelindex;
	protected ModelInfo sourcemodelinfo;
	protected ExtractorWorkbench workbench;
	
	@Expose public ArrayList<ExtractionInfo> extractionnodes = new ArrayList<ExtractionInfo>();
	
	public ModelExtractionGroup(ModelInfo info) {
		sourcemodelinfo = info;
		sourcemodelindex = info.modelindex;
		workbench = new ExtractorWorkbench(info.Model);
	}

	public ExtractionNode createExtraction(String extractname, ArrayList<Node<?>> nodestoextract) {
		Extractor extractor = workbench.makeNewExtraction(extractname);
		
		SemSimModel extractedmodel = doExtraction(extractor, nodestoextract);
		
		if (extractedmodel==null) return null;
		
		ExtractionInfo newextract = new ExtractionInfo(sourcemodelinfo.Model, extractedmodel, extractionnodes.size());
		extractionnodes.add(newextract);
		return newextract.modelnode;
	}
	
	public ExtractionNode createExtractionExcluding(String extractname, ArrayList<Node<?>> nodestoexclude) {
		Extractor extractor = workbench.makeNewExtractionExclude(extractname);
		
		SemSimModel extractedmodel = doExtraction(extractor, nodestoexclude);
		
		if (extractedmodel==null) return null;

		ExtractionInfo newextract = new ExtractionInfo(sourcemodelinfo.Model, extractedmodel, extractionnodes.size());
		extractionnodes.add(newextract);
		return newextract.modelnode;
	}
	
	public ExtractionNode addNodestoExtraction(Integer extractionindex, ArrayList<Node<?>> nodestoadd) {
		Extractor extractor = workbench.makeAddExtractor(extractionindex);
		SemSimModel extractedmodel = doExtraction(extractor, nodestoadd);

		extractionnodes.get(extractionindex).Model = extractedmodel;
		ExtractionNode extraction = new ExtractionNode(extractedmodel, extractionindex);

		return extraction;
	}
	
	public ExtractionNode removeNodesfromExtraction(Integer extractionindex, ArrayList<Node<?>> nodestoadd) {
		Extractor extractor = workbench.makeRemoveExtractor(extractionindex);
		extractionnodes.get(extractionindex).setModel(doExtraction(extractor, nodestoadd));
		return extractionnodes.get(extractionindex).modelnode;
	}
	
	public boolean removeExtraction(int indextoremove) {
		extractionnodes.set(indextoremove, null);
		workbench.removeExtraction(indextoremove);
		return isEmpty();
	}
	
	//Find node by saved hash and verify with id - should be faster than straight id
	public Node<?> getNodebyHash(int nodehash, String nodeid, int extractionindex) {
		Node<?> returnnode = extractionnodes.get(extractionindex).modelnode.getNodebyHash(nodehash, nodeid);
		if (returnnode!=null) return returnnode; 
		return null;
	}
	
	//Find node by saved hash and verify with id - should be faster than straight id
	public Node<?> getPhysioMapNodebyHash(int nodehash, String nodeid, int extractionindex) {
		Node<?> returnnode = extractionnodes.get(extractionindex).modelnode.getPhysioMapNodebyHash(nodehash, nodeid);
		if (returnnode!=null) return returnnode; 

		return null;
	}
	
	public ModelAccessor getAccessorbyIndexAlways(Integer tosave) {
		return workbench.getAccessorbyIndexAlways(tosave);	
	}
	
	public ModelAccessor saveExtraction(Integer tosave) {
			ModelAccessor ma = workbench.saveModel(tosave);
			if(ma!=null) extractionnodes.get(tosave).setChangesSaved(true);
			return ma;
	}
	
	public ModelAccessor exportExtraction(Integer tosave) {
		ModelAccessor ma = workbench.saveModelAs(tosave);	
		extractionnodes.get(tosave).setChangesSaved(true);
		return ma;	
	}
	
	private SemSimModel doExtraction(Extractor extractor, ArrayList<Node<?>> nodestoextract) {
		for (Node<?> node : nodestoextract) {
			node.collectforExtraction(extractor);
		}
		//Ensure the model contains data structures to avoid creating an empty model
		
		// Do this so that the ExtractExclude object sets the list of data structures to INclude from the ones specified to exclude
		if( extractor instanceof ExtractExclude) 
			((ExtractExclude) extractor).collectElementstoKeep(); 
		
		if (!extractor.containsDataStructures()) return null;
		
		SemSimModel result = extractor.run();
		return result;
	}
	
	public ExtractionInfo getExtractionInfo(int index) {
		return this.extractionnodes.get(index);
	}
	
	public ExtractionNode getExtractionNode(int index) {
		return this.extractionnodes.get(index).modelnode;
	}
	
	public ArrayList<ExtractionNode> getExtractionArray() {
		ArrayList<ExtractionNode> ens = new ArrayList<ExtractionNode>();
		for (ExtractionInfo ei : this.extractionnodes) {
			ens.add(ei.modelnode);
		}
		return ens;
	}
	
	public boolean isEmpty() {
		if (extractionnodes.isEmpty()) {
			return true;
		}
		for (ExtractionInfo einfo : extractionnodes) {
			if (einfo != null) return false;
		}
		return true;
	}
}
