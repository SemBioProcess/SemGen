package semgen;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import semgen.extraction.ExtractorTab;
import semsim.model.computational.DataStructure;
import semsim.model.physical.PhysicalEntity;




public class DependencyCluster {
	public Hashtable<PhysicalEntity, Set<DataStructure>> entnamesanddatastrs = new Hashtable<PhysicalEntity, Set<DataStructure>>();
	public Set<String> cdwduris = new HashSet<String>();
	public Set<String> compositeentstrings = new HashSet<String>();
	public ExtractorTab extractor;
	//public OWLOntology ontology;

	public DependencyCluster(Set<String> cdwduriset, ExtractorTab extractor) {
		this.cdwduris = cdwduriset;
		this.extractor = extractor;
		// compositeentstrings = getEntityAnnotationStrings(cdwduris);
		entnamesanddatastrs = extractor.listentities();
	}
	/*
	 * public Set<String> getEntityAnnotationStrings(Set<String> cdwduris){
	 * for(String cdwduri : cdwduris){ String indexent = ""; String ent = "";
	 * try {indexent = OWLMethods.getFunctionalIndObjectProperty(ontology,
	 * cdwduri + "_property", SemGen.base + "physicalPropertyOf");
	 * if(!indexent.equals("")){ ent =
	 * OWLMethods.makeStringFromEntityAnnotationArray
	 * (OWLMethods.getCompositeEntityAnnotationComponents(extractor.ontology,
	 * indexent, ver), ontology, ver, false); }
	 * 
	 * sempanel.add(new JLabel(ent));
	 * if(!entnames.contains(ent)){entnames.add(ent);System.out.println(" " +
	 * ent);}
	 * 
	 * } catch (OWLException e1) {e1.printStackTrace();} } }
	 */
}
