package semgen.resource.uicomponent;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxEditor;

public class DropDownCheckList extends JComboBox<CheckBox> implements ActionListener {
	private static final long serialVersionUID = 1L;
	CheckBox combotitle, current;
	public DropDownCheckList(String title) {
		super(new DefaultComboBoxModel<CheckBox>());
		setEditable(true);
		setEditor(new ComboBoxEditor());
		combotitle = new CheckBox(title,false);
		setRenderer(new CheckComboRenderer());
		current = combotitle;
		setSelectedItem(combotitle);
		addActionListener(this); 
	}

	    public void actionPerformed(ActionEvent e) { 
		    	CheckBox store = (CheckBox) getSelectedItem();
		    	this.disableEvents(ActionEvent.ACTION_PERFORMED);
			    if (store != combotitle) {
			    	current = store;
			    	current.setSelected(!current.isSelected());
			    	setSelectedItem(combotitle);
		    	}
			   this.enableEvents(ActionEvent.ACTION_PERFORMED);
	    } 
	    //Need to disable the action performed event to prevent the listener from triggering
	    //when an item is added.
	    public void addItem(String caption, boolean selected) {
	    	this.disableEvents(ActionEvent.ACTION_PERFORMED);
	    	super.addItem(new CheckBox(caption, selected));
	    	this.enableEvents(ActionEvent.ACTION_PERFORMED);
	    }
	    
	    public String toString() {
			return getName();
	    }
	    public String getLastSelectedItem() {
	    	String cur = current.toString();
	    	current = combotitle;
	    	return cur;
	    }
	    
		/** adapted from comment section of ListCellRenderer api */  
	class CheckComboRenderer extends JCheckBox implements ListCellRenderer<Object>  {  
			private static final long serialVersionUID = 1L;
	
			public CheckComboRenderer() {
				this.setOpaque(true);
			}
		
		    public JComponent getListCellRendererComponent(JList<?> list,  
		                                                  Object item,  
		                                                  int index,  
		                                                  boolean isSelected,  
		                                                  boolean cellHasFocus) {
		    	
		    	if (item==combotitle) {
		    		JLabel title = new JLabel(combotitle.toString());
		    		title.setForeground(Color.gray);
		    		title.setOpaque(true);
		    		return title;
		    	}
		    	
			    setText(item.toString());
			    setSelected(((CheckBox)item).isSelected());  
			    setBackground(((CheckBox)item).isSelected() ? Color.red : Color.white);  
			    setForeground(((CheckBox)item).isSelected() ? Color.white : Color.black);  
	
		        return this;  
		    }  
		}

	class ComboBoxEditor extends BasicComboBoxEditor {
		  public ComboBoxEditor() {
		   editor.setEditable(false);
		  }
		 }
}
class CheckBox extends Component {
		private static final long serialVersionUID = 1L;
		String caption;
		String tooltip;
		boolean selected;
		public CheckBox(String capt, boolean sel) {
			caption = capt;
			selected = sel;
		}
		
		public Boolean isSelected() {
			return selected;
		}
		
		public void setSelected(boolean sel) {
			 selected = sel;
		}
		
		public String toString() {
			return caption;
		}
	}