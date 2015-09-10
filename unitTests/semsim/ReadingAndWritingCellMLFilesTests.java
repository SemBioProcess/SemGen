package semsim;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import semgen.UnitTestBase;
import semsim.reading.CellMLreader;
import semsim.writing.CellMLwriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class ReadingAndWritingCellMLFilesTests extends UnitTestBase {
	
	@Rule
	private TemporaryFolder _tempFolder = new TemporaryFolder();

	@BeforeClass
	public static void setupClass() {
		System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
				"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
		System.setProperty("javax.xml.parsers.SAXParserFactory",
				"org.apache.xerces.jaxp.SAXParserFactoryImpl");
		System.setProperty("javax.xml.transform.TransformerFactory",
				"org.apache.xalan.processor.TransformerFactoryImpl");
	}

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
		File validCellMLFile = CollateralHelper.GetCollateral(CollateralHelper.Files.AlbrechtColegroveFriel2002_CellML_Clean);
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
	 public void readFromFile_readThenWriteCleanAndValidFile_VerifyFileContents() throws Exception {
		// Arrange
		File validCellMLFile = CollateralHelper.GetCollateral(CollateralHelper.Files.AlbrechtColegroveFriel2002_CellML_Clean);
		CellMLreader reader = new CellMLreader(validCellMLFile);

		// Act
		semsim.model.collection.SemSimModel model = reader.readFromFile();
		CellMLwriter writer = new CellMLwriter(model);
		File newModelFile = createTempFile();
		writer.writeToFile(newModelFile);

		// Assert
		{
			// Setup XMLUnit
			XMLUnit.setIgnoreAttributeOrder(true);
			XMLUnit.setIgnoreComments(true);
			XMLUnit.setIgnoreWhitespace(true);

			// Parse the files we want to compare
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
			Document controlDocument = docBuilder.parse(validCellMLFile);
			Document testDocument = docBuilder.parse(newModelFile);

			// Create a diff object and only compare nodes if their name and attributes match
			Diff myDiff = new Diff(controlDocument, testDocument);
			myDiff.overrideElementQualifier(new ElementNameAndAttributeQualifier());

			// Get detailed diff information just in case this fails
			DetailedDiff detailedDiff = new DetailedDiff(myDiff);
			List differences = detailedDiff.getAllDifferences();
			System.out.println("Differences: " + differences);

			// Finally, assert that there aren't any differences
			Assert.assertTrue("Num differences should be 0", myDiff.similar());
		}
	}

	@Test
	public void readFromFile_readThenWriteAnOriginalAndValidFile_VerifyFileContents() throws Exception {
		// Arrange
		File validCellMLFile = CollateralHelper.GetCollateral(CollateralHelper.Files.Pandit_Clark_Giles_2001_Endocardial_Cell);
		CellMLreader reader = new CellMLreader(validCellMLFile);

		// Act
		semsim.model.collection.SemSimModel model = reader.readFromFile();
		CellMLwriter writer = new CellMLwriter(model);
		File newModelFile = createTempFile();
		writer.writeToFile(newModelFile);

		// Assert
		{
			// Setup XMLUnit
			XMLUnit.setIgnoreAttributeOrder(true);
			XMLUnit.setIgnoreComments(true);
			XMLUnit.setIgnoreWhitespace(true);

			// Parse the files we want to compare
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
			Document controlDocument = docBuilder.parse(validCellMLFile);
			Document testDocument = docBuilder.parse(newModelFile);

			// Create a diff object and only compare nodes if their name and attributes match
			Diff myDiff = new Diff(controlDocument, testDocument);
			myDiff.overrideElementQualifier(new ElementNameAndAttributeQualifier());

			// Get detailed diff information just in case this fails
			DetailedDiff detailedDiff = new DetailedDiff(myDiff);
			List differences = detailedDiff.getAllDifferences();
			System.out.println("Differences: " + differences);

			// Finally, assert that there aren't any differences
			Assert.assertTrue("Num differences should be 0", myDiff.similar());
		}
	}

	private File createTempFile() {
		try {
			return _tempFolder.newFile("temp.xml");
		}
		catch(Exception e) {
			fail();
		}
		
		return null;
	}
}
