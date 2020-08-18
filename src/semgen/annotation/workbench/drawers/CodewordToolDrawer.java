package semgen.annotation.workbench.drawers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import semgen.SemGen;
import semgen.annotation.workbench.AnnotatorWorkbench.WBEvent;
import semgen.annotation.workbench.AnnotatorWorkbench.ModelEdit;
import semsim.annotation.AnnotationCopier;
import semsim.annotation.ReferenceTerm;
import semsim.annotation.SemSimTermLibrary;
import semsim.definitions.PropertyType;
import semsim.definitions.SemSimTypes;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.SBMLFunctionOutput;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.PhysicalPropertyInComposite;
import semsim.utilities.SemSimComponentComparator;
/**
 * Class for accessing and modifying codewords (data structures) in a model 
 * @author Christopher
 *
 */

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
	
	public CodewordToolDrawer(SemSimTermLibrary lib, ArrayList<DataStructure> dslist) {
		super(lib);
		componentlist.addAll(dslist);
		
		Collections.sort(componentlist, new SemSimComponentComparator());
	}
	
	public void reloadCodewords() {
		Collections.sort(componentlist, new SemSimComponentComparator());
		setChanged();
		notifyObservers(ModelEdit.CWLIST_CHANGED);
	}
	
	public ArrayList<DataStructure> getCodewords() {
		return new ArrayList<DataStructure>(componentlist);
	}
	
	public ArrayList<Integer> getCodewordstoDisplay(Boolean[] options){
		ArrayList<Integer> cws = new ArrayList<Integer>();
		
		int i = 0;
		for (DataStructure ds : componentlist) {
			
			if( ! (ds instanceof SBMLFunctionOutput) && ds.isDeclared()){
				if ( ! ds.isImportedViaSubmodel() || options[0]) cws.add(i);
			}
			i++;
		}
		
		if(options[1]) setCodewordsbyAnnCompleteness(cws);
		if(options[2]) setCodewordsbyMarker(cws);
		return cws;
	}
	
	private void setCodewordsbyMarker(ArrayList<Integer> displaylist){
		ArrayList<Integer> entset = new ArrayList<Integer>();
		ArrayList<Integer> procset = new ArrayList<Integer>();
		ArrayList<Integer> forceset = new ArrayList<Integer>();
		ArrayList<Integer> depset = new ArrayList<Integer>();

		for (Integer index : displaylist) {
			PropertyType type = getPropertyType(index);
			
			// Group according to physical property type
			if(type  == PropertyType.PROPERTY_OF_PHYSICAL_ENTITY)
				entset.add(index);
			else if(type == PropertyType.PROPERTY_OF_PHYSICAL_PROCESS)
				procset.add(index);
			else if(type == PropertyType.PROPERTY_OF_PHYSICAL_ENERGY_DIFFERENTIAL)
				forceset.add(index);
			else depset.add(index);
		}
		
		displaylist.clear();
		displaylist.addAll(entset);
		displaylist.addAll(procset);
		displaylist.addAll(forceset);
		displaylist.addAll(depset);
	}
	
	public PropertyType getPropertyType() {
		return getFocus().getPropertyType(SemGen.semsimlib);
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
		return componentlist.get(index).hasPhysicalDefinitionAnnotation();
	}

	public URI getPhysicalPropertyURI() {
		return getFocusAssociatedProperty().getPhysicalDefinitionURI();
	}
	
	public URI getPhysicalComponentURI() {
		return ((ReferenceTerm)getFocusComposite()).getPhysicalDefinitionURI();
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
		if (getFocus().hasUnits()) {
			return getFocus().getUnit().getName();
		}
		return "dimensionless";
	}
	
	public String getEquationasString() {
		if (this.isMapped()) {
			MappableVariable var = (MappableVariable)getFocus();
			if (var.getMappedFrom()!=null) {
				return "(value mapped from " + var.getMappedFrom().getName() + ")";
			}
		}
		if(getFocus().getComputation()!=null){
			return getFocus().getComputation().getComputationalCode();
		}
		return "";
	}
	
	public boolean isMapped() {
		return getFocus().isMapped();
	}
	
	public boolean isProcess() {
		PhysicalPropertyInComposite pp = getFocusAssociatedProperty();
		if (pp == null && ! hasPhysicalModelComponent()) return false; 
		if (pp==null) {
			return getPhysicalCompositeType().equals(SemSimTypes.CUSTOM_PHYSICAL_PROCESS) || 
					getPhysicalCompositeType().equals(SemSimTypes.REFERENCE_PHYSICAL_PROCESS);
		}
		
		return SemGen.semsimlib.isOPBprocessProperty(pp.getPhysicalDefinitionURI());
	}
	
	
	public boolean isEnergyDifferential() {
		PhysicalPropertyInComposite pp = getFocusAssociatedProperty();
		if (pp == null && ! hasPhysicalModelComponent()) return false; 
		if (pp==null) {
			return getPhysicalCompositeType().equals(SemSimTypes.CUSTOM_PHYSICAL_FORCE);
		}
		
		return SemGen.semsimlib.isOPBenergyDiffProperty(pp.getPhysicalDefinitionURI());
	}

	
	public void copyToMappedVariables() {
		MappableVariable thevar = (MappableVariable)getFocus();
		
		Set<MappableVariable> mapped = AnnotationCopier.copyAllAnnotationsToMappedVariables(thevar, SemGen.semsimlib);
		addComponentstoChangeSet(mapped);
		changeNotification();
	}
	
	public void copyToLocallyMappedVariables() {
		if (getFocus()==null) return;
		if (!getFocus().isMapped() || getFocus().isImportedViaSubmodel()) return; 
		
		copyToMappedVariables();
	}

	public Integer getIndexofPhysicalProperty() {
		if (!componentlist.get(currentfocus).hasPhysicalProperty()) return -1;
		return termlib.getPhysicalPropertyIndex(getFocusAssociatedProperty());
	}
	
	private void setDataStructurePhysicalProperty(Integer dsindex, Integer ppindex) {
		DataStructure ds = componentlist.get(dsindex);
		//If the new selection is equivalent to the old, do nothing.
		if (termlib.getPhysicalPropertyIndex(ds.getPhysicalProperty())==ppindex) {
			return;
		}
		if (ppindex!=-1) {
			ds.setAssociatedPhysicalProperty(termlib.getAssociatePhysicalProperty(ppindex));
		}
		else ds.setAssociatedPhysicalProperty(null);
		changeset.add(dsindex);
	}
	
	public void batchSetDataStructurePhysicalProperty(ArrayList<Integer> dsindicies, Integer ppi) {
		for (Integer dsi : dsindicies) {
			setDataStructurePhysicalProperty(dsi, ppi);
		}
		changeNotification(ModelEdit.PROPERTY_CHANGED);
	}
	
	public void setDataStructurePhysicalProperty(Integer index) {
		setDataStructurePhysicalProperty(currentfocus, index);
		changeNotification(ModelEdit.PROPERTY_CHANGED);
	}
	
	private void setDataStructureAssociatedPhysicalComponent(Integer cwindex, Integer compindex) {
		DataStructure ds = componentlist.get(cwindex);
		//If the new selection is equivalent to the old, do nothing
		if ((termlib.getComponentIndex(ds.getAssociatedPhysicalModelComponent(), true)==compindex)) {
			return;
		}
		if (compindex==-1) {
			ds.setAssociatedPhysicalModelComponent(null);
		}
		else {
			ds.setAssociatedPhysicalModelComponent(termlib.getComponent(compindex));
		}
		changeset.add(cwindex);
	}
	
	public void setDataStructureAssociatedPhysicalComponent(Integer index) {
		if (currentfocus == -1) return; 
		setDataStructureAssociatedPhysicalComponent(currentfocus, index);
		
		changeNotification(ModelEdit.COMPOSITE_CHANGED);
	}
	
	public void batchSetAssociatedPhysicalComponent(ArrayList<Integer> cws, int selectedIndex) {
		for (Integer i : cws) {
			setDataStructureAssociatedPhysicalComponent(i, selectedIndex);
		}
		changeNotification();
	}
	
	public int countEntitiesinCompositeEntity() {
		CompositePhysicalEntity cpe = (CompositePhysicalEntity)getFocusComposite();
		return cpe.getArrayListOfEntities().size();
	}
	
	public ArrayList<Integer> getCompositeEntityIndicies() {
		ArrayList<Integer> indexlist = new ArrayList<Integer>();
		if (hasPhysicalModelComponent()) {
			indexlist.addAll(termlib.getCompositeEntityIndicies((CompositePhysicalEntity)getFocusComposite()));
		}
		else {
			indexlist.add(-1);
		}
		return indexlist;
	}
	
	public boolean hasAssociatedPhysicalProperty() {
		return getFocus().hasPhysicalProperty();
	}
	
	public boolean hasPhysicalModelComponent() {
		return getFocus().hasAssociatedPhysicalComponent();
	}
	
	public boolean hasSingularAnnotation() {
		return hasSingularAnnotation(currentfocus);
	}
	
	public Integer getSingularAnnotationLibraryIndex() {
		return getSingularAnnotationLibraryIndex(currentfocus);
	}
	
	public Integer getSingularAnnotationLibraryIndex(int index) {
		return termlib.getPhysicalPropertyIndex(componentlist.get(index).getSingularTerm());
	}
	
	@Override
	protected void selectionNotification() {
		notifyObservers(WBEvent.CWSELECTION);
	}
	
	@Override
	public boolean isImported() {
		return getFocus().isImportedViaSubmodel();
	}
	
	public int getIndexOfAssociatedPhysicalModelComponent() {
		return termlib.getComponentIndex(getFocus().getAssociatedPhysicalModelComponent(), true);
	}
	
	public void batchSetSingularAnnotation(ArrayList<Integer> cws, int selectedIndex) {
		for (Integer i : cws) {
			setSingularAnnotation(i, selectedIndex);
		}
		changeNotification();
	}
	

	public void setSingularAnnotation(int cwindex, int selectedIndex) {
		if (selectedIndex!=-1) {
			componentlist.get(cwindex).setSingularAnnotation((PhysicalProperty)termlib.getComponent(selectedIndex));
		}
		else {
			componentlist.get(cwindex).setSingularAnnotation(null);
		}
		changeset.add(cwindex);
	}
	
	@Override
	public void setHumanReadableDefinition(String newdef, boolean autoann){
		componentlist.get(currentfocus).setDescription(newdef);
		setChanged();
		notifyObservers(ModelEdit.FREE_TEXT_CHANGED);
		if (autoann) copyToLocallyMappedVariables();
	}
	
	public void setSingularAnnotation(int selectedIndex) {
		setSingularAnnotation(currentfocus, selectedIndex);
		changeNotification();
	}
	
	public SemSimTypes getPhysicalCompositeType() {
		if (!hasPhysicalModelComponent()) {
			return null;
		}
		return getFocusComposite().getSemSimType();
	}
		
	private DataStructure getFocus() {
		if (currentfocus==-1) return null;
		return componentlist.get(currentfocus);
	}
	
	private PhysicalModelComponent getFocusComposite() {
		return getFocus().getAssociatedPhysicalModelComponent();
	}
	
	private PhysicalPropertyInComposite getFocusAssociatedProperty() {
		return getFocus().getPhysicalProperty();
	}
	
	public boolean checkPropertyPMCCompatibility(Integer index) {
		if (index == -1) return true;
		boolean isprocess = SemGen.semsimlib.isOPBprocessProperty(termlib.getReferenceComponentURI(index));
		boolean isforce = SemGen.semsimlib.isOPBenergyDiffProperty(termlib.getReferenceComponentURI(index));
		boolean issomethingelse = ! isprocess && ! isforce;
		SemSimTypes type = getPhysicalCompositeType();
		
		if ( ! hasPhysicalModelComponent() || 
				(isprocess && (type.equals(SemSimTypes.CUSTOM_PHYSICAL_PROCESS) || type.equals(SemSimTypes.REFERENCE_PHYSICAL_PROCESS)) || 
				(isforce && type.equals(SemSimTypes.CUSTOM_PHYSICAL_FORCE)) ||
				( issomethingelse && type.equals(SemSimTypes.COMPOSITE_PHYSICAL_ENTITY)))) {
			return true;
		}
		return false;
	}
	
	public HashSet<Integer> getAllAssociatedComposites() {
		HashSet<Integer> associated = new HashSet<Integer>();
		for (DataStructure ds : componentlist) {
			if (ds.hasAssociatedPhysicalComponent()) {
				associated.add(termlib.getComponentIndex(ds.getAssociatedPhysicalModelComponent(), true));
			}
		}
		return associated;
	}
	
	@Override
	protected void changeNotification() {
		setChanged();
		notifyObservers(ModelEdit.CODEWORD_CHANGED);
	}
	
	private void changeNotification(ModelEdit edit) {
		setChanged();
		notifyObservers(edit);
		changeNotification();
	}

}
