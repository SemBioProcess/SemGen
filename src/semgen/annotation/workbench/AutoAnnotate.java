package semgen.annotation.workbench;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import org.jdom.JDOMException;
import org.semanticweb.owlapi.model.OWLException;

import semgen.resource.SemGenError;
import semgen.resource.uicomponents.ProgressBar;
import semsim.SemSim;
import semsim.SemSimConstants;
import semsim.SemSimLibrary;
import semsim.model.SemSimModel;
import semsim.model.annotation.ReferenceOntologyAnnotation;
import semsim.model.computational.DataStructure;
import semsim.model.computational.MappableVariable;
import semsim.reading.ModelClassifier;
import semsim.reading.ReferenceTermNamer;
import semsim.reading.SBMLAnnotator;
import semsim.webservices.WebserviceTester;

public class AutoAnnotate {
	private File sourcefile;
	private SemSimModel semsimmodel;
	private SemSimLibrary semsimlib = SemSim.semsimlib;
	public AutoAnnotate(File file, SemSimModel semsimmod) {
		sourcefile = file;
		semsimmodel = semsimmod;
	}
	
	public void doAutoAnnotate(Boolean testifonline) throws OWLException, IOException, JDOMException, ServiceException {
		if (semsimmodel.getErrors().isEmpty()) {
			ProgressBar progframe = null;
			semsimmodel = autoAnnotateWithOPB(semsimmodel);
			if (semsimmodel.getSourceModelType()==ModelClassifier.SBML_MODEL) {
				progframe = new ProgressBar("Annotating with web services...", true);
				boolean online = WebserviceTester.testBioPortalWebservice("Annotation via web services failed.");
				if(!online) 
					SemGenError.showWebConnectionError(null, "BioPortal search service");
				SBMLAnnotator.annotate(sourcefile, semsimmodel, online, SemSim.semsimlib.getOntTermsandNamesCache());
				ReferenceTermNamer.getNamesForOntologyTermsInModel(semsimmodel, semsimlib.getOntTermsandNamesCache(), online);
				SBMLAnnotator.setFreeTextDefinitionsForDataStructuresAndSubmodels(semsimmodel);
				progframe.requestFocusInWindow();
			}
			else if (semsimmodel.getSourceModelType()==ModelClassifier.CELLML_MODEL) {
				progframe = new ProgressBar("Annotating " + semsimmodel.getName() + " with web services...", true);
				Boolean online = true;
				if(testifonline){
					online = WebserviceTester.testBioPortalWebservice("Annotation via web services failed.");
					if(!online) SemGenError.showWebConnectionError(null, "BioPortal search service");
				}
				if(progframe!=null) progframe.requestFocusInWindow();
				ReferenceTermNamer.getNamesForOntologyTermsInModel(semsimmodel, semsimlib.getOntTermsandNamesCache(), online);
			}
			progframe.dispose();
		}
		
	}
	
	
	public SemSimModel autoAnnotateWithOPB(SemSimModel semsimmodel) {
		Set<DataStructure> candidateamounts = new HashSet<DataStructure>();
		Set<DataStructure> candidateforces = new HashSet<DataStructure>();
		Set<DataStructure> candidateflows = new HashSet<DataStructure>();
		
		// If units present, set up physical property connected to each data structure
		for(DataStructure ds : semsimmodel.getDataStructures()){
			if(ds.hasUnits()){
				ReferenceOntologyAnnotation roa = semsimlib.getOPBAnnotationFromPhysicalUnit(ds);
				if(roa!=null){

					// If the codeword represents an OPB:Amount property (OPB_00135)
					if(SemSim.semsimlib.OPBhasAmountProperty(roa))
						candidateamounts.add(ds);
					// If the codeword represents an OPB:Force property (OPB_00574)
					else if(SemSim.semsimlib.OPBhasForceProperty(roa))
						candidateforces.add(ds);
					// If the codeword represents an OPB:Flow rate property (OPB_00573)
					else if(SemSim.semsimlib.OPBhasFlowProperty(roa)){
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
			if((camount instanceof MappableVariable)) hasinitval = (((MappableVariable)camount).getCellMLinitialValue()!=null);
			if(hasinitval && !camount.isDiscrete() 
					&& !camount.getPhysicalProperty().hasRefersToAnnotation()){
				ReferenceOntologyAnnotation roa = semsimlib.getOPBAnnotationFromPhysicalUnit(camount);
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
			for(DataStructure newcamount : getDownstreamDataStructures(unconfirmedamounts, camount, camount)){
				confirmedamounts.add(newcamount);
				ReferenceOntologyAnnotation roa = SemSim.semsimlib.getOPBAnnotationFromPhysicalUnit(newcamount);
				if(!newcamount.getPhysicalProperty().hasRefersToAnnotation())
					newcamount.getPhysicalProperty().addReferenceOntologyAnnotation(
						SemSimConstants.REFERS_TO_RELATION, roa.getReferenceURI(), roa.getValueDescription());
			}
		}
		// ID the forces
		Set<DataStructure> unconfirmedforces = new HashSet<DataStructure>();
		Set<DataStructure> confirmedforces = new HashSet<DataStructure>();
		Boolean annotate;
		for(DataStructure cforce : candidateforces){
			annotate = false;
			// If the candidate force is solved using a confirmed amount, annotate it
			if(cforce.getComputation()!=null){
				for(DataStructure cforceinput : cforce.getComputation().getInputs()){
					if(confirmedamounts.contains(cforceinput)){ annotate=true; break;}
				}
			}
			// If already decided to annotate, or the candidate is solved with an ODE and it's not a discrete variable, annotate it
			if((cforce.hasStartValue() || annotate) && !cforce.isDiscrete() 
					&& !cforce.getPhysicalProperty().hasRefersToAnnotation()){
				ReferenceOntologyAnnotation roa = SemSim.semsimlib.getOPBAnnotationFromPhysicalUnit(cforce);
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
			for(DataStructure newcforce : getDownstreamDataStructures(unconfirmedforces, cforce, cforce)){
				confirmedforces.add(newcforce);
				if(!newcforce.getPhysicalProperty().hasRefersToAnnotation()){
					ReferenceOntologyAnnotation roa = SemSim.semsimlib.getOPBAnnotationFromPhysicalUnit(newcforce);
					newcforce.getPhysicalProperty().addReferenceOntologyAnnotation(
						SemSimConstants.REFERS_TO_RELATION, roa.getReferenceURI(), roa.getValueDescription());
				}
			}
		}
		
		// ID the flows
		Set<DataStructure> unconfirmedflows = new HashSet<DataStructure>();
		Set<DataStructure> confirmedflows = new HashSet<DataStructure>();
		for(DataStructure cflow : candidateflows){
			annotate = false;
			// If the candidate flow is solved using a confirmed amount or force, annotate it
			if(cflow.getComputation()!=null){
				for(DataStructure cflowinput : cflow.getComputation().getInputs()){
					if(confirmedamounts.contains(cflowinput) || confirmedforces.contains(cflowinput)){ annotate=true; break;}
				}
			}
			// If already decided to annotate, or the candidate is solved with an ODE and it's not a discrete variable, annotate it
			if((cflow.hasStartValue() || annotate || cflow.getName().contains(":")) && !cflow.isDiscrete()
					&& !cflow.getPhysicalProperty().hasRefersToAnnotation()){
				ReferenceOntologyAnnotation roa = semsimlib.getOPBAnnotationFromPhysicalUnit(cflow);
				cflow.getPhysicalProperty().addReferenceOntologyAnnotation(
						SemSimConstants.REFERS_TO_RELATION, roa.getReferenceURI(), roa.getValueDescription());
			}
			unconfirmedflows.add(cflow);
		}
		// Second pass at flows
		temp.clear();
		temp.addAll(confirmedflows);
		for(DataStructure cflow : temp){
			for(DataStructure newcflow : getDownstreamDataStructures(unconfirmedflows, cflow, cflow)){
				confirmedforces.add(newcflow);
				if(!newcflow.getPhysicalProperty().hasRefersToAnnotation()){
					ReferenceOntologyAnnotation roa = semsimlib.getOPBAnnotationFromPhysicalUnit(newcflow);
					newcflow.getPhysicalProperty().addReferenceOntologyAnnotation(
						SemSimConstants.REFERS_TO_RELATION, roa.getReferenceURI(), roa.getValueDescription());
				}
			}
		}
		return semsimmodel;
	}
	
	public Set<DataStructure> getDownstreamDataStructures(Set<DataStructure> candidates, DataStructure mainroot, DataStructure curroot){
		// traverse all nodes that belong to the parent
		Set<DataStructure> newamounts = new HashSet<DataStructure>();
		for(DataStructure downstreamds : curroot.getUsedToCompute()){
			if(candidates.contains(downstreamds) && !newamounts.contains(downstreamds) && downstreamds!=mainroot && downstreamds!=curroot){
				newamounts.add(downstreamds);
				newamounts.addAll(getDownstreamDataStructures(newamounts, mainroot, downstreamds));
			}
		}
		return newamounts;
	}
}
