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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdom.JDOMException;

import semgen.SemGen;
import semgen.annotation.AnnotatorTab;
import semgen.utilities.GenericThread;
import semgen.utilities.SemGenError;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.ExternalURLButton;
import semsim.Annotatable;
import semsim.SemSimConstants;
import semsim.model.physical.object.PhysicalProperty;
import semsim.webservices.BioPortalConstants;
import semsim.webservices.BioPortalSearcher;
import semsim.webservices.UniProtSearcher;

public class ReferenceClassFinderPanel extends JPanel implements
		ActionListener, ListSelectionListener {
	private static final long serialVersionUID = -7884648622981159203L;
	public AnnotatorTab annotator;
	
	public JComboBox<String> ontologychooser;
	public Map<String,String> ontologySelectionsAndBioPortalIDmap = new HashMap<String,String>();
	private JComboBox<String> findchooser = new JComboBox<String>();

	private JButton findbutton = new JButton("Go");
	private ExternalURLButton externalURLbutton = new ExternalURLButton();
	public JButton loadingbutton = new JButton(SemGenIcon.blankloadingicon);

	public JTextField findbox = new JTextField();
	public JList<String> resultslistright = new JList<String>();

	private Hashtable<String,String> rdflabelsanduris = new Hashtable<String,String>();
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

		createOntologyList();
		
		String[] ontologyboxitems = ontologySelectionsAndBioPortalIDmap.keySet().toArray(new String[]{});
		Arrays.sort(ontologyboxitems);

		JLabel selectKBsource = new JLabel("Select ontology: ");
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
		
		JPanel selectKBsourcepanel = new JPanel();
		selectKBsourcepanel.add(selectKBsource);
		selectKBsourcepanel.add(ontologychooser);
		selectKBsourcepanel.setMaximumSize(new Dimension(900, 40));

		JPanel querypanel = new JPanel();
		querypanel.setLayout(new BoxLayout(querypanel, BoxLayout.X_AXIS));

		JLabel findtext = new JLabel("Term search:  ");
		findtext.setFont(SemGenFont.defaultPlain());

		findchooser.setFont(SemGenFont.defaultItalic(-1));
		findchooser.addItem("contains");
		findchooser.addItem("exact match");
		findchooser.setMaximumSize(new Dimension(125, 25));

		findbox.setForeground(Color.blue);
		findbox.setBorder(BorderFactory.createBevelBorder(1));
		findbox.setFont(SemGenFont.defaultPlain());
		findbox.setMaximumSize(new Dimension(300, 25));
		findbox.addActionListener(this);

		findbutton.setVisible(true);
		findbutton.addActionListener(this);

		JPanel findpanel = new JPanel();
		findpanel.setLayout(new BoxLayout(findpanel, BoxLayout.X_AXIS));
		findpanel.add(findtext);
		findpanel.add(findchooser);
		findpanel.add(findbox);
		findpanel.add(findbutton);

		loadingbutton.setBorderPainted(false);
		loadingbutton.setContentAreaFilled(false);
		findpanel.add(loadingbutton);

		JPanel resultspanelright = new JPanel();
		resultspanelright.setLayout(new BoxLayout(resultspanelright,BoxLayout.Y_AXIS));
		
		JPanel resultspanelrightheader = new JPanel(new BorderLayout());
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

		JScrollPane resultsscrollerright = new JScrollPane(resultslistright);
		resultsscrollerright.setBorder(BorderFactory.createTitledBorder("Search results"));
		resultsscrollerright.setPreferredSize(new Dimension(650, 400));

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
	
	// Create item list for ontology selector box
	private void createOntologyList() {
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
	}
	
	// Show the RDF labels for the classes in the results list instead of the class names
	public void showRDFlabels() {
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
		String text = findbox.getText();
		String ontologyselection = ontologychooser.getSelectedItem().toString();
		// Executed when the search button is pressed
		rdflabelsanduris.clear();
		resultslistright.setEnabled(true);
		resultslistright.removeAll();
		
		String bioportalID = null;
		if (ontologySelectionsAndBioPortalIDmap.get(ontologyselection)!=null)
			bioportalID = ontologySelectionsAndBioPortalIDmap.get(ontologyselection);
		
		// If the user is searching BioPortal
		if (bioportalID!=null) {
			BioPortalSearcher bps = new BioPortalSearcher();
			try {
				bps.search(text, bioportalID, findchooser.getSelectedIndex());
			} catch (IOException e) {
				e.printStackTrace();
				SemGenError.showWebConnectionError("BioPortal web service");
			} catch (JDOMException e) {
				e.printStackTrace();
			}
			
			rdflabelsanduris = bps.rdflabelsanduris;
			if(annotatable!=null){
				if (ontologyselection.startsWith(SemSimConstants.ONTOLOGY_OF_PHYSICS_FOR_BIOLOGY_FULLNAME) && annotatable instanceof PhysicalProperty) {
					rdflabelsanduris = SemGen.semsimlib.removeNonPropertiesFromOPB(rdflabelsanduris);
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
				SemGenError.showWebConnectionError("UniProt web service");
			}
			rdflabelsanduris = ups.rdflabelsanduris;
		}

		// Sort the results
		if (!rdflabelsanduris.isEmpty()) {
			String[] resultsarray = rdflabelsanduris.keySet().toArray(new String[] {});
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

	public String getSelection() {
		return resultslistright.getSelectedValue().trim();
	}
	
	public String getSelectionURI() {
		return rdflabelsanduris.get(getSelection());
	}
	
	public void valueChanged(ListSelectionEvent arg0) {
        boolean adjust = arg0.getValueIsAdjusting();
        if (!adjust) {
          JList<?> list = (JList<?>) arg0.getSource();
          if(list.getSelectedValue()!=null){
        	  String termuri = rdflabelsanduris.get(list.getSelectedValue());
        	  externalURLbutton.setTermURI(URI.create(termuri));
          }
        }
	}
}
