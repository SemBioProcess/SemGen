package semgen;

import java.io.File;

public class FileFilter extends javax.swing.filechooser.FileFilter {
	// public String extension = "";
	// public String uppercase = extension.toUpperCase();
	public String[] filetypes;

	public FileFilter(String[] filetypes) {
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

	/*
	 * 
	 * 
	 * public boolean accept(File afile) { String afilename = afile.getName();
	 * if (afilename.endsWith("." + extension)) return true; if
	 * (afilename.endsWith("." + uppercase)) return true; if
	 * (afile.isDirectory()) return true; return false; }
	 */

	public String getDescription() {
		String desc = "";
		for (int x = 0; x < filetypes.length; x++) {
			desc = desc + "*." + filetypes[x] + " ";
		}
		return desc.trim();
	}

}
