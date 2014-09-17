package semgen.resource.uicomponents;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Dialog.ModalityType;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import semgen.SemGen;
import semgen.resource.GenericThread;
import semgen.resource.SemGenIcon;
import semgen.resource.SemGenFont;
import semsim.SemSimConstants;
import semsim.owl.CustomRestrictionVisitor;
import semsim.owl.SemSimOWLFactory;
import semsim.webservices.BioPortalSearcher;

public class MoreInfoButton extends JLabel implements PropertyChangeListener, MouseListener{
	private static final long serialVersionUID = 1701314749125646788L;
	public JDialog moreinfodialog;
	public JOptionPane optionPane;
	public String onturi = "";
	public String termuri;
	public String bioportalID = "";
	public String shortid = "";
	
	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	private OWLDataFactory factory;
	
	public MoreInfoButton(String termuri){
		this.termuri = termuri;
		this.activate();
	}
	public MoreInfoButton(String onturi, String termuri){
		this.onturi = onturi;
		this.termuri = termuri;
		this.bioportalID = BioPortalSearcher.getBioPortalIDfromTermURI(termuri);
		this.activate();
	}
	public MoreInfoButton(String onturi, String termuri, String bioportalID){
		this.onturi = onturi;
		this.termuri = termuri;
		this.bioportalID = bioportalID;
		this.activate();
	}
	public MoreInfoButton(String onturi, String termuri, String bioportalID, String shortid){
		this.onturi = onturi;
		this.termuri = termuri;
		this.bioportalID = bioportalID;
		this.shortid = shortid;
		this.activate();
	}

	public void getMoreInfo(){
		if(this.getIcon()!=SemGenIcon.loadingiconsmall){
			setIcon(SemGenIcon.loadingiconsmall);
			goOnAndGetIt(onturi,termuri,bioportalID,shortid);
		}
	}
	
	public void getMoreInfo(String onturi1, String termuri1, String bioportalID1, String shortid1){
		if(this.getIcon()!=SemGenIcon.loadingiconsmall){
			setIcon(SemGenIcon.loadingiconsmall);
			goOnAndGetIt(onturi1,termuri1,bioportalID1,shortid1);
		}
	}
	
	public void goOnAndGetIt(String onturi, String termuri, String bioportalID, String shortid){
		// If the term can't be found on bioportal
		System.out.println("term: " + termuri);
		if(termuri == null){
			setIcon(SemGenIcon.moreinfoicon);
			return;
		}

		System.out.println("namespace: " + SemSimOWLFactory.getNamespaceFromIRI(termuri));
		bioportalID = BioPortalSearcher.getBioPortalIDfromTermURI(termuri);
		if(bioportalID == null){
			JOptionPane.showMessageDialog(null, "Cannot get info on this term.\nThe source ontology is not available through BioPortal");
			reset();
			return;
		}
		// otherwise...
		String versionid = SemSimOWLFactory.getLatestVersionIDForBioPortalOntology(bioportalID);
		if(versionid==null){
			System.err.println("Could not get latest version of ontology from BioPortal");
			reset();
			return;
		}
		// make sure we have an ontology uri (needed for vSPARQL stuff)
		if(onturi.equals("")){
			onturi = 
				"http://rest.bioontology.org/bioportal/ontologies/download/" + versionid + "?apikey=" + SemSimConstants.BIOPORTAL_API_KEY;
		}
		// make sure we have a shortid for searching bioportal
		if(shortid.equals("")){
			
			shortid = SemSimOWLFactory.getIRIfragment(termuri);
			String ontabbrev = SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.get(SemSimOWLFactory.getNamespaceFromIRI(termuri));
			if(!shortid.toLowerCase().contains(ontabbrev.toLowerCase())){
				if(ontabbrev.equals("FMA")){
					shortid = ontabbrev.toLowerCase() + ":" + shortid;
				}
			}
		}

		Hashtable<String, Set<String>> propsandvals = new Hashtable<String, Set<String>>();
		JTextArea area = new JTextArea();
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		area.setLineWrap(true);
		area.setText(null);
		String termlabel = "";
		

		if (bioportalID!=null && !bioportalID.equals("")) {
			// Get the selected ontology's latest version id
			SAXBuilder builder = new SAXBuilder();
			Document doc = new Document();
			try {
				URL url = new URL(
						"http://rest.bioontology.org/bioportal/concepts/" + versionid + "?conceptid=" + shortid + "&apikey=" + SemSimConstants.BIOPORTAL_API_KEY);
				System.out.println(url.toString());
				URLConnection yc = url.openConnection();
				yc.setReadTimeout(60000); // Tiemout after a minute
				BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
				doc = builder.build(in);
				in.close();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null,"There was a problem searching BioPortal with REST url\n" +
						e.getLocalizedMessage(),"Error",JOptionPane.ERROR_MESSAGE);
				this.reset();
				return;
			} catch (JDOMException e) {
				e.printStackTrace();
			}

			// Process XML results from BioPortal REST service
			if (doc.hasRootElement()) {
				termlabel = doc.getRootElement().getChild("data").getChild("classBean").getChildText("label");
				List<Element> deflist = doc.getRootElement().getChild("data").getChild("classBean").getChildren("definitions");
				List<Element> resultlist = doc.getRootElement().getChild("data").getChild("classBean").getChild("relations").getChildren("entry");
				if (resultlist.isEmpty() && deflist.isEmpty()) {
					JOptionPane.showMessageDialog(this, "No additional data associated with concept");
				} else {
					// Get definition of term, if there is one
					Iterator<Element> defiterator = deflist.iterator();
					while (defiterator.hasNext()) {
						Element nextel = defiterator.next();
						String property = "Definition";
						String def = nextel.getChildText("string");
						if (propsandvals.get(property) != null) {
							propsandvals.get(property).add(def);
						} else {
							Set<String> set = new HashSet<String>();
							set.add(def);
							propsandvals.put(property, set);
						}
					}
					// Get the other properties associated with the term
					Iterator<Element> resultsiterator = resultlist.iterator();
					while (resultsiterator.hasNext()) {
						String property = "";
						Element nextel = resultsiterator.next();
						property = nextel.getChildText("string");
						if (nextel.getChild("list") != null) {
							// get relations with other concepts (object
							// properties)
							if (nextel.getChild("list").getChildren("classBean") != null) {
								List<Element> vallist = nextel.getChild("list").getChildren("classBean");
								Iterator<Element> beaniterator = vallist.iterator();
								while (beaniterator.hasNext()) {
									Element nextbean = beaniterator.next();
									if (propsandvals.get(property) != null) {
										propsandvals.get(property).add(nextbean.getChildText("label"));
									} else {
										Set<String> set = new HashSet<String>();
										set.add(nextbean.getChildText("label"));
										propsandvals.put(property, set);
									}
								}
							}
							// Get string datatype values
							if (nextel.getChild("list").getChildren("string") != null) {
								List<Element> stringvallist = nextel.getChild("list").getChildren("string");
								Iterator<Element> stringiterator = stringvallist.iterator();
								while (stringiterator.hasNext()) {
									Element nextstring = stringiterator.next();
									if (propsandvals.get(property) != null) {
										propsandvals.get(property).add(
												nextstring.getText());
									} else {
										Set<String> set = new HashSet<String>();
										set.add(nextstring.getText());
										propsandvals.put(property, set);
									}
								}
							}
						}

						// get INT datatype values
						else if (nextel.getChild("int") != null) {
							List<Element> intlist = nextel.getChildren("int");
							Iterator<Element> intiterator = intlist.iterator();
							while (intiterator.hasNext()) {
								Element nextint = intiterator.next();
								if (propsandvals.get(property) != null) {
									propsandvals.get(property).add(
											nextint.getText());
								} else {
									Set<String> set = new HashSet<String>();
									set.add(nextint.getText());
									propsandvals.put(property, set);
								}
							}
						}
					}
				}
			}
		}
		// Otherwise we're using a local file and we need to access it with the OWL API (sigh)
		else {
			OWLOntology localont = null;
			try {
				localont = SemSimOWLFactory.getOntologyIfPreviouslyLoaded(IRI.create(onturi), manager);
				if (localont == null) {
					localont = manager.loadOntologyFromOntologyDocument(IRI.create(onturi));
				}
			} catch (OWLOntologyCreationException e) {
				JOptionPane.showMessageDialog(this,"Could not load the local ontology");
				SemGen.logfilewriter.println(e.toString());
				this.reset();
			}
			OWLClass selectedclass = factory.getOWLClass(IRI.create(termuri));
			CustomRestrictionVisitor rv = new CustomRestrictionVisitor(Collections.singleton(localont));

			for (OWLSubClassOfAxiom ax : localont.getSubClassAxiomsForSubClass(selectedclass)) {
				OWLClassExpression superCls = ax.getSuperClass();
				superCls.accept(rv);
			}
			Set<OWLObjectPropertyExpression> owlobjectprops = rv.getRestrictedObjectProperties(); 
			Set<OWLDataPropertyExpression> owldataprops = rv.getRestrictedDataProperties();

			for (OWLObjectPropertyExpression objectprop : owlobjectprops) {
				Set<String> objectvals = new HashSet<String>(); 
																
				Set<String> objectvals2 = new HashSet<String>(); 
				// For now we only get some, value and cardinality restrictions
				if (rv.restrictedSomeObjectPropertiesTable.keySet().contains(objectprop)) {
					objectvals.addAll(rv.getSomeObjectProperty(objectprop));
					objectvals2.addAll(rv.getSomeObjectProperty(objectprop));
				}
				if (rv.restrictedValueObjectPropertiesTable.keySet().contains(objectprop)) {
					objectvals.add(rv.getValueObjectProperty(objectprop).getLiteral());
					objectvals2.add(rv.getValueObjectProperty(objectprop).getLiteral());
				}
				if (rv.restrictedExactCardinalityObjectPropertiesTable.keySet().contains(objectprop)) {
					objectvals.add(rv.getCardinalityObjectProperty(objectprop));
					objectvals2.add(rv.getCardinalityObjectProperty(objectprop));
				}
				Set<String> labels = new HashSet<String>();
				// use the rdf labels, if they exist
				for (String objectval : objectvals) {
					String label = SemSimOWLFactory.getRDFLabels(localont,factory.getOWLClass(IRI.create(objectval)))[0];
					if (!label.equals("")) {
						labels.add(label);
						objectvals2.remove(objectval);
					}
				}
				Set<String> finalvalset = new HashSet<String>();
				finalvalset.addAll(objectvals2);
				finalvalset.addAll(labels);
				String propname = SemSimOWLFactory.getIRIfragment(objectprop.asOWLObjectProperty().getIRI().toString());
				if (SemSimOWLFactory.getRDFLabels(localont,objectprop.asOWLObjectProperty()).length != 0) {
					propname = SemSimOWLFactory.getRDFLabels(localont,objectprop.asOWLObjectProperty())[0];
				}
				propsandvals.put(propname, finalvalset);
			}

			for (OWLDataPropertyExpression dataprop : owldataprops) {
				Set<String> valset = new HashSet<String>();
				valset.add(rv.getValueForDataProperty(dataprop).getLiteral().toString());
				propsandvals.put(dataprop.asOWLDataProperty().getIRI().getFragment().toString(), valset);
			}
		}

		if (!propsandvals.isEmpty()) {
			// process all results for display here
			for (String prop : propsandvals.keySet()) {
				for (String val : propsandvals.get(prop)) {
					JPanel subpanel = new JPanel();
					JLabel proplabel = new JLabel(prop + ": ");
					proplabel.setFont(SemGenFont.defaultBold());
					JTextArea vallabel = new JTextArea();
					vallabel.setEditable(false);
					vallabel.setText(val);
					vallabel.setCaretPosition(0);
					vallabel.setLineWrap(true);
					vallabel.setWrapStyleWord(true);
					SemGenScrollPane pane = new SemGenScrollPane(vallabel);
					pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
					pane.setPreferredSize(new Dimension(350, 50));
					subpanel.add(proplabel);
					subpanel.add(pane);
					panel.add(subpanel);
					panel.add(new JSeparator());
				}
			}

			// show the results of the query
			area.setCaretPosition(0);
			JScrollPane scroller = new JScrollPane(panel);
			scroller.getHorizontalScrollBar().setUnitIncrement(12);
			scroller.getVerticalScrollBar().setUnitIncrement(12);

			moreinfodialog = new JDialog();
			moreinfodialog.setPreferredSize(new Dimension(700, 700));
			optionPane = new JOptionPane(scroller, JOptionPane.PLAIN_MESSAGE,
					JOptionPane.OK_OPTION, null);

			optionPane.addPropertyChangeListener(this);
			Object[] options = new Object[] { "Close" };
			optionPane.setOptions(options);
			optionPane.setInitialValue(options[0]);
			moreinfodialog.setContentPane(optionPane);
			moreinfodialog.setModalityType(ModalityType.APPLICATION_MODAL);
			moreinfodialog.pack();
			moreinfodialog.setLocationRelativeTo(getParent());
			moreinfodialog.setTitle("Properties of " + termlabel + " (" + termuri + ")");
			moreinfodialog.setResizable(true);
			moreinfodialog.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(null, "No data or object properties associated with term");
		}
		reset();
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		String propertyfired = arg0.getPropertyName();
		if (propertyfired.equals("value")) {
			String value = optionPane.getValue().toString();
			if (value.equals("Close")) {
				moreinfodialog.dispose();
			}
		}
	}
	
	public void activate(){
		reset();
		setToolTipText("Get more info about term");
		setOpaque(false);
		addMouseListener(this);
		setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
	}
	
	public void reset(){
		this.setText("");
		this.setIcon(SemGenIcon.moreinfoicon);
		this.setEnabled(true);
	}
		
	public void mouseClicked(MouseEvent arg0) {
		if(arg0.getSource()==this){
			GenericThread querythread = new GenericThread(this, "getMoreInfo");
			querythread.start();
		}
	}
	public void mouseEntered(MouseEvent arg0) {
		setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	public void mouseExited(MouseEvent arg0) {
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
	public void mousePressed(MouseEvent arg0) {
		setBorder(BorderFactory.createLineBorder(Color.blue,1));
	}
	public void mouseReleased(MouseEvent arg0) {
		setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
	}
}
