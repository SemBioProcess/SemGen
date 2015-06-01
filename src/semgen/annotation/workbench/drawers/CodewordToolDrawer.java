package semgen.annotation.workbench.drawers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import semgen.SemGen;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.annotation.workbench.AnnotatorWorkbench.WBEvent;
import semgen.annotation.workbench.AnnotatorWorkbench.modeledit;
import semgen.annotation.workbench.routines.AnnotationCopier;
import semsim.PropertyType;
import semsim.annotation.ReferenceTerm;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.CustomPhysicalEntity;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.PhysicalPropertyinComposite;
import semsim.model.physical.object.ReferencePhysicalEntity;
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
	
	public CodewordToolDrawer(SemSimTermLibrary lib, Set<DataStructure> dslist) {
		super(lib);
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
	
	public PropertyType getPropertyType() {
		return componentlist.get(currentfocus).getPropertyType(SemGen.semsimlib);
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

		if (ds.hasPhysicalProperty() && ds.hasAssociatedPhysicalComponent()) {
			return CodewordCompletion.hasAll;
		}
		else if (ds.hasPhysicalProperty() && !ds.hasAssociatedPhysicalComponent()) {
			return CodewordCompletion.hasPhysProp;
		}
		else if (!ds.hasPhysicalProperty() && ds.hasAssociatedPhysicalComponent()) {
			return CodewordCompletion.hasPhysEnt;
		}
		return CodewordCompletion.noAnnotations;
	}
	
	public boolean isEditable(int index) {
		return !componentlist.get(index).isImportedViaSubmodel();
	}
	
	public boolean hasSingularAnnotation(int index) {
		return componentlist.get(index).hasRefersToAnnotation();
	}

	public URI getPhysicalPropertyURI() {
		return componentlist.get(currentfocus).getPhysicalProperty().getReferstoURI();
	}
	
	public URI getPhysicalComponentURI() {
		return ((ReferenceTerm)componentlist.get(currentfocus).getAssociatedPhysicalModelComponent()).getReferstoURI();
	}
	
	public String getLookupName(int index) {
		String name = componentlist.get(index).getName();
		if (!name.contains(".")) return name;
		return name.substring(name.lastIndexOf(".")+1);
	}
	
	public String getFocusLookupName() {
		return getLookupName(currentfocus);
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
	
	public boolean isProcess() {
		PhysicalPropertyinComposite pp = componentlist.get(currentfocus).getPhysicalProperty();
		if (pp == null) return false; 
		return SemGen.semsimlib.isOPBprocessProperty(pp.getReferstoURI());
	}

	public void copytoMappedVariables() {
		MappableVariable thevar = (MappableVariable)componentlist.get(currentfocus);
		
		Set<MappableVariable> mapped = AnnotationCopier.copyAllAnnotationsToMappedVariables(thevar);
		addComponentstoChangeSet(mapped);
		changeNotification();
	}

	public Integer getIndexofPhysicalProperty() {
		if (!componentlist.get(currentfocus).hasPhysicalProperty()) return -1;
		return termlib.getPhysicalPropertyIndex(componentlist.get(currentfocus).getPhysicalProperty());
	}
	
	public void setDatastructurePhysicalProperty(Integer index) {
		DataStructure ds = componentlist.get(currentfocus);
		//If the new selection is equivalent to the old, do nothing.
		if (termlib.getPhysicalPropertyIndex(ds.getPhysicalProperty())==index) {
			return;
		}
		if (index!=-1) {
			ds.setAssociatePhysicalProperty(termlib.getAssociatePhysicalProperty(index));
		}
		else ds.setAssociatePhysicalProperty(null);
		
		changeNotification(modeledit.propertychanged);
	}
	
	public void setDataStructureComposite(Integer index) {
		DataStructure ds = componentlist.get(currentfocus);
		//If the new selection is equivalent to the old, do nothing.
		if (termlib.getComponentIndex(ds.getAssociatedPhysicalModelComponent())==index || index==-1) {
			return;
		}
		ds.setAssociatedPhysicalModelComponent(termlib.getComponent(index));

		changeNotification(modeledit.compositechanged);
	}
	
	public int countEntitiesinCompositeEntity() {
		CompositePhysicalEntity cpe = (CompositePhysicalEntity)componentlist.get(currentfocus).getAssociatedPhysicalModelComponent();
		return cpe.getArrayListOfEntities().size();
	}
	
	public ArrayList<Integer> getCompositeEntityIndicies() {
		ArrayList<Integer> indexlist = new ArrayList<Integer>();
		if (hasPhysicalModelComponent()) {
			CompositePhysicalEntity cpe = (CompositePhysicalEntity)componentlist.get(currentfocus).getAssociatedPhysicalModelComponent();
		
			for (PhysicalEntity pe : cpe.getArrayListOfEntities()) {
				int i;
				if (pe.hasRefersToAnnotation()) {
					i = termlib.getIndexofReferencePhysicalEntity((ReferencePhysicalEntity)pe);
				}
				else {
					i = termlib.getIndexofCustomPhysicalEntity((CustomPhysicalEntity)pe);
				}
				indexlist.add(i);
			}
		}
		else {
			indexlist.add(-1);
		}
		return indexlist;
	}
	
	public boolean hasAssociatedPhysicalProperty() {
		return componentlist.get(currentfocus).hasPhysicalProperty();
	}
	
	public boolean hasPhysicalModelComponent() {
		return componentlist.get(currentfocus).hasAssociatedPhysicalComponent();
	}
	
	@Override
	public Integer getSingularAnnotationLibraryIndex(int index) {
		return termlib.getComponentIndex((PhysicalModelComponent)componentlist.get(index).getSingularTerm());
	}
	
	@Override
	protected void selectionNotification() {
		notifyObservers(WBEvent.cwselection);
	}
	
	@Override
	public boolean isImported() {
		return componentlist.get(currentfocus).isImportedViaSubmodel();
	}
	
	public int getIndexofModelComponent() {
		return termlib.getComponentIndex(componentlist.get(currentfocus).getAssociatedPhysicalModelComponent());
	}
	
	@Override
	public void setSingularAnnotation(int selectedIndex) {
		if (selectedIndex!=-1) {
			componentlist.get(currentfocus).setSingularAnnotation((PhysicalProperty)termlib.getComponent(selectedIndex));
		}
		else {
			componentlist.get(currentfocus).setSingularAnnotation(null);
		}
		changeset.add(currentfocus);
		changeNotification();
	}
		
	@Override
	protected void changeNotification() {
		setChanged();
		notifyObservers(modeledit.codewordchanged);
	}
	
	private void changeNotification(modeledit edit) {
		setChanged();
		notifyObservers(edit);
		changeNotification();
	}

}
