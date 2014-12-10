package semsim.reading;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import semsim.CollateralHelper;
import semsim.SemSimConstants;
import semsim.SemSimLibrary;
import semsim.model.SemSimModel;
import semsim.model.annotation.Annotation;

public class CellMLReaderUnitTests {
	SemSimLibrary sslib;
	
	public CellMLReaderUnitTests(SemSimLibrary lib) {
		sslib = lib;
	}
	@Test
	public void readFromFile_readValidFile_VerifyNonNullModelReturned() {
		// Arrange
		File validCellMLFile = CollateralHelper.GetCollateral(CollateralHelper.Files.AlbrechtColegroveFriel2002_CellML);
		CellMLreader reader = new CellMLreader();
		
		// Act
		SemSimModel model = reader.readFromFile(validCellMLFile);
		
		// Assert
		assertNotNull("Verify the model is not null", model);
	}
	
	@Test
	public void readFromFile_readValidFile_VerifyAnnotation() {
		// Arrange
		File validCellMLFile = CollateralHelper.GetCollateral(CollateralHelper.Files.AlbrechtColegroveFriel2002_CellML);
		CellMLreader reader = new CellMLreader();
		
		// Act
		SemSimModel model = reader.readFromFile(validCellMLFile);
		
		// Assert
		
		// Look for the pubmed id
		Annotation expectedAnnotation = new Annotation(SemSimConstants.REFERENCE_PUBLICATION_PUBMED_ID_RELATION, "11865019");
		assertTrue("Verify the expected annotation is present", model.getAnnotations().contains(expectedAnnotation));
	}
}
