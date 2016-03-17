package semsim.reading;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import semgen.UnitTestBase;
import semsim.CollateralHelper;
import semsim.annotation.CurationalMetadata.Metadata;
import semsim.model.collection.SemSimModel;

public class CellMLReaderUnitTests extends UnitTestBase {
	
	@Test
	public void readFromFile_readValidFile_VerifyNonNullModelReturned() {
		// Arrange
		File validCellMLFile = CollateralHelper.GetCollateral(CollateralHelper.Files.AlbrechtColegroveFriel2002_CellML);
		CellMLreader reader = new CellMLreader(validCellMLFile);
		
		// Act
		SemSimModel model = reader.read();
		
		// Assert
		assertNotNull("Verify the model is not null", model);
	}
	
	@Test
	public void readFromFile_readValidFile_VerifyAnnotation() {
		// Arrange
		File validCellMLFile = CollateralHelper.GetCollateral(CollateralHelper.Files.AlbrechtColegroveFriel2002_CellML);
		CellMLreader reader = new CellMLreader(validCellMLFile);
		
		// Act
		SemSimModel model = reader.read();
		
		// Assert
		
		// Look for the pubmed id
		String pubmedid = model.getCurationalMetadata().getAnnotationValue(Metadata.pubmedid);
		assertTrue("Verify the expected annotation is present", pubmedid.equals("11865019"));
	}
}
