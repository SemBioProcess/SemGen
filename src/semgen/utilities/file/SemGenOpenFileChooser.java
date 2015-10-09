package semgen.utilities.file;

import java.io.File;
import java.net.URL;
import java.util.Set;

import javax.swing.JFileChooser;
import semgen.SemGen;
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
	
	public SemGenOpenFileChooser(Set<File> file, String title, String[] filters){
		super(title, filters);
		setMultiSelectionEnabled(true);
		initialize();
		openFile(file);
	}
	
	private void initialize(){
		setPreferredSize(filechooserdims);

		addChoosableFileFilter(fileextensions);
		setFileFilter(fileextensions);

	}
		
	private void openFile(Set<File> files) {
		if (showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			currentdirectory = getCurrentDirectory();
			for (File file : getSelectedFiles()) {
				files.add(file);
			}
		}
		else {
			this.setSelectedFiles(null);
			this.setSelectedFile(null);
		}
	}
	
	private void openFile() {	
		int choice = showOpenDialog(this);
		if (choice == JFileChooser.APPROVE_OPTION) {
			currentdirectory = getCurrentDirectory();
		}
		else {
			this.setSelectedFiles(null);
			this.setSelectedFile(null);	}
	}
	
	public void closeAndWriteStringAsModelContent(URL url, String content){
		cancelSelection();
		String urlstring = url.toString();
		String name = urlstring.substring(urlstring.lastIndexOf("/"));
		
		File tempfile = new File(SemGen.tempdir.getAbsoluteFile() + "/" + name);
		SemSimUtil.writeStringToFile(content, tempfile);
	}
	
}
