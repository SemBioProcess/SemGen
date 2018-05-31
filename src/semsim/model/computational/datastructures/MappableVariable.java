package semsim.model.computational.datastructures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import semsim.model.collection.SemSimModel;
import semsim.model.computational.Computation;

/**
 * A MappableVariable is a {@link Decimal} that has a specified 
 * input/output designation within the context of a {@link semsim.model.collection.FunctionalSubmodel}.
 * It can be mapped to or from another MappableVariable.  This class
 * was created to correspond to the &lt;variable&gt; element in CellML models.
 * <p>
 * http://www.cellml.org/specifications/cellml_1.1/#sec_model_structure
 */
public class MappableVariable extends Decimal {

	private String publicInterfaceValue = new String("");
	private String privateInterfaceValue = new String("");
	private String CellMLinitialValue = new String("");
	private Set<MappableVariable> mappedTo = new HashSet<MappableVariable>();
	private MappableVariable mappedFrom = null;
	
	public MappableVariable(String name) {
		super(name);
	}
	
	/**
	 * Copy constructor
	 * @param mvtocopy The MappableVariable to copy
	 */
	public MappableVariable(MappableVariable mvtocopy) {
		super(mvtocopy);
		publicInterfaceValue = new String(mvtocopy.publicInterfaceValue);
		privateInterfaceValue = new String(mvtocopy.privateInterfaceValue);
		CellMLinitialValue = new String(mvtocopy.CellMLinitialValue);
		mappedTo.addAll(mvtocopy.mappedTo);
		mappedFrom = mvtocopy.mappedFrom;
	}
	
	/**
	 * Constructor for creating a MappableVariable from a {@link Decimal}
	 * @param dstoconvert A Decimal object
	 */
	public MappableVariable(Decimal dstoconvert) {
		super(dstoconvert);

	}
	
	/** Adds a mapping between this variable and another
	 * @param var The MappableVariable to which this one is mapped */
	public void addVariableMappingTo(MappableVariable var){
		getMappedTo().add(var);
		if(var.getMappedFrom()!=this)
			var.setMappedFrom(this);
	}

	/**
	 * Sets the public interface designation for this variable
	 * @param publicInterfaceValue Valid values are "in", "out", or "none"
	 * in accordance with the CellML 1.1 specification
	 */
	public void setPublicInterfaceValue(String publicInterfaceValue) {
		this.publicInterfaceValue = publicInterfaceValue;
	}

	/** @return The public interface designation for this variable */
	public String getPublicInterfaceValue() {
		return publicInterfaceValue;
	}

	/**
	 * Sets the private interface designation for this variable
	 * @param privateInterfaceValue Valid values are "in", "out", or "none"
	 * in accordance with the CellML 1.1 specification
	 */
	public void setPrivateInterfaceValue(String privateInterfaceValue) {
		this.privateInterfaceValue = privateInterfaceValue;
	}

	/** @return The private interface designation for this variable */
	public String getPrivateInterfaceValue() {
		return privateInterfaceValue;
	}

	/**
	 * Sets the value of the variable at the start of simulation. This property is 
	 * included to support reading and writing of CellML models; the initial_value attribute
	 * in CellML models has a slightly different use than the "startValue" property in the SemSim DataStructure class.
	 * In CellML models, the value of a variable that remains constant throughout 
	 * a simulation can be set using the initial_value attribute. The attribute is also used to 
	 * set the initial values of state variables. In SemSim, the startValue property
	 * is only used to set the initial values of state variables whereas the values of simulation
	 * constants are determined by the DataStructure's corresponding {@link Computation} class.
	 * @param initialValue A string representation of the varible's initial value (in the CellML sense)
	 */
	public void setCellMLinitialValue(String initialValue) {
		this.CellMLinitialValue = initialValue;
	}
	
	/** @return The initial value for the variable (in the CellML sense)*/
	public String getCellMLinitialValue() {
		return CellMLinitialValue;
	}
	
	/** @return Whether the initial value is set on the variable*/
	public boolean hasCellMLinitialValue(){
		return  ! CellMLinitialValue.isEmpty() && CellMLinitialValue != null;
	}

	/** @return The set of MappableVariables to which this variable is mapped.
	 * In other words, the set of variables which receive this variable's value
	 * as an input.*/
	public Set<MappableVariable> getMappedTo() {
		return mappedTo;
	}

	/** @return The set of MappableVariables from which this variable is mapped.
	 * In other words, the set of variables that determine this variable's value.*/
	public MappableVariable getMappedFrom() {
		return mappedFrom;
	}
	
	/** @param to The MappableVariables that receive their values from this MappableVariable */
	public void setMappedTo(Set<MappableVariable> to) {
		mappedTo = to;
	}
	
	/** @param from The MappableVariable that provides values for this MappableVariable */
	public void setMappedFrom(MappableVariable from) {
		mappedFrom = from;
	}
	
	/**
	 * @return Whether a mapping exists between this MappableVariable and another in the model
	 */
	public boolean isMapped() {
		return !getMappedTo().isEmpty() || getMappedFrom()!=null; 
	}
	
	@Override
	public boolean isFunctionalSubmodelInput(){
		// If the mapped variable has an "in" interface value, return true
		return getPublicInterfaceValue().equals("in") || getPrivateInterfaceValue().equals("in"); 
	}
	
	@Override
	public DataStructure copy() {
		return new MappableVariable(this);
	}
	
	@Override
	public void flatten() {
		if (this.mappedFrom != null) {
			for (DataStructure tods : this.mappedTo) {
				tods.replaceDataStructureReference(tods, mappedFrom);
			}
		}
	}
	
	@Override
	public void replaceAllDataStructures(HashMap<DataStructure, DataStructure> dsmap) {
		super.replaceAllDataStructures(dsmap);
		this.mappedFrom = (MappableVariable) dsmap.get(this.mappedFrom);
		Set<MappableVariable> newmappedto = new HashSet<MappableVariable>();
		for (MappableVariable tomv : this.mappedTo) {
			DataStructure dsreplacement = dsmap.get(tomv);
			if (dsreplacement != null) {
				newmappedto.add((MappableVariable) dsreplacement);
			}
			
		}
		this.mappedTo = newmappedto;
	}

	/**
	 * Replace a MappableVariable that is mapped either to or from this MappableVariable
	 * with another
	 * @param replacer The MappableVariable replacement
	 * @param replacee The MappableVariable to replace
	 */
	public void replaceDataStructureReference(MappableVariable replacer, MappableVariable replacee) {
		super.replaceDataStructureReference(replacer, replacee);
		
		if (this.mappedFrom == replacee) {
			mappedFrom = replacer;
		}
		
		if (this.mappedTo.remove(replacee)) {
			mappedTo.add(replacer);
		}
	}
	
	@Override
	public DataStructure removeFromModel(SemSimModel model) {
		// Remove mappings to the data structure
		if (getMappedFrom()!=null) getMappedFrom().removeOutput(this);

		// Remove mappings from the data structure
		for(MappableVariable tov : getMappedTo()){
			tov.setMappedFrom(null);
		}
		
		model.removeDataStructure(this);
		return this;
	}
	
	@Override
	public void clearInputs() {
		super.clearInputs();
		mappedFrom = null;
		setPrivateInterfaceValue("");
		setPublicInterfaceValue("");
	}
}
