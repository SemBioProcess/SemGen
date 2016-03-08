package semsim.annotation;

import java.util.ArrayList;

import semsim.definitions.ReferenceOntologies.ReferenceOntology;

public class Ontology {
	private String fullname;
	private String nickname;
	private String bioportalnamespace;
	private ArrayList<String> namespaces = new ArrayList<String>();
	private String description;
	
	public Ontology(String name, String abrev, String[] ns, String desc, String bpns) {
		fullname = name;
		nickname = abrev;
		description = desc;
		for (String s : ns) {
			namespaces.add(s.trim());
		}
		bioportalnamespace = bpns;
	}
	
	public Ontology(String name, String abrev, String[] ns, String desc) {
		fullname = name;
		nickname = abrev;
		description = desc;
		for (String s : ns) {
			namespaces.add(s.trim());
		}
	}
	
	public Ontology(ReferenceOntology ro) {
		fullname = ro.getFullName();
		nickname = ro.getNickName();
		bioportalnamespace = ro.getBioPortalNamespace();
		namespaces = ro.getNamespaces();
		description = ro.getDescription();
	}
	
	public boolean hasNamespace(String nspace) {
		for (String ns : namespaces) {
			if (nspace.startsWith(ns)) return true; 
		}
		return false;
	}
	
	public String getFullName() {
		return new String(fullname);
	}
	
	public String getNickName() {
		return new String(nickname);
	}
	
	public String getBioPortalNamespace() {
		return bioportalnamespace;
	}
	
	public ArrayList<String> getNameSpaces() {
		return namespaces;
	}
	
	public String getDescription() {
		return description;
	}
}
