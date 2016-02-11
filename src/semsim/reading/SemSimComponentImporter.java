package semsim.reading;

import java.io.File;

import org.semanticweb.owlapi.model.OWLException;

import semsim.SemSimLibrary;
import semsim.model.collection.FunctionalSubmodel;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;

public class SemSimComponentImporter {
/**
 * This class provides methods for importing submodels 
 * and units from other models. Motivated by need to support
 * CellML 1.1 models.
 * @throws CloneNotSupportedException 
 */

	public static FunctionalSubmodel importFunctionalSubmodel(File receivingmodelfile, SemSimModel receivingmodel,
			String localcompname, String origcompname, String hrefValue, SemSimLibrary sslib){
		
		String supplyingmodelfilepath = getPathToSupplyingModel(receivingmodelfile, receivingmodel, hrefValue);
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
		SemSimModel importedmodel = null;
		int modeltype = ModelClassifier.classify(importedmodelfile);
		if(modeltype == ModelClassifier.SEMSIM_MODEL){
			try {
				importedmodel = new SemSimOWLreader(importedmodelfile).read();
			} catch (OWLException e) {
				e.printStackTrace();
			}
		}
		else if(modeltype == ModelClassifier.CELLML_MODEL){
			try {
				importedmodel = new CellMLreader(importedmodelfile).read();
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
	
	
	private static FunctionalSubmodel addImportedComponentToModel(SemSimModel model, String localsemsimname, String localsourcemodelname, String hrefValue, FunctionalSubmodel origsubmodel, FunctionalSubmodel parent) throws CloneNotSupportedException{
		FunctionalSubmodel importedcompclone = new FunctionalSubmodel(origsubmodel);
		importedcompclone.setName(localsemsimname);
		importedcompclone.setLocalName(localsourcemodelname);
		importedcompclone.setImported(true);
		importedcompclone.setReferencedName(origsubmodel.getName());
		importedcompclone.setHrefValue(hrefValue);
		if(parent!=null) importedcompclone.setParentImport(parent);
		
		model.addSubmodel(importedcompclone);
		System.out.println("Added imported component " + importedcompclone.getName() + " to " + model.getName());

		for(DataStructure ds : importedcompclone.getAssociatedDataStructures()){
			// Rename the data structure using a part of the local name of the imported component
			String localdsname = localsemsimname + "." + ds.getName().substring(ds.getName().lastIndexOf(".")+1);
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
	
	private static String getPathToSupplyingModel(File receivingmodelfile, SemSimModel receivingmodel, String hrefValue){
		String supplyingmodelfilepath = null;
		if(hrefValue.startsWith("http://")){
			String error = "ERROR: Cannot import components from...\n\n\t" + hrefValue + "\n\n...because it is not a local file.";
			System.err.println(error);
			receivingmodel.addError(error);
			return null;
		}
		else if(hrefValue.startsWith("/") || hrefValue.startsWith("\\"))
			hrefValue = hrefValue.substring(1);
		
		supplyingmodelfilepath = receivingmodelfile.getParent() + "/" + hrefValue;
		return supplyingmodelfilepath;
	}
}
