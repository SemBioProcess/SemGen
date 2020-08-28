package semgen.annotation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.AnnotatorWorkbench.LibraryRequest;
import semgen.utilities.SemGenError;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.DropDownCheckList;
import semgen.utilities.uicomponent.SemGenTabToolbar;

public class AnnotatorToolBar extends SemGenTabToolbar implements ActionListener {
	private static final long serialVersionUID = 1L;

	private AnnotatorWorkbench workbench;
	private SemGenToolbarButton autocopymappedvars;
	public static String sourcemodelcodetooltip = "Link the SemSim model with its computational code";
	private SemGenToolbarButton annotateitemchangesourcemodelcode = new SemGenToolbarButton(SemGenIcon.setsourceicon);
	private SemGenToolbarButton annotateitemcopy = new SemGenToolbarButton(SemGenIcon.libraryimporticon);
	private SemGenToolbarButton annotateitemexportcsv = new SemGenToolbarButton(SemGenIcon.exporticon);
	private SemGenToolbarButton annotateitemshowmarkers;
	private JButton annotateitemshowimports;
	private SemGenToolbarButton opentermcreator = new SemGenToolbarButton(SemGenIcon.libraryaddicon);
	private SemGenToolbarButton opentermlibrary= new SemGenToolbarButton(SemGenIcon.librarymodifyicon);
	private SemGenToolbarButton annotateitemtreeview;
	private SemGenToolbarButton stagebutton = new SemGenToolbarButton(SemGenIcon.stageicon);
	//private SemGenToolbarButton coderbutton = new SemGenToolbarButton(SemGenIcon.codericon);
	
	private DropDownCheckList sortselector = new DropDownCheckList(" Sort Options");
	private String sortbytype = new String("By Type");
	private String sortbycompletion = new String("By Composite Completeness");
	GlobalActions globalactions;

	public AnnotatorToolBar(GlobalActions gacts, AnnotatorWorkbench wkbnch, SemGenSettings sets) {
		super(sets);
		workbench = wkbnch;
		globalactions = gacts;

		
		annotateitemshowimports = new JButton(displayImportMessageToUse());
		annotateitemshowimports.addActionListener(this);
		annotateitemshowimports.setToolTipText("Show/hide imported codewords and submodels");
		
		annotateitemshowmarkers = new SemGenToolbarButton(displayMarkerIconToUse());
		annotateitemshowmarkers.addActionListener(this);
		annotateitemshowmarkers.setToolTipText("Show/hide markers that indicate a codeword's property type");
		
		annotateitemtreeview = new SemGenToolbarButton(displayTreeIconToUse());
		annotateitemtreeview.addActionListener(this);
		annotateitemtreeview.setToolTipText("Display codewords and submodels within the submodel tree");
		
		sortselector.addItem(sortbytype, "Sort codewords by physical property type", settings.organizeByPropertyType());
		sortselector.addItem(sortbycompletion, "Sort by the completeness of the composite term", settings.organizeByCompositeCompleteness());
		
		annotateitemcopy.addActionListener(this);
		annotateitemcopy.setToolTipText("Import annotation components from another model");
		
		autocopymappedvars = new SemGenToolbarButton(displayAutoCopyIconToUse());
		autocopymappedvars.addActionListener(this);
		autocopymappedvars.setToolTipText("Auto-copy annotations to mapped variables");
		
		annotateitemchangesourcemodelcode.addActionListener(this);
		annotateitemchangesourcemodelcode.setToolTipText(sourcemodelcodetooltip);
		
		annotateitemexportcsv.setToolTipText("Create a .csv file that tabulates model codeword annotations for use in spreadsheets, manuscript preparation, etc.");
		annotateitemexportcsv.addActionListener(this);

		opentermcreator.addActionListener(this);
		opentermcreator.setToolTipText("Add annotation terms for this model");
		
		opentermlibrary.addActionListener(this);
		opentermlibrary.setToolTipText("Manage all annotation terms for this model");

		stagebutton.setToolTipText("Open model in new Project tab");
		stagebutton.addActionListener(this);

//		coderbutton.setToolTipText("Encode this model for simulation");
//		coderbutton.addActionListener(this);
		
		add(annotateitemtreeview);
		add(annotateitemshowmarkers);
		add(autocopymappedvars);
		add(annotateitemshowimports);
		add(sortselector);
		addSeparator();
		
		add(annotateitemchangesourcemodelcode);
		add(annotateitemexportcsv);
		addSeparator();
		
		add(new ToolBarLabel("Reference Terms:"));
		add(opentermcreator);
		add(opentermlibrary);
		add(annotateitemcopy);
		addSeparator();
		
		add(stagebutton);

		sortselector.addItemListener(new SortSelectionListener(settings));
	}
	
	public void enableSort(boolean enable) {
		sortselector.setEnabled(enable);
	}
	
	private ImageIcon displayTreeIconToUse() {
		if (settings.useTreeView()) return SemGenIcon.treeicon;
		return SemGenIcon.treeofficon;
	}
	
	private ImageIcon displayMarkerIconToUse() {
		if (settings.useDisplayMarkers()) return SemGenIcon.onicon;
		return SemGenIcon.officon;
	}
	
	private ImageIcon displayAutoCopyIconToUse(){
		if (settings.doAutoAnnotateMapped()) return SemGenIcon.copyannotationicon;
		return SemGenIcon.copyannotationofficon;
	}
	
	private String displayImportMessageToUse(){
		if (settings.showImports()) return "Showing imports";
		return "Hiding imports";
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		
		if (o == annotateitemshowmarkers){
			settings.toggleDisplayMarkers();
			annotateitemshowmarkers.setIcon(displayMarkerIconToUse());
		}
	
		if(o == annotateitemshowimports){
			// Set visbility of imported codewords and submodels
			settings.toggleShowImports();
			annotateitemshowimports.setText(displayImportMessageToUse());
		}
		
		if(o == annotateitemtreeview){
			settings.toggleTreeView();
			annotateitemtreeview.setIcon(displayTreeIconToUse());
		}
		if (o == stagebutton) {
			try {
					if(workbench.getSemSimModel().hasImportedComponents())
						SemGenError.showModelsWithImportsNotViewableInProjectTabError(false);
					
					else {
						
						if(workbench.unsavedChanges())
							globalactions.NewStageTab(workbench.getModelAccessor());
						
					}
			} catch (Exception e1) {
				e1.printStackTrace();}
		}
		
		if (o == autocopymappedvars){
			settings.toggleAutoAnnotateMapped();
			autocopymappedvars.setIcon(displayAutoCopyIconToUse());
		}
		
		if (o == annotateitemchangesourcemodelcode) {
				workbench.changeModelSourceLocation();
		}

		if(o == annotateitemexportcsv){
				workbench.exportCSV(); 
		}
		
		if (o == opentermcreator) {
			workbench.sendTermLibraryEvent(LibraryRequest.REQUEST_CREATOR);
		}
		
		if (o == annotateitemcopy) {
			workbench.sendTermLibraryEvent(LibraryRequest.REQUEST_IMPORT);
		}
		
		if (o == opentermlibrary) {
			workbench.sendTermLibraryEvent(LibraryRequest.REQUEST_LIBRARY);
		} 
	}

	class SortSelectionListener implements ItemListener {
		SemGenSettings settings;
		public SortSelectionListener(SemGenSettings sets) {
			settings =sets;
		}
		
		@Override
		public void itemStateChanged(ItemEvent arg0) {
			String obj = sortselector.getLastSelectedItem();
			if (obj == sortbytype) {
				settings.toggleByPropertyType();
		    }
			if (obj == sortbycompletion) {
				settings.toggleCompositeCompleteness();
		    }
		}
	}
}
