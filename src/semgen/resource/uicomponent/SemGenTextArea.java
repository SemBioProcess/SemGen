package semgen.resource.uicomponent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

public class SemGenTextArea extends JTextArea{
	private static final long serialVersionUID = -9202322761777280913L;
	protected Highlighter hilit = new DefaultHighlighter();
	public SemGenTextArea() {
        setOpaque(false);
		setEditable(false);
		setBackground(new Color(250, 250, 227));
		setHighlighter(hilit);
    }
	
	public ArrayList<Integer> highlightAll(String origword, Boolean forcdwd, Boolean caseinsensitive) {
		ArrayList<Integer> listofindexes = new ArrayList<Integer>();
		Pattern pattern = Pattern.compile("[\\W_]" + Pattern.quote(origword) + "[\\W_]");
		if(caseinsensitive){
			pattern = Pattern.compile("[\\W_]" + Pattern.quote(origword) + "[\\W_]", Pattern.CASE_INSENSITIVE);
		}
		HighlightPainter allpainter = new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);
		
		Boolean match = true;
		Matcher m = pattern.matcher(getText());
		while(match){
			if(m.find()){
				int index = m.start();
				
				try{
					hilit.addHighlight(index+1, m.end()-1, allpainter);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				listofindexes.add(index);
			}
			else{
				match = false;
			}
		}
		if(!forcdwd && listofindexes.size()>0){setCaretPosition(0);}
		return listofindexes;
	}
	
	public void removeAllHighlights() {
		hilit.removeAllHighlights();
	}
}
