package semsim;

import java.io.File;

import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import semgen.UnitTestBase;
import semsim.model.collection.SemSimModel;
import semsim.reading.SemSimOWLreader;
import semsim.writing.SemSimOWLwriter;

public class ModelCopyTest extends UnitTestBase {
	@Test
	public void readFromFile_readThenWriteValidFile_VerifyFileNotEmpty() throws OWLOntologyCreationException {
		// Arrange
		File validCellMLFile = CollateralHelper.GetCollateral(CollateralHelper.Files.Cardiovascularmodel_OWL);
		
		try {
			SemSimOWLreader reader = new SemSimOWLreader(validCellMLFile);
			
			// Act
			semsim.model.collection.SemSimModel model = reader.readFromFile();
			SemSimModel copy = model.clone();
			SemSimOWLwriter writer = new SemSimOWLwriter(copy);
			File newModelFile = new File(System.getProperty("user.home"),"CVCopy.owl");
			writer.writeToFile(newModelFile);
		} catch (OWLException e) {
			e.printStackTrace();
		}
		

	}
}
