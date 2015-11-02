package semgen;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import semgen.utilities.SemGenFont;
import semgen.utilities.uicomponent.SemGenDialog;


public class PreferenceDialog extends SemGenDialog implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;
	protected SemGenSettings settings;
	public JOptionPane optionPane;
	public ArrayList<PrefCheckBox> checklist = new ArrayList<PrefCheckBox>();
	
	public PreferenceDialog(SemGenSettings sets) {
		super("SemGen default preferences");
		settings = sets;	

		makePreferences();
		drawCheckList();
		
		setContentPane(optionPane);
		
		optionPane.getComponent(JOptionPane.OK_OPTION).setEnabled(false);
		showDialog();
	}
	
	public void propertyChange(PropertyChangeEvent e) {
		String propertyfired = e.getPropertyName();
		if (propertyfired.equals("value")) {
			String value = optionPane.getValue().toString();
			if (value == "OK") {
				for (PrefCheckBox pref : checklist ) {
					pref.toggleSetting(pref.isSelected());
				}
			}
			dispose();
		} 
	}

	@SuppressWarnings("serial")
	public void makePreferences() {
		checklist.add(new PrefCheckBox(
				"Auto Annotate", "Automatically annotate a newly imported model", 
				settings.doAutoAnnotate()) 
				{protected void toggleSetting(Boolean sel) {settings.toggleAutoAnnotate(sel);}});
		checklist.add(new PrefCheckBox(
				"Auto Annotate Locally Mapped Variables", "Automatically annotate local CellML-type mapped variables", 
				settings.doAutoAnnotateMapped()) 
				{protected void toggleSetting(Boolean sel) {settings.toggleAutoAnnotateMapped(sel);}});
		checklist.add(new PrefCheckBox(
				"Tree View", "Display codewords and submodels within the submodel tree", 
				settings.useTreeView()) 
				{protected void toggleSetting(Boolean sel) {settings.toggleTreeView(sel);}});
		checklist.add(new PrefCheckBox(
				"Show Imports", "Make imported codewords and submodels visible", 
				settings.showImports()) 
				{protected void toggleSetting(Boolean sel) {settings.toggleShowImports(sel);}});
		checklist.add(new PrefCheckBox(
				"Composite Display Markers", "Display markers that indicate a codeword's property type", 
				settings.useDisplayMarkers()) 
				{protected void toggleSetting(Boolean sel) {settings.toggleDisplayMarkers(sel);}});
		checklist.add(new PrefCheckBox(
				"Organize by Property Type", "Sort codewords by its property type", 
				settings.organizeByPropertyType()) 
				{protected void toggleSetting(Boolean sel) {settings.toggleByPropertyType(sel);}});
		checklist.add(new PrefCheckBox(
				"Organize by Composite Completeness", "Sort codewords by how complete their composite annotation is", 
				settings.organizeByCompositeCompleteness()) 
				{protected void toggleSetting(Boolean sel) {settings.toggleCompositeCompleteness(sel);}});
	}
	
	public void drawCheckList() {
		JPanel prefpanel = new JPanel();
		prefpanel.setLayout(new BoxLayout(prefpanel, BoxLayout.Y_AXIS));
		JLabel msglabel = new JLabel("Choose preferences to use when opening new tabs");
		msglabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		prefpanel.add(msglabel);
		
		
		for (PrefCheckBox pref : checklist) {
			prefpanel.add(pref);
		}
		
		Object[] array = new Object[] { prefpanel };
		Object[] options = new Object[] { "OK", "Cancel" };

		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
		optionPane.addPropertyChangeListener(this);
		
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);
	}
	
	class PrefCheckBox extends JCheckBox implements ActionListener{
		private static final long serialVersionUID = 1L;

		PrefCheckBox(String capt, String tip, boolean selected) {
			super(capt, selected);
			setToolTipText(tip);
			setFont(SemGenFont.defaultPlain());
			addActionListener(this);
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			optionPane.getComponent(JOptionPane.OK_OPTION).setEnabled(true);
		}
		
		protected void toggleSetting(Boolean sel) {}
	}
}
