package semsim.owl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import semsim.SemSimConstants;
import semsim.model.SemSimModel;
import semsim.model.computational.DataStructure;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.ReferencePhysicalEntity;
import semsim.model.physical.ReferencePhysicalProcess;
import semsim.model.physical.PhysicalProperty;
import semsim.model.physical.Submodel;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
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
	
	public static OWLOntology getOntologyIfPreviouslyLoaded(OWLOntology testont, OWLOntologyManager manager) {
		OWLOntologyManager tempmanager = OWLManager.createOWLOntologyManager();
		OWLOntology newont = testont;

		OWLOntology returnont = null;
		for (OWLOntology ont : manager.getOntologies()) {
			if (ont.getOntologyID().toString().equals(newont.getOntologyID().toString())) {
				returnont = ont;
				tempmanager.removeOntology(newont);
			}
		}
		return returnont;
	}
	
	public static OWLOntology setUniqueSemSimOntologyName(String dateID, File tempfile, OWLOntology ont, OWLOntologyManager manager)
			throws IOException {
		
		String ID = dateID;
		File tempdir = new File(System.getProperty("java.io.tmpdir"));
		File tempout = new File(tempdir.getAbsolutePath() + "/" + "tempout.owl");
		PrintWriter writer = new PrintWriter(new FileWriter(tempout));
		Scanner scanner = new Scanner(tempfile);

		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			
			// Replace ontology name
			if (line.contains("<owl:Ontology rdf:about=\"")) {
				if(ont.getAnnotations().isEmpty()){
					line = "<owl:Ontology rdf:about=\"http://www.bhi.washington.edu/SemSim/" + ID + ".owl\"/>";
				}
				else{
					line = "<owl:Ontology rdf:about=\"http://www.bhi.washington.edu/SemSim/" + ID + ".owl\">";
				}
			}
			// Replace base
			else if (line.contains("xml:base=\"")) {
				line = "     xml:base=\"http://www.bhi.washington.edu/SemSim/" + ID + ".owl\"";
			}
			// MIGHT ALSO NEED TO REPLACE xmlns= (for merged ontologies)
			writer.println(line);
		}
		writer.close();
		scanner.close();
		manager.removeOntology(ont);
		PrintWriter writer2 = new PrintWriter(new FileWriter(tempfile));
		Scanner scanner2 = new Scanner(tempout);
		while (scanner2.hasNext()) {
			String line = scanner2.nextLine();
			writer2.println(line);
		}
		writer2.close();
		scanner2.close();
		try {
			ont = manager.loadOntologyFromOntologyDocument(tempfile);
		} catch (OWLException e) {
			e.printStackTrace();
		}
		return ont;
	}

	// METHODS ON CLASSES

	public static void addExternalReferenceClasses(OWLOntology destinationont,
			Hashtable<String,String> classesandsourceonturis, String physicaltype,
			String clssuffix, Boolean useuriforname, OWLOntologyManager manager) {
		String[] keyset = new String[classesandsourceonturis.size()];
		String parentname = SemSimConstants.SEMSIM_NAMESPACE + "Reference_physical_" + physicaltype;
		keyset = (String[]) classesandsourceonturis.keySet().toArray(keyset);
		for (int i = 0; i < keyset.length; i++) {

			OWLClass parent = factory.getOWLClass(IRI.create(parentname));
			OWLClass classtoadd = factory.getOWLClass(IRI.create(keyset[i] + clssuffix));
			OWLAxiom axiom = factory.getOWLSubClassOfAxiom(classtoadd, parent);
			AddAxiom addAxiom = new AddAxiom(destinationont, axiom);
			manager.applyChange(addAxiom);

			OWLDataProperty referstoprop = factory.getOWLDataProperty(IRI.create(SemSimConstants.SEMSIM_NAMESPACE + "refersTo"));

			OWLLiteral con2 = factory.getOWLLiteral(keyset[i] + clssuffix);

			OWLClassExpression refersto = factory.getOWLDataHasValue(referstoprop, con2);

			OWLSubClassOfAxiom ax2 = factory.getOWLSubClassOfAxiom(classtoadd,refersto);

			AddAxiom addAx2 = new AddAxiom(destinationont, ax2);

			manager.applyChange(addAx2);

			setRDFLabel(destinationont, classtoadd,
					getIRIfragment(keyset[i]), manager);
		}
	}

	// ADD EXTERNAL CLASS
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

	public static void addClass(OWLOntology ont, Hashtable<String,String[]> urisandparenturis, OWLOntologyManager manager) {
		String[] keyset = new String[urisandparenturis.size()];

		keyset = (String[]) urisandparenturis.keySet().toArray(keyset);
		for (int i = 0; i < keyset.length; i++) {
			String[] parentnames = (String[]) urisandparenturis.get(keyset[i]);
			addClass(ont, keyset[i], parentnames, manager);
		}
	}

	public static Set<String> getClsValueObjectProperty(OWLOntology ont, String clsuri, String propuri) {
		Set<String> valset = new HashSet<String>();
		OWLClass theclass = factory.getOWLClass(IRI.create(clsuri));
		CustomRestrictionVisitor visitor = new CustomRestrictionVisitor(Collections.singleton(ont));

		for (OWLSubClassOfAxiom ax : ont.getSubClassAxiomsForSubClass(theclass)) {
			OWLClassExpression superCls = ax.getSuperClass();
			superCls.accept(visitor);
		}

		for (OWLObjectPropertyExpression prop : visitor.getRestrictedObjectProperties()) {
			OWLObjectProperty property = prop.asOWLObjectProperty();
			if (property.getIRI().toString().equals(propuri)) {
				valset.add(visitor.getValueObjectProperty(prop).getLiteral());
			}
		}
		return valset;
	}

	public static Set<String> getClsSomeObjectProperty(OWLOntology ont,
			String clsuri, String propuri) {
		Set<String> valset = new HashSet<String>();
		OWLClass theclass = factory.getOWLClass(IRI.create(clsuri));
		CustomRestrictionVisitor visitor = new CustomRestrictionVisitor(
				Collections.singleton(ont));

		for (OWLSubClassOfAxiom ax : ont.getSubClassAxiomsForSubClass(theclass)) {
			OWLClassExpression superCls = ax.getSuperClass();
			superCls.accept(visitor);
		}

		for (OWLObjectPropertyExpression prop : visitor.getRestrictedObjectProperties()) {
			OWLObjectProperty property = prop.asOWLObjectProperty();
			if (property.getIRI().toString().equals(propuri)) {
				valset = visitor.getSomeObjectProperty(prop);
			}
		}
		return valset;
	}
	
	public static Set<String> getClsDatatypeProperty(OWLOntology ont, String clsuri, String propuri) {
		Set<String> vals = new HashSet<String>();
		OWLClass theclass = factory.getOWLClass(IRI.create(clsuri));
		CustomRestrictionVisitor visitor = new CustomRestrictionVisitor(Collections.singleton(ont));
		for (OWLSubClassOfAxiom ax : ont.getSubClassAxiomsForSubClass(theclass)) {
			OWLClassExpression superCls = ax.getSuperClass();
			superCls.accept(visitor);
		}
		for (OWLDataPropertyExpression prop : visitor.getRestrictedDataProperties()) {
			OWLDataProperty property = prop.asOWLDataProperty();
			if (property.getIRI().toString().equals(propuri)) {
				vals.add(visitor.getValueForDataProperty(prop).getLiteral());
			}
		}
		return vals;
	}
	

	public static String getFunctionalClsDatatypeProperty(OWLOntology ont, String clsuri, String propuri) {
		return getClsDatatypeProperty(ont, clsuri, propuri).toArray(new String[]{})[0];
	}

	// SET A CLASS' OBJECT PROPERTY
	public static void setClsObjectProperty(OWLOntology ont, Hashtable<String,String[]> clsnamesandvals, String rel, String invrel,
			Boolean applyinverse, OWLOntologyManager manager) {
		String[] keys = (String[]) clsnamesandvals.keySet().toArray(new String[] {});
		for (int i = 0; i < keys.length; i++) {
			if (clsnamesandvals.get(keys[i]) != null) {
				String[] val = (String[]) clsnamesandvals.get(keys[i]);
				for (int y = 0; y < val.length; y++) {
					if (val[y] != null) {
						setClsObjectProperty(ont, keys[i], val[y], rel, invrel, applyinverse, manager);
					}
				}
			}
		}
	}
	
	
	public static void setClsObjectProperty(OWLOntology ont, String object, String subject, String rel, String invrel,
			Boolean applyinverse, OWLOntologyManager manager){
		OWLClass cls = factory.getOWLClass(IRI.create(object));
		OWLClass value = factory.getOWLClass(IRI.create(subject));
		OWLObjectProperty prop = factory.getOWLObjectProperty(IRI.create(rel));
		OWLClassExpression prop_val = factory.getOWLObjectSomeValuesFrom(prop, value);
		OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(cls, prop_val);
		AddAxiom addAxiom = new AddAxiom(ont, axiom);
		manager.applyChange(addAxiom);

		if (applyinverse) {
			OWLObjectProperty invprop = factory.getOWLObjectProperty(IRI.create(invrel));
			OWLClassExpression invprop_cls = factory.getOWLObjectSomeValuesFrom(invprop, cls);
			OWLSubClassOfAxiom invaxiom = factory.getOWLSubClassOfAxiom(value, invprop_cls);
			AddAxiom invAddAxiom = new AddAxiom(ont, invaxiom);
			manager.applyChange(invAddAxiom);
		}
	}

	// SET A CLASS' DATATYPE PROPERTY
	public static void setClsDatatypeProperty(OWLOntology ont, String clsname,
			String rel, String value, OWLOntologyManager manager)
			throws OWLException {

		OWLClass cls = factory.getOWLClass(IRI.create(clsname));
		OWLDataProperty theprop = factory.getOWLDataProperty(IRI.create(rel));
		OWLLiteral valueconstant = factory.getOWLLiteral(value);

		OWLClassExpression propdesc = factory.getOWLDataHasValue(theprop,valueconstant);
		OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(cls, propdesc);
		AddAxiom addAxiom = new AddAxiom(ont, axiom);
		manager.applyChange(addAxiom);
	}

	// REMOVE AN OBJECT PROPERTY VALUE FOR ONE CLASS (all)
	public static void removeClsObjectAllRestriction(OWLOntology ont,String clsname, String propname, OWLOntologyManager manager)
			throws OWLException {
		OWLClass cls = factory.getOWLClass(IRI.create(clsname));
		OWLObjectPropertyExpression prop = factory.getOWLObjectProperty(IRI.create(propname));
		Set<OWLClassExpression> descs = cls.getSuperClasses(ont);
		for (OWLClassExpression desc : descs) {
			if (desc instanceof OWLObjectAllValuesFrom) {
				OWLObjectAllValuesFrom allrest = (OWLObjectAllValuesFrom) desc;
				if (allrest.getProperty() == prop) {
					OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(cls, desc);
					RemoveAxiom removeax = new RemoveAxiom(ont, axiom);
					manager.applyChange(removeax);
				}
			}
		}
	}

	// REMOVE AN OBJECT PROPERTY VALUE FOR ONE CLASS (all)
	public static void removeClsObjectSomeRestriction(OWLOntology ont,String clsname, String propname, OWLOntologyManager manager)
			throws OWLException {
		OWLClass cls = factory.getOWLClass(IRI.create(clsname));
		OWLObjectPropertyExpression prop = factory.getOWLObjectProperty(IRI.create(propname));
		Set<OWLClassExpression> descs = cls.getSuperClasses(ont);
		for (OWLClassExpression desc : descs) {
			if (desc instanceof OWLObjectSomeValuesFrom) {
				OWLObjectSomeValuesFrom allrest = (OWLObjectSomeValuesFrom) desc;
				if (allrest.getProperty() == prop) {
					OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(cls, desc);
					RemoveAxiom removeax = new RemoveAxiom(ont, axiom);
					manager.applyChange(removeax);
				}
			}
		}
	}

	// REMOVE AN OBJECT PROPERTY VALUE FOR ONE CLASS (hasValue)
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

	// REMOVE AN OBJECT PROPERTY VALUE FOR ONE CLASS
	public static void removeClsDatatypeProperty(OWLOntology ont,
			String clsname, String propname) throws OWLException {
		OWLClass cls = factory.getOWLClass(IRI.create(clsname));
		Set<OWLClassExpression> superclasses = cls.getSuperClasses(ont);
		for (OWLClassExpression onedesc : superclasses) {
			if (onedesc.isAnonymous()) {
				System.out.println("Anonymous");
			}
		}
	}

	
	public static Set<String> convertOWLNamedIndividualSetToStringSet(Set<OWLNamedIndividual> owlindset){
		Set<String> stringset = new HashSet<String>();
		for(OWLNamedIndividual ind : owlindset){
			stringset.add(ind.getIRI().toString());
		}
		return stringset;
	}

	// METHODS ON INDIVIDUALS

	// CREATE INDIVIDUALS WITHIN ONE PARENT CLASS
	public static void createSemSimIndividuals(OWLOntology ont, String[] indnames, String parentclassname, String suffix, OWLOntologyManager manager) throws OWLException {
		OWLClass parent = factory.getOWLClass(IRI.create(parentclassname));
		for (int i = 0; i < indnames.length; i++) {
			createSemSimIndividual(ont, indnames[i], parent, suffix, manager);
		}
	}

	// CREATE INDIVIDUALS UNDER DIFFERENT PARENT CLASSES
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
	
	
	public static void createSemSimIndividual(OWLOntology ont, String indname, OWLClass parent, String suffix, OWLOntologyManager manager){
		OWLIndividual ind = factory.getOWLNamedIndividual(IRI.create(indname + suffix));
		OWLIndividualAxiom axiom = factory.getOWLClassAssertionAxiom(parent, ind);
		if(!ont.getAxioms().contains(axiom)){
			AddAxiom addAxiom = new AddAxiom(ont, axiom);
			manager.applyChange(addAxiom);
		}
	}
	
	public static void subclassIndividual(OWLOntology ont, String ind, String parent, OWLOntologyManager manager){
		OWLClass refclass = factory.getOWLClass(IRI.create(parent));
		OWLIndividual pmcind = factory.getOWLNamedIndividual(IRI.create(ind));
		OWLIndividualAxiom axiom = factory.getOWLClassAssertionAxiom(refclass, pmcind);
		AddAxiom addAxiom = new AddAxiom(ont, axiom);
		manager.applyChange(addAxiom);
	}

	// SET AN OBJECT PROPERTY FOR MULTIPLE INDIVIDUALS
	public static void setIndObjectProperty(OWLOntology ont,
			Hashtable<String, String[]> table, String rel, String invrel,
			OWLOntologyManager manager) throws OWLException {
		String[] keys = (String[]) table.keySet().toArray(new String[] {});
		for (int i = 0; i < keys.length; i++) {
			if (table.get(keys[i]) != null) {
				String[] val = (String[]) table.get(keys[i]);
				for (int y = 0; y < val.length; y++) {
					if (val[y] != null) {
						setIndObjectProperty(ont, keys[i], val[y], rel, invrel, manager);
					}
				}
			}
		}
	}

	// SET AN OBJECT PROPERTY FOR ONE INDIVIDUAL
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
			String object, String rel, String invrel, Set<OWLAnnotation> annsforrel,
			Set<OWLAnnotation> annsforinvrel, OWLOntologyManager manager)
			throws OWLException{
			AddAxiom addAxiom = new AddAxiom(ont, createIndObjectPropertyAxiom(ont, subject, object, rel, annsforrel, manager));
			manager.applyChange(addAxiom);
			OWLAxiom invaxiom = createIndObjectPropertyAxiom(ont, object, subject, invrel, annsforinvrel, manager);
			
			if(!invrel.equals("") && invrel!=null){
				AddAxiom addinvAxiom = new AddAxiom(ont, invaxiom);
				manager.applyChange(addinvAxiom);
			}
	}

	// SET A DATATYPE PROPERTY FOR A SET OF INDIVIDUALS
	public static void setIndDatatypeProperty(OWLOntology ont, Hashtable<String, Object[]> table,
			String rel, OWLOntologyManager manager) throws OWLException {
		String[] keys = (String[]) table.keySet().toArray(new String[] {});
		for (int i = 0; i < keys.length; i++) {
			Object[] val = (Object[]) table.get(keys[i]);
			for (int y = 0; y < val.length; y++) {
				if(!val[y].equals("") && val[y]!=null){
					setIndDatatypeProperty(ont, keys[i], rel, val[y], manager);
				}
			}
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

	// REMOVE A DATATYPE PROPERTY VALUE FOR ONE INDIVIDUAL
	public static void removeIndDatatypeProperty(OWLOntology ont,
			String indname, String propname, OWLOntologyManager manager)
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

	// REMOVE ALL AXOIMS FOR AN INDIVIDUAL'S OBJECT PROPERTY VALUE
	public static void removeAllIndObjectPropertyValues(OWLOntology ont,
			String indname, String propname, OWLOntologyManager manager)
			throws OWLException {
		OWLIndividual ind = factory.getOWLNamedIndividual(IRI.create(indname));
		OWLObjectPropertyExpression prop = factory.getOWLObjectProperty(IRI.create(propname));

		// Have to get value from the axiom you want to remove
		Set<String> values = getIndObjectProperty(ont, indname, propname);
		if (!values.isEmpty()) {
			for (String onevalue : values) {
				OWLIndividual onevalueasind = factory.getOWLNamedIndividual(IRI.create(onevalue));
				OWLAxiom removeax = factory.getOWLObjectPropertyAssertionAxiom(prop, ind, onevalueasind);
				manager.applyChange(new RemoveAxiom(ont, removeax));
			}
		} 
	}

	// REMOVE ONE OF THE AXOIMS FOR AN INDIVIDUAL'S OBJECT PROPERTY VALUE
	public static void removeOneIndObjectPropertyValue(OWLOntology ont,
			String indname, String propname, String valname,
			OWLOntologyManager manager) throws OWLException {
		OWLIndividual ind = factory.getOWLNamedIndividual(IRI.create(indname));
		OWLObjectPropertyExpression prop = factory.getOWLObjectProperty(IRI.create(propname));
		OWLIndividual onevalueasind = factory.getOWLNamedIndividual(IRI.create(valname));
		OWLAxiom removeax = factory.getOWLObjectPropertyAssertionAxiom(prop,ind, onevalueasind);
		manager.applyChange(new RemoveAxiom(ont, removeax));
	}

	// RETRIEVE THE DATATYPE PROPERTY VALUES FOR ONE INDIVIDUAL (RETURNS A SET)
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

	// // RETRIEVE A FUNCTIONAL DATATYPE PROPERTY VALUE FOR ONE INDIVIDUAL
	// (RETURNS A SINGLE STRING)
	public static String getFunctionalIndDatatypeProperty(OWLOntology ont, String indname, String propname) throws OWLException {
		// The hashtable keys are individual IRIs, values are relations (as IRIs)
		Set<String> values = SemSimOWLFactory.getIndDatatypeProperty(ont, indname, propname);
		String[] valuearray = values.toArray(new String[] {});
		
		return (valuearray.length == 1)  ? valuearray[0] : "";
	}

	// RETRIEVE THE VALUES OF AN OBJECT PROPERTY FOR ONE INDIVIDUAL
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
	
	// RETRIEVE THE VALUE OF AN OBJECT PROPERTY FOR ONE INDIVIDUAL
	public static String getFunctionalIndObjectProperty(OWLOntology ont,
			String indname, String propname) throws OWLException {
		String value = "";
		Set<String> values = getIndObjectProperty(ont, indname, propname);
		if(!values.isEmpty()){
			value = values.toArray(new String[]{})[0];
		}
		return value;
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
	

	// CHECK WHETHER AN INDIVIDUAL WITH A GIVEN IRI IS IN AN ONTOLOGY
	public static boolean indExistsInClass(String indtocheck, String parent, OWLOntology ontology) throws OWLException {
		boolean indpresent = false;
		OWLClass parentclass = factory.getOWLClass(IRI.create(parent));
		Set<OWLIndividual> inds = parentclass.getIndividuals(ontology);
		for (OWLIndividual ind : inds) {
			if (ind.asOWLNamedIndividual().getIRI().toString().equals(indtocheck)) {
				indpresent = true;
			}
		}
		return indpresent;
	}

	// CHECK WHETHER AN INDIVIDUAL WITH A GIVEN IRI IS IN AN ONTOLOGY
	public static boolean indExistsInTree(String indtocheck, String parent, OWLOntology ontology) throws OWLException {
		boolean indpresent = false;
		Set<String> allsubclasses = SemSimOWLFactory.getAllSubclasses(ontology, parent, true);
		for (String oneclass : allsubclasses) {
			OWLClass oneowlclass = factory.getOWLClass(IRI.create(oneclass));
			Set<OWLIndividual> inds = oneowlclass.getIndividuals(ontology);
			for (OWLIndividual ind : inds) {
				if (ind.asOWLNamedIndividual().getIRI().toString().equals(indtocheck)) {
					indpresent = true;
				}
			}
		}
		return indpresent;
	}

	// Remove an individual from an ontology (references and all)
	public static void removeInd(OWLOntology ontology, String indname, OWLOntologyManager manager) {
		OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI.create(indname));
		Set<OWLAxiom> refax = ontology.getReferencingAxioms(ind);
		OWLAxiom[] refaxarray = (OWLAxiom[]) refax.toArray(new OWLAxiom[] {});
		for (int i = 0; i < refaxarray.length; i++) {
			manager.applyChange(new RemoveAxiom(ontology, refaxarray[i]));
		}
	}

	public static void removeClass(OWLOntology ontology, OWLClass theclass,
			Boolean andinds, OWLOntologyManager manager) {
		if (andinds) {
			for (OWLIndividual ind : theclass.getIndividuals(ontology)) {
				Set<OWLAxiom> refaxind = ind.asOWLNamedIndividual().getReferencingAxioms(ontology);
				OWLAxiom[] refaxindarray = (OWLAxiom[]) refaxind.toArray(new OWLAxiom[] {});
				for (int i = 0; i < refaxindarray.length; i++) {
					manager.applyChange(new RemoveAxiom(ontology,refaxindarray[i]));
				}
			}
		}

		Set<OWLAxiom> refax = theclass.getReferencingAxioms(ontology);
		OWLAxiom[] refaxarray = (OWLAxiom[]) refax.toArray(new OWLAxiom[] {});
		for (int i = 0; i < refaxarray.length; i++) {
			manager.applyChange(new RemoveAxiom(ontology, refaxarray[i]));
		}
	}

	public static void removeIndAxiomsFromOntology(OWLOntology ont,
			URI sourceonturi, String theinduri, OWLOntologyManager manager) {
		try {
			OWLEntity ind = factory.getOWLNamedIndividual(IRI.create(theinduri));
			OWLOntology sourceontologycopy = manager.loadOntologyFromOntologyDocument(new File(sourceonturi));
			for (OWLAxiom singleax : sourceontologycopy.getReferencingAxioms(ind)) {
				manager.applyChange(new RemoveAxiom(ont, singleax));
			}
		} catch (OWLException j) {
		}
	}

	public static void moveIndToNewClass(OWLOntology ont, String induri,
			String newclassuri, OWLOntologyManager manager) {
		OWLIndividual ind = factory.getOWLNamedIndividual(IRI.create(induri));
		OWLClassAssertionAxiom[] axiomarray = ont.getClassAssertionAxioms(ind).toArray(new OWLClassAssertionAxiom[] {});
		for (int t = 0; t < axiomarray.length; t++) {
			manager.applyChange(new RemoveAxiom(ont, axiomarray[t]));
		}
		OWLClass parent = factory.getOWLClass(IRI.create(newclassuri));
		OWLIndividualAxiom axiom = factory.getOWLClassAssertionAxiom(parent,ind);
		AddAxiom addAxiom = new AddAxiom(ont, axiom);
		manager.applyChange(addAxiom);
	}

	public static void renameOWLEntities(OWLOntology ont,
			Hashtable<String, String> oldandnewiris, OWLOntologyManager manager) {
		for (String oldiri : oldandnewiris.keySet()) {
			renameOWLEntities(ont, oldiri, oldandnewiris.get(oldiri), manager);
		}
	}

	public static void renameOWLEntities(OWLOntology ont, String oldiri,String newiri, OWLOntologyManager manager) {
		// Does not appear to work for ontology-level annotations
		Set<OWLOntology> onts = new HashSet<OWLOntology>();
		onts.add(ont);
		OWLEntityRenamer renamer = new OWLEntityRenamer(manager, onts);
		List<OWLOntologyChange> changelist = renamer.changeIRI(IRI.create(oldiri), IRI.create(newiri));
		Iterator<OWLOntologyChange> it = changelist.iterator();
		while (it.hasNext()) {
			OWLOntologyChange remove = (OWLOntologyChange) it.next();
			OWLOntologyChange add = (OWLOntologyChange) it.next();
			manager.applyChange(new RemoveAxiom(remove.getOntology(), remove.getAxiom()));
			manager.applyChange(new AddAxiom(add.getOntology(), add.getAxiom()));
		}
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

	public static void setRDFLabel(OWLOntology ontology, Hashtable<String,String[]> urisandvals,
			OWLOntologyManager manager) {
		OWLAnnotationProperty label = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		String[] keys = (String[]) urisandvals.keySet().toArray(new String[] {});
		for (int i = 0; i < keys.length; i++) {
			if(!urisandvals.get(keys[i])[0].equals("") && urisandvals.get(keys[i])[0]!=null){
				OWLNamedIndividual annind = factory.getOWLNamedIndividual(IRI.create(keys[i]));
				Set<OWLAnnotation> anns = annind.getAnnotations(ontology, label);
				for (OWLAnnotation ann : anns) {
					OWLAxiom removeax = factory.getOWLAnnotationAssertionAxiom((OWLAnnotationSubject) annind, ann);
					manager.applyChange(new RemoveAxiom(ontology, removeax));
				}
				String[] array = urisandvals.get(keys[i]);
				OWLAnnotation newann = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), factory.getOWLLiteral(array[0], "en"));
				OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(annind.getIRI(), newann);
				manager.applyChange(new AddAxiom(ontology, ax));
			}
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
	
	
	public static void removeOntologyAnnotation(OWLOntology ont, OWLAnnotation ann, OWLOntologyManager manager){
		if(ann!=null && ont.getAnnotations().contains(ann)){
			manager.applyChange(new RemoveOntologyAnnotation(ont, ann));
		}
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
	
	
	// Overloaded method for traversing tree - this one allows traversal over multiple relation types
	public static Set<String> traverseRelationTree(OWLOntology ontology, String parenturi, String[] relations) throws OWLException {
	    // traverse all nodes that belong to the parent
		Set<String> nodes  = new HashSet<String>();
		Set<String> set = new HashSet<String>();
		for(String relation : relations){
			set.addAll(SemSimOWLFactory.getIndObjectProperty(ontology, parenturi, relation));
		}
	    for(String node : set){
		    // store node information
		    nodes.add(node);
		    // traverse children recursively
		    traverseRelationTree(ontology, node, relations);
	    }
	    return nodes;
	}
	
	

	public static String getIRIfragment(String uri) {
		String result = "";
		if (!uri.equals("")) {
			if (uri.contains("#")) 
				result = uri.substring(uri.lastIndexOf("#") + 1, uri.length());
			else if (uri.startsWith("http://identifiers.org"))
				result = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
			else if (uri.startsWith("urn:miriam:"))
				result = uri.substring(uri.lastIndexOf(":") + 1, uri.length());
			else if (uri.contains("/"))
				result = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
			else
				result = uri;
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
		else {
			if (uri.startsWith("http://purl.obolibrary.org/obo/")) { // To deal with CHEBI and Mouse Adult Gross Anatomy Ontology
				return uri.substring(0, uri.lastIndexOf("_"));
			} 
			else if(uri.startsWith("urn:miriam:")){
				return uri.substring(0, uri.lastIndexOf(":") + 1);
			}
			else {
				return uri.substring(0, uri.lastIndexOf("/") + 1);
			}
		}
	}

	public static String addOntologyNickname(String ontname, String texttoappend, SemSimConstants constants) {
		String newtext = texttoappend;
		if (SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.containsKey(ontname)) {
			String abbrev = (String) SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.get(ontname);
			newtext = newtext + " (" + abbrev + ")";
		}
		return newtext;
	}

	

	// REPLACING SPECIAL CHARACTERS THAT SHOULDN'T BE USED IN A IRI
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
	
	
	public void makeURIstringFromPhysicalModelComponent(PhysicalModelComponent pmc, SemSimModel semsimmodel){
		String uristring = "Physical_";
		
		// If we have a property of a physical entity
		if(pmc instanceof ReferencePhysicalEntity){
			uristring = uristring + "entity_";
		}
		// If it's a property of a physical process
		else if(pmc instanceof ReferencePhysicalProcess){
			uristring = uristring + "process_";
		}
		else if(pmc instanceof PhysicalProperty){
			PhysicalProperty pp = (PhysicalProperty)pmc;
			uristring = pp.getAssociatedDataStructure().getName() + "_property";
		}
		
		if(!(pmc instanceof PhysicalProperty)){
			if(pmc.getFirstRefersToReferenceOntologyAnnotation()!=null){
				uristring = semsimmodel.getNamespace() + uristring + 
					pmc.getFirstRefersToReferenceOntologyAnnotation().getOntologyAbbreviation() +
					"_" + SemSimOWLFactory.getIRIfragment(pmc.getFirstRefersToReferenceOntologyAnnotation().getReferenceURI().toString());
			}
			else{
				uristring = semsimmodel.getNamespace() + uristring + pmc.getName();
			}
		}
	}

	
	public static URI getURIforPhysicalProperty(SemSimModel model, PhysicalProperty prop){
		return URI.create(model.getNamespace() + URIencoding(prop.getAssociatedDataStructure().getName()) + "_property");
	}
	
	
	// given a uri for a composite annotation component, this will find all the indexEntity uris that are connected 
	// to that component via structural relations
	public static Set<String> findIndexEntitiesLinkedToAnnotationComponent(OWLOntology ont, String uri) throws OWLException{
		Set<String> returnuris = new HashSet<String>();
		String[] relationsarray = new String[]{SemSimConstants.HAS_PART_URI.toString(), SemSimConstants.CONTAINS_URI.toString(),
				SemSimConstants.SEMSIM_NAMESPACE + "component_in"}; //, SemSimConstants.SEMSIM_NAMESPACE+"hasPhysicalProperty",SemSimConstants.SEMSIM_NAMESPACE+"hasComputationalComponent"};
		// use tree traversal to get all components that use the affected component, then find those that are index entities,
		// then add to the returned set of strings
		for(String indintree : SemSimOWLFactory.traverseRelationTree(ont, uri, relationsarray)){
			String indexent = SemSimOWLFactory.getFunctionalIndObjectProperty(ont, indintree, SemSimConstants.SEMSIM_NAMESPACE + "isIndexEntityFor");
			if(!indexent.equals("")){
				returnuris.add(indintree);
			}
		}
		return returnuris;
	}
	
	

	public static String[] getAllAnnotationsForCodeword(DataStructure ds) {
		String compositeann = ds.getCompositeAnnotationAsString(false);
		String noncompositeann = "";  // need to deal with this later
		// comments are natural language definitions
		String humread = ds.getDescription();
		return new String[] { compositeann, noncompositeann, humread };
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

	public static String abbreviateCompositeAnnotation(String text) {
		// System.out.println(text);
		text = text.replace("<SEMSIM:physicalPropertyOf>", "<of> ");
		text = text.replace("<RO:contained_in>", "<contained in> ");
		text = text.replace("<RO:part_of>", "<part of> ");
		text = text.replace("<SEMSIM:composed_of>", "<of> ");
		text = text.replace("Portion of ", "");
		text = text.replace("<contained_in> ", "<contained in> ");
		text = text.replace("<part_of> ", "<part of> ");
		text = text.replace("<composed_of> ", "<of> ");
		String firstletter = text.substring(0, 1);
		firstletter = firstletter.toUpperCase();
		text = firstletter.concat(text.substring(1, text.length()));
		return text;
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
	
	public static String getBioPortalShortIDFromURI(String uri){
		return SemSimOWLFactory.getIRIfragment(uri);
	}
}