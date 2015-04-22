package semsim.reading;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import semgen.UnitTestBase;
import semsim.CollateralHelper;
import semsim.SemSimConstants;
import semsim.SemSimLibrary;
import semsim.annotation.Annotation;
import semsim.annotation.CurationalMetadata;
import semsim.model.SemSimModel;

public class CellMLReaderUnitTests extends UnitTestBase {
	
	@Test
	public void readFromFile_readValidFile_VerifyNonNullModelReturned() {
		// Arrange
		File validCellMLFile = CollateralHelper.GetCollateral(CollateralHelper.Files.AlbrechtColegroveFriel2002_CellML);
		CellMLreader reader = new CellMLreader(validCellMLFile);
		
		// Act
		SemSimModel model = reader.readFromFile();
		
		// Assert
		assertNotNull("Verify the model is not null", model);
	}
	
	@Test
	public void readFromFile_readValidFile_VerifyAnnotation() {
		// Arrange
		File validCellMLFile = CollateralHelper.GetCollateral(CollateralHelper.Files.AlbrechtColegroveFriel2002_CellML);
		CellMLreader reader = new CellMLreader(validCellMLFile);
		
		// Act
		SemSimModel model = reader.readFromFile();
		
		// Assert
		
		// Look for the pubmed id
		Annotation expectedAnnotation = new Annotation(CurationalMetadata.REFERENCE_PUBLICATION_PUBMED_ID_RELATION, "11865019");
		assertTrue("Verify the expected annotation is present", model.getAnnotations().contains(expectedAnnotation));
	}
}
