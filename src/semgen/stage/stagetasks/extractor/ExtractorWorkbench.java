package semgen.stage.stagetasks.extractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

import semgen.utilities.Workbench;
import semgen.utilities.file.SaveSemSimModel;
import semgen.utilities.file.SemGenSaveFileChooser;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.reading.ModelAccessor;


public class ExtractorWorkbench extends Workbench {
	private SemSimModel sourcemodel;
	private ArrayList<SemSimModel> extractions= new ArrayList<SemSimModel>();
	private BatchSave saver;
	private ArrayList<ModelAccessor> modelaccessorlist = new ArrayList<ModelAccessor>();

	public ExtractorWorkbench(ModelAccessor accessor, SemSimModel model) {
		modelaccessorlist.add(accessor);
		sourcemodel = model;
	}
	
	@Override
	public void initialize() {}
	
	public Extractor makeNewExtraction(String name) {
		SemSimModel extraction = new SemSimModel();
		extraction.setName(name);
		extractions.add(extraction);
		modelaccessorlist.add(null);
		return new ExtractNew(sourcemodel, extraction);
		
	}

	public Extractor makeNewExtractionExclude(String name) {
		SemSimModel extraction = new SemSimModel();
		extraction.setName(name);
		extractions.add(extraction);
		modelaccessorlist.add(null);
		return new ExtractRemove(sourcemodel, extraction);
		
	}
	
	public void saveExtractions(ArrayList<Integer> indicies) {
		saver = new BatchSave(indicies);
		boolean hasnext = false;
		while (!hasnext) {
			saveModel();
			hasnext = saver.next();
		}
		saver = null;
	}
	
	@Override
	public void setModelSaved(boolean val) {}

	@Override
	public String getCurrentModelName() {
		return sourcemodel.getName();
	}

	@Override
	public ModelAccessor getModelSourceLocation() {
		return sourcemodel.getLegacyCodeLocation();
	}
	
	@Override
	public ModelAccessor saveModel() {
		ModelAccessor ma = saver.getModelAccessor();
		if (ma == null) ma = saveModelAs();
		else {
			SaveSemSimModel.writeToFile(saver.getModelOnDeck(), ma, ma.getFileThatContainsModel(), saver.getModelOnDeck().getSourceModelType());
		}
		return ma;
	}

	@Override
	public ModelAccessor saveModelAs() {
		SemSimModel model = saver.getModelOnDeck();
		SemGenSaveFileChooser filec = new SemGenSaveFileChooser(new String[]{"owl", "proj", "cellml", "sbml"}, "owl");
		ModelAccessor ma = filec.SaveAsAction(model);
		
		if (ma != null) {
			model.setName(ma.getModelName());
			
			SaveSemSimModel.writeToFile(model, ma, ma.getFileThatContainsModel(), filec.getFileFilter());

			saver.setModelAccessor(ma);
		}

		return ma;
	}

	public SemSimModel getSourceModel() {
		return sourcemodel;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		
	}
	
	public SemSimModel getExtractedModelbyIndex(Integer index) {
		return this.extractions.get(index);
	}
	
	public ModelAccessor getAccessorbyIndex(Integer index) {
		return this.modelaccessorlist.get(index);
	}
	
	
	private class BatchSave {
		ArrayList<SemSimModel> tobesaved = new ArrayList<SemSimModel>();
		int ondeck = 0;
		
		public BatchSave(ArrayList<Integer> indicies) {
			for (Integer index : indicies) {
				tobesaved.add(extractions.get(index));
			}
		}
		
		public SemSimModel getModelOnDeck() {
			return tobesaved.get(ondeck);
		}
		
		public ModelAccessor getModelAccessor() {
			return modelaccessorlist.get(ondeck+1);
		}
		
		public void setModelAccessor(ModelAccessor newaccessor) {
			modelaccessorlist.set(ondeck, newaccessor);
		}
		
		public boolean next() {
			ondeck++;
			return ondeck == tobesaved.size();
		}
	}
}
