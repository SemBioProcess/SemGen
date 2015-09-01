package semsim;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import semgen.UnitTestBase;
import semsim.reading.CellMLreader;
import semsim.writing.CellMLwriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


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

	@Test
	 public void readFromFile_readThenWriteValidFile_VerifyFileContents() throws Exception {
		// Arrange
		File validCellMLFile = CollateralHelper.GetCollateral(CollateralHelper.Files.AlbrechtColegroveFriel2002_CellML);
		CellMLreader reader = new CellMLreader(validCellMLFile);

		// Act
		semsim.model.collection.SemSimModel model = reader.readFromFile();
		CellMLwriter writer = new CellMLwriter(model);
		File newModelFile = createTempFile();
		writer.writeToFile(newModelFile);

		// Assert
		System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
				"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
		System.setProperty("javax.xml.parsers.SAXParserFactory",
				"org.apache.xerces.jaxp.SAXParserFactoryImpl");
		System.setProperty("javax.xml.transform.TransformerFactory",
				"org.apache.xalan.processor.TransformerFactoryImpl");
		XMLUnit.setIgnoreAttributeOrder(true);
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreWhitespace(true);

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
		Document controlDocument = docBuilder.parse(validCellMLFile);
		Document testDocument = docBuilder.parse(newModelFile);

		Diff myDiff = new Diff(controlDocument, testDocument);
		DetailedDiff detailedDiff = new DetailedDiff(myDiff);
		List allDifferences = detailedDiff.getAllDifferences();
		System.out.println(allDifferences);
		assertEquals(myDiff.toString(), 2, allDifferences.size());
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
