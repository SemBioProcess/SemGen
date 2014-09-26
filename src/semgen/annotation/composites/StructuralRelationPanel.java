package semgen.annotation.composites;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JPanel;

import org.semanticweb.owlapi.model.OWLException;

import semgen.SemGenGUI;
import semgen.annotation.AnnotationDialog;
import semsim.model.annotation.StructuralRelation;

public class StructuralRelationPanel extends JPanel implements ActionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4319031091828000135L;
	public AnnotationDialog anndialog;
	public StructuralRelationComboBox srcb;
	public StructuralRelation structuralRelation;


	public StructuralRelationPanel(AnnotationDialog anndialog, StructuralRelation relation){
		this.anndialog = anndialog;
		structuralRelation = relation;
		setLayout(new BorderLayout());
		srcb = new StructuralRelationComboBox(relation);
		srcb.addActionListener(this);
		add(srcb, BorderLayout.WEST);
		add(Box.createGlue(), BorderLayout.EAST);
		
		
	}

	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getSource()==srcb){
			structuralRelation = (StructuralRelation) SemGenGUI.namestructuralrelationmap.get(srcb.getSelectedItem());
			try {
				anndialog.updateCompositeAnnotationFromUIComponents();
			} catch (OWLException e) {
				e.printStackTrace();
			}
		}
	}
}
