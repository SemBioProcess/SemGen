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

import semsim.fileaccessors.FileAccessorFactory;
import semsim.fileaccessors.ModelAccessor;
import semsim.fileaccessors.OMEXAccessor;
import semsim.reading.CellMLreader;
import semsim.reading.ModelClassifier.ModelType;
import unitTests.unitTestBase.CollateralHelper;
import unitTests.unitTestBase.UnitTestBase;

public class ReadingAndWritingCellMLFilesTests extends UnitTestBase {
	
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
		ModelAccessor validCellMLFile = CollateralHelper.GetCollateral(CollateralHelper.Files.AlbrechtColegroveFriel2002_CellML);
		CellMLreader reader = new CellMLreader(validCellMLFile);
		
		// Act
		semsim.model.collection.SemSimModel model;
		
		try {
			model = reader.read();
		
			ModelAccessor newModelFile = FileAccessorFactory.getModelAccessor(createTempFile());
			newModelFile.writetoFile(model);
			
			//assert
			assertTrue(!newModelFile.getModelAsString().isEmpty());
		} catch (IOException | JDOMException e) {
			fail();
		}
	}
	
	@Test
	public void readFromArchive_readThenCreateValidFileinArchive_VerifyFileNotEmpty() {
		ModelAccessor validCellMLFile = CollateralHelper.GetCollateral(CollateralHelper.Files.CellML_OMEX_Example);
		CellMLreader reader = new CellMLreader(validCellMLFile);
		// Act
		semsim.model.collection.SemSimModel model;
		
		try {
			model = reader.read();
		
			ModelAccessor newModelFile = FileAccessorFactory.getModelAccessor(createTempFile());
			newModelFile.writetoFile(model);
			
			//assert
			assertTrue(!newModelFile.getModelAsString().isEmpty());
			
			OMEXAccessor newOMEXArchive =  FileAccessorFactory.getOMEXArchive(new File("cellmlomextemp.omex"), new File("model/cellmltemp.cellml"), ModelType.CELLML_MODEL);
			//assert
			newOMEXArchive.writetoFile(model);
			String text = newOMEXArchive.getModelAsString();
			assertTrue(!text.isEmpty());
		} catch (IOException | JDOMException e) {
			e.printStackTrace();
			fail();
			
			_tempFolder.delete();
		}
		_tempFolder.delete();
	}
	
	private File createTempFile() {
		try {
			return _tempFolder.newFile("cellmltemp.cellml");
		}
		catch(Exception e) {
			fail();
		}
		
		return null;
	}
	
}
