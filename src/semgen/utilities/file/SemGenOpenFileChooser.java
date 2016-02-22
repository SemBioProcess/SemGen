package semgen.utilities.file;

import java.io.File;
import java.net.URL;
import java.util.Set;

import javax.swing.JFileChooser;

import semgen.SemGen;
import semsim.reading.ModelAccessor;
import semsim.utilities.SemSimUtil;

public class SemGenOpenFileChooser extends SemGenFileChooser {
	
	private static final long serialVersionUID = -9040553448654731532L;
		
	public SemGenOpenFileChooser(String title, Boolean multi){
		super(title);
		setMultiSelectionEnabled(multi);
		initialize();
		openFile();
	}
	
	public SemGenOpenFileChooser(String title, String[] filters, Boolean multi){
		super(title, filters);
		setMultiSelectionEnabled(multi);
		initialize();
		openFile();
	}
	
	public SemGenOpenFileChooser(Set<ModelAccessor> modelaccessors, String title, String[] filters){
		super(title, filters);
		setMultiSelectionEnabled(true);
		initialize();
		openFile(modelaccessors);
	}
	
	private void initialize(){
		setPreferredSize(filechooserdims);

		addChoosableFileFilter(fileextensions);
		setFileFilter(fileextensions);

	}
		
	private void openFile(Set<ModelAccessor> modelaccessors) {
		if (showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			currentdirectory = getCurrentDirectory();
			modelaccessors.addAll(getSelectedFilesAsModelAccessors());
		}
		else {
			setSelectedFiles(null);
			setSelectedFile(null);
		}
	}
	
	private void openFile() {	
		int choice = showOpenDialog(this);
		if (choice == JFileChooser.APPROVE_OPTION) {
			currentdirectory = getCurrentDirectory();
		}
		else {
			setSelectedFiles(null);
			setSelectedFile(null);	}
	}
	
	public void closeAndWriteStringAsModelContent(URL url, String content){
		cancelSelection();
		String urlstring = url.toString();
		String name = urlstring.substring(urlstring.lastIndexOf("/"));
		
		File tempfile = new File(SemGen.tempdir.getAbsoluteFile() + "/" + name);
		SemSimUtil.writeStringToFile(content, tempfile);
	}
	
}
