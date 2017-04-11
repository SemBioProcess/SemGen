package semsim.model.computational;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import semsim.definitions.SemSimTypes;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalDependency;

/** A Computation represents how the value of a SemSim {@link DataStructure} is determined, computationally. */

public class Computation extends ComputationalModelComponent{
	
	private Set<DataStructure> outputs = new HashSet<DataStructure>();
	private Set<DataStructure> inputs = new HashSet<DataStructure>();
	private String computationalCode = new String("");
	private String mathML = new String("");
	private Set<Event> events = new HashSet<Event>();
	private PhysicalDependency dependency = null;
	
	/**
	 * Class constructor with no output(s) specified
	 */
	public Computation(){
		super(SemSimTypes.COMPUTATION);
	}
	
	/**
	 * Class constructor with a single {@link DataStructure} set as the computation's output
	 * @param output The output DataStructure of the Computation
	 */
	public Computation(DataStructure output){
		super(SemSimTypes.COMPUTATION);
		outputs.add(output);
	}
	
	/**
	 * Class constructor that specifies a set of {@link DataStructure}s that the computation solves
	 * @param outputs The output DataStructures of the Computation
	 */
	public Computation(Set<DataStructure> outputs){
		super(SemSimTypes.COMPUTATION);
		outputs = new HashSet<DataStructure>(outputs);
	}
	
	public Computation(Computation comptocopy) {
		super(comptocopy);
		
		outputs.addAll(comptocopy.outputs);
		inputs.addAll(comptocopy.inputs);
		computationalCode = new String(comptocopy.computationalCode);
		mathML = new String(comptocopy.mathML);
		events.addAll(comptocopy.events);
	}
	
	/**
	 * Add a {@link DataStructure} to the Computation's set of inputs
	 * @param input The DataStructure to add as an input
	 * @return The set of all inputs for the Computation
	 */
	public Set<DataStructure> addInput(DataStructure input){
		
		if(! inputs.contains(input)){
			inputs.add(input);
			input.addUsedToCompute(getOutputs());
		}
		return inputs;
	}
	
	/**
	 * @return A string representation of the computational code used to solve the output
	 * DataStructure(s)
	 */
	public String getComputationalCode() {
		return computationalCode;
	}
	
	/**
	 * @return The list of {@link DataStructure} inputs used in the Computation
	 */
	public Set<DataStructure> getInputs(){
		return inputs;
	}
	
	/**
	 * @return The MathML representation of the computational code required to 
	 * solve the output(s)
	 */
	public String getMathML() {
		return mathML;
	}
	
	/** @return Whether the MathML for this computation is set*/
	public boolean hasMathML(){
		return ! mathML.equals("") && mathML!=null;
	}
	
	/**
	 * @ return The physical dependency associated with this computation
	 */
	public PhysicalDependency getPhysicalDependency(){
		return dependency;
	}
	
	/**
	 * Set the string representation of the computational code used to solve
	 * the output(s)
	 * @param code
	 */
	public void setComputationalCode(String code){
		if (code == null) code = new String("");
		computationalCode = code;
	}
	
	/**
	 * Set the inputs required to compute the output(s)
	 * @param inputs The required inputs for the computation
	 */
	public void setInputs(Set<DataStructure> inputs){
		this.inputs.clear();
		this.inputs.addAll(inputs);
	}
	
	/**
	 * Set the MathML representation of the computational code that
	 * solves the output(s)
	 * @param mathml The MathML code as a string
	 */
	public void setMathML(String mathml){
		mathML = mathml;
	}
	
	/**
	 * Set the outputs solved by the computation
	 * @param outputs The solved outputs
	 */
	public void setOutputs(Set<DataStructure> outputs) {
		this.outputs.clear();
		this.outputs.addAll(outputs);
	}
	
	/**
	 * Set the physical dependency associated with this computation
	 * @param dep The physical dependency to associate with this computation
	 */
	public void setPhysicalDependency(PhysicalDependency dep){
		dependency = dep;
	}
	
	/**
	 * Add an output for the computation
	 * @param output
	 */
	public void addOutput(DataStructure output){
		this.outputs.add(output);
	}

	/**
	 * @return The DataStructures solved by the computation
	 */
	public Set<DataStructure> getOutputs() {
		return outputs;
	}
	
	/**
	 * @return The all DataStructures involved in the computation
	 */
	public Set<DataStructure> getOutputsandInputs() {
		Set<DataStructure> alllinks = new HashSet<DataStructure>(outputs);
		alllinks.addAll(inputs);
		return alllinks;
	}
	
	
	/**	
	 * @return The set of discrete events that are part of this computation
	 */
	public Set<Event> getEvents() {
		return events;
	}

	/**
	 * Assign the set of discrete events associated with this computation
	 * @param events The events associated with this computation
	 */
	public void setEvents(Set<Event> events) {
		this.events.clear();
		this.events.addAll(events);
	}
	
	/**
	 * @return Whether the computation uses any {@link Events}
	 */
	public boolean hasEvents(){
		return ! events.isEmpty();
	}
	
	/**
	 * Add a discrete event to this computation
	 * @param event The discrete event to add
	 */
	public void addEvent(Event event){
		this.getEvents().add(event);
	}
	
	/**
	 * Remove a discrete event from the set of events associated with the computation
	 * @param event The discrete event to remove
	 */
	public void removeEvent(Event event){
		this.getEvents().remove(event);
	}
	
	public void removeOutput(DataStructure dstoremove) {
		outputs.remove(dstoremove);
	}
	
	/**
	 * @return Whether there is a {@link PhysicalDependency} associated with the computation 
	 */
	public boolean hasPhysicalDependency(){
		return dependency != null;
	}
	
	public void replaceAllDataStructures(HashMap<DataStructure, DataStructure> dsmap) {
		replaceOutputs(dsmap);
		replaceInputs(dsmap);
	}
	
	public void replaceOutputs(HashMap<DataStructure, DataStructure> dsmap) {
		Set<DataStructure> newoutputs = new HashSet<DataStructure>();
		for (DataStructure output : getOutputs()) {
			DataStructure replacer = dsmap.get(output);
			if (replacer != null) {
				newoutputs.add(replacer);
			}
		}
		setOutputs(newoutputs);
	}
	
	public void replaceInputs(HashMap<DataStructure, DataStructure> dsmap) {
		Set<DataStructure> newinputs = new HashSet<DataStructure>();
		for (DataStructure input : getInputs()) {
			DataStructure replacer = dsmap.get(input);
			if (replacer != null) {
				newinputs.add(replacer);
			}
		}
		setInputs(newinputs);
	}
	

	@Override
	public void addToModel(SemSimModel model) {}
}
