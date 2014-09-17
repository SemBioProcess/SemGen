package semgen.annotation.dialog;

import semgen.SemGenGUI;
import semgen.annotation.AnnotatorTab;
import semgen.resource.SemGenIcon;
import semgen.resource.uicomponents.SemGenScrollPane;
import semsim.SemSimConstants;
import semsim.model.annotation.Annotation;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class ModelLevelMetadataEditor extends JDialog implements PropertyChangeListener, ActionListener {

	private static final long serialVersionUID = 222072128470808990L;
	private JOptionPane optionPane;
	private SemGenScrollPane scrollpane;
	private JPanel genmodinfo = new JPanel();
	public JButton addbutton = new JButton("Add annotation");
	
	public ModelLevelMetadataEditor() {
		setSize(700, 655);
		setTitle("Edit model-level annotations");
		setResizable(true);
		
		JPanel toppanel = new JPanel(new BorderLayout());
		addbutton.addActionListener(this);
		toppanel.add(addbutton, BorderLayout.WEST);
		
		JPanel mainpanel = new JPanel(new BorderLayout());
		mainpanel.add(toppanel, BorderLayout.NORTH);

		genmodinfo.setBorder(BorderFactory.createEmptyBorder(0, 12, 24, 24));
		genmodinfo.setLayout(new BoxLayout(genmodinfo, BoxLayout.Y_AXIS));
		getModelLevelAnnotations();
				
		scrollpane = new SemGenScrollPane(genmodinfo);
		scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollpane.getVerticalScrollBar().setUnitIncrement(12);
		mainpanel.add(scrollpane, BorderLayout.CENTER);

		optionPane = new JOptionPane(new Object[]{mainpanel}, JOptionPane.PLAIN_MESSAGE,JOptionPane.OK_CANCEL_OPTION, null);
		optionPane.addPropertyChangeListener(this);
		Object[] options = new Object[] { "Apply", "Cancel" };
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);

		setContentPane(optionPane);
		setModal(true);
		setVisible(true);
	}
	
	public class MetadataItem extends JPanel implements MouseListener, ActionListener{

		private static final long serialVersionUID = 3245322304789828616L;
		public JButton removebutton = new JButton();
		public ModelLevelMetadataEditor ed;
		public Annotation ann;
		public Boolean editable;
		public JComboBox<String> cb;
		public JTextArea ta;
		public SemGenScrollPane sgsp;
		
		public MetadataItem(String labeltext, String tatext, Annotation ann, ModelLevelMetadataEditor ed, Boolean editable){
			this.ann = ann;
			this.ed = ed;
			this.editable = editable;
			
			JLabel label = new JLabel(labeltext);
			ta = new JTextArea(tatext);
			sgsp = new SemGenScrollPane(ta);
			Format(label, editable);
			this.add(label);
			this.add(sgsp);
			this.add(removebutton);
		}
		
		public MetadataItem(String tatext, Annotation ann, ModelLevelMetadataEditor ed, Boolean editable){
			this.ann = ann;
			this.ed = ed;
			this.editable = editable;
			
			cb = new JComboBox<String>(SemGenGUI.semsimlib.getListofMetaDataRelations());
			cb.setSelectedItem(cb.getItemAt(0));
			
			ta = new JTextArea(tatext);
			JLabel label = new JLabel();
			sgsp = new SemGenScrollPane(ta);
			Format(label, editable);
			add(cb);
			add(sgsp);
			add(removebutton);
		}
		
		
		private void Format(JLabel label, Boolean editable) {
			label.setFont(new Font("SansSerif", Font.ITALIC, 12));
			label.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 8));
			label.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			ta.setEditable(editable);
			ta.setAlignmentX(Component.LEFT_ALIGNMENT);
			ta.setFont(new Font("SansSerif", Font.PLAIN, 12));
			ta.setForeground(Color.blue);
			ta.setLineWrap(true);
			ta.setWrapStyleWord(true);
			if(!editable){ta.setBackground(Color.lightGray);}

			sgsp.setMaximumSize(new Dimension(300, 300));
			if(ta.getText().length()<45){sgsp.setPreferredSize(new Dimension(300, 35));}
				else{sgsp.setPreferredSize(new Dimension(300, 100));}
			removebutton.setBackground(this.getBackground());
			removebutton.addMouseListener(this);
			removebutton.addActionListener(this);
			removebutton.setOpaque(false);
			removebutton.setBorderPainted(false);
			removebutton.setContentAreaFilled(false);
			removebutton.setIcon(SemGenIcon.eraseicon);
			removebutton.setEnabled(editable);
		}
		
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == removebutton) {
				ed.genmodinfo.remove(this);
				genmodinfo.validate();
				genmodinfo.repaint();
			}
		}
		
		public void mouseEntered(MouseEvent arg0) {
			if (arg0.getComponent() == removebutton) {
				removebutton.setBorderPainted(true);
				removebutton.setContentAreaFilled(true);
			}
		}
		public void mouseExited(MouseEvent arg0) {
			if (arg0.getComponent() == removebutton) {
				removebutton.setBorderPainted(false);
				removebutton.setContentAreaFilled(false);
			}
		}
		public void mousePressed(MouseEvent arg0) {}
		public void mouseReleased(MouseEvent arg0) {}
		public void mouseClicked(MouseEvent arg0) {}
	}
	// End of MetadataItem class

	// Retrieve the model-level annotations
	public Set<Annotation> getModelLevelAnnotations(){
		for(Annotation ann : semsimmodel.getAnnotations()){
			if(ann.getRelation()==SemSimConstants.KEY_TERM_RELATION || ann.getRelation()==SemSimConstants.SEMSIM_VERSION_RELATION){
				String label = null;
				if(ann.getValueDescription()!=null){
					label = ann.getValueDescription();
				}
				else{
					label = ann.getValue().toString();
				}
				genmodinfo.add(new MetadataItem(ann.getRelation().getName(), label, ann, this, false));
			}
			else{
				String fragment = "?";
				if(ann.getRelation().getURI().toString().contains("#")) fragment = ann.getRelation().getURI().getFragment();
				else{
					fragment = ann.getRelation().getURI().toString();
					fragment = fragment.substring(fragment.lastIndexOf("/")+1,fragment.length());
				}
				if(ann.getValue()!=null){
					genmodinfo.add(new MetadataItem(fragment, ann.getValue().toString(), ann, this, true));
				}
			}
		}
		genmodinfo.add(Box.createGlue());
		return semsimmodel.getAnnotations();
	}

	
	public final void propertyChange(PropertyChangeEvent e) {
		// Set the model-level annotations
		if (optionPane.getValue().toString() == "Apply") {
			// Test to make sure all annotations are complete
			Component[] cmpnts = genmodinfo.getComponents();
			for(int g=0; g<cmpnts.length; g++){
				if(cmpnts[g] instanceof MetadataItem){
					MetadataItem mi = (MetadataItem)cmpnts[g];
					if(mi.editable){
						if(mi.ta.getText()==null || mi.ta.getText().equals("")){
							JOptionPane.showMessageDialog(this, "Please complete or remove all annotations first.", "Error", JOptionPane.ERROR_MESSAGE);
							optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
							return;
						}
					}
				}
			}
			
			// Remove all existing model-level annotations
			semsimmodel.setAnnotations(new HashSet<Annotation>());
			for(int g=0; g<cmpnts.length; g++){
				if(cmpnts[g] instanceof MetadataItem){
					MetadataItem mi = (MetadataItem)cmpnts[g];
					// This will probably need to be edited so that working with combo boxes functions correctly
					if(mi.editable){
						if(mi.cb!=null){
							semsimmodel.addAnnotation(
								new Annotation(SemSimConstants.getRelationFromURI(URI.create(SemSimConstants.SEMSIM_NAMESPACE + (String)mi.cb.getSelectedItem())), mi.ta.getText()));
						}
						else if(mi.ann.getValue() instanceof String){
							semsimmodel.addAnnotation(new Annotation(mi.ann.getRelation(), mi.ta.getText()));
						}
					}
					else{
						semsimmodel.addAnnotation(new Annotation(mi.ann.getRelation(), mi.ann.getValue()));
					}
				}
			}
		}
		setVisible(false);

	}
	
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getSource() == addbutton){
			MetadataItem mi = new MetadataItem("", null, this, true);
			genmodinfo.add(mi,0);
			genmodinfo.validate();
			genmodinfo.repaint();
			scrollpane.validate();
			scrollpane.repaint();
			scrollpane.scrollToComponent(mi);
		}
	}
}