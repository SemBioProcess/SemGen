package semsim.model.physical.object;

import java.net.URI;
import java.util.ArrayList;

import semsim.annotation.ReferenceTerm;
import semsim.definitions.SemSimRelations.StructuralRelation;
import semsim.definitions.SemSimTypes;
import semsim.model.collection.SemSimModel;
import semsim.model.physical.PhysicalEntity;
import semsim.owl.SemSimOWLFactory;

/**
 * Class for representing physical entities that are 
 * constructed in a post-coordinated fashion using multiple
 * component physical entities linked by structural relations. 
 * 
 * Background: The existing corpus of available
 * biomedical knowledge resources does not, and perhaps should not,
 * provide reference terms for all physical entities that can 
 * be represented in biosimulation models.
 * 
 * By linking more fundamental physical entity terms that are
 * available in knowledge resources in a post-coordinated fashion,
 * the SemSim architecture provides a way to precisely represent the 
 * physical entities in a wide variety of modeled system, even if
 * reference terms defining such entities are not available in 
 * knowledge resources.
 *  
 * @author mneal
 *
 */
public class CompositePhysicalEntity extends PhysicalEntity implements Comparable<CompositePhysicalEntity>{
	
	private ArrayList<PhysicalEntity> arrayListOfPhysicalEntities = new ArrayList<PhysicalEntity>();
	private ArrayList<StructuralRelation> arrayListOfStructuralRelations = new ArrayList<StructuralRelation>();

	public CompositePhysicalEntity(ArrayList<PhysicalEntity> ents, ArrayList<StructuralRelation> rels){
		super(SemSimTypes.COMPOSITE_PHYSICAL_ENTITY);
		if (ents.contains(null)) {
			System.err.println("Error constructing composite physical entity: " +
					"entity list (" + rels.size() + 
					") contained null entry.");
		}
		else if(ents.size()-1 != rels.size()){
			System.err.println("Error constructing composite physical entity: " +
					"length of relations array (" + rels.size() + 
					") must be one less than entity array (" + ents.size() + ").");
		}
		else{
			setArrayListOfEntities(ents);
			setArrayListOfStructuralRelations(rels);
		}
	}
	
	/**
	 * Copy constructor
	 * @param cpetocopy The CompositePhysicalEntity to copy
	 */
	public CompositePhysicalEntity(CompositePhysicalEntity cpetocopy) {
		super(cpetocopy);
		setArrayListOfEntities(cpetocopy.arrayListOfPhysicalEntities);
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
	
	/** @return A human-readable name for the CompositePhysicalEntity
	 * based on the names of its component entities and structural
	 * relations */
	public String makeName(){
		String name = null;
		if(getArrayListOfEntities().size()>0) name = "";
		for(int x=0; x<getArrayListOfEntities().size(); x++){
			PhysicalEntity ent = getArrayListOfEntities().get(x);
			if(ent.hasPhysicalDefinitionAnnotation())
				name = name + ent.getName();
			else
				name = name + "\"" + ent.getName() + "\"";
			
			if(x<getArrayListOfEntities().size()-1){
				name = name + " in ";
			}
		}
		return name;
	}

	/**
	 * Set the ordered list of physical entities comprising the 
	 * CompositePhysicalEntity
	 * @param arrayListOfEntities An ordered list of {@link PhysicalEntity}s 
	 */
	public void setArrayListOfEntities(ArrayList<PhysicalEntity> arrayListOfEntities) {
		arrayListOfPhysicalEntities.clear();
		for (PhysicalEntity pe : arrayListOfEntities) {
			arrayListOfPhysicalEntities.add(pe);
		}
	}

	/** @return The ordered list of {@link PhysicalEntity}s 
	 * that comprise this CompositePhysicalEntity */
	public ArrayList<PhysicalEntity> getArrayListOfEntities() {
		return arrayListOfPhysicalEntities;
	}

	/**
	 * Append the ordered list of {@link PhysicalEntity}s 
	 * with an additional entity
	 * @param pe The {@link PhysicalEntity} to add
	 */
	public void addPhysicalEntity(PhysicalEntity pe) {
		arrayListOfPhysicalEntities.add(pe);
	}
	
	/**
	 * Replace one of the {@link PhysicalEntity}s that comprises
	 * this CompositePhysicalEntity with another
	 * @param tobereplaced The entity to replace
	 * @param replacer The replacement
	 */
	public void replacePhysicalEntity(PhysicalEntity tobereplaced, PhysicalEntity replacer) {
		if (!arrayListOfPhysicalEntities.contains(tobereplaced)) return;
		arrayListOfPhysicalEntities.set(arrayListOfPhysicalEntities.indexOf(tobereplaced), replacer);
	}
	
	/**
	 * Replace one of the {@link PhysicalEntity}s that comprises
	 * this CompositePhysicalEntity with another. The entity to be replaced
	 * is specified by its position in the ordered list of entities comprising
	 * this CompositePhysicalEntity.
	 * @param index The index of the entity to replace
	 * @param pe The replacement entity
	 */
	public void replacePhysicalEntity(int index, PhysicalEntity pe) {
		if (index == arrayListOfPhysicalEntities.size()) {
			addPhysicalEntity(pe);
		}
		arrayListOfPhysicalEntities.set(index, pe);
	}
	
	/**
	 * Remove a physical entity from the ordered list of
	 * entities that comprise this CompositePhysicalEntity
	 * @param pe The {@link PhysicalEntity} to remove
	 */
	public void removePhysicalEntity(PhysicalEntity pe) {
		arrayListOfPhysicalEntities.remove(pe);
		if (this.arrayListOfStructuralRelations.size() > 0) {
			this.arrayListOfStructuralRelations.remove(0);
		}
		
	}
	
	/**
	 * Remove a physical entity from the ordered list of
	 * entities that comprise this CompositePhysicalEntity
	 * @param index The index position of the {@link PhysicalEntity} to 
	 * remove in the ordered list of entities that make up this
	 * CompositePhysicalEntity.
	 */
	public void removePhysicalEntity(int index) {
		arrayListOfPhysicalEntities.remove(index);
		if (this.arrayListOfStructuralRelations.size() > 0) {
			this.arrayListOfStructuralRelations.remove(0);
		}
	}
	
	/**
	 * Set the ordered list of {@link StructuralRelation}s that
	 * link the component entities in this CompositePhysicalEntity
	 * @param arrayListOfStructuralRelations The ordered list of 
	 * {@link StructuralRelation}s
	 */
	public void setArrayListOfStructuralRelations(ArrayList<StructuralRelation> arrayListOfStructuralRelations) {
		this.arrayListOfStructuralRelations.clear();
		for (StructuralRelation sr : arrayListOfStructuralRelations) {
			this.arrayListOfStructuralRelations.add(sr);
		}
	}

	/** @return The ordered list of {@link StructuralRelation}s that link the
	 * physical entities in this CompositePhysicalEntity
	 */
	public ArrayList<StructuralRelation> getArrayListOfStructuralRelations() {
		return arrayListOfStructuralRelations;
	}
	
	@Override
	public int compareTo(CompositePhysicalEntity that) {
		if(arrayListOfPhysicalEntities.size()==that.arrayListOfPhysicalEntities.size() &&
				arrayListOfStructuralRelations.size()==that.arrayListOfStructuralRelations.size()){
			// Test first physical entity equivalence
			if(!getArrayListOfEntities().get(0).equals(that.getArrayListOfEntities().get(0))){
				return 1;
			}
			// Test remaining entities and structural relation equivalence
			for(int i=1;i<getArrayListOfEntities().size(); i++){
				if(!getArrayListOfEntities().get(i).equals(that.getArrayListOfEntities().get(i))) { 
					return 1;
				}
			}
			return 0;
		}
		// Else the arrays were different sizes
		return 1;
	}
	
	/**
	 * 
	 * @param namespace A URI namespace
	 * @return A URI representing this CompositePhysicalEntity
	 * comprised of an input namespace and a concatenation of the
	 * URI fragments of the annotated component physical entities
	 * (or names, for unannotated entities)
	 */
	public URI makeURI(String namespace) {
		String uristring = namespace;
		for(int y=0; y<getArrayListOfEntities().size();y++){
			PhysicalEntity pe = getArrayListOfEntities().get(y);
			// If a reference physical entity
			if(pe.hasPhysicalDefinitionAnnotation()){
				uristring = uristring + SemSimOWLFactory.getIRIfragment(((ReferenceTerm)pe).getPhysicalDefinitionURI().toString());
			}
			// If custom physical entity
			else{
				uristring = uristring + SemSimOWLFactory.URIencoding(pe.getName());
			}
			// Concatenate with the relation string, if needed
			if(y<getArrayListOfEntities().size()-1){
				uristring = uristring + "_" + 
					SemSimOWLFactory.getIRIfragment(getArrayListOfStructuralRelations().get(y).getURI().toString()) + "_";
			}
		}
		return URI.create(uristring);
	}

	@Override
	protected boolean isEquivalent(Object obj) {
		return compareTo((CompositePhysicalEntity)obj)==0;
	}
	@Override
	public CompositePhysicalEntity addToModel(SemSimModel model) {
		return model.addCompositePhysicalEntity(this);
	}
	@Override
	public void removeFromModel(SemSimModel model) {
		model.removePhysicalEntityFromCache(this);
	}

}
