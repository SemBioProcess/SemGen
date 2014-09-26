package semgen.extraction.workbench;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.semanticweb.owlapi.model.OWLException;

import edu.uci.ics.jung.graph.util.Pair;
import semgen.extraction.Clusterer;
import semgen.extraction.ExtractorJCheckBox;
import semgen.resource.SemGenTask;
import semgen.resource.Workbench;
import semgen.resource.file.FileFilter;
import semgen.resource.file.LoadSemSimModel;
import semgen.resource.file.SemGenOpenFileChooser;
import semgen.resource.uicomponents.ProgressBar;
import semsim.model.SemSimModel;
import semsim.model.computational.DataStructure;
import semsim.model.computational.MappableVariable;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.PhysicalProperty;
import semsim.model.physical.Submodel;
import semsim.reading.ModelClassifier;


public class ExtractorWorkbench implements Workbench {
	private SemSimModel semsimmodel;
	public File sourcefile; 
	private boolean modelsaved = true;
	private int lastsavedas = -1;
	public PrintWriter clusterwriter;
	
	public Hashtable<PhysicalEntity, Set<DataStructure>> entsanddatastrs = new Hashtable<PhysicalEntity, Set<DataStructure>>();
	public Hashtable<DataStructure, PhysicalEntity> datastrsandents = new Hashtable<DataStructure, PhysicalEntity>();
	public Hashtable<PhysicalProcess, Set<DataStructure>> processesanddatastrs = new Hashtable<PhysicalProcess, Set<DataStructure>>();
	public Hashtable<DataStructure, PhysicalProcess> datastrsandprocesses = new Hashtable<DataStructure, PhysicalProcess>();
	public Map<DataStructure, Set<? extends DataStructure>> allpreserveddatastructures;
	
	public ExtractorWorkbench(File file) {
		sourcefile = file;
		loadModel();
	}
	
	public void loadModel() {
		NewExtractorTask task = new NewExtractorTask();
		
		task.execute();
		semsimmodel = LoadSemSimModel.loadSemSimModelFromFile(sourcefile);
		
		if(ModelClassifier.classify(sourcefile)==ModelClassifier.CELLML_MODEL || semsimmodel.getFunctionalSubmodels().size()>0){
			JOptionPane.showMessageDialog(this, "Sorry. Extraction of models with CellML-type components not yet supported.");
			return;
		}
	}
	
	public void includeInputsInExtraction(DataStructure onedatastr) throws OWLException{
		for (DataStructure nextds : onedatastr.getComputation().getInputs()) {
			allpreserveddatastructures.put(nextds, nextds.getComputation().getInputs());
			for(DataStructure secondaryds : nextds.getComputation().getInputs()){
				if (!allpreserveddatastructures.keySet().contains(secondaryds)) {
					allpreserveddatastructures.put(secondaryds, new HashSet<DataStructure>());
				}
			}
		}
	}
	
	// List physical processes
		public Hashtable<PhysicalProcess, Set<DataStructure>> listprocesses() {
			Map<PhysicalProperty,PhysicalProcess> propandproc = semsimmodel.getPropertyAndPhysicalProcessTable();
			// List physical properties of processes
			for(PhysicalProperty prop : propandproc.keySet()){
				PhysicalProcess proc = propandproc.get(prop);
				Set<DataStructure> cdwds = new HashSet<DataStructure>();
				if(!processesanddatastrs.containsKey(proc)){
					cdwds.add(prop.getAssociatedDataStructure());
					processesanddatastrs.put(proc, cdwds);
				}
				// Otherwise we already added the process to the process-datastructure map, add the current property
				else
					processesanddatastrs.get(proc).add(prop.getAssociatedDataStructure());
			}
			
			for (PhysicalProcess proc : processesanddatastrs.keySet()) {
				for (DataStructure datastr : processesanddatastrs.get(proc)) {
					datastrsandprocesses.put(datastr, proc);
				}
			}
			return processesanddatastrs;
		}
		
		// List physical entities
		public Hashtable<PhysicalEntity, Set<DataStructure>> listentities() {
			Map<PhysicalProperty,PhysicalEntity> propandent = semsimmodel.getPropertyAndPhysicalEntityMap();
			for(PhysicalProperty prop : propandent.keySet()){
				PhysicalEntity ent = propandent.get(prop);
				if(entsanddatastrs.containsKey(ent)){
					entsanddatastrs.get(ent).add(prop.getAssociatedDataStructure());
				}
				else{
					Set<DataStructure> cdwds = new HashSet<DataStructure>();
					cdwds.add(prop.getAssociatedDataStructure());
					entsanddatastrs.put(ent, cdwds);
				}
			}
			for (PhysicalEntity ent : entsanddatastrs.keySet()) {
				for (DataStructure datastr : entsanddatastrs.get(ent)) {
					datastrsandents.put(datastr, ent);
				}
			}
			return entsanddatastrs;
		}

		// List the components
		public Hashtable<Submodel, Set<DataStructure>> listsubmodels(){
			Hashtable<Submodel,Set<DataStructure>> subsanddatastrs = new Hashtable<Submodel,Set<DataStructure>>();
			for(Submodel sub : semsimmodel.getSubmodels()){
				subsanddatastrs.put(sub, sub.getAssociatedDataStructures());
			}
			return subsanddatastrs;
		}
		
		public Hashtable<DataStructure, Set<DataStructure>> listcodewords() {
			// Iinda weird that we do it this way, but it's because of the way it used to be
			Hashtable<DataStructure, Set<DataStructure>> table = new Hashtable<DataStructure, Set<DataStructure>>();
			for(DataStructure ds : semsimmodel.getDataStructures()){
				Set<DataStructure> dsset = new HashSet<DataStructure>();
				dsset.add(ds);
				table.put(ds, dsset);
			}
			return table;
		}
		
		// returns the preserved data structures in the extract
		public Map<DataStructure, Set<? extends DataStructure>> primeextraction() throws IOException, OWLException {
			allpreserveddatastructures = new HashMap<DataStructure, Set<? extends DataStructure>>(); 

			// Get all the data structures in the selected processes panel
			// Then all the data structures in the selected entities panel
			// Then all the data structures associated with the selected variables
			// Then all the data structures from the selected modules
			for(int p = 0; p < processespanel.checkboxpanel.getComponentCount(); p++){
				if (processespanel.checkboxpanel.getComponent(p) instanceof ExtractorJCheckBox) {
					ExtractorJCheckBox tempbox = (ExtractorJCheckBox) processespanel.checkboxpanel.getComponent(p);
					if (tempbox.isSelected()) {
						for (DataStructure onedatastr : tempbox.associateddatastructures) {
							// Need to reroute for components
							Set<DataStructure> requiredinputs = onedatastr.getComputation().getInputs();
							allpreserveddatastructures.put(onedatastr, requiredinputs);
													
							// Un-comment this block and comment out "includeInputsInExtraction", above, to make the extraction more limited
							for(DataStructure oneinput : requiredinputs){
								if(!allpreserveddatastructures.containsKey(oneinput))
									allpreserveddatastructures.put(oneinput, new HashSet<DataStructure>());
							}
							
							// If user wants to include the process participants
							if (includepartipantscheckbox.isSelected()){
								// Add data structures associated with the participants in the process
								for(PhysicalEntity ent : ((PhysicalProcess)tempbox.pmc).getParticipantsAsPhysicalEntities()){
									if(entitiespanel.termandcdwdstable.containsKey(ent)){
										for(DataStructure entds : entitiespanel.termandcdwdstable.get(ent)){
											// Maybe change so that if a cdwd that we're including is dependent on another that's
											// a participant, make sure to include its inputs (all inputs?)
											allpreserveddatastructures.put(entds, entds.getComputation().getInputs());
//											// Add the entity's inputs, make them terminal
											for(DataStructure oneentin : entds.getComputation().getInputs()){
												if(!allpreserveddatastructures.containsKey(oneentin)){
													allpreserveddatastructures.put(oneentin, new HashSet<DataStructure>());
												}
											}
										}
									}
								}
							}
							// Add the process data structure's inputs, make them terminal
						}
					}
				}
			}
			
			// Collect data structures to preserve from the entities panel
			for (int w = 0; w < entitiespanel.checkboxpanel.getComponentCount(); w++) {
				if (entitiespanel.checkboxpanel.getComponent(w) instanceof ExtractorJCheckBox) {
					ExtractorJCheckBox tempbox = (ExtractorJCheckBox) entitiespanel.checkboxpanel.getComponent(w);
					if (tempbox.isSelected()) {
						for (DataStructure onedatastr : tempbox.associateddatastructures) {
							// Need to reroute for components
							allpreserveddatastructures.put(onedatastr, onedatastr.getComputation().getInputs()); 
							// If the more thorough extraction is used, trace the data structure's use in other equations and preserve their output data structures as well
							if (this.extractionlevelchooserentities.isSelected()) 
								includeInputsInExtraction(onedatastr);
							else{
								// Add the data structure's inputs, make them terminal
								for(DataStructure onein : onedatastr.getComputation().getInputs()){
									if(!allpreserveddatastructures.containsKey(onein)){
										allpreserveddatastructures.put(onein, new HashSet<DataStructure>());
									}
								}
							}
						}
					}
				}
			}

			
			// Collect the data structures to preserve from the sub-models checkboxes
			for (int x = 0; x < submodelspanel.checkboxpanel.getComponentCount(); x++) {
				if (codewordspanel.checkboxpanel.getComponent(x) instanceof ExtractorJCheckBox) {
					ExtractorJCheckBox tempbox = (ExtractorJCheckBox) submodelspanel.checkboxpanel.getComponent(x);
					if (tempbox.isSelected()) {
						for (DataStructure onedatastr : tempbox.associateddatastructures) {
							Set<DataStructure> requiredinputs = new HashSet<DataStructure>();
							if(onedatastr.getComputation()!=null){
								requiredinputs.addAll(onedatastr.getComputation().getInputs());
							}
							else if(onedatastr instanceof MappableVariable)
								requiredinputs.addAll(((MappableVariable)onedatastr).getMappedTo());
							allpreserveddatastructures.put(onedatastr, requiredinputs);
							for(DataStructure oneinput : requiredinputs){
								if(!allpreserveddatastructures.containsKey(oneinput)){
									allpreserveddatastructures.put(oneinput, new HashSet<DataStructure>());
								}
							}
						}
					}
				}
			}
			Set<DataStructure> outputspreserveddatastructures = new HashSet<DataStructure>();
			// Collect the data structures to preserve from the codewords checkboxes
			for (int x = 0; x < codewordspanel.checkboxpanel.getComponentCount(); x++) {
				if (codewordspanel.checkboxpanel.getComponent(x) instanceof ExtractorJCheckBox) {
					ExtractorJCheckBox tempbox = (ExtractorJCheckBox) codewordspanel.checkboxpanel.getComponent(x);
					if (tempbox.isSelected()) {
						for (DataStructure onedatastr : tempbox.associateddatastructures) {
							outputspreserveddatastructures.add(onedatastr);
						}
					}
				}
			}


			for (DataStructure pds : outputspreserveddatastructures) {
				// If the full dependency chain is requested
				if (this.extractionlevelchooser2.isSelected()) {
					for (DataStructure dstokeep : getDependencyChain(pds)) {
						allpreserveddatastructures.put(dstokeep, dstokeep.getComputation().getInputs());
					}
				}
				// If only the immediate inputs are requested
				else {
					Set<DataStructure> tempdsset = pds.getComputation().getInputs();
					allpreserveddatastructures.put(pds, tempdsset);
					for (DataStructure oneinput : tempdsset) {
						if (!allpreserveddatastructures.keySet().contains(oneinput)) {
							allpreserveddatastructures.put(oneinput, new HashSet<DataStructure>());
						} else if (allpreserveddatastructures.get(oneinput).isEmpty()) {
							System.out.println("Already added " + oneinput.getName() + ": leaving as is");
						}
					}
				}
			}

			// Get all selected clusters
			Set<DataStructure> moduleinputs = new HashSet<DataStructure>();
			for (int w = 0; w < clusterpanel.checkboxpanel.getComponentCount(); w++) {
				if (clusterpanel.checkboxpanel.getComponent(w) instanceof ExtractorJCheckBox) {
					ExtractorJCheckBox tempbox = (ExtractorJCheckBox) clusterpanel.checkboxpanel.getComponent(w);
					if (tempbox.isSelected()) {
						for (DataStructure onedatastr : tempbox.associateddatastructures) {
							if (!allpreserveddatastructures.keySet().contains(onedatastr)) {
								if(onedatastr.getComputation()!=null)
									allpreserveddatastructures.put(onedatastr, onedatastr.getComputation().getInputs());
							}
							// If the data structure was added as an input but it should be an output, make it an output
							else if (allpreserveddatastructures.get(onedatastr).isEmpty()) {
								allpreserveddatastructures.remove(onedatastr);
								allpreserveddatastructures.put(onedatastr, onedatastr.getComputation().getInputs());
							}
						}
						for (DataStructure onedatastr : tempbox.associateddatastructures) {
								moduleinputs.addAll(onedatastr.getComputation().getInputs());
						}
					}
				}
			}
			for (DataStructure ds : moduleinputs) {
				if (!allpreserveddatastructures.keySet().contains(ds)) allpreserveddatastructures.put(ds, new HashSet<DataStructure>());
			}
			
			// Make sure all the state variable derivative terms are included, include their inputs
			Map<DataStructure, Set<DataStructure>> tempmap = new HashMap<DataStructure, Set<DataStructure>>();
			for(DataStructure ds : allpreserveddatastructures.keySet()){
				if(ds.hasSolutionDomain()){
					if(ds.hasStartValue() && !allpreserveddatastructures.get(ds).isEmpty()
							&& semsimmodel.getDataStructure(ds.getName() + ":" + ds.getSolutionDomain().getName())!=null){
						tempmap.put(ds, ds.getComputation().getInputs());
						// Assumes that all inputs to the state variable are derivatives with respect to some solution domain
						// This is the way the SemSim model is structured, but is different from what is in the XMML
						for(DataStructure inds : ds.getComputation().getInputs()){
							tempmap.put(inds, inds.getComputation().getInputs());
							// Preserve the inputs to the derivative term. If not already preserved, make them static inputs
							for(DataStructure ininds : inds.getComputation().getInputs())
								if (!allpreserveddatastructures.keySet().contains(ininds))
									tempmap.put(ininds, new HashSet<DataStructure>());
						}
					}
				}
			}
			allpreserveddatastructures.putAll(tempmap);
			return allpreserveddatastructures;
		}
		
	public void batchCluster() throws IOException {
		visualizeAllDataStructures(false);
		cd = new Clusterer(junggraph, this);
		cd.setVisible(false);

		JFileChooser filec = new JFileChooser(SemGenOpenFileChooser.currentdirectory);
		filec.setDialogTitle("Choose location to save clustering results");
		filec.addChoosableFileFilter(new FileFilter(new String[] { "txt" }));
		Boolean saveok = false;
		File batchclusterfile = null;
		while (!saveok) {
			int returnVal = filec.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				batchclusterfile = new File(filec.getSelectedFile().getAbsolutePath());
				if (!batchclusterfile.getAbsolutePath().endsWith(".txt")
						&& !batchclusterfile.getAbsolutePath().endsWith(".TXT")) {
					batchclusterfile = new File(filec.getSelectedFile().getAbsolutePath() + ".txt");
				}
				if (batchclusterfile.exists()) {
					int overwriteval = JOptionPane.showConfirmDialog(this, "Overwrite existing file?",
							batchclusterfile.getName() + " already exists",JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
					if (overwriteval == JOptionPane.OK_OPTION) {
						saveok = true;
					}
				} else {
					saveok = true;
				}
			} else {
				saveok = true;
				batchclusterfile = null;
			}
		}

		if (batchclusterfile != null) {
			try {
				clusterwriter = new PrintWriter(new FileWriter(batchclusterfile));
			} catch (IOException e) {
				e.printStackTrace();
			}
			clusterwriter.println("-----Removing 0 edges-----");

			// Need task here
			BatchClusterTask task = new BatchClusterTask();
			task.execute();
		}
	}
	
	public class BatchClusterTask extends SemGenTask {
		private float maxclusteringiterations;

		public BatchClusterTask(){}
        
        public Void doInBackground() {
        	progframe = new ProgressBar("Performing clustering analysis...", false);
        	try {
        		performClusteringAnalysis();
			} catch (Exception e) {
				e.printStackTrace();
			}
            return null;
        }

		public void performClusteringAnalysis() throws IOException {
			// Make sure to ignore edges for state variables that are inputs to themselves
			int statevars = 0;
			for (Number edge : cd.mygraph.getEdges()) {
				Pair<String> pair = cd.mygraph.getEndpoints(edge);
				if (pair.getFirst().equals(pair.getSecond())) {
					statevars++;
				}
			}
			maxclusteringiterations = cd.mygraph.getEdgeCount() - statevars;
	
			// Loop through all clustering levels
			String moduletable = "";
			for (int y = 1; y <= maxclusteringiterations; y++) {
	
				clusterwriter.println("\n-----Removing " + y + " edges-----");
				System.out.println("-----Removing " + y + " edges-----");
				String newmoduletable = cd.clusterAndRecolor(cd.layout, y, cd.similarColors, Clusterer.groupVertices.isSelected());
				clusterwriter.println("-----Found " + cd.nummodules + " modules-----");
				if (!newmoduletable.equals(moduletable)) {
					moduletable = newmoduletable;
					clusterwriter.println(moduletable);
				} else {
					clusterwriter.println("(no change)");
				}
	
				progframe.bar.setValue(Math.round(100 * (y / maxclusteringiterations)));
			}
			clusterwriter.flush();
			clusterwriter.close();
	
			JOptionPane.showMessageDialog(null, "Finished clustering analysis");
		}


	}
	
	@Override
	public boolean getModelSaved() {
		return modelsaved;
	}

	@Override
	public void setModelSaved(boolean val) {
		modelsaved = val;
	}

	@Override
	public String getCurrentModelName() {

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

	public class NewExtractorTask extends SemGenTask {
		public File file;
        public NewExtractorTask(){
        	SemGenOpenFileChooser sgc =  new SemGenOpenFileChooser("Extractor - Select source SemSim model");	//new String[]{"owl"},
        	file = sgc.getSelectedFile();
        }
        public NewExtractorTask(File f){
        	file = f;
        }
        @Override
        public Void doInBackground() {
        	if (file==null) endTask(); //If no file was selected, abort
			progframe = new ProgressBar("Loading model for extraction...", true);
        	try {
        		newExtractorTabAction(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
            return null;
        }
    }
	
}
