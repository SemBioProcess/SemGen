package semgen.annotation;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;

import semgen.SemGenSettings;
import semgen.UniversalActions;
import semgen.annotation.codewordpane.CodewordButton;
import semgen.annotation.dialog.LegacyCodeChooser;
import semgen.annotation.dialog.ModelLevelMetadataEditor;
import semgen.annotation.dialog.SemanticSummaryDialog;
import semgen.annotation.dialog.referencedialog.AddReferenceClassDialog;
import semgen.annotation.dialog.selectordialog.AnnotationComponentReplacer;
import semgen.annotation.dialog.selectordialog.RemovePhysicalComponentDialog;
import semgen.annotation.dialog.textminer.TextMinerDialog;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.ReferenceTermToolbox;
import semgen.resource.CSVExporter;
import semgen.resource.SemGenIcon;
import semgen.resource.uicomponents.SemGenMenu;
import semsim.SemSimConstants;
import semsim.model.physical.CompositePhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;

public class AnnotatorToolbar extends JToolBar implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JButton extractorbutton, coderbutton;
	SemGenSettings settings;
	AnnotatorWorkbench canvas;
	ReferenceTermToolbox toolbox;
	UniversalActions uacts;
	public JMenuItem semanticsummary;

	public JCheckBoxMenuItem annotateitemshowimports;
	private JMenuItem annotateitemshowmarkers;
	private JMenuItem annotateitemtreeview;
	private JMenuItem annotateitemaddrefterm;
	private JMenuItem annotateitemremoverefterm;
	private JMenuItem annotateitemchangesourcemodelcode;
	private JMenuItem annotateitemreplacerefterm;
	private JMenuItem annotateitemcopy;
	private JMenuItem annotateitemharvestfromtext;
	private JMenuItem annotateitemeditmodelanns;
	private JMenuItem annotateitemexportcsv;
	private JMenuItem annotateitemthai;

	public AnnotatorToolbar(UniversalActions ua, SemGenSettings sets, AnnotatorWorkbench can, ReferenceTermToolbox rtt) {
		canvas =can;
		toolbox = rtt;
		uacts=ua;
		settings = sets;
		extractorbutton = new JButton(SemGenIcon.extractoricon);
		extractorbutton.setToolTipText("Open this model in Extractor");
		extractorbutton.setSize(new Dimension(10, 10));
		extractorbutton.setPreferredSize(new Dimension(20, 20));

		coderbutton = new JButton(SemGenIcon.codericon);
		coderbutton.setToolTipText("Encode this model for simulation");
		coderbutton.setPreferredSize(new Dimension(20, 20));
		
		add(new JSeparator());

		semanticsummary = formatMenuItem(semanticsummary, "Biological summary", KeyEvent.VK_U, true, true);
		semanticsummary.setToolTipText("View summary of the biological concepts used in the model");
		
		annotateitemharvestfromtext = formatMenuItem(annotateitemharvestfromtext, "Find terms in text", KeyEvent.VK_F, true, true);
		annotateitemharvestfromtext.setToolTipText("Use natural language processing to find reference ontology terms in a block of text");

		annotateitemaddrefterm = formatMenuItem(annotateitemaddrefterm, "Add reference term", KeyEvent.VK_D,true,true);
		annotateitemaddrefterm.setToolTipText("Add a reference ontology term to use for annotating this model");
		
		annotateitemremoverefterm = formatMenuItem(annotateitemremoverefterm, "Remove annotation component", KeyEvent.VK_R,true,true);
		annotateitemremoverefterm.setToolTipText("Remove a physical entity or process term from the model");
		
		annotateitemcopy = formatMenuItem(annotateitemcopy, "Import annotations", KeyEvent.VK_I,true,true);
		annotateitemcopy.setToolTipText("Annotate codewords using data from identical codewords in another model");
		
		annotateitemreplacerefterm = formatMenuItem(annotateitemreplacerefterm, "Replace reference term", KeyEvent.VK_P, true, true);
		annotateitemreplacerefterm.setToolTipText("Replace a reference ontology term with another");
		
		add(new JSeparator());
		
		annotateitemchangesourcemodelcode = formatMenuItem(annotateitemchangesourcemodelcode, "Change legacy code", KeyEvent.VK_K, true, true);
		annotateitemchangesourcemodelcode.setToolTipText("Link the SemSim model with its computational code");
		
		annotateitemexportcsv = formatMenuItem(annotateitemexportcsv, "Export codeword table", KeyEvent.VK_T, true, true);
		annotateitemexportcsv.setToolTipText("Create a .csv file that tabulates model codeword annotations for use in spreadsheets, manuscript preparation, etc.");
		
		annotateitemeditmodelanns = formatMenuItem(annotateitemeditmodelanns, "Edit model-level annotations", KeyEvent.VK_J,true, true);
		annotateitemeditmodelanns.setToolTipText("Edit metadata for this SemSim model");
		
		add(new JSeparator());
		
		annotateitemtreeview = new JCheckBoxMenuItem("Tree view");
		annotateitemtreeview.setSelected(settings.useTreeView());
		annotateitemtreeview.addActionListener(this);
		annotateitemtreeview.setToolTipText("Display codewords and submodels within the submodel tree");
		add(annotateitemtreeview);
		
		if(!settings){
			annotateitemaddrefterm.setEnabled(true);
			annotateitemremoverefterm.setEnabled(true);
		}
		
		addButton(extractorbutton);
		addButton(coderbutton);
		setAlignmentY(JPanel.TOP_ALIGNMENT);
	}
	
	private void addButton(JButton button) {
		button.addActionListener(this);
		button.setRolloverEnabled(true);
		button.setAlignmentY(JButton.TOP_ALIGNMENT);
		add(button);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		
	if(o == semanticsummary){
			canvas.summarizeSemantics();
	}
	
	if(o == annotateitemharvestfromtext){
		toolbox.runTextMiner();
		}

	if (o == annotateitemaddrefterm) {
		toolbox.addTermRequest();
	}
	if (o == annotateitemremoverefterm) {
		canvas.removeRefTermfromModel();
	}

	if (o == annotateitemchangesourcemodelcode) {
		canvas.changeModelSourceFile();
	}

	if(o == annotateitemexportcsv){
		canvas.exportCSV();
	}
	
	if(o == annotateitemeditmodelanns){
			new ModelLevelMetadataEditor();
	}

	if (o == annotateitemreplacerefterm) {
		canvas.replaceTerminModel();
	}

	if (o == annotateitemcopy) {
		canvas.importandAnnotate();
	}
			
	if(o == annotateitemthai){
		canvas.doBatchCellML();
	}
	
	if (o == annotateitemshowmarkers){
		settings.toggleDisplayMarkers();
	}
	// Set visbility of imported codewords and submodels
	if(o == annotateitemshowimports){	
		settings.toggleShowImports();
	}
		
		if (o == extractorbutton) {
			try {
				Boolean open = true;
				if (!canvas.getModelSaved()) {
					int savefilechoice = JOptionPane.showConfirmDialog(getParent(),
							"Save changes before opening Extractor?",
							"There are unsaved changes",
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if(savefilechoice == JOptionPane.CANCEL_OPTION){open = false;}
					else {
						if (savefilechoice == JOptionPane.YES_OPTION) {
							open = canvas.saveModel().exists();
						}
					}
				}
				if(open){
					uacts.NewExtractorTab();
				}
			} catch (Exception e1) {e1.printStackTrace();}
		}

		if (o == coderbutton) {
			if (!canvas.getModelSaved()) {
				int savefilechoice = JOptionPane.showConfirmDialog(getParent(),
						"Save changes before encoding model?",
						"There are unsaved changes",
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (savefilechoice == JOptionPane.YES_OPTION) {
					canvas.saveModel();
				}
			}
			canvas.encodeModel(); 
		}
		
	}
	
	// Format menu items, assign shortcuts, action listeners
	public JMenuItem formatMenuItem(JMenuItem item, String text, Integer accelerator, Boolean enabled, Boolean addactionlistener){
		item = new JMenuItem(text);
		item.setEnabled(enabled);
		if(accelerator!=null){item.setAccelerator(KeyStroke.getKeyStroke(accelerator, SemGenMenu.maskkey));}
		if(addactionlistener){item.addActionListener(this);}
		return item;
	}
}
