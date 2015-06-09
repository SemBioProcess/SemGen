package semsim.model.physical.object;

import java.net.URI;
import java.util.ArrayList;

import semsim.SemSimConstants;
import semsim.annotation.StructuralRelation;
import semsim.model.physical.PhysicalEntity;

public class CompositePhysicalEntity extends PhysicalEntity implements Comparable<CompositePhysicalEntity>{
	
	private ArrayList<PhysicalEntity> arrayListOfPhysicalEntities = new ArrayList<PhysicalEntity>();
	private ArrayList<StructuralRelation> arrayListOfStructuralRelations = new ArrayList<StructuralRelation>();

	public CompositePhysicalEntity(ArrayList<PhysicalEntity> ents, ArrayList<StructuralRelation> rels){
		if(ents.size()-1 != rels.size()){
			System.err.println("Error constructing composite physical entity: " +
					"length of relations array (" + rels.size() + 
					") must be one less than entity array (" + ents.size() + ").");
		}
		else{
			setArrayListOfEntities(ents);
			setArrayListOfStructuralRelations(rels);
		}
	}
	/** Copy constructor **/
	public CompositePhysicalEntity(CompositePhysicalEntity cpetocopy) {
		this.setArrayListOfEntities(cpetocopy.arrayListOfPhysicalEntities);
		setArrayListOfStructuralRelations(cpetocopy.arrayListOfStructuralRelations);
	}
	
	@Override
	public String getName(){
		return makeName();
	}
	
	@Override
	public String getDescription(){
		return makeName();
	}
	
	public String makeName(){
		String name = null;
		if(getArrayListOfEntities().size()>0) name = "";
		for(int x=0; x<getArrayListOfEntities().size(); x++){
			PhysicalEntity ent = getArrayListOfEntities().get(x);
			if(ent.hasRefersToAnnotation())
				name = name + ent.getName();
			else
				name = name + "\"" + ent.getName() + "\"";
			
			if(x<getArrayListOfEntities().size()-1){
				name = name + " in ";
			}
		}
		return name;
	}

	public void setArrayListOfEntities(ArrayList<PhysicalEntity> arrayListOfEntities) {
		arrayListOfPhysicalEntities.clear();
		for (PhysicalEntity pe : arrayListOfEntities) {
			arrayListOfPhysicalEntities.add(pe);
		}
	}

	public ArrayList<PhysicalEntity> getArrayListOfEntities() {
		return arrayListOfPhysicalEntities;
	}

	public void addPhysicalEntity(PhysicalEntity pe) {
		arrayListOfPhysicalEntities.add(pe);
	}
	
	public void replacePhysicalEntity(PhysicalEntity tobereplaced, PhysicalEntity replacer) {
		if (!arrayListOfPhysicalEntities.contains(tobereplaced)) return;
		arrayListOfPhysicalEntities.set(arrayListOfPhysicalEntities.indexOf(tobereplaced), replacer);
	}
	
	public void replacePhysicalEntity(int index, PhysicalEntity pe) {
		if (index == arrayListOfPhysicalEntities.size()) {
			addPhysicalEntity(pe);
		}
		arrayListOfPhysicalEntities.set(index, pe);
	}
	
	public void removePhysicalEntity(PhysicalEntity pe) {
		arrayListOfPhysicalEntities.remove(pe);
	}
	
	public void removePhysicalEntity(int index) {
		arrayListOfPhysicalEntities.remove(index);
	}
	
	public void setArrayListOfStructuralRelations(ArrayList<StructuralRelation> arrayListOfStructuralRelations) {
		this.arrayListOfStructuralRelations.clear();
		for (StructuralRelation sr : arrayListOfStructuralRelations) {
			this.arrayListOfStructuralRelations.add(sr);
		}
	}

	public ArrayList<StructuralRelation> getArrayListOfStructuralRelations() {
		return arrayListOfStructuralRelations;
	}
	
	public int compareTo(CompositePhysicalEntity that) {
		if(arrayListOfPhysicalEntities.size()==that.arrayListOfPhysicalEntities.size() &&
				arrayListOfStructuralRelations.size()==that.arrayListOfStructuralRelations.size()){
			// Test first physical entity equivalence
			if(getArrayListOfEntities().get(0)!=that.getArrayListOfEntities().get(0)){
				return 1;
			}
			// Test remaining entities and structural relation equivalence
			for(int i=1;i<getArrayListOfEntities().size(); i++){
				if((getArrayListOfEntities().get(i)!=that.getArrayListOfEntities().get(i)) ||
				(getArrayListOfStructuralRelations().get(i-1)!=that.getArrayListOfStructuralRelations().get(i-1))) {
					return 1;
				}
			}
			return 0;
		}
		// Else the arrays were different sizes
		return 1;
	}

	@Override
	public URI getSemSimClassURI() {
		return SemSimConstants.COMPOSITE_PHYSICAL_ENTITY_CLASS_URI;
	}
	@Override
	protected boolean isEquivalent(Object obj) {
		return compareTo((CompositePhysicalEntity)obj)==0;
	}
}
