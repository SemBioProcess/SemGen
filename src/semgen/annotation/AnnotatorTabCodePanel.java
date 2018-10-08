// Create the scroll pane for the imported model code
package semgen.annotation;

import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.drawers.ModelAnnotationsBench;
import semgen.utilities.SemGenError;
import semgen.utilities.SemGenFont;
import semgen.utilities.uicomponent.SemGenProgressBar;
import semgen.utilities.uicomponent.SemGenTextArea;
import semsim.fileaccessors.FileAccessorFactory;
import semsim.fileaccessors.ModelAccessor;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnnotatorTabCodePanel extends SemGenTextArea implements Observer {
	private static final long serialVersionUID = 1L;
	private int currentindexforcaret;
	private HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(SemGenSettings.lightblue);

	private ArrayList<Integer> indexesOfHighlightedTerms;
	private String codeword = "";
	private CodePopUp popup = new CodePopUp();
	private AnnotatorWorkbench workbench;

	public AnnotatorTabCodePanel(AnnotatorWorkbench bench) {
		workbench = bench;
		workbench.addObservertoModelAnnotator(this);
		setFont(SemGenFont.Bold("Monospaced"));
		setMargin(new Insets(5, 15, 5, 5));
		setForeground(Color.black);
		addMouseListener(new PopupListener());
		
		try {
			setCodeView(workbench.getModelSourceLocation());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public void setCodeView(ModelAccessor srccodema) throws IOException {
		Boolean cont = true;
		setText("");
		String modelloc = "";
		
		if(srccodema != null){
						
			modelloc = srccodema.getFilePath();
			
			// If the legacy model code is on the web
			if (srccodema.modelIsOnline()) {
				SemGenProgressBar progframe = new SemGenProgressBar("Retrieving code...", false);

				URL url = new URL(srccodema.getFilePath());
				HttpURLConnection.setFollowRedirects(false);
				HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
				httpcon.setReadTimeout(60000);
				httpcon.setRequestMethod("HEAD");
				
				Boolean online = true;
				try {
					httpcon.getResponseCode();
				} catch (Exception e) {
					e.printStackTrace();
					SemGenError.showWarning("Legacy code for " + workbench.getModelAccessor().getShortLocation() + " could not be found at address\n" +
							srccodema.getFullPath(), "404 Not Found");
					online = false;
				}
				if (online) {
					progframe.setProgressValue(0);
					
					URLConnection urlcon = url.openConnection();
					urlcon.setDoInput(true);
					urlcon.setUseCaches(false);
					urlcon.setReadTimeout(60000);
					
					// If there's a file at the URL
					if(urlcon.getContentLength()>0){
						BufferedReader d = new BufferedReader(new InputStreamReader(urlcon.getInputStream()));
						String s;
						float charsremaining = urlcon.getContentLength();
						float totallength = charsremaining;
						while ((s = d.readLine()) != null) {
							append(s);
							append("\n");
							charsremaining = charsremaining - s.length();
							int x = Math.round(100*(totallength-charsremaining)/totallength);
							progframe.setProgressValue(x);
						}
						d.close();
						setCaretPosition(0);
					}
					else cont = false;
				} 
				else cont = false;
				progframe.dispose();
			}
			
			// Otherwise it's a local file or within a local file
			else {
				
				// If the local file doesn't exist, check the directory that the
				// SemSim model is in
				if( ! srccodema.getFile().exists()){
					String srcfilename = srccodema.getFile().getName();
					
					if(workbench.getModelAccessor().sharesDirectory(srcfilename))
						srccodema = FileAccessorFactory.getModelAccessor(workbench.getModelAccessor().getDirectoryPath() + "/" + srcfilename);
				}
				
				String modelcode = srccodema.getModelasString();
				
				if (modelcode != null && ! modelcode.equals("")){
					append(modelcode);
					append("\n");
					setCaretPosition(0);
				} 
				else cont = false;
			}
		} 
		else { 
			modelloc = "<model location not specified>";
			cont = false; 
		}

		if (!cont) {
			setText("WARNING: Could not access model code"
					+ " at " + modelloc
					+ "\n\nIf the file is on the web, make sure you are connected to the internet."
					+ "\nIf the file is local, make sure it exists at the location above."
					+ "\n\nUse the '" + AnnotatorToolBar.sourcemodelcodetooltip
					+ "' toolbar \nbutton to specify the simulation code associated with this SemSim model.");
			setCaretPosition(0);
		}
	}
	
	public void setCodeword(String s) {
		codeword = s;
	}
	
	// Highlight occurrences of codeword in legacy code
	public void HighlightOccurances(Boolean fbex) throws BadLocationException {
		removeAllHighlights();
		getIndexesForCodewordOccurrences(codeword, false);
		
		if(indexesOfHighlightedTerms.size()>0){
			hilit.addHighlight(indexesOfHighlightedTerms.get(0).intValue()+1, 
				(indexesOfHighlightedTerms.get(0).intValue()+1+codeword.length()), painter);
			
			highlightAllOccurrencesOfString( fbex);
			setCaretPosition(indexesOfHighlightedTerms.get(0));
		}
		currentindexforcaret = 0;
	}

	public void getIndexesForCodewordOccurrences(String origword, Boolean caseinsensitive) {
		indexesOfHighlightedTerms = new ArrayList<Integer>();
		hilit.removeAllHighlights();
		
		Pattern pattern = Pattern.compile("[\\W_]" + Pattern.quote(origword) + "[\\W_]");
		if(caseinsensitive){
			pattern = Pattern.compile("[\\W_]" + Pattern.quote(origword) + "[\\W_]", Pattern.CASE_INSENSITIVE);
		}
		Boolean match = true;
		Matcher m = pattern.matcher(getText());
		while(match){
			if(m.find()){
				indexesOfHighlightedTerms.add(m.start());
			}
			else{
				match = false;
			}
		}
	}
			
	public void findNextStringInText(Boolean fbex){
		if(!indexesOfHighlightedTerms.isEmpty()){
			hilit.removeAllHighlights();
			if(indexesOfHighlightedTerms.size()-1>currentindexforcaret){
				currentindexforcaret++;
				setCaretPosition(indexesOfHighlightedTerms.get(currentindexforcaret));
			}
			else if(indexesOfHighlightedTerms.size()-1==currentindexforcaret){
				// Wrap and beep
				Toolkit.getDefaultToolkit().beep();
				setCaretPosition(indexesOfHighlightedTerms.get(0));
				currentindexforcaret = 0;
			}
			try {
				hilit.addHighlight(getCaretPosition()+1, getCaretPosition() + 1 + codeword.length(), painter);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			highlightAllOccurrencesOfString(fbex);
		}
	}
	
	public void highlightAllOccurrencesOfString(Boolean fbex){
		if(fbex)
			indexesOfHighlightedTerms = highlightAll(codeword, true, false);
	}
	
	public String getHighlightedText() {
		return getSelectedText();
	}
	
	private class CodePopUp extends JPopupMenu{
		private static final long serialVersionUID = 1L;
		JMenuItem next, copy, setasfree;
		
		CodePopUp() {
			next = add(new JMenuItem("Next", KeyEvent.VK_N));
			next.setToolTipText("Find next instance of codeword in code");
			add(new JSeparator());
			copy = add(new JMenuItem("Copy", KeyEvent.VK_C));
			setasfree = add(new JMenuItem("Set as Freetext", KeyEvent.VK_V));
			
			setActions();
		
		}
		
		void setActions() {
			next.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					findNextStringInText(true);
				}
			});	
			
			copy.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					StringSelection sel = new StringSelection(getSelectedText());
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
				}
			});
			
			setasfree.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					workbench.useCodeWindowFreetext();
				}
			});
		}
	}

	class PopupListener extends MouseAdapter {
	    public void mousePressed(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
		     maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
		     if (e.isPopupTrigger()) {
		         popup.show(e.getComponent(),
		                       e.getX(), e.getY());
		        }
		    }
		}

	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1 == ModelAnnotationsBench.ModelChangeEnum.SOURCECHANGED) {
			try {
				setCodeView(workbench.getModelSourceLocation());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
	}
}
