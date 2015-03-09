package semgen.annotation.dialog.referenceclass.compositedialog;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import semgen.annotation.AnnotatorTab;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.utilities.SemGenError;
import semgen.utilities.SemGenFont;
import semgen.utilities.uicomponent.SemGenDialog;
import semsim.SemSimConstants;
import semsim.annotation.StructuralRelation;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.object.CompositePhysicalEntity;

public class CreateCompositeDialog extends SemGenDialog implements PropertyChangeListener{
	private static final long serialVersionUID = 1L;
	private CreateCompositePanel ccpanel;
	private boolean keepopen;
	
	private JOptionPane optionPane;
	private JTextArea utilarea = new JTextArea();
	private AnnotatorWorkbench workbench; 
	private CompositePhysicalEntity cpe = null;

	public CreateCompositeDialog(AnnotatorWorkbench wb, AnnotatorTab tab, boolean remainopen) {
		super("Create Composite");
		workbench = wb;
		keepopen = remainopen;
		
		utilarea.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		utilarea.setBackground(new Color(0,0,0,0));
		utilarea.setLineWrap(true);
		utilarea.setWrapStyleWord(true);
		utilarea.setEditable(false);
		utilarea.setFont(SemGenFont.defaultBold(-1));
		
		ccpanel = new CreateCompositePanel(workbench, tab) {
			private static final long serialVersionUID = 1L;

			public void componentChanged() {
				newComponent();
			}
		};
		
		Object[] options = {"Add Composite", "Close"};
		Object[] array = { utilarea, ccpanel };

		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
		optionPane.addPropertyChangeListener(this);
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);
		setContentPane(optionPane);
		showDialog();
	}
	
	protected void newComponent() {
		ccpanel.revalidate();
		validate();
		pack();
	}
	
	public CompositePhysicalEntity makeComposite() {
		ArrayList<PhysicalEntity> pelist = ccpanel.getListofEntities();
		if (pelist == null) {
			return null;
		}
		
		ArrayList<StructuralRelation> rellist = new ArrayList<StructuralRelation>();
		for (int i=0; i < pelist.size()-1; i++) {
			rellist.add(SemSimConstants.PART_OF_RELATION);
		}
		
		cpe = new CompositePhysicalEntity(pelist, rellist);
		workbench.getSemSimModel().addCompositePhysicalEntity(cpe);
		
		return cpe;
	}
	
	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getPropertyName()=="value") {
			if (optionPane.getValue() == JOptionPane.UNINITIALIZED_VALUE) return;
			
			String value = optionPane.getValue().toString();
			
			if (value == "Add Composite") {
				if (makeComposite()==null) {
					SemGenError.showError(this, "Please specify all annotations for all entities.", "Unspecified Terms");
					return;
				}
				workbench.compositeChanged();
			}
			
			if (value == "Close" || !keepopen) {
				dispose();
				return;
			}
			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
		}
	}
	
	public CompositePhysicalEntity getComposite() {
		return cpe;
	}
}
