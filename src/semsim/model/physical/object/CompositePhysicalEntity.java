package semsim.model.physical.object;

import java.net.URI;
import java.util.ArrayList;

import semsim.SemSimConstants;
import semsim.annotation.StructuralRelation;
import semsim.model.physical.PhysicalEntity;

public class CompositePhysicalEntity extends PhysicalEntity implements Comparable<CompositePhysicalEntity>{
	
	private ArrayList<PhysicalEntity> arrayListOfPhysicalEntities;
	private ArrayList<StructuralRelation> arrayListOfStructuralRelations;

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
		setName(makeName());
		setDescription(getName());
	}
	
	public String makeName(){
		String name = null;
		if(getArrayListOfEntities().size()>0) name = "";
		for(int x=0; x<getArrayListOfEntities().size(); x++){
			PhysicalEntity ent = getArrayListOfEntities().get(x);
			if(ent.hasRefersToAnnotation())
				name = name + ent.getFirstRefersToReferenceOntologyAnnotation().getValueDescription();
			else
				name = name + "\"" + ent.getName() + "\"";
			
			if(x<getArrayListOfEntities().size()-1){
				name = name + " in ";
			}
		}
		return name;
	}

	public void setArrayListOfEntities(ArrayList<PhysicalEntity> arrayListOfEntities) {
		this.arrayListOfPhysicalEntities = arrayListOfEntities;
	}

	public ArrayList<PhysicalEntity> getArrayListOfEntities() {
		return arrayListOfPhysicalEntities;
	}

	public void setArrayListOfStructuralRelations(
			ArrayList<StructuralRelation> arrayListOfStructuralRelations) {
		this.arrayListOfStructuralRelations = arrayListOfStructuralRelations;
	}

	public ArrayList<StructuralRelation> getArrayListOfStructuralRelations() {
		return arrayListOfStructuralRelations;
	}

	
	public int compareTo(CompositePhysicalEntity that) {
		if(this.arrayListOfPhysicalEntities.size()==that.arrayListOfPhysicalEntities.size() &&
				this.arrayListOfStructuralRelations.size()==that.arrayListOfStructuralRelations.size()){
			// Test physical entity equivalence
			for(int i=0;i<getArrayListOfEntities().size(); i++){
				if(this.getArrayListOfEntities().get(i)!=that.getArrayListOfEntities().get(i)){
					return 1;
				}
			}
			// Test structural relation equivalence
			for(int i=0; i<getArrayListOfStructuralRelations().size(); i++){
				if(this.getArrayListOfStructuralRelations().get(i)!=that.getArrayListOfStructuralRelations().get(i)){
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
}
