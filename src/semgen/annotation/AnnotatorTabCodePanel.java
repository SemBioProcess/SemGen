// Create the scroll pane for the imported model code
package semgen.annotation;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import semgen.SemGenSettings;
import semgen.resource.SemGenFont;
import semgen.resource.uicomponent.SemGenProgressBar;
import semgen.resource.uicomponent.SemGenTextArea;

public class AnnotatorTabCodePanel extends SemGenTextArea {
	private static final long serialVersionUID = 1L;
	private int currentindexforcaret;
	private HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(SemGenSettings.lightblue);

	private ArrayList<Integer> indexesOfHighlightedTerms;
	private String codeword = "";
	private CodePopUp popup = new CodePopUp();

	public AnnotatorTabCodePanel() {
		setFont(SemGenFont.Bold("Monospaced"));
		setMargin(new Insets(5, 15, 5, 5));
		setForeground(Color.black);
		addMouseListener(new PopupListener());
	}
	
	public void setCodeView(String modelloc) throws IOException {
		Boolean cont = true;
		setText("");
		if(modelloc!=null && !modelloc.equals("")){
			File modelfile = null;
			// If the legacy model code is on the web
			if (modelloc.startsWith("http://")) {
				SemGenProgressBar progframe = new SemGenProgressBar("Retrieving legacy code...", false);
				
				Boolean online = true;
				modelfile = new File(modelloc);
	
				URL url = new URL(modelloc);
				HttpURLConnection.setFollowRedirects(false);
				HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
				httpcon.setReadTimeout(60000);
				httpcon.setRequestMethod("HEAD");
				try {
					httpcon.getResponseCode();
				} catch (Exception e) {
					e.printStackTrace(); 
					online = false;
				}
				if (online) {
					progframe.setProgressValue(0);
					
					URLConnection urlcon = url.openConnection();
					urlcon.setDoInput(true);
					urlcon.setUseCaches(false);
					urlcon.setReadTimeout(60000);
					// If there's no file at the URL
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
			// Otherwise it's a local file
			else {
				modelfile = new File(modelloc);
				if (modelfile.exists()) {
					// Read in the model code and append it to the codearea
					// JTextArea in the top pane
					String filename = modelfile.getAbsolutePath();
					Scanner importfilescanner = new Scanner(new File(filename));
					
					String nextline;
					while (importfilescanner.hasNextLine()) {
						nextline = importfilescanner.nextLine();
						append(nextline);
						append("\n");
						setCaretPosition(0);
					}
					importfilescanner.close();
				} else cont = false;
			}
		} else { 
			cont = false; 
			modelloc = "<file location not specified>";
		}

		if (!cont) {
			setText("ERROR: Could not access model code"
					+ " at " + modelloc
					+ "\n\nIf the file is on the web, make sure you are connected to the internet."
					+ "\nIf the file is local, make sure it exists at the location above."
					+ "\n\nUse the 'Change Associated Legacy Code' menu item to specify this SemSim model's legacy code.");
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
		
	private class CodePopUp extends JPopupMenu{
		private static final long serialVersionUID = 1L;
		JMenuItem next, copy;
		
		CodePopUp() {
			next = add(new JMenuItem("Next", KeyEvent.VK_N));
			next.setToolTipText("Find next instance of codeword in code");
			add(new JSeparator());
			copy = add(new JMenuItem("Copy", KeyEvent.VK_C));
			
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
}
