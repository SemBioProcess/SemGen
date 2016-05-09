package semgen.extraction;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import edu.uci.ics.jung.graph.SparseMultigraph;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.extraction.RadialGraph.Clusterer;
import semgen.extraction.RadialGraph.SemGenRadialGraphView;
import semgen.extraction.workbench.ExtractorWorkbench;
import semgen.utilities.ComparatorByName;
import semgen.utilities.SemGenError;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;
import semgen.utilities.SemGenTask;
import semgen.utilities.file.FileFilter;
import semgen.utilities.file.SaveSemSimModel;
import semgen.utilities.file.SemGenFileChooser;
import semgen.utilities.file.SemGenSaveFileChooser;
import semgen.utilities.uicomponent.SemGenProgressBar;
import semgen.utilities.uicomponent.SemGenTab;
import semsim.extraction.Extraction;
import semsim.model.collection.SemSimModel;
import semsim.model.collection.Submodel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.reading.ModelAccessor;
import semsim.reading.ModelClassifier;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

public class ExtractorTab extends SemGenTab implements ActionListener, ItemListener {

	public static final long serialVersionUID = -5142058482154697778L;
	SemGenSettings settings;
	public SemSimModel semsimmodel;
	public OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public static final String LABEL = "codeword";
	public static final Schema LABEL_SCHEMA = new Schema();
	static {LABEL_SCHEMA.addColumn(LABEL, String.class, "");}
	public static final String COMPCODE = "computationalCode";
	public static final Schema COMPCODE_SCHEMA = new Schema();
	static {COMPCODE_SCHEMA.addColumn(COMPCODE, String.class, "");}
	public static final String COMPOSITEANN = "compositeAnnotation";
	public static final Schema COMPOSITEANN_SCHEMA = new Schema();
	static {COMPOSITEANN_SCHEMA.addColumn(COMPOSITEANN, String.class, "");}
	public static final String INPUT = "input";
	public static final Schema INPUT_SCHEMA = new Schema();
	static {INPUT_SCHEMA.addColumn(INPUT, boolean.class, false);}
	public static final String VAR2INPUT = "var2input";
	public static final Schema VAR2INPUT_SCHEMA = new Schema();
	static {VAR2INPUT_SCHEMA.addColumn(VAR2INPUT, boolean.class, false);}
	public static final String PROCESS = "process";
	public static final Schema PROCESS_SCHEMA = new Schema();
	static {PROCESS_SCHEMA.addColumn(PROCESS, boolean.class, false);}
	public static final String TOOLTIP = "tooltip";
	public static final Schema TOOLTIP_SCHEMA = new Schema();
	static {TOOLTIP_SCHEMA.addColumn(TOOLTIP, String.class, "");}

	public static int leftpanewidth = 400;
	
	public JButton vizsourcebutton = new JButton("Show source model");
	public JButton clusterbutton = new JButton("Cluster");
	public JCheckBox extractionlevelchooserentities = new JCheckBox("More inclusive");
	public JCheckBox includepartipantscheckbox = new JCheckBox("Include participants");
	public JCheckBox extractionlevelchooser2 = new JCheckBox("Include full dependency chain");
	public JButton extractbutton = new JButton("EXTRACT");
	public ExtractorSelectionPanel processespanel;
	public ExtractorSelectionPanel entitiespanel;
	public ExtractorSelectionPanel submodelspanel;
	public ExtractorSelectionPanel codewordspanel;
	public ExtractorSelectionPanel clusterpanel;
	public JPanel physiomappanel = new JPanel();
	public JTabbedPane graphtabpane = new JTabbedPane();
	
	public ModelAccessor extractedaccessor;
	public File autogendirectory;
	public SemSimModel extractedmodel;
	
	public SemGenRadialGraphView view;
	public Graph tempgraph;
	public Graph physiomapgraph;
	public SparseMultigraph<String, Number> junggraph;

	public Clusterer cd;
	public PrintWriter clusterwriter;
	public ExtractorToolbar toolbar;
	private ExtractorWorkbench workbench;
//	public enum selectionTypes {processes, entities, datastructures, submodels, clusters};
	
	public ExtractorTab(SemGenSettings sets, GlobalActions gacts, ExtractorWorkbench bench) {
		super(bench.getCurrentModelName(), SemGenIcon.extractoricon, "Extracting from " + bench.getCurrentModelName(), sets, gacts);
		settings = sets;
		workbench = bench;
		semsimmodel = workbench.getSourceModel();
	}
	
	@Override
	public void loadTab() {
		this.setLayout(new BorderLayout());
		vizsourcebutton.setFont(SemGenFont.defaultPlain());
		vizsourcebutton.addActionListener(this);

		extractbutton.setForeground(Color.blue);
		extractbutton.setFont(SemGenFont.defaultBold());
		extractbutton.addActionListener(this);
		
		extractionlevelchooserentities.setFont(SemGenFont.defaultPlain(-2));
		extractionlevelchooserentities.setBorder(BorderFactory.createEmptyBorder(0,35,0,0));
		extractionlevelchooserentities.addItemListener(this);
		
		includepartipantscheckbox.setFont(SemGenFont.defaultPlain(-2));
		includepartipantscheckbox.setBorder(BorderFactory.createEmptyBorder(0,35,0,0));
		includepartipantscheckbox.setSelected(false);
		includepartipantscheckbox.addItemListener(this);

		extractionlevelchooser2.setFont(SemGenFont.defaultPlain(-2));
		extractionlevelchooser2.addItemListener(this);

		clusterbutton.addActionListener(this);
		
		// List entities first because listprocesses needs data in entitiespanel
		processespanel = new ExtractorSelectionPanel(this, "Processes", createProcessDataStructureMap(), includepartipantscheckbox);
		entitiespanel = new ExtractorSelectionPanel(this, "Entities", createEntityDataStructureMap(), null);
		submodelspanel = new ExtractorSelectionPanel(this, "Sub-models", createSubmodelDataStructureMap(), null);
		codewordspanel = new ExtractorSelectionPanel(this, "Codewords", createDataStructureMap(), extractionlevelchooser2);
		HashMap<PhysicalModelComponent,Set<DataStructure>> temp = new HashMap<PhysicalModelComponent,Set<DataStructure>>();
		clusterpanel = new ExtractorSelectionPanel(this, "Clusters", temp, clusterbutton);

		AlphabetizeCheckBoxes(processespanel);
		AlphabetizeCheckBoxes(entitiespanel);
		AlphabetizeCheckBoxes(submodelspanel);
		AlphabetizeCheckBoxes(codewordspanel);
		
		createToolbar();
		
		JPanel leftpanel = new JPanel();
		leftpanel.setLayout(new BoxLayout(leftpanel, BoxLayout.Y_AXIS));
		leftpanel.add(toolbar);
		leftpanel.add(processespanel.titlepanel);
		leftpanel.add(processespanel.scroller);
		leftpanel.add(entitiespanel.titlepanel);
		leftpanel.add(entitiespanel.scroller);
		leftpanel.add(submodelspanel.titlepanel);
		leftpanel.add(submodelspanel.scroller);
		leftpanel.add(codewordspanel.titlepanel);
		leftpanel.add(codewordspanel.scroller);
		leftpanel.add(clusterpanel.titlepanel);
		leftpanel.add(clusterpanel.scroller);
		
		JPanel rightpanel = new JPanel();
		rightpanel.setLayout(new BoxLayout(rightpanel,BoxLayout.Y_AXIS));
		rightpanel.add(graphtabpane);
		JSplitPane centersplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftpanel, rightpanel);
		centersplitpane.setOneTouchExpandable(true);
		add(centersplitpane, BorderLayout.CENTER);
		setVisible(true);
		visualizeAllDataStructures(false);
	}
	
	public void createToolbar() {
		toolbar = new ExtractorToolbar(settings) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Object o = arg0.getSource();
				if (o == toolbar.extractoritembatchcluster) {
						try {
							batchCluster();
						} catch (IOException e1) {e1.printStackTrace();}
				}

				if (o == toolbar.extractoritemopenann) {
						try {
							globalactions.NewAnnotatorTab(workbench.getModelAccessor());
						} catch (Exception e1) {e1.printStackTrace();} 
					}	
			}
		};
	}
	
	// Generate the mappings between processes and the data structures they are associated with
	private Map<PhysicalProcess,Set<DataStructure>> createProcessDataStructureMap(){
		
		Map<PhysicalProcess,Set<DataStructure>> processdatastructuremap = new HashMap<PhysicalProcess,Set<DataStructure>>();
		Set<DataStructure> propandproc = semsimmodel.getDataStructureswithPhysicalProcesses();
		
		// List physical properties of processes
		for(DataStructure ds : propandproc){
			PhysicalProcess proc = (PhysicalProcess) ds.getAssociatedPhysicalModelComponent();
			
			if(processdatastructuremap.containsKey(proc)){
				processdatastructuremap.get(proc).add(ds);
			}
			else{
				Set<DataStructure> cdwds = new HashSet<DataStructure>();
				cdwds.add(ds);
				processdatastructuremap.put(proc, cdwds);
			}
		}
		return processdatastructuremap;
	}
	
	// Generate the mappings between entities and the data structures they are associated with
	private Map<PhysicalEntity,Set<DataStructure>> createEntityDataStructureMap(){
		
		Map<PhysicalEntity,Set<DataStructure>> entitydatastructuremap = new HashMap<PhysicalEntity,Set<DataStructure>>();
		Set<DataStructure> dses = semsimmodel.getDataStructureswithCompositesEntities();
		
		for(DataStructure ds : dses){
			CompositePhysicalEntity ent = (CompositePhysicalEntity) ds.getAssociatedPhysicalModelComponent();
			
			if(entitydatastructuremap.containsKey(ent)){
				entitydatastructuremap.get(ent).add(ds);
			}
			else{
				Set<DataStructure> cdwds = new HashSet<DataStructure>();
				cdwds.add(ds);
				entitydatastructuremap.put(ent, cdwds);
			}
		}
		
		return entitydatastructuremap;
	}
	
	// Generate the mappings between submodels and the data structures they are associated with
	private Map<Submodel,Set<DataStructure>> createSubmodelDataStructureMap(){
			
		Map<Submodel, Set<DataStructure>> submodeldatastructuremap = new HashMap<Submodel,Set<DataStructure>>();
		
		for(Submodel submodel : semsimmodel.getSubmodels()){
			Set<DataStructure> dsset = new HashSet<DataStructure>();
			dsset.addAll(submodel.getAssociatedDataStructures());
			submodeldatastructuremap.put(submodel, dsset);
		}
		
		return submodeldatastructuremap;
	}

	
	public Map<DataStructure, Set<DataStructure>> createDataStructureMap() {
		// Iinda weird that we do it this way, but it's because of the way it used to be
		HashMap<DataStructure, Set<DataStructure>> table = new HashMap<DataStructure, Set<DataStructure>>();
		for(DataStructure ds : semsimmodel.getAssociatedDataStructures()){
			Set<DataStructure> dsset = new HashSet<DataStructure>();
			dsset.add(ds);
			table.put(ds, dsset);
		}
		return table;
	}

	// returns the preserved data structures in the extract
	public Extraction primeextraction() {
		
		workbench.getExtraction().reset(); 
		
		Set<DataStructure> outputspreserveddatastructures = new HashSet<DataStructure>();

		// Get all the contents in the selected processes panel
		// Then all the contents in the selected entities panel
		// Then all the contents associated with the selected variables
		// Then all the contents from the selected modules
		for(int p = 0; p < processespanel.checkboxpanel.getComponentCount(); p++){
			
			if (processespanel.checkboxpanel.getComponent(p) instanceof ExtractorJCheckBox) {
				ExtractorJCheckBox tempbox = (ExtractorJCheckBox) processespanel.checkboxpanel.getComponent(p);
				
				if (tempbox.isSelected()) {
					
					PhysicalProcess processtoextract = (PhysicalProcess)tempbox.smc;
					Boolean includeparticipants = includepartipantscheckbox.isSelected();
					workbench.getExtraction().addProcessToExtract(processtoextract, includeparticipants);
					
					for (DataStructure onedatastr : tempbox.associateddatastructures) {

						Set<DataStructure> requiredinputs = onedatastr.getComputationInputs();
						workbench.getExtraction().addDataStructureToExtraction(onedatastr, true);						
						
						for(DataStructure oneinput : requiredinputs){
							if(!workbench.getExtraction().getDataStructuresToExtract().containsKey(oneinput))
								workbench.getExtraction().addDataStructureToExtraction(oneinput, false);
						}
						
						// If user wants to include the process participants
						if (includepartipantscheckbox.isSelected()){
							addParticipantsToExtractionMap((PhysicalProcess)tempbox.smc);
						}
					}
				}
			}
		}
		
		// Collect contents to preserve from the entities panel
		for (int w = 0; w < entitiespanel.checkboxpanel.getComponentCount(); w++) {
			
			if (entitiespanel.checkboxpanel.getComponent(w) instanceof ExtractorJCheckBox) {
				ExtractorJCheckBox tempbox = (ExtractorJCheckBox) entitiespanel.checkboxpanel.getComponent(w);
				
				if (tempbox.isSelected()) {
					
					PhysicalEntity enttoextract = (PhysicalEntity)tempbox.smc;
					workbench.getExtraction().addEntityToExtract(enttoextract, true);
					
					for (DataStructure onedatastr : tempbox.associateddatastructures) {
						
						workbench.getExtraction().getDataStructuresToExtract().put(onedatastr, true); 
						
						// If the more thorough extraction is used, trace the data structure's use in other equations and preserve their output data structures as well
						if (this.extractionlevelchooserentities.isSelected()) 
							workbench.getExtraction().addInputsToExtract(onedatastr);
						else{
							
							// Add the data structure's inputs, make them terminal
							for(DataStructure onein : onedatastr.getComputationInputs()){
								
								if(! workbench.getExtraction().getDataStructuresToExtract().containsKey(onein)){
									workbench.getExtraction().addDataStructureToExtraction(onein, false);
								}
							}
						}
					}
				}
			}
		}
		
		// Collect the contents to preserve from the sub-models checkboxes
		for (int x = 0; x < submodelspanel.checkboxpanel.getComponentCount(); x++) {
			
			if (codewordspanel.checkboxpanel.getComponent(x) instanceof ExtractorJCheckBox) {
				ExtractorJCheckBox tempbox = (ExtractorJCheckBox) submodelspanel.checkboxpanel.getComponent(x);
				
				if (tempbox.isSelected()) {
					
					Submodel submodel = (Submodel)tempbox.smc;
					
					workbench.getExtraction().addSubmodelToExtract(submodel, true);
					
					// Add associated data structures to the extraction
					// If the data structure is an input, don't include the variables that map to it 
					for (DataStructure onedatastr : tempbox.associateddatastructures) {
						
						boolean isinputforsubmodel = false;
						
						// Don't get the computational inputs if the data structure is a mappable variable
						// with a public "in" interface. This avoids adding in unnecessary variables that are
						// mapped to the data structure.
						
						if(onedatastr instanceof MappableVariable){
							MappableVariable mv = (MappableVariable)onedatastr;
							isinputforsubmodel = mv.getPublicInterfaceValue().equals("in");
						}
						
						workbench.getExtraction().addDataStructureToExtraction(onedatastr, ! isinputforsubmodel);

						if( ! isinputforsubmodel){
							Set<DataStructure> requiredinputs = onedatastr.getComputationInputs();											
							
							for(DataStructure oneinput : requiredinputs){
								
								if(!workbench.getExtraction().getDataStructuresToExtract().containsKey(oneinput)){
									workbench.getExtraction().addDataStructureToExtraction(oneinput, false);
								}
							}
						}
					}
				}
			}
		}
		Boolean first = true;
		
		// Collect the contents to preserve from the codewords checkboxes
		for (int x = 0; x < codewordspanel.checkboxpanel.getComponentCount(); x++) {
			
			if (codewordspanel.checkboxpanel.getComponent(x) instanceof ExtractorJCheckBox) {
				ExtractorJCheckBox tempbox = (ExtractorJCheckBox) codewordspanel.checkboxpanel.getComponent(x);
				
				if (tempbox.isSelected()) {
					
					for (DataStructure onedatastr : tempbox.associateddatastructures) {
						outputspreserveddatastructures.add(onedatastr);
					}
					
					if (first) {
						codewordspanel.checkboxpanel.getComponent(x).getName();
						first = false;
					}
				}
			}
		}

		for (DataStructure pds : outputspreserveddatastructures) {
			// If the full dependency chain is requested
			if (this.extractionlevelchooser2.isSelected()) {
				for (DataStructure dstokeep : workbench.getDataStructureDependencyChain(pds)) {
					workbench.getExtraction().addDataStructureToExtraction(dstokeep, true);
				}
			}
			// If only the immediate inputs are requested
			else {
				Set<DataStructure> tempdsset = pds.getComputationInputs();
				workbench.getExtraction().addDataStructureToExtraction(pds, true);
				
				for (DataStructure oneinput : tempdsset) {
					
					if (! workbench.getExtraction().getDataStructuresToExtract().containsKey(oneinput)) {
						workbench.getExtraction().addDataStructureToExtraction(oneinput, false);
					} 
					else if (workbench.getExtraction().getDataStructuresToExtract().get(oneinput)==false) {
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
						
						if (!workbench.getExtraction().getDataStructuresToExtract().containsKey(onedatastr)) {
							
							if(onedatastr.getComputation()!=null)
								workbench.getExtraction().addDataStructureToExtraction(onedatastr, true);
						}
						// If the data structure was added as an input but it should be an output, make it an output
						else if (workbench.getExtraction().getDataStructuresToExtract().get(onedatastr)==false) {
							workbench.getExtraction().getDataStructuresToExtract().remove(onedatastr);
							workbench.getExtraction().addDataStructureToExtraction(onedatastr, true);
						}
					}
					for (DataStructure onedatastr : tempbox.associateddatastructures) {
							moduleinputs.addAll(onedatastr.getComputationInputs());
					}
				}
			}
		}
		for (DataStructure ds : moduleinputs) {
			
			if (! workbench.getExtraction().getDataStructuresToExtract().containsKey(ds)) 
				workbench.getExtraction().addDataStructureToExtraction(ds, false);
		}
		// Make sure all the state variable derivative terms are included, include their inputs
		processStateVariables();
		
		return workbench.getExtraction();
	}
	
	
	// Add the data structures associated with a process's participants
	public void addParticipantsToExtractionMap(PhysicalProcess process) {
		
		// Add data structures associated with the participants in the process
		// Add entities to extraction
		for(PhysicalEntity ent : process.getParticipants()){
			workbench.getExtraction().addEntityToExtract(ent, true);
			
			if(entitiespanel.termandcdwdsmap.containsKey(ent)){
				for(DataStructure entds : entitiespanel.termandcdwdsmap.get(ent)){
					
					workbench.getExtraction().addDataStructureToExtraction(entds, true);
					
					// Add the entity's inputs, make them terminal
					for(DataStructure oneentin : entds.getComputationInputs()){
						if(! workbench.getExtraction().getDataStructuresToExtract().containsKey(oneentin)){
							workbench.getExtraction().addDataStructureToExtraction(oneentin, false);
						}
					}
				}
			}
		}
	}
	
	

	public void processStateVariables() {
		Map<DataStructure, Boolean> tempmap = new HashMap<DataStructure, Boolean>();
		for(DataStructure ds : workbench.getExtraction().getDataStructuresToExtract().keySet()){
			
			if(ds.hasSolutionDomain()){
				
				if(ds.hasStartValue() && workbench.getExtraction().getDataStructuresToExtract().get(ds)
						&& semsimmodel.getAssociatedDataStructure(ds.getName() + ":" + ds.getSolutionDomain().getName())!=null){
					
					tempmap.put(ds, true);
					
					// Assumes that all inputs to the state variable are derivatives with respect to some solution domain
					// This is the way the SemSim model is structured, but is different from what is in the XMML
					for(DataStructure inds : ds.getComputationInputs()){
						tempmap.put(inds, true);
						
						// Preserve the inputs to the derivative term. If not already preserved, make them static inputs
						for(DataStructure ininds : inds.getComputationInputs())
							
							if (! workbench.getExtraction().getDataStructuresToExtract().containsKey(ininds))
								tempmap.put(ininds, false);
					}
				}
			}
		}
		workbench.getExtraction().getDataStructuresToExtract().putAll(tempmap);
	}
	
	
	// Visualize a graph of the SemSim model
	public void visualize(Extraction extraction, Boolean clusteringonly) {
		
		if (! extraction.isEmpty()) {

			if (!clusteringonly) {
				physiomappanel.removeAll();
				physiomappanel.validate();
				physiomappanel.repaint();
			}
			if (tempgraph != null) {
				tempgraph.dispose();
			}
			if(physiomapgraph !=null){
				physiomapgraph.dispose();
			}
			
			tempgraph = new Graph(true);
			physiomapgraph = new Graph(true);
			junggraph = new SparseMultigraph<String, Number>();
			tempgraph.getNodeTable().addColumns(LABEL_SCHEMA);
			tempgraph.getNodeTable().addColumns(INPUT_SCHEMA);
			tempgraph.getNodeTable().addColumns(VAR2INPUT_SCHEMA);
			tempgraph.getNodeTable().addColumns(TOOLTIP_SCHEMA);
			physiomapgraph.getNodeTable().addColumns(LABEL_SCHEMA);
			physiomapgraph.getNodeTable().addColumns(PROCESS_SCHEMA);
			
			// Create the computational dependency graph first
			int edgenum = 1;
			Set<PhysicalProcess> PMprocesses = new HashSet<PhysicalProcess>();
			
			for (DataStructure keptds : extraction.getDataStructuresToExtract().keySet()) {
				
				if(keptds.getPhysicalProperty()!=null && extraction.getDataStructuresToExtract().get(keptds)){
					if(keptds.getAssociatedPhysicalModelComponent() instanceof PhysicalProcess){
						PMprocesses.add((PhysicalProcess) keptds.getAssociatedPhysicalModelComponent());
					}
				}
				Node node = null;
				Boolean add = true;
				Iterator<Node> nodeit = tempgraph.nodes();
				
				// check if the individual has already been added to the graph
				while (nodeit.hasNext()) {
					Node thenode = (Node) nodeit.next();
					if (keptds.getName().equals(thenode.getString(LABEL))) {
						node = thenode;
						add = false;
					}
				}
				
				Set<DataStructure> domaincodewords = new HashSet<DataStructure>();
				for(DataStructure ds : semsimmodel.getSolutionDomains()){
					domaincodewords.add(semsimmodel.getAssociatedDataStructure(ds.getName() + ".min"));
					domaincodewords.add(semsimmodel.getAssociatedDataStructure(ds.getName() + ".max"));
					domaincodewords.add(semsimmodel.getAssociatedDataStructure(ds.getName() + ".delta"));
				}

				// If the data structure is part of a solution domain declaration and it is not used to compute any other terms, ignore it
				if ((keptds.isSolutionDomain()
					|| (domaincodewords.contains(keptds)) && keptds.getUsedToCompute().isEmpty()))
						add =false;

				if (add) {
					node = tempgraph.addNode();
					node.setString(LABEL, keptds.getName());
					node.setBoolean(INPUT, extraction.isInputForExtraction(keptds));
					node.setBoolean(VAR2INPUT, extraction.isVariableConvertedToInput(keptds));
					node.setString(TOOLTIP, getToolTipForDataStructure(keptds));

					junggraph.addVertex(keptds.getName());
				}
				
				// If we are preserving the inputs to the data structure, add them to the graph as needed
				if(workbench.getExtraction().getDataStructuresToExtract().get(keptds)==true){
					
					for (DataStructure oneinput : keptds.getComputationInputs()) {
	
						Boolean terminalinput = extraction.isInputForExtraction(oneinput);
						Boolean convertedtoinput = extraction.isVariableConvertedToInput(oneinput);
						
						Boolean addtograph = true;
						Node othernode = null;
						Iterator<Node> othernodeit = tempgraph.nodes();
						
						// check if the input has already been added to the graph
						while (othernodeit.hasNext()) {
							Node theothernode = (Node) othernodeit.next();
							if (oneinput.getName().equals(theothernode.getString(LABEL))) {
								addtograph = false;
								othernode = theothernode;
							}
						}
						
						if (addtograph) {
							othernode = tempgraph.addNode();
							othernode.setString(LABEL, oneinput.getName());
							othernode.setBoolean(INPUT, terminalinput);
							othernode.setBoolean(VAR2INPUT, convertedtoinput);
							othernode.setString(TOOLTIP, getToolTipForDataStructure(oneinput));
							junggraph.addVertex(oneinput.getName());
						}
						tempgraph.addEdge(node, othernode);
						junggraph.addEdge(edgenum, oneinput.getName(), keptds.getName(), EdgeType.UNDIRECTED);
						edgenum++;
					}
				}
			}
			
			// Create the PhysioMap
			for(PhysicalProcess proc : PMprocesses){
				Node pnode = physiomapgraph.addNode();
				pnode.setString(LABEL, proc.getName());
				pnode.setBoolean(PROCESS, true);
				for(PhysicalEntity ent : proc.getSourcePhysicalEntities()){
					Node srcnode = retrieveNodeByLabel(physiomapgraph, ent.getName());
					srcnode.setBoolean(PROCESS, false);
					physiomapgraph.addEdge(pnode, srcnode);
				}
				for(PhysicalEntity ent : proc.getSinkPhysicalEntities()){
					Node snknode = retrieveNodeByLabel(physiomapgraph, ent.getName());
					snknode.setBoolean(PROCESS, false);
					physiomapgraph.addEdge(snknode, pnode);
				}
				for(PhysicalEntity ent : proc.getMediatorPhysicalEntities()){
					Node mdtrnode = retrieveNodeByLabel(physiomapgraph, ent.getName());
					mdtrnode.setBoolean(PROCESS, false);
					physiomapgraph.addEdge(mdtrnode, pnode);
				}
				if(proc.getSources().isEmpty()){
					Node nullsource = physiomapgraph.addNode();
					nullsource.setString(LABEL, "...");
					nullsource.setBoolean(PROCESS, false);
					physiomapgraph.addEdge(nullsource, pnode);
				}
				if(proc.getSinks().isEmpty()){
					Node nullsink = physiomapgraph.addNode();
					nullsink.setString(LABEL, "...");
					nullsink.setBoolean(PROCESS, false);
					physiomapgraph.addEdge(nullsink, pnode);
				}
			}

			if (!clusteringonly) {
				// display the graphs
				view = new SemGenRadialGraphView(settings, tempgraph, LABEL, semsimmodel);
				int selectedview = graphtabpane.getSelectedIndex();
				graphtabpane.removeAll();
				graphtabpane.add("Computational network", view.demo(tempgraph, LABEL, this));
				if(selectedview!=-1)
					graphtabpane.setSelectedIndex(selectedview);
				else graphtabpane.setSelectedIndex(0);
			}
		}
		// If there are no codewords to visualize
		else{
			tempgraph = new Graph(true);
			tempgraph.getNodeTable().addColumns(LABEL_SCHEMA);
			view = new SemGenRadialGraphView(settings, tempgraph, LABEL, semsimmodel);
			graphtabpane.removeAll();
			graphtabpane.add("Computational network", view.demo(tempgraph, LABEL, this));
		}
	}
	
	
	public String getToolTipForDataStructure(DataStructure ds) {
		String code = "";
		if(ds.getComputation()!=null)
			code = ds.getComputation().getComputationalCode();
		String units = "dimensionless";
		if(ds.getUnit()!=null) units = ds.getUnit().getComputationalCode();
		String tip = "<html>";
		if(ds.getDescription()!=null)
			tip = tip + ("<p><b>Description:</b> " + ds.getDescription() + "</p>");
		if(ds.hasPhysicalProperty()){
			if(ds.getAssociatedPhysicalModelComponent()!=null)
				tip = tip + ("<p><b>Composite annotation:</b> " + ds.getCompositeAnnotationAsString(false) + "</p>");
		}
		tip = tip + ("<p><b>Equation:</b> " + code + "</p>");
		tip = tip + ("<p><b>Units:</b> " + units + "</p></html>");
		return tip;
	}

	public Node retrieveNodeByLabel(Graph g, String label){
		Boolean otheradd = true;
		Node othernode = null;
		Iterator<Node> othernodeit = g.nodes();
		// check if the input has already been added to the graph
		while (othernodeit.hasNext()) {
			Node theothernode = (Node) othernodeit.next();
			if (label.equals(theothernode.getString(LABEL))) {
				otheradd = false;
				othernode = theothernode;
			}
		}
		if(otheradd){
			othernode = g.addNode();
			othernode.setString(LABEL, label);
		}
		return othernode;
	}

	public void AlphabetizeCheckBoxes(JPanel panel) {
		// First put all the check boxes in an array so they can be sorted
		ArrayList<ExtractorJCheckBox> boxset = new ArrayList<ExtractorJCheckBox>();

		for (Component onecomp : Arrays.asList(panel.getComponents())) {
			if (onecomp instanceof ExtractorJCheckBox) {
				boxset.add((ExtractorJCheckBox) onecomp);
				panel.remove(onecomp);
			}
		}
		// Sort the array alphabetically using the custom Comparator
		Collections.sort(boxset, new ComparatorByName());
		
		for (ExtractorJCheckBox box : boxset) {
			panel.add(box);
			box.setVisible(true);
		}
	}

	public void actionPerformed(ActionEvent e){
		Object o = e.getSource();
		if (o == extractbutton) {
			
			primeextraction();
			
			if ( ! workbench.getExtraction().isEmpty()) {
				
				// Choose file location for extraction
				String selectedext = "owl";  // Default extension type
				int modtype = semsimmodel.getSourceModelType();
				
				if(modtype==ModelClassifier.MML_MODEL_IN_PROJ || modtype==ModelClassifier.MML_MODEL) selectedext = "proj";
				else if(modtype==ModelClassifier.CELLML_MODEL) selectedext = "cellml";
				
				// Create the extracted semsim model
				try {
					extractedmodel = workbench.getExtraction().extractToNewModel();
				} catch (CloneNotSupportedException e2) {
					e2.printStackTrace();
				}
				
				SemGenSaveFileChooser filec = new SemGenSaveFileChooser(new String[]{"owl", "proj", "cellml"}, selectedext);
				
				ModelAccessor ma = filec.SaveAsAction(extractedmodel);
				
				if (ma != null)
					SaveSemSimModel.writeToFile(extractedmodel, ma, ma.getFileThatContainsModel(), filec.getFileFilter());
			} 
			else
				SemGenError.showError("Nothing to extract because no check boxes selected in extraction panels", "Extraction Error");
		}

		if (o == vizsourcebutton) visualizeAllDataStructures(false);

		// If user hits the "Cluster" button
		if (o == clusterbutton) {
			clusterpanel.scroller.setVisible(true);
			if(cd != null){ // If clusterer already opened once
				cd.setVisible(true);
			}
			else{
				visualizeAllDataStructures(true);
				try {
					cd = new Clusterer(junggraph, this);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public void visualizeAllDataStructures(boolean clusteringonly) {
		
		workbench.getExtraction().reset();
		
		for(DataStructure ds : semsimmodel.getAssociatedDataStructures()){
			workbench.getExtraction().addDataStructureToExtraction(ds, true);
		}
		visualize(workbench.getExtraction(), clusteringonly);
	}
	

	public void batchCluster() throws IOException {
		visualizeAllDataStructures(false);
		cd = new Clusterer(junggraph, this);
		cd.setVisible(false);

		JFileChooser filec = new JFileChooser();
		filec.setPreferredSize(new Dimension(550,550));
		Boolean saveok = false;
		File batchclusterfile = null;
		while (!saveok) {
			filec.setCurrentDirectory(SemGenFileChooser.currentdirectory);
			filec.setDialogTitle("Choose location to save clustering results");
			filec.addChoosableFileFilter(new FileFilter(new String[] { "txt" }));
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
        public BatchClusterTask(){
	        	progframe = new SemGenProgressBar("Performing clustering analysis...", false);
	    }
	    @Override
	    public Void doInBackground() {
    		try {
				performClusteringAnalysis();
			} catch (IOException e) {
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
			float maxclusteringiterations = cd.mygraph.getEdgeCount() - statevars;
	
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

				progframe.setProgressValue(Math.round(100 * ((float)y / maxclusteringiterations)));
			}
			clusterwriter.flush();
			clusterwriter.close();
        }
        
        public void endTask() {
        	progframe.dispose();
        	JOptionPane.showMessageDialog(null, "Finished clustering analysis");
        }
    }

	// Update whenever the set of items included in the extraction changes
	public void itemStateChanged(ItemEvent arg0) {
		try {
			visualize(primeextraction(), false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isSaved() {
		return true;
	}

	@Override
	public void requestSave() {}

	@Override
	public void requestSaveAs() {}
}
