package semgen.utilities.file;

import java.awt.Dimension;
import java.io.File;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import semgen.SemGen;
import semsim.reading.ModelClassifier.ModelType;


public abstract class SemGenFileChooser extends JFileChooser {
	private static final long serialVersionUID = 1L;
	public static final FileNameExtensionFilter CSV_FILTER = new FileNameExtensionFilter("Comma-separated values (*.csv)", "csv");
	protected FileFilters fileextensions = new FileFilters(new String[]{"owl", "xml", "sbml", "cellml", "mod", "proj", "omex"});
	
	private static HashMap<String, ModelType> filtermap = new HashMap<String, ModelType>(); 
	
	Dimension filechooserdims = new Dimension(550,550);
	public static File currentdirectory;
	public ModelType modeltype = ModelType.UNKNOWN;
	
	public SemGenFileChooser(String title) {
		super(currentdirectory);
		setDialogTitle(title);
		createMap();
		createDialog(SemGen.getSemGenGUI()).setLocationRelativeTo(SemGen.getSemGenGUI());
	}
	
	public SemGenFileChooser(String title, String[] filters) {
		super(currentdirectory);
		setDialogTitle(title);
		addFilters(filters);
		createMap();
		createDialog(SemGen.getSemGenGUI()).setLocationRelativeTo(SemGen.getSemGenGUI());
	}
	
	private void createMap() {
		filtermap.put("owl", ModelType.SEMSIM_MODEL);
		filtermap.put("cellml", ModelType.CELLML_MODEL);
		filtermap.put("sbml", ModelType.SBML_MODEL);
		filtermap.put("mod", ModelType.MML_MODEL);
		filtermap.put("proj", ModelType.MML_MODEL_IN_PROJ);
		filtermap.put("omex", ModelType.OMEX_ARCHIVE);
	}
	
	protected FileFilter getFilter(String key) {
		if(key.equals("csv")) return CSV_FILTER;
		else return filtermap.get(key).getFileFilter();
	}
	
	protected FileFilter[] getFilter(String[] keys) {
		FileFilter[] filters = new FileFilter[keys.length];
		int i = 0;
		for (String key : keys) {
			filters[i] = filtermap.get(key).getFileFilter();
			i++;
		}
		return filters;
	}
	
	public void addFilters(String[] filters) {
		for(String filter : filters) addChoosableFileFilter(getFilter(filter));
	}
	
	public void addFilters(FileNameExtensionFilter[] filters) {
		for(FileNameExtensionFilter filter : filters) addChoosableFileFilter(filter);
	}
	
	public ModelType getFileType() {
		return modeltype;
	}
	
}
