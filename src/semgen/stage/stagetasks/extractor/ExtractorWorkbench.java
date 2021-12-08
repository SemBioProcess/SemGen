package semgen.stage.stagetasks.extractor;

import java.util.ArrayList;
import java.util.Observable;

import semgen.SemGenGUI;
import semgen.SemGenGUI.saveTask;
import semgen.utilities.Workbench;
import semgen.utilities.file.SemGenSaveFileChooser;
import semsim.fileaccessors.ModelAccessor;
import semsim.model.collection.SemSimModel;


public class ExtractorWorkbench extends Workbench {
	private SemSimModel sourcemodel;
	private ArrayList<SemSimModel> extractions= new ArrayList<SemSimModel>();
	private ArrayList<ModelAccessor> modelaccessorlist = new ArrayList<ModelAccessor>();
	public saveTask savetask = null;

	public ExtractorWorkbench(SemSimModel model) {
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
		return new ExtractExclude(sourcemodel, extraction);
		
	}
	
	public Extractor makeAddExtractor(int extractionindex) {
		ExtractAdd adder = new ExtractAdd(sourcemodel, extractions.get(extractionindex));
		extractions.set(extractionindex, adder.getNewExtractionModel());
		return adder;
	}
	
	public Extractor makeRemoveExtractor(int extractionindex) {
		return new ExtractRemove(sourcemodel, extractions.get(extractionindex));
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
	public ModelAccessor saveModel(Integer index) {
		ModelAccessor ma = modelaccessorlist.get(index);
		if (ma == null) ma = saveModelAs(index);
		else {
			ma.writetoFile(extractions.get(index));
		}
		return ma;
	}

	@Override
	public ModelAccessor saveModelAs(Integer index) {
		SemSimModel model = extractions.get(index);
		SemGenSaveFileChooser filec = new SemGenSaveFileChooser(new String[]{"owl", "proj", "cellml", "sbml", "omex"}, "owl");
		ModelAccessor ma = filec.SaveAsAction(model);
		
		if (ma != null) {
			model.setName(ma.getModelName());
			model.setSourceFileLocation(ma);
			savetask = new SemGenGUI.saveTask(ma, model);
			savetask.execute();
		}

		return ma;
	}
	
	@Override
	public void exportModel(Integer index){}

	public SemSimModel getSourceModel() {
		return sourcemodel;
	}
	
	public void removeExtraction(Integer model) {
		extractions.set(model, null);
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
	
	//Get the extraction accessor, if there isn't one, make one.
	public ModelAccessor getAccessorbyIndexAlways(Integer index) {
		if (this.modelaccessorlist.get(index)==null) {
			return saveModelAs(index);
		}
		return this.modelaccessorlist.get(index);
	}
}
