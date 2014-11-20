package semgen.annotation.dialog;

import semgen.SemGen;
import semgen.annotation.AnnotatorTab;
import semgen.resource.SemGenError;
import semgen.resource.SemGenFont;
import semgen.resource.SemGenIcon;
import semgen.resource.uicomponent.SemGenScrollPane;
import semsim.SemSimConstants;
import semsim.model.annotation.Annotation;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
	private Object[] options;
	public AnnotatorTab annotator;
	private SemGenScrollPane scrollpane;
	private JPanel genmodinfo = new JPanel();
	public JButton addbutton = new JButton("Add annotation");

	private int initwidth = 700;
	private int initheight = 655;

	public ModelLevelMetadataEditor(AnnotatorTab annotator) {

		this.setSize(initwidth, initheight);
		this.setLocationRelativeTo(null);
		this.setTitle("Edit model-level annotations");
		this.setResizable(true);
		this.annotator = annotator;
		
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
		options = new Object[] { "Apply", "Cancel" };
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);

		setContentPane(optionPane);
		this.setModal(true);
		this.setVisible(true);
	}
	
	
	public class MetadataItem extends JPanel implements MouseListener, ActionListener{
		private static final long serialVersionUID = 3245322304789828616L;
		public JButton removebutton = new JButton();
		public Annotation ann;
		public Boolean editable;
		public JComboBox<String> cb;
		public JTextArea ta;
		public SemGenScrollPane sgsp = new SemGenScrollPane(ta);
		
		public MetadataItem(String labeltext, String tatext, Annotation ann, Boolean editable){
			this.ann = ann;
			this.editable = editable;
			
			JLabel label = new JLabel(labeltext);
			ta = new JTextArea(tatext);
			Format(label, ta, removebutton, editable);
			this.add(label);
			this.add(sgsp);
			this.add(removebutton);
		}
		
		public MetadataItem(String tatext, Annotation ann, Boolean editable){
			this.ann = ann;
			this.editable = editable;
			
			cb = new JComboBox<String>(SemGen.semsimlib.getListofMetaDataRelations());
			cb.setSelectedItem(cb.getItemAt(0));
			
			ta = new JTextArea(tatext);
			JLabel label = new JLabel();
			Format(label, ta, removebutton, editable);
			this.add(cb);
			this.add(sgsp);
			this.add(removebutton);
		}
		
		private void Format(JLabel label, JTextArea area, JButton removebutton, Boolean editable) {
			label.setFont(SemGenFont.defaultItalic());
			label.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 8));
			label.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			area.setEditable(editable);
			area.setAlignmentX(Component.LEFT_ALIGNMENT);
			area.setFont(SemGenFont.defaultPlain());
			area.setForeground(Color.blue);
			area.setLineWrap(true);
			area.setWrapStyleWord(true);
			if(!editable){area.setBackground(Color.lightGray);}

			sgsp.setMaximumSize(new Dimension(300, 300));
			if(area.getText().length()<45){sgsp.setPreferredSize(new Dimension(300, 35));}
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
			Object o = e.getSource();
			if (o == removebutton) {
				genmodinfo.remove(this);
				genmodinfo.validate();
				genmodinfo.repaint();
			}
		}
		public void mouseClicked(MouseEvent arg0) {
		}
		public void mouseEntered(MouseEvent arg0) {
			Component component = arg0.getComponent();
			if (component == removebutton) {
				removebutton.setBorderPainted(true);
				removebutton.setContentAreaFilled(true);
			}
		}
		public void mouseExited(MouseEvent arg0) {
			Component component = arg0.getComponent();
			if (component == removebutton) {
				removebutton.setBorderPainted(false);
				removebutton.setContentAreaFilled(false);
			}
		}
		public void mousePressed(MouseEvent arg0) {}
		public void mouseReleased(MouseEvent arg0) {}
	}
	// End of MetadataItem class

	
	// Retrieve the model-level annotations
	public Set<Annotation> getModelLevelAnnotations(){
		for(Annotation ann : annotator.semsimmodel.getAnnotations()){
			if(ann.getRelation()==SemSimConstants.KEY_TERM_RELATION || ann.getRelation()==SemSimConstants.SEMSIM_VERSION_RELATION){
				String label = null;
				if(ann.getValueDescription()!=null){
					label = ann.getValueDescription();
				}
				else{
					label = ann.getValue().toString();
				}
				genmodinfo.add(new MetadataItem(ann.getRelation().getName(), label, ann, false));
			}
			else{
				String fragment = "?";
				if(ann.getRelation().getURI().toString().contains("#")) fragment = ann.getRelation().getURI().getFragment();
				else{
					fragment = ann.getRelation().getURI().toString();
					fragment = fragment.substring(fragment.lastIndexOf("/")+1,fragment.length());
				}
				if(ann.getValue()!=null){
					genmodinfo.add(new MetadataItem(fragment, ann.getValue().toString(), ann, true));
				}
			}
		}
		genmodinfo.add(Box.createGlue());
		return annotator.semsimmodel.getAnnotations();
	}

	
	public final void propertyChange(PropertyChangeEvent e) {
		// Set the model-level annotations
		String propertyfired = e.getPropertyName();
		if (propertyfired.equals("value")) {
			String value = optionPane.getValue().toString();
			if (value == "Apply") {
				// Test to make sure all annotations are complete
				Component[] cmpnts = genmodinfo.getComponents();
				for(int g=0; g<cmpnts.length; g++){
					if(cmpnts[g] instanceof MetadataItem){
						MetadataItem mi = (MetadataItem)cmpnts[g];
						if(mi.editable){
							if(mi.ta.getText()==null || mi.ta.getText().equals("")){
								SemGenError.showError("Please complete or remove all annotations first.", "Error");
								optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
								return;
							}
						}
					}
				}
				
				// Remove all existing model-level annotations
				annotator.semsimmodel.setAnnotations(new HashSet<Annotation>());
				for(int g=0; g<cmpnts.length; g++){
					if(cmpnts[g] instanceof MetadataItem){
						MetadataItem mi = (MetadataItem)cmpnts[g];
						
						// This will probably need to be edited so that working with combo boxes functions correctly
						if(mi.editable){
							if(mi.cb!=null){
								annotator.semsimmodel.addAnnotation(
									new Annotation(SemSimConstants.getRelationFromURI(URI.create(SemSimConstants.SEMSIM_NAMESPACE + (String)mi.cb.getSelectedItem())), mi.ta.getText()));
							}
							else if(mi.ann.getValue() instanceof String){
								annotator.semsimmodel.addAnnotation(new Annotation(mi.ann.getRelation(), mi.ta.getText()));
							}
						}
						else{
							annotator.semsimmodel.addAnnotation(new Annotation(mi.ann.getRelation(), mi.ann.getValue()));
						}
					}
				}
				annotator.setModelSaved(false);
			}
			dispose();
		}
	}

	public void actionPerformed(ActionEvent arg0) {
		Object o = arg0.getSource();
		if(o == addbutton){
			MetadataItem mi = new MetadataItem("", null, true);
			genmodinfo.add(mi,0);
			genmodinfo.validate();
			genmodinfo.repaint();
			scrollpane.validate();
			scrollpane.repaint();
			scrollpane.scrollToComponent(mi);
		}
	}
}