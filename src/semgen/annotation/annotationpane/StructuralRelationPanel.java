package semgen.annotation.annotationpane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.semanticweb.owlapi.model.OWLException;

import semgen.resource.SemGenFont;
import semsim.SemSimConstants;
import semsim.model.annotation.SemSimRelation;
import semsim.model.annotation.StructuralRelation;

public class StructuralRelationPanel extends JPanel implements ActionListener{

	private static final long serialVersionUID = 4319031091828000135L;
	public StructuralRelationComboBox srcb;
	public StructuralRelation structuralRelation;
	private Map<String, SemSimRelation> namestructuralrelationmap = new HashMap<String, SemSimRelation>();
	
	public StructuralRelationPanel(StructuralRelation relation){
		structuralRelation = relation;
		namestructuralrelationmap.put("contained_in", SemSimConstants.CONTAINED_IN_RELATION);
		namestructuralrelationmap.put("part_of", SemSimConstants.PART_OF_RELATION);
		
		setLayout(new BorderLayout());
		srcb = new StructuralRelationComboBox(relation);
		srcb.addActionListener(this);
		add(srcb, BorderLayout.WEST);
		add(Box.createGlue(), BorderLayout.EAST);	
	}

	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getSource()==srcb){
			structuralRelation = (StructuralRelation) namestructuralrelationmap.get(srcb.getSelectedItem());
			try {
				updateCompositeAnnotationFromUIComponents();
			} catch (OWLException e) {
				e.printStackTrace();
			}
		}
	}

	private class StructuralRelationComboBox extends JComboBox<String>{
		private static final long serialVersionUID = -4455216268075370509L;

		public StructuralRelationComboBox(StructuralRelation rel){
			addItem("contained_in");
			addItem("part_of");
			setSelectedItem(rel.getURI().getFragment());
			setFont(SemGenFont.defaultItalic());
			setPreferredSize(new Dimension(135,30));
			setMaximumSize(new Dimension(135,30));
		}
	}
}


