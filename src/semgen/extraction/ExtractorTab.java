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

import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;

import edu.uci.ics.jung.graph.SparseMultigraph;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import semgen.SemGenGUI;
import semgen.SemGenSettings;
import semgen.UniversalActions;
import semgen.encoding.Encoder;
import semgen.extraction.graph.SemGenRadialGraphView;
import semgen.extraction.workbench.ExtractorWorkbench;
import semgen.resource.ComparatorByName;
import semgen.resource.GenericThread;
import semgen.resource.SemGenIcon;
import semgen.resource.SemGenFont;
import semgen.resource.SemGenTask;
import semgen.resource.file.FileFilter;
import semgen.resource.file.LoadSemSimModel;
import semgen.resource.file.SemGenOpenFileChooser;
import semgen.resource.file.SemGenSaveFileChooser;
import semgen.resource.uicomponents.ProgressBar;
import semgen.resource.uicomponents.SemGenTab;
import semsim.extraction.Extractor;
import semsim.model.SemSimModel;
import semsim.model.computational.MappableVariable;
import semsim.model.computational.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.PhysicalProperty;
import semsim.model.physical.Submodel;
import semsim.reading.ModelClassifier;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

import java.awt.BorderLayout;

public class ExtractorTab extends SemGenTab implements ActionListener, ItemListener {

	public static final long serialVersionUID = -5142058482154697778L;
	public SemSimModel semsimmodel;
	public static final String LABEL = "codeword";
	public static final Schema LABEL_SCHEMA = new Schema();
	static {LABEL_SCHEMA.addColumn(LABEL, String.class, "");}
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
	public JButton vizsourcebutton = new JButton("Show source model");;
	public JButton clusterbutton = new JButton("Cluster");
	public JCheckBox extractionlevelchooserentities = new JCheckBox("More inclusive");
	public JCheckBox includepartipantscheckbox = new JCheckBox("Include participants");
	public JCheckBox extractionlevelchooser2 = new JCheckBox("Include full dependency chain");
	public JButton extractbutton = new JButton("EXTRACT");;
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
	public Graph tempgraph, physiomapgraph;
	public SparseMultigraph<String, Number> junggraph;


	
	public ExtractorWorkbench workbench;

	public Clusterer cd;
	
	public ExtractorTab(File srcfile, SemGenSettings sets, UniversalActions ua) throws OWLException {
		super(srcfile.getName(), SemGenIcon.extractoricon, "Extracting from " + srcfile.getName(), sets, ua);
		
		sourcefile = srcfile;
		setLayout(new BorderLayout());
	}
	
	public Boolean Initialize() {
		JPanel toppanel = new JPanel(new BorderLayout());
		toppanel.setOpaque(true);
		
		vizsourcebutton.setFont(SemGenFont.defaultBold());
		vizsourcebutton.addActionListener(this);

		extractbutton.setForeground(Color.blue);
		extractbutton.setFont(SemGenFont.defaultBold());
		extractbutton.addActionListener(this);
		
		extractionlevelchooserentities.setFont(SemGenFont.defaultPlain());
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
		entitiespanel = new ExtractorSelectionPanel(this, "Entities", workbench.listentities(), null);
		
		processespanel = new ExtractorSelectionPanel(this, "Processes", workbench.listprocesses(), includepartipantscheckbox);
		submodelspanel = new ExtractorSelectionPanel(this, "Sub-models", workbench.listsubmodels(), null);
		codewordspanel = new ExtractorSelectionPanel(this, "Codewords", workbench.listcodewords(), extractionlevelchooser2);
		clusterpanel = new ExtractorSelectionPanel(this, "Clusters", null, clusterbutton);

		AlphabetizeCheckBoxes(processespanel);
		AlphabetizeCheckBoxes(entitiespanel);
		AlphabetizeCheckBoxes(submodelspanel);
		AlphabetizeCheckBoxes(codewordspanel);

		JPanel leftpanel = new JPanel();
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
		return true;
	}

	public void optionToEncode(String filenamesuggestion) {
		int x = JOptionPane.showConfirmDialog(this, "Finished extracting "
				+ extractedfile.getName()
				+ "\nGenerate simulation code from extracted model?", "",
				JOptionPane.YES_NO_OPTION);
		if (x == JOptionPane.YES_OPTION) {
				new Encoder(extractedmodel, filenamesuggestion);
		}
	}

	public Set<DataStructure> getDependencyChain(DataStructure startds){
		// The hashtable contains the data structure URIs and whether the looping alogrithm here should collect 
		// their inputs (true = collect)
		Hashtable<DataStructure, Boolean> table = new Hashtable<DataStructure, Boolean>();
		Set<DataStructure> keys = new HashSet<DataStructure>();
		Set<DataStructure> inputstoadd = new HashSet<DataStructure>();
		table.put(startds, true);
		DataStructure key = null;
		
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
		DataStructure[] array = {};
		table.keySet().toArray(array);
		Set<DataStructure> dsset = new HashSet<DataStructure>();
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
				@SuppressWarnings("unchecked")
				Iterator<Node> nodeit = (Iterator<Node>)tempgraph.nodes();
				// check if the individual has already been added to the graph
				while (nodeit.hasNext()) {
					Node thenode = nodeit.next();
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
					Boolean otheradd = true;
					Node othernode = null;
					@SuppressWarnings("unchecked")
					Iterator<Node> othernodeit = tempgraph.nodes();
					// check if the input has already been added to the graph
					while (othernodeit.hasNext()) {
						Node theothernode = othernodeit.next();
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
				view = new SemGenRadialGraphView(tempgraph, LABEL, semsimmodel, settings.getAppSize());
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
			view = new SemGenRadialGraphView(tempgraph, LABEL, semsimmodel, settings.getAppSize());
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
			Node theothernode = othernodeit.next();
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
				Map<DataStructure, Set<? extends DataStructure>> table = workbench.primeextraction();
				if (table.size() != 0) {
					extractedfile = SemGenGUI.SaveAsAction(this, null, new String[]{
							"cellml", "owl"});
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
	
	public void atomicDecomposition() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setCurrentDirectory(SemGenOpenFileChooser.currentdirectory);
		fc.setDialogTitle("Select directory for extracted models");
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = new File(fc.getSelectedFile().getAbsolutePath());
			SemGenOpenFileChooser.currentdirectory = fc.getCurrentDirectory();
			ProgressBar progframe = new ProgressBar("Performing decomposition", false);
			if (file != null) {
				GenericThread task = new GenericThread(this, "decompose");
				task.start();
			}
			progframe.dispose();
		}
	}

	

	public void itemStateChanged(ItemEvent arg0) {
		try {
			visualize(workbench.primeextraction(), false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getTabType() {
		return ExtractorTab;
	}

	@Override
	public void requestSave() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void requestSaveAs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isSaved() {
		// TODO Auto-generated method stub
		return false;
	}
}