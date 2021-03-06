package semgen.utilities.file;

import java.io.File;

public class FileFilters extends javax.swing.filechooser.FileFilter {
	public String[] filetypes;

	public FileFilters(String[] filetypes) {
		this.filetypes = filetypes;
	}

	public boolean accept(File afile) {
		Boolean match = false;
		if (afile.isDirectory()) {
			return true;
		}
		String path = afile.getAbsolutePath().toLowerCase();
		String extension = path.substring(path.lastIndexOf(".") + 1,
				path.length()).toLowerCase();
		if (extension != null) {
			for (int x = 0; x < filetypes.length; x++) {
				if (filetypes[x].equals(extension)) {
					match = true;
				}
			}
		}
		return match;
	}

	public String getDescription() {
		String desc = "";
		for (int x = 0; x < filetypes.length; x++) {
			desc = desc + "*." + filetypes[x] + " ";
		}
		return desc.trim();
	}

}
