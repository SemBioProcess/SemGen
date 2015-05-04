package semgen.annotation.workbench.drawers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import semgen.SemGen;
import semgen.annotation.workbench.AnnotatorWorkbench.WBEvent;
import semgen.annotation.workbench.AnnotatorWorkbench.modeledit;
import semsim.PropertyType;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.object.PhysicalProperty;
import semsim.utilities.SemSimComponentComparator;

public class CodewordToolDrawer extends AnnotatorDrawer<DataStructure> {
	public enum CodewordCompletion {
		noAnnotations("_"), hasPhysProp("P+_"), hasPhysEnt("_+X"), hasAll("P+X");
		private String code;
		
		private CodewordCompletion(String txt) {
			code = txt;
		}
		
		public String getCode() {
			return code;
		}
	}
	
	public CodewordToolDrawer(Set<DataStructure> dslist) {
		componentlist.addAll(dslist);
		
		Collections.sort(componentlist, new SemSimComponentComparator());
	}
	
	public ArrayList<Integer> getCodewordstoDisplay(Boolean[] options){
		ArrayList<Integer> cws = new ArrayList<Integer>();
		
		int i = 0;
		for (DataStructure ds : componentlist) {
			if (ds.isImportedViaSubmodel() && options[0]) continue;
			cws.add(i);
			i++;
		}
		
		if(options[1]) setCodewordsbyAnnCompleteness(cws);
		if(options[2]) setCodewordsbyMarker(cws);
		return cws;
	}
	
	private void setCodewordsbyMarker(ArrayList<Integer> displaylist){
		ArrayList<Integer> entset = new ArrayList<Integer>();
		ArrayList<Integer> procset = new ArrayList<Integer>();
		ArrayList<Integer> depset = new ArrayList<Integer>();

		for (Integer index : displaylist) {
			PropertyType type = getPropertyType(index);
			
			// Group according to physical property type
			if(type  == PropertyType.PropertyOfPhysicalEntity)
				entset.add(index);
			else if(type == PropertyType.PropertyOfPhysicalProcess)
				procset.add(index);
			else depset.add(index);
		}
		
		displaylist.clear();
		displaylist.addAll(entset);
		displaylist.addAll(procset);
		displaylist.addAll(depset);
	}
	
	public PropertyType getPropertyType(int index) {
		return componentlist.get(index).getPropertyType(SemGen.semsimlib);
	}
	
	private void setCodewordsbyAnnCompleteness(ArrayList<Integer> displaylist) {
		ArrayList<Integer> nonelist = new ArrayList<Integer>();
		ArrayList<Integer> physproplist = new ArrayList<Integer>();
		ArrayList<Integer> physentlist = new ArrayList<Integer>();
		ArrayList<Integer> alllist = new ArrayList<Integer>();
		
		for (Integer index : displaylist) {
			switch (getAnnotationStatus(index)) {
			case hasPhysProp:
				physproplist.add(index);
				break;
			case hasPhysEnt:
				physentlist.add(index);
				break;
			case hasAll:
				alllist.add(index);
				break;
			default:
				nonelist.add(index);
				break;
			}
		}
		
		displaylist.clear();
		displaylist.addAll(nonelist);
		displaylist.addAll(physproplist);
		displaylist.addAll(physentlist);
		displaylist.addAll(alllist);
	}
	
	public CodewordCompletion getAnnotationStatus(int index) {
		DataStructure ds = componentlist.get(index);

		if (!ds.hasPhysicalProperty() && !ds.hasAssociatedPhysicalComponent()) {
			return CodewordCompletion.noAnnotations;
		}
		else if (ds.hasPhysicalProperty() && !ds.hasAssociatedPhysicalComponent()) {
			return CodewordCompletion.hasPhysProp;
		}
		else if (!ds.hasPhysicalProperty() && ds.hasAssociatedPhysicalComponent()) {
			return CodewordCompletion.hasPhysEnt;
		}
		return CodewordCompletion.hasAll;
	}
	
	public boolean isEditable(int index) {
		return !componentlist.get(index).isImportedViaSubmodel();
	}
	
	public boolean hasSingularAnnotation(int index) {
		return componentlist.get(index).hasRefersToAnnotation();
	}
	
	@Override
	public URI getSingularAnnotationURI(int index) {
		return componentlist.get(index).getFirstRefersToReferenceOntologyAnnotation().getReferenceURI();
	}

	public String getFocusLookupName() {
		DataStructure ds = componentlist.get(currentfocus);
		return ds.getName().substring(ds.getName().lastIndexOf(".")+1);
	}

	public String getUnits() {
		if (componentlist.get(currentfocus).hasUnits()) {
			return componentlist.get(currentfocus).getUnit().getName();
		}
		return "dimensionless";
	}
	
	public String getEquationasString() {
		if(componentlist.get(currentfocus).getComputation()!=null){
			return componentlist.get(currentfocus).getComputation().getComputationalCode();
		}
		return "";
	}
	
	public boolean isMapped() {
		return componentlist.get(currentfocus).isMapped();
	}
	
	@Override
	public String getSingularAnnotationasString(int index) {
		if (hasSingularAnnotation(index)) {
			return componentlist.get(index).getFirstRefersToReferenceOntologyAnnotation().getNamewithOntologyAbreviation();
		}
		return SemSimModel.unspecifiedName;
	}
	
	@Override
	protected void selectionNotification() {
		notifyObservers(WBEvent.cwselection);
	}

	public boolean isProcess() {
		PhysicalProperty pp = componentlist.get(currentfocus).getPhysicalProperty();
		if (pp == null) return false; 
		return SemGen.semsimlib.isOPBprocessProperty(pp.getFirstRefersToReferenceOntologyAnnotation().getReferenceURI());
	}

	@Override
	public boolean isImported() {
		return componentlist.get(currentfocus).isImportedViaSubmodel();
	}
	
//	public int countEntities() {
//		PhysicalProperty pp = componentlist.get(currentfocus).getPhysicalProperty().getPhysicalPropertyOf();
//	}
}
