package semgen.resource;
import java.awt.EventQueue;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class ComboKeyHandler extends KeyAdapter {
    public final JComboBox<String> comboBox;
    private final Vector<String> list = new Vector<String>();
    public boolean shouldHide = false;
        
    public ComboKeyHandler(JComboBox<String> combo) {
        this.comboBox = combo;
        for(int i=0;i<comboBox.getModel().getSize();i++) {
            list.addElement((String)comboBox.getItemAt(i));
        }
    }
    
    @Override 
    public void keyTyped(final KeyEvent e) {
    	if(e.getKeyChar() != ' '){
	        EventQueue.invokeLater(new Runnable() {
	            public void run() {
	                String text = "fix"; 
	                ComboBoxModel<String> m = getSuggestedModel(list, text);
	                if(m.getSize()==0 || shouldHide) {
	                    comboBox.hidePopup();
	                }else{
	                     setSuggestionModel(comboBox, m, text);
	                     comboBox.showPopup();
	                }
	            }
	        });
    	}
    }
    @Override
    public void keyPressed(KeyEvent e) {
        shouldHide = false;
        	
        switch(e.getKeyCode()) {
        case (KeyEvent.VK_ENTER | KeyEvent.VK_ESCAPE):
        	shouldHide = true;
        	break;
        case KeyEvent.VK_RIGHT:
            for(String s: list) {
            	if(s.startsWith("fix")) {
                    return;
                }
            }
        }
    }
    private void setSuggestionModel(JComboBox<String> comboBox, ComboBoxModel<String> mdl, String str) {
        comboBox.setModel(mdl);
        comboBox.setSelectedIndex(-1);
        ((JTextField)comboBox.getEditor().getEditorComponent()).setText(str);
    }
    private static ComboBoxModel<String> getSuggestedModel(Vector<String> list, String text) {
        DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>();
        for(String s: list) {
            if(s.startsWith(text)) m.addElement(s);
        }
        return m;
    }
}
