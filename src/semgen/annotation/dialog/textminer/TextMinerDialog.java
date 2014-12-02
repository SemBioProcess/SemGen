package semgen.annotation.dialog.textminer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import semsim.ResourcesManager;
import semgen.utilities.GenericThread;
import semgen.utilities.SemGenError;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenDialog;
import semgen.utilities.uicomponent.SemGenScrollPane;
import semgen.utilities.uicomponent.SemGenTextArea;
import semsim.SemSimConstants;
import semsim.model.physical.object.ReferencePhysicalEntity;
import semsim.model.physical.object.ReferencePhysicalProcess;
import semsim.webservices.BioPortalAnnotatorClient;
import semsim.webservices.BioPortalConstants;

public class TextMinerDialog extends SemGenDialog implements PropertyChangeListener, ActionListener{
	private static final long serialVersionUID = -4832887891785870465L;
	public JButton parsebutton = new JButton("Parse");
	public JTextField pmarea = new JTextField();
	public JButton pmbutton = new JButton("Find abstract");
	public String pubmedid;
	public SemGenTextArea inputtextarea = new SemGenTextArea();
	public SemGenScrollPane spresults;
	public JOptionPane optionPane;
	private JPanel sppanel = new JPanel();
	public JPanel resultspanel = new JPanel();
	public Namespace zns = Namespace.getNamespace("z", "http://www.ebi.ac.uk/z");

	public JButton loadingbutton = new JButton(SemGenIcon.blankloadingicon);
	protected Set<ReferencePhysicalEntity> collectedents = new HashSet<ReferencePhysicalEntity>();
	protected Set<ReferencePhysicalProcess> collectedproc = new HashSet<ReferencePhysicalProcess>();

	public TextMinerDialog() throws FileNotFoundException{		
		super("Parse text for ontology terms");
		JLabel toplabel = new JLabel("Enter text to parse, or find a PubMed abstract by its ID, then hit \"Parse\" to identify ontology terms");
		parsebutton.setMaximumSize(new Dimension(100,999999));
		parsebutton.addActionListener(this);

		loadingbutton.setBorderPainted(false);
		loadingbutton.setContentAreaFilled(false);
		
		JPanel pmpanel = new JPanel();
		JLabel pmlabel = new JLabel("Enter PubMed ID");
		pmbutton.addActionListener(this);
		pmarea.setPreferredSize(new Dimension(150,30));
		pmarea .setMaximumSize(new Dimension(150,30));
		pmarea.addActionListener(this);
		pmpanel.add(pmlabel);
		pmpanel.add(pmarea);
		pmpanel.add(pmbutton);
		
		JPanel parsepanel = new JPanel();
		parsepanel.add(parsebutton);
		parsepanel.add(loadingbutton);
		
		inputtextarea.setLineWrap(true);
		inputtextarea.setWrapStyleWord(true);
		inputtextarea.setFont(SemGenFont.defaultPlain(-1));

		SemGenScrollPane sptext = new SemGenScrollPane(inputtextarea);
		sptext.setPreferredSize(new Dimension(350,230));
		JPanel toppanel = new JPanel(new BorderLayout());
		toppanel.add(parsepanel, BorderLayout.WEST);
		toppanel.add(pmpanel, BorderLayout.EAST);
		
		sppanel.setLayout(new BoxLayout(sppanel, BoxLayout.Y_AXIS));
		JCheckBox fmabox = new JCheckBox(SemSimConstants.FOUNDATIONAL_MODEL_OF_ANATOMY_FULLNAME);
		JCheckBox chebibox = new JCheckBox(SemSimConstants.CHEMICAL_ENTITIES_OF_BIOLOGICAL_INTEREST_FULLNAME);
		JCheckBox gobox = new JCheckBox(SemSimConstants.GENE_ONTOLOGY_FULLNAME);

		JCheckBox[] checkboxarray = new JCheckBox[]{
				fmabox,
				chebibox,
				gobox
				};
		for(int c=0; c<checkboxarray.length; c++){
			sppanel.add(checkboxarray[c]);
			checkboxarray[c].setSelected(true);
		}
		
		SemGenScrollPane spboxes = new SemGenScrollPane(sppanel);
		spboxes.setPreferredSize(new Dimension(350,230));
		
		resultspanel.setLayout(new BoxLayout(resultspanel,BoxLayout.Y_AXIS));
		spresults = new SemGenScrollPane(resultspanel);
		spresults.setPreferredSize(new Dimension(400, 230));
		
		JSplitPane splitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,sptext,spboxes),spresults);
		splitpane.setMinimumSize(new Dimension(500,400));
		
		Object[] array = { toplabel, toppanel, splitpane, new JLabel()};
		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
		optionPane.addPropertyChangeListener(this);
		Object[] options = new Object[] { "Collect & Close", "Collect", "Close" };
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);
		pmarea.requestFocusInWindow();
		
		setContentPane(optionPane);
		showDialog();
	}
	
	// Perform natural-language processing via BioPortal Annotator service to find ontology terms
	public void parse() throws FileNotFoundException{
		if(!inputtextarea.getText().startsWith("\\W")){inputtextarea.setText(" " + inputtextarea.getText());}
		if(!inputtextarea.getText().endsWith("\\W")){inputtextarea.append(" ");}
		String texttoparse = inputtextarea.getText().trim();
		inputtextarea.removeAllHighlights();
		resultspanel.removeAll();
		
		Set<String> ontologyids = new HashSet<String>();
		// Limit the annotation to just the selected ontologies 
		ontologyids.clear();
		for(int y=0; y<sppanel.getComponentCount(); y++){
			if(sppanel.getComponent(y) instanceof JCheckBox){
				JCheckBox jcbox = (JCheckBox)sppanel.getComponent(y);
				if(jcbox.isSelected()){
					System.out.println(jcbox.getText());
					ontologyids.add(BioPortalConstants.ONTOLOGY_FULL_NAMES_AND_BIOPORTAL_IDS.get(jcbox.getText()));
				}
			}
		}
		
		BioPortalAnnotatorClient client = new BioPortalAnnotatorClient(texttoparse, ontologyids, 
				ResourcesManager.createHashtableFromFile("cfg/NCBOAnnotatorSettings.txt"));
		String xmlresult = null;
		try {
			xmlresult = client.annotate();
		} catch (IOException e1) {
			SemGenError.showWebConnectionError("BioPortal Annotator service");
		} 
		
		try {
			Reader in = new StringReader(xmlresult);
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(in);
			Iterator<Element> annit = doc.getRootElement().getChild("data").getChild("annotatorResultBean").getChild("annotations").getChildren("annotationBean").iterator();
			if(!annit.hasNext()){resultspanel.add(new JLabel("NCBO annotator did not find any ontology terms"));}
			else{
				Set<String> boxnames = new HashSet<String>();
				while(annit.hasNext()){
					
					Element concept = annit.next().getChild("concept");
					String prefname = concept.getChildText("preferredName");
					String uri = concept.getChildText("fullId");
					Iterator<Element> synonymsit = concept.getChild("synonyms").getChildren("string").iterator();
					while(synonymsit.hasNext()){
						inputtextarea.highlightAll(synonymsit.next().getText(), false, true);
					}
					inputtextarea.highlightAll(prefname, false, true);
			    	
					String localconceptid = concept.getChildText("localConceptId");
					String shortid = localconceptid.substring(localconceptid.indexOf("/")+1,localconceptid.length());
					String ontacronym = "";
					if(shortid.contains(":")){ontacronym = shortid.substring(0,shortid.indexOf(":")).toUpperCase();}
					else{ontacronym = "OPB";}
					String boxname = prefname + " (" + ontacronym + ")";
					if(!boxnames.contains(boxname)){
						boxnames.add(boxname);
						TextMinerCheckBox box = new TextMinerCheckBox(boxname, uri, "");
						box.setName(boxname);
				    	box.setToolTipText(uri);
						box.setSelected(true);
						String bioportalID = "";
						if(ontacronym.equals("CHEBI")){bioportalID = "1007";}
						else if(ontacronym.equals("GO")){bioportalID = "1070";}
						else if(ontacronym.equals("FMA")){bioportalID = "1053";}
						else if(ontacronym.equals("OPB")){bioportalID = "1141";}
						
						System.out.println("Local ont ID: " + concept.getChildText("localOntologyId"));
						TextMinerPanel newpanel = new TextMinerPanel(box, 
								"http://rest.bioontology.org/bioportal/ontologies/download/" + concept.getChildText("localOntologyId") + "?apikey=" + SemSimConstants.BIOPORTAL_API_KEY, bioportalID, uri, shortid);
						resultspanel.add(newpanel);
					}
				}
			}
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		} 
		resultspanel.add(Box.createGlue());		
		loadingbutton.setIcon(SemGenIcon.blankloadingicon);
		spresults.validate();
		spresults.repaint();
	}
	
	public HashMap<String,String[]> processqueryresult(String xml, String ontid){
		HashMap<String,String[]> termsanduris = new HashMap<String,String[]>();
		Set<String> allfoundids = new HashSet<String>();
		SAXBuilder builder = new SAXBuilder();
		try {
			InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
			Document doc = builder.build(is);
			Element root = doc.getRootElement();
			Iterator<Element> sentit = root.getChild("text").getChildren("SENT").iterator();
			while(sentit.hasNext()){
				Iterator<Element> zit = sentit.next().getChild("plain").getChildren(ontid, zns).iterator();
				while(zit.hasNext()){
					Element nextel = zit.next();
					String ids = nextel.getAttributeValue("ids"); //.replace(":", "_");
					StringTokenizer st = new StringTokenizer(ids,",");
					Set<String> idset = new HashSet<String>();
					while(st.hasMoreElements()){
						String oneid = st.nextElement().toString();
						if(oneid.contains(":")){oneid = oneid.substring(oneid.indexOf(":")+1,oneid.length());}
						if(!allfoundids.contains(oneid)){
							idset.add(oneid);
							allfoundids.add(oneid);
						}
					}
					String key = nextel.getText() + " (" + ontid.toUpperCase() + ")";
					if(!termsanduris.keySet().contains(key) && !idset.isEmpty()){
						termsanduris.put(key, idset.toArray(new String[]{}));
					}
				}
			}
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		} 
		return termsanduris;
	}
	
	// Add the ontology terms to the SemSim model
	public Boolean collect(){
		Component[] comps = resultspanel.getComponents();
		for(int c=0;c<comps.length;c++){
			if(comps[c] instanceof TextMinerPanel){
				TextMinerPanel panel = (TextMinerPanel)comps[c];
				if(panel.box.isSelected()){
					String termname = panel.box.getName().substring(0, panel.box.getName().lastIndexOf(" ("));
					if(panel.bioportalID.equals("1141")){ // If OPB
						
					}
					else if(panel.bioportalID.equals("1070")){ // If GO
						int choice = JOptionPane.showOptionDialog(this,
								"GeneOntology contains entities and processes.\nIs " + termname + " a physical entity or process?",
								"Choose classification",
								JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
								null,
								new Object[]{"Entity","Process","Cancel"},
								"Entity");
						if(choice == JOptionPane.YES_OPTION){
							collectedents.add(new ReferencePhysicalEntity(URI.create(panel.box.uri), termname));						}
						else if(choice == JOptionPane.NO_OPTION){
							collectedproc.add(new ReferencePhysicalProcess(URI.create(panel.box.uri), termname));						}
						else if(choice == JOptionPane.CANCEL_OPTION){
							return false;
						}
					}
					else{
						collectedents.add(new ReferencePhysicalEntity(URI.create(panel.box.uri), termname));					}
				}
			}
		}
		return true;
	}
	
	
	public String findpubmedabstract(){
		String abstr = "";
		if(!pubmedid.equals("")){
			inputtextarea.setText("");
			SAXBuilder builder = new SAXBuilder();
			Document doc = new Document();
			try {
				URL url = new URL("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=" + pubmedid + "&rettype=xml&retmode=text");
				System.out.println(url);
				URLConnection yc = url.openConnection();
				yc.setReadTimeout(60000); // Tiemout after a minute
				BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
				doc = builder.build(in);
				in.close();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this,
								"Please make sure you are connected to the web, \notherwise the PubMed service may be experiencing difficulties.",
								"Problem querying PubMed",
								JOptionPane.ERROR_MESSAGE);
				loadingbutton.setIcon(SemGenIcon.blankloadingicon);
				return "";
			} catch (JDOMException e) {
				e.printStackTrace();
			}
			Element root = doc.getRootElement();
			Iterator<Element> artit = root.getChildren("PubmedArticle").iterator();
			Boolean clear = true;
			while(artit.hasNext()){
				Element nextart = artit.next();
				
				if(nextart.getChild("MedlineCitation")!=null){
					Element medcite = nextart.getChild("MedlineCitation");
					if(medcite.getChild("Article")!=null){
						Element art = medcite.getChild("Article");
						if(art.getChild("Abstract")!=null){
							Element abs = art.getChild("Abstract");
							if(!abs.getChildText("AbstractText").equals("")){
								abstr = nextart.getChild("MedlineCitation").getChild("Article").getChild("Abstract").getChild("AbstractText").getText();
								if(clear){
									inputtextarea.setText(""); clear = false;
								}
								inputtextarea.append(abstr + "\n\n");
							}
						}
					}
				}
			}
			loadingbutton.setIcon(SemGenIcon.blankloadingicon);
		}
		return abstr;
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		String value = optionPane.getValue().toString();
		if (value == "Collect"){
			collect(); 
		}
		if (value == "Close"){
			dispose();
		}
		if (value == "Collect & Close"){			
			if(collect()){
				dispose();
			}
		}
		optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
	}

	public void actionPerformed(ActionEvent arg0) {
		Object o = arg0.getSource();
		if(o == parsebutton){
			if(inputtextarea.getText()!=null && !inputtextarea.getText().equals("")){
				GenericThread parsethread = new GenericThread(this, "parse");
				loadingbutton.setIcon(SemGenIcon.loadingicon);
				parsethread.start();
			}
		}
		if(o == pmarea || o == pmbutton){
			pubmedid = pmarea.getText();
			GenericThread pubmedthread = new GenericThread(this, "findpubmedabstract");
			loadingbutton.setIcon(SemGenIcon.loadingicon);
			pubmedthread.start();
		}
	}
	
	public Set<ReferencePhysicalEntity> getCollectedEntities() {
		return collectedents;
	}
	public Set<ReferencePhysicalProcess> getCollectedProcesses() {
		return collectedproc;
	}

}  