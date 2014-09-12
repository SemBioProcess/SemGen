package semsim.writing;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class SemSimRDFwriter {

	public static void write(OWLOntology ont, File saveloc) throws OWLOntologyStorageException{
//		RDFXMLOntologyFormat format = new RDFXMLOntologyFormat();
		RDFOntologyFormat format = new RDFXMLOntologyFormat();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        manager.saveOntology(ont, format, IRI.create(saveloc));
	}
	
}
