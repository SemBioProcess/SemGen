package semgen.resource.file;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import semgen.SemGen;
import semgen.resource.SemGenError;
import semgen.resource.uicomponent.SemGenProgressBar;
import semsim.SemSimConstants;
import semsim.model.SemSimModel;
import semsim.model.annotation.ReferenceOntologyAnnotation;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.reading.CellMLreader;
import semsim.reading.MMLreader;
import semsim.reading.ModelClassifier;
import semsim.reading.ReferenceTermNamer;
import semsim.reading.SBMLAnnotator;
import semsim.reading.SemSimOWLreader;
import semsim.webservices.WebserviceTester;

public class LoadSemSimModel {
	
	public static SemSimModel loadSemSimModelFromFile(File file, boolean autoannotate) {
		SemSimModel semsimmodel = null;
		int modeltype = ModelClassifier.classify(file);

		String JSimBuildDir = "./jsimhome";
		Boolean autoannotatesbml = (modeltype==ModelClassifier.SBML_MODEL && autoannotate);
		try {
			switch (modeltype){
			
			case ModelClassifier.MML_MODEL:
					semsimmodel = new MMLreader(JSimBuildDir).readFromFile(file);
					if(semsimmodel.getErrors().isEmpty() && autoannotate)
						semsimmodel = autoAnnotateWithOPB(semsimmodel);
				break;
					
			case ModelClassifier.SBML_MODEL:// MML
					semsimmodel = new MMLreader(JSimBuildDir).readFromFile(file);
					if(semsimmodel.getErrors().isEmpty() && autoannotatesbml){
						// If it's an SBML model and we should auto-annotate
						semsimmodel = autoAnnotateWithOPB(semsimmodel);
						SemGenProgressBar progframe = new SemGenProgressBar("Annotating with web services...",true);
						boolean online = WebserviceTester.testBioPortalWebservice("Annotation via web services failed.");
						if(!online) 
							SemGenError.showWebConnectionError(null, "BioPortal search service");
						SBMLAnnotator.annotate(file, semsimmodel, online, SemGen.semsimlib.getOntTermsandNamesCache());
						ReferenceTermNamer.getNamesForOntologyTermsInModel(semsimmodel, SemGen.semsimlib.getOntTermsandNamesCache(), online);
						SBMLAnnotator.setFreeTextDefinitionsForDataStructuresAndSubmodels(semsimmodel);
						progframe.dispose();
					}
				break;
				
			case ModelClassifier.CELLML_MODEL:
				semsimmodel = new CellMLreader().readFromFile(file);
				if(semsimmodel.getErrors().isEmpty()){
					if(autoannotate){
						semsimmodel = autoAnnotateWithOPB(semsimmodel);
						SemGenProgressBar progframe = new SemGenProgressBar("Annotating " + file.getName() + " with web services...",true);
						Boolean online = true;
						
							online = WebserviceTester.testBioPortalWebservice("Annotation via web services failed.");
							if(!online) SemGenError.showWebConnectionError(null, "BioPortal search service");

						progframe.dispose();
						ReferenceTermNamer.getNamesForOntologyTermsInModel(semsimmodel,  SemGen.semsimlib.getOntTermsandNamesCache(), online);
					}
				}
				break;		
			case ModelClassifier.SEMSIM_MODEL:
				semsimmodel = loadSemSimOWL(file);
				break;
				
			default:
				JOptionPane.showMessageDialog(null, "SemGen did not recognize the file type for " + file.getName(),
						"Error: Unrecognized model format", JOptionPane.ERROR_MESSAGE);
				break;
			}
		}
		catch(Exception e){e.printStackTrace();}
		
		if(semsimmodel!=null){
			if(!semsimmodel.getErrors().isEmpty()){
				String errormsg = "";
				for(String catstr : semsimmodel.getErrors())
					errormsg = errormsg + catstr + "\n";
				JOptionPane.showMessageDialog(null, errormsg, "ERROR", JOptionPane.ERROR_MESSAGE);
				return semsimmodel;
			}
			semsimmodel.setName(file.getName().substring(0, file.getName().lastIndexOf(".")));
			semsimmodel.setSourceModelType(modeltype);				
		}

		return semsimmodel;
	}
	
	public static SemSimModel loadSemSimOWL(File file) throws Exception {
		return new SemSimOWLreader().readFromFile(file);
	}
	
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
				if((camount instanceof MappableVariable)) hasinitval = (((MappableVariable)camount).getCellMLinitialValue()!=null);
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
						if(confirmedamounts.contains(cforceinput)){ annotate=true; break;}
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
