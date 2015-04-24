package semgen.annotation.workbench;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import semgen.SemGen;
import semsim.PropertyType;
import semsim.model.computational.datastructures.DataStructure;
import semsim.utilities.SemSimComponentComparator;

public class CodewordToolDrawer extends AnnotatorDrawer {
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
	
	private ArrayList<DataStructure> datastructures = new ArrayList<DataStructure>();
	public CodewordToolDrawer(Set<DataStructure> dslist) {
		datastructures.addAll(dslist);
		
		Collections.sort(datastructures, new SemSimComponentComparator());
	}
	
	public ArrayList<Integer> getCodewordstoDisplay(Boolean[] options){
		ArrayList<Integer> cws = new ArrayList<Integer>();
		
		int i = 0;
		for (DataStructure ds : datastructures) {
			if (ds.isImportedViaSubmodel() && options[0]) continue;
			cws.add(i);
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
			PropertyType type = datastructures.get(index).getPropertyType(SemGen.semsimlib);
			
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
		DataStructure ds = datastructures.get(index);

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
	
	public String getCodewordName(int index) {
		return datastructures.get(index).getName();
	}
	
	public boolean isEditable(int index) {
		return !datastructures.get(index).isImportedViaSubmodel();
	}
	
	public boolean hasSingularAnnotation(int index) {
		return datastructures.get(index).hasRefersToAnnotation();
	}
	
	public boolean hasHumanReadableDef(int index) {
		return datastructures.get(index).getDescription()!="";
	}
	
	public int getFocusIndex() {
		return currentfocus;
	}
	
	public String getFocusLookupName() {
		DataStructure ds = datastructures.get(currentfocus);
		return ds.getName().substring(ds.getName().lastIndexOf(".")+1);
	}

	public Boolean containsComponentwithName(String name){
		for (DataStructure ds : datastructures) {
			if (ds.getName().equals(name)) return true;
		}
		return false;
	}
	
	//Temporary
	public DataStructure getCurrentSelection() {
		return datastructures.get(currentfocus);
	}
	

}
