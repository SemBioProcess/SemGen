package semgen.annotation.dialog.termlibrary;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import semgen.SemGenSettings;
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.utilities.uicomponent.SemGenDialog;
import semsim.annotation.ReferenceOntologies.OntologyDomain;

public class AddReferenceClassDialog extends SemGenDialog implements
		PropertyChangeListener  {

	private static final long serialVersionUID = -3830623199860161812L;
	protected ReferenceClassFinderPanel refclasspanel;
	protected JOptionPane optionPane;

	public AddReferenceClassDialog(SemSimTermLibrary lib, OntologyDomain dom) {
		super("Select reference concept");
	    
		refclasspanel = new ReferenceClassFinderPanel(lib, dom);
	    
		Object[] array = {refclasspanel};
		Object[] options = new Object[]{"Annotate","Close"};
		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
		
		optionPane.setBackground(SemGenSettings.lightblue);
		
		optionPane.addPropertyChangeListener(this);
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);
		
		addComponentListener(refclasspanel);
		setContentPane(optionPane);
		this.changeBackgroundColor(this.getComponents(), SemGenSettings.lightblue);
		showDialog();
	}
	
	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getPropertyName()=="value") {
			Object value = optionPane.getValue();
			if (value == JOptionPane.UNINITIALIZED_VALUE) return;
			if (value.toString() == "Close") {
				refclasspanel.clearSelection();
				dispose();
				return;
			}
			refclasspanel.addTermtoLibrary();
			dispose();
		}
	}
	
	public int getIndexofSelection() {
		return refclasspanel.getSelectedTermIndex();
	}
	
	private void changeBackgroundColor(Component[] comp, Color color)
	  {
	    for(int x = 0; x < comp.length; x++)
	    {
	      if(comp[x] instanceof Container) changeBackgroundColor(((Container)comp[x]).getComponents(), color);
	      try
	      {
	    	  if(comp[x] instanceof JPanel) comp[x].setBackground(color);  
	      }
	      catch(Exception e){e.printStackTrace();}
	    }
	  }
}
