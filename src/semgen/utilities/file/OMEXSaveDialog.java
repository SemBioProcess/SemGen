package semgen.utilities.file;

import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import semgen.SemGenSettings;
import semgen.utilities.SemGenFont;
import semgen.utilities.uicomponent.SemGenDialog;
import semsim.model.collection.SemSimModel;
import semsim.reading.ModelClassifier.ModelType;

public class OMEXSaveDialog extends SemGenDialog implements PropertyChangeListener{
	private static final long serialVersionUID = 1L;
	protected JOptionPane optionPane;
	protected Object[] options = new Object[]{"OK","Cancel"};
	private JComboBox<String> typechooser;
	private JTextField modelnamefield = new JTextField();
	private String modelname = "";
	protected ModelType modeltype;
	private Integer formatselection = -1;
	private SemSimModel semsimmodel;
	protected boolean approvedtowrite = false;
	
	private static String[] ALL_WRITABLE_TYPES = new String[]{"CellML", "SBML"};
	
	public OMEXSaveDialog(SemSimModel ssm) {
		super("Save to OMEX Archive As");
		semsimmodel = ssm;
		
		layoutDialog();
	}
	
	private void layoutDialog() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(SemGenSettings.lightblue);
		
		JLabel selectmodeltype = new JLabel("Model Name: ");
		
		JPanel modelnamepane = new JPanel();
		modelnamepane.setLayout(new BoxLayout(modelnamepane, BoxLayout.X_AXIS));
		modelnamepane.setAlignmentX(Box.LEFT_ALIGNMENT);
		this.modelnamefield = new JTextField();
		modelnamefield.setForeground(Color.blue);
		modelnamefield.setBorder(BorderFactory.createBevelBorder(1));
		modelnamefield.setFont(SemGenFont.defaultPlain());
		modelnamefield.setPreferredSize(new Dimension(250, 25));
		
		modelnamepane.add(selectmodeltype);
		modelnamepane.add(modelnamefield);
		
		JLabel selform = new JLabel("Select Format: ");
		selectmodeltype.setFont(SemGenFont.defaultPlain());

		typechooser = new JComboBox<String>(ALL_WRITABLE_TYPES);
		typechooser.setFont(SemGenFont.defaultPlain());
		typechooser.setPreferredSize(new Dimension(40,25));
		
		// Set ontology chooser to recently used ontology
		typechooser.setSelectedIndex(0);
		
		JPanel modeltypepane = new JPanel();
		modeltypepane.setLayout(new BoxLayout(modeltypepane, BoxLayout.X_AXIS));
		modeltypepane.setAlignmentX(Box.LEFT_ALIGNMENT);
		modeltypepane.add(selform);
		modeltypepane.add(typechooser);
		modeltypepane.add(Box.createHorizontalGlue());
		
		Object[] array = {modelnamepane, modeltypepane };

		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
		
		optionPane.addPropertyChangeListener(this);
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);

		setContentPane(optionPane);
		showDialog();
		
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		
		String propertyfired = e.getPropertyName();
		
		if (propertyfired.equals("value")) {
			String value = optionPane.getValue().toString();
			
			if (value == "OK") {
				formatselection = typechooser.getSelectedIndex();
				modelname = modelnamefield.getText();
				
				String modeltypestring = ALL_WRITABLE_TYPES[formatselection];
				
				modeltype = null;
				if(modeltypestring.equals("CellML")) {
					modeltype = ModelType.CELLML_MODEL;
				}
				else if(modeltypestring.equals("SBML")) {
					modeltype = ModelType.SBML_MODEL;
				}
				
				if(modelNameIsValid()){
					
					if(SemGenSaveFileChooser.outputTypeIsCompatibile(modeltype, semsimmodel, this)){
						approvedtowrite = true;
						dispose();
					}
					else optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

				}
				else{
					JOptionPane.showMessageDialog(this, "Please enter valid model name");
					optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
					return;
				}
			}
			else if(value == "Cancel") dispose();
			
		}
	}
	
 public ModelType getModelType() {
	 return modeltype;
 }
 
 public String getModelName() {
	 return modelname;
 }
 
 private boolean modelNameIsValid(){
	 if(modelname == null) return false;
	 else if(modelname.isEmpty()) return false;
	 else if(modelname.trim().isEmpty()) return false;
	 else return true;
 }
}
