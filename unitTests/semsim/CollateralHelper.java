package semsim;

import java.io.File;
import java.net.URL;

/**
 * This class helps retrieve files from the collateral directory
 * @author Ryan
 *
 */
public class CollateralHelper {

	// Dummy instance used to retrieve the resource.
	// The instance helps us build a url to the collateral directory, and then the file we need to retrieve
	private static final CollateralHelper DummyCollateralHelper = new CollateralHelper();
	
	// Collateral director name
	private static final String _collateralDirName = "/collateral/";
	
	/**
	 * Get a file by name from the collateral directory.
	 * 
	 * Note: if the file you are retrieving is not a top level file
	 * you'll need to include the folder(s) that contain it in the file name string
	 * (e.g. "parentFolder/filename.cellml"
	 * 
	 * @param fileName - Name of the file
	 * @return File object for file
	 */
	public static File GetCollateral(String fileName) {
		URL url = DummyCollateralHelper.getClass().getResource(_collateralDirName + fileName);
		return new File(url.getFile());
	}
	
	/**
	 * Contains the names of files in the collateral folder
	 * This enables tests to refer to files via variables, instead of string literals
	 * @author Ryan
	 *
	 */
	public class Files
	{
		public static final String AlbrechtColegroveFriel2002_CellML = "albrecht_colegrove_friel_2002.cellml";
		public static final String Cardiovascularmodel_OWL = "CardiovascularDynamics.owl";
	}
}
