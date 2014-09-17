package semgen.annotation.workbench;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import semgen.annotation.dialog.referencedialog.SingularAnnotationEditor;
import semgen.annotation.dialog.selectordialog.SemSimComponentSelectorDialog;
import semsim.SemSimConstants;
import semsim.SemSimUtil;
import semsim.model.SemSimComponent;
import semsim.model.SemSimModel;
import semsim.model.annotation.ReferenceOntologyAnnotation;
import semsim.model.computational.DataStructure;
import semsim.model.physical.PhysicalProperty;

public class CodewordAnnotations extends AnnotatorObservable {
	LinkedHashSet<Integer> entset = new LinkedHashSet<Integer>();
	LinkedHashSet<Integer> procset = new LinkedHashSet<Integer>();
	LinkedHashSet<Integer> depset = new LinkedHashSet<Integer>();	
	
	public CodewordAnnotations(SemSimModel ssm) {
		super(ssm);
	}
	
	public void generateList() {
		ComponentList.clear();
		Set<DataStructure> dsset = semsimmodel.getDataStructures();
		ArrayList<String> names = new ArrayList<String>(semsimmodel.getDataStructureNames());
		alphabetizeandCreateList(names, dsset);
		classifyDataStructures();
	}
	
	public Integer getFocusIndex() {
		return cindex;
	}

	public ReferenceOntologyAnnotation getSingular(int index) {
		return ((DataStructure)ComponentList.get(index)).getFirstRefersToReferenceOntologyAnnotation();
	}
	
	@Override
	public boolean hasSingular(int index) {
		 return !((DataStructure)ComponentList.get(index)).hasRefersToAnnotation();
	}
	
	public boolean hasComposite(int index) {
		 PhysicalProperty pp = ((DataStructure)ComponentList.get(index)).getPhysicalProperty();
		 if ((pp.getPhysicalPropertyOf()!=null)) {
			 return true;
		 }
		 return false;
	}
	
	public void notifyObservers() {
		setChanged();
		notifyObservers(CODEWORD);
	}

	@Override
	public Boolean isVisible() {
		return !((DataStructure)ComponentList.get(cindex)).isImportedViaSubmodel();
	}
	
	public Boolean isVisible(int index) {
		return !((DataStructure)ComponentList.get(index)).isImportedViaSubmodel();
	}
	
	public void classifyDataStructures() {
		entset.clear();
		depset.clear();
		procset.clear();
		for (SemSimComponent ds : ComponentList) {
			int type = ((DataStructure)ds).getPropertyType();
			Integer index = ComponentList.indexOf(ds);
			
			switch (type) {
			case SemSimConstants.PROPERTY_OF_PHYSICAL_ENTITY:
				entset.add(index);
				break;
			case SemSimConstants.PROPERTY_OF_PHYSICAL_PROCESS:
				procset.add(index);
				break;
			default:
				depset.add(index);
				break;
			}
		}
	}
	
	public ArrayList<Integer> getTypeSortedList() {
		ArrayList<Integer> list = new ArrayList<Integer>();

		list.addAll(entset);
		list.addAll(procset);
		list.addAll(depset);
		return list;
	}

	@Override
	public void setSingular(int index) {
		DataStructure ds = getDataStructure(index);
		ReferenceOntologyAnnotation roa = getSingular(index);
		SingularAnnotationEditor sae = new SingularAnnotationEditor(roa);
		if (!sae.getAnnotation().matches(roa)) {
			ds.removeAllReferenceAnnotations();
			ds.addReferenceOntologyAnnotation(sae.getAnnotation());
		}
		notifyObservers();
	}
	
	private DataStructure getDataStructure(Integer index) {
		return ((DataStructure)ComponentList.get(index));
	}
	
	public int countDataStructures() {
		return semsimmodel.getDataStructures().size();
	}
	
	protected ArrayList<DataStructure> getDataStructuresfromIndicies(ArrayList<Integer> indicies) {
		ArrayList<DataStructure> dss = new ArrayList<DataStructure>();
		for (Integer index : indicies) {
			dss.add((DataStructure) ComponentList.get(index));
		}
		return dss;
	}
	
	protected ArrayList<Integer> getListofIndicies(ArrayList<DataStructure> dss) {
		ArrayList<Integer> indicies = new ArrayList<Integer>();
		for (DataStructure ds : dss) {
			indicies.add(ComponentList.indexOf(ds));
		}
		return indicies;
	}
	
	public ArrayList<DataStructure> codewordSelectionDialog(String title, ArrayList<DataStructure> structstoCheck) {
		SemSimComponentSelectorDialog dss = new SemSimComponentSelectorDialog(
				SemSimUtil.getNamesfromComponentSet(ComponentList, false), 
				getListofIndicies(structstoCheck), null, title);
		if (dss.isChanged()) {
			return getDataStructuresfromIndicies(dss.getUserSelections());
		}
		return null;
	}
}
