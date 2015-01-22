package semsim.owl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.HashSet;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import semsim.SemSimConstants;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.Submodel;
import semsim.model.physical.object.PhysicalProperty;

import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

public class SemSimOWLFactory {

	public static OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();

	public SemSimOWLFactory() {}

	public static String getXMLbaseFromOntology(OWLOntology ont) {
		String base = ont.getOntologyID().toString();
		base = base.substring(base.indexOf("<") + 1, base.indexOf(">")) + "#";
		return base;
	}

	public static OWLOntology getOntologyIfPreviouslyLoaded(IRI iri,OWLOntologyManager manager) {
		OWLOntologyManager tempmanager = OWLManager.createOWLOntologyManager();
		OWLOntology newont = null;

		try {
			newont = tempmanager.loadOntologyFromOntologyDocument(iri);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}

		OWLOntology returnont = null;
		for (OWLOntology ont : manager.getOntologies()) {
			if (ont.getOntologyID().toString().equals(newont.getOntologyID().toString())) {
				returnont = ont;
				tempmanager.removeOntology(newont);
			}
		}
		return returnont;
	}
	
	// METHODS ON CLASSES

	// Add external class
	public static void addExternalReferenceClass(OWLOntology destinationont,
			String clsuri, String physicaltype, String humreadname, OWLOntologyManager manager) {
		String parentname = SemSimConstants.SEMSIM_NAMESPACE + "Reference_physical_" + physicaltype;
		OWLClass parent = factory.getOWLClass(IRI.create(parentname));
		OWLClass classtoadd = factory.getOWLClass(IRI.create(clsuri));
		OWLAxiom axiom = factory.getOWLSubClassOfAxiom(classtoadd, parent);
		AddAxiom addAxiom = new AddAxiom(destinationont, axiom);
		manager.applyChange(addAxiom);

		OWLDataProperty referstoprop = factory.getOWLDataProperty(IRI.create(SemSimConstants.SEMSIM_NAMESPACE + "refersTo"));
		OWLLiteral con2 = factory.getOWLLiteral(clsuri);
		OWLClassExpression refersto = factory.getOWLDataHasValue(referstoprop,con2);
		OWLSubClassOfAxiom ax2 = factory.getOWLSubClassOfAxiom(classtoadd,refersto);
		AddAxiom addAx2 = new AddAxiom(destinationont, ax2);
		manager.applyChange(addAx2);
		
		setRDFLabel(destinationont, classtoadd, humreadname, manager);
	}
	
	public static void addClass(OWLOntology ont, String uri, String[] parentnames, OWLOntologyManager manager) {
		for (int x = 0; x < parentnames.length; x++) {
			OWLClass parent = factory.getOWLClass(IRI.create(parentnames[x]));
			OWLClass classtoadd = factory.getOWLClass(IRI.create(uri));
			OWLAxiom axiom = factory.getOWLSubClassOfAxiom(classtoadd,parent);
			AddAxiom addAxiom = new AddAxiom(ont, axiom);
			manager.applyChange(addAxiom);
		}
	}

	// Remove an object property value for one class (uses hasValue relation)
		public static void removeClsDataValueRestriction(OWLOntology ont, String clsname, String propname, OWLOntologyManager manager)
				throws OWLException {
			OWLClass cls = factory.getOWLClass(IRI.create(clsname));
			OWLDataPropertyExpression prop = factory.getOWLDataProperty(IRI.create(propname));
			Set<OWLClassExpression> descs = cls.getSuperClasses(ont);
			for (OWLClassExpression desc : descs) {
				if (desc instanceof OWLDataHasValue) {
					OWLDataHasValue allrest = (OWLDataHasValue) desc;
					if (allrest.getProperty().asOWLDataProperty().getIRI().toString().equals(prop.asOWLDataProperty().getIRI().toString())) {
						OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(cls, desc);
						RemoveAxiom removeax = new RemoveAxiom(ont, axiom);
						manager.applyChange(removeax);
					}
				}
			}
		}
		
		
	// METHODS ON INDIVIDUALS
	
	public static void createSemSimIndividual(OWLOntology ont, String indname, OWLClass parent, String suffix, OWLOntologyManager manager){
		OWLIndividual ind = factory.getOWLNamedIndividual(IRI.create(indname + suffix));
		OWLIndividualAxiom axiom = factory.getOWLClassAssertionAxiom(parent, ind);
		if(!ont.getAxioms().contains(axiom)){
			AddAxiom addAxiom = new AddAxiom(ont, axiom);
			manager.applyChange(addAxiom);
		}
	}
	
	
	// Create individuals within one parent class
	public static void createSemSimIndividuals(OWLOntology ont, String[] indnames, String parentclassname, String suffix, OWLOntologyManager manager) throws OWLException {
		OWLClass parent = factory.getOWLClass(IRI.create(parentclassname));
		for (int i = 0; i < indnames.length; i++) {
			createSemSimIndividual(ont, indnames[i], parent, suffix, manager);
		}
	}

	// Create individuals under different parent classes
	public static void createSemSimIndividuals(OWLOntology ont, Hashtable<String,String[]> indandparents, String suffix, OWLOntologyManager manager)
			throws OWLException {

		String[] keyset = new String[indandparents.size()];
		String[] parentname = new String[] {};
		keyset = (String[]) indandparents.keySet().toArray(keyset);
		for (int i = 0; i < keyset.length; i++) {
			parentname = (String[]) indandparents.get(keyset[i]);
			OWLClass parent = factory.getOWLClass(IRI.create(parentname[0]));
			createSemSimIndividual(ont, keyset[i], parent, suffix, manager);
		}
	}

	
	
	public static void subclassIndividual(OWLOntology ont, String ind, String parent, OWLOntologyManager manager){
		OWLClass refclass = factory.getOWLClass(IRI.create(parent));
		OWLIndividual pmcind = factory.getOWLNamedIndividual(IRI.create(ind));
		OWLIndividualAxiom axiom = factory.getOWLClassAssertionAxiom(refclass, pmcind);
		AddAxiom addAxiom = new AddAxiom(ont, axiom);
		manager.applyChange(addAxiom);
	}

	// Set an object property for one individual
	public static void setIndObjectProperty(OWLOntology ont, String subject, String object, String rel, String invrel, OWLOntologyManager manager)
			throws OWLException {

		AddAxiom addAxiom = new AddAxiom(ont, createIndObjectPropertyAxiom(ont, subject, object, rel, null, manager));
		manager.applyChange(addAxiom);
		OWLAxiom invaxiom = createIndObjectPropertyAxiom(ont, object, subject, invrel, null, manager);
		if(!invrel.equals("") && invrel!=null){
			AddAxiom addinvAxiom = new AddAxiom(ont, invaxiom);
			manager.applyChange(addinvAxiom);
		}
	}
	
	public static OWLAxiom createIndObjectPropertyAxiom(OWLOntology ont, String subject,
			String object, String rel, Set<OWLAnnotation> anns, OWLOntologyManager manager)
		throws OWLException {
		OWLIndividual ind = factory.getOWLNamedIndividual(IRI.create(subject));
		OWLIndividual value = factory.getOWLNamedIndividual(IRI.create(object));
		OWLObjectProperty prop = factory.getOWLObjectProperty(IRI.create(rel));
		OWLAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(prop, ind,value);
		if(anns!=null)
			axiom = axiom.getAnnotatedAxiom(anns);
		return axiom;
	}
	
	public static void setIndObjectPropertyWithAnnotations(OWLOntology ont, String subject,
		String object, String rel, String invrel, Set<OWLAnnotation> annsforrel, OWLOntologyManager manager)
		throws OWLException{
		AddAxiom addAxiom = new AddAxiom(ont, createIndObjectPropertyAxiom(ont, subject, object, rel, annsforrel, manager));
		manager.applyChange(addAxiom);
		OWLAxiom invaxiom = createIndObjectPropertyAxiom(ont, object, subject, invrel, null, manager);
		
		if(!invrel.equals("") && invrel!=null){
			AddAxiom addinvAxiom = new AddAxiom(ont, invaxiom);
			manager.applyChange(addinvAxiom);
		}
	}

	public static OWLAxiom createIndDatatypePropertyAxiom(OWLOntology ont, String subject, String rel,
			Object val, Set<OWLAnnotation> anns, OWLOntologyManager manager)
		throws OWLException {
		OWLIndividual ind = factory.getOWLNamedIndividual(IRI.create(subject));
		OWLDataProperty prop = factory.getOWLDataProperty(IRI.create(rel));
		OWLLiteral valueconstant;

		Set<String> rangeset = new HashSet<String>();
		for (OWLDataRange x : prop.getRanges(ont)) {
			rangeset.add(x.toString());
		}
		if (rangeset.contains("xsd:boolean"))
			valueconstant = factory.getOWLLiteral((Boolean) val);
		else valueconstant = factory.getOWLLiteral(val.toString());

		OWLAxiom axiom = factory.getOWLDataPropertyAssertionAxiom(prop, ind, valueconstant);
		
		if(anns!=null)
			axiom = axiom.getAnnotatedAxiom(anns);
		return axiom;
	}

	public static void setIndDatatypeProperty(OWLOntology ont, String induri, String rel, Object val, OWLOntologyManager manager) throws OWLException {
		if(val!=null && !val.equals("")){
			OWLAxiom axiom = createIndDatatypePropertyAxiom(ont, induri, rel, val, null, manager);
			AddAxiom addAxiom = new AddAxiom(ont, axiom);
			manager.applyChange(addAxiom);
		}
	}
	
	public static void setIndDatatypePropertyWithAnnotations(OWLOntology ont, String induri, String rel, Object val,
			Set<OWLAnnotation> anns, OWLOntologyManager manager) throws OWLException{
		AddAxiom addAxiom = new AddAxiom(ont, createIndDatatypePropertyAxiom(ont, induri, rel, val, anns, manager));
		manager.applyChange(addAxiom);
	}

	// Retrieve the datatype property values for one individual (returns a set)
	public static Set<String> getIndDatatypeProperty(OWLOntology ont, String indname, String propname) throws OWLException {
		Set<String> values = new HashSet<String>();
		OWLIndividual ind = factory.getOWLNamedIndividual(IRI.create(indname));
		OWLDataPropertyExpression prop = factory.getOWLDataProperty(IRI.create(propname));
		Map<OWLDataPropertyExpression, Set<OWLLiteral>> dataprops = ind.getDataPropertyValues(ont);
		Set<OWLDataPropertyExpression> datapropskeyset = dataprops.keySet();
		for (OWLDataPropertyExpression expression : datapropskeyset) {
			if (expression.equals(prop)) {
				if (dataprops.get(expression) != null) {
					for (OWLLiteral referstovalue : dataprops.get(expression)) {
						values.add(referstovalue.getLiteral());
					}
				}
			}
		}
		return values;
	}

	// // Retrieve a functional datatype property value for one individuals (returns a single string)
	public static String getFunctionalIndDatatypeProperty(OWLOntology ont, String indname, String propname) throws OWLException {
		// The hashtable keys are individual IRIs, values are relations (as IRIs)
		Set<String> values = SemSimOWLFactory.getIndDatatypeProperty(ont, indname, propname);
		String[] valuearray = values.toArray(new String[] {});
		
		return (valuearray.length == 1)  ? valuearray[0] : "";
	}

	// Retrieve the values of an object property for one individual
	public static Set<String> getIndObjectProperty(OWLOntology ont, String indname, String propname) throws OWLException {
		Set<String> values = new HashSet<String>();
		OWLIndividual ind = factory.getOWLNamedIndividual(IRI.create(indname));
		OWLObjectProperty prop = factory.getOWLObjectProperty(IRI.create(propname));
		Map<OWLObjectPropertyExpression, Set<OWLIndividual>> objprops = ind.getObjectPropertyValues(ont);
		for (OWLObjectPropertyExpression expression : objprops.keySet()) {
			if (expression.equals(prop)) {
				if (objprops.get(expression) != null) {
					for (OWLIndividual referstovalue : objprops.get(expression)) {
						values.add(referstovalue.asOWLNamedIndividual().getIRI().toString());
					}
				}
			}
		}
		return values;
	}
	
	// Retrieve the value of an object property for one individual
	public static String getFunctionalIndObjectProperty(OWLOntology ont, String indname, String propname) throws OWLException {
		String value = "";
		Set<String> values = getIndObjectProperty(ont, indname, propname);
		if(!values.isEmpty()){
			value = values.toArray(new String[]{})[0];
		}
		return value;
	}
	
	
	// Remove a datatype property value for one individual
	public static void removeIndDatatypeProperty(OWLOntology ont, String indname, String propname, OWLOntologyManager manager)
			throws OWLException {
		OWLIndividual ind = factory.getOWLNamedIndividual(IRI.create(indname));
		Set<OWLAxiom> refaxs = ind.asOWLNamedIndividual().getReferencingAxioms(ont);
		for(OWLAxiom refax : refaxs){
			if(refax instanceof OWLDataPropertyAssertionAxiom){
				OWLDataPropertyAssertionAxiom axiom = (OWLDataPropertyAssertionAxiom)refax;
				if(axiom.getProperty().asOWLDataProperty().getIRI().toString().equals(propname)){
					manager.applyChange(new RemoveAxiom(ont, axiom));
				}
			}
		}
	}
	

	public static Set<String> getIndividualsAsStrings(OWLOntology ont, String parentclass) {
		Set<String> indstrings = new HashSet<String>();
		Set<OWLIndividual> existinginds = factory.getOWLClass(IRI.create(parentclass)).getIndividuals(ont);
		for (OWLIndividual ind : existinginds) {
			indstrings.add(ind.asOWLNamedIndividual().getIRI().toString());
		}
		return indstrings;
	}
	
	public static Set<String> getIndividualsInTreeAsStrings(OWLOntology ont, String parentclass) throws OWLException {
		Set<String> indstrings = new HashSet<String>();
		for(String cls : SemSimOWLFactory.getAllSubclasses(ont, parentclass, true)){
			Set<OWLIndividual> existinginds = factory.getOWLClass(IRI.create(cls)).getIndividuals(ont);
			for (OWLIndividual ind : existinginds) {
				indstrings.add(ind.asOWLNamedIndividual().getIRI().toString());
			}
		}
		return indstrings;
	}
	
	// Check whether an individual with a given IRI is in an ontology
	public static boolean indExistsInClass(String indtocheck, String parent, OWLOntology ontology) throws OWLException {
		OWLClass parentclass = factory.getOWLClass(IRI.create(parent));
		Set<OWLIndividual> inds = parentclass.getIndividuals(ontology);
		for (OWLIndividual ind : inds) {
			if (ind.asOWLNamedIndividual().getIRI().toString().equals(indtocheck)) {
				return true;
			}
		}
		return false;
	}

	// Check whether an individual with a given IRI is in an ontology
	public static boolean indExistsInTree(String indtocheck, String parent, OWLOntology ontology) throws OWLException {
		Set<String> allsubclasses = SemSimOWLFactory.getAllSubclasses(ontology, parent, true);
		for (String oneclass : allsubclasses) {
			OWLClass oneowlclass = factory.getOWLClass(IRI.create(oneclass));
			Set<OWLIndividual> inds = oneowlclass.getIndividuals(ontology);
			for (OWLIndividual ind : inds) {
				if (ind.asOWLNamedIndividual().getIRI().toString().equals(indtocheck)) {
					return true;
				}
			}
		}
		return false;
	}

	public static Hashtable<String,String> getReferencedIRIs(OWLOntology ontology,String parent) throws OWLException {
		Hashtable<String,String> namesanduris = new Hashtable<String,String>();
		Set<String> leafclasses = SemSimOWLFactory.getAllSubclasses(ontology, parent, true);
		for (String oneclass : leafclasses) {
			OWLClass oneowlclass = factory.getOWLClass(IRI.create(oneclass));
			for (OWLIndividual ind : oneowlclass.getIndividuals(ontology)) {
				String uri = getFunctionalIndDatatypeProperty(ontology, ind.asOWLNamedIndividual().getIRI().toString(), SemSimConstants.SEMSIM_NAMESPACE + "refersTo");
				String humread = getFunctionalIndDatatypeProperty(ontology, ind.asOWLNamedIndividual().getIRI().toString(), SemSimConstants.SEMSIM_NAMESPACE + "humanReadableName");
				// If the refersTo slot is empty, assume it's a custom entity
				// unique to this SemSim model
				if (uri.equals("")) {
					uri = ind.asOWLNamedIndividual().getIRI().toString();
				}
				namesanduris.put(humread, uri);
			}
		}
		return namesanduris;
	}
	
	public static String[] getRDFLabels(OWLOntology ont, OWLEntity ent) {
		OWLLiteral val = null;
		OWLAnnotationProperty label = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		OWLAnnotation[] annarray = ent.getAnnotations(ont, label).toArray(new OWLAnnotation[] {});
		if (annarray.length == 0) {
			return new String[] { "" };
		} else {
			String[] labeltexts = new String[annarray.length];
			for (int x = 0; x < annarray.length; x++) {
				val = (OWLLiteral) annarray[x].getValue();
				labeltexts[x] = val.getLiteral();
			}
			return labeltexts;
		}
	}

	public static void setRDFLabel(OWLOntology ontology, OWLNamedIndividual annind, String value, OWLOntologyManager manager) {
		if(value!=null && !value.equals("")){
			OWLAnnotationProperty label = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
			Set<OWLAnnotation> anns = annind.getAnnotations(ontology, label);
			for (OWLAnnotation ann : anns) {
				OWLAnnotationSubject annsub = annind.getIRI();
				OWLAxiom removeax = factory.getOWLAnnotationAssertionAxiom(annsub,ann);
				manager.applyChange(new RemoveAxiom(ontology, removeax));
			}
			OWLAnnotation newann = factory.getOWLAnnotation(
					factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()),
					factory.getOWLLiteral(value, "en"));
			OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(annind.getIRI(),newann);
			manager.applyChange(new AddAxiom(ontology, ax));
		}
	}

	public static void setRDFLabel(OWLOntology ontology, OWLClass ent,
			String value, OWLOntologyManager manager) {
		if(value!=null && !value.equals("")){
			OWLAnnotationProperty label = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
			Set<OWLAnnotation> anns = ent.getAnnotations(ontology, label);
			for (OWLAnnotation ann : anns) {
				OWLAnnotationSubject annsub = ent.getIRI();
				OWLAxiom removeax = factory.getOWLAnnotationAssertionAxiom(annsub,ann);
				manager.applyChange(new RemoveAxiom(ontology, removeax));
			}
			OWLAnnotation newann = factory.getOWLAnnotation(
					factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), factory.getOWLLiteral(value, "en"));
			OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(ent.getIRI(),newann);
			manager.applyChange(new AddAxiom(ontology, ax));
		}
	}
	
	// This removes all existing RDF comments and creates a new comment using "value"
	public static void setRDFComment(OWLOntology ontology, OWLEntity ent, String value, OWLOntologyManager manager) {
		if(value!=null && !value.equals("")){
			OWLAnnotationProperty label = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI());
			Set<OWLAnnotation> anns = ent.getAnnotations(ontology, label);
			for (OWLAnnotation ann : anns) {
				OWLAnnotationSubject annsub = ent.getIRI();
				OWLAxiom removeax = factory.getOWLAnnotationAssertionAxiom(annsub,ann);
				manager.applyChange(new RemoveAxiom(ontology, removeax));
			}
			OWLAnnotation newann = factory.getOWLAnnotation(
					factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()), factory.getOWLLiteral(value, "en"));
			OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(ent.getIRI(),newann);
			manager.applyChange(new AddAxiom(ontology, ax));
		}
	}

	public static void replaceRDFcomment(String oldannotation, String newannotation, String uri, OWLOntology ontology, 
		OWLOntologyManager manager) {
		try {
			OWLAnnotation targetann = null;
			OWLNamedIndividual _real = factory.getOWLNamedIndividual(IRI.create(uri));
			// Go through both the real and property individuals, change their
			// comment annotation
			OWLAnnotationProperty comment = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI());
			for (OWLAnnotation ann : _real.getAnnotations(ontology,comment)) {
				if (ann.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) ann.getValue();
					if (val.getLiteral().equals(oldannotation)) {
						targetann = ann;
					}
				}
			}

			// If the individual was previously annotated, remove it so the new
			// annotation can replace it
			if (targetann != null) {
				OWLAxiom removeax = factory.getOWLAnnotationAssertionAxiom(_real.getIRI(), targetann);
				manager.applyChange(new RemoveAxiom(ontology,removeax));
			}
			// Add the new annotation
			if (!newannotation.equals("")) {
				OWLAnnotation commentAnno = factory.getOWLAnnotation(
						factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()),
						factory.getOWLLiteral(newannotation, "en"));

				OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(_real.asOWLNamedIndividual().getIRI(), commentAnno);
				manager.applyChange(new AddAxiom(ontology, ax));
			}
		} catch (OWLOntologyChangeException r) {
			System.err.println("Could not edit ontology: " + r.getMessage());
		}
	}
	
	
	public static void addOntologyAnnotation(OWLOntology ont, String property, OWLEntity ent, OWLOntologyManager manager){
		OWLAnnotationValue entiri = ent.getIRI();
		OWLAnnotation anno = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(IRI.create(property)), entiri);
		manager.applyChange(new AddOntologyAnnotation(ont, anno));
	}
	
	public static void addOntologyAnnotation(OWLOntology ont, String property, String val, String lang, OWLOntologyManager manager){
		OWLLiteral lit = factory.getOWLLiteral(val,lang);
		OWLAnnotation anno = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(IRI.create(property)), lit);
		manager.applyChange(new AddOntologyAnnotation(ont, anno));
	}

	public static void addOntologyAnnotation(OWLOntology ont, IRI property, String val, OWLOntologyManager manager){
		OWLLiteral lit = factory.getOWLLiteral(val,"en");
		OWLAnnotation anno = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(property), lit);
		manager.applyChange(new AddOntologyAnnotation(ont, anno));
	}
	
	public static Set<String> getAllSubclasses(OWLOntology ont, String parent, Boolean includeparent) throws OWLException{
		// traverse all nodes that belong to the parent
		Set<String> nodes  = new HashSet<String>();
		if(includeparent) nodes.add(parent);
		OWLClass parentclass = factory.getOWLClass(IRI.create(parent));
		Set<OWLClassExpression> set = parentclass.getSubClasses(ont);
	    for(OWLClassExpression node : set){
		    // store node information
	    	String nodestring = node.asOWLClass().getIRI().toString();
		    nodes.add(nodestring);
		    // traverse children recursively
		    nodes.addAll(getAllSubclasses(ont, nodestring, false));
	    }
	    return nodes;
	}
		
	// Change this to return all the datastructures associated with the nested components
	public static Set<DataStructure> getCodewordsAssociatedWithNestedSubmodels(Submodel sub) {
		Set<Submodel> associatedcomponents = traverseNestedComponentTreeForDataStructures(sub);
		Set<DataStructure> newcdwds = new HashSet<DataStructure>();
		for(Submodel submod : associatedcomponents){
			newcdwds.addAll(submod.getAssociatedDataStructures());
		}
		return newcdwds;
	}
	
	
	public static Set<Submodel> traverseNestedComponentTreeForDataStructures(Submodel sub) {
	    // traverse all nodes that belong to the parent
		Set<Submodel> nodes  = new HashSet<Submodel>();
		Set<Submodel> set = sub.getSubmodels();
	    for(Submodel node : set){
		    // store node information
		    nodes.add(node);
		    // traverse children recursively
		    traverseNestedComponentTreeForDataStructures(node);
	    }
	    return nodes;
	}

	public static String getIRIfragment(String uri) {
		String result = uri;
		if (!uri.equals("")) {
			if (uri.contains("#")) 
				result = uri.substring(uri.lastIndexOf("#") + 1, uri.length());
			else if (uri.contains("/"))
				result = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
			else if (uri.startsWith("http://identifiers.org"))
				result = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
			else if (uri.startsWith("urn:miriam:"))
				result = uri.substring(uri.lastIndexOf(":") + 1, uri.length());
		}
		return result;
	}
	
	public static String getURIdecodedFragmentFromIRI(String uri){
		return URIdecoding(getIRIfragment(uri));
	}
	
	public static String getNamespaceFromIRI(String uri) {
		if (uri.contains("#")) {
			return uri.substring(0, uri.indexOf("#") + 1);
		} 
		if (uri.startsWith("http://purl.obolibrary.org/obo/")) { // To deal with CHEBI and Mouse Adult Gross Anatomy Ontology
			return uri.substring(0, uri.lastIndexOf("_"));
		} 
		if(uri.startsWith("urn:miriam:")){
			return uri.substring(0, uri.lastIndexOf(":") + 1);
		}
		return uri.substring(0, uri.lastIndexOf("/") + 1);
	}

	// Replac special characters that shouldn't be used in an IRI
	public static String URIencoding(String word) {
		String result = word;
		result = result.replace(" ", "_");
		result = result.replace("--","-");
		
		try {
			result = URLEncoder.encode(result, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		result = result.replace("*", "%2A");
		return result;
	}
	
	public static String URIdecoding(String uri) {
		String result = uri;
		try {
			result = URLDecoder.decode(result, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		result = result.replace("%2A", "*");
		return result;
	}
	
	public static URI getURIforPhysicalProperty(SemSimModel model, PhysicalProperty prop){
		return URI.create(model.getNamespace() + URIencoding(prop.getAssociatedDataStructure().getName()) + "_property");
	}
	
	public static String[] getRDFComments(OWLOntology ont, String indiri) {
		Set<String> commentset = new HashSet<String>();
		OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI.create(indiri));
		OWLLiteral val = null;
		OWLAnnotationProperty comment = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI());
		Set<OWLAnnotation> anns = ind.getAnnotations(ont, comment);
		for (OWLAnnotation annotation : anns) {
			val = (OWLLiteral) annotation.getValue();
			commentset.add(val.getLiteral());
		}
		if(commentset.size()>0) return commentset.toArray(new String[] {});
		return null;
	}

	public static String getRDFcomment(OWLOntology ont, OWLIndividual ind) {
		String commentstring = "";
		OWLAnnotationProperty comment = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI());

		if (ind.asOWLNamedIndividual().getAnnotations(ont, comment).size() <= 1) {
			for (OWLAnnotation annotation : ind.asOWLNamedIndividual().getAnnotations(ont, comment)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					if (val.getLiteral() != null) commentstring = val.getLiteral();
				}
			}
		} else {
			System.err.println("ERROR: Multiple comments for " + ind.toString());
		}
		return commentstring;
	}
	
	/**
	 * @deprecated
	 */
	public static String getLatestVersionIDForBioPortalOntology(String bioportalID){
		SAXBuilder builder = new SAXBuilder();
		Document doc = new Document();
		String versionid = null;
		try {
			URL url = new URL(
					"http://rest.bioontology.org/bioportal/virtual/ontology/" + bioportalID + "?apikey=" + SemSimConstants.BIOPORTAL_API_KEY);
			URLConnection yc = url.openConnection();
			yc.setReadTimeout(60000); // Tiemout after a minute
			BufferedReader in = new BufferedReader(new InputStreamReader(
					yc.getInputStream()));
			doc = builder.build(in);
			in.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Returned error: " + e.getLocalizedMessage()+ "\n\nPlease make sure you are online,\notherwise BioPortal may be experiencing problems",
					"Problem searching BioPortal",JOptionPane.ERROR_MESSAGE);
			return null;
		} catch (JDOMException e) {e.printStackTrace();}

		if (doc.hasRootElement()) {
			versionid = doc.getRootElement().getChild("data").getChild("ontologyBean").getChildText("id");
		}
		return versionid;
	}
	
	public static String generateUniqueIRIwithNumber(String iritoappend, Set<String> existingmembers) {
		int x = 0;
		iritoappend = iritoappend + x;
		if(existingmembers.contains(iritoappend)){
			Boolean findnew = true;
			while (findnew) {
				int lx = Integer.toString(x).length();
				x++;
				iritoappend = iritoappend.substring(0,iritoappend.length()-lx) + x;
				findnew = existingmembers.contains(iritoappend);
			}
		}
		return iritoappend;
	}

}