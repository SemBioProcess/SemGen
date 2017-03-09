package semsim.annotation;

import java.util.ArrayList;

import semsim.definitions.ReferenceOntologies.ReferenceOntology;

/**
 * Class representing identifier information about an ontology.
 * This includes the ontology's full name, BioPortal namespace,
 * textual description, etc.
 */
public class Ontology {
	private String fullname;
	private String nickname;
	private String bioportalnamespace;
	private ArrayList<String> namespaces = new ArrayList<String>();
	private String description;
	
	/**
	 * @param name Full name of ontology
	 * @param abrev Nickname of ontology
	 * @param ns Array of namespaces used for this ontology
	 * @param desc Textual description of ontology
	 * @param bpns BioPortal namespace of the ontology
	 */
	public Ontology(String name, String abrev, String[] ns, String desc, String bpns) {
		fullname = name;
		nickname = abrev;
		description = desc;
		for (String s : ns) {
			namespaces.add(s.trim());
		}
		bioportalnamespace = bpns;
	}
	
	/**
	 * @param name Full name of ontology
	 * @param abrev Nickname of ontology
	 * @param ns Array of namespaces used for this ontology
	 * @param desc Textual description of ontology
	 */
	public Ontology(String name, String abrev, String[] ns, String desc) {
		fullname = name;
		nickname = abrev;
		description = desc;
		for (String s : ns) {
			namespaces.add(s.trim());
		}
	}
	
	/**
	 * Constructor for creating an {@link Ontology} class
	 * from a {@link ReferenceOntology} class
	 * @param ro The {@link ReferenceOntology} that will have
	 * its information copied to this class.
	 */
	public Ontology(ReferenceOntology ro) {
		fullname = ro.getFullName();
		nickname = ro.getNickName();
		bioportalnamespace = ro.getBioPortalNamespace();
		namespaces = ro.getNamespaces();
		description = ro.getDescription();
	}
	
	/**
	 * @param nspace Namespace to test for association with the ontology
	 * @return Whether the namespace is in the array of namespaces associated
	 * with this ontology
	 */
	public boolean hasNamespace(String nspace) {
		for (String ns : namespaces) {
			if (nspace.startsWith(ns)) return true; 
		}
		return false;
	}
	
	/**
	 * @return Full name of ontology
	 */
	public String getFullName() {
		return new String(fullname);
	}
	
	/**
	 * @return Nickname of ontology
	 */
	public String getNickName() {
		return new String(nickname);
	}
	
	/**
	 * @return The BioPortal namespace of the ontology
	 */
	public String getBioPortalNamespace() {
		return bioportalnamespace;
	}
	
	/**
	 * @return The set of namespaces associated with the ontology
	 */
	public ArrayList<String> getNameSpaces() {
		return namespaces;
	}
	
	/**
	 * @return A free-text description of the ontology
	 */
	public String getDescription() {
		return description;
	}
}
