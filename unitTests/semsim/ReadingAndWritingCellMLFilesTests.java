package semsim;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.*;
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
import java.util.ArrayList;
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
	public void readCleanFile_writeNewFileUsingTheCellMLWriter_VerifyFileNotEmpty() {
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
	 public void readCleanFile_writeNewFileUsingTheCellMLWriter_VerifyFileContents() throws Exception {
		readFileAndWriteNewFileThenCompareDifferences(CollateralHelper.Files.AlbrechtColegroveFriel2002_CellML_Clean);
	}

	@Test
	public void readOriginalPanditFile_writeNewFileUsingTheCellMLWriter_VerifyFileContents() throws Exception {
		readFileAndWriteNewFileThenCompareDifferences(CollateralHelper.Files.Pandit_Clark_Giles_2001_Endocardial_Cell);
	}

	private void readFileAndWriteNewFileThenCompareDifferences(String fileName) {
		// Arrange
		File validCellMLFile = CollateralHelper.GetCollateral(fileName);
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

			Document controlDocument, testDocument;
			try {
				DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
				controlDocument = docBuilder.parse(validCellMLFile);
				testDocument = docBuilder.parse(newModelFile);
			} catch( Exception e ) {
				System.out.println("Exception thrown while creating documents: " + e.getMessage());
				fail();
				return;
			}

			// Create a diff object and only compare nodes if their name and attributes match
			Diff myDiff = new Diff(controlDocument, testDocument);
			myDiff.overrideElementQualifier(new ElementNameAndAttributeQualifier());

			// Are the documents similar?
			SemGenModelFileDifferenceListener semGenDifferenceListener = new SemGenModelFileDifferenceListener(myDiff);
			boolean similar = semGenDifferenceListener.similar();

			// What are the differences, if any?
			List<Difference> differences = semGenDifferenceListener.getSimilarDifferences();
			System.out.println("Differences: " + differences);

			// Finally, assert that there aren't any differences
			Assert.assertTrue("Num differences should be 0", similar);
		}
	}

	private File createTempFile() {
		try {
			File tempFile = _tempFolder.newFile("temp.xml");
			System.out.println("Test file: " + tempFile.toString());

			return tempFile;
		}
		catch(Exception e) {
			fail();
		}
		
		return null;
	}

	private class SemGenModelFileDifferenceListener extends DetailedDiff {

		private List<Difference> _similarDifferences = new ArrayList<Difference>();

		public SemGenModelFileDifferenceListener(Diff prototype) {
			super(prototype);
		}

		@Override
		public int differenceFound(Difference difference) {
			// Need to use base class method to set isRecoverable properly
			int returnValue = super.differenceFound(difference);

			// If this is a real difference then save it
			if(!difference.isRecoverable())
				_similarDifferences.add(difference);

			return returnValue;
		}

		public List<Difference> getSimilarDifferences() {
			return _similarDifferences;
		}
	}
}
