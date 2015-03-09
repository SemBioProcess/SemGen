package semgen.annotation.dialog.referenceclass.compositedialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JPanel;

import semgen.SemGenSettings;
import semgen.annotation.AnnotatorTab;
import semgen.annotation.annotatorpane.composites.StructuralRelationPanel;
import semgen.annotation.dialog.CustomPhysicalComponentEditor;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semsim.model.SemSimModel;
import semsim.model.physical.PhysicalEntity;

public class CreateCompositePanel extends Box implements ActionListener{
	private static final long serialVersionUID = -3955122899870904199L;
	private AnnotatorWorkbench workbench;
	private AnnotatorTab annotator;
	private JButton addentbutton = new JButton("Add entity");
	private ArrayList<EntitySelectionPanel> entpans = new ArrayList<EntitySelectionPanel>();
	private ArrayList<StructuralRelationPanel> lbllist = new ArrayList<StructuralRelationPanel>();
	private TreeMap<String, PhysicalEntity> idpemap;
	private ArrayList<String> peidlist = new ArrayList<String>();
	
	public CreateCompositePanel(AnnotatorWorkbench bench, AnnotatorTab tab){
		super(BoxLayout.Y_AXIS);
		annotator = tab;
		workbench = bench;
		setAlignmentX(Box.LEFT_ALIGNMENT);
		createUI();
		loadIDMap();
	}
	
	public void createUI(){
		JPanel addentprocpanel = new JPanel();
		addentprocpanel.setLayout(new BoxLayout(addentprocpanel, BoxLayout.X_AXIS));
		addentprocpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		addentprocpanel.add(addentbutton);
		addentbutton.addActionListener(this);
		addentprocpanel.setBackground(SemGenSettings.lightblue);
		
		JPanel addempanel = new JPanel(new BorderLayout());
		addempanel.add(addentprocpanel, BorderLayout.WEST);
				
		add(addempanel);
		alignAndPaint();
	}
	
	private void loadIDMap() {
		idpemap = workbench.getPhysicalEntityIDList();
		peidlist.clear();
		peidlist.addAll(idpemap.keySet());
	}
	
	private void refreshLists() {
		loadIDMap();
		for (SemSimEntityPanel p : entpans) {
			p.setComboBoxList(new DefaultComboBoxModel<String>(peidlist.toArray(new String[]{})));
		}
	}
	
	public void alignAndPaint(){
		int x = 0;
		int i = 0;
		for(SemSimEntityPanel p : entpans){
			p.setBorder(BorderFactory.createEmptyBorder(0, x, 5, 0));
			if (i < lbllist.size()) {
				lbllist.get(i).setBorder(BorderFactory.createEmptyBorder(0, x+15, 5, 0));
			}
			x = x + 15;
			i++;
		}
		validate();
	}

	private void addEntityPanel() {
		if (entpans.size()!=0) {
			StructuralRelationPanel lbl = new StructuralRelationPanel(null);
			lbllist.add(lbl); 
			add(lbl);
		}
		EntitySelectionPanel esp = new EntitySelectionPanel();
		entpans.add(esp);
		add(esp, BorderLayout.NORTH);
	}
	
	private void removeEntity(EntitySelectionPanel ent) {
		int index = entpans.indexOf(ent);
		if (index!=0) {
			remove(lbllist.get(index-1));
			lbllist.remove(index-1);
		}
		entpans.remove(ent);
		remove(ent);
		componentChanged();
	}
	
	public void actionPerformed(ActionEvent arg0) {
		// If the "Add entity" button is pressed
		if(arg0.getSource() == addentbutton){
			addEntityPanel();
			alignAndPaint();
			componentChanged();
		}
	}

	public ArrayList<PhysicalEntity> getListofEntities() {
		ArrayList<PhysicalEntity> pelist = new ArrayList<PhysicalEntity>();
		String name;
		if (entpans.isEmpty()) return null;
		for (EntitySelectionPanel pan : entpans) {
			name = pan.getSelection();
			if (name.equals(SemSimModel.unspecifiedName)) {				
				return null;
			}
			PhysicalEntity pe = idpemap.get(name);
			
			pelist.add(pe);
		}
		
		return pelist;
	}
	
	public void componentChanged() {}
	
	class EntitySelectionPanel extends SemSimEntityPanel {
		private static final long serialVersionUID = 1L;

		public EntitySelectionPanel() {
			super(workbench, annotator, new DefaultComboBoxModel<String>(peidlist.toArray(new String[]{})));
		}

		protected void modifyLabelClicked() {
			String key = (String) combobox.getSelectedItem();
			CustomPhysicalComponentEditor cpce = new CustomPhysicalComponentEditor(workbench, idpemap.get(key));
			if (cpce.getCustomTerm()!=null) {
				refreshLists();
			}
		}
		
		@Override
		protected void eraseLabelClicked() {
			removeEntity(this);
			alignAndPaint();
		}

		@Override
		protected void refreshAndSet(String sel) {
			refreshLists();
			combobox.setSelectedItem(sel);
			
		}

		@Override
		public PhysicalEntity getPhysicalEntity(String id) {
			return idpemap.get(id);
		}
		
	}
	
}
