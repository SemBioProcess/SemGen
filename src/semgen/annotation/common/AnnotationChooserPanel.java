package semgen.annotation.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import semgen.SemGenSettings;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.ExternalURLButton;

public abstract class AnnotationChooserPanel extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;
	private ArrayList<JLabel> lbllist = new ArrayList<JLabel>();
	protected JComboBox<String> combobox = new JComboBox<String>();
	
	private JPanel itempanel = new JPanel();
	private static Dimension dim = new Dimension(350,30);
	private ComponentPanelLabel searchlabel;
	private ComponentPanelLabel eraselabel;
	private ComponentPanelLabel modifylabel;
	private ComponentPanelLabel createlabel;
	protected ExternalURLButton urlbutton;
	public static String unspecifiedName = "*unspecified*";
	
	public AnnotationChooserPanel() {
		super(new BorderLayout());
		this.setBackground(SemGenSettings.lightblue);
		
		combobox.setPreferredSize(dim);
		combobox.setMaximumSize(dim);
		
		itempanel.setBackground(SemGenSettings.lightblue);
		itempanel.add(combobox);

	}
	
	public void makeStaticPanel(String selection, boolean isrefterm) {
		combobox.setEnabled(false);
		combobox.addItem(selection);
		if (isrefterm) {
			addURLButton();
		}
	}
	
	public void makePhysicalPropertySelector() {
		addURLButton();
		addSearchButton();
		addEraseButton();
	}

	public void makeEntitySelector() {
		addURLButton();
		addSearchButton();
		addCustomButtons();
		addEraseButton();
	}
	
	public void makeProcessSelector() {
		combobox.setFont(SemGenFont.defaultItalic());
		addCustomButtons();
		addEraseButton();
	}
	
	public void constructSelector() {
		for (JLabel btn : lbllist) {
			itempanel.add(btn);
		}
		itempanel.validate();
		add(itempanel, BorderLayout.WEST);
		add(Box.createGlue(), BorderLayout.EAST);
		validate();
	}
	
	protected void addURLButton() {
		urlbutton = new ExternalURLButton();
		urlbutton.addMouseListener(new WebMouseAdapter());
		lbllist.add(urlbutton);
	}
	
	@SuppressWarnings("serial")
	protected void addSearchButton() {
		searchlabel = new ComponentPanelLabel(SemGenIcon.searchicon,"Look up reference ontology term") {
			public void onClick() {
				searchButtonClicked();
			}
		};
		lbllist.add(searchlabel);
	}
	
	@SuppressWarnings("serial")
	protected void addCustomButtons() {
			createlabel = new ComponentPanelLabel(SemGenIcon.createicon,"Create new custom term"){ 
				public void onClick() {
					createButtonClicked();
				}
			};
			modifylabel = new ComponentPanelLabel(SemGenIcon.modifyicon, "Edit custom term") {
				public void onClick() {
					modifyButtonClicked();
				}
			};
		
			itempanel.add(createlabel);
			itempanel.add(modifylabel);
	}
	
	@SuppressWarnings("serial")
	protected void addEraseButton() {
		eraselabel = new ComponentPanelLabel(SemGenIcon.eraseicon, "Remove annotation component"){
			public void onClick() {
				searchButtonClicked();
			}
		};
		lbllist.add(eraselabel);
	}
	
	public void toggleNoneSelected() {
		combobox.setSelectedIndex(0);
		if (modifylabel != null) modifylabel.setEnabled(false); 
		if (urlbutton != null) urlbutton.setEnabled(false); 
	}
	
	public void toggleCustom(boolean iscustom) {
		modifylabel.setEnabled(iscustom);
		toggleWebSearch(!iscustom);
	}
	
	public void toggleWebSearch(boolean iscustom) {
		if (urlbutton != null) urlbutton.setEnabled(iscustom); 
	}
	
	public int getSelection() {
		return combobox.getSelectedIndex()-1;
	}
	
	public void setSelection(int index) {
		combobox.setSelectedIndex(index+1);
	}
	
	public void setComboList(ArrayList<String> peidlist, Integer item) {
		combobox.removeActionListener(this);
		combobox.removeAllItems();
		ArrayList<String> idlist = new ArrayList<String>();
		idlist.add(unspecifiedName);
		idlist.addAll(peidlist);
		
		combobox.setModel(new DefaultComboBoxModel<String>(idlist.toArray(new String[]{})));
		combobox.setSelectedIndex(item+1);
		combobox.repaint();
		combobox.addActionListener(this);
	}
	
	public abstract void webButtonClicked();
	public abstract void eraseButtonClicked();
	public abstract void searchButtonClicked();
	public abstract void createButtonClicked();
	public abstract void modifyButtonClicked();
	
	class ComponentPanelLabel extends JLabel {
		private static final long serialVersionUID = 1L;

		ComponentPanelLabel(Icon icon, String tooltip) {
			super(icon);
			setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
			setBackground(Color.white);
			addMouseListener(new LabelMouseBehavior());
			setToolTipText(tooltip);
		}
		
		public void onClick() {}
		
		class LabelMouseBehavior extends MouseAdapter {
			public void mouseClicked(MouseEvent arg0) {
				onClick();
			}
			
			public void mouseEntered(MouseEvent e) {
				setCursor(new Cursor(Cursor.HAND_CURSOR));
			}
	
			public void mouseExited(MouseEvent e) {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
			public void mousePressed(MouseEvent arg0) {
				setBorder(BorderFactory.createLineBorder(Color.blue,1));
			}
	
			public void mouseReleased(MouseEvent arg0) {
				setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
			}
		}
	}

	class WebMouseAdapter extends MouseAdapter {
		public void mouseClicked(MouseEvent arg0) {
			webButtonClicked();
		}
	}
}
