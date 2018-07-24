package semgen.annotation.dialog.modelanns;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom.JDOMException;
import org.xml.sax.SAXException;

import semgen.utilities.file.SemGenOpenFileChooser;
import semgen.utilities.uicomponent.SemGenDialog;
import semsim.fileaccessors.FileAccessorFactory;
import semsim.fileaccessors.ModelAccessor;

public class LegacyCodeChooser extends SemGenDialog implements ActionListener,
		PropertyChangeListener {

	private static final long serialVersionUID = 5097390254331353085L;
	public JOptionPane optionPane;
	public JTextField txtfld = new JTextField();
	public JButton locbutton = new JButton("or choose local file");
	private String locationtoadd = "";
	private ModelAccessor currentma;
	
	public LegacyCodeChooser(ModelAccessor current) {
		super("Enter URL of legacy code or choose a local file");
		currentma = current;
		JPanel srcmodpanel = new JPanel();
		txtfld.setPreferredSize(new Dimension(350, 25));
		locbutton.addActionListener(this);
		srcmodpanel.add(txtfld);
		srcmodpanel.add(locbutton);
		Object[] options = new Object[] { "OK", "Cancel" };

		optionPane = new JOptionPane(srcmodpanel, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
		optionPane.addPropertyChangeListener(this);
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);
		
		if (currentma != null) setLocationInTextField(currentma);
		
		setContentPane(optionPane);
		showDialog();
	}
	
	private void setLocationInTextField(ModelAccessor ma){
		
		String text = getSourceLocationText(ma);
		txtfld.setText(text);
	}

	
	private String getSourceLocationText(ModelAccessor ma){
		URI uri = ma.getFileThatContainsModelAsURI();
		return ma.modelIsOnline() ? uri.toString() : uri.getPath();
	}
	
	public void propertyChange(PropertyChangeEvent e) {
		
		if (e.getPropertyName().equals("value")) {
			String value = optionPane.getValue().toString();
			
			if (value == "OK")
				locationtoadd = txtfld.getText();
			
			else if(value == "Cancel")
				locationtoadd = getSourceLocationText(currentma);
			
			dispose();
		}
	}

	public ModelAccessor getCodeLocation() {
		return FileAccessorFactory.getModelAccessor(locationtoadd);
	}
	
	public void actionPerformed(ActionEvent arg0) {
		Object o = arg0.getSource();
		if (o == locbutton) {
			SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select legacy model code", false);
			File file = sgc.getSelectedFile();
			if (file!=null){
				
				ModelAccessor ma;
				try {
					ma = sgc.convertFileToModelAccessorList(file).get(0);
					setLocationInTextField(ma);
				} catch (ParserConfigurationException | SAXException | URISyntaxException | JDOMException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}
		
}
