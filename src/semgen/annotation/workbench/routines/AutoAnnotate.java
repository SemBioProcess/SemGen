package semgen.annotation.workbench.routines;

import java.util.HashSet;
import java.util.Set;

import semgen.SemGen;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.physical.object.PhysicalProperty;

public class AutoAnnotate {
	// Automatically apply OPB annotations to the physical properties associated
	// with the model's data structures
	public static SemSimModel autoAnnotateWithOPB(SemSimModel semsimmodel) {		
		Set<DataStructure> candidateamounts = new HashSet<DataStructure>();
		Set<DataStructure> candidateforces = new HashSet<DataStructure>();
		Set<DataStructure> candidateflows = new HashSet<DataStructure>();
		
		// If units present, set up physical property connected to each data structure
		for(DataStructure ds : semsimmodel.getAssociatedDataStructures()){
			if(ds.hasUnits()){
				PhysicalProperty pp = SemGen.semsimlib.getOPBAnnotationFromPhysicalUnit(ds);			
				
				if(pp!=null){
					semsimmodel.addPhysicalProperty(pp);
					ReferenceOntologyAnnotation roa = pp.getRefersToReferenceOntologyAnnotation();
					// If the codeword represents an OPB:Amount property (OPB_00135)
					if(SemGen.semsimlib.OPBhasAmountProperty(roa))
						candidateamounts.add(ds);
					// If the codeword represents an OPB:Force property (OPB_00574)
					else if(SemGen.semsimlib.OPBhasForceProperty(roa))
						candidateforces.add(ds);
					// If the codeword represents an OPB:Flow rate property (OPB_00573)
					else if(SemGen.semsimlib.OPBhasFlowProperty(roa)){
						candidateflows.add(ds);
					}
				}
			}
		}
		// ID the amounts
		Set<DataStructure> unconfirmedamounts = new HashSet<DataStructure>();
		Set<DataStructure> confirmedamounts = new HashSet<DataStructure>();
		for(DataStructure camount : candidateamounts){
			Boolean hasinitval = camount.hasStartValue();
			if((camount instanceof MappableVariable)) {
				hasinitval = (((MappableVariable)camount).getCellMLinitialValue()!=null);
			}
			
			if(hasinitval && !camount.isDiscrete() 
					&& !camount.hasPhysicalProperty()){
				camount.setPhysicalProperty(SemGen.semsimlib.getOPBAnnotationFromPhysicalUnit(camount));
				confirmedamounts.add(camount);
			}
			else unconfirmedamounts.add(camount);
		}
		// second pass at amounts
		Set<DataStructure> temp = new HashSet<DataStructure>();
		temp.addAll(confirmedamounts);
		for(DataStructure camount : temp){
			for(DataStructure newcamount : camount.getDownstreamDataStructures(unconfirmedamounts, null)){
				confirmedamounts.add(newcamount);
				if (!newcamount.hasPhysicalProperty()) {
					newcamount.setPhysicalProperty(SemGen.semsimlib.getOPBAnnotationFromPhysicalUnit(newcamount));
				}
			}
		}
		// ID the forces
		Set<DataStructure> unconfirmedforces = new HashSet<DataStructure>();
		Set<DataStructure> confirmedforces = new HashSet<DataStructure>();
		for(DataStructure cforce : candidateforces){
			Boolean annotate = false;
			// If the candidate force is solved using a confirmed amount, annotate it
			if(cforce.getComputation()!=null){
				for(DataStructure cforceinput : cforce.getComputation().getInputs()){
					if(confirmedamounts.contains(cforceinput)){ 
						annotate=true; 
						break;
					}
				}
			}
			// If already decided to annotate, or the candidate is solved with an ODE and it's not a discrete variable, annotate it
			if((cforce.hasStartValue() || annotate) && !cforce.isDiscrete() 
					&& !cforce.hasPhysicalProperty()) {
				cforce.setPhysicalProperty(SemGen.semsimlib.getOPBAnnotationFromPhysicalUnit(cforce));
			}
			else unconfirmedforces.add(cforce);
		}
		
		// Second pass at forces
		temp.clear();
		temp.addAll(confirmedforces);
		for(DataStructure cforce : temp){
			for(DataStructure newcforce : cforce.getDownstreamDataStructures(unconfirmedforces, null)){
				confirmedforces.add(newcforce);
				if(!newcforce.hasPhysicalProperty()){
					newcforce.setPhysicalProperty(SemGen.semsimlib.getOPBAnnotationFromPhysicalUnit(newcforce));
				}
			}
		}
		
		// ID the flows
		Set<DataStructure> unconfirmedflows = new HashSet<DataStructure>();
		Set<DataStructure> confirmedflows = new HashSet<DataStructure>();
		for(DataStructure cflow : candidateflows){
			Boolean annotate = false;
			// If the candidate flow is solved using a confirmed amount or force, annotate it
			if(cflow.getComputation()!=null){
				for(DataStructure cflowinput : cflow.getComputation().getInputs()){
					if(confirmedamounts.contains(cflowinput) || confirmedforces.contains(cflowinput)){ annotate=true; break;}
				}
			}
			// If already decided to annotate, or the candidate is solved with an ODE and it's not a discrete variable, annotate it
			if((cflow.hasStartValue() || annotate || cflow.getName().contains(":")) && !cflow.isDiscrete()
					&& !cflow.hasPhysicalProperty()){
				cflow.setPhysicalProperty(SemGen.semsimlib.getOPBAnnotationFromPhysicalUnit(cflow));
			}
			else unconfirmedflows.add(cflow);
		}
		// Second pass at flows
		temp.clear();
		temp.addAll(confirmedflows);
		for(DataStructure cflow : temp){
			for(DataStructure newcflow : cflow.getDownstreamDataStructures(unconfirmedflows, null)){
				confirmedforces.add(newcflow);
				if(!newcflow.hasPhysicalProperty()){
					newcflow.setPhysicalProperty(SemGen.semsimlib.getOPBAnnotationFromPhysicalUnit(newcflow));
				}
			}
		}
		return semsimmodel;
	}
}
