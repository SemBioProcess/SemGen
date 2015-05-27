package semgen.annotation.dialog.termlibrary; 

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.BorderFactory;

import java.awt.event.MouseEvent;

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
import semgen.annotation.workbench.SemSimTermLibrary;
import semgen.utilities.GenericThread;
import semgen.utilities.SemGenError;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.ExternalURLButton;
import semsim.SemSimConstants;
import semsim.model.physical.object.PhysicalProperty;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.model.physical.object.ReferencePhysicalProcess;
import semsim.utilities.ReferenceOntologies.OntologyDomain;
import semsim.utilities.ReferenceOntologies.ReferenceOntology;
import semsim.utilities.webservices.BioPortalSearcher;
import semsim.utilities.webservices.UniProtSearcher;

public class ReferenceClassFinderPanel extends JPanel implements
		ActionListener, ListSelectionListener {
	private static final long serialVersionUID = -7884648622981159203L;
	private SemSimTermLibrary library;
	
	private JComboBox<String> ontologychooser;
	private JComboBox<String> findchooser = new JComboBox<String>();

	private JButton findbutton = new JButton("Go");
	private ExternalURLButton externalURLbutton;
	private JButton loadingbutton = new JButton(SemGenIcon.blankloadingicon);

	private JTextField findbox = new JTextField();
	private JList<String> resultslistright = new JList<String>();

	private HashMap<String,String> rdflabelsanduris = new HashMap<String,String>();
	private GenericThread querythread = new GenericThread(this, "performSearch");
	private OntologyDomain domain;
	private ReferenceOntology selected = null;
	private int termindex = -1;
	
	public ReferenceClassFinderPanel(SemSimTermLibrary lib, OntologyDomain dom) {
		library = lib;
		domain = dom;
		setUpUI();
	}
	
	// Set up the interface
	@SuppressWarnings("serial")
	public void setUpUI(){
		setOpaque(false);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JLabel selectKBsource = new JLabel("Select ontology: ");
		selectKBsource.setFont(SemGenFont.defaultPlain());

		ontologychooser = new JComboBox<String>(domain.getArrayofOntologyNames());
		ontologychooser.setFont(SemGenFont.defaultPlain());
		
		// Set ontology chooser to recently used ontology
		ontologychooser.setSelectedIndex(0);

		ontologychooser.addActionListener(this);
		if(domain.domainhasReferenceOntology(library.getLastOntology())){
			ontologychooser.setSelectedIndex(domain.getOrdinalofOntology(library.getLastOntology()));
		}
		else {
			ontologychooser.setSelectedIndex(0);
		}
		
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
		
		externalURLbutton = new ExternalURLButton() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				String seluri = rdflabelsanduris.get(resultslistright.getSelectedValue());
				openTerminBrowser(URI.create(seluri));
			}
		};
		rightscrollerinfobuttonpanel.add(externalURLbutton);
		externalURLbutton.setEnabled(false);
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
			querythread = new GenericThread(this, "performSearch");
			querythread.start();
		}

		if (o == ontologychooser) {
			selected = domain.getDomainOntologyatIndex(ontologychooser.getSelectedIndex());
			if(ontologychooser.getItemCount()>2){
				library.setLastOntology(selected);
			}
		}
	}

	// Executed when the search button is pressed
	public void performSearch() {
		String text = findbox.getText();
		rdflabelsanduris.clear();
		resultslistright.setEnabled(true);
		resultslistright.removeAll();
		
		String bioportalID = selected.getBioPortalID();
		
		// If the user is searching BioPortal
		if (bioportalID!=null) {
			BioPortalSearcher bps = new BioPortalSearcher();
			try {
				rdflabelsanduris = bps.search(text, bioportalID, findchooser.getSelectedIndex());
			} catch (IOException e) {
				e.printStackTrace();
				SemGenError.showWebConnectionError("BioPortal web service");
			} catch (JDOMException e) {
				e.printStackTrace();
			}

			if (domain.equals(OntologyDomain.PhysicalProperty) ) {
				rdflabelsanduris = SemGen.semsimlib.removeNonPropertiesFromOPB(rdflabelsanduris);
			}
		}
		else if(selected.getFullName().startsWith(SemSimConstants.UNIPROT_FULLNAME)){
			UniProtSearcher ups = new UniProtSearcher();
			try {
				rdflabelsanduris = ups.search(text);
			} catch (JDOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				SemGenError.showWebConnectionError("UniProt web service");
			}
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
			externalURLbutton.setEnabled(false);
		}

		findbutton.setText("Go");
		loadingbutton.setIcon(SemGenIcon.blankloadingicon);
		findbox.setEnabled(true);
		findbutton.setEnabled(true);
	}
	
	public void addTermtoLibrary() {
		String sel = resultslistright.getSelectedValue();
		URI uri = URI.create(rdflabelsanduris.get(sel));
		if (domain.equals(OntologyDomain.PhysicalProperty)) {
			termindex = library.addPhysicalProperty(new PhysicalProperty(sel, uri));
		}
		if (domain.equals(OntologyDomain.PhysicalEntity)) {
			termindex = library.addReferencePhysicalEntity(new ReferencePhysicalEntity(uri, sel));
		}
		if (domain.equals(OntologyDomain.PhysicalProcess)) {
			termindex = library.addPhysicalProcess(new ReferencePhysicalProcess(uri, sel));
		}
	}
	
	public int getSelectedTermIndex() {
		return termindex;
	}
	
	public void clearSelection() {
		termindex = -1;
	}
	
	public void valueChanged(ListSelectionEvent arg0) {
        boolean adjust = arg0.getValueIsAdjusting();
        if (!adjust) {
	        if(!resultslistright.isSelectionEmpty()){
	        	externalURLbutton.setEnabled(true);
	        }
        }
	}
}
