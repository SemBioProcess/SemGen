package semgen.extraction;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.*;

import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Observer;
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
import semgen.resource.ComparatorByName;
import semgen.resource.GenericThread;
import semgen.resource.SemGenFont;
import semgen.resource.SemGenIcon;
import semgen.resource.SemGenTask;
import semgen.resource.file.FileFilter;
import semgen.resource.file.SemGenFileChooser;
import semgen.resource.file.SemGenSaveFileChooser;
import semgen.resource.uicomponent.SemGenProgressBar;
import semgen.resource.uicomponent.SemGenTab;
import semsim.SemSimUtil;
import semsim.extraction.Extractor;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.MappableVariable;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.PhysicalProperty;
import semsim.model.physical.Submodel;
import semsim.writing.MMLwriter;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

import java.awt.BorderLayout;

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
	public File sourcefile;
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
	public SemSimModel extractedmodel;
	public File extractedfile;
	public SemGenRadialGraphView view;
	public Graph tempgraph;
	public Graph physiomapgraph;
	public SparseMultigraph<String, Number> junggraph;

	public String initialfocusnodename;
	public Map<DataStructure, Set<? extends DataStructure>> allpreserveddatastructures;
	public Hashtable<PhysicalEntity, Set<DataStructure>> entsanddatastrs = new Hashtable<PhysicalEntity, Set<DataStructure>>();
	public Hashtable<DataStructure, PhysicalEntity> datastrsandents = new Hashtable<DataStructure, PhysicalEntity>();
	public Hashtable<PhysicalProcess, Set<DataStructure>> processesanddatastrs = new Hashtable<PhysicalProcess, Set<DataStructure>>();
	public Hashtable<DataStructure, PhysicalProcess> datastrsandprocesses = new Hashtable<DataStructure, PhysicalProcess>();
	public File autogendirectory;

	public Clusterer cd;
	public PrintWriter clusterwriter;

	public ExtractorTab(File srcfile, SemSimModel ssmodel, SemGenSettings sets, GlobalActions gacts) throws OWLException {
		super(srcfile.getName(), SemGenIcon.extractoricon, "Extracting from " + srcfile.getName(), sets, gacts);
		settings = sets;
		this.setLayout(new BorderLayout());
		this.semsimmodel = ssmodel;
		this.sourcefile = srcfile;

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

		JPanel leftpanel = new JPanel();
		leftpanel.add(new ExtractorToolbar(settings, this));
		leftpanel.setLayout(new BoxLayout(leftpanel, BoxLayout.Y_AXIS));
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
		initialfocusnodename = null;
		Boolean first = true;
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
						Set<DataStructure> requiredinputs = onedatastr.getComputation().getInputs();
						allpreserveddatastructures.put(onedatastr, requiredinputs);						
						
						// Un-comment this block and comment out "includeInputsInExtraction", above, to make the extraction more limited
						for(DataStructure oneinput : requiredinputs){
							if(!allpreserveddatastructures.containsKey(oneinput))
								allpreserveddatastructures.put(oneinput, new HashSet<DataStructure>());
							else{}
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
//										// Add the entity's inputs, make them terminal
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
		
		// Collect the data structures to preserve from the codewords checkboxes
		for (int x = 0; x < codewordspanel.checkboxpanel.getComponentCount(); x++) {
			if (codewordspanel.checkboxpanel.getComponent(x) instanceof ExtractorJCheckBox) {
				ExtractorJCheckBox tempbox = (ExtractorJCheckBox) codewordspanel.checkboxpanel.getComponent(x);
				if (tempbox.isSelected()) {
					for (DataStructure onedatastr : tempbox.associateddatastructures) {
						outputspreserveddatastructures.add(onedatastr);
					}
					if (first) {
						initialfocusnodename = codewordspanel.checkboxpanel.getComponent(x).getName();
						first = false;
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
		Set<DataStructure> dsset = new HashSet<DataStructure>();
		Hashtable<DataStructure, Boolean> table = new Hashtable<DataStructure, Boolean>();
		Set<DataStructure> keys = new HashSet<DataStructure>();
		Set<DataStructure> inputstoadd = new HashSet<DataStructure>();
		table.put(startds, true);
		DataStructure key = null;
		DataStructure[] array = {};
		Boolean cont = true;
		while (cont) {
			cont = false;
			keys = table.keySet();
			for (DataStructure onekey : keys) {
				key = onekey;
				if ((Boolean) table.get(onekey) == true) {
					cont = true;
					inputstoadd = onekey.getComputation().getInputs();
					for (DataStructure oneaddedinput : inputstoadd) {
						if (!keys.contains(oneaddedinput)) {
							table.put(oneaddedinput, !oneaddedinput.getComputation().getInputs().isEmpty());
						}
					}
					break;
				}
			}
			table.remove(key);
			table.put(key, false);
		}
		array = (DataStructure[]) table.keySet().toArray(array);
		for (int y = 0; y < array.length; y++) {
			dsset.add(array[y]);
		}
		return dsset;
	}
	
	// Visualize a graph of the SemSim model
	public void visualize(Map<DataStructure, Set<? extends DataStructure>> alldatastrs, Boolean clusteringonly) {
		if (!alldatastrs.keySet().isEmpty()) {

			Set<DataStructure> domaincodewords = new HashSet<DataStructure>();
			for(DataStructure ds : semsimmodel.getSolutionDomains()){
				domaincodewords.add(semsimmodel.getDataStructure(ds.getName() + ".min"));
				domaincodewords.add(semsimmodel.getDataStructure(ds.getName() + ".max"));
				domaincodewords.add(semsimmodel.getDataStructure(ds.getName() + ".delta"));
			}

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
					if(keptds.getPhysicalProperty().getPhysicalPropertyOf() instanceof PhysicalProcess){
						PMprocesses.add((PhysicalProcess) keptds.getPhysicalProperty().getPhysicalPropertyOf());
					}
				}
				Node node = null;
				Boolean add = true;
				Iterator<Node> nodeit = tempgraph.nodes();
				// check if the individual has already been added to the graph
				while (nodeit.hasNext()) {
					Node thenode = (Node) nodeit.next();
					if (keptds.getName().equals(thenode.getString(LABEL))) {
						add = false;
						node = thenode;
					}
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
						recievesInput = !keptds.getComputation().getInputs().isEmpty();
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
							recievesInput2 = !oneinput.getComputation().getInputs().isEmpty();
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
		String tip = "<html>";
		String code = "";
		if(ds.getComputation()!=null)
			code = ds.getComputation().getComputationalCode();
		String units = "dimensionless";
		if(ds.getUnit()!=null) units = ds.getUnit().getComputationalCode();
		if(ds.getDescription()!=null)
			tip = tip + ("<p><b>Description:</b> " + ds.getDescription() + "</p>");
		if(ds.hasPhysicalProperty()){
			if(ds.getPhysicalProperty().getPhysicalPropertyOf()!=null)
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
		Component[] comparray = panel.getComponents();
		Set<ExtractorJCheckBox> boxset = new HashSet<ExtractorJCheckBox>();

		for (int i = 0; i < comparray.length; i++) {
			Component onecomp = comparray[i];
			if (onecomp instanceof ExtractorJCheckBox) {
				boxset.add((ExtractorJCheckBox) onecomp);
				panel.remove(onecomp);
			}
		}
		ExtractorJCheckBox[] boxarray = boxset.toArray(new ExtractorJCheckBox[] {});
		// Sort the array alphabetically using the custom Comparator

		Arrays.sort(boxarray, new ComparatorByName());

		for (int x = 0; x < boxarray.length; x++) {
			panel.add(boxarray[x]);
			boxarray[x].setVisible(true);
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
					JOptionPane.showMessageDialog(this,"Nothing to extract because no check boxes selected in extraction panels");
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
		for(DataStructure ds : semsimmodel.getDataStructures()){
			if(ds.getComputation()!=null)
				alldatastrs.put(ds, ds.getComputation().getInputs());
			else if(ds instanceof MappableVariable)
				alldatastrs.put(ds, ((MappableVariable)ds).getMappedTo());
		}
		visualize(alldatastrs, clusteringonly);
	}
	
	public void atomicDecomposition() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setCurrentDirectory(SemGenFileChooser.currentdirectory);
		fc.setDialogTitle("Select directory for extracted models");
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = new File(fc.getSelectedFile().getAbsolutePath());
			SemGenFileChooser.currentdirectory = fc.getCurrentDirectory();
			if (file != null) {
				autogendirectory = file;
				
				GenericThread task = new GenericThread(this, "decompose");
				task.start();
			}
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
					out = new MMLwriter().writeToString(extractedmodel);
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
        public BatchClusterTask(){}
        @Override
        public Void doInBackground() {
        	progframe = new SemGenProgressBar("Performing clustering analysis...", false);
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

			float fltmaxnum = maxclusteringiterations;

			progframe.bar.setValue(Math.round(100 * ((float)y / fltmaxnum)));
		}
		clusterwriter.flush();
		clusterwriter.close();
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
		// TODO Auto-generated method stub
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

	@Override
	public void addObservertoWorkbench(Observer obs) {
		// TODO Auto-generated method stub
		
	}
}