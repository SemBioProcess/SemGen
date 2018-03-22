package unitTests.semsim;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.semanticweb.owlapi.model.OWLException;

import JSim.util.Xcept;
import semsim.fileaccessors.FileAccessorFactory;
import semsim.fileaccessors.ModelAccessor;
import semsim.reading.CellMLreader;
import semsim.reading.MMLtoXMMLconverter;
import semsim.reading.XMMLreader;
import semsim.utilities.ErrorLog;
import unitTests.unitTestBase.CollateralHelper;
import unitTests.unitTestBase.UnitTestBase;

public class ReadingAndWritingModFilesTests extends UnitTestBase {
	
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
		ModelAccessor validModFile = CollateralHelper.GetCollateral(CollateralHelper.Files.LouRudy_Mod);
		
		
				
		
		
		// Act
		semsim.model.collection.SemSimModel model;
		
		try {
			String srcText = validModFile.getModelasString();
			Document xmmldoc = MMLtoXMMLconverter.convert(srcText, validModFile.getFileName());
			
			if (ErrorLog.hasErrors()) fail();
			XMMLreader xmmlreader = new XMMLreader(validModFile, xmmldoc, srcText.toString());
			model = xmmlreader.readFromDocument();
		
			ModelAccessor newModelFile = FileAccessorFactory.getModelAccessor(createTempFile());
			newModelFile.writetoFile(model);
			
			//assert
			assertTrue(!newModelFile.getModelasString().isEmpty());
		} catch (IOException | Xcept | InterruptedException | OWLException e) {
			fail();
		}
	}
	
	
	private File createTempFile() {
		try {
			return _tempFolder.newFile("modtemp.mod");
		}
		catch(Exception e) {
			fail();
		}
		
		return null;
	}
	
}
