package semgen.merging.workbench;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.jdom.JDOMException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import JSim.util.Xcept;
import semgen.encoding.Encoder;
import semgen.merging.FileToMergeLabel;
import semgen.merging.ResolutionPanel;
import semgen.merging.dialog.ConversionFactorDialog;
import semgen.resource.SemGenError;
import semgen.resource.SemGenTask;
import semgen.resource.Workbench;
import semgen.resource.file.LoadSemSimModel;
import semgen.resource.file.SemGenOpenFileChooser;
import semgen.resource.uicomponents.ProgressBar;
import semsim.SemSimUtil;
import semsim.model.SemSimModel;
import semsim.model.computational.DataStructure;
import semsim.model.physical.CompositePhysicalEntity;
import semsim.model.physical.Submodel;
import semsim.reading.ModelClassifier;

public class MergerWorkbench implements Workbench {
	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public File file1, file2, mergedfile;
	public SemSimModel semsimmodel1, semsimmodel2;
	
	public Set<String> initialidenticalinds = new HashSet<String>();
	public Set<String> identicaldsnames = new HashSet<String>();
	
	@Override
	public void loadModel() {
		// TODO Auto-generated method stub
		
	}

	public void merge() throws IOException, CloneNotSupportedException, OWLException, InterruptedException, JDOMException, Xcept {
		SemSimModel ssm1clone = semsimmodel1.clone();
		SemSimModel ssm2clone = semsimmodel2.clone();
		
		// First collect all the data structures that aren't going to be used in the resulting merged model
		// Include a mapping between the solution domains
		Component[] resolutionpanels = new Component[resolvepanel.getComponentCount()+1]; 
		for(int j=0; j<resolutionpanels.length-1;j++) resolutionpanels[j] = resolvepanel.getComponent(j);
		
		DataStructure soldom1 = ssm1clone.getSolutionDomains().toArray(new DataStructure[]{})[0];
		DataStructure soldom2 = ssm2clone.getSolutionDomains().toArray(new DataStructure[]{})[0];
		
		resolutionpanels[resolutionpanels.length-1] = new ResolutionPanel(null, soldom1, soldom2, ssm1clone, ssm2clone, 
				"automated solution domain mapping");
		
		SemSimModel modelfordiscardedds = null;
		for (int x = 0; x < resolutionpanels.length; x++) {
			ResolutionPanel rp = null;
			if ((resolutionpanels[x] instanceof ResolutionPanel)) {
				rp = (ResolutionPanel) resolutionpanels[x];
				DataStructure discardedds = null;
				DataStructure keptds = null;
				int sact = rp.getSelectedAction();
				if (sact==1 || rp.ds1.isSolutionDomain()){
					discardedds = rp.ds2;
					keptds = rp.ds1;
					modelfordiscardedds = ssm2clone;
				}
				else if(sact==2){
					discardedds = rp.ds1;
					keptds = rp.ds2;
					modelfordiscardedds = ssm1clone;
				}
				
				// If "ignore equivalency" is not selected"
				if(keptds!=null && discardedds !=null){
				
					// If we need to add in a unit conversion factor
					String replacementtext = keptds.getName();
					Boolean cancelmerge = false;
					double conversionfactor = 1;
					if(keptds.hasUnits() && discardedds.hasUnits()){
						if (!keptds.getUnit().getComputationalCode().equals(discardedds.getUnit().getComputationalCode())){
							ConversionFactorDialog condia = new ConversionFactorDialog(null,
									keptds.getName(), discardedds.getName(), keptds.getUnit().getComputationalCode(),
									discardedds.getUnit().getComputationalCode());
							replacementtext = condia.cdwdAndConversionFactor;
							conversionfactor = condia.conversionfactor;
							cancelmerge = !condia.process;
						}
					}
					
					if(cancelmerge) return;
					
					// if the two terms have different names, or a conversion factor is required
					if(!discardedds.getName().equals(keptds.getName()) || conversionfactor!=1){
						SemSimUtil.replaceCodewordInAllEquations(discardedds, keptds, modelfordiscardedds, discardedds.getName(), replacementtext, conversionfactor);
					}
					// What to do about sol doms that have different units?
					
					if(discardedds.isSolutionDomain()){
					  // Re-set the solution domain designations for all DataStructures in model 2
						for(DataStructure nsdds : ssm2clone.getDataStructures()){
							if(nsdds.hasSolutionDomain())
								nsdds.setSolutionDomain(soldom1);
						}
						// Remove .min, .max, .delta solution domain DataStructures
						for (String s : new String[]{".min", ".max", ".delta"}) {
							modelfordiscardedds.removeDataStructure(discardedds.getName() + s);
							identicaldsnames.remove(discardedds.getName() + s);
						}
					}
					
					// Remove the discarded Data Structure
					modelfordiscardedds.removeDataStructure(discardedds.getName());
					
					// If we are removing a state variable, remove its derivative, if present
					if(discardedds.hasSolutionDomain()){
						if(modelfordiscardedds.containsDataStructure(discardedds.getName() + ":" + discardedds.getSolutionDomain().getName())){
							modelfordiscardedds.removeDataStructure(discardedds.getName() + ":" + discardedds.getSolutionDomain().getName());
						}
					}
					
					// If the semantic resolution took care of a syntactic resolution
					identicaldsnames.remove(discardedds.getName());
				}
			}
		}
		
		// Why isn't this working for Pandit-Hinch merge?
		// Prompt the user to resolve the points of SYNTACTIC overlap (same codeword names)
		for (String dsname : identicaldsnames) {
			Boolean cont = true;
			while(cont){
				String newdsname = JOptionPane.showInputDialog(null, "Both models contain codeword " + dsname + ".\n" +
						"Enter new name for use in " + file1.getName() + " equations.\nNo special characters, no spaces.", "Duplicate codeword", JOptionPane.OK_OPTION);
				if(newdsname!=null && !newdsname.equals("") && !newdsname.equals(dsname)){
					ssm1clone.getDataStructure(dsname).setName(newdsname);
					Boolean derivreplace = false;
					String derivname = null;
					
					// If there is a derivative of the data structure that we're renaming, rename it, too
					if(ssm1clone.getDataStructure(newdsname).hasSolutionDomain()){
						derivname = dsname + ":" + ssm1clone.getDataStructure(newdsname).getSolutionDomain().getName();
						if(ssm1clone.containsDataStructure(derivname)){
							ssm1clone.getDataStructure(derivname).setName(derivname.replace(dsname, newdsname));
							derivreplace = true;
						}
					}
					// Use the new name in all the equations
					SemSimUtil.replaceCodewordInAllEquations(ssm1clone.getDataStructure(newdsname), ssm1clone.getDataStructure(newdsname),
							ssm1clone, dsname, newdsname, 1);
					
					// IS THERE AN ISSUE WITH SELF_REF_ODEs HERE?
					if(derivreplace){
						SemSimUtil.replaceCodewordInAllEquations(ssm1clone.getDataStructure(derivname.replace(dsname, newdsname)),
								ssm1clone.getDataStructure(derivname.replace(dsname, newdsname)),
								ssm1clone, derivname, derivname.replace(dsname, newdsname), 1);
					}
					cont = false;
				}
				else if(newdsname.equals(dsname)){
					JOptionPane.showMessageDialog(null, "That is the existing name. Please choose a new one.");
				}
			}
		}
		
		// What if both models have a custom phys component with the same name?
		SemSimModel mergedmodel = ssm1clone;
		
		// Create submodels representing the merged components, copy over all info from model2 into model1
		if(ssm1clone.getSolutionDomains().size()<=1 && ssm2clone.getSolutionDomains().size()<=1){
			
			Submodel sub1 = new Submodel(ssm1clone.getName());
			sub1.setAssociatedDataStructures(ssm1clone.getDataStructures());
			sub1.setSubmodels(ssm1clone.getSubmodels());
			
			Submodel sub2 = new Submodel(ssm2clone.getName());
			sub2.setAssociatedDataStructures(ssm2clone.getDataStructures());
			sub2.addDataStructure(soldom1);
			
			if(ssm1clone.containsDataStructure(soldom1.getName() + ".min"))
				sub2.addDataStructure(ssm1clone.getDataStructure(soldom1.getName() + ".min"));
			if(ssm1clone.containsDataStructure(soldom1.getName() + ".max"))
				sub2.addDataStructure(ssm1clone.getDataStructure(soldom1.getName() + ".max"));
			if(ssm1clone.containsDataStructure(soldom1.getName() + ".delta"))
				sub2.addDataStructure(ssm1clone.getDataStructure(soldom1.getName() + ".delta"));
			
			sub2.setSubmodels(ssm2clone.getSubmodels());
			mergedmodel.addSubmodel(sub1);
			mergedmodel.addSubmodel(sub2);
			
			// Copy in all data structures
			for(DataStructure dsfrom2 : ssm2clone.getDataStructures()){
				mergedmodel.addDataStructure(dsfrom2);
			}
			
			// Copy in the units
			mergedmodel.getUnits().addAll(ssm2clone.getUnits());
			
			// Copy in the submodels
			for(Submodel subfrom2 : ssm2clone.getSubmodels()){
				mergedmodel.addSubmodel(subfrom2);
			}
			
			// MIGHT NEED TO COPY IN PHYSICAL MODEL COMPONENTS?
		}
		else{
			JOptionPane.showMessageDialog(null, 
					"ERROR: One of the models to be merged has multiple solution domains.\nMerged model not saved.");
			return;
		}
		
		// WHAT TO DO ABOUT ONTOLOGY-LEVEL ANNOTATIONS?
		
		mergedmodel.setNamespace(mergedmodel.generateNamespaceFromDateAndTime());
		manager.saveOntology(mergedmodel.toOWLOntology(), new RDFXMLOntologyFormat(), IRI.create(mergedfile));
		optionToEncode(mergedmodel);
		refreshModelsToMerge();
	}
	
	public String validateModels(Component[] filecomponents) {
		if (filecomponents.length > 1) {
			refreshModelsToMerge();
			
			// If either of the models have errors, quit
			for(SemSimModel model : refreshModelsToMerge()){
				if(!model.getErrors().isEmpty()){
					return model.getName();
				}
			}
		}
		return "";
	}
	
	public Boolean testCompositePhysicalEntityEquivalency(CompositePhysicalEntity cpe1, CompositePhysicalEntity cpe2){
		if(cpe1.getArrayListOfEntities().size()!=cpe2.getArrayListOfEntities().size())
			return false;
		for(int i=0; i<cpe1.getArrayListOfEntities().size(); i++){
			if(cpe1.getArrayListOfEntities().get(i).hasRefersToAnnotation() && cpe2.getArrayListOfEntities().get(i).hasRefersToAnnotation()){
				if(!cpe1.getArrayListOfEntities().get(i).getFirstRefersToReferenceOntologyAnnotation().matches( 
					cpe2.getArrayListOfEntities().get(i).getFirstRefersToReferenceOntologyAnnotation())){
					return false;
				}
			}
			else return false;
		}
		return true;
	}

	
	public Set<SemSimModel> refreshModelsToMerge() {
		FileToMergeLabel label1 = (FileToMergeLabel) filelistpanel.getComponent(0);
		FileToMergeLabel label2 = (FileToMergeLabel) filelistpanel.getComponent(1);
		file1 = new File(label1.getText());
		file2 = new File(label2.getText());
		
		semsimmodel1 = LoadSemSimModel.loadSemSimModelFromFile(file1);
		
		if(semsimmodel1.getFunctionalSubmodels().size()>0) SemGenError.showFunctionalSubmodelError(null, file1);
		
		semsimmodel2 = LoadSemSimModel.loadSemSimModelFromFile(file2);
		
		if(semsimmodel2.getFunctionalSubmodels().size()>0) SemGenError.showFunctionalSubmodelError(null, file2);
		
		Set<SemSimModel> models = new HashSet<SemSimModel>();
		models.add(semsimmodel1);
		models.add(semsimmodel2);
		return models;
	}
	
	public Set<String> identifyIdenticalCodewords() {
		Set<String> matchedcdwds = new HashSet<String>();
		for (DataStructure ds : semsimmodel1.getDataStructures()) {
			if (semsimmodel2.containsDataStructure(ds.getName()))
				matchedcdwds.add(ds.getName());
		}
		return matchedcdwds;
	}
	
	public void optionToEncode(SemSimModel model) throws IOException, OWLException {
		int x = JOptionPane.showConfirmDialog(null, "Finished merging "
				+ mergedfile.getName()
				+ "\nGenerate simulation code from merged model?", "",
				JOptionPane.YES_NO_OPTION);
		if (x == JOptionPane.YES_OPTION) {
			new Encoder(model, model.getName());
		}
	}
	
	public void startAdditionOfModels(){
		AddModelsToMergeTask task = new AddModelsToMergeTask();
		task.execute();	
	}
	
	private class AddModelsToMergeTask extends SemGenTask {
		public Set<File> files = new HashSet<File>();
        public AddModelsToMergeTask(){
    		new SemGenOpenFileChooser(files, "Select SemSim models to merge");
        }
        @Override
        public Void doInBackground() {
        	progframe = new ProgressBar("Loading models...", true);
        	if (files.size() == 0) endTask();
        	try {
				addModelsToMerge(files);
			} catch (Exception e) {
				e.printStackTrace();
			}
            return null;
        }
    }

	private void addModelsToMerge(Set<File> files) {
		initialidenticalinds = new HashSet<String>();
		identicaldsnames = new HashSet<String>();
		for (File f : files) {
			if(ModelClassifier.classify(f)==ModelClassifier.CELLML_MODEL){
				SemGenError.showFunctionalSubmodelError(null, f);
			}
			else{
				FileToMergeLabel templabel = new FileToMergeLabel(f.getAbsolutePath());
				templabel.addMouseListener(this);
				templabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
				if (filelistpanel.getComponentCount() == 0) templabel.setForeground(Color.blue);
				else if (filelistpanel.getComponentCount() == 1) templabel.setForeground(Color.red);
				filelistpanel.add(templabel);
			}
		}
		if(filelistpanel.getComponentCount() > 1) primeForMergingStep();
		validate();
		repaint();
	}
	
	public class MergeTask extends SemGenTask {
		public MergeTask(){}
        @Override
        public Void doInBackground() {
        	progframe = new ProgressBar("Merging...", true);
        	try {
				merge();
			} catch (Exception e) {
				e.printStackTrace();
			}
            return null;
        }
	}
	
	@Override
	public boolean getModelSaved() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setModelSaved(boolean val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getCurrentModelName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getModelSourceFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File saveModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File saveModelAs() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
