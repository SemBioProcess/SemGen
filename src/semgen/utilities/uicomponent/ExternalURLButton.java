package semgen.utilities.uicomponent;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import semgen.SemGen;
import semgen.utilities.BrowserLauncher;
import semgen.utilities.SemGenIcon;
import semsim.annotation.Ontology;
import semsim.definitions.ReferenceOntologies;
import semsim.definitions.ReferenceOntologies.ReferenceOntology;
import semsim.owl.SemSimOWLFactory;

public class ExternalURLButton extends JLabel implements MouseListener{

	private static final long serialVersionUID = 1L;

	public ExternalURLButton(){
		addMouseListener(this);
		setIcon(SemGenIcon.externalURLicon);
		setEnabled(true);
		setToolTipText("Show term details in browser");
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
	}

	public void openTerminBrowser(URI termuri) {
		if(termuri!=null){
			System.out.println(termuri);
			Ontology ont = SemGen.semsimlib.getOntologyfromTermURI(termuri.toString());
			if (ont == ReferenceOntologies.unknown) {
				JOptionPane.showMessageDialog(getParent(), "Sorry, could not determine where to find more information about that resource.");
				return;
			}
			// If an identifiers.org URI is used, just treat the identifier as the URL
			if(termuri.toString().startsWith("http://identifiers.org")) BrowserLauncher.openURL(termuri.toString());

			// ...else, if it's a UNIPROT term...
			else if(ont.getFullName().equals(ReferenceOntology.UNIPROT.getFullName())){
				String id = SemSimOWLFactory.getIRIfragment(termuri.toString());
				String urlstring = "http://www.uniprot.org/uniprot/" + id;
				BrowserLauncher.openURL(urlstring);				
			}
			
			// ...else if we have identified the ontology and it is available through BioPortal, open the BioPortal URL
			else if(!ont.getBioPortalID().isEmpty()){
				// Special case for BRENDA
				String abbrev = ont.getNickName();
				if(abbrev.equals("BRENDA")) abbrev = "BTO";
				String urlstring = "http://bioportal.bioontology.org/ontologies/" + abbrev + "/?p=classes&conceptid=" + SemSimOWLFactory.URIencoding(termuri.toString());
				BrowserLauncher.openURL(urlstring);
			}
			// ...otherwise the knowledge resource is not known or not available online
			else{
				JOptionPane.showMessageDialog(getParent(), "Sorry, could not determine where to find more information about that resource.");
				return;
			}
		}
	}
	
	public void mouseClicked(MouseEvent arg0) {
		
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
