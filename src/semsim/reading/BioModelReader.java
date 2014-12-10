package semsim.reading;

import semsim.SemSimLibrary;

public abstract class BioModelReader {
	protected static SemSimLibrary sslib;
	
	public static void pointtoSemSimLibrary(SemSimLibrary lib) {
		sslib = lib;
	}
}
