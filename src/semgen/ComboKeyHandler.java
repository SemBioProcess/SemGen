package semgen;
import java.awt.EventQueue;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class ComboKeyHandler extends KeyAdapter implements ListDataListener{
    public final JComboBox comboBox;
    private final Vector<String> list = new Vector<String>();
    public ComboKeyHandler(JComboBox combo) {
        this.comboBox = combo;
        for(int i=0;i<comboBox.getModel().getSize();i++) {
            list.addElement((String)comboBox.getItemAt(i));
        }
    }
    
    public boolean shouldHide = false;
    @Override public void keyTyped(final KeyEvent e) {
    	if(e.getKeyChar() != ' '){
	        EventQueue.invokeLater(new Runnable() {
	            public void run() {
	                //String text = ((JTextField)e.getSource()).getText();
	                String text = "fix"; //SBMLreactionFinder.tf.getText();
	                if(text.length()==0) {
	                    setSuggestionModel(comboBox, new DefaultComboBoxModel(list), "");
	                    comboBox.hidePopup();
	                }else{
	                    ComboBoxModel m = getSuggestedModel(list, text);
	                    if(m.getSize()==0 || shouldHide) {
	                        comboBox.hidePopup();
	                    }else{
	                        setSuggestionModel(comboBox, m, text);
	                        comboBox.showPopup();
	                    }
	                }
	            }
	        });
    	}
    }
    @Override public void keyPressed(KeyEvent e) {
        String text = "fix"; //SBMLreactionFinder.tf.getText();
        if(e.getKeyCode()!=KeyEvent.VK_ENTER){
        	shouldHide = false;
        }
        else{
        	shouldHide = true;
        }
        switch(e.getKeyCode()) {
          case KeyEvent.VK_RIGHT:
            for(String s: list) {
            	if(s.startsWith(text)) {
                    // FIX SBMLreactionFinder.tf.setText(s);
                    return;
                }
            }
            break;
            
          case KeyEvent.VK_DOWN:
        	  break;
            
          case KeyEvent.VK_ESCAPE:
            shouldHide = true;
            break;
        }
    }
    private void setSuggestionModel(JComboBox comboBox, ComboBoxModel mdl, String str) {
        mdl.addListDataListener(this);
        

        comboBox.setModel(mdl);
        comboBox.setSelectedIndex(-1);
        ((JTextField)comboBox.getEditor().getEditorComponent()).setText(str);
    }
    private static ComboBoxModel getSuggestedModel(Vector<String> list, String text) {
        DefaultComboBoxModel m = new DefaultComboBoxModel();
        for(String s: list) {
            if(s.startsWith(text)) m.addElement(s);
        }
        return m;
    }
	public void contentsChanged(ListDataEvent arg0) {}
	public void intervalAdded(ListDataEvent arg0) {}
	public void intervalRemoved(ListDataEvent arg0) {}
}
