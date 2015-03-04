package semgen.annotation.annotatorpane.composites;

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
import javax.swing.JLabel;
import javax.swing.JPanel;

import semgen.SemGen;
import semgen.SemGenSettings;
import semgen.annotation.annotatorpane.AnnotationPanel;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.utilities.SemGenFont;
import semsim.Annotatable;
import semsim.PropertyType;
import semsim.SemSimConstants;
import semsim.annotation.StructuralRelation;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.CompositePhysicalEntity;

public class CompositeAnnotationPanel extends Box implements ActionListener{
	private static final long serialVersionUID = -3955122899870904199L;
	private AnnotatorWorkbench workbench;
	public DataStructure datastructure;
	public AnnotationPanel ad;
	public JButton addpropertybutton = new JButton("Add property");
	public JButton addentbutton = new JButton("Add entity");
	public JButton addprocbutton = new JButton("Add process");
	protected SemGenSettings settings;
	
	public CompositeAnnotationPanel(AnnotatorWorkbench bench, int orientation, SemGenSettings sets, AnnotationPanel ad, DataStructure ds){
		super(orientation);
		workbench = bench;
		settings = sets;
		this.setBackground(new Color(207, 215, 252));
		this.ad = ad;
		setAlignmentX(Box.LEFT_ALIGNMENT);
		datastructure = ds;
	}
		
	public void refreshUI(){
		removeAll();
		if(addentbutton.getActionListeners().length==0 && addprocbutton.getActionListeners().length==0){
			addentbutton.addActionListener(this);
			addprocbutton.addActionListener(this);
		}
		
		JPanel addentprocpanel = new JPanel();
		addentprocpanel.setLayout(new BoxLayout(addentprocpanel, BoxLayout.X_AXIS));
		addentprocpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		addentprocpanel.add(addentbutton);
		addentprocpanel.add(addprocbutton);
		addentprocpanel.setBackground(SemGenSettings.lightblue);
		
		JPanel addempanel = new JPanel(new BorderLayout());
		addempanel.add(addentprocpanel, BorderLayout.WEST);
		
		if(datastructure.getPhysicalProperty()!=null){
			add(new SemSimComponentAnnotationPanel(workbench, ad, settings, datastructure.getPhysicalProperty()));
		}
		else{
			add(addpropertybutton);
		}
		JPanel propofpanel = new JPanel( new BorderLayout() );
		
		JLabel propertyoflabel = new JLabel("property_of");
        propertyoflabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        propertyoflabel.setFont(SemGenFont.defaultItalic());
        propertyoflabel.setBorder(BorderFactory.createEmptyBorder(0, ad.indent, 0, 0));
        propofpanel.add(propertyoflabel);
        add(propofpanel);
		
		if(datastructure.getPhysicalProperty()!=null){
			// If we've got a target for the property
			if(datastructure.getPhysicalProperty().getPhysicalPropertyOf()!=null){
				PhysicalModelComponent pmc = datastructure.getPhysicalProperty().getPhysicalPropertyOf();
				// If it's a composite physical entity, enter iteratively
				if(pmc instanceof CompositePhysicalEntity){
					CompositePhysicalEntity cpe = (CompositePhysicalEntity)pmc;
					int s = 0;
					for(PhysicalEntity ent : cpe.getArrayListOfEntities()){
						add(new SemSimComponentAnnotationPanel(workbench, ad, settings, ent));
						if(s<cpe.getArrayListOfStructuralRelations().size()){
							add(new StructuralRelationPanel(cpe.getArrayListOfStructuralRelations().get(s)));
						}
						s++;
					}
				}
				// Else the property target is a single physical entity or process
				else{
					add(new SemSimComponentAnnotationPanel(workbench, ad, settings, pmc));
				}
			}
		}
		add(addempanel);
		alignAndPaint();
	}
	
	
	public void alignAndPaint(){
		int x = ad.indent;
		for(Component c : getComponents()){
			if(c instanceof JComponent) ((JComponent)c).setBorder(BorderFactory.createEmptyBorder(0, x, 5, 0));
			c.setBackground(SemGenSettings.lightblue);
			x = x + 15;
		}
		setAddButtonsEnabled();
		validate();
	}
	
	
	public void setAddButtonsEnabled(){
		// If we can edit the composite annotation
		if(ad.thebutton.editable){
			// If the physical property is specified and there is either a process or entity that it is a property of
			if(getComponent(2) instanceof SemSimComponentAnnotationPanel){
				if(((SemSimComponentAnnotationPanel)getComponent(2)).smc instanceof PhysicalProcess){
					addentbutton.setEnabled(false);
					addprocbutton.setEnabled(false);
				}
				else{
					addentbutton.setEnabled(true);
					addprocbutton.setEnabled(false);
				}
			}
			// Otherwise there is no process or entity specified. Set the add entity/ add process buttons
			// based on type of physical property specified 
			else if(datastructure.getPhysicalProperty().hasRefersToAnnotation()){
				PropertyType type = datastructure.getPropertyType(SemGen.semsimlib);
				if(type == PropertyType.PropertyOfPhysicalEntity){
					addentbutton.setEnabled(true);
					addprocbutton.setEnabled(false);
				}
				else if(type == PropertyType.PropertyOfPhysicalProcess){
					addentbutton.setEnabled(false);
					addprocbutton.setEnabled(true);
				}
				else{
					addentbutton.setEnabled(false);
					addprocbutton.setEnabled(false);
				}
			}
			// Otherwise no annotation for property specified, enable both add process and add entity buttons
			else{
				addentbutton.setEnabled(true);
				addprocbutton.setEnabled(true);
			}
		}
		else{
			addentbutton.setEnabled(false);
			addprocbutton.setEnabled(false);
		}
	}
	

	public void actionPerformed(ActionEvent arg0) {
		// If the "Add entity" button is pressed
		if(arg0.getSource() == addentbutton){
			if(datastructure.getPhysicalProperty().getPhysicalPropertyOf()==null){
				datastructure.getPhysicalProperty().setPhysicalPropertyOf(workbench.getSemSimModel().getCustomPhysicalEntityByName(SemSimModel.unspecifiedName));
			}
			else{
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
				datastructure.getPhysicalProperty().setPhysicalPropertyOf(workbench.getSemSimModel().addCompositePhysicalEntity(ents, rels));
			}
			workbench.setModelSaved(false);
			
			if(ad.thebutton.refreshAllCodes() && settings.organizeByPropertyType()){
				ad.annotator.AlphabetizeAndSetCodewords();
				if(!settings.useTreeView())
					ad.annotator.codewordscrollpane.scrollToComponent(ad.thebutton);
			}
			refreshUI();
		}
		// If "Add process" button pressed
		if(arg0.getSource() == addprocbutton){
			if(datastructure.getPhysicalProperty().getPhysicalPropertyOf()==null){
				datastructure.getPhysicalProperty().setPhysicalPropertyOf(workbench.getSemSimModel().getCustomPhysicalProcessByName(SemSimModel.unspecifiedName));
				workbench.setModelSaved(false);
			}
			refreshUI();
		}
	}
}
