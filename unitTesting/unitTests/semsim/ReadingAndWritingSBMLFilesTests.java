package unitTests.semsim;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.jdom.JDOMException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.semanticweb.owlapi.model.OWLException;

import semsim.fileaccessors.FileAccessorFactory;
import semsim.fileaccessors.ModelAccessor;
import semsim.fileaccessors.OMEXAccessor;
import semsim.reading.SBMLreader;
import semsim.reading.ModelClassifier.ModelType;
import unitTests.unitTestBase.CollateralHelper;
import unitTests.unitTestBase.UnitTestBase;

public class ReadingAndWritingSBMLFilesTests extends UnitTestBase {
	
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
		ModelAccessor validCellMLFile = CollateralHelper.GetCollateral(CollateralHelper.Files.BIOMD006_SBML);
		SBMLreader reader = new SBMLreader(validCellMLFile);
		
		// Act
		semsim.model.collection.SemSimModel model;
		
		try {
			model = reader.read();
		
			ModelAccessor newModelFile = FileAccessorFactory.getModelAccessor(createTempFile());
			newModelFile.writetoFile(model);
			
			//assert
			assertTrue(!newModelFile.getModelAsString().isEmpty());
		} catch (IOException | InterruptedException | OWLException | XMLStreamException e) {
			fail();
		} catch (TransformerConfigurationException e) {
			fail();
		} catch (JDOMException e) {
			fail();
		} catch (TransformerException e) {
			fail();
		} catch (TransformerFactoryConfigurationError e) {
			fail();
		}
	}
	
	@Test
	public void readFromArchive_readThenCreateValidFileinArchive_VerifyFileNotEmpty() {
		ModelAccessor validCellMLFile = CollateralHelper.GetCollateral(CollateralHelper.Files.SBML_OMEX_Example);
		SBMLreader reader = new SBMLreader(validCellMLFile);
		// Act
		semsim.model.collection.SemSimModel model;
		
		try {
			model = reader.read();
		
			ModelAccessor newModelFile = FileAccessorFactory.getModelAccessor(createTempFile());
			newModelFile.writetoFile(model);
			
			//assert
			assertTrue(!newModelFile.getModelAsString().isEmpty());
			
			OMEXAccessor newOMEXArchive =  FileAccessorFactory.getOMEXArchive(new File("sbmlomextemp.omex"), new File("model/sbmltemp.sbml"), ModelType.SBML_MODEL);
			//assert
			newOMEXArchive.writetoFile(model);
			String text = newOMEXArchive.getModelAsString();
			assertTrue(!text.isEmpty());
		} catch (IOException | InterruptedException | OWLException | XMLStreamException | JDOMException | TransformerException | TransformerFactoryConfigurationError e) {
			e.printStackTrace();
			fail();
			
			_tempFolder.delete();
		} 
		_tempFolder.delete();
	}
	
	private File createTempFile() {
		try {
			return _tempFolder.newFile("sbmltemp.sbml");
		}
		catch(Exception e) {
			fail();
		}
		
		return null;
	}
	
}
