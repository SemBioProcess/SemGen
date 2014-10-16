package semgen.merging;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jdom.JDOMException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import JSim.util.Xcept;
import semgen.SemGenFileChooser;
import semgen.SemGenGUI;
import semgen.SemGenSettings;
import semgen.resource.GenericThread;
import semgen.resource.SemGenError;
import semgen.resource.SemGenFont;
import semgen.resource.SemGenIcon;
import semgen.resource.uicomponent.ProgressFrame;
import semgen.resource.uicomponent.SemGenScrollPane;
import semgen.resource.uicomponent.SemGenTab;
import semsim.SemSimUtil;
import semsim.model.SemSimModel;
import semsim.model.annotation.ReferenceOntologyAnnotation;
import semsim.model.computational.DataStructure;
import semsim.model.physical.CompositePhysicalEntity;
import semsim.model.physical.PhysicalProperty;
import semsim.model.physical.Submodel;
import semsim.owl.SemSimOWLFactory;
import semsim.reading.ModelClassifier;
import semsim.writing.CaseInsensitiveComparator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;

public class MergerTab extends SemGenTab implements ActionListener, MouseListener {

	private static final long serialVersionUID = -1383642730474574843L;
	public OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public File file1;
	public File file2;
	public SemSimModel semsimmodel1;
	public SemSimModel semsimmodel2;
	public File mergedfile;
	public int dividerlocation = 350;
	public JButton closebutton;
	public JPanel filelistpanel;
	public JButton plusbutton;
	public JButton minusbutton;

	public JPanel resolvepanel = new JPanel();
	public SemGenScrollPane resolvescroller;
	public JButton mergebutton;

	public JSplitPane resmapsplitpane;
	public MappingPanel mappingpanelleft;
	public MappingPanel mappingpanelright;
	public JButton addmanualmappingbutton;
	public JButton loadingbutton;
	public Set<String> initialidenticalinds = new HashSet<String>();
	public Set<String> identicaldsnames = new HashSet<String>();
	public Hashtable<String,Set<String>> dispandsetofadds = new Hashtable<String,Set<String>>();
	public JFileChooser fc;
	public Boolean contmerging;

	public MergerTab(SemGenSettings sets) {
		super("Merger", SemGenIcon.mergeicon, "Tab for Merging SemSim Models", sets);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel filelistheader = new JPanel();
		JLabel filelisttitle = new JLabel("Models to merge");
		filelisttitle.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));

		JPanel plusminuspanel = new JPanel();
		plusbutton = new JButton(SemGenIcon.plusicon);
		plusbutton.addActionListener(this);
		minusbutton = new JButton(SemGenIcon.minusicon);
		minusbutton.addActionListener(this);

		loadingbutton = new JButton(SemGenIcon.blankloadingiconsmall);
		loadingbutton.setBorderPainted(false);
		loadingbutton.setContentAreaFilled(false);

		plusminuspanel.setLayout(new BoxLayout(plusminuspanel, BoxLayout.X_AXIS));
		plusminuspanel.add(filelisttitle);
		plusminuspanel.add(plusbutton);
		plusminuspanel.add(minusbutton);

		filelistheader.add(plusminuspanel);

		closebutton = new JButton("Close tab");
		closebutton.setForeground(Color.blue);
		closebutton.setFont(new Font("SansSerif", Font.ITALIC, 11));
		closebutton.setBorderPainted(false);
		closebutton.setContentAreaFilled(false);
		closebutton.setOpaque(false);
		closebutton.addActionListener(this);
		closebutton.addMouseListener(this);
		closebutton.setRolloverEnabled(true);

		filelistpanel = new JPanel();
		filelistpanel.setBackground(Color.white);
		filelistpanel.setLayout(new BoxLayout(filelistpanel, BoxLayout.Y_AXIS));

		JScrollPane filelistscroller = new JScrollPane(filelistpanel);

		JPanel mergebuttonpanel = new JPanel();

		mergebutton = new JButton("MERGE");
		mergebutton.setFont(SemGenFont.defaultBold());
		mergebutton.setForeground(Color.blue);
		mergebutton.addActionListener(this);

		mergebuttonpanel.add(mergebutton);
		
		JPanel filelistpanel = new JPanel(new BorderLayout());
		filelistpanel.add(filelistheader, BorderLayout.WEST);
		filelistpanel.add(filelistscroller, BorderLayout.CENTER);
		filelistpanel.add(mergebuttonpanel, BorderLayout.EAST);
		filelistpanel.setAlignmentX(LEFT_ALIGNMENT);
		filelistpanel.setPreferredSize(new Dimension(SemGenGUI.desktop.getWidth() - 200, 60));
		filelistpanel.setMaximumSize(new Dimension(99999, 175));
		
		resolvepanel.setLayout(new BoxLayout(resolvepanel, BoxLayout.Y_AXIS));
		resolvepanel.setBackground(Color.white);
		resolvescroller = new SemGenScrollPane(resolvepanel);
		resolvescroller.setBorder(BorderFactory.createTitledBorder("Resolution points between models"));
		resolvescroller.setAlignmentX(LEFT_ALIGNMENT);

		mappingpanelleft = new MappingPanel("[ ]");
		mappingpanelright = new MappingPanel("[ ]");
		JSplitPane mappingsplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mappingpanelleft, mappingpanelright);
		mappingsplitpane.setOneTouchExpandable(true);
		mappingsplitpane.setAlignmentX(LEFT_ALIGNMENT);
		mappingsplitpane.setDividerLocation((SemGenGUI.desktop.getWidth() - 20) / 2);

		resmapsplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, resolvescroller, mappingsplitpane);
		resmapsplitpane.setOneTouchExpandable(true);
		resmapsplitpane.setDividerLocation(dividerlocation);

		JPanel mappingbuttonpanel = new JPanel();
		mappingbuttonpanel.setAlignmentX(LEFT_ALIGNMENT);
		addmanualmappingbutton = new JButton("Add manual mapping");
		addmanualmappingbutton.addActionListener(this);
		mappingbuttonpanel.add(addmanualmappingbutton);

		this.add(filelistpanel);
		this.add(resmapsplitpane);
		this.add(mappingbuttonpanel);
		this.add(Box.createGlue());
		this.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
	}

	public void actionPerformed(ActionEvent arg0) {
		Object o = arg0.getSource();
		
		if (o == closebutton){
			try {
				SemGenGUI.closeTabAction(this);
			} catch (HeadlessException e0) {
				e0.printStackTrace();
			}
		}
		if (o == plusbutton)
			PlusButtonAction();

		if (o == minusbutton) {
			Boolean cont = false;
			for (Component comp : filelistpanel.getComponents()) {
				if (comp instanceof FileToMergeLabel) {
					FileToMergeLabel ftml = (FileToMergeLabel) comp;
					if (ftml.selected) {
						filelistpanel.remove(comp);
						cont = true;
					}
				}
			}
			if(cont){
				filelistpanel.validate();
				if (filelistpanel.getComponents().length > 0) {
					filelistpanel.getComponent(0).setForeground(Color.blue);
					if (filelistpanel.getComponents().length > 1) {
						filelistpanel.getComponent(1).setForeground(Color.red);
						GenericThread primethread = new GenericThread(this, "primeForMergingStep");
						loadingbutton.setIcon(SemGenIcon.loadingiconsmall);
						primethread.start();
					}
				}
				filelistpanel.repaint();
				filelistpanel.validate();
			}
		}

		if (o == mergebutton) {
			File file = null;
			if (filelistpanel.getComponents().length == 2) {
				file = SemGenGUI.SaveAsAction(this, null, new FileNameExtensionFilter[]{SemGenGUI.cellmlfilter, SemGenGUI.owlfilter});
				
				if (file!=null) {
					mergedfile = file;
					addmanualmappingbutton.setEnabled(false);
					
					MergeTask task = new MergeTask();
					task.execute();
					SemGenGUI.progframe = new ProgressFrame("Merging...", true, task);
				}
			}
		}

		if (o == addmanualmappingbutton) {
			if (!mappingpanelleft.scrollercontent.isSelectionEmpty()
					&& !mappingpanelright.scrollercontent.isSelectionEmpty()) {
				String cdwd1 = (String) mappingpanelleft.scrollercontent.getSelectedValue();
				String cdwd2 = (String) mappingpanelright.scrollercontent.getSelectedValue();
				cdwd1 = cdwd1.substring(cdwd1.lastIndexOf("(") + 1, cdwd1.lastIndexOf(")"));
				cdwd2 = cdwd2.substring(cdwd2.lastIndexOf("(") + 1, cdwd2.lastIndexOf(")"));

				if (!codewordsAlreadyMapped(cdwd1, cdwd2, true)) {
					if(resolvepanel.getComponentCount()!=0) resolvepanel.add(new JSeparator());
					ResolutionPanel newrespanel = new ResolutionPanel(semsimmodel1.getDataStructure(cdwd1),
							semsimmodel2.getDataStructure(cdwd2),
							semsimmodel1, semsimmodel2, "(manual mapping)", true);
					resolvepanel.add(newrespanel);
					resolvepanel.repaint();
					resolvepanel.validate();
					this.validate();
					resolvescroller.scrollToComponent(newrespanel);
				}
			} else {
				JOptionPane.showMessageDialog(this,"Please select a codeword from both component models","", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public int PlusButtonAction(){
		int choice = SemGenGUI.showSemGenFileChooser(SemGenGUI.currentdirectory,
				new String[]{"owl", "xml", "sbml", "cellml", "mod"}, "Select SemSim models to merge", 
				SemGenFileChooser.MERGING_TASK, true, this);
		if (JFileChooser.APPROVE_OPTION == choice){
			startAdditionOfModels(SemGenGUI.fc.getSelectedFiles());
		}
		return choice;
	}

	public void startAdditionOfModels(File[] files){
		AddModelsToMergeTask task = new AddModelsToMergeTask(files);
		task.execute();
		SemGenGUI.progframe = new ProgressFrame("Loading models...", true, task);
	}
	
	private class AddModelsToMergeTask extends SwingWorker<Void, Void> {
		public File[] files;
        public AddModelsToMergeTask(File[] files){
        	this.files = files;
        }
        @Override
        public Void doInBackground() {
        	try {
				addModelsToMerge(files);
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

	private void addModelsToMerge(File[] files) {
		initialidenticalinds = new HashSet<String>();
		identicaldsnames = new HashSet<String>();
		for (int x = 0; x < files.length; x++) {
			if(ModelClassifier.classify(files[x])==ModelClassifier.CELLML_MODEL){
				SemGenError.showFunctionalSubmodelError(this,files[x]);
			}
			else{
				FileToMergeLabel templabel = new FileToMergeLabel(files[x].getAbsolutePath());
				templabel.addMouseListener(this);
				templabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
				if (filelistpanel.getComponentCount() == 0) templabel.setForeground(Color.blue);
				else if (filelistpanel.getComponentCount() == 1) templabel.setForeground(Color.red);
				filelistpanel.add(templabel);
			}
		}
		if(filelistpanel.getComponentCount() > 1) 
			primeForMergingStep();
		validate();
		repaint();
	}
	
	public class MergeTask extends SwingWorker<Void,Void>{
		public MergeTask(){}
        @Override
        public Void doInBackground() {
        	try {
				merge();
			} catch (Exception e) {
				e.printStackTrace();
			}
            return null;
        }
        @Override
        public void done() {
        	SemGenGUI.progframe.setVisible(false);
			addmanualmappingbutton.setEnabled(true);
        }
	}

	public void primeForMergingStep() {
		mergebutton.setEnabled(true);
		Component[] filecomponents = filelistpanel.getComponents();
		if (filecomponents.length > 1) {
			
			refreshModelsToMerge();
			
			// If either of the models have errors, quit
			for(SemSimModel model : refreshModelsToMerge()){
				if(!model.getErrors().isEmpty()){
					JOptionPane.showMessageDialog(SemGenGUI.desktop, "Model " + model.getName() + " has errors.",
							"Failed to analyze.", JOptionPane.ERROR_MESSAGE);
					mergebutton.setEnabled(false);
					return;
				}
			}
		}
		
		populateMappingPanel(file1.getName(), semsimmodel1, mappingpanelleft, Color.blue);
		populateMappingPanel(file2.getName(), semsimmodel2, mappingpanelright, Color.red);
		identicaldsnames = identifyIdenticalCodewords();
		initialidenticalinds.addAll(identicaldsnames);
		identifyExactSemanticOverlap();
		resmapsplitpane.setDividerLocation(dividerlocation);
		loadingbutton.setIcon(SemGenIcon.blankloadingiconsmall);
	}

	public void populateMappingPanel(String filename, SemSimModel model, MappingPanel mappingpanel, Color color) {
		Set<String> descannset = new HashSet<String>();
		Set<String> nodescannset = new HashSet<String>();
		for (DataStructure datastr : model.getDataStructures()) {
			String desc = "(" + datastr.getName() + ")";
			if(datastr.getDescription()!=null){
				desc = datastr.getDescription() + " " + desc;
				descannset.add(desc);
			}
			else nodescannset.add(desc);
		}
		JList<String> list = new JList<String>();
		list.setForeground(color);
		String[] descannarray = (String[]) descannset.toArray(new String[] {});
		String[] nodescannarray = (String[]) nodescannset.toArray(new String[] {});
		Arrays.sort(descannarray,new CaseInsensitiveComparator());
		Arrays.sort(nodescannarray,new CaseInsensitiveComparator());
		String[] comboarray = new String[descannarray.length + nodescannarray.length];
		for(int i=0; i<comboarray.length; i++){
			if(i<descannarray.length) comboarray[i] = descannarray[i];
			else comboarray[i] = nodescannarray[i-descannarray.length];
		}
		list.setListData(comboarray);
		mappingpanel.scrollercontent.setForeground(color);
		mappingpanel.scrollercontent.setListData(comboarray);
		mappingpanel.title.setText(filename);
	}

	public Set<String> identifyIdenticalCodewords() {
		Set<String> matchedcdwds = new HashSet<String>();
		for (DataStructure ds : semsimmodel1.getDataStructures()) {
			if (semsimmodel2.containsDataStructure(ds.getName()))
				matchedcdwds.add(ds.getName());
		}
		return matchedcdwds;
	}

	public void identifyExactSemanticOverlap() {
		resolvepanel.removeAll();
		resolvepanel.validate();

		SemGenGUI.progframe.updateMessage("Comparing models...");
		// Only include the annotated data structures in the resolution process
		for(DataStructure ds1 : semsimmodel1.getDataStructures()){
			for(DataStructure ds2 : semsimmodel2.getDataStructures()){
				SemGenGUI.progframe.bar.setValue(101);
				Boolean match = false;
				
				// Test singular annotations
				if(ds1.hasRefersToAnnotation() && ds2.hasRefersToAnnotation()) {
					match = testNonCompositeAnnotations(ds1.getFirstRefersToReferenceOntologyAnnotation(),
							ds2.getFirstRefersToReferenceOntologyAnnotation());
				}
				
				// If the physical properties are not null
				if(!match && ds1.getPhysicalProperty()!=null && ds2.getPhysicalProperty()!=null){
					// And they are properties of a specified physical model component
					if(ds1.getPhysicalProperty().getPhysicalPropertyOf()!=null && ds2.getPhysicalProperty().getPhysicalPropertyOf()!=null){
						PhysicalProperty prop1 = ds1.getPhysicalProperty();
						PhysicalProperty prop2 = ds2.getPhysicalProperty();
						
						// and they are annotated against reference ontologies
						if(prop1.hasRefersToAnnotation() && prop2.hasRefersToAnnotation()){
							// and the annotations match
							if(prop1.getFirstRefersToReferenceOntologyAnnotation().getReferenceURI().toString().equals(prop2.getFirstRefersToReferenceOntologyAnnotation().getReferenceURI().toString())){
								
								// and they are properties of the same kind of physical model component
								if(prop1.getPhysicalPropertyOf().getClass() == prop2.getPhysicalPropertyOf().getClass()){
									
									// if they are properties of a composite physical entity
									if(prop1.getPhysicalPropertyOf() instanceof CompositePhysicalEntity){
										CompositePhysicalEntity cpe1 = (CompositePhysicalEntity)prop1.getPhysicalPropertyOf();
										CompositePhysicalEntity cpe2 = (CompositePhysicalEntity)prop2.getPhysicalPropertyOf();
										match = testCompositePhysicalEntityEquivalency(cpe1, cpe2);
									}
									// if they are properties of a physical process or singular physical entity
									else{
										// and if they are both annotated against reference ontology terms
										if(prop1.getPhysicalPropertyOf().hasRefersToAnnotation() && prop2.getPhysicalPropertyOf().hasRefersToAnnotation()){
											// and if the annotations match
											if(prop1.getPhysicalPropertyOf().getFirstRefersToReferenceOntologyAnnotation().getReferenceURI().toString().equals(
													prop2.getPhysicalPropertyOf().getFirstRefersToReferenceOntologyAnnotation().getReferenceURI().toString())){
												match = true;
											}
										}
									}
								}
							}
						}
					}
				}
				if(match){
					resolvepanel.add(new ResolutionPanel(ds1, ds2,
							semsimmodel1, semsimmodel2, "(exact semantic match)", false));
					resolvepanel.add(new JSeparator());
					resolvepanel.validate();
					resolvepanel.repaint();
				}
			} // end of iteration through model2 data structures
		} // end of iteration through model1 data structures
		if (resolvepanel.getComponents().length==0) {
			JOptionPane.showMessageDialog(SemGenGUI.desktop,
					"SemGen did not find any semantic equivalencies between the models","Merger message", JOptionPane.PLAIN_MESSAGE);
		}
		else resolvepanel.remove(resolvepanel.getComponentCount()-1); // remove last JSeparator
	}

	public Boolean testNonCompositeAnnotations(ReferenceOntologyAnnotation ann1, ReferenceOntologyAnnotation ann2){
		return (ann1.getReferenceURI().toString().equals(ann2.getReferenceURI().toString()));
	}
	
	public Boolean testCompositePhysicalEntityEquivalency(CompositePhysicalEntity cpe1, CompositePhysicalEntity cpe2){
		if(cpe1.getArrayListOfEntities().size()!=cpe2.getArrayListOfEntities().size())
			return false;
		for(int i=0; i<cpe1.getArrayListOfEntities().size(); i++){
			if(cpe1.getArrayListOfEntities().get(i).hasRefersToAnnotation() && cpe2.getArrayListOfEntities().get(i).hasRefersToAnnotation()){
				if(!cpe1.getArrayListOfEntities().get(i).getFirstRefersToReferenceOntologyAnnotation().getReferenceURI().toString().equals( 
					cpe2.getArrayListOfEntities().get(i).getFirstRefersToReferenceOntologyAnnotation().getReferenceURI().toString())){

					return false;
				}
			}
			else return false;
		}
		return true;
	}

	public Boolean codewordsAlreadyMapped(String cdwd1uri, String cdwd2uri,
			Boolean prompt) {
		String cdwd1 = SemSimOWLFactory.getIRIfragment(cdwd1uri);
		String cdwd2 = SemSimOWLFactory.getIRIfragment(cdwd2uri);
		Boolean alreadymapped = false;
		Component[] resolutionpanels = this.resolvepanel.getComponents();
		for (int x = 0; x < resolutionpanels.length; x++) {
			if (resolutionpanels[x] instanceof ResolutionPanel) {
				ResolutionPanel rp = (ResolutionPanel) resolutionpanels[x];
				if ((cdwd1.equals(rp.ds1.getName()) && cdwd2.equals(rp.ds2.getName()))
						|| (cdwd1.equals(rp.ds2.getName()) && cdwd2.equals(rp.ds1.getName()))) {
					alreadymapped = true;
					if (prompt) {
						JOptionPane.showMessageDialog(SemGenGUI.desktop, cdwd1
								+ " and " + cdwd2 + " are already mapped");
					}
				}
			}
		}
		return alreadymapped;
	}

	public void merge() throws IOException, CloneNotSupportedException, OWLException, InterruptedException, JDOMException, Xcept {
		SemSimModel ssm1clone = semsimmodel1.clone();
		SemSimModel ssm2clone = semsimmodel2.clone();
		
		SemSimModel modelfordiscardedds = null;
		
		// First collect all the data structures that aren't going to be used in the resulting merged model
		// Include a mapping between the solution domains
		Component[] resolutionpanels = new Component[resolvepanel.getComponentCount()+1]; 
		for(int j=0; j<resolutionpanels.length-1;j++) resolutionpanels[j] = resolvepanel.getComponent(j);
		
		DataStructure soldom1 = ssm1clone.getSolutionDomains().toArray(new DataStructure[]{})[0];
		DataStructure soldom2 = ssm2clone.getSolutionDomains().toArray(new DataStructure[]{})[0];
		
		resolutionpanels[resolutionpanels.length-1] = new ResolutionPanel(soldom1, soldom2, ssm1clone, ssm2clone, 
				"automated solution domain mapping", false);
		
		for (int x = 0; x < resolutionpanels.length; x++) {
			ResolutionPanel rp = null;
			if ((resolutionpanels[x] instanceof ResolutionPanel)) {
				rp = (ResolutionPanel) resolutionpanels[x];
				DataStructure discardedds = null;
				DataStructure keptds = null;
				if (rp.rb1.isSelected() || rp.ds1.isSolutionDomain()){
					discardedds = rp.ds2;
					keptds = rp.ds1;
					modelfordiscardedds = ssm2clone;
				}
				else if(rp.rb2.isSelected()){
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
							ConversionFactorDialog condia = new ConversionFactorDialog(
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
						modelfordiscardedds.removeDataStructure(discardedds.getName() + ".min");
						modelfordiscardedds.removeDataStructure(discardedds.getName() + ".max");
						modelfordiscardedds.removeDataStructure(discardedds.getName() + ".delta");
						identicaldsnames.remove(discardedds.getName() + ".min");
						identicaldsnames.remove(discardedds.getName() + ".max");
						identicaldsnames.remove(discardedds.getName() + ".delta");
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
					if(!rp.rb3.isSelected()){
						identicaldsnames.remove(discardedds.getName());
					}
				}
			}
		}
		
		// Why isn't this working for Pandit-Hinch merge?
		// Prompt the user to resolve the points of SYNTACTIC overlap (same codeword names)
		for (String dsname : identicaldsnames) {
			Boolean cont = true;
			while(cont){
				String newdsname = JOptionPane.showInputDialog(SemGenGUI.progframe, "Both models contain codeword " + dsname + ".\n" +
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
					JOptionPane.showMessageDialog(SemGenGUI.progframe, "That is the existing name. Please choose a new one.");
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
			JOptionPane.showMessageDialog(this, 
					"ERROR: One of the models to be merged has multiple solution domains.\nMerged model not saved.");
			return;
		}
		
		// WHAT TO DO ABOUT ONTOLOGY-LEVEL ANNOTATIONS?
		
		mergedmodel.setNamespace(mergedmodel.generateNamespaceFromDateAndTime());
		manager.saveOntology(mergedmodel.toOWLOntology(), new RDFXMLOntologyFormat(), IRI.create(mergedfile));
		optionToEncode(mergedmodel);
		refreshModelsToMerge();
	}

	public void optionToEncode(SemSimModel model) throws IOException, OWLException {
		int x = JOptionPane.showConfirmDialog(SemGenGUI.desktop, "Finished merging "
				+ mergedfile.getName()
				+ "\nGenerate simulation code from merged model?", "",
				JOptionPane.YES_NO_OPTION);
		if (x == JOptionPane.YES_OPTION) {
			SemGenGUI.startEncoding(model, mergedfile.getName().substring(0, mergedfile.getName().lastIndexOf(".")));
		}
	}
	
	public Set<SemSimModel> refreshModelsToMerge() {
		
		FileToMergeLabel label1 = (FileToMergeLabel) filelistpanel.getComponent(0);
		FileToMergeLabel label2 = (FileToMergeLabel) filelistpanel.getComponent(1);
		file1 = new File(label1.getText());
		file2 = new File(label2.getText());
		
		semsimmodel1 = SemGenGUI.loadSemSimModelFromFile(file1, true);
		
		if(semsimmodel1.getFunctionalSubmodels().size()>0) SemGenError.showFunctionalSubmodelError(this, file1);
		
		semsimmodel2 = SemGenGUI.loadSemSimModelFromFile(file2, true);
		
		if(semsimmodel2.getFunctionalSubmodels().size()>0) SemGenError.showFunctionalSubmodelError(this, file1);
		
		Set<SemSimModel> models = new HashSet<SemSimModel>();
		models.add(semsimmodel1);
		models.add(semsimmodel2);
		return models;
	}

	public Set<String> getOPBsubclasses(String parentclass) {

		OWLClass parent = SemGenGUI.factory.getOWLClass(IRI.create(parentclass));
		Set<OWLClassExpression> subclasses = parent.getSubClasses(SemGenGUI.OPB);
		Set<String> subclassset = new HashSet<String>();
		for (OWLClassExpression subclass : subclasses) {
			subclassset.add(subclass.asOWLClass().getIRI().toString());
		}
		return subclassset;
	}

	public void mouseEntered(MouseEvent arg0) {
		Component component = arg0.getComponent();
		if (component instanceof AbstractButton) {
			AbstractButton button = (AbstractButton) component;
			button.setBorderPainted(true);
			button.setContentAreaFilled(true);
			button.setOpaque(true);
		}
	}

	public void mouseExited(MouseEvent arg0) {
		Component component = arg0.getComponent();
		if (component instanceof AbstractButton) {
			AbstractButton button = (AbstractButton) component;
			button.setBorderPainted(false);
			button.setContentAreaFilled(false);
			button.setOpaque(false);
		}
	}

	public void mousePressed(MouseEvent arg0) {
		if (arg0.getSource() instanceof FileToMergeLabel
				&& arg0.getClickCount() > 0) {
			FileToMergeLabel ftml = (FileToMergeLabel) arg0.getSource();
			arg0.consume();
			ftml.setSelected();

			for (Component comp : filelistpanel.getComponents()) {
				if (comp instanceof FileToMergeLabel) {
					FileToMergeLabel otherftml = (FileToMergeLabel) comp;
					if (!otherftml.filepath.equals(ftml.filepath)) {
						otherftml.setUnselected();
					} else {
						otherftml.setSelected();
					}
				}
			}
		}
		filelistpanel.validate();
		filelistpanel.repaint();
	}
	
	public void mouseClicked(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
}
