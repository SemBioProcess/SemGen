package semgen.annotation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;

import semgen.SemGen;
import semgen.SemGenGUI;
import semgen.SemGenSettings;
import semgen.annotation.dialog.AnnotationComponentReplacer;
import semgen.annotation.dialog.ModelLevelMetadataEditor;
import semgen.annotation.dialog.RemovePhysicalComponentDialog;
import semgen.annotation.dialog.referenceclass.AddReferenceClassDialog;
import semgen.resource.CSVExporter;
import semgen.resource.SemGenIcon;
import semgen.resource.uicomponent.DropDownCheckList;
import semgen.resource.uicomponent.SemGenToolbarButton;
import semsim.SemSimConstants;
import semsim.model.physical.CompositePhysicalEntity;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;

public class AnnotatorToolBar extends JToolBar implements ActionListener {
	private static final long serialVersionUID = 1L;

	AnnotatorTab anntab;
	private SemGenToolbarButton extractorbutton = new SemGenToolbarButton(SemGenIcon.extractoricon);
	private SemGenToolbarButton coderbutton = new SemGenToolbarButton(SemGenIcon.codericon);
	private JButton annotateitemchangesourcemodelcode = new JButton("Set Source Code");;
	private JButton annotateitemcopy = new JButton("Import annotations");
	private JButton annotateitemeditmodelanns = new JButton("Edit model-level annotations");
	private JButton annotateitemexportcsv = new JButton("Export codeword table");
	private DropDownCheckList sortselector = new DropDownCheckList("Sort Options");
	private JButton annotateitemshowmarkers = new JButton("Display markers");
	private JButton annotateitemshowimports = new JButton("Show imports");
	public JButton annotateitemreplacerefterm = new JButton("Replace reference term");
	private JButton annotateitemaddrefterm= new JButton("Add reference term");
	private JButton annotateitemremoverefterm = new JButton("Remove annotation component");
	private JButton annotateitemtreeview = new JButton("Tree view");
	private String sortbytype = new String("By Type");
	private String sortbycompletion = new String("By Composite Completeness");
	protected SemGenSettings settings;

	public AnnotatorToolBar(AnnotatorTab tab, SemGenSettings sets) {
		anntab = tab;
		settings=sets;
		this.setFloatable(false);
		setOpaque(true);
		
		extractorbutton.setToolTipText("Open this model in Extractor");
		extractorbutton.addActionListener(this);

		coderbutton.setToolTipText("Encode this model for simulation");
		coderbutton.addActionListener(this);
		
		annotateitemcopy.addActionListener(this);
		annotateitemcopy.setToolTipText("Annotate codewords using data from identical codewords in another model");
		
		annotateitemcopy.setEnabled(true);
		
		annotateitemchangesourcemodelcode.addActionListener(this);
		annotateitemchangesourcemodelcode.setToolTipText("Link the SemSim model with its computational code");
		
		annotateitemexportcsv.setToolTipText("Create a .csv file that tabulates model codeword annotations for use in spreadsheets, manuscript preparation, etc.");
		annotateitemexportcsv.addActionListener(this);

		annotateitemeditmodelanns.setToolTipText("Edit metadata for this SemSim model");
		annotateitemeditmodelanns.addActionListener(this);

		sortselector.addItem(sortbytype, settings.organizeByPropertyType());
		sortselector.addItem(sortbycompletion, settings.organizeByCompositeCompleteness());
		
		annotateitemshowimports.addActionListener(this);
		annotateitemshowimports.setToolTipText("Make imported codewords and submodels visible");
		
		annotateitemshowmarkers.addActionListener(this);
		annotateitemshowmarkers.setToolTipText("Display markers that indicate a codeword's property type");
		
		annotateitemtreeview.addActionListener(this);
		annotateitemtreeview.setToolTipText("Display codewords and submodels within the submodel tree");
		
		annotateitemaddrefterm.addActionListener(this);
		annotateitemaddrefterm.setToolTipText("Add a reference ontology term to use for annotating this model");
		
		annotateitemremoverefterm.addActionListener(this);
		annotateitemremoverefterm.setToolTipText("Remove a physical entity or process term from the model");

		annotateitemreplacerefterm.setToolTipText("Replace a reference ontology term with another");

		add(annotateitemtreeview);
		add(annotateitemshowmarkers);
		add(sortselector);
		sortselector.addItemListener(new SortSelectionListener(settings));
		add(annotateitemshowimports);
		addSeparator();
		
		add(annotateitemchangesourcemodelcode);
		add(annotateitemcopy);
		add(annotateitemexportcsv);
		add(annotateitemeditmodelanns);
		addSeparator();
		
		add(annotateitemremoverefterm);
		add(annotateitemaddrefterm);
		add(annotateitemreplacerefterm);
		addSeparator();
		
		add(extractorbutton);
		add(coderbutton);
		setAlignmentY(JPanel.TOP_ALIGNMENT);
	}
	
	public void enableSort(boolean enable) {
		sortselector.setEnabled(enable);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		
		if (o == annotateitemshowmarkers){
			settings.toggleDisplayMarkers();
				for(String s : anntab.codewordbuttontable.keySet()){
					CodewordButton cb = anntab.codewordbuttontable.get(s);
					((CodewordButton)cb).propoflabel.setVisible(settings.useDisplayMarkers());
					cb.validate();
				}
		}
	
		if(o == annotateitemshowimports){
			// Set visbility of imported codewords and submodels
			settings.toggleShowImports();
			anntab.refreshAnnotatableElements();
		}
		
		if(o == annotateitemtreeview){
			settings.toggleTreeView();
			anntab.refreshAnnotatableElements();
		}
		if (o == extractorbutton) {
			try {
				if(anntab.unsavedChanges()){
					SemGenGUI.NewExtractorTask task = new SemGenGUI.NewExtractorTask(anntab.sourcefile);
					task.execute();
				}
			} catch (Exception e1) {
				e1.printStackTrace();}
		}
		
		if (o == annotateitemchangesourcemodelcode) {
				anntab.changeLegacyLocation();
		}

		if(o == annotateitemexportcsv){
				try {
					new CSVExporter(anntab.semsimmodel).exportCodewords();
				} catch (Exception e1) {e1.printStackTrace();} 
		}
		
		if(o == annotateitemeditmodelanns){
				new ModelLevelMetadataEditor(anntab);
		}
		
		if (o == annotateitemcopy) {
				try {
					new AnnotationCopier(anntab);
				} catch (OWLException | CloneNotSupportedException e1) {
					e1.printStackTrace();
				}
		}
		
		if (o == annotateitemaddrefterm) {
				new AddReferenceClassDialog(anntab, SemSimConstants.ALL_SEARCHABLE_ONTOLOGIES, 
						new Object[]{"Add as entity","Add as process","Close"}, anntab.semsimmodel).packAndSetModality();
			} 
		
		if (o == annotateitemremoverefterm) {
				Set<PhysicalModelComponent> pmcs = new HashSet<PhysicalModelComponent>();
				for(PhysicalModelComponent pmc : anntab.semsimmodel.getPhysicalModelComponents()){
					if(!(pmc instanceof CompositePhysicalEntity) && (pmc instanceof PhysicalEntity || pmc instanceof PhysicalProcess))
						pmcs.add(pmc);
				}
				new RemovePhysicalComponentDialog(anntab, pmcs, null, false, "Select components to remove");
			}
		
		if (o == annotateitemreplacerefterm) {
				try {
					new AnnotationComponentReplacer(anntab);
				} catch (OWLException e1) {
					e1.printStackTrace();
				}
		}
		
		if (o == coderbutton) {
			String filenamesuggestion = null;
			if(anntab.sourcefile!=null) filenamesuggestion = anntab.sourcefile.getName().substring(0, anntab.sourcefile.getName().lastIndexOf("."));
			try {
				if (!anntab.getModelSaved()) {
					int savefilechoice = JOptionPane.showConfirmDialog(this,
							"Save changes before encoding model?",
							"There are unsaved changes",
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (savefilechoice == JOptionPane.YES_OPTION) {
						File tempfile = new File(SemGen.tempdir.getAbsoluteFile() + "/" + SemGenSettings.sdf.format(SemGen.datenow) + ".owl");

						anntab.manager.saveOntology(anntab.semsimmodel.toOWLOntology(), new RDFXMLOntologyFormat(), IRI.create(tempfile));
						if(SemGenGUI.SaveAction(this, anntab.lastSavedAs)) SemGenGUI.startEncoding(anntab.semsimmodel, filenamesuggestion);
					}
					else if(savefilechoice == JOptionPane.NO_OPTION)
						SemGenGUI.startEncoding(anntab.semsimmodel, filenamesuggestion);
				}
				else SemGenGUI.startEncoding(anntab.semsimmodel, filenamesuggestion); 
			} 
			catch (OWLException e3) {
				e3.printStackTrace();
			} 
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
			anntab.AlphabetizeAndSetCodewords();
		}
	}
	
}
