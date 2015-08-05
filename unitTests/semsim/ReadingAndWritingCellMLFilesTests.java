package semsim;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import semgen.UnitTestBase;
import semsim.CollateralHelper;
import semsim.reading.CellMLreader;
import semsim.writing.CellMLwriter;

public class ReadingAndWritingCellMLFilesTests extends UnitTestBase {
	
	@Rule
	private TemporaryFolder _tempFolder = new TemporaryFolder();

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
		File validCellMLFile = CollateralHelper.GetCollateral(CollateralHelper.Files.AlbrechtColegroveFriel2002_CellML);
		CellMLreader reader = new CellMLreader(validCellMLFile);
		
		// Act
		semsim.model.collection.SemSimModel model = reader.readFromFile();
		CellMLwriter writer = new CellMLwriter(model);
		File newModelFile = createTempFile();
		writer.writeToFile(newModelFile);
		
		// Assert
		try {
			assertTrue(!FileUtils.readFileToString(newModelFile, "utf-8").isEmpty());
		} catch (IOException e) {
			fail();
		}
	}
	
	private File createTempFile() {
		try {
			return _tempFolder.newFile();
		}
		catch(Exception e) {
			fail();
		}
		
		return null;
	}
}
