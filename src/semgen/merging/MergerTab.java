package semgen.merging;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import javax.swing.JSplitPane;

import org.apache.commons.lang3.tuple.Pair;
import org.semanticweb.owlapi.model.OWLException;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.merging.dialog.ConversionFactorDialog;
import semgen.merging.filepane.ModelList;
import semgen.merging.resolutionpane.ResolutionPane;
import semgen.merging.workbench.MergerWorkbench;
import semgen.merging.workbench.Merger.ResolutionChoice;
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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

public class MergerTab extends SemGenTab implements ActionListener, Observer {

	private static final long serialVersionUID = -1383642730474574843L;


	private int dividerlocation = 350;
	private JButton plusbutton = new JButton(SemGenIcon.plusicon);
	private JButton minusbutton = new JButton(SemGenIcon.minusicon);

	private ResolutionPane respane;
	private JButton mergebutton = new JButton("MERGE");

	private JSplitPane resmapsplitpane;
	private JButton addmanualmappingbutton = new JButton("Add manual mapping");
	private JButton loadingbutton = new JButton(SemGenIcon.blankloadingiconsmall);
	private MappingPanel mappingpanelleft, mappingpanelright; 
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
		
		respane = new ResolutionPane(workbench);
		
		SemGenScrollPane resolvescroller = new SemGenScrollPane(respane);
		resolvescroller.setBorder(BorderFactory.createTitledBorder("Resolution points between models"));
		resolvescroller.setAlignmentX(LEFT_ALIGNMENT);
	
		mappingpanelleft = new MappingPanel(workbench);
		mappingpanelright = new MappingPanel(workbench);
		
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
		
		plusButtonAction();
	}
	
	public void actionPerformed(ActionEvent arg0) {
		Object o = arg0.getSource();
		
		if (o == plusbutton)
			plusButtonAction();

		if (o == minusbutton) {
			minusButtonAction();
		}

		if (o == mergebutton) {
			mergeButtonAction();
		}

		if (o == addmanualmappingbutton) {
			manualMappingButtonAction();
		}
	}
	
	private void mergeButtonAction() {
		ArrayList<ResolutionChoice> choicelist = respane.pollResolutionList();
		
		if (choicelist != null) {
				addmanualmappingbutton.setEnabled(false);
				choicelist.add(0, ResolutionChoice.first); //add for solution domains
				HashMap<String, String> cwnamemap = workbench.createIdenticalNameMap(choicelist);
				for (String name : cwnamemap.keySet()) {
					String newname = changeCodeWordNameDialog(name);
					cwnamemap.put(name, newname);
				}
				ArrayList<Boolean> unitoverlaps = workbench.getUnitOverlaps();
				ArrayList<Pair<Double,String>> conversionlist = new ArrayList<Pair<Double,String>>(); 
				for (int i=0; i<unitoverlaps.size(); i++) {
					if (!unitoverlaps.get(i)) {
						ResolutionChoice choice = choicelist.get(i);
						if (!choice.equals(ResolutionChoice.ignore)) {
							//Prompt user for conversion factors, selection cancel returns 0 and cancels the merge
							ConversionFactorDialog condia = new ConversionFactorDialog(workbench.getDSDescriptors(i), choice.equals(ResolutionChoice.first));
							if (condia.getConversionFactor().getLeft().equals(0.0)) return;
							conversionlist.add(condia.getConversionFactor());
							continue;
						}
					}
					conversionlist.add(Pair.of(1.0, "*"));
				}
				SemGenProgressBar progframe = new SemGenProgressBar("Merging...", true);
				String error = workbench.executeMerge(cwnamemap, choicelist, conversionlist, progframe);
				if (error!=null){
					SemGenError.showError(
							"ERROR: " + error, "Merge Failed");
				}
		}
		else {
			JOptionPane.showMessageDialog(this, "Some codeword overlaps are unresolved.");
			return;
		}
	}
	
	private void plusButtonAction(){
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

	private void minusButtonAction() {
		workbench.removeSelectedModel();
		mappingpanelleft.clearPanel();
		mappingpanelright.clearPanel();
		primeForMerging();
	}
	
	private void manualMappingButtonAction() {
		if ((mappingpanelleft.getSelectionIndex()!=-1)
					&& (mappingpanelright.getSelectionIndex()!=-1)) {
				Pair<String, String> names = workbench.addManualCodewordMapping(
						mappingpanelleft.getSelectionIndex(), 
						mappingpanelright.getSelectionIndex());
			
				if (names!=null) {
					JOptionPane.showMessageDialog(this, 
							names.getLeft() + " and " + names.getRight() + " are already mapped");
				}
			} else {
				SemGenError.showError("Please select a codeword from both component models","");
			}
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
		if (workbench.getNumberofStagedModels() == 0) return;
		mappingpanelleft.populatePanel(0);
		if(workbench.hasMultipleModels()) {
			mappingpanelright.populatePanel(1);

			SemGenProgressBar progframe = new SemGenProgressBar("Comparing models...", true);
			workbench.mapModels();
			progframe.dispose();
			loadingbutton.setIcon(SemGenIcon.blankloadingiconsmall);
			mergebutton.setEnabled(true);
		}
		else mergebutton.setEnabled(false);
		
		resmapsplitpane.setDividerLocation(dividerlocation);
	}

	public String changeCodeWordNameDialog(String dsname) {
		String newdsname = null;
		while(true){
			newdsname = JOptionPane.showInputDialog(this, "Both models contain codeword " + dsname + ".\n" +
					"Enter new name for use in " + workbench.getOverlapMapModelNames() + " equations.\nNo special characters, no spaces.", "Duplicate codeword", JOptionPane.OK_OPTION);
			
			if(newdsname!=null && !newdsname.equals("")){
				if(newdsname.equals(dsname)){
					JOptionPane.showMessageDialog(this, "That is the existing name. Please choose a new one.");
				}
				else break;
			}
		}
		return newdsname;
	}
	
	public void optionToEncode(String filepath) throws IOException, OWLException {
		int x = JOptionPane.showConfirmDialog(this, "Finished merging "
				+ workbench.getMergedModelName()
				+ "\nGenerate simulation code from merged model?", "",
				JOptionPane.YES_NO_OPTION);
		if (x == JOptionPane.YES_OPTION) {
			workbench.encodeMergedModel(filepath);
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
				new String[]{"owl"});
		
		if (filec.SaveAsAction()!=null) {
			return filec.getSelectedFile();
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
		if (arg == MergeEvent.mergecompleted) {
			File file = null;
			while (file == null) {
				file = saveMerge();
			}
			workbench.saveMergedModel(file);
			addmanualmappingbutton.setEnabled(true);
			try {
				optionToEncode(file.getAbsolutePath());
			} catch (IOException | OWLException e) {
				e.printStackTrace();
			}
		}
	}
}
