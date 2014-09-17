package semgen.resource.file;

import java.awt.Dimension;
import java.io.File;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public abstract class SemGenFileChooser extends JFileChooser {
	private static final long serialVersionUID = 1L;
	
	protected static final FileNameExtensionFilter owlfilter = new FileNameExtensionFilter("SemSim (*.owl)", "owl");
	protected static final FileNameExtensionFilter cellmlfilter = new FileNameExtensionFilter("CellML (*.cellml, .xml)", "cellml", "xml");
	protected static final FileNameExtensionFilter sbmlfilter = new FileNameExtensionFilter("SBML (*.sbml, .xml)", "sbml", "xml");
	protected static final FileNameExtensionFilter mmlfilter = new FileNameExtensionFilter("MML (*.mod)", "mod");
	protected static final FileNameExtensionFilter csvfilter = new FileNameExtensionFilter("CSV (*.csv)", "csv");
	protected FileFilter fileextensions = new FileFilter(new String[]{"owl", "xml", "sbml", "cellml", "mod"});
	
	private static HashMap<String, FileNameExtensionFilter> filtermap = new HashMap<String, FileNameExtensionFilter>(); 
	
	Dimension filechooserdims = new Dimension(550,550);
	public static File currentdirectory = new File("");
	int modeltype = -1;
	
	public SemGenFileChooser(String title) {
		super(currentdirectory);
		setDialogTitle(title);
		createMap();
	}
	
	private void createMap() {
		filtermap.put("owl", owlfilter);
		filtermap.put("cellml", cellmlfilter);
		filtermap.put("sbml", sbmlfilter);
		filtermap.put("mml", mmlfilter);
		filtermap.put("csv", csvfilter);				
	}
	
	protected FileNameExtensionFilter getFilter(String key) {
		return filtermap.get(key);
	}
	
	protected FileNameExtensionFilter[] getFilter(String[] keys) {
		FileNameExtensionFilter[] filters = new FileNameExtensionFilter[keys.length];
		int i = 0;
		for (String key : keys) {
			filters[i] = filtermap.get(key);
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
	
	public int getFileType() {
		return modeltype;
	}
}
