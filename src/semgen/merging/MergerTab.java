package semgen.merging;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;

import org.jdom.JDOMException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import JSim.util.Xcept;
import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.encoding.Encoder;
import semgen.merging.dialog.ConversionFactorDialog;
import semgen.merging.filepane.ModelList;
import semgen.merging.workbench.MergerWorkbench;
import semgen.merging.workbench.MergerWorkbench.MergeEvent;
import semgen.utilities.SemGenError;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;
import semgen.utilities.SemGenTask;
import semgen.utilities.file.SemGenOpenFileChooser;
import semgen.utilities.file.SemGenSaveFileChooser;
import semgen.utilities.uicomponent.SemGenProgressBar;
import semgen.utilities.uicomponent.SemGenScrollPane;
import semgen.utilities.uicomponent.SemGenTab;
import semsim.SemSimUtil;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.Submodel;
import semsim.model.physical.object.CompositePhysicalEntity;
import semsim.model.physical.object.PhysicalProperty;
import semsim.owl.SemSimOWLFactory;
import semsim.writing.CaseInsensitiveComparator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

public class MergerTab extends SemGenTab implements ActionListener, Observer {

	private static final long serialVersionUID = -1383642730474574843L;
	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	private File mergedfile;

	private int dividerlocation = 350;
	private JButton plusbutton = new JButton(SemGenIcon.plusicon);
	private JButton minusbutton = new JButton(SemGenIcon.minusicon);

	private JPanel resolvepanel = new JPanel();
	private SemGenScrollPane resolvescroller;
	private JButton mergebutton = new JButton("MERGE");

	private JSplitPane resmapsplitpane;
	private MappingPanel mappingpanelleft = new MappingPanel("[ ]");
	private MappingPanel mappingpanelright = new MappingPanel("[ ]");
	private JButton addmanualmappingbutton = new JButton("Add manual mapping");
	private JButton loadingbutton = new JButton(SemGenIcon.blankloadingiconsmall);
	
	private MergerWorkbench workbench;
	
	public MergerTab(SemGenSettings sets, GlobalActions globalacts, MergerWorkbench bench) {
		super("Merger", SemGenIcon.mergeicon, "Tab for Merging SemSim Models", sets, globalacts);
		
		workbench = bench;
		workbench.addObserver(this);
	}
	
	@Override
	public void loadTab() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	
		JLabel filelisttitle = new JLabel("Models to merge");
		filelisttitle.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
	
		plusbutton.addActionListener(this);
		minusbutton.addActionListener(this);
	
		loadingbutton.setBorderPainted(false);
		loadingbutton.setContentAreaFilled(false);
		
		JPanel plusminuspanel = new JPanel();
		plusminuspanel.setLayout(new BoxLayout(plusminuspanel, BoxLayout.X_AXIS));
		plusminuspanel.add(filelisttitle);
		plusminuspanel.add(plusbutton);
		plusminuspanel.add(minusbutton);
		
		JPanel filelistheader = new JPanel();
		filelistheader.add(plusminuspanel);
	
		mergebutton.setFont(SemGenFont.defaultBold());
		mergebutton.setForeground(Color.blue);
		mergebutton.addActionListener(this);
		mergebutton.setEnabled(false);
		
		JPanel mergebuttonpanel = new JPanel();
		mergebuttonpanel.add(mergebutton);
		
		JPanel filepane = new JPanel(new BorderLayout());
		filepane.add(filelistheader, BorderLayout.WEST);
		filepane.add(new ModelList(workbench), BorderLayout.CENTER);
		filepane.add(mergebuttonpanel, BorderLayout.EAST);
		filepane.setAlignmentX(LEFT_ALIGNMENT);
		filepane.setPreferredSize(new Dimension(settings.getAppWidth() - 200, 60));
		filepane.setMaximumSize(new Dimension(99999, 175));
		
		resolvepanel.setLayout(new BoxLayout(resolvepanel, BoxLayout.Y_AXIS));
		resolvepanel.setBackground(Color.white);
		resolvescroller = new SemGenScrollPane(resolvepanel);
		resolvescroller.setBorder(BorderFactory.createTitledBorder("Resolution points between models"));
		resolvescroller.setAlignmentX(LEFT_ALIGNMENT);
	
		JSplitPane mappingsplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mappingpanelleft, mappingpanelright);
		mappingsplitpane.setOneTouchExpandable(true);
		mappingsplitpane.setAlignmentX(LEFT_ALIGNMENT);
		mappingsplitpane.setDividerLocation((settings.getAppWidth() - 20) / 2);
	
		resmapsplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, resolvescroller, mappingsplitpane);
		resmapsplitpane.setOneTouchExpandable(true);
		resmapsplitpane.setDividerLocation(dividerlocation);
	
		addmanualmappingbutton.addActionListener(this);
		
		JPanel mappingbuttonpanel = new JPanel();
		mappingbuttonpanel.setAlignmentX(LEFT_ALIGNMENT);
		mappingbuttonpanel.add(addmanualmappingbutton);
	
		this.add(filepane);
		this.add(resmapsplitpane);
		this.add(mappingbuttonpanel);
		this.add(Box.createGlue());
		this.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
	}
	
	public void actionPerformed(ActionEvent arg0) {
		Object o = arg0.getSource();
		
		if (o == plusbutton)
			PlusButtonAction();

		if (o == minusbutton) {
			workbench.removeSelectedModel();
		}

		if (o == mergebutton) {
			File file = saveMerge();
				if (file!=null) {
					mergedfile = file;
					addmanualmappingbutton.setEnabled(false);
					
					MergeTask task = new MergeTask();
					task.execute();
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
					ResolutionPanel newrespanel = new ResolutionPanel(workbench, workbench.getModel(0).getDataStructure(cdwd1),
							workbench.getModel(1).getDataStructure(cdwd2),
							workbench.getModel(0), workbench.getModel(1), "(manual mapping)", true);
					resolvepanel.add(newrespanel);
					resolvepanel.repaint();
					resolvepanel.validate();
					this.validate();
					resolvescroller.scrollToComponent(newrespanel);
				}
			} else {
				SemGenError.showError("Please select a codeword from both component models","");
			}
		}
	}

	public void PlusButtonAction(){
		Set<File> files = new HashSet<File>();
		new SemGenOpenFileChooser(files, "Select SemSim models to merge",
        			new String[]{"owl", "xml", "sbml", "mod"});
		
		if (files.size() == 0) return;
		if (files.size()+workbench.getNumberofStagedModels() > 2) {
			SemGenError.showError("Currently, SemGen can only merge two models at a time.", "Too many models");
			return;
		}
		AddModelsToMergeTask task = new AddModelsToMergeTask(files);
		task.execute(); 
	}
	
	private class AddModelsToMergeTask extends SemGenTask {
		public Set<File> files;
        public AddModelsToMergeTask(Set<File> filestoload){
        	files = filestoload;
        	progframe = new SemGenProgressBar("Loading models...", true);
        }
        @Override
        public Void doInBackground() {
        	try {
				cancel(!workbench.addModels(files, settings.doAutoAnnotate()));
			} catch (Exception e) {
				e.printStackTrace();
			}
            return null;
        }
    }

	public void primeForMerging() {
				populateMappingPanel(workbench.getModel(0).getName(), workbench.getModel(0), mappingpanelleft, Color.blue);
		if(workbench.hasMultipleModels()) {
			populateMappingPanel(workbench.getModel(1).getName(), workbench.getModel(1), mappingpanelright, Color.red);
			identicaldsnames = identifyIdenticalCodewords();
			initialidenticalinds.addAll(identicaldsnames);
			resolvepanel.removeAll();
			resolvepanel.validate();
			SemGenProgressBar progframe = new SemGenProgressBar("Comparing models...", true);
			identifyExactSemanticOverlap();
			progframe.dispose();
			resmapsplitpane.setDividerLocation(dividerlocation);
			loadingbutton.setIcon(SemGenIcon.blankloadingiconsmall);
			mergebutton.setEnabled(true);
		}
		else mergebutton.setEnabled(false);
	}
		
	public class MergeTask extends SemGenTask {
		public MergeTask(){
			progframe = new SemGenProgressBar("Merging...", true);
		}
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
        public void endTask() {
			addmanualmappingbutton.setEnabled(true);
        }
	}

	public void populateMappingPanel(String filename, SemSimModel model, MappingPanel mappingpanel, Color color) {
		ArrayList<String> descannset = new ArrayList<String>();
		ArrayList<String> nodescannset = new ArrayList<String>();
		for (DataStructure datastr : model.getDataStructures()) {
			String desc = "(" + datastr.getName() + ")";
			if(datastr.getDescription()!=null){
				desc = datastr.getDescription() + " " + desc;
				descannset.add(desc);
			}
			else nodescannset.add(desc);
		}
		Collections.sort(descannset, new CaseInsensitiveComparator());
		Collections.sort(nodescannset, new CaseInsensitiveComparator());
		
		String[] comboarray = new String[descannset.size() + nodescannset.size()];
		for(int i=0; i<comboarray.length; i++){
			if(i<descannset.size()) comboarray[i] = descannset.get(i);
			else comboarray[i] = nodescannset.get(i-descannset.size());
		}
		mappingpanel.scrollercontent.setForeground(color);
		mappingpanel.scrollercontent.setListData(comboarray);
		mappingpanel.setTitle(filename);
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

	public void merge() throws IOException, CloneNotSupportedException, OWLException, InterruptedException, JDOMException, Xcept {
		SemSimModel ssm1clone = workbench.getModel(0).clone();
		SemSimModel ssm2clone = workbench.getModel(1).clone();

		// First collect all the data structures that aren't going to be used in the resulting merged model
		// Include a mapping between the solution domains
		Component[] resolutionpanels = new Component[resolvepanel.getComponentCount()+1]; 
		for(int j=0; j<resolutionpanels.length-1;j++) resolutionpanels[j] = resolvepanel.getComponent(j);
		
		DataStructure soldom1 = ssm1clone.getSolutionDomains().toArray(new DataStructure[]{})[0];
		DataStructure soldom2 = ssm2clone.getSolutionDomains().toArray(new DataStructure[]{})[0];
		
		resolutionpanels[resolutionpanels.length-1] = new ResolutionPanel(workbench, soldom1, soldom2, ssm1clone, ssm2clone, 
				"automated solution domain mapping", false);
		
		SemSimModel modelfordiscardedds = null;
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
				String newdsname = JOptionPane.showInputDialog(this, "Both models contain codeword " + dsname + ".\n" +
						"Enter new name for use in " + workbench.getModel(0).getName() + " equations.\nNo special characters, no spaces.", "Duplicate codeword", JOptionPane.OK_OPTION);
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
					JOptionPane.showMessageDialog(this, "That is the existing name. Please choose a new one.");
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
			SemGenError.showError(
					"ERROR: One of the models to be merged has multiple solution domains.\nMerged model not saved.","Merge Failed");
			return;
		}
		
		// WHAT TO DO ABOUT ONTOLOGY-LEVEL ANNOTATIONS?
		
		mergedmodel.setNamespace(mergedmodel.generateNamespaceFromDateAndTime());
		manager.saveOntology(mergedmodel.toOWLOntology(), new RDFXMLOntologyFormat(), IRI.create(mergedfile));
		optionToEncode(mergedmodel);
		workbench.reloadAllModels(settings.doAutoAnnotate());
	}

	public void optionToEncode(SemSimModel model) throws IOException, OWLException {
		int x = JOptionPane.showConfirmDialog(this, "Finished merging "
				+ mergedfile.getName()
				+ "\nGenerate simulation code from merged model?", "",
				JOptionPane.YES_NO_OPTION);
		if (x == JOptionPane.YES_OPTION) {
			new Encoder(model, mergedfile.getName().substring(0, mergedfile.getName().lastIndexOf(".")));
		}
	}

	@Override
	public boolean isSaved() {
		return true;
	}

	@Override
	public void requestSave() {
				
	}

	@Override
	public void requestSaveAs() {
				
	}
	
	public File saveMerge() {
		SemGenSaveFileChooser filec = new SemGenSaveFileChooser("Choose location to save file", 
				new String[]{"cellml","owl"});
		if (filec.SaveAsAction()!=null) {
			mergedfile = filec.getSelectedFile();
			return mergedfile;
		}
		return null;
	}

	@Override
	public void addObservertoWorkbench(Observer obs) {
		workbench.addObserver(obs);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg == MergeEvent.functionalsubmodelerr) {
			SemGenError.showFunctionalSubmodelError(((MergeEvent)arg).getMessage());
		}
		if (arg == MergeEvent.threemodelerror) {
			SemGenError.showError("Currently, SemGen can only merge two models at a time.", "Too many models");
		}	
		if (arg == MergeEvent.modelerrors) {
			JOptionPane.showMessageDialog(this, "Model " + ((MergeEvent)arg).getMessage() + " has errors.",
					"Failed to analyze.", JOptionPane.ERROR_MESSAGE);
			mergebutton.setEnabled(false);
		}
		if (arg == MergeEvent.modellistupdated) {
			primeForMerging();
		}
	}
}
