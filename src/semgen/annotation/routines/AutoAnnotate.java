package semgen.annotation.routines;

import java.util.HashSet;
import java.util.Set;

import semgen.SemGen;
import semsim.SemSimConstants;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;

public class AutoAnnotate {
	// Automatically apply OPB annotations to the physical properties associated
	// with the model's data structures
	public static SemSimModel autoAnnotateWithOPB(SemSimModel semsimmodel) {		
		Set<DataStructure> candidateamounts = new HashSet<DataStructure>();
		Set<DataStructure> candidateforces = new HashSet<DataStructure>();
		Set<DataStructure> candidateflows = new HashSet<DataStructure>();
		
		// If units present, set up physical property connected to each data structure
		for(DataStructure ds : semsimmodel.getDataStructures()){
			if(ds.hasUnits()){
				ReferenceOntologyAnnotation roa = SemGen.semsimlib.getOPBAnnotationFromPhysicalUnit(ds);
				if(roa!=null){

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
					&& !camount.getPhysicalProperty().hasRefersToAnnotation()){
				ReferenceOntologyAnnotation roa = SemGen.semsimlib.getOPBAnnotationFromPhysicalUnit(camount);
				camount.getPhysicalProperty().addReferenceOntologyAnnotation(
						SemSimConstants.REFERS_TO_RELATION, roa.getReferenceURI(), roa.getValueDescription());
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
				ReferenceOntologyAnnotation roa = SemGen.semsimlib.getOPBAnnotationFromPhysicalUnit(newcamount);
				if(!newcamount.getPhysicalProperty().hasRefersToAnnotation())
					newcamount.getPhysicalProperty().addReferenceOntologyAnnotation(
						SemSimConstants.REFERS_TO_RELATION, roa.getReferenceURI(), roa.getValueDescription());
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
					&& !cforce.getPhysicalProperty().hasRefersToAnnotation()){
				ReferenceOntologyAnnotation roa = SemGen.semsimlib.getOPBAnnotationFromPhysicalUnit(cforce);
				cforce.getPhysicalProperty().addReferenceOntologyAnnotation(
						SemSimConstants.REFERS_TO_RELATION, roa.getReferenceURI(), roa.getValueDescription());
				confirmedforces.add(cforce);
			}
			else unconfirmedforces.add(cforce);
		}
		
		// Second pass at forces
		temp.clear();
		temp.addAll(confirmedforces);
		for(DataStructure cforce : temp){
			for(DataStructure newcforce : cforce.getDownstreamDataStructures(unconfirmedforces, null)){
				confirmedforces.add(newcforce);
				if(!newcforce.getPhysicalProperty().hasRefersToAnnotation()){
					ReferenceOntologyAnnotation roa = SemGen.semsimlib.getOPBAnnotationFromPhysicalUnit(newcforce);
					newcforce.getPhysicalProperty().addReferenceOntologyAnnotation(
						SemSimConstants.REFERS_TO_RELATION, roa.getReferenceURI(), roa.getValueDescription());
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
					&& !cflow.getPhysicalProperty().hasRefersToAnnotation()){
				ReferenceOntologyAnnotation roa = SemGen.semsimlib.getOPBAnnotationFromPhysicalUnit(cflow);
				cflow.getPhysicalProperty().addReferenceOntologyAnnotation(
						SemSimConstants.REFERS_TO_RELATION, roa.getReferenceURI(), roa.getValueDescription());
				confirmedflows.add(cflow);
			}
			else unconfirmedflows.add(cflow);
		}
		// Second pass at flows
		temp.clear();
		temp.addAll(confirmedflows);
		for(DataStructure cflow : temp){
			for(DataStructure newcflow : cflow.getDownstreamDataStructures(unconfirmedflows, null)){
				confirmedforces.add(newcflow);
				if(!newcflow.getPhysicalProperty().hasRefersToAnnotation()){
					ReferenceOntologyAnnotation roa = SemGen.semsimlib.getOPBAnnotationFromPhysicalUnit(newcflow);
					newcflow.getPhysicalProperty().addReferenceOntologyAnnotation(
						SemSimConstants.REFERS_TO_RELATION, roa.getReferenceURI(), roa.getValueDescription());
				}
			}
		}
		return semsimmodel;
	}
}
