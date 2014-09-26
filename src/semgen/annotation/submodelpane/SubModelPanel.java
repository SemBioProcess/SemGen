package semgen.annotation.submodelpane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.semanticweb.owlapi.model.OWLException;

import semgen.SemGenSettings;
import semgen.annotation.uicomponents.AnnotationObjectButton;
import semgen.annotation.uicomponents.ComponentPane;
import semgen.annotation.workbench.SubModelAnnotations;
import semgen.resource.SemGenIcon;

public class SubModelPanel extends ComponentPane implements ActionListener, Observer {
	private static final long serialVersionUID = 1L;
	private JButton addsubmodelbutton = new JButton(SemGenIcon.plusicon);
	private JButton removesubmodelbutton = new JButton(SemGenIcon.minusicon);
	private SubModelAnnotations sma;
	
	public SubModelPanel(SubModelAnnotations canvas, SemGenSettings sets) {
		super(sets);
		sma = canvas;
		addsubmodelbutton.addActionListener(this);
		removesubmodelbutton.addActionListener(this);
	}

	public void makeButtons() {
		int nbuttons = settings.showImports() ? sma.getNumberVisible() : sma.getNumberofElements(); 
		for (int i=0; i<nbuttons; i++) {
			aoblist.add(new SubmodelButton());
		}
		add(Box.createGlue());
		setTitle();
	}
	
	public void removeButton(AnnotationObjectButton key) {
		Integer index = aobmap.inverseBidiMap().get(key);
		aoblist.remove(key);
		aobmap.remove(index);
		sma.removeSubmodel(index);
	}
	
	public void addNewSubmodelButton() throws OWLException {
		String newname = JOptionPane.showInputDialog(this,"Enter a name for the new sub-model");
		if (newname != "") {
			aoblist.add(new SubmodelButton());			
			sma.addSubModel(newname);
		}
	}
		
	@Override
	public void refreshAnnotatableElements() {
		int nb = aoblist.size();
		//If number of submodels has changed redraw buttons
		if (sma.getNumberVisible() != nb) {
			removeAll();
			createAddRemovePanel();
			makeButtons();
		}
		for (AnnotationObjectButton smb : aoblist) {
			for (int i=0; i<sma.getNumberofElements(); i++) {
				if (!aobmap.containsKey(i) && sma.isVisible(i)) {
					aobmap.put(i, smb);
					smb.assignButton(sma.getName(i), new Boolean[]{sma.hasFreeText(i), sma.hasSingular(i)});	
				}
			}
		}
		
	}
	
	private void createAddRemovePanel() {
		JPanel componentaddremovepanel = new JPanel();
		componentaddremovepanel.setOpaque(false);
		componentaddremovepanel.setLayout(new BoxLayout(componentaddremovepanel, BoxLayout.X_AXIS));
		componentaddremovepanel.add(addsubmodelbutton);
		componentaddremovepanel.add(removesubmodelbutton);
		add(componentaddremovepanel);
	}
	
	private void setTitle() {
		setName("Sub-models " + "(" + sma.countSubmodels() + " total, " + aoblist.size() + " editable)" );
		if (sma.countSubmodels()==0) add(new JLabel("No sub-models found"));
	}
	
	@Override
	public void update(Observable o, Object arg) {
		refreshAnnotatableElements();
		repaint();
		validate();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		
		if (o == addsubmodelbutton){
			try {
				addNewSubmodelButton();
			} catch (OWLException e1) {
				e1.printStackTrace();
			}
		}
		if(o == removesubmodelbutton){
			if(removeSubModelDialog() == JOptionPane.YES_OPTION) {
					removeButton(aobmap.get(sma.getFocusIndex()));
			}
		}
		
	}

	public int removeSubModelDialog() {
		return 	JOptionPane.showConfirmDialog(getParent(), 
		"Are you sure you want to remove component " + sma.getName() + "?", "Confirm removal", JOptionPane.YES_NO_OPTION);
	}

	@Override
	public void freeTextRequest(int index) {
		sma.setFreeText(index);
		
	}

	@Override
	public void singAnnRequest(int index) {
		sma.setSingular(index);
		
	}
	

}
