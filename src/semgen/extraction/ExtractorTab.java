package semgen.extraction;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
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
import semgen.ComparatorByName;
import semgen.FileFilter;
import semgen.GenericThread;
import semgen.ProgressFrame;
import semgen.SemGenGUI;
import semsim.SemSimUtil;
import semsim.extraction.Extractor;
import semsim.model.SemSimModel;
import semsim.model.computational.MappableVariable;
import semsim.model.computational.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.PhysicalProperty;
import semsim.model.physical.Submodel;
import semsim.writing.MMLwriter;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

import java.awt.BorderLayout;

public class ExtractorTab extends JPanel implements ActionListener, ItemListener {

	/**
	 * 
	 */
	public static final long serialVersionUID = -5142058482154697778L;
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
	

	public final JTabbedPane pane;
	public static int leftpanewidth = 400;
	public File sourcefile;
	public JButton sourcebutton;
	// public JButton annotatorbutton;
	public JButton vizsourcebutton;
	public JButton closebutton;
	public JButton clusterbutton;
	public JCheckBox extractionlevelchooserentities;
	public JCheckBox includepartipantscheckbox;
	public JCheckBox extractionlevelchooser2;
	public JComboBox vizchooser;
	public JButton extractbutton;
	public JTextField sourcetextfield;
	public JPanel toppanel;
	public JPanel leftpanel;
	public JPanel rightpanel;
	public ExtractorSelectionPanel processespanel;
	public ExtractorSelectionPanel entitiespanel;
	public ExtractorSelectionPanel submodelspanel;
	public ExtractorSelectionPanel codewordspanel;
	public ExtractorSelectionPanel clusterpanel;
	public JPanel physiomappanel;
	public JTabbedPane graphtabpane;
	public JSplitPane centersplitpane;
	//public OWLOntology ontology;
	public String base;
	public SemSimModel extractedmodel;
	public ExtractorJCheckBox checkbox;
	public File extractedfile;
	public SemGenRadialGraphView view;
	public Graph g;
	public Graph tempgraph;
	public Graph physiomapgraph;
	public SparseMultigraph<String, Number> junggraph;

	public JPanel graphpanel;
	public JPanel PMgraphpanel;
	public String initialfocusnodename;
	public Map<DataStructure, Set<? extends DataStructure>> allpreserveddatastructures;
	public Hashtable<PhysicalEntity, Set<DataStructure>> entsanddatastrs = new Hashtable<PhysicalEntity, Set<DataStructure>>();
	public Hashtable<DataStructure, PhysicalEntity> datastrsandents = new Hashtable<DataStructure, PhysicalEntity>();
	public Hashtable<PhysicalProcess, Set<DataStructure>> processesanddatastrs = new Hashtable<PhysicalProcess, Set<DataStructure>>();
	public Hashtable<DataStructure, PhysicalProcess> datastrsandprocesses = new Hashtable<DataStructure, PhysicalProcess>();
	public File autogendirectory;
	public float maxclusteringiterations;
	public ProgressFrame progframe;

	public Clusterer cd;
	public PrintWriter clusterwriter;
	public File clusterfile;

	public ExtractorTab(final JTabbedPane pane, SemSimModel semsimmodel, File sourcefile) throws OWLException {
		this.pane = pane;
		this.setLayout(new BorderLayout());
		this.semsimmodel = semsimmodel;
		this.sourcefile = sourcefile;

		base = semsimmodel.getNamespace();

		toppanel = new JPanel();
		toppanel.setLayout(new BorderLayout());
		toppanel.setOpaque(true);
		vizsourcebutton = new JButton("Show source model");
		vizsourcebutton.setFont(new Font("SansSerif", Font.PLAIN, SemGenGUI.defaultfontsize));
		vizsourcebutton.addActionListener(this);

		extractbutton = new JButton("EXTRACT");
		extractbutton.setForeground(Color.blue);
		extractbutton.setFont(new Font("SansSerif", Font.BOLD, SemGenGUI.defaultfontsize));
		//extractbutton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		extractbutton.addActionListener(this);
		closebutton = new JButton("Close tab");
		closebutton.setForeground(Color.blue);
		closebutton.setFont(new Font("SansSerif", Font.ITALIC, 11));
		closebutton.setBorderPainted(false);
		closebutton.setContentAreaFilled(false);
		closebutton.setOpaque(false);
		closebutton.addActionListener(this);
		closebutton.addMouseListener(buttonMouseListener);
		closebutton.setRolloverEnabled(true);
		JPanel buttonpanel = new JPanel();
		//buttonpanel.add(vizsourcebutton);
		//buttonpanel.add(extractbutton);
		buttonpanel.add(closebutton);
//		toppanel.add(Box.createGlue(), BorderLayout.WEST);
//		toppanel.add(buttonpanel, BorderLayout.EAST);
		//toppanel.setMaximumSize(new Dimension(999999,35));
		
		extractionlevelchooserentities = new JCheckBox("More inclusive");
		extractionlevelchooserentities.setFont(new Font("SansSerif",Font.PLAIN, SemGenGUI.defaultfontsize-2));
		extractionlevelchooserentities.setBorder(BorderFactory.createEmptyBorder(0,35,0,0));
		extractionlevelchooserentities.addItemListener(this);
		
		includepartipantscheckbox = new JCheckBox("Include participants");
		includepartipantscheckbox.setFont(new Font("SansSerif",Font.PLAIN, SemGenGUI.defaultfontsize-2));
		includepartipantscheckbox.setBorder(BorderFactory.createEmptyBorder(0,35,0,0));
		includepartipantscheckbox.setSelected(true);
		includepartipantscheckbox.addItemListener(this);

		extractionlevelchooser2 = new JCheckBox("Include full dependency chain");
		extractionlevelchooser2.setFont(new Font("SansSerif", Font.PLAIN,SemGenGUI.defaultfontsize - 2));
		extractionlevelchooser2.addItemListener(this);

		clusterbutton = new JButton("Cluster");
		clusterbutton.addActionListener(this);
		
		// List entities first because listprocesses needs data in entitiespanel
		//entitiespanel = new ExtractorSelectionPanel(this, "Entities", listentities(), extractionlevelchooserentities);
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
		
		// testGroupingQuery();

		leftpanel = new JPanel();
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

		graphtabpane = new JTabbedPane();
		physiomappanel = new JPanel();
		//graphtabpane.addTab("PhysioMap", physiomappanel);
		
		//vizpanel.setBackground(Color.white);
		rightpanel = new JPanel();
		rightpanel.setLayout(new BoxLayout(rightpanel,BoxLayout.Y_AXIS));
		//rightpanel.add(toppanel);
		rightpanel.add(graphtabpane);
		//rightpanel.add(Box.createGlue());
		centersplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftpanel, rightpanel);
		centersplitpane.setOneTouchExpandable(true);
		//add(toppanel, BorderLayout.NORTH);
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
			//if(ds.isDeclared()){
			Set<DataStructure> dsset = new HashSet<DataStructure>();
			dsset.add(ds);
			table.put(ds, dsset);
			//}
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
						
						//includeInputsInExtraction(onedatastr);
						
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
										//includeInputsInExtraction(entds);
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
							else{} //leave alone
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
//		for(DataStructure ds : tempmap.keySet()){
//			allpreserveddatastructures.remove(ds);
//			allpreserveddatastructures.put(ds, tempmap.get(ds));
//		}
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
				SemGenGUI.startEncoding(extractedmodel, filenamesuggestion);
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
					Iterator othernodeit = tempgraph.nodes();
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
					//}
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
				view = new SemGenRadialGraphView(tempgraph, LABEL, semsimmodel);
				PhysioMapRadialGraphView PMview = new PhysioMapRadialGraphView(physiomapgraph, LABEL, semsimmodel);
				int selectedview = graphtabpane.getSelectedIndex();
				graphtabpane.removeAll();
				graphtabpane.add("Computational network", view.demo(tempgraph, LABEL, this));
				//graphtabpane.add("PhysioMap",PMview.demo(physiomapgraph, LABEL));
				if(selectedview!=-1)
					graphtabpane.setSelectedIndex(selectedview);
				else graphtabpane.setSelectedIndex(0);
			}
		}
		// If there are no codewords to visualize
		else{
			tempgraph = new Graph(true);
			tempgraph.getNodeTable().addColumns(LABEL_SCHEMA);
			view = new SemGenRadialGraphView(tempgraph, LABEL, semsimmodel);
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
		Iterator othernodeit = g.nodes();
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
		Comparator<Component> byVarName = new ComparatorByName();
		Arrays.sort(boxarray, byVarName);

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
					extractedfile = SemGenGUI.SaveAsAction(SemGenGUI.desktop, null, new FileNameExtensionFilter[]{
							SemGenGUI.cellmlfilter, SemGenGUI.owlfilter});
					if (extractedfile != null) {
						try {
							extractedmodel = Extractor.extract(semsimmodel, table);
						} catch (CloneNotSupportedException e1) {e1.printStackTrace();}
						manager.saveOntology(extractedmodel.toOWLOntology(), new RDFXMLOntologyFormat(),IRI.create(extractedfile));
						optionToEncode(extractedfile.getName());
					}
				} else {
					JOptionPane.showMessageDialog(this,"Nothing to extract because no check boxes selected in extraction panels");
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (OWLException e2) {
				e2.printStackTrace();
			}
		}

		if (o == vizsourcebutton) visualizeAllDataStructures(false);

		if (o == closebutton){
			try {
				SemGenGUI.closeTabAction(this);
			} catch (HeadlessException e0) {
				e0.printStackTrace();
			}
		}

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

	public void closeAction() {
		
	}

	public final static MouseListener buttonMouseListener = new MouseAdapter() {
		public void mouseEntered(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(true);
				button.setContentAreaFilled(true);
				button.setOpaque(true);
			}
		}

		public void mouseExited(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(false);
				button.setContentAreaFilled(false);
				button.setOpaque(false);
			}
		}
	};

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
/*
	public void testGroupingQuery() {
		Set<String> entnames = new HashSet<String>();
		for(PhysicalEntity ent : semsimmodel.getPhysicalEntities()){
			if(ent.hasRefersToAnnotation()) entnames.add(ent.getFirstRefersToReferenceOntologyAnnotation().getDescription());
			else entnames.add(ent.getName());
		}

		String entquerystring = "";
		for (String oneent : entnames) {
			if (oneent.startsWith("http://sig.biostr.washington.edu/fma3.0#")) {
				entquerystring = entquerystring + "set:set1 set:member fma:" + SemSimOWLFactory.getIRIfragment(oneent) + ". ";
			}
		}
		String results = "";
		VSparQLService service = new VSparQLServiceProxy();
		try {
			results = service.executeQuery("PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
							+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
							+ "PREFIX owl:<http://www.w3.org/2002/07/owl#>"
							+ "PREFIX gleen:<java:edu.washington.sig.gleen.>"
							+ "PREFIX hier:<http://sig.biostr.washington.edu/hier#>"
							+ "PREFIX set:<http://sig.biostr.washington.edu/set#>"
							+ "PREFIX fma:<http://sig.biostr.washington.edu/fma3.0#>"
							+
							"CONSTRUCT { ?a hier:child_of ?c. }"
							+ "FROM <http://sig.biostr.washington.edu/fma3.0>"
							+ "FROM NAMED <input_list> [  " + "CONSTRUCT" + "{"
							+ entquerystring
							+
							/*
							 * "set:set1 set:member fma:Pulmonary_valve. "+
							 * "set:set1 set:member fma:Blood_in_left_ventricle. "
							 * + "set:set1 set:member fma:Mitral_valve. "+
							 * "set:set1 set:member fma:Wall_of_left_ventricle. "
							 * + "set:set1 set:member fma:Aortic_valve. "+
							 * "set:set1 set:member fma:Blood_in_left_atrium. "+
							 * "set:set1 set:member fma:Wall_of_left_atrium. "+
							 */
	/*
							"}"
							+ "FROM <http://sig.biostr.washington.edu/fma3.0>"
							+ "WHERE{}"
							+ "]"
							+ "FROM NAMED <temp_hierarchy> ["
							+ "CONSTRUCT "
							+ "{"
							+ "?x hier:temp ?z. "
							+ "}"
							+ "FROM NAMED <input_list> "
							+ "FROM <http://sig.biostr.washington.edu/fma3.0> "
							+ "WHERE"
							+ "{"
							+ "GRAPH <input_list> {set:set1 set:member ?a.} "
							+ "(?a \"(([fma:constitutional_part_of]|[fma:regional_part_of]))+\" ?b) gleen:Subgraph (?x ?y ?z). "
							+
							// |[fma:regional_part_of]|[fma:contained_in]|[fma:subClassOf]
							"}"
							+ "] "
							+ "FROM NAMED <inter_hierarchy> [ "
							+ "CONSTRUCT {?a ?b ?c. hier:sink hier:perm ?x.} "
							+ "FROM <temp_hierarchy> "
							+ "WHERE"
							+ "{"
							+ "{?a ?b ?c.} "
							+ "UNION"
							+ "{"
							+ "?x ?y ?z. "
							+ "OPTIONAL {?child ?rel ?x.} "
							+ "FILTER(!bound(?child)). "
							+ "}"
							+ "} "
							+ "UNION"
							+ "CONSTRUCT {?child hier:perm ?parent.?child_prime hier:temp ?parent_prime} "
							+ "FROM NAMED <inter_hierarchy> "
							+ "WHERE"
							+ "{"
							+ "GRAPH <inter_hierarchy> "
							+ "{"
							+
							// "#case1: parent has other children"+
							"{"
							+ "?x hier:perm ?child. "
							+ "?child hier:temp ?parent. "
							+ "OPTIONAL {?y hier:perm ?child2. ?child2 hier:temp ?parent.FILTER(?child!=?child2)} "
							+ "FILTER(bound(?child2)). "
							+ "}"
							+

							// "#case2: parent has no other children"+
							"UNION"
							+ "{"
							+ "?x2 hier:perm ?child_prime. "
							+ "?child_prime hier:temp ?temp_parent. "
							+ "OPTIONAL {?child2_prime hier:temp ?temp_parent.FILTER(?child_prime!=?child2_prime)}"
							+ "FILTER(!bound(?child2_prime)). "
							+ "?temp_parent hier:temp ?parent_prime "
							+ "}"
							+ "}"
							+ "}"
							+ "]"
							+ "WHERE"
							+ "{"
							+ "GRAPH <inter_hierarchy> {?a hier:perm ?c. FILTER(?a!=hier:sink)} "
							+ "}");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		// System.out.println(results);
		File testfile = new File("temp/test.txt");
		Scanner scanner = new Scanner(results);
		PrintWriter outfile = null;
		try {
			outfile = new PrintWriter(new FileWriter(testfile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (scanner.hasNextLine()) {
			String nextline = scanner.nextLine();
			outfile.println(nextline);
		}
		outfile.close();
		ExtractorEntitiesTree tree = new ExtractorEntitiesTree(testfile);
		entitiespanel.checkboxpanel = tree;
	}
	*/

	
	public void atomicDecomposition() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setCurrentDirectory(SemGenGUI.currentdirectory);
		fc.setDialogTitle("Select directory for extracted models");
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = new File(fc.getSelectedFile().getAbsolutePath());
			SemGenGUI.currentdirectory = fc.getCurrentDirectory();
			if (file != null) {
				autogendirectory = file;
				progframe = new ProgressFrame("Performing decomposition", false, null);
				GenericThread task = new GenericThread(this, "decompose");
				task.start();
			}
		}
	}

	
	public void decompose() {
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
		
		// Go through the entities list and create one model for each physical entity
		int num = 0;
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
				num++;
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
		progframe.setVisible(false);
	}

	
	
	public void batchCluster() throws IOException {
		visualizeAllDataStructures(false);
		cd = new Clusterer(junggraph, this);
		cd.setVisible(false);

		JFileChooser filec = new JFileChooser();
		filec.setPreferredSize(SemGenGUI.filechooserdims);
		Boolean saveok = false;
		File batchclusterfile = null;
		while (!saveok) {
			filec.setCurrentDirectory(SemGenGUI.currentdirectory);
			filec.setDialogTitle("Choose location to save clustering results");
			filec.addChoosableFileFilter(new FileFilter(new String[] { "txt" }));
			int returnVal = filec.showSaveDialog(SemGenGUI.desktop);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				batchclusterfile = new File(filec.getSelectedFile().getAbsolutePath());
				if (!batchclusterfile.getAbsolutePath().endsWith(".txt")
						&& !batchclusterfile.getAbsolutePath().endsWith(".TXT")) {
					batchclusterfile = new File(filec.getSelectedFile().getAbsolutePath() + ".txt");
				} else {
				}
				if (batchclusterfile.exists()) {
					int overwriteval = JOptionPane.showConfirmDialog(SemGenGUI.desktop, "Overwrite existing file?",
							batchclusterfile.getName() + " already exists",JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
					if (overwriteval == JOptionPane.OK_OPTION) {
						saveok = true;
					} else {
						saveok = false;
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
			//SemGenGUI.pft = new ProgressFrameThread("Clustering...", false);
			//new Thread(SemGenGUI.pft).start();
			BatchClusterTask task = new BatchClusterTask();
			task.execute();
			SemGenGUI.progframe = new ProgressFrame("Performing clustering analysis...", false, task);
		}
	}
	
	public class BatchClusterTask extends SwingWorker<Void, Void> {
        public BatchClusterTask(){}
        @Override
        public Void doInBackground() {
        	try {
        		performClusteringAnalysis();
			} catch (Exception e) {
				e.printStackTrace();
			}
            return null;
        }
        @Override
        public void done() {
        	SemGenGUI.progframe.setVisible(false);
        }
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
			//System.out.println("Layout: " + cd.layout.getGraph());
			String newmoduletable = cd.clusterAndRecolor(cd.layout, y, cd.similarColors, Clusterer.groupVertices.isSelected());
			clusterwriter.println("-----Found " + cd.nummodules + " modules-----");
			if (!newmoduletable.equals(moduletable)) {
				moduletable = newmoduletable;
				clusterwriter.println(moduletable);
			} else {
				clusterwriter.println("(no change)");
			}

			float fltnum = y;
			float fltmaxnum = maxclusteringiterations;

			SemGenGUI.progframe.bar.setValue(Math.round(100 * (fltnum / fltmaxnum)));
		}
		clusterwriter.flush();
		clusterwriter.close();
		SemGenGUI.progframe.bar.setVisible(false);
		JOptionPane.showMessageDialog(SemGenGUI.desktop, "Finished clustering analysis");
	}


	public void itemStateChanged(ItemEvent arg0) {
		try {
			visualize(primeextraction(), false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}