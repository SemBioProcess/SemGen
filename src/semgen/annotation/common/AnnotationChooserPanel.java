package semgen.annotation.common;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import semgen.SemGenSettings;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.ExternalURLButton;
import semsim.annotation.SemSimTermLibrary;

public abstract class AnnotationChooserPanel extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;
	protected ArrayList<JComponent> lbllist = new ArrayList<JComponent>();
	protected JComboBox<String> combobox = new JComboBox<String>();
	private ArrayList<Integer> comboindicies;
	
	private JPanel itempanel = new JPanel();
	private static Dimension dim = new Dimension(350,30);
	private static Dimension choosedim = new Dimension(9999, 250);
	private ComponentPanelLabel searchlabel;
	private ComponentPanelLabel eraselabel;
	private ComponentPanelLabel modifylabel;
	private ComponentPanelLabel createlabel;
	protected ExternalURLButton urlbutton;
	protected SemSimTermLibrary library;
	
	public static String unspecifiedName = "*unspecified*";
	
	public AnnotationChooserPanel(SemSimTermLibrary lib) {
		super(new BorderLayout());
		library = lib;
		this.setBackground(SemGenSettings.lightblue);
		
		combobox.setPreferredSize(dim);
		combobox.setMaximumSize(dim);
		setAlignmentY(TOP_ALIGNMENT);
		itempanel.setLayout(new FlowLayout(FlowLayout.LEADING, 3, 3));
		itempanel.setBackground(SemGenSettings.lightblue);
		itempanel.add(combobox);
		itempanel.setAlignmentY(TOP_ALIGNMENT);
		setMaximumSize(choosedim);
	}
	
	
	public JComboBox<String> getComboBox(){
		return combobox;
	}
	
	public void makeStaticPanel(int selection) {
		combobox.setEnabled(false);
		String ppname;
		if (selection==-1) {
			ppname = AnnotationChooserPanel.unspecifiedName;
		}
		else {
			ppname = library.getComponentName(selection);
			if (library.isReferenceTerm(selection)) {
				addURLButton();
			}
		}
		combobox.addItem(ppname);
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
	
	
	public void makeForceSelector(){
		combobox.setFont(SemGenFont.defaultItalic());
		addCustomButtons();
		createlabel.setVisible(false);
		combobox.setEnabled(false);
		addEraseButton();
	}
	
	public void constructSelector() {
		for (JComponent btn : lbllist) {
			itempanel.add(btn);
		}
		itempanel.validate();
		add(itempanel, BorderLayout.WEST);
		add(Box.createGlue(), BorderLayout.EAST);
		validate();
	}
	
	public void enableEraseButton(boolean isenabled) {
		if(eraselabel != null) eraselabel.setEnabled(isenabled);
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
				eraseButtonClicked();
			}
		};
		JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
		separator.setPreferredSize(new Dimension(2,25));

		lbllist.add(separator);
		lbllist.add(eraselabel);
	}
	
	
	public void toggleNoneSelected(boolean noselection) {
		if(eraselabel !=null) eraselabel.setEnabled(!noselection);
		if (modifylabel != null) modifylabel.setEnabled(!noselection); 
		if (urlbutton != null) urlbutton.setEnabled(!noselection); 
	}
	
	protected void toggleCustom(boolean iscustom) {
		modifylabel.setEnabled(iscustom);
		toggleWebSearch(!iscustom);
	}
	
	public void toggleWebSearch(boolean iscustom) {
		if (urlbutton != null) urlbutton.setEnabled(iscustom); 
	}
	
	public int getSelection() {
		return comboindicies.get(combobox.getSelectedIndex());
	}
	
	public void setSelection(int index) {
		combobox.setSelectedIndex(comboindicies.indexOf(index));
	}
	
	public void setComboList(ArrayList<Integer> peidlist, Integer selection) {
		combobox.removeActionListener(this);
		combobox.removeAllItems();
		
		setLibraryIndicies(peidlist);
		
		ArrayList<String> idlist = new ArrayList<String>();
		idlist.add(unspecifiedName);
		idlist.addAll(library.getComponentNames(peidlist));
		
		combobox.setModel(new DefaultComboBoxModel<String>(idlist.toArray(new String[]{})));
		setSelection(selection);
		
		toggleNoneSelected(selection == -1);
		if (modifylabel != null && selection != -1) toggleCustom( ! library.isReferenceTerm(selection));
		combobox.addActionListener(this);
		
		combobox.repaint();
	}
	
	public void setLibraryIndicies(ArrayList<Integer> peidlist) {
		comboindicies = new ArrayList<Integer>();
		comboindicies.add(-1);
		comboindicies.addAll(peidlist);
	}
	
	public abstract void searchButtonClicked();
	public abstract void createButtonClicked();
	public abstract void modifyButtonClicked();
	public abstract void eraseButtonClicked();
	

	class WebMouseAdapter extends MouseAdapter {
		public void mouseClicked(MouseEvent arg0) {
			if (urlbutton.isEnabled()) {
				urlbutton.openTerminBrowser(library.getReferenceComponentURI(getSelection()));
			}
		}
	}
}
