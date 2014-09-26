package semgen.merging;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;

import org.jdom.JDOMException;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;

import JSim.util.Xcept;
import semgen.SemGenSettings;
import semgen.UniversalActions;
import semgen.encoding.Encoder;
import semgen.merging.dialog.ConversionFactorDialog;
import semgen.merging.workbench.MergerWorkbench;
import semgen.resource.GenericThread;
import semgen.resource.SemGenError;
import semgen.resource.SemGenIcon;
import semgen.resource.SemGenFont;
import semgen.resource.SemGenTask;
import semgen.resource.file.LoadSemSimModel;
import semgen.resource.file.SemGenOpenFileChooser;
import semgen.resource.file.SemGenSaveFileChooser;
import semgen.resource.uicomponents.ProgressBar;
import semgen.resource.uicomponents.SemGenScrollPane;
import semgen.resource.uicomponents.SemGenTab;
import semsim.SemSimUtil;
import semsim.model.SemSimModel;
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

public class MergerTab extends SemGenTab implements ActionListener, MouseListener {

	private static final long serialVersionUID = -1383642730474574843L;

	private MergerWorkbench workbench;
	
	private int dividerlocation = 350;
	public JPanel filelistpanel = new JPanel();
	public JButton plusbutton = new JButton(SemGenIcon.plusicon);
	public JButton minusbutton = new JButton(SemGenIcon.minusicon);

	public JPanel resolvepanel = new JPanel();
	public JButton mergetempbutton = new JButton("MERGE INTO TEMP FILE");
	public JButton mergebutton = new JButton("MERGE");
	
	private SemGenScrollPane resolvescroller; 
	public JSplitPane resmapsplitpane;
	public MappingPanel mappingpanelleft = new MappingPanel("[ ]");
	public MappingPanel mappingpanelright = new MappingPanel("[ ]");
	public JButton addmanualmappingbutton = new JButton("Add manual mapping");
	public JButton loadingbutton = new JButton(SemGenIcon.blankloadingiconsmall);;

	public MergerTab(SemGenSettings sets, UniversalActions ua) {
		super("Merger", SemGenIcon.mergeicon, "Tab for Merging SemSim Models", sets, ua);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		workbench = new MergerWorkbench();
	}

	@Override
	public Boolean Initialize() {
		JPanel filelistheader = new JPanel();
		JLabel filelisttitle = new JLabel("Models to merge");
		filelisttitle.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));

		JPanel plusminuspanel = new JPanel();
		plusbutton.addActionListener(this);
		minusbutton.addActionListener(this);

		loadingbutton.setBorderPainted(false);
		loadingbutton.setContentAreaFilled(false);

		plusminuspanel.setLayout(new BoxLayout(plusminuspanel, BoxLayout.X_AXIS));
		plusminuspanel.add(filelisttitle);
		plusminuspanel.add(plusbutton);
		plusminuspanel.add(minusbutton);

		filelistheader.add(plusminuspanel);

		filelistpanel.setBackground(Color.white);
		filelistpanel.setLayout(new BoxLayout(filelistpanel, BoxLayout.Y_AXIS));
		JScrollPane filelistscroller = new JScrollPane(filelistpanel);
		
		mergebutton.setFont(SemGenFont.defaultBold());
		mergebutton.setForeground(Color.blue);
		mergebutton.addActionListener(this);

		mergetempbutton.setForeground(Color.blue);
		mergetempbutton.addActionListener(this);

		JPanel mergebuttonpanel = new JPanel();
		mergebuttonpanel.add(mergebutton);
		
		JPanel filelistpanel = new JPanel(new BorderLayout());
		filelistpanel.add(filelistheader, BorderLayout.WEST);

		filelistpanel.add(filelistscroller, BorderLayout.CENTER);
		filelistpanel.add(mergebuttonpanel, BorderLayout.EAST);
		filelistpanel.setAlignmentX(LEFT_ALIGNMENT);
		filelistpanel.setPreferredSize(new Dimension(settings.getAppWidth() - 200, 60));
		filelistpanel.setMaximumSize(new Dimension(99999, 175));
		
		resolvepanel.setLayout(new BoxLayout(resolvepanel, BoxLayout.Y_AXIS));
		resolvepanel.setBackground(Color.white);
		resolvescroller = new SemGenScrollPane(resolvepanel);
		resolvescroller.setBorder(BorderFactory.createTitledBorder("Resolution points between models"));
		resolvescroller.setAlignmentX(LEFT_ALIGNMENT);

		JSplitPane mappingsplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mappingpanelleft, mappingpanelright);
		mappingsplitpane.setOneTouchExpandable(true);
		mappingsplitpane.setAlignmentX(LEFT_ALIGNMENT);
		mappingsplitpane.setDividerLocation((settings.getAppWidth()- 20) / 2);

		resmapsplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, resolvescroller, mappingsplitpane);
		resmapsplitpane.setOneTouchExpandable(true);
		resmapsplitpane.setDividerLocation(dividerlocation);

		JPanel mappingbuttonpanel = new JPanel();
		mappingbuttonpanel.setAlignmentX(LEFT_ALIGNMENT);
		addmanualmappingbutton.addActionListener(this);
		mappingbuttonpanel.add(addmanualmappingbutton);

		this.add(filelistpanel);
		this.add(resmapsplitpane);
		this.add(mappingbuttonpanel);
		this.add(Box.createGlue());
		this.mergetempbutton.setVisible(false);
		this.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
		return null;
	}
	
	public void actionPerformed(ActionEvent arg0) {
		Object o = arg0.getSource();
		
		if (o == plusbutton)
			workbench.startAdditionOfModels();

		if (o == minusbutton) {
			Boolean cont = false;
			for (Component comp : filelistpanel.getComponents()) {
				if (comp instanceof FileToMergeLabel) {
					FileToMergeLabel ftml = (FileToMergeLabel) comp;
					if (ftml.isSelected()) {
						filelistpanel.remove(comp);
						cont = true;
					}
				}
			}
			if(cont){
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
			if (filelistpanel.getComponents().length == 2) {
				SemGenSaveFileChooser fc = new SemGenSaveFileChooser(null, new String[]{"cellml", "owl"});
				File file = fc.SaveAsAction();
				
				if (file!=null) {
					mergedfile = file;
					addmanualmappingbutton.setEnabled(false);
					
					MergeTask task = new MergeTask();
					task.execute();
					addmanualmappingbutton.setEnabled(true);					
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
				
				ResolutionPanel newrespanel = null;
				if (!codewordsAlreadyMapped(cdwd1, cdwd2, true)) {
					if(resolvepanel.getComponentCount()!=0) resolvepanel.add(new JSeparator());
					newrespanel = new ResolutionPanel(this, semsimmodel1.getDataStructure(cdwd1),
							semsimmodel2.getDataStructure(cdwd2),
							semsimmodel1, semsimmodel2, "(manual mapping)");
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
	
	
	
	public void primeForMergingStep() {
		mergebutton.setEnabled(true);
		String rslt = validateModels(filelistpanel.getComponents());
		if (rslt!="") {
			JOptionPane.showMessageDialog(this, "Model " + rslt + " has errors.",
					"Failed to analyze.", JOptionPane.ERROR_MESSAGE);
					mergebutton.setEnabled(false);
					return;
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
		CaseInsensitiveComparator cic = new CaseInsensitiveComparator();
		Arrays.sort(descannarray,cic);
		Arrays.sort(nodescannarray,cic);
		String[] comboarray = new String[descannarray.length + nodescannarray.length];
		for(int i=0; i<comboarray.length; i++){
			if(i<descannarray.length) comboarray[i] = descannarray[i];
			else comboarray[i] = nodescannarray[i-descannarray.length];
		}
		list.setListData(comboarray);
		mappingpanel.scrollercontent.setForeground(color);
		mappingpanel.scrollercontent.setListData(comboarray);
		mappingpanel.setTitle(filename);
	}


	public void identifyExactSemanticOverlap() {
		resolvepanel.removeAll();
		resolvepanel.validate();

		ProgressBar progframe = new ProgressBar("Comparing models...", true);
		
		// Only include the annotated data structures in the resolution process
		for(DataStructure ds1 : semsimmodel1.getDataStructures()){
			for(DataStructure ds2 : semsimmodel2.getDataStructures()){
				
				Boolean match = false;
				
				// Test singular annotations
				if(ds1.hasRefersToAnnotation() && ds2.hasRefersToAnnotation()) {
					match = ds1.getFirstRefersToReferenceOntologyAnnotation().matches(ds2.getFirstRefersToReferenceOntologyAnnotation());
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
							if(prop1.getFirstRefersToReferenceOntologyAnnotation().matches(prop2.getFirstRefersToReferenceOntologyAnnotation())){
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
											match = prop1.getPhysicalPropertyOf().getFirstRefersToReferenceOntologyAnnotation().matches(
													prop2.getPhysicalPropertyOf().getFirstRefersToReferenceOntologyAnnotation());
										}
									}
								}
							}
						}
					}
				}
				if(match){
					resolvepanel.add(new ResolutionPanel(this, ds1, ds2,
							semsimmodel1, semsimmodel2, "(exact semantic match)"));
					resolvepanel.add(new JSeparator());
					resolvepanel.validate();
					resolvepanel.repaint();
				}
			} // end of iteration through model2 data structures
		} // end of iteration through model1 data structures
		if (resolvepanel.getComponents().length==0) {
			JOptionPane.showMessageDialog(this,
					"SemGen did not find any semantic equivalencies between the models","Merger message", JOptionPane.PLAIN_MESSAGE);
		}
		else resolvepanel.remove(resolvepanel.getComponentCount()-1); // remove last JSeparator
		progframe.dispose();
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
						JOptionPane.showMessageDialog(this, cdwd1
								+ " and " + cdwd2 + " are already mapped");
					}
				}
			}
		}
		return alreadymapped;
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
			ftml.setSelected(true);

			for (Component comp : filelistpanel.getComponents()) {
				if (comp instanceof FileToMergeLabel) {
					FileToMergeLabel otherftml = (FileToMergeLabel) comp;
					if (!otherftml.getFilePath().equals(ftml.getFilePath())) {
						otherftml.setSelected(false);
					} else {
						otherftml.setSelected(true);
					}
				}
			}
		}
		filelistpanel.validate();
		filelistpanel.repaint();
	}

	public void mouseReleased(MouseEvent arg0) {}
	public void mouseClicked(MouseEvent arg0) {}

	@Override
	public int getTabType() {
		return MergerTab;
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
