package semsim.model.computational;

import semsim.model.SemSimComponent;

/**
 * SemSim models are separated into ComputationalModelComponents and 
 * PhysicalModelComponents. A ComputationalModelComponent represents a 
 * computational or mathematical element of a SemSim model.
 * A PhysicalModelComponent represents a physical aspect of a model,
 * i.e. the "real world" phenomena that the model simulates
 * (PhysicalEntities, PhysicalProcesses, etc.).
 */
public abstract class ComputationalModelComponent extends SemSimComponent{
}
