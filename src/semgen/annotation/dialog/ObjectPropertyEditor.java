package semgen.annotation.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import semgen.annotation.dialog.selectordialog.SemSimComponentSelectorDialog;
import semgen.resource.SemGenIcon;
import semgen.resource.uicomponents.SemGenScrollPane;
import semsim.Annotatable;
import semsim.SemSimConstants;
import semsim.model.SemSimComponent;
import semsim.model.SemSimModel;
import semsim.model.annotation.Annotation;
import semsim.model.annotation.SemSimRelation;
import semsim.model.annotation.ReferenceOntologyAnnotation;
import semsim.model.physical.MediatorParticipant;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.SinkParticipant;
import semsim.model.physical.SourceParticipant;
import semsim.owl.SemSimOWLFactory;

public class ObjectPropertyEditor extends JPanel implements ActionListener, PropertyChangeListener {
	private static final long serialVersionUID = 2140271391558665212L;
	public SemSimModel model;
	public SemSimRelation relation;
	public Annotatable subject;
	public JButton plusbutton = new JButton(SemGenIcon.plusicon);
	public JButton minusbutton = new JButton(SemGenIcon.minusicon);
	public JList<String> listcomponent = new JList<String>();
	public Hashtable<String,Object> namesandobjects = new Hashtable<String,Object>();
	public SemSimComponentSelectorDialog sscsd; 

	public ObjectPropertyEditor(SemSimModel mod, SemSimRelation rel, Annotatable sub) {
		model = mod;
		relation = rel;
		subject = sub;

		setOpaque(false);

		JLabel headerlabel = new JLabel(SemSimOWLFactory.getIRIfragment(relation.getURI().toString()));

		plusbutton.addActionListener(this);
		plusbutton.setToolTipText("Add reference term");

		minusbutton.addActionListener(this);
		minusbutton.setToolTipText("Remove selected reference term");

		JPanel headerpanel = new JPanel();
		headerpanel.setOpaque(false);
		headerpanel.add(headerlabel);
		headerpanel.add(plusbutton);
		headerpanel.add(minusbutton);

		// get the list contents
		if (this.subject != null) {
			if(relation!=SemSimConstants.HAS_SOURCE_RELATION && relation!=SemSimConstants.HAS_SINK_RELATION
					&& relation!=SemSimConstants.HAS_MEDIATOR_RELATION){
				for(Annotation ann : subject.getAnnotations()){
					if(ann.getRelation()==relation && (ann instanceof ReferenceOntologyAnnotation)){
						String desc = ((ReferenceOntologyAnnotation)ann).getValueDescription();
						namesandobjects.put(
								desc + " (" + ((ReferenceOntologyAnnotation)ann).getOntologyAbbreviation() + ")",
								subject);
					}
				}
			}
			
			else{
				if(relation == SemSimConstants.HAS_SOURCE_RELATION){
					for(SourceParticipant sp : ((PhysicalProcess)subject).getSources()){
						namesandobjects.put(sp.getPhysicalEntity().getName(), sp.getPhysicalEntity());
					}
				}
				else if(relation == SemSimConstants.HAS_SINK_RELATION){
					for(SinkParticipant sp : ((PhysicalProcess)subject).getSinks()){
						namesandobjects.put(sp.getPhysicalEntity().getName(), sp.getPhysicalEntity());
					}
				}
				else if(relation == SemSimConstants.HAS_MEDIATOR_RELATION){
					for(MediatorParticipant mp : ((PhysicalProcess)subject).getMediators()){
						namesandobjects.put(mp.getPhysicalEntity().getName(), mp.getPhysicalEntity());
					}
				}
			}
			listcomponent.setListData(namesandobjects.keySet().toArray(new String[] {}));
		}

		SemGenScrollPane scroller = new SemGenScrollPane(listcomponent);
		scroller.setPreferredSize(new Dimension(150, 70));

		setLayout(new BorderLayout());
		add(headerpanel, BorderLayout.NORTH);
		add(scroller, BorderLayout.SOUTH);
	}

	public void refreshListData() {
		String[] namesarray = namesandobjects.keySet().toArray(new String[] {});
		listcomponent.setListData(namesarray);
	}

	public void actionPerformed(ActionEvent ae) {
		Object o = ae.getSource();
		if (o == plusbutton) {
			// Edit CustomEditor so that changes to sources, sinks and mediators are stored
			if(relation!=SemSimConstants.HAS_SOURCE_RELATION && relation!=SemSimConstants.HAS_SINK_RELATION
					&& relation!=SemSimConstants.HAS_MEDIATOR_RELATION){
				
				Set<SemSimComponent> sscs = new HashSet<SemSimComponent>();
				for(String ssctempname : namesandobjects.keySet())
					sscs.add((SemSimComponent) namesandobjects.get(ssctempname));
				if(subject instanceof PhysicalEntity){
					sscsd = new SemSimComponentSelectorDialog(model.getReferencePhysicalEntities(), null, sscs, null, "Physical entities");
					sscsd.setUpUI(this);
				}
				else if (subject instanceof PhysicalProcess){
					sscsd = new SemSimComponentSelectorDialog(model.getReferencePhysicalProcesses(), null, sscs, null, "Physical processes");
					sscsd.setUpUI(this);
				}
				sscsd.optionPane.addPropertyChangeListener(this);
			}
			else{
				Set<SemSimComponent> sscs = new HashSet<SemSimComponent>();
				for(String ssctempname : namesandobjects.keySet()){
					sscs.add((SemSimComponent) namesandobjects.get(ssctempname));
				}
				sscsd = new SemSimComponentSelectorDialog(model.getPhysicalEntities(), null, sscs, null, "Physical entities");
				sscsd.setUpUI(this);
			}
		}

		if (o == minusbutton) {
			if (listcomponent.getSelectedValue() != null) {
				String removestring = listcomponent.getSelectedValue();
				namesandobjects.remove(removestring);
				listcomponent.setListData(namesandobjects.keySet().toArray(new String[] {}));
			}
		}
	}

	public void propertyChange(PropertyChangeEvent e) {
		String value = sscsd.optionPane.getValue().toString();
		if (value == "OK") {
			namesandobjects.clear();
			for(Component c : sscsd.panel.getComponents()){
				if(c instanceof JCheckBox){
					JCheckBox box = (JCheckBox)c;
					if(box.isSelected()){
						if(relation==SemSimConstants.BQB_IS_VERSION_OF_RELATION
								|| relation==SemSimConstants.PART_OF_RELATION
								|| relation==SemSimConstants.HAS_PART_RELATION
								|| relation==SemSimConstants.HAS_SOURCE_RELATION
								|| relation==SemSimConstants.HAS_SINK_RELATION
								|| relation== SemSimConstants.HAS_MEDIATOR_RELATION){
							// If the relation is for a reference ontology annotation, use the corresponding physical model component as the object in namesandobjects
							namesandobjects.put(box.getName(), 
									((PhysicalModelComponent)sscsd.nameobjectmap.get(box.getName())));
						}
						else{
							namesandobjects.put(box.getName(), 
									((PhysicalModelComponent)sscsd.nameobjectmap.get(box.getName())));
						}
					}
				}
			}
			listcomponent.setListData(namesandobjects.keySet().toArray(new String[]{}));
		}

		sscsd.optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
		sscsd.setVisible(false);
	}
}
