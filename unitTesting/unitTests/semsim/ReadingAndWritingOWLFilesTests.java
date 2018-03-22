package unitTests.semsim;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.jdom.JDOMException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.semanticweb.owlapi.model.OWLException;

import semsim.fileaccessors.FileAccessorFactory;
import semsim.fileaccessors.ModelAccessor;
import semsim.reading.SemSimOWLreader;
import unitTests.unitTestBase.CollateralHelper;
import unitTests.unitTestBase.UnitTestBase;

public class ReadingAndWritingOWLFilesTests extends UnitTestBase {
	
	@Rule
	public TemporaryFolder _tempFolder = new TemporaryFolder();

	@Before
	public void setup() throws IOException, NoSuchMethodException, SecurityException {
	    _tempFolder.create();
	}
	
	@After
	public void tearDown() throws Exception {
		_tempFolder.delete();
	}
	
	@Test
	public void readFromFile_readThenWriteValidFile_VerifyFileNotEmpty() {
		// Arrange
		ModelAccessor validOWLFile = CollateralHelper.GetCollateral(CollateralHelper.Files.Cardiovascularmodel_OWL);
		SemSimOWLreader reader = new SemSimOWLreader(validOWLFile);
		
		// Act
		semsim.model.collection.SemSimModel model;
		
		try {
			model = reader.read();
		
			ModelAccessor newModelFile = FileAccessorFactory.getModelAccessor(createTempFile());
			newModelFile.writetoFile(model);
			
			//assert
			assertTrue(!newModelFile.getModelasString().isEmpty());
		} catch (IOException | JDOMException | OWLException e) {
			fail();
		}
	}
	
	
	private File createTempFile() {
		try {
			return _tempFolder.newFile("owltemp.owl");
		}
		catch(Exception e) {
			fail();
		}
		
		return null;
	}
	
}
