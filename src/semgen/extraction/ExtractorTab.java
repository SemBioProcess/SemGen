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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import edu.uci.ics.jung.graph.SparseMultigraph;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.encoding.Encoder;
import semgen.extraction.RadialGraph.Clusterer;
import semgen.extraction.RadialGraph.SemGenRadialGraphView;
import semgen.extraction.workbench.ExtractorWorkbench;
import semgen.utilities.ComparatorByName;
import semgen.utilities.GenericThread;
import semgen.utilities.SemGenError;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;
import semgen.utilities.SemGenTask;
import semgen.utilities.file.FileFilter;
import semgen.utilities.file.SemGenFileChooser;
import semgen.utilities.file.SemGenOpenFileChooser;
import semgen.utilities.file.SemGenSaveFileChooser;
import semgen.utilities.uicomponent.SemGenProgressBar;
import semgen.utilities.uicomponent.SemGenTab;
import semsim.extraction.Extractor;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.Submodel;
import semsim.model.physical.object.PhysicalPropertyinComposite;
import semsim.utilities.SemSimUtil;
import semsim.writing.MMLwriter;
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
	
	public File sourcefile;
	public File extractedfile;
	public File autogendirectory;
	public SemSimModel extractedmodel;
	
	public SemGenRadialGraphView view;
	public Graph tempgraph;
	public Graph physiomapgraph;
	public SparseMultigraph<String, Number> junggraph;

	public Map<DataStructure, Set<? extends DataStructure>> allpreserveddatastructures;
	public Hashtable<PhysicalEntity, Set<DataStructure>> entsanddatastrs = new Hashtable<PhysicalEntity, Set<DataStructure>>();
	public Hashtable<PhysicalProcess, Set<DataStructure>> processesanddatastrs = new Hashtable<PhysicalProcess, Set<DataStructure>>();
	public Clusterer cd;
	public PrintWriter clusterwriter;
	public ExtractorToolbar toolbar;
	private ExtractorWorkbench workbench;
	
	public ExtractorTab(SemGenSettings sets, GlobalActions gacts, ExtractorWorkbench bench) {
		super(bench.getCurrentModelName(), SemGenIcon.extractoricon, "Extracting from " + bench.getCurrentModelName(), sets, gacts);
		settings = sets;
		workbench = bench;
		sourcefile = workbench.getSourceFile();
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
		includepartipantscheckbox.setSelected(true);
		includepartipantscheckbox.addItemListener(this);

		extractionlevelchooser2.setFont(SemGenFont.defaultPlain(-2));
		extractionlevelchooser2.addItemListener(this);

		clusterbutton.addActionListener(this);
		
		// List entities first because listprocesses needs data in entitiespanel
		entitiespanel = new ExtractorSelectionPanel(this, "Entities", listentities(), null);
		
		processespanel = new ExtractorSelectionPanel(this, "Processes", listprocesses(), includepartipantscheckbox);
		submodelspanel = new ExtractorSelectionPanel(this, "Sub-models", listsubmodels(), null);
		codewordspanel = new ExtractorSelectionPanel(this, "Codewords", listcodewords(), extractionlevelchooser2);
		Hashtable<PhysicalModelComponent,Set<DataStructure>> temp = new Hashtable<PhysicalModelComponent,Set<DataStructure>>();
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
							globalactions.NewAnnotatorTab(sourcefile);
						} catch (Exception e1) {e1.printStackTrace();} 
					}	
			}
		};
	}
	
	// List physical processes
	public Hashtable<PhysicalProcess, Set<DataStructure>> listprocesses() {
		Set<DataStructure> propandproc = semsimmodel.getDataStructureswithPhysicalProcesses();
		// List physical properties of processes
		for(DataStructure ds : propandproc){
			PhysicalProcess proc = (PhysicalProcess)ds.getAssociatedPhysicalModelComponent();
			Set<DataStructure> cdwds = new HashSet<DataStructure>();
			if(!processesanddatastrs.containsKey(proc)){
				cdwds.add(ds);
				processesanddatastrs.put(proc, cdwds);
			}
			// Otherwise we already added the process to the process-datastructure map, add the current property
			else
				processesanddatastrs.get(proc).add(ds);
		}
		return processesanddatastrs;
	}
	
	// List physical entities
	public Hashtable<PhysicalEntity, Set<DataStructure>> listentities() {
		Set<DataStructure> propandent = semsimmodel.getDataStructureswithCompositesEntities();
		for(DataStructure ds : propandent){
			PhysicalPropertyinComposite prop = ds.getPhysicalProperty();
			PhysicalEntity ent = (PhysicalEntity)ds.getAssociatedPhysicalModelComponent();
			if(entsanddatastrs.containsKey(ent)){
				entsanddatastrs.get(ent).add(ds);
			}
			else{
				Set<DataStructure> cdwds = new HashSet<DataStructure>();
				cdwds.add(ds);
				entsanddatastrs.put(ent, cdwds);
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
		for(DataStructure ds : semsimmodel.getAssociatedDataStructures()){
			Set<DataStructure> dsset = new HashSet<DataStructure>();
			dsset.add(ds);
			table.put(ds, dsset);
		}
		return table;
	}

	// returns the preserved data structures in the extract
	public Map<DataStructure, Set<? extends DataStructure>> primeextraction() throws IOException, OWLException {
		allpreserveddatastructures = new HashMap<DataStructure, Set<? extends DataStructure>>(); 
		Set<DataStructure> outputspreserveddatastructures = new HashSet<DataStructure>();

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
						Set<DataStructure> requiredinputs = onedatastr.getComputationInputs();
						allpreserveddatastructures.put(onedatastr, requiredinputs);						
						
						// Un-comment this block and comment out "includeInputsInExtraction", above, to make the extraction more limited
						for(DataStructure oneinput : requiredinputs){
							if(!allpreserveddatastructures.containsKey(oneinput))
								allpreserveddatastructures.put(oneinput, new HashSet<DataStructure>());
						}
						
						// If user wants to include the process participants
						if (includepartipantscheckbox.isSelected()){
							includeProcessParticipants((PhysicalProcess)tempbox.pmc);
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
						allpreserveddatastructures.put(onedatastr, onedatastr.getComputationInputs()); 
						// If the more thorough extraction is used, trace the data structure's use in other equations and preserve their output data structures as well
						if (this.extractionlevelchooserentities.isSelected()) 
							includeInputsInExtraction(onedatastr);
						else{
							// Add the data structure's inputs, make them terminal
							for(DataStructure onein : onedatastr.getComputationInputs()){
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
							requiredinputs.addAll(onedatastr.getComputationInputs());
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
		Boolean first = true;
		// Collect the data structures to preserve from the codewords checkboxes
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
				for (DataStructure dstokeep : getDependencyChain(pds)) {
					allpreserveddatastructures.put(dstokeep, dstokeep.getComputationInputs());
				}
			}
			// If only the immediate inputs are requested
			else {
				Set<DataStructure> tempdsset = pds.getComputationInputs();
				allpreserveddatastructures.put(pds, tempdsset);
				for (DataStructure oneinput : tempdsset) {
					if (!allpreserveddatastructures.containsKey(oneinput)) {
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
								allpreserveddatastructures.put(onedatastr, onedatastr.getComputationInputs());
						}
						// If the data structure was added as an input but it should be an output, make it an output
						else if (allpreserveddatastructures.get(onedatastr).isEmpty()) {
							allpreserveddatastructures.remove(onedatastr);
							allpreserveddatastructures.put(onedatastr, onedatastr.getComputationInputs());
						}
					}
					for (DataStructure onedatastr : tempbox.associateddatastructures) {
							moduleinputs.addAll(onedatastr.getComputationInputs());
					}
				}
			}
		}
		for (DataStructure ds : moduleinputs) {
			if (!allpreserveddatastructures.keySet().contains(ds)) allpreserveddatastructures.put(ds, new HashSet<DataStructure>());
		}
		// Make sure all the state variable derivative terms are included, include their inputs
		processStateVariables();
		
		return allpreserveddatastructures;
	}
	
	public void includeProcessParticipants(PhysicalProcess pmc) {
		// Add data structures associated with the participants in the process
		for(PhysicalEntity ent : pmc.getParticipants()){
			if(entitiespanel.termandcdwdstable.containsKey(ent)){
				for(DataStructure entds : entitiespanel.termandcdwdstable.get(ent)){
					// Maybe change so that if a cdwd that we're including is dependent on another that's
					// a participant, make sure to include its inputs (all inputs?)
					allpreserveddatastructures.put(entds, entds.getComputationInputs());
					// Add the entity's inputs, make them terminal
					for(DataStructure oneentin : entds.getComputationInputs()){
						if(!allpreserveddatastructures.containsKey(oneentin)){
							allpreserveddatastructures.put(oneentin, new HashSet<DataStructure>());
						}
					}
				}
			}
		}
	}
	
	public void includeInputsInExtraction(DataStructure onedatastr) throws OWLException{
		for (DataStructure nextds : onedatastr.getComputationInputs()) {
			allpreserveddatastructures.put(nextds, nextds.getComputationInputs());
			for(DataStructure secondaryds : nextds.getComputationInputs()){
				if (!allpreserveddatastructures.containsKey(secondaryds)) {
					allpreserveddatastructures.put(secondaryds, new HashSet<DataStructure>());
				}
			}
		}
	}

	public void processStateVariables() {
		Map<DataStructure, Set<DataStructure>> tempmap = new HashMap<DataStructure, Set<DataStructure>>();
		for(DataStructure ds : allpreserveddatastructures.keySet()){
			if(ds.hasSolutionDomain()){
				if(ds.hasStartValue() && !allpreserveddatastructures.get(ds).isEmpty()
						&& semsimmodel.getAssociatedDataStructure(ds.getName() + ":" + ds.getSolutionDomain().getName())!=null){
					tempmap.put(ds, ds.getComputationInputs());
					// Assumes that all inputs to the state variable are derivatives with respect to some solution domain
					// This is the way the SemSim model is structured, but is different from what is in the XMML
					for(DataStructure inds : ds.getComputationInputs()){
						tempmap.put(inds, inds.getComputationInputs());
						// Preserve the inputs to the derivative term. If not already preserved, make them static inputs
						for(DataStructure ininds : inds.getComputationInputs())
							if (!allpreserveddatastructures.keySet().contains(ininds))
								tempmap.put(ininds, new HashSet<DataStructure>());
					}
				}
			}
		}
		allpreserveddatastructures.putAll(tempmap);
	}
	
	public void optionToEncode(String filenamesuggestion) {
		int x = JOptionPane.showConfirmDialog(this, "Finished extracting "
				+ extractedfile.getName()
				+ "\nGenerate simulation code from extracted model?", "",
				JOptionPane.YES_NO_OPTION);
		if (x == JOptionPane.YES_OPTION) {
			try {
				new Encoder(extractedmodel, filenamesuggestion);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Set<DataStructure> getDependencyChain(DataStructure startds){
		// The hashtable contains the data structure URIs and whether the looping alogrithm here should collect 
		// their inputs (true = collect)
		Hashtable<DataStructure, Boolean> table = new Hashtable<DataStructure, Boolean>();
		table.put(startds, true);
		DataStructure key = null;
		Boolean cont = true;
		while (cont) {
			cont = false; //This aborts the loop immediately! What is the point of this????????????
			for (DataStructure onekey : table.keySet()) {
				key = onekey;
				if ((Boolean) table.get(onekey) == true) {
					cont = true;
					for (DataStructure oneaddedinput : onekey.getComputationInputs()) {
						if (!table.containsKey(oneaddedinput)) {
							table.put(oneaddedinput, !oneaddedinput.getComputationInputs().isEmpty());
						}
					}
					break;
				}
			}
			table.remove(key);
			table.put(key, false);
		}
		Set<DataStructure> dsset = new HashSet<DataStructure>(table.keySet());
		
		return dsset;
	}
	
	// Visualize a graph of the SemSim model
	public void visualize(Map<DataStructure, Set<? extends DataStructure>> alldatastrs, Boolean clusteringonly) {
		if (!alldatastrs.keySet().isEmpty()) {

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
			for (DataStructure keptds : alldatastrs.keySet()) {
				if(keptds.getPhysicalProperty()!=null && !alldatastrs.get(keptds).isEmpty()){
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
					node.setBoolean(INPUT, (alldatastrs.get(keptds).isEmpty()));
					boolean recievesInput = false;
					if(keptds.getComputation()!=null)
						recievesInput = !keptds.getComputationInputs().isEmpty();
					else if(keptds instanceof MappableVariable)
						recievesInput = !((MappableVariable)keptds).getMappedTo().isEmpty();
					node.setBoolean(VAR2INPUT, (alldatastrs.get(keptds).isEmpty() && recievesInput));
					node.setString(TOOLTIP, getToolTipForDataStructure(keptds));

					junggraph.addVertex(keptds.getName());
				}
				for (DataStructure oneinput : alldatastrs.get(keptds)) {
					//if(oneinput.isDeclared()){
					Boolean otheradd = true;
					Node othernode = null;
					Iterator<Node> othernodeit = tempgraph.nodes();
					// check if the input has already been added to the graph
					while (othernodeit.hasNext()) {
						Node theothernode = (Node) othernodeit.next();
						if (oneinput.getName().equals(theothernode.getString(LABEL))) {
							otheradd = false;
							othernode = theothernode;
						}
					}
					if (otheradd) {
						othernode = tempgraph.addNode();
						othernode.setString(LABEL, oneinput.getName());
						othernode.setBoolean(INPUT, (alldatastrs.get(oneinput).isEmpty()));
						boolean recievesInput2 = false;
						if(oneinput.getComputation()!=null)
							recievesInput2 = !oneinput.getComputationInputs().isEmpty();
						else if(oneinput instanceof MappableVariable)
							recievesInput2 = !((MappableVariable)oneinput).getMappedTo().isEmpty();
						othernode.setBoolean(VAR2INPUT, (alldatastrs.get(oneinput).isEmpty() && recievesInput2));
						othernode.setString(TOOLTIP, getToolTipForDataStructure(oneinput));
						junggraph.addVertex(oneinput.getName());
					}
					tempgraph.addEdge(node, othernode);
					junggraph.addEdge(edgenum, oneinput.getName(), keptds.getName(), EdgeType.UNDIRECTED);
					edgenum++;
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
	
	
	String getToolTipForDataStructure(DataStructure ds) {
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
		Collections.sort(boxset, new ComparatorByName());
		// Sort the array alphabetically using the custom Comparator
		
		for (ExtractorJCheckBox box : boxset) {
			panel.add(box);
			box.setVisible(true);
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o == extractbutton) {
			try {
				Map<DataStructure, Set<? extends DataStructure>> table = primeextraction();
				if (table.size() != 0) {
					if (extractedfile != null) {
						try {
							extractedmodel = Extractor.extract(semsimmodel, table);
						} catch (CloneNotSupportedException e1) {
							e1.printStackTrace();
						}
						manager.saveOntology(extractedmodel.toOWLOntology(), new RDFXMLOntologyFormat(),IRI.create(extractedfile));
						optionToEncode(extractedfile.getName());
					}
				} else {
					SemGenError.showError("Nothing to extract because no check boxes selected in extraction panels", "Extraction Error");
				}
			} catch (IOException | OWLException e1) {
				e1.printStackTrace();
			}
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

	public void visualizeAllDataStructures(Boolean clusteringonly) {
		Hashtable<DataStructure, Set<? extends DataStructure>> alldatastrs = new Hashtable<DataStructure, Set<? extends DataStructure>>();
		for(DataStructure ds : semsimmodel.getAssociatedDataStructures()){
			if(ds.getComputation()!=null)
				alldatastrs.put(ds, ds.getComputationInputs());
			else if(ds instanceof MappableVariable)
				alldatastrs.put(ds, ((MappableVariable)ds).getMappedTo());
		}
		visualize(alldatastrs, clusteringonly);
	}
	
	public void atomicDecomposition() {
		SemGenOpenFileChooser fc = new SemGenOpenFileChooser("Select directory for extracted models", false);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		File file = new File(fc.getSelectedFile().getAbsolutePath());
		if (file != null) {
			autogendirectory = file;
			GenericThread task = new GenericThread(this, "decompose");
			task.start();
		}
	}

	public void decompose() {
		SemGenProgressBar progframe = new SemGenProgressBar("Performing decomposition", false, null);
		Component[][] setofcomponentsinpanels = new Component[][]{
				processespanel.checkboxpanel.getComponents(),
				entitiespanel.checkboxpanel.getComponents(),
				submodelspanel.checkboxpanel.getComponents(),
				codewordspanel.checkboxpanel.getComponents(),
				clusterpanel.checkboxpanel.getComponents()};
		
		// first, deselect all the checkboxes
		for(int n=0; n<5; n++){
			for(int a=0; a<setofcomponentsinpanels[n].length; a++){
				Component[] components = setofcomponentsinpanels[n];
				ExtractorJCheckBox ebox = (ExtractorJCheckBox) components[a];
				ebox.setSelected(false);
			}
		}
		
		for (int x = 0; x < setofcomponentsinpanels[1].length; x++) {
			ExtractorJCheckBox ebox = (ExtractorJCheckBox) setofcomponentsinpanels[1][x];
			if (x > 0) {
				ExtractorJCheckBox previousebox = (ExtractorJCheckBox) setofcomponentsinpanels[1][x-1];
				previousebox.setSelected(false);
			}
			ebox.setSelected(true);
			extractedfile = new File(autogendirectory.getAbsolutePath() + "/" + ebox.getText() + "_FROM_" + semsimmodel.getName());
			if (extractedfile != null) {
				File mmldir = new File(autogendirectory + "/" + "MML_output");
				mmldir.mkdir();
				File coderfile = new File(mmldir.getAbsolutePath() + "/" + extractedfile.getName() + ".mod");
				String out = null;
					out = new MMLwriter(extractedmodel).writeToString();
					SemSimUtil.writeStringToFile(out, coderfile);
			} else {
				System.out.println("ERROR in creating file during atomic decomposition");
			}
			progframe.bar.setValue(Math.round((100 * (x + 1)) / setofcomponentsinpanels[1].length));
		}

		// Deselect all entities
		for (int a = 0; a < setofcomponentsinpanels[1].length; a++) {
			ExtractorJCheckBox ebox = (ExtractorJCheckBox) setofcomponentsinpanels[1][a];
			ebox.setSelected(false);
		}
		JOptionPane.showMessageDialog(this, "Atomic decomposition completed");
		progframe.dispose();
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

				progframe.bar.setValue(Math.round(100 * ((float)y / maxclusteringiterations)));
			}
			clusterwriter.flush();
			clusterwriter.close();
        }
        
        public void endTask() {
        	JOptionPane.showMessageDialog(null, "Finished clustering analysis");
        }
    }

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
	
	public File saveExtraction() {
		SemGenSaveFileChooser filec = new SemGenSaveFileChooser("Choose location to save file", new String[]{"cellml","owl"});
		if (filec.SaveAsAction()!=null) {
			extractedfile = filec.getSelectedFile();
			return extractedfile;
		}
		return null;
	}
}
