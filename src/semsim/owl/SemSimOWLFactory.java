package semsim.owl;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.HashSet;
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
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import semsim.annotation.Relation;
import semsim.definitions.RDFNamespace;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;

import java.util.Map;
import java.util.Set;

/**
 * Class mostly providing convenience methods for working with OWL API objects
 * @author mneal
 *
 */
public class SemSimOWLFactory {

	public static OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();

	public SemSimOWLFactory() {}

	
	/**
	 * @param ont An OWLOntology
	 * @return The XML base namespace for the ontology
	 */
	public static String getXMLbaseFromOntology(OWLOntology ont) {
		String base = ont.getOntologyID().toString();
		base = base.substring(base.indexOf("<") + 1, base.indexOf(">")) + "#";
		return base;
	}

	
	/**
	 * @param iri An IRI pointing to an ontology's location
	 * @param manager The OWLOntologyManager that will be checked to see if it has already 
	 * loaded the ontology at the input IRI
	 * @return Whether the ontology at the input IRI has already been loaded into the 
	 * input OWLOntologyManager
	 */
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
			if (ont.getOntologyID().toString().contentEquals(newont.getOntologyID().toString())) {
				returnont = ont;
				tempmanager.removeOntology(newont);
			}
		}
		return returnont;
	}
	
	
	// METHODS ON CLASSES

	/**
	 * Add an external reference class to a SemSim model ontology 
	 * @param destinationont The SemSim model ontology to which the class will be added
	 * @param clsIRI The IRI of the class as a string
	 * @param physicaltype The physical type of the reference class (e.g. physical entity, process, etc.)
	 * @param humreadname A human-readable free text definition for the class
	 * @param manager An OWLOntologyManager instance
	 */
	public static void addExternalReferenceClass(OWLOntology destinationont,
			String clsIRI, String physicaltype, String humreadname, OWLOntologyManager manager) {
		String parentname = RDFNamespace.SEMSIM.getNamespaceAsString() + "Reference_physical_" + physicaltype;
		OWLClass parent = factory.getOWLClass(IRI.create(parentname));
		OWLClass classtoadd = factory.getOWLClass(IRI.create(clsIRI));
		OWLAxiom axiom = factory.getOWLSubClassOfAxiom(classtoadd, parent);
		AddAxiom addAxiom = new AddAxiom(destinationont, axiom);
		manager.applyChange(addAxiom);

		OWLDataProperty hasphysdefprop = factory.getOWLDataProperty(SemSimRelation.HAS_PHYSICAL_DEFINITION.getIRI());
		OWLLiteral con2 = factory.getOWLLiteral(clsIRI);
		OWLClassExpression physdef = factory.getOWLDataHasValue(hasphysdefprop,con2);
		OWLSubClassOfAxiom ax2 = factory.getOWLSubClassOfAxiom(classtoadd, physdef);
		AddAxiom addAx2 = new AddAxiom(destinationont, ax2);
		manager.applyChange(addAx2);
		
		setRDFLabel(destinationont, classtoadd, humreadname, manager);
	}
	
	
	/**
	 * Add a class to a SemSim model ontology
	 * @param ont The SemSim model ontology to which the class will be added
	 * @param classIRI The IRI of the class as a String
	 * @param parentnames IRIs (as Strings) of the parent classes in the ontology
	 * that should subsume the input class
	 * @param manager An OWLOntologyManager instance
	 */
	public static void addClass(OWLOntology ont, String classIRI, String[] parentnames, OWLOntologyManager manager) {
		for (int x = 0; x < parentnames.length; x++) {
			OWLClass parent = factory.getOWLClass(IRI.create(parentnames[x]));
			OWLClass classtoadd = factory.getOWLClass(IRI.create(classIRI));
			OWLAxiom axiom = factory.getOWLSubClassOfAxiom(classtoadd,parent);
			AddAxiom addAxiom = new AddAxiom(ont, axiom);
			manager.applyChange(addAxiom);
		}
	}

	
	/**
	 * Remove a data property value for one class (uses hasValue relation)
	 * @param ont The ontology containing the class
	 * @param clsIRI The IRI of the class as a String
	 * @param propIRI The IRI of the data property as a String
	 * @param manager An OWLOntologyManager instance
	 * @throws OWLException
	 */
	public static void removeClsDataValueRestriction(OWLOntology ont, String clsIRI, String propIRI, OWLOntologyManager manager)
			throws OWLException {
		OWLClass cls = factory.getOWLClass(IRI.create(clsIRI));
		OWLDataPropertyExpression prop = factory.getOWLDataProperty(IRI.create(propIRI));
		Set<OWLClassExpression> descs = cls.getSuperClasses(ont);
		for (OWLClassExpression desc : descs) {
			if (desc instanceof OWLDataHasValue) {
				OWLDataHasValue allrest = (OWLDataHasValue) desc;
				if (allrest.getProperty().asOWLDataProperty().getIRI().toString().contentEquals(prop.asOWLDataProperty().getIRI().toString())) {
					OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(cls, desc);
					RemoveAxiom removeax = new RemoveAxiom(ont, axiom);
					manager.applyChange(removeax);
				}
			}
		}
	}
		
		
	// METHODS ON INDIVIDUALS
	
	/**
	 * Instantiate an OWL individual in a SemSim model
	 * @param ont The SemSim model ontology to which the individual will be added
	 * @param indIRI The individual's IRI as a String
	 * @param parent The parent class for the new individual
	 * @param suffix An optional suffix to append to the individual's IRI
	 * @param manager An OWLOntologyManager instance
	 */
	public static void createSemSimIndividual(OWLOntology ont, String indIRI, OWLClass parent, String suffix, OWLOntologyManager manager){
		OWLIndividual ind = factory.getOWLNamedIndividual(IRI.create(indIRI + suffix));
		OWLIndividualAxiom axiom = factory.getOWLClassAssertionAxiom(parent, ind);
		if(!ont.getAxioms().contains(axiom)){
			AddAxiom addAxiom = new AddAxiom(ont, axiom);
			manager.applyChange(addAxiom);
		}
	}
	
	
	/**
	 * Create multiple individuals within one parent class
	 * @param ont The SemSim model ontology to which the individuals will be added
	 * @param indIRIs The IRIs of the individuals as Strings
	 * @param parentIRI The IRI of the parent class that will subsume the individuals (as a String)
	 * @param suffix An option suffix to append to each individual's IRI
	 * @param manager An OWLOntologyManager instance
	 * @throws OWLException
	 */
	public static void createSemSimIndividuals(OWLOntology ont, String[] indIRIs, String parentIRI, String suffix, OWLOntologyManager manager) throws OWLException {
		OWLClass parent = factory.getOWLClass(IRI.create(parentIRI));
		for (int i = 0; i < indIRIs.length; i++) {
			createSemSimIndividual(ont, indIRIs[i], parent, suffix, manager);
		}
	}

	
	/**
	 * Create individuals under different parent classes
	 * @param ont The SemSim model ontology to which the individuals will be added
	 * @param indandparents A Hashtable containing a the list of the individuals' IRIs
	 * (as Strings) and the IRIs of the parent classes that should subsume them (also
	 * as Strings).
	 * @param suffix An optional suffix to append to each individual's IRI
	 * @param manager An OWLOntologyManager instance
	 * @throws OWLException
	 */
	public static void createSemSimIndividuals(OWLOntology ont, Hashtable<String,String[]> indandparents, String suffix, OWLOntologyManager manager)
			throws OWLException {

		String[] parentname = new String[] {};
		String[] keyset = (String[]) indandparents.keySet().toArray();
		for (int i = 0; i < keyset.length; i++) {
			parentname = (String[]) indandparents.get(keyset[i]);
			OWLClass parent = factory.getOWLClass(IRI.create(parentname[0]));
			createSemSimIndividual(ont, keyset[i], parent, suffix, manager);
		}
	}

	
	/**
	 * Add a class assertion axiom to an ontology to indicate that an input 
	 * individual is a member of a certain class.
	 * @param ont The ontology in which to make this assertion
	 * @param indIRI The IRI of the individual to subclass (as a String)
	 * @param parentIRI The IRI of the parent class (as a String)
	 * @param manager An OWLOntologyManager instance
	 */
	public static void subclassIndividual(OWLOntology ont, String indIRI, String parentIRI, OWLOntologyManager manager){
		OWLClass refclass = factory.getOWLClass(IRI.create(parentIRI));
		OWLIndividual pmcind = factory.getOWLNamedIndividual(IRI.create(indIRI));
		OWLIndividualAxiom axiom = factory.getOWLClassAssertionAxiom(refclass, pmcind);
		AddAxiom addAxiom = new AddAxiom(ont, axiom);
		manager.applyChange(addAxiom);
	}
	
	
	/**
	 * Add an object property restriction to use as a superclass on an individual (uses the "some" restriction type).
	 * E.g. individual x -contains- some reference ontology class
	 * @param ont Ontology that will contain the restriction
	 * @param ind The IRI of the individual that the restriction is on (as a String)
	 * @param property The IRI of the object property used in the restriction (as a String)
	 * @param value The IRI of the class used in the object property restriction
	 * @param manager An OWLOntologyManager
	 */
	public static void addExistentialObjectPropertyRestrictionOnIndividual(OWLOntology ont, String ind, String property,
			String value, OWLOntologyManager manager){
				
		OWLObjectProperty owlproperty = factory.getOWLObjectProperty(IRI.create(property));
        OWLClass owlvalue = factory.getOWLClass(IRI.create(value));
        OWLClassExpression owlexp = factory.getOWLObjectSomeValuesFrom(owlproperty, owlvalue);
		OWLNamedIndividual owlind = factory.getOWLNamedIndividual(IRI.create(ind));

        OWLClassAssertionAxiom ax = factory.getOWLClassAssertionAxiom(owlexp, owlind);
        AddAxiom addAx = new AddAxiom(ont, ax);
        manager.applyChange(addAx);	
	}

	
	/**
	 * Assert an object property axiom on an individual
	 * E.g. individual x -partOf- individual y
	 * @param ont The ontology that will contain the axiom
	 * @param subject The IRI of the subject individual (as in subject-predicate-object statements)
	 * @param object The IRI of the object individual
	 * @param rel The object property used in the axiom (as a {@link Relation})
	 * @param invrel The inverse of the object property used in the axiom (to assert inverse axiom as well)
	 * @param manager An OWLOntologyManager
	 * @throws OWLException
	 */
	public static void setIndObjectProperty(OWLOntology ont, String subject, String object, Relation rel, Relation invrel, OWLOntologyManager manager)
			throws OWLException {

		AddAxiom addAxiom = new AddAxiom(ont, createIndObjectPropertyAxiom(ont, subject, object, rel, null, manager));
		manager.applyChange(addAxiom);
		if(invrel!=null){
			OWLAxiom invaxiom = createIndObjectPropertyAxiom(ont, object, subject, invrel, null, manager);
			AddAxiom addinvAxiom = new AddAxiom(ont, invaxiom);
			manager.applyChange(addinvAxiom);
		}
	}
	
	
	/**
	 * Used internally to make object property assertions on individuals in 
	 * a SemSim ontology
	 * @param ont The SemSim ontology that will contain the axiom
	 * @param subject IRI of the subject individual
	 * @param object IRI of the obejct individual
	 * @param rel The object property used in the axiom (as a {@link Relation}
	 * @param anns A set of OWLAnnotations to add to the axiom
	 * @param manager An OWLOntologyManager
	 * @return The object property axiom added to the ontology
	 * @throws OWLException
	 */
	private static OWLAxiom createIndObjectPropertyAxiom(OWLOntology ont, String subject,
			String object, Relation rel, Set<OWLAnnotation> anns, OWLOntologyManager manager)
		throws OWLException {
		OWLIndividual ind = factory.getOWLNamedIndividual(IRI.create(subject));
		OWLIndividual value = factory.getOWLNamedIndividual(IRI.create(object));
		OWLObjectProperty prop = factory.getOWLObjectProperty(rel.getIRI());
		OWLAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(prop, ind,value);
		if(anns!=null)
			axiom = axiom.getAnnotatedAxiom(anns);
		return axiom;
	}
	
	
	/**
	 * Add an object property axiom to an individual along with annotations on the axiom
	 * @param ont The ontology that will contain the axiom
	 * @param subject IRI of the subject individual
	 * @param object IRI of the object individual
	 * @param rel The object property used in the axiom (as a {@link Relation}
	 * @param invrel The inverse of the object property used in the axiom, to assert
	 * inverse axiom if desired
	 * @param annsforrel A set of OWLAnnotations to add to the axiom
	 * @param manager An OWLOntologyManager
	 * @throws OWLException
	 */
	public static void setIndObjectPropertyWithAnnotations(OWLOntology ont, String subject,
		String object, Relation rel, Relation invrel, Set<OWLAnnotation> annsforrel, OWLOntologyManager manager)
		throws OWLException{
		AddAxiom addAxiom = new AddAxiom(ont, createIndObjectPropertyAxiom(ont, subject, object, rel, annsforrel, manager));
		manager.applyChange(addAxiom);
		if(invrel!=null){
			OWLAxiom invaxiom = createIndObjectPropertyAxiom(ont, object, subject, invrel, null, manager);

			AddAxiom addinvAxiom = new AddAxiom(ont, invaxiom);
			manager.applyChange(addinvAxiom);
		}
	}

	
	/**
	 * Used internally to assert datatype property axioms on individuals
	 * @param ont The ontology to which the axiom will be added
	 * @param subject IRI of the subject individual
	 * @param rel Datatype property used in the axiom (as a {@link Relation}
	 * @param val The datatype value used in the axiom
	 * @param anns Annotations to add to the axiom
	 * @param manager An OWLOntologyManager
	 * @return The datatype property axiom added to the ontology
	 * @throws OWLException
	 */
	private static OWLAxiom createIndDatatypePropertyAxiom(OWLOntology ont, String subject, Relation rel,
			Object val, Set<OWLAnnotation> anns, OWLOntologyManager manager)
		throws OWLException {
		OWLIndividual ind = factory.getOWLNamedIndividual(IRI.create(subject));
		OWLDataProperty prop = factory.getOWLDataProperty(rel.getIRI());
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

	
	/**
	 * Assert a datatype property on an individual
	 * @param ont The ontology to which the axiom will be added
	 * @param indIRI IRI of the subject individual (as a String)
	 * @param rel The datatype property used in the axiom (as a {@link Relation})
	 * @param val The datatype value used in the axiom
	 * @param manager An OWLOntologyManager
	 * @throws OWLException
	 */
	public static void setIndDatatypeProperty(OWLOntology ont, String indIRI, Relation rel, Object val, OWLOntologyManager manager) throws OWLException {
		if(!val.equals("")){
			OWLAxiom axiom = createIndDatatypePropertyAxiom(ont, indIRI, rel, val, null, manager);
			AddAxiom addAxiom = new AddAxiom(ont, axiom);
			manager.applyChange(addAxiom);
		}
	}
	
	
	/**
	 * Assert a datatype property axiom and include annotations on the axiom
	 * @param ont The ontology to which the axiom will be added
	 * @param indIRI IRI of the subject individual (as a String)
	 * @param rel The datatype property used in the axiom (as a {@link Relation})
	 * @param val The datatype value used in the axiom
	 * @param anns Annotations to add to the axiom
	 * @param manager An OWLOntologyManager
	 * @throws OWLException
	 */
	public static void setIndDatatypePropertyWithAnnotations(OWLOntology ont, String indIRI, Relation rel, Object val,
			Set<OWLAnnotation> anns, OWLOntologyManager manager) throws OWLException{
		AddAxiom addAxiom = new AddAxiom(ont, createIndDatatypePropertyAxiom(ont, indIRI, rel, val, anns, manager));
		manager.applyChange(addAxiom);
	}

	
	/**
	 * Retrieve the datatype property values for one individual
	 * @param ont The ontology containing the individual
	 * @param indIRI IRI of the individual
	 * @param propIRI IRI of the datatype property of interest
	 * @return The datatype values for all axioms on the individual
	 * that use the datatype relation
	 * @throws OWLException
	 */
	public static Set<String> getIndDatatypePropertyValues(OWLOntology ont, String indIRI, String propIRI) throws OWLException {
		Set<String> values = new HashSet<String>();
		OWLIndividual ind = factory.getOWLNamedIndividual(IRI.create(indIRI));
		OWLDataPropertyExpression prop = factory.getOWLDataProperty(IRI.create(propIRI));
		Map<OWLDataPropertyExpression, Set<OWLLiteral>> dataprops = ind.getDataPropertyValues(ont);
		Set<OWLDataPropertyExpression> datapropskeyset = dataprops.keySet();
		for (OWLDataPropertyExpression expression : datapropskeyset) {
			if (expression.equals(prop)) {
				if (dataprops.get(expression) != null) {
					for (OWLLiteral value : dataprops.get(expression)) {
						values.add(value.getLiteral());
					}
				}
			}
		}
		return values;
	}

	
	/**
	 * Retrieve a functional datatype property value for one individual
	 * @param ont The ontology containing the individual
	 * @param indIRI IRI of the individual
	 * @param propIRI The IRI of the datatype property
	 * @return The value for the datatype axiom on the individual, if present. If there are 
	 * multiple datatype axioms on the individual that use the property, the value for the 
	 * first axiom is returned.
	 * @throws OWLException
	 */
	public static String getFunctionalIndDatatypePropertyValues(OWLOntology ont, String indIRI, String propIRI) throws OWLException {
		// The hashtable keys are individual IRIs, values are relations (as IRIs)
		Set<String> values = getIndDatatypePropertyValues(ont, indIRI, propIRI);
		String[] valuearray = values.toArray(new String[] {});
		
		return (valuearray.length == 1)  ? valuearray[0] : "";
	}

	
	/**
	 * @param ont The ontology containing the individual
	 * @param indIRI IRI of the individual
	 * @param propIRI IRI of the object property
	 * @return The objects of all object property axioms on an individual
	 * that uses the given object property
	 * @throws OWLException
	 */
	public static Set<String> getIndObjectPropertyObjects(OWLOntology ont, String indIRI, String propIRI) throws OWLException {
		Set<String> values = new HashSet<String>();
		OWLIndividual ind = factory.getOWLNamedIndividual(IRI.create(indIRI));
		OWLObjectProperty prop = factory.getOWLObjectProperty(IRI.create(propIRI));
		Map<OWLObjectPropertyExpression, Set<OWLIndividual>> objprops = ind.getObjectPropertyValues(ont);
		for (OWLObjectPropertyExpression expression : objprops.keySet()) {
			if (expression.equals(prop)) {
				if (objprops.get(expression) != null) {
					for (OWLIndividual value : objprops.get(expression)) {
						values.add(value.asOWLNamedIndividual().getIRI().toString());
					}
				}
			}
		}
		return values;
	}
	
	
	/**
	 * @param ont The ontology containing the individual
	 * @param induri IRI of the individual
	 * @param propuri IRI of the object property
	 * @return The value of an object property for one individual (i.e. one IRI as a String)
	 * @throws OWLException
	 */
	public static String getFunctionalIndObjectPropertyObject(OWLOntology ont, String induri, String propuri) throws OWLException {
		String value = "";
		Set<String> values = getIndObjectPropertyObjects(ont, induri, propuri);
		if(!values.isEmpty()){
			value = values.toArray(new String[]{})[0];
		}
		return value;
	}
	
	
	/**
	 * Remove a datatype property axiom on one individual
	 * @param ont The ontology containing the axiom
	 * @param induri IRI of the individual in the axiom
	 * @param propuri IRI of the datatype property in the axiom
	 * @param manager An OWLOntologyManager
	 * @throws OWLException
	 */
	public static void removeIndDatatypePropertyAxiom(OWLOntology ont, String induri, String propuri, OWLOntologyManager manager)
			throws OWLException {
		OWLIndividual ind = factory.getOWLNamedIndividual(IRI.create(induri));
		Set<OWLAxiom> refaxs = ind.asOWLNamedIndividual().getReferencingAxioms(ont);
		for(OWLAxiom refax : refaxs){
			if(refax instanceof OWLDataPropertyAssertionAxiom){
				OWLDataPropertyAssertionAxiom axiom = (OWLDataPropertyAssertionAxiom)refax;
				if(axiom.getProperty().asOWLDataProperty().getIRI().toString().contentEquals(propuri)){
					manager.applyChange(new RemoveAxiom(ont, axiom));
				}
			}
		}
	}
	

	/**
	 * @param ont The ontology containing the class
	 * @param parentclass IRI of the class
	 * @return All OWL individuals subclassed under a given class in an ontology
	 */
	public static Set<String> getIndividualsAsStrings(OWLOntology ont, String parentclass) {
		Set<String> indstrings = new HashSet<String>();
		Set<OWLIndividual> existinginds = factory.getOWLClass(IRI.create(parentclass)).getIndividuals(ont);
		for (OWLIndividual ind : existinginds) {
			indstrings.add(ind.asOWLNamedIndividual().getIRI().toString());
		}
		return indstrings;
	}
	
	
	/**
	 * @param ont The ontology containing the class
	 * @param parentclass IRI of the class
	 * @return All individuals subclassed under the class, or within its
	 * subclass hierarchy
	 * @throws OWLException
	 */
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
	
	
	/**
	 * @param indtocheck An OWL individual's IRI
	 * @param parent Parent OWL class to check
	 * @param ontology Ontology containing the parent class
	 * @return Whether an individual with a given IRI is subclassed under a specified parent 
	 * @throws OWLException
	 */
	public static boolean indExistsInClass(String indtocheck, String parent, OWLOntology ontology) throws OWLException {
		OWLClass parentclass = factory.getOWLClass(IRI.create(parent));
		Set<OWLIndividual> inds = parentclass.getIndividuals(ontology);
		for (OWLIndividual ind : inds) {
			if (ind.asOWLNamedIndividual().getIRI().toString().contentEquals(indtocheck)) {
				return true;
			}
		}
		return false;
	}

	
	/**
	 * @param indIRItocheck IRI of an OWL individual
	 * @param parentclsIRI IRI of hierarchy root class to check for individual
	 * @param ontology The ontology to look within
	 * @return Whether an individual with a given IRI is an instance of a given class hierarchy in an ontology
	 * @throws OWLException
	 */
	public static boolean indExistsInTree(String indIRItocheck, String parentclsIRI, OWLOntology ontology) throws OWLException {
		Set<String> allsubclasses = SemSimOWLFactory.getAllSubclasses(ontology, parentclsIRI, true);
		for (String oneclass : allsubclasses) {
			OWLClass oneowlclass = factory.getOWLClass(IRI.create(oneclass));
			Set<OWLIndividual> inds = oneowlclass.getIndividuals(ontology);
			for (OWLIndividual ind : inds) {
				if (ind.asOWLNamedIndividual().getIRI().toString().contentEquals(indIRItocheck)) {
					return true;
				}
			}
		}
		return false;
	}

	
	/**
	 * @param ont The ontology containing the OWLEntity
	 * @param ent An OWLEntity
	 * @return The RDF:label annotations on a given OWLEntity
	 */
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
	
	
	/**
	 * @param ont The ontology containing the OWLEntity
	 * @param ent The OWLEntity to check for RDF:labels
	 * @return The first RDF:label annotation on a given OWLEntity
	 */
	public static String getRDFLabel(OWLOntology ont, OWLEntity ent){
		return getRDFLabels(ont,ent)[0];
	}

	
	/**
	 * Set the RDF:label annotation for a given OWL individual
	 * @param ontology The ontology containing the individual
	 * @param annind The individual to annotate
	 * @param value The RDF:label value
	 * @param manager An OWLOntologyManager
	 */
	public static void setRDFLabel(OWLOntology ontology, OWLNamedIndividual annind, String value, OWLOntologyManager manager) {
		if(value!=null && !value.contentEquals("")){
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

	
	/**
	 * Set the RDF:label for a given OWL class (removes all previously applied RDF:labels)
	 * @param ontology The ontology containing the class
	 * @param cls The OWL class
	 * @param value The RDF:label value
	 * @param manager An OWLOntologyManager
	 */
	public static void setRDFLabel(OWLOntology ontology, OWLClass cls, String value, OWLOntologyManager manager) {
		if(value!=null && !value.contentEquals("")){
			OWLAnnotationProperty label = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
			Set<OWLAnnotation> anns = cls.getAnnotations(ontology, label);
			for (OWLAnnotation ann : anns) {
				OWLAnnotationSubject annsub = cls.getIRI();
				OWLAxiom removeax = factory.getOWLAnnotationAssertionAxiom(annsub,ann);
				manager.applyChange(new RemoveAxiom(ontology, removeax));
			}
			OWLAnnotation newann = factory.getOWLAnnotation(
					factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), factory.getOWLLiteral(value, "en"));
			OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(cls.getIRI(),newann);
			manager.applyChange(new AddAxiom(ontology, ax));
		}
	}
	
	
	/**
	 * Set the RDF:comment for a an OWLEntity (removes all existing RDF:comment annotations)
	 * @param ontology The ontology containing the OWLEntity
	 * @param ent The OWLEntity
	 * @param value The RDF:comment value
	 * @param manager An OWLOntologyManager
	 */
	public static void setRDFComment(OWLOntology ontology, OWLEntity ent, String value, OWLOntologyManager manager) {
		if(value!=null && !value.contentEquals("")){
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
	
	
	/**
	 * Add an ontology-level annotation that has an OWLEntity as its object
	 * @param ont The ontology to annotate
	 * @param property IRI of the OWLAnnotationProperty to use in the annotation
	 * @param ent IRI of the OWLEntity that is the object in the annotation
	 * @param manager An OWLOntologyManager
	 */
	public static void addOntologyAnnotation(OWLOntology ont, String property, OWLEntity ent, OWLOntologyManager manager){
		OWLAnnotationValue entiri = ent.getIRI();
		OWLAnnotation anno = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(IRI.create(property)), entiri);
		manager.applyChange(new AddOntologyAnnotation(ont, anno));
	}
	
	
	/**
	 * Add an ontology-level annotation that has a language-specific OWLLiteral as its object 
	 * @param ont The ontology to annotate
	 * @param property IRI of the OWLAnnotationProperty to use in the annotation
	 * @param val The OWLLiteral value used in the annotation
	 * @param lang The language for the OWLLiteral
	 * @param manager An OWLOntologyManager
	 */
	public static void addOntologyAnnotation(OWLOntology ont, String property, String val, String lang, OWLOntologyManager manager){
		OWLLiteral lit = factory.getOWLLiteral(val,lang);
		OWLAnnotation anno = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(IRI.create(property)), lit);
		manager.applyChange(new AddOntologyAnnotation(ont, anno));
	}

	
	/**
	 * Add an ontology-level annotation that has an OWLLiteral as its object (English is assumed
	 * for the language of the OWLLiteral)
	 * @param ont The ontology to annotate
	 * @param property IRI of the OWLAnnotationProperty used in the annotation
	 * @param val The OWLLiteral value used in the annotation
	 * @param manager An OWLOntologyManager
	 */
	public static void addOntologyAnnotation(OWLOntology ont, IRI property, String val, OWLOntologyManager manager){
		OWLLiteral lit = factory.getOWLLiteral(val,"en");
		OWLAnnotation anno = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(property), lit);
		manager.applyChange(new AddOntologyAnnotation(ont, anno));
	}
	
	
	/**
	 * @param ont The ontology containing the parent class
	 * @param parentIRI IRI of the parent class
	 * @param includeparent Whether to include the parent in the returned class list
	 * @return A list of all subclasses subsumed by a given parent class
	 * @throws OWLException
	 */
	public static Set<String> getAllSubclasses(OWLOntology ont, String parentIRI, Boolean includeparent) throws OWLException{
		// traverse all nodes that belong to the parent
		Set<String> nodes  = new HashSet<String>();
		if(includeparent) nodes.add(parentIRI);
		OWLClass parentclass = factory.getOWLClass(IRI.create(parentIRI));
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
		
	
	
	/**
	 * @param iri An IRI as a String
	 * @return The fragment of an input IRI
	 */
	public static String getIRIfragment(String iri) {
		String result = iri;
		if (!iri.contentEquals("")) {
			if (iri.contains("#")) 
				result = iri.substring(iri.lastIndexOf("#") + 1, iri.length());
			else if (iri.contains("/"))
				result = iri.substring(iri.lastIndexOf("/") + 1, iri.length());
			else if (iri.startsWith("http://identifiers.org"))
				result = iri.substring(iri.lastIndexOf("/") + 1, iri.length());
			else if (iri.startsWith("urn:miriam:"))
				result = iri.substring(iri.lastIndexOf(":") + 1, iri.length());
		}
		return result;
	}
	
	
	/**
	 * @param iri An IRI as a String
	 * @return A URL-decoded version of an IRI's fragment
	 */
	public static String getURIdecodedFragmentFromIRI(String iri){
		return URIdecoding(getIRIfragment(iri));
	}
	
	
	/**
	 * @param iri An IRI as a String
	 * @return The namespace of a given IRI
	 */
	public static String getNamespaceFromIRI(String iri) {
		if (iri.contains("#")) {
			return iri.substring(0, iri.indexOf("#") + 1);
		} 
		if (iri.startsWith("http://purl.obolibrary.org/obo/")) { // To deal with CHEBI and Mouse Adult Gross Anatomy Ontology
			return iri.substring(0, iri.lastIndexOf("_"));
		} 
		if(iri.startsWith("urn:miriam:")){
			return iri.substring(0, iri.lastIndexOf(":") + 1);
		}
		return iri.substring(0, iri.lastIndexOf("/") + 1);
	}

	
	/**
	 * Replace special characters that shouldn't be used in an IRI
	 * @param word An input String
	 * @return A URL-encoded version of the String
	 */
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
	
	
	/**
	 * Used internally to perform URL decoding on Strings
	 * @param uri An input URI
	 * @return A URL-decoded version of the input URI
	 */
	private static String URIdecoding(String uri) {
		String result = uri;
		try {
			result = URLDecoder.decode(result, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		result = result.replace("%2A", "*");
		return result;
	}
	
	
	/**
	 * Generate a SemSim-compliant URI for an individual representing the physical property 
	 * simulated by a {@link DataStructure}
	 * @param model The SemSim model containing the {@link DataStructure}
	 * @param ds The {@link DataStructure}
	 * @return A SemSim-compliant URI for an individual representing the physical property 
	 * simulated by a {@link DataStructure}
	 */
	public static URI getURIforPhysicalProperty(SemSimModel model, DataStructure ds){
		return URI.create(model.getNamespace() + URIencoding(ds.getName()) + "_property");
	}
	

	/**
	 * Retrieve the RDF:comment on an OWLEntity. Returns an empty String if there 
	 * are no RDF:comments or if multiple RDF:comments exist.
	 * @param ont The ontology containing the OWLEntity
	 * @param ent The OWLEntity
	 * @return The RDF:comment on an OWLEntity, if there is exactly one. If zero or more
	 * than one, returns an empty String.
	 */
	public static String getRDFcomment(OWLOntology ont, OWLEntity ent) {
		String commentstring = "";
		OWLAnnotationProperty comment = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI());

		if (ent.getAnnotations(ont, comment).size() <= 1) {
			for (OWLAnnotation annotation : ent.getAnnotations(ont, comment)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					if (val.getLiteral() != null) commentstring = val.getLiteral();
				}
			}
		} else {
			System.err.println("ERROR: Multiple comments for " + ent.toString());
		}
		return commentstring;
	}
		
	
	/**
	 * If a given IRI matches an IRI in an input set of IRIs, make it unique by appending it with an integer
	 * @param iritoappend The IRI to append with an integer until it is different from the IRIs in the input set
	 * @param existingmembers The set of other IRIs
	 * @return An IRI that does not match any of the IRIs in the input set
	 */
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
	
	
	/**
     * Code from OWL API example on GitHub
     * Visits existential restrictions and collects the properties which are
     * restricted.
     */
    public static class RestrictionVisitor extends
            OWLClassExpressionVisitorAdapter {

        private final Set<OWLClass> processedClasses;
        private final Map<OWLObjectPropertyExpression, OWLClassExpression> restrictedPropertiesAndFillersMap;
        private final Set<OWLOntology> onts;

        public RestrictionVisitor(Set<OWLOntology> onts) {
            processedClasses = new HashSet<OWLClass>();
            restrictedPropertiesAndFillersMap = new HashMap<OWLObjectPropertyExpression, OWLClassExpression>();
            this.onts = onts;
        }

//        public Set<OWLObjectPropertyExpression> getRestrictedProperties() {
//            return restrictedProperties;
//        }
        
        public Map<OWLObjectPropertyExpression, OWLClassExpression> getPropertyFillerMap(){
        	return restrictedPropertiesAndFillersMap;
        }

        @Override
        public void visit(OWLClass desc) {
            if (!processedClasses.contains(desc)) {
                // If we are processing inherited restrictions then we
                // recursively visit named supers. Note that we need to keep
                // track of the classes that we have processed so that we don't
                // get caught out by cycles in the taxonomy
                processedClasses.add(desc);
                for (OWLOntology ont : onts) {
                    for (OWLSubClassOfAxiom ax : ont
                            .getSubClassAxiomsForSubClass(desc)) {
                        ax.getSuperClass().accept(this);
                    }
                }
            }
        }

        @Override
        public void visit(OWLObjectSomeValuesFrom desc) {
            // This method gets called when a class expression is an existential
            // (someValuesFrom) restriction and it asks us to visit it
        	restrictedPropertiesAndFillersMap.put(desc.getProperty(),desc.getFiller());
        }
    }

}