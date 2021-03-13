package semgen.annotation.workbench.routines;

import java.io.File;
import java.io.IOException;

import org.jdom.JDOMException;

import semgen.SemGen;
import semgen.utilities.SemGenJob;
import semgen.utilities.file.LoadModelJob;
import semsim.annotation.Annotation;
import semsim.annotation.AnnotationCopier;
import semsim.annotation.Person;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.SemSimTermLibrary;
import semsim.fileaccessors.FileAccessorFactory;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;

public class AnnotationImporter extends SemGenJob {
	SemSimModel importedmodel; // The model providing the data
	SemSimModel importingmodel; // The model receiving the data
	SemSimTermLibrary library;
	
	private Boolean[] options;
	private File sourcefile;
	
	public AnnotationImporter(SemSimTermLibrary lib, SemSimModel currentmodel, File file, Boolean[] opts) {
		sourcefile = file;
		library = lib;
		importingmodel = currentmodel;
		options = opts;
	}
	
	@Override
	public void run() {
		try {
			loadSourceModel();
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
		copyModelAnnotations();
	}
	
	private void loadSourceModel() throws JDOMException, IOException {
		LoadModelJob loader = new LoadModelJob(FileAccessorFactory.getModelAccessor(sourcefile), this);
		loader.run();
		if (!loader.isValid()) {
			abort();
			return;
		}
		importedmodel = loader.getLoadedModel();
	}
	
	private boolean copyModelAnnotations() {
		library.addTermsinModel(importedmodel);
		boolean changed = false;
		if (options[0]) {
			if (importModelLevelAnnotations()) changed = true;
			changed = true;
		}
		if (options[1]) {
			if (importCodewords()) changed = true;
		}
		
		if (options[2]) {
			if (copySubmodels()) changed = true;
		}
		return changed;
	}
	
	
	private boolean importModelLevelAnnotations() {
		Boolean changed = importedmodel.hasDescription() || 
				importedmodel.getAnnotations().size()>0 || 
				importedmodel.getCreators().size()>0;
		
		// Overwrite description
		if(importedmodel.hasDescription())
			importingmodel.setDescription(importedmodel.getDescription());
		
		
		// Add in annotations (we do not overwrite existing annotations on importingmodel)
		for(Annotation ann : importedmodel.getAnnotations()) {
			
			if(ann instanceof ReferenceOntologyAnnotation) {
				ReferenceOntologyAnnotation refann = (ReferenceOntologyAnnotation)ann;
				importingmodel.addReferenceOntologyAnnotation(refann.getRelation(), 
						refann.getReferenceURI(), refann.getValueDescription(), SemGen.semsimlib);
			}
			else {
				Annotation newann = new Annotation(ann.getRelation(), ann.getValue(), ann.getValueDescription());
				importingmodel.addAnnotation(newann);
			}
		}
		
		// Add the creator info - we do not overwrite existing creator info
		System.out.println("Num creators: " + importedmodel.getCreators().size());
		for(Person creator : importedmodel.getCreators()) {
			Person newcreator = new Person(creator.getName(), creator.getEmail(), creator.getAccountName(), creator.getAccountServiceHomepage());
			importingmodel.addCreator(newcreator);
		}
		
		return changed;
	}
	
	// Copy over all the submodel data
	// Make sure to include change flag functionality
	private boolean copySubmodels() {
		Boolean changemadetosubmodels = false;
		for(Submodel sub : importingmodel.getSubmodels()){
			if(importedmodel.getSubmodel(sub.getName()) !=null){
				changemadetosubmodels = true;
				
				Submodel srcsub = importedmodel.getSubmodel(sub.getName());
				
				// Copy free-text description
				sub.setDescription(srcsub.getDescription());
			}
		}
		return changemadetosubmodels;
	}
	
	private boolean importCodewords() {
		
		boolean changes = false;
			
		for(DataStructure ds : importingmodel.getAssociatedDataStructures()){
				
			if(importedmodel.containsDataStructure(ds.getName())){
				try{
					changes = true;
				
					DataStructure srcds = importedmodel.getAssociatedDataStructure(ds.getName());				
					ds.copyDescription(srcds);
					ds.copySingularAnnotations(srcds, SemGen.semsimlib);
					AnnotationCopier.copyCompositeAnnotation(library, ds, srcds);
				}
				catch(Exception ex){
					ex.printStackTrace();
				}
				
			} // otherwise no matching data structure found in source model
		} // end of data structure loop
		return changes;
	}


}
