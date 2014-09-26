package semgen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import semgen.SemGenGUI.NewAnnotatorTask;
import semgen.annotation.Annotator;
import semgen.merging.Merger;
import semsim.SemSimUtil;

public class SemGenFileChooser extends JFileChooser implements ActionListener{

	/**
	 * 
	 */
	public int toolType;
	public Merger merger;
	private JPanel chooseURLpanel;
	private JTextField textField;
	private JButton URLselectButton;
	public String buttonActiveText = "Open from URL";
	public String buttonInactiveText = "Opening...";
	public JButton stopButton;
	public String URLcontent;
	public JProgressBar progbar;
	private LoadFromURLTask task;
	
	public static final int NEW_ANNOTATOR_TASK = 0;
	public static final int NEW_EXTRACTOR_TASK = 1;
	public static final int ATOMIC_DECOMPOSITION_TASK = 2;
	public static final int BATCH_CLUSTER_TASK = 3;
	public static final int ENCODING_TASK = 4;
	public static final int MERGING_TASK = 5;
	
	
	private static final long serialVersionUID = -9040553448654731532L;
	
	public SemGenFileChooser(int toolType, Merger merger){
		super();
		this.merger = merger;
		initialize(toolType);
	}

	public SemGenFileChooser(int toolType){
		super();
		initialize(toolType);
	}
	
	private void initialize(int toolType){
		this.toolType = toolType;
		chooseURLpanel = new JPanel();
		
		textField = new JTextField();
		textField.setForeground(Color.blue);
		textField.setPreferredSize(new Dimension(300, 25));
		
		URLselectButton = new JButton(buttonActiveText);
		URLselectButton.addActionListener(this);
		
		stopButton = new JButton("Stop");
		stopButton.addActionListener(this);
		stopButton.setVisible(false);
		
		progbar = new JProgressBar();
		progbar.setVisible(false);
		
		chooseURLpanel.add(textField);
		chooseURLpanel.add(URLselectButton);
		chooseURLpanel.add(stopButton);
		
		Component c = getComponent(getComponentCount()-1); // This is the bottom-most component
		if(c instanceof JPanel){
			((JPanel)c).add(Box.createVerticalStrut(20));
			((JPanel)c).add(new JSeparator());
			((JPanel)c).add(Box.createVerticalStrut(5));
			((JPanel)c).add(chooseURLpanel);
			((JPanel)c).add(progbar);
			((JPanel)c).add(Box.createVerticalStrut(5));
		}
	}
	
	
	public void toggleOKandCancelButtons(JComponent c, boolean value){
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
						task = new LoadFromURLTask(text, this);
						URLselectButton.setEnabled(false);
						stopButton.setVisible(true);
						URLselectButton.setText(buttonInactiveText);
						task.execute();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
				else{
					JOptionPane.showMessageDialog(this, "Please enter a valid URL");
				}
			}
		}
		else if(o == stopButton){
			if(task!=null){
				task.interrupt = true;
			}
		}
	}
	
	
	public static class LoadFromURLTask extends SwingWorker<Void, Void> {
		public URL url;
		public String content = "";
		public SemGenFileChooser fc;
		public boolean interrupt;
        public LoadFromURLTask(String text, SemGenFileChooser fc) throws MalformedURLException{
        	url = new URL(text);
        	this.fc = fc;
        }
        @Override
        public Void doInBackground(){
        	
			try{
				Boolean online = true;
				interrupt = false;
				HttpURLConnection.setFollowRedirects(false);
				HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
				httpcon.setReadTimeout(60000);
				httpcon.setRequestMethod("HEAD");
				
				try {
					httpcon.getResponseCode();
				} 
				catch (Exception e) {e.printStackTrace(); online = false;}
				
				if (online) {
					
					fc.progbar.setIndeterminate(false);
					fc.progbar.setValue(0);
					fc.progbar.setVisible(true);
					
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
								fc.progbar.setValue(x);
								
								if(interrupt)
									return null;
							}
							d.close();
						}
						catch(Exception x){
							JOptionPane.showMessageDialog(SemGenGUI.desktop, "Problem retrieving URL content: \n\n" + x.getLocalizedMessage(),
								"Error", JOptionPane.ERROR_MESSAGE);
							return null;
						}
					}
					else{
						JOptionPane.showMessageDialog(SemGenGUI.desktop, "Did not find content at that URL",
								"Error", JOptionPane.ERROR_MESSAGE);
							return null;
					}
				} 
				else{
					SemGenGUI.showWebConnectionError(url.toString());
					return null;
				}
				fc.closeAndWriteStringAsModelContent(url, content);
			}
			catch(IOException x){x.printStackTrace();}
            return null;	
        }
        @Override
        public void done() {
        	fc.progbar.setVisible(false);
        	fc.stopButton.setVisible(false);
        	fc.URLselectButton.setText(fc.buttonActiveText);
        	fc.URLselectButton.setEnabled(true);
        	fc.toggleOKandCancelButtons(fc, true);
        }
    }
	
	
	
	public void closeAndWriteStringAsModelContent(URL url, String content){
		
		this.cancelSelection();
		String urlstring = url.toString();
		String name = urlstring.substring(urlstring.lastIndexOf("/"));
		
		File tempfile = new File(SemGenGUI.tempdir.getAbsoluteFile() + "/" + name);
		SemSimUtil.writeStringToFile(content, tempfile);
		
		if(toolType==NEW_ANNOTATOR_TASK){
			SemGenGUI.startNewAnnotatorTask(new File[]{tempfile});
		}
		if(toolType==NEW_EXTRACTOR_TASK){
			SemGenGUI.startNewExtractorTask(tempfile);
		}
		if(toolType==ATOMIC_DECOMPOSITION_TASK){
			SemGenGUI.startAtomicDecomposition(tempfile);
		}
		if(toolType==BATCH_CLUSTER_TASK){
			SemGenGUI.startBatchClustering(tempfile);
		}
		if(toolType==MERGING_TASK){
			if(merger!=null){
				merger.startAdditionOfModels(new File[]{tempfile});
			}
		}
		if(this.toolType==ENCODING_TASK){
			SemGenGUI.startEncoding(tempfile, null);
		}
	}
}
