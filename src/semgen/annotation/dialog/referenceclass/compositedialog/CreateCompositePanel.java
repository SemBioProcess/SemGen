package semgen.annotation.dialog.referenceclass.compositedialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import semgen.SemGenSettings;
import semgen.annotation.annotatorpane.composites.SemSimComponentAnnotationPanel;
import semgen.annotation.annotatorpane.composites.StructuralRelationPanel;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semsim.Annotatable;
import semsim.SemSimConstants;
import semsim.annotation.StructuralRelation;
import semsim.model.SemSimModel;
import semsim.model.physical.PhysicalEntity;

public class CreateCompositePanel extends Box implements ActionListener{
	private static final long serialVersionUID = -3955122899870904199L;
	private AnnotatorWorkbench workbench;
	public JButton addentbutton = new JButton("Add entity");
	
	public CreateCompositePanel(AnnotatorWorkbench bench){
		super(BoxLayout.Y_AXIS);
		workbench = bench;
		this.setBackground(new Color(207, 215, 252));
		setAlignmentX(Box.LEFT_ALIGNMENT);
	}
		
	public void refreshUI(){
		removeAll();
		if(addentbutton.getActionListeners().length==0){
			addentbutton.addActionListener(this);
		}
		
		JPanel addentprocpanel = new JPanel();
		addentprocpanel.setLayout(new BoxLayout(addentprocpanel, BoxLayout.X_AXIS));
		addentprocpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		addentprocpanel.add(addentbutton);
		addentprocpanel.setBackground(SemGenSettings.lightblue);
		
		JPanel addempanel = new JPanel(new BorderLayout());
		addempanel.add(addentprocpanel, BorderLayout.WEST);
				
		add(addempanel);
		alignAndPaint();
	}
	
	
	public void alignAndPaint(){
		int x = 15;
		for(Component c : getComponents()){
			if(c instanceof JComponent) ((JComponent)c).setBorder(BorderFactory.createEmptyBorder(0, x, 5, 0));
			c.setBackground(SemGenSettings.lightblue);
			x = x + 15;
		}
		validate();
	}

	public void actionPerformed(ActionEvent arg0) {
		// If the "Add entity" button is pressed
		if(arg0.getSource() == addentbutton){
			ArrayList<PhysicalEntity> ents = new ArrayList<PhysicalEntity>();
			ArrayList<StructuralRelation> rels = new ArrayList<StructuralRelation>();
			for(Component c : getComponents()){
				if(c instanceof SemSimComponentAnnotationPanel){
					Annotatable smc = ((SemSimComponentAnnotationPanel)c).smc;
					if(smc instanceof PhysicalEntity){
						ents.add((PhysicalEntity)smc);
					}
				}
				if(c instanceof StructuralRelationPanel){
					StructuralRelationPanel srp = (StructuralRelationPanel)c;
					rels.add(srp.structuralRelation);
				}
			}
			ents.add(workbench.getSemSimModel().getCustomPhysicalEntityByName(SemSimModel.unspecifiedName));
			rels.add(SemSimConstants.PART_OF_RELATION);
		}
			
		refreshUI();
	}

}
