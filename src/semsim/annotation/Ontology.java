package semsim.annotation;

import java.util.ArrayList;

import semsim.definitions.ReferenceOntologies.ReferenceOntology;

public class Ontology {
	private String fullname;
	private String nickname;
	private String bioportalid = null;
	private ArrayList<String> namespaces = new ArrayList<String>();
	private String description;
	
	public Ontology(String name, String abrev, String[] ns, String desc, String bpid) {
		fullname = name;
		nickname = abrev;
		description = desc;
		for (String s : ns) {
			namespaces.add(s.trim());
		}
		bioportalid = bpid;
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
		bioportalid = ro.getBioPortalID();
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
	
	public String getBioPortalID() {
		return bioportalid;
	}
	
	public ArrayList<String> getNameSpaces() {
		return namespaces;
	}
	
	public String getDescription() {
		return description;
	}
}
