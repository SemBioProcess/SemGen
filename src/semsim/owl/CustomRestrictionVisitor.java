package semsim.owl;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;

import java.util.Set;
import java.util.HashSet;
import java.util.Hashtable;

/**
 * Visits existential restrictions and collects the properties which are
 * restricted
 */
public class CustomRestrictionVisitor extends OWLClassExpressionVisitorAdapter {

	private boolean processInherited = true;
	private Set<OWLClass> processedClasses;
	public Hashtable<OWLDataPropertyExpression, OWLLiteral> restrictedDataPropertiesTable;
	public Hashtable<OWLObjectPropertyExpression, OWLIndividual> restrictedValueObjectPropertiesTable;
	public Hashtable<OWLObjectPropertyExpression, Set<String>> restrictedAllObjectPropertiesTable;
	public Hashtable<OWLObjectPropertyExpression, Set<String>> restrictedSomeObjectPropertiesTable;
	public Hashtable<OWLObjectPropertyExpression, String> restrictedExactCardinalityObjectPropertiesTable;
	public Set<OWLDataPropertyExpression> restrictedDataProperties;
	public Set<OWLObjectPropertyExpression> restrictedObjectProperties;
	private Set<OWLOntology> onts;

	public CustomRestrictionVisitor(Set<OWLOntology> onts) {
		restrictedDataProperties = new HashSet<OWLDataPropertyExpression>();
		restrictedDataPropertiesTable = new Hashtable<OWLDataPropertyExpression, OWLLiteral>();
		restrictedObjectProperties = new HashSet<OWLObjectPropertyExpression>();
		restrictedValueObjectPropertiesTable = new Hashtable<OWLObjectPropertyExpression, OWLIndividual>();
		restrictedAllObjectPropertiesTable = new Hashtable<OWLObjectPropertyExpression, Set<String>>();
		restrictedSomeObjectPropertiesTable = new Hashtable<OWLObjectPropertyExpression, Set<String>>();
		restrictedExactCardinalityObjectPropertiesTable = new Hashtable<OWLObjectPropertyExpression, String>();

		processedClasses = new HashSet<OWLClass>();
		this.onts = onts;
	}

	public OWLLiteral getValueForDataProperty(OWLDataPropertyExpression exp) {
		OWLLiteral con = (OWLLiteral) restrictedDataPropertiesTable.get(exp);
		return con;
	}

	public OWLLiteral getValueObjectProperty(OWLObjectPropertyExpression exp) {
		OWLLiteral con = (OWLLiteral) restrictedValueObjectPropertiesTable.get(exp);
		return con;
	}

	public Set<String> getSomeObjectProperty(OWLObjectPropertyExpression exp) {
		return (Set<String>) restrictedSomeObjectPropertiesTable.get(exp);
	}

	public Set<String> getAllObjectProperty(OWLObjectPropertyExpression exp) {
		return (Set<String>) restrictedAllObjectPropertiesTable.get(exp);
	}

	public String getCardinalityObjectProperty(OWLObjectPropertyExpression exp) {
		return restrictedExactCardinalityObjectPropertiesTable.get(exp);
	}

	public Set<OWLDataPropertyExpression> getRestrictedDataProperties() {
		return restrictedDataProperties;
	}

	public Set<OWLObjectPropertyExpression> getRestrictedObjectProperties() {
		return restrictedObjectProperties;
	}

	public void visit(OWLClass desc) {
		if (processInherited && !processedClasses.contains(desc)) {
			// If we are processing inherited restrictions then
			// we recursively visit named supers. Note that we
			// need to keep track of the classes that we have processed
			// so that we don't get caught out by cycles in the taxonomy
			processedClasses.add(desc);
			for (OWLOntology ont : onts) {
				for (OWLSubClassOfAxiom ax : ont
						.getSubClassAxiomsForSubClass(desc)) {
					ax.getSuperClass().accept(this);
				}
			}
		}
	}

	public void reset() {
		processedClasses.clear();
	}

	public void visit(OWLObjectSomeValuesFrom desc) {
		// This method gets called when a description (OWLDescription) is an
		// existential (someValuesFrom) restriction and it asks us to visit it
		Set<String> fillerset = new HashSet<String>();
		Set<String> test = (Set<String>) restrictedSomeObjectPropertiesTable
				.get(desc.getProperty());
		if (test != null) {
			fillerset = (Set<String>) restrictedSomeObjectPropertiesTable
					.get(desc.getProperty());
		}
		fillerset.add(desc.getFiller().asOWLClass().getIRI().toString());
		restrictedSomeObjectPropertiesTable.put(desc.getProperty(), fillerset);
		restrictedObjectProperties.add(desc.getProperty());
	}

	public void visit(OWLDataHasValue desc) {
		// This method gets called when a description (OWLDescription) is an
		// existential (someValuesFrom) restriction and it asks us to visit it
		restrictedDataPropertiesTable.put(desc.getProperty(), desc.getValue());
		restrictedDataProperties.add(desc.getProperty());
	}

	public void visit(OWLObjectHasValue desc) {
		restrictedValueObjectPropertiesTable.put(desc.getProperty(),
				desc.getValue());
		restrictedObjectProperties.add(desc.getProperty());
	}

	public void visit(OWLObjectExactCardinality desc) {
		restrictedExactCardinalityObjectPropertiesTable.put(desc.getProperty(),
				desc.getFiller().asOWLClass().getIRI().toString());
		restrictedObjectProperties.add(desc.getProperty());
	}

	public void visit(OWLObjectAllValuesFrom desc) {
		Set<String> fillerset = new HashSet<String>();
		if (restrictedAllObjectPropertiesTable.get(desc.getProperty()) != null) {
			fillerset = (Set<String>) restrictedAllObjectPropertiesTable
					.get(desc.getProperty());
		}
		fillerset.add(desc.getFiller().toString());
		restrictedAllObjectPropertiesTable.put(desc.getProperty(), fillerset);
		restrictedObjectProperties.add(desc.getProperty());
	}
}
