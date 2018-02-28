package unitTests.semsim.reading;

import static org.junit.Assert.*;

import java.io.IOException;

import org.jdom.JDOMException;
import org.junit.Test;

import semsim.annotation.CurationalMetadata.Metadata;
import semsim.fileaccessors.ModelAccessor;
import semsim.model.collection.SemSimModel;
import semsim.reading.CellMLreader;
import unitTests.semgen.UnitTestBase;
import unitTests.semsim.CollateralHelper;

public class CellMLReaderUnitTests extends UnitTestBase {
	
	@Test
	public void readFromFile_readValidFile_VerifyNonNullModelReturned() {
		// Arrange
		ModelAccessor validCellMLFile = CollateralHelper.GetCollateral(CollateralHelper.Files.AlbrechtColegroveFriel2002_CellML);
		CellMLreader reader = new CellMLreader(validCellMLFile);
		
		// Act
		SemSimModel model;
		try {
			model = reader.read();
			// Assert
			assertNotNull("Verify the model is not null", model);
		} catch (IOException | JDOMException e) {
			e.printStackTrace();
		}
		
		
	}
	
	@Test
	public void readFromFile_readValidFile_VerifyAnnotation() {
		// Arrange
		ModelAccessor validCellMLFile = CollateralHelper.GetCollateral(CollateralHelper.Files.AlbrechtColegroveFriel2002_CellML);
		CellMLreader reader = new CellMLreader(validCellMLFile);
		
		// Act
		SemSimModel model;
		try {
			model = reader.read();	
			// Look for the pubmed id
			String pubmedid = model.getCurationalMetadata().getAnnotationValue(Metadata.pubmedid);
			assertTrue("Verify the expected annotation is present", pubmedid.equals("11865019"));
		} catch (IOException | JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Assert
		

	}
}
