package semgen.annotation.dialog.referencedialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import semgen.annotation.annotationtree.BrowseTree;
import semgen.resource.GenericThread;
import semgen.resource.SemGenIcon;
import semgen.resource.SemGenFont;
import semgen.resource.uicomponents.ExternalURLButton;
import semsim.SemSim;
import semsim.SemSimConstants;
import semsim.webservices.BioPortalConstants;

public class ReferenceClassFinderPanel extends JPanel implements
		ActionListener, ListSelectionListener {

	private static final long serialVersionUID = -7884648622981159203L;

	public JComboBox<String> ontologychooser;
	public Map<String,String> ontologySelectionsAndBioPortalIDmap = new HashMap<String,String>();
	private JComboBox<String> findchooser = new JComboBox<String>(new String[]{"contains", "exact match"});
	private JButton findbutton = new JButton("Go");
	private ExternalURLButton externalURLbutton = new ExternalURLButton();
	public JButton loadingbutton = new JButton(SemGenIcon.blankloadingicon);

	public JTextField findbox = new JTextField();
	public JList<String> resultslistright = new JList<String>();

	public Hashtable<String,String> resultsanduris = new Hashtable<String,String>();
	public Hashtable<String,String> rdflabelsanduris = new Hashtable<String,String>();
	public GenericThread querythread = new GenericThread(this, "performSearch");
	private static String ontspref;
		
	public ReferenceClassFinderPanel(String[] ontList) {
		setUpUI(ontList);
	}
	
	// Set up the interface
	public void setUpUI(String[] ontList){
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

		ontologychooser = new JComboBox<String>(ontologyboxitems);
		ontologychooser.setFont(SemGenFont.defaultPlain());
		
		// Set ontology chooser to recently used ontology
		if(ontspref!=null){
			for(String ont : ontologyboxitems){
				if(ont.equals(ontspref)){
					ontologychooser.setSelectedItem(ontspref);
				}
			}
		}
		ontologychooser.addActionListener(this);

		JLabel selectKBsource = new JLabel("Select ontology: ");
		selectKBsource.setFont(SemGenFont.defaultPlain());
		
		JPanel selectKBsourcepanel = new JPanel();
		selectKBsourcepanel.add(selectKBsource);
		selectKBsourcepanel.add(ontologychooser);
		selectKBsourcepanel.setMaximumSize(new Dimension(900, 40));

		JLabel findtext = new JLabel("Term search:  ");
		findtext.setFont(SemGenFont.defaultPlain());

		findchooser.setFont(SemGenFont.defaultItalic(-1));
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

		resultslistright.addListSelectionListener(this);
		resultslistright.setBackground(Color.white);
		resultslistright.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultslistright.setBorder(BorderFactory.createBevelBorder(1));
		resultslistright.setEnabled(true);

		BrowseTree browsetree = new BrowseTree();
		browsetree.tree.setEnabled(false);

		JScrollPane resultsscrollerright = new JScrollPane(resultslistright);
		resultsscrollerright.setBorder(BorderFactory.createTitledBorder("Search results"));
		resultsscrollerright.setPreferredSize(new Dimension(650, 400));
		JScrollPane resultsscrollerleft = new JScrollPane(browsetree.tree);
		resultsscrollerleft.setEnabled(false);

		JPanel rightscrollerbuttonpanel = new JPanel(new BorderLayout());
		rightscrollerbuttonpanel.setOpaque(false);
		JPanel rightscrollerinfobuttonpanel = new JPanel();
		rightscrollerinfobuttonpanel.add(externalURLbutton);
		rightscrollerbuttonpanel.add(rightscrollerinfobuttonpanel, BorderLayout.WEST);
		rightscrollerbuttonpanel.add(Box.createGlue(), BorderLayout.EAST);

		JComponent[] arrayright = { selectKBsourcepanel, findpanel, resultsscrollerright, rightscrollerbuttonpanel};

		for (int i = 0; i < arrayright.length; i++) {
			add(arrayright[i]);
		}
		findbox.requestFocusInWindow();
	}

	// Show the RDF labels for the classes in the results list instead of the class names
	public void showRDFlabels() {
		resultsanduris = rdflabelsanduris;
		String[] resultsarray = rdflabelsanduris.keySet().toArray(new String[] {});
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
			querythread.start();
		}

		if (o == ontologychooser && ontologychooser.getItemCount()>2) {
				ontspref = (String) ontologychooser.getSelectedItem();
		}
	}
	
	// Remove any OPB terms that are not Physical Properties
	public void removeNonPropertiesFromOPB(Hashtable<String, String> table){
		for(String key : table.keySet()){
			if(!SemSim.semsimlib.OPBhasProperty(table.get(key)))
				table.remove(key);
		}
	}
	
	public void valueChanged(ListSelectionEvent arg0) {
        if (!arg0.getValueIsAdjusting()) {
          JList<String> list = (JList<String>)arg0.getSource();
          if(list.getSelectedValue()!=null){
        	  String termuri = resultsanduris.get(list.getSelectedValue());
        	  externalURLbutton.setTermURI(URI.create(termuri)); 
          }
        }
	}
}
