package semgen.stage.stagetasks.extractor;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

import semgen.stage.serialization.ExtractionNode;
import semgen.stage.serialization.Node;
import semgen.stage.stagetasks.ModelInfo;
import semsim.model.collection.SemSimModel;

public class ModelExtractionGroup {
	@Expose public Integer sourcemodelindex;
	protected ExtractorWorkbench workbench;
	
	@Expose public ArrayList<ExtractionNode> extractionnodes = new ArrayList<ExtractionNode>();
	
	public ModelExtractionGroup(ModelInfo info) {
		sourcemodelindex = info.modelindex;
		workbench = new ExtractorWorkbench(info.accessor, info.Model);
	}

	public ExtractionNode createExtraction(String extractname, ArrayList<Node<?>> nodestoextract) {
		Extractor extractor = workbench.makeNewExtraction(extractname);
		
		SemSimModel extractedmodel = doExtraction(extractor, nodestoextract);
		ExtractionNode newextract = new ExtractionNode(extractedmodel, extractionnodes.size());
		extractionnodes.add(newextract);
		return newextract;
	}
	
	public ExtractionNode createExtractionExcluding(String extractname, ArrayList<Node<?>> nodestoexclude) {
		Extractor extractor = workbench.makeNewExtractionExclude(extractname);
		
		SemSimModel extractedmodel = doExtraction(extractor, nodestoexclude);
		ExtractionNode extraction = new ExtractionNode(extractedmodel, extractionnodes.size());
		extractionnodes.add(extraction);
		return extraction;
	}
	
	public ExtractionNode addNodestoExtraction(Integer extractionindex, ArrayList<Node<?>> nodestoadd) {
		Extractor extractor = workbench.makeAddExtractor(extractionindex);
		SemSimModel extractedmodel = doExtraction(extractor, nodestoadd);
		ExtractionNode extraction = new ExtractionNode(extractedmodel, extractionindex);
		
		extractionnodes.set(extractionindex, extraction);
		return extraction;
	}
	
	public ExtractionNode removeNodesfromExtraction(Integer extractionindex, ArrayList<Node<?>> nodestoadd) {
		Extractor extractor = workbench.makeRemoveExtractor(extractionindex);
		SemSimModel extractedmodel = doExtraction(extractor, nodestoadd);
		ExtractionNode extraction = new ExtractionNode(extractedmodel, extractionindex);
		
		extractionnodes.set(extractionindex, extraction);
		return extraction;
	}
	
	public void removeExtraction(int indextoremove) {
		ExtractionNode nodetoremove = extractionnodes.set(indextoremove, null);
		workbench.removeExtraction(nodetoremove.getSourceObject());
	}
	
	//Find node by saved hash and verify with id - should be faster than straight id
	public Node<?> getNodebyHash(int nodehash, String nodeid, int extractionindex) {
		Node<?> returnnode = extractionnodes.get(extractionindex).getNodebyHash(nodehash, nodeid);
		if (returnnode!=null) return returnnode; 
		return null;
	}
	
	//Find node by saved hash and verify with id - should be faster than straight id
	public Node<?> getPhysioMapNodebyHash(int nodehash, String nodeid, int extractionindex) {
		Node<?> returnnode = extractionnodes.get(extractionindex).getPhysioMapNodebyHash(nodehash, nodeid);
		if (returnnode!=null) return returnnode; 

		return null;
	}
	
	private SemSimModel doExtraction(Extractor extractor, ArrayList<Node<?>> nodestoextract) {
		for (Node<?> node : nodestoextract) {
			node.collectforExtraction(extractor);
		}
		SemSimModel result = extractor.run();
		return result;
	}
	
	
	
}
