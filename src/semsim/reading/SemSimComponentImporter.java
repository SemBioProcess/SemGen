package semsim.reading;

import java.io.File;
import java.io.IOException;

import org.jdom.JDOMException;
import org.semanticweb.owlapi.model.OWLException;

import semsim.SemSimLibrary;
import semsim.fileaccessors.FileAccessorFactory;
import semsim.fileaccessors.ModelAccessor;
import semsim.model.collection.FunctionalSubmodel;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.reading.ModelClassifier.ModelType;

/**
 * This class provides methods for importing submodels 
 * and units from other models. Motivated by need to support
 * CellML 1.1 models.
 */
public class SemSimComponentImporter {


	/**
	 * Import a FunctionalSubmodel (AKA a CellML component element) into a SemSimModel
	 * @param supplyingmodelfile Location of the model containing the FunctionalSubmodel to import
	 * @param receivingmodel The SemSimModel to which the FunctionalSubmodel will be added
	 * @param localcompname The name of the imported FunctionalSubmodel to use in the SemSimModel
	 * @param origcompname The original name of the imported FunctionalSubmodel used in the supplying model
	 * @param hrefValue Attribute equal to the Uniform Resource Identifier that identifies the location of the supplying model.
	 * @param sslib A SemSimLibrary instance
	 * @return The FunctionalSubmodel imported into the receiving SemSimModel
	 * @throws JDOMException
	 * @throws IOException
	 */
	protected static FunctionalSubmodel importFunctionalSubmodel(ModelAccessor supplyingmodelfile, SemSimModel receivingmodel,
			String localcompname, String origcompname, String hrefValue, SemSimLibrary sslib) throws JDOMException, IOException{
		
		String supplyingmodelfilepath = getPathToSupplyingModel(supplyingmodelfile, receivingmodel, hrefValue);
		if(supplyingmodelfilepath==null){
			return null;
		}
		
		File importedmodelfile = new File(supplyingmodelfilepath);
		if(!importedmodelfile.exists()){
			String error = "ERROR: Could not import from...\n\n\t" + hrefValue + "\n\n...because file was not found.";
			receivingmodel.addError(error);
			System.err.println(error);
			return null;
		}
		ModelAccessor importedmodelaccessor = FileAccessorFactory.getModelAccessor(importedmodelfile);
		SemSimModel importedmodel = null;
		ModelType modeltype = supplyingmodelfile.getModelType();
		if(modeltype == ModelType.SEMSIM_MODEL){
			try {
				importedmodel = new SemSimOWLreader(importedmodelaccessor).read();
			} catch (OWLException e) {
				e.printStackTrace();
			}
		}
		else if(modeltype == ModelType.CELLML_MODEL){
			try {
				if (!ModelClassifier.isValidCellMLmodel(importedmodelfile)) return null;
				importedmodel = new CellMLreader(importedmodelaccessor).read();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		FunctionalSubmodel sourcecomp = (FunctionalSubmodel) importedmodel.getSubmodel(origcompname);
		FunctionalSubmodel sourcecompclone = null;
		try {
			sourcecompclone = addImportedComponentToModel(receivingmodel, localcompname, localcompname, hrefValue, sourcecomp, null);
			getGroupedComponentsFromImport(receivingmodel, localcompname, sourcecomp);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return sourcecompclone;
	}
	
	
	/**
	 * Import units from a CellML or SemSimModel into another SemSimModel
	 * @param model The SemSimModel to import into
	 * @param localunitname The local name of the unit in the receiving SemSimModel
	 * @param originalname The original name of the unit in the model from which it is imported
	 * @param hrefValue Attribute equal to the Uniform Resource Identifier that identifies the location of 
	 * model supplying the imported unit
	 * @return The UnitOfMeasurement imported into the specified SemSimModel
	 */
	protected static UnitOfMeasurement importUnits(SemSimModel model, String localunitname, String originalname, String hrefValue){
		
		UnitOfMeasurement newunit = null;
		
		// If unit already in model through an import, get it and make sure it's updated with the correct import info
		if(model.containsUnit(localunitname)) newunit = model.getUnit(localunitname);
		else{
			newunit = new UnitOfMeasurement(localunitname);
			model.addUnit(newunit);
		}
		newunit.setLocalName(localunitname);
		newunit.setReferencedName(originalname);
		// Even if we're reusing an existing unit in the model, set this, because the unit may
		// not have this info in it (it may already be in the model by virtue of a sub-model that was imported). If that's the case, 
		// the hrefValue needs to be updated.
		newunit.setHrefValue(hrefValue); 
		newunit.setImported(true);
		newunit.setParentImport(null);
		return newunit;
	}
	
	
	/**
	 * Used internally to clone a FunctionalSubmodel from one model and add the clone to another model
	 * @param model The model that the clone will be added to
	 * @param name Unique name for the FunctionalSubmodel within the SemSimModel
	 * @param localname Unique name for the FunctionalSubmodel within a parent group of FunctionalSubmodels
	 * @param hrefValue Attribute equal to the Uniform Resource Identifier that identifies the location of 
	 * model supplying the imported FunctionalSubmodel
	 * @param origsubmodel The FunctionalSubmodel to clone
	 * @param parent If present, the parent imported FunctionalSubmodel that necessitated importing the origsubmodel
	 * @return The FunctionalSubmodel clone
	 * @throws CloneNotSupportedException
	 */
	private static FunctionalSubmodel addImportedComponentToModel(SemSimModel model, String name, String localname, String hrefValue, FunctionalSubmodel origsubmodel, FunctionalSubmodel parent) throws CloneNotSupportedException{
		FunctionalSubmodel importedcompclone = new FunctionalSubmodel(origsubmodel);
		importedcompclone.setName(name);
		importedcompclone.setLocalName(localname);
		importedcompclone.setImported(true);
		importedcompclone.setReferencedName(origsubmodel.getName());
		importedcompclone.setHrefValue(hrefValue);
		if(parent!=null) importedcompclone.setParentImport(parent);
		
		model.addSubmodel(importedcompclone);
		System.out.println("Added imported component " + importedcompclone.getName() + " to " + model.getName());

		for(DataStructure ds : importedcompclone.getAssociatedDataStructures()){
			// Rename the data structure using a part of the local name of the imported component
			String localdsname = name + "." + ds.getName().substring(ds.getName().lastIndexOf(".")+1);
			ds.setName(localdsname);

			// When parsing SemSim OWL files, all Data Structures are added to model first. They may be imported, so reuse if already present.
			if(!model.containsDataStructure(localdsname)) model.addDataStructure(ds);
			else{
				ds = model.getAssociatedDataStructure(localdsname);
			}
			if(ds.getUnit()!=null){
				// Reuse units, if present
				if(model.containsUnit(ds.getUnit().getName())){
					ds.setUnit(model.getUnit(ds.getUnit().getName()));
				}
				// If not present, add to model
				else{
					model.addUnit(ds.getUnit());
					ds.getUnit().setParentImport(new UnitOfMeasurement("junk"));
					for(UnitFactor uf : ds.getUnit().getUnitFactors()){
						if(!model.containsUnit(uf.getBaseUnit().getName())){
							model.addUnit(uf.getBaseUnit());
							ds.getUnit().setParentImport(new UnitOfMeasurement("junk"));
						}
					}
				}
			}
			ds.setImportedViaSubmodel(true);
		}
		return importedcompclone;
	}
	
	
	/**
	 * Used to iteratively add any FunctionalSubmodels that are part of a group encapsulated by
	 * a parent FunctionalSubmodel
	 * @param model The SemSimModel to add the FunctionalSubmodels to
	 * @param localcompname Local name used for the parent FunctionalSubmodel
	 * @param sourcecomp The parent FunctionalSubmodel
	 * @throws CloneNotSupportedException
	 */
	private static void getGroupedComponentsFromImport(SemSimModel model, String localcompname, FunctionalSubmodel sourcecomp) throws CloneNotSupportedException{
		for(String rel : sourcecomp.getRelationshipSubmodelMap().keySet()){
			for(FunctionalSubmodel fsub : sourcecomp.getRelationshipSubmodelMap().get(rel)){
				String groupedlocalname = localcompname + "." + fsub.getName();
				
				String hrefValue = (fsub.isImported()) ? fsub.getHrefValue() : sourcecomp.getHrefValue();
				addImportedComponentToModel(model, groupedlocalname, fsub.getLocalName(), hrefValue, fsub, sourcecomp);
				
				// Iterate recursively to get groups of groups
				getGroupedComponentsFromImport(model, groupedlocalname, fsub);
			}
		}
	}
	
	/**
	 * Get the file path to a model from which an object is imported
	 * @param modelfile Location of the model referencing an imported object
	 * @param receivingmodel The SemSimModel that will receive the import, if the import can be found
	 * @param hrefValue Attribute equal to the Uniform Resource Identifier that identifies the location of 
	 * model supplying the imported object
	 * @return The file path to the model supplying an imported object
	 */
	private static String getPathToSupplyingModel(ModelAccessor modelfile, SemSimModel receivingmodel, String hrefValue){
		String supplyingmodelfilepath = null;
		if(hrefValue.startsWith("http")){
			String error = "ERROR: Cannot import components from...\n\n\t" + hrefValue + "\n\n...because it is not a local file.";
			System.err.println(error);
			receivingmodel.addError(error);
			return null;
		}
		else if(hrefValue.startsWith("/") || hrefValue.startsWith("\\"))
			hrefValue = hrefValue.substring(1);
		
		supplyingmodelfilepath = modelfile.getFile().getParent() + "/" + hrefValue;
		return supplyingmodelfilepath;
	}
}
