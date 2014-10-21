package semgen.annotation.dialog.referenceclass;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdom.JDOMException;

import semgen.SemGen;
import semgen.SemGenGUI;
import semgen.annotation.AnnotatorTab;
import semgen.annotation.annotatorpane.AnnotationPanel;
import semgen.resource.GenericThread;
import semgen.resource.SemGenError;
import semgen.resource.SemGenFont;
import semgen.resource.SemGenIcon;
import semgen.resource.uicomponent.ExternalURLButton;
import semsim.Annotatable;
import semsim.SemSimConstants;
import semsim.model.physical.PhysicalProperty;
import semsim.webservices.BioPortalConstants;
import semsim.webservices.BioPortalSearcher;
import semsim.webservices.UniProtSearcher;

public class ReferenceClassFinderPanel extends JPanel implements
		ActionListener, ListSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7884648622981159203L;
	public JPanel panelright;
	public AnnotatorTab annotator;
	public String codeword;
	public AnnotationPanel anndialog;
	private JPanel selectKBsourcepanel;
	private JLabel selectKBsource;

	public JComboBox<String> ontologychooser;
	public Map<String,String> ontologySelectionsAndBioPortalIDmap = new HashMap<String,String>();
	private JComboBox<String> findchooser;

	public String[] existingresultskeysetarray;
	private JPanel querypanel;
	private JButton findbutton;
	private ExternalURLButton externalURLbutton;
	public JButton loadingbutton = new JButton(SemGenIcon.blankloadingicon);

	private JLabel findtext;
	private JPanel findpanel;
	public JTextField findbox;
	private JPanel resultspanelrightheader;
	private JPanel resultspanelright;
	public JList<String> resultslistright = new JList<String>();
	private JScrollPane resultsscrollerright;

	public Hashtable<String,String> resultsanduris = new Hashtable<String,String>();
	public Hashtable<String,String> rdflabelsanduris = new Hashtable<String,String>();
	public Hashtable<String, String> classnamesandshortconceptids = new Hashtable<String, String>();
	public JOptionPane optionPane;
	public String bioportalID = null;
	public GenericThread querythread = new GenericThread(this, "performSearch");
	public Annotatable annotatable;
	private String[] ontList;

	public ReferenceClassFinderPanel(AnnotatorTab ann, Annotatable annotatable, String[] ontList) {
		annotator = ann;
		this.ontList = ontList;
		this.annotatable = annotatable;
		setUpUI();
	}
	
	public ReferenceClassFinderPanel(AnnotatorTab ann, String[] ontList) {
		annotator = ann;
		this.ontList = ontList;
		setUpUI();
	}
	
	// Set up the interface
	public void setUpUI(){

		setOpaque(false);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// Create item list for ontology selector box
		for(String ontfullname : ontList){
			String itemtext = ontfullname;
			if(SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.containsKey(ontfullname)){
				itemtext = itemtext + " (" + SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.get(ontfullname) + ")";
			}
			if(SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.containsKey(ontfullname)
					&& BioPortalConstants.ONTOLOGY_FULL_NAMES_AND_BIOPORTAL_IDS.containsKey(ontfullname)){
				ontologySelectionsAndBioPortalIDmap.put(itemtext, SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.get(ontfullname));
			}
			else ontologySelectionsAndBioPortalIDmap.put(itemtext, null);
		}
		
		String[] ontologyboxitems = ontologySelectionsAndBioPortalIDmap.keySet().toArray(new String[]{});
		Arrays.sort(ontologyboxitems);

		selectKBsourcepanel = new JPanel();
		selectKBsource = new JLabel("Select ontology: ");
		selectKBsource.setFont(SemGenFont.defaultPlain());

		ontologychooser = new JComboBox<String>(ontologyboxitems);
		ontologychooser.setFont(SemGenFont.defaultPlain());
		
		// Set ontology chooser to recently used ontology
		ontologychooser.setSelectedIndex(0);
		if(annotator.ontspref!=null){
			for(String ont : ontologyboxitems){
				if(ont.equals(annotator.ontspref)){
					ontologychooser.setSelectedItem(annotator.ontspref);
				}
			}
		}
		ontologychooser.addActionListener(this);

		selectKBsourcepanel.add(selectKBsource);
		selectKBsourcepanel.add(ontologychooser);
		selectKBsourcepanel.setMaximumSize(new Dimension(900, 40));

		querypanel = new JPanel();
		querypanel.setLayout(new BoxLayout(querypanel, BoxLayout.X_AXIS));

		findtext = new JLabel("Term search:  ");
		findtext.setFont(SemGenFont.defaultPlain());

		findchooser = new JComboBox<String>();
		findchooser.setFont(SemGenFont.defaultItalic(-1));
		findchooser.addItem("contains");
		findchooser.addItem("exact match");
		findchooser.setMaximumSize(new Dimension(125, 25));

		findbox = new JTextField();
		findbox.setForeground(Color.blue);
		findbox.setBorder(BorderFactory.createBevelBorder(1));
		findbox.setFont(SemGenFont.defaultPlain());
		findbox.setMaximumSize(new Dimension(300, 25));
		findbox.addActionListener(this);

		findbutton = new JButton("Go");
		findbutton.setVisible(true);
		findbutton.addActionListener(this);

		findpanel = new JPanel();
		findpanel.setLayout(new BoxLayout(findpanel, BoxLayout.X_AXIS));
		findpanel.add(findtext);
		findpanel.add(findchooser);
		findpanel.add(findbox);
		findpanel.add(findbutton);

		loadingbutton.setBorderPainted(false);
		loadingbutton.setContentAreaFilled(false);
		findpanel.add(loadingbutton);

		resultspanelright = new JPanel();
		resultspanelright.setLayout(new BoxLayout(resultspanelright,BoxLayout.Y_AXIS));
		
		resultspanelrightheader = new JPanel(new BorderLayout());
		resultspanelrightheader.setOpaque(false);

		JLabel resultslabelright = new JLabel("Search results");
		resultslabelright.setFont(SemGenFont.defaultPlain());
		resultslabelright.setEnabled(true);

		resultslistright = new JList<String>();
		resultslistright.addListSelectionListener(this);
		resultslistright.setBackground(Color.white);
		resultslistright.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultslistright.setBorder(BorderFactory.createBevelBorder(1));
		resultslistright.setEnabled(true);

		Dimension scrollerdim = new Dimension(650, 400);
		resultsscrollerright = new JScrollPane(resultslistright);
		resultsscrollerright.setBorder(BorderFactory.createTitledBorder("Search results"));
		resultsscrollerright.setPreferredSize(scrollerdim);

		externalURLbutton = new ExternalURLButton();

		JPanel rightscrollerbuttonpanel = new JPanel(new BorderLayout());
		rightscrollerbuttonpanel.setOpaque(false);
		JPanel rightscrollerinfobuttonpanel = new JPanel();
		rightscrollerinfobuttonpanel.add(externalURLbutton);
		rightscrollerbuttonpanel.add(rightscrollerinfobuttonpanel, BorderLayout.WEST);
		rightscrollerbuttonpanel.add(Box.createGlue(), BorderLayout.EAST);
		resultspanelrightheader.add(resultslabelright, BorderLayout.WEST);
		resultspanelrightheader.add(Box.createGlue(), BorderLayout.EAST);

		resultspanelright.setOpaque(false);

		JComponent[] arrayright = { selectKBsourcepanel, querypanel, findpanel, resultsscrollerright, rightscrollerbuttonpanel};

		for (int i = 0; i < arrayright.length; i++) {
			this.add(arrayright[i]);
		}
		findbox.requestFocusInWindow();
	}

	// Show the RDF labels for the classes in the results list instead of the class names
	public void showRDFlabels() {
		resultsanduris = rdflabelsanduris;
		String[] resultsarray = (String[]) rdflabelsanduris.keySet().toArray(new String[] {});
		Arrays.sort(resultsarray);
		resultslistright.setListData(resultsarray);
	}

	
	public void actionPerformed(ActionEvent arg0) {
		Object o = arg0.getSource();
		if ((o == findbox || o == findbutton || o == findchooser) && !findbox.getText().equals("")) {
			loadingbutton.setIcon(SemGenIcon.loadingicon);
			findbox.setEnabled(false);
			findbutton.setEnabled(false);
			resultslistright.setListData(new String[] {});
			externalURLbutton.setTermURI(null);
			querythread = new GenericThread(this, "performSearch");
			querythread.start();
		}

		if (o == ontologychooser) {
			if(ontologychooser.getItemCount()>2){
				annotator.ontspref = (String) ontologychooser.getSelectedItem();
			}
		}
	}

	public void performSearch() {
		bioportalID = null;
		String text = findbox.getText();
		String ontologyselection = ontologychooser.getSelectedItem().toString();
		// Executed when the search button is pressed
		rdflabelsanduris.clear();
		classnamesandshortconceptids.clear();
		resultslistright.setEnabled(true);
		resultslistright.removeAll();

		// If the user is searching BioPortal
		if (bioportalID!=null && SemGenGUI.annotateitemusebioportal.isSelected()) {
			BioPortalSearcher bps = new BioPortalSearcher();
			try {
				bps.search(text, bioportalID, findchooser.getSelectedIndex());
			} catch (IOException e) {
				e.printStackTrace();
				SemGenError.showWebConnectionError(null, "BioPortal web service");
			} catch (JDOMException e) {e.printStackTrace();}
			
			rdflabelsanduris = bps.rdflabelsanduris;
			classnamesandshortconceptids = bps.classnamesandshortconceptids;
			
			if(annotatable!=null){
				if(ontologyselection.startsWith(SemSimConstants.ONTOLOGY_OF_PHYSICS_FOR_BIOLOGY_FULLNAME) && 
						annotatable instanceof PhysicalProperty){
					rdflabelsanduris = removeNonPropertiesFromOPB(rdflabelsanduris);
				}
			}
		}
		else if(ontologyselection.startsWith(SemSimConstants.UNIPROT_FULLNAME)){
			UniProtSearcher ups = new UniProtSearcher();
			try {
				ups.search(text);
			} catch (JDOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				SemGenError.showWebConnectionError(null, "UniProt web service");
			}
			rdflabelsanduris = ups.rdflabelsanduris;
		}

		// Sort the results
		if (!rdflabelsanduris.isEmpty()) {
			resultsanduris = rdflabelsanduris;
			String[] resultsarray = (String[]) resultsanduris.keySet().toArray(new String[] {});
			Arrays.sort(resultsarray);
			resultslistright.setListData(resultsarray);
		} 
		else {
			resultslistright.setListData(new String[] { "---Search returned no results---" });
			resultslistright.setEnabled(false);
		}

		findbutton.setText("Go");
		loadingbutton.setIcon(SemGenIcon.blankloadingicon);
		findbox.setEnabled(true);
		findbutton.setEnabled(true);
	}
	
	// Remove any OPB terms that are not Physical Properties
	public Hashtable<String,String> removeNonPropertiesFromOPB(Hashtable<String, String> table){
		Hashtable<String,String> newtable = new Hashtable<String,String>();
		for(String key : table.keySet()){
			if(SemGen.semsimlib.OPBhasProperty(table.get(key)))
				newtable.put(key, table.get(key));
		}
		return newtable;
	}
	
	public void valueChanged(ListSelectionEvent arg0) {
        boolean adjust = arg0.getValueIsAdjusting();
        if (!adjust) {
          JList<?> list = (JList<?>) arg0.getSource();
          if(list.getSelectedValue()!=null){
        	  String termuri = (String) resultsanduris.get(list.getSelectedValue());
        	  externalURLbutton.setTermURI(URI.create(termuri));
          }
        }
	}
}
