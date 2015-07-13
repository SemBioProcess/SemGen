package semgen.utilities.file;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import semgen.SemGen;
import semgen.utilities.SemGenError;
import semgen.utilities.SemGenTask;
import semgen.utilities.uicomponent.SemGenProgressBar;
import semsim.utilities.SemSimUtil;

public class SemGenOpenFileChooser extends SemGenFileChooser implements ActionListener {

	private JTextField textField = new JTextField();
	private JButton URLselectButton;
	public String buttonActiveText = "Open from URL";
	public String buttonInactiveText = "Opening...";
	
	private static final long serialVersionUID = -9040553448654731532L;
		
	public SemGenOpenFileChooser(String title, Boolean multi){
		super(title);
		setMultiSelectionEnabled(multi);
		initialize();
		openFile();
	}
	
	public SemGenOpenFileChooser(String title, String[] filters, Boolean multi){
		super(title, filters);
		setMultiSelectionEnabled(multi);
		initialize();
		openFile();
	}
	
	public SemGenOpenFileChooser(Set<File> file, String title, String[] filters){
		super(title, filters);
		setMultiSelectionEnabled(true);
		initialize();
		openFile(file);
	}
	
	private void initialize(){
		setPreferredSize(filechooserdims);

		addChoosableFileFilter(fileextensions);
		setFileFilter(fileextensions);
		JPanel chooseURLpanel = new JPanel();

		textField.setForeground(Color.blue);
		textField.setPreferredSize(new Dimension(300, 25));
		
		URLselectButton = new JButton(buttonActiveText);
		URLselectButton.addActionListener(this);
		
		chooseURLpanel.add(textField);
		chooseURLpanel.add(URLselectButton);
		
		Component c = getComponent(getComponentCount()-1); // This is the bottom-most component
		if(c instanceof JPanel){
			((JPanel)c).add(Box.createVerticalStrut(20));
			((JPanel)c).add(new JSeparator());
			((JPanel)c).add(Box.createVerticalStrut(5));
			((JPanel)c).add(chooseURLpanel);
			((JPanel)c).add(Box.createVerticalStrut(5));
		}
	}
		
	private void openFile(Set<File> files) {
		if (showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			currentdirectory = getCurrentDirectory();
			for (File file : getSelectedFiles()) {
				files.add(file);
			}
		}
		else {
			this.setSelectedFiles(null);
			this.setSelectedFile(null);
		}
	}
	
	private void openFile() {	
		int choice = showOpenDialog(this);
		if (choice == JFileChooser.APPROVE_OPTION) {
			currentdirectory = getCurrentDirectory();
		}
		else {
			this.setSelectedFiles(null);
			this.setSelectedFile(null);
		}
	}
	
	private void toggleOKandCancelButtons(JComponent c, boolean value){
		for(Component csub : c.getComponents()){
			if(csub instanceof JButton){
				JButton button = (JButton)csub;
				if(button.getText()!=null){
					if(button.getText().equals("Open") || button.getText().equals("Cancel")){
						csub.setEnabled(value);
					}
				}
			}
			if(csub instanceof JComponent){
				if(((JComponent)csub).getComponentCount()>0){
					toggleOKandCancelButtons((JComponent) csub, value);
				}
			}
		}
	}
	
	public void actionPerformed(ActionEvent arg0) {		
		Object o = arg0.getSource();
		if(o == URLselectButton){
			String text = textField.getText();
			if(text!=null && !text.equals("")){
				if(text.startsWith("http")){
					try {
						toggleOKandCancelButtons(this, false);
						LoadFromURLTask task = new LoadFromURLTask(text, this);
						URLselectButton.setEnabled(false);
						URLselectButton.setText(buttonInactiveText);
						task.execute();
			        	URLselectButton.setText(buttonActiveText);
			        	URLselectButton.setEnabled(true);
			        	toggleOKandCancelButtons(this, true);
						
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
				else{
					JOptionPane.showMessageDialog(this, "Please enter a valid URL");
				}
			}
		}
	}
	
	public static class LoadFromURLTask extends SemGenTask {
		private URL url;
		private SemGenOpenFileChooser fc;
		private boolean interrupt;
		
        public LoadFromURLTask(String text, SemGenOpenFileChooser fc) throws MalformedURLException{
        	url = new URL(text);
        	this.fc = fc;
        }
        
        @Override
        public Void doInBackground(){
			try {
				String content = "";
				Boolean online = true;
				interrupt = false;

				HttpURLConnection.setFollowRedirects(false);
				HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
				httpcon.setReadTimeout(60000);
				httpcon.setRequestMethod("HEAD");
				
				try {
					httpcon.getResponseCode();
				} 
				catch (Exception e) {
					e.printStackTrace(); 
					online = false;
				}
				
				if (online) {
					progframe = new SemGenProgressBar("Retreiving", false);
					progframe.bar.setIndeterminate(false);
					progframe.bar.setValue(0);
					
					URLConnection urlcon = url.openConnection();
					urlcon.setDoInput(true);
					urlcon.setUseCaches(false);
					urlcon.setReadTimeout(60000);
					
					// If there's content at the URL...
					if(urlcon.getContentLength()>0){
						try{
							BufferedReader d = new BufferedReader(new InputStreamReader(urlcon.getInputStream()));
							String s;
							float charsremaining = urlcon.getContentLength();
							float totallength = charsremaining;
							
							while ((s = d.readLine()) != null) {
								content = content + s + "\n";
								charsremaining = charsremaining - s.length();
								int x = Math.round(100*(totallength-charsremaining)/totallength);
					        	fc.URLselectButton.setText(fc.buttonInactiveText + x + "%");
								progframe.bar.setValue(x);
								
								if(interrupt) break;
							}
							d.close();
						}
						catch(Exception x){
							JOptionPane.showMessageDialog(fc, "Problem retrieving URL content: \n\n" + x.getLocalizedMessage(),
								"Error", JOptionPane.ERROR_MESSAGE);
						}
					}
					else{
						JOptionPane.showMessageDialog(fc, "Did not find content at that URL",
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				} 
				else{
					SemGenError.showWebConnectionError(url.toString());
				}
				fc.closeAndWriteStringAsModelContent(url, content);
			}
			catch(IOException x){
				x.printStackTrace();
				}
            return null;	
        }
    }
	
	public void closeAndWriteStringAsModelContent(URL url, String content){
		cancelSelection();
		String urlstring = url.toString();
		String name = urlstring.substring(urlstring.lastIndexOf("/"));
		
		File tempfile = new File(SemGen.tempdir.getAbsoluteFile() + "/" + name);
		SemSimUtil.writeStringToFile(content, tempfile);
	}
	
}
