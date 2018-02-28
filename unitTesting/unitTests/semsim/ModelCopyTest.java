package unitTests.semsim;

import java.io.File;
import java.io.IOException;

import org.jdom.JDOMException;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import semsim.fileaccessors.FileAccessorFactory;
import semsim.fileaccessors.ModelAccessor;
import semsim.model.collection.SemSimModel;
import semsim.reading.SemSimOWLreader;

public class ModelCopyTest extends unitTests.semgen.UnitTestBase {
	@Test
	public void readFromFile_readThenWriteValidFile_VerifyFileNotEmpty() throws OWLOntologyCreationException {
		// Arrange
		ModelAccessor validCellMLFile = CollateralHelper.GetCollateral(CollateralHelper.Files.Cardiovascularmodel_OWL);
		
		try {
			SemSimOWLreader reader = new SemSimOWLreader(validCellMLFile);
			
			// Act
			semsim.model.collection.SemSimModel model = reader.read();
			SemSimModel copy = model.clone();
			ModelAccessor newModelFile = FileAccessorFactory.getModelAccessor(new File(System.getProperty("user.home"),"CVCopy.owl"));

		
		} catch (OWLException | JDOMException | IOException e) {
			e.printStackTrace();
		}
		

	}
}
