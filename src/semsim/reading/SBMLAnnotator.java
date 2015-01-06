package semsim.reading;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import org.jdom.JDOMException;
import org.sbml.libsbml.CVTerm;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SBase;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.libsbmlConstants;
import org.semanticweb.owlapi.model.OWLException;

import semsim.SemSimConstants;
import semsim.annotation.Annotation;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.StructuralRelation;
import semsim.annotation.CurationalMetadata.Metadata;
import semsim.model.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.PhysicalModelComponent;
import semsim.model.physical.Submodel;
import semsim.model.physical.object.CompositePhysicalEntity;

public class SBMLAnnotator {

	public static void annotate(File sbmlfile, SemSimModel semsimmodel, boolean isonline, Hashtable<String, String[]> ontologytermsandnamescache) throws OWLException, IOException, JDOMException, ServiceException {
		Hashtable<String,MIRIAMannotation> resourcesandanns = new Hashtable<String,MIRIAMannotation>();
		Map<Compartment,PhysicalEntity> compsandphysents = new HashMap<Compartment,PhysicalEntity>();
		Map<Species,PhysicalEntity> speciesandphysents = new HashMap<Species,PhysicalEntity>();

		if(ontologytermsandnamescache==null)
			ontologytermsandnamescache = new Hashtable<String, String[]>();
			
		Model sbmlmodel = null;
		
		SBMLDocument sbmldoc = new SBMLReader().readSBMLFromFile(sbmlfile.getAbsolutePath());
		if (sbmldoc.getNumErrors()>0){
		      System.err.println("Encountered the following SBML errors:");
		      sbmldoc.printErrors();
		}
		else {
			sbmlmodel = sbmldoc.getModel();
			
			// Collect model-level information, apply as annotations on SemSimModel object	
			// First get model notes
			String notes = sbmlmodel.getNotesString();
			notes = notes.replace("<notes>", "");
			notes = notes.replace("</notes>", "");
			semsimmodel.addAnnotation(new Annotation(SemSimConstants.HAS_NOTES_RELATION, notes));
			if(sbmlmodel.getName()!=null)semsimmodel.setModelAnnotation(Metadata.fullname, sbmlmodel.getName());
			if(sbmlmodel.getId()!=null) semsimmodel.setModelAnnotation(Metadata.sourcemodelid, sbmlmodel.getId());

			// Get dc terms
			
			// Get CV terms
			for(int i=0; i<sbmlmodel.getNumCVTerms();i++){
				CVTerm term = sbmlmodel.getCVTerm(i);
				if(term.getQualifierType()==1){
					Integer t = Integer.valueOf(term.getBiologicalQualifierType());
					if(SemSimConstants.BIOLOGICAL_QUALIFIER_TYPES_AND_RELATIONS.containsKey(t)){
						for(int j=0; j<term.getNumResources(); j++){
							String uri = term.getResourceURI(j);
							semsimmodel.addAnnotation(new Annotation(SemSimConstants.BIOLOGICAL_QUALIFIER_TYPES_AND_RELATIONS.get(t), uri));
						}
					}
				}
				if(term.getQualifierType()==0){
					Integer t = Integer.valueOf(term.getModelQualifierType());
					if(SemSimConstants.MODEL_QUALIFIER_TYPES_AND_RELATIONS.containsKey(t)){
						for(int h=0; h<term.getNumResources(); h++){
							String uri = term.getResourceURI(h);
							semsimmodel.addAnnotation(new Annotation(SemSimConstants.MODEL_QUALIFIER_TYPES_AND_RELATIONS.get(t), uri));
						}
					}
				}
			}
			
			// collect all compartment annotations
			for(int c=0; c<sbmlmodel.getListOfCompartments().size(); c++){
				Compartment comp = sbmlmodel.getCompartment(c);
				DataStructure ds = semsimmodel.getDataStructure(xxx(comp.getId()));
				
				String OPBclassID = null;
				// Annotate the physical property of the compartment
				if(ds.getUnit()!=null){
					if(ds.getUnit().getUnitType().equals("volume")){
						OPBclassID = "OPB_00154";
					}
					else if(ds.getUnit().getUnitType().equals("area")){
						OPBclassID = "OPB_00295";
					}
					
					if(OPBclassID!=null){
						ds.getPhysicalProperty().addAnnotation(new ReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION,
								URI.create(SemSimConstants.OPB_NAMESPACE + OPBclassID), null));
					}
				}
				PhysicalEntity ent = null;
				String description = null;
				
				if(comp.getName()!=null && !comp.getName().equals("")) description = comp.getName();
				else description = comp.getId();
				
				boolean hasusableann = false;

				// If there is some annotation
				boolean entcreated = false;
				for(int m=0; m<comp.getNumCVTerms(); m++){
					CVTerm cvterm = comp.getCVTerm(m);
					for(int r=0; r<cvterm.getNumResources(); r++){
						String resource = cvterm.getResourceURI(r);
						hasusableann = ((cvterm.getBiologicalQualifierType()==libsbmlConstants.BQB_IS) || 
								(cvterm.getBiologicalQualifierType()==libsbmlConstants.BQB_IS_VERSION_OF));
						MIRIAMannotation ma = getMiriamAnnotation(resource, ontologytermsandnamescache);
						if(ma!=null && !entcreated){
							if(ma.fulluri!=null && semsimmodel.containsDataStructure(xxx(comp.getId()))){
								
								// If the annotation is an "is-a" annotation
								if(cvterm.getBiologicalQualifierType()==libsbmlConstants.BQB_IS){
									ent = semsimmodel.addReferencePhysicalEntity(ma.fulluri, ma.rdflabel);
									description = ent.getFirstRefersToReferenceOntologyAnnotation().getValueDescription();
									resourcesandanns.put(resource, ma);
									entcreated = true;
								}
								else if(cvterm.getBiologicalQualifierType()==libsbmlConstants.BQB_IS_VERSION_OF){
									// Put physical entity in model but don't connect it to data structure
									PhysicalEntity ivoent = semsimmodel.getPhysicalEntityByReferenceURI(ma.fulluri);
									if(ivoent == null){
										ivoent = semsimmodel.addReferencePhysicalEntity(ma.fulluri, ma.rdflabel);
									}
									// Establish is-a relationship with reference annotation here
									if(comp.getName()!=null && !comp.getName().equals(""))
										ent = semsimmodel.addCustomPhysicalEntity(comp.getName(), comp.getName());
									else
										ent = semsimmodel.addCustomPhysicalEntity(comp.getId(), comp.getId());
									ReferenceOntologyAnnotation ann = new ReferenceOntologyAnnotation(
											SemSimConstants.BQB_IS_VERSION_OF_RELATION, ma.fulluri, ma.rdflabel);
									ent.addAnnotation(ann);
									entcreated = true;
									description = ent.getDescription();
								}
							}
						}
					}
				}
				
				// If no annotation present, create a new custom physical entity from compartment name
				if(!hasusableann || (isonline && hasusableann && !entcreated)){
					ent = semsimmodel.addCustomPhysicalEntity(description, description);
				}
				if(ent!=null){
					compsandphysents.put(comp, ent);
					ds.getPhysicalProperty().setPhysicalPropertyOf(ent);
				}
			}
			// end of compartment for loop, next process semantics for all species
			
			for(int g=0; g<sbmlmodel.getNumSpecies(); g++){
				setCompositeAnnotationForModelComponent(sbmlmodel.getSpecies(g), sbmlmodel, semsimmodel, resourcesandanns, isonline, 
						compsandphysents, speciesandphysents, ontologytermsandnamescache);
			}
			
			// process semantics for reactions
			for(int r=0; r<sbmlmodel.getNumReactions();r++){
				Reaction rxn = sbmlmodel.getReaction(r);
				setCompositeAnnotationForModelComponent(rxn, sbmlmodel, semsimmodel, resourcesandanns, isonline, 
						compsandphysents, speciesandphysents, ontologytermsandnamescache);
				String ratecdwd = xxx(rxn.getId());
				DataStructure theds = semsimmodel.getDataStructure(ratecdwd);
				
				if(semsimmodel.containsDataStructure(xxx(rxn.getId()) + ".rate")){
					ratecdwd = ratecdwd + ".rate";
				}
				PhysicalModelComponent pmc = theds.getPhysicalProperty().getPhysicalPropertyOf();

				// Establish the relationships between the reaction and its reactants, products and modifiers
				for(int l=0;l<rxn.getNumReactants();l++){
					Species speciesreact = sbmlmodel.getSpecies(rxn.getReactant(l).getSpecies());
					if(speciesandphysents.containsKey(speciesreact)){
						PhysicalEntity reac = speciesandphysents.get(speciesreact);
						if(pmc instanceof PhysicalProcess){
							PhysicalProcess pmcp = (PhysicalProcess)pmc;
							pmcp.addSource(reac, (int)rxn.getReactant(l).getStoichiometry());
						}
					}
				}
				for(int l=0;l<rxn.getNumProducts();l++){
					Species speciesprod = sbmlmodel.getSpecies(rxn.getProduct(l).getSpecies());
					if(speciesandphysents.containsKey(speciesprod)){
						PhysicalEntity prod = speciesandphysents.get(speciesprod);
						if(pmc instanceof PhysicalProcess){
							PhysicalProcess pmcp = (PhysicalProcess)pmc;
							pmcp.addSink(prod, (int)rxn.getReactant(l).getStoichiometry());
						}
					}
				}
				for(int l=0;l<rxn.getNumModifiers();l++){
					Species speciesmod = sbmlmodel.getSpecies(rxn.getModifier(l).getSpecies());
					if(speciesandphysents.containsKey(speciesmod)){
						PhysicalEntity mod = speciesandphysents.get(speciesmod);
						if(pmc instanceof PhysicalProcess){
							PhysicalProcess pmcp = (PhysicalProcess)pmc;
							pmcp.addMediator(mod, (int)rxn.getReactant(l).getStoichiometry());
						}
					}
				}
				
				// Create a new model component (sub-model) that represents the reaction
				Set<DataStructure> dss = new HashSet<DataStructure>();
				dss.add(theds);
				
				for(DataStructure dsin : theds.getComputation().getInputs())  dss.add(dsin);
				for(int sp=0; sp<rxn.getNumReactants(); sp++){
					SpeciesReference speciesref = rxn.getReactant(sp);
					String speciesid = sbmlmodel.getSpecies(speciesref.getSpecies()).getId();
					
					// species ids that start with _ are prefixed with xxx during SBML-to-MML translation
					if(semsimmodel.getDataStructure(xxx(speciesid))!=null){
						dss.add(semsimmodel.getDataStructure(xxx(speciesid)));
					}
				}
				for(int sp=0; sp<rxn.getNumProducts(); sp++){
					SpeciesReference speciesref = rxn.getProduct(sp);
					String speciesid = sbmlmodel.getSpecies(speciesref.getSpecies()).getId();
					
					// species ids that start with _ are prefied with xxx during SBML-to-MML translation
					if(semsimmodel.getDataStructure(speciesid)!=null){
						dss.add(semsimmodel.getDataStructure(xxx(speciesid)));
					}
				}
				for(int sp=0; sp<rxn.getNumModifiers(); sp++){
					ModifierSpeciesReference speciesref = rxn.getModifier(sp);
					String speciesid = sbmlmodel.getSpecies(speciesref.getSpecies()).getId();
					
					// species ids that start with _ are prefied with xxx during SBML-to-MML translation
					if(semsimmodel.getDataStructure(speciesid)!=null){
						dss.add(semsimmodel.getDataStructure(xxx(speciesid)));
					}
				}
				
				String reactionname = rxn.getId();
				
				if(rxn.getName()!=null && !rxn.getName().equals("")){
					reactionname = rxn.getName();
				}
				
				reactionname = reactionname + " reaction";
				Submodel sub = new Submodel(reactionname);
				
				for(DataStructure ds : dss) sub.addDataStructure(ds);
				semsimmodel.addSubmodel(sub);
				
				if(pmc!=null){
					if(pmc.hasRefersToAnnotation()){
						ReferenceOntologyAnnotation refann = pmc.getFirstRefersToReferenceOntologyAnnotation();
						sub.addReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, refann.getReferenceURI(), 
								refann.getValueDescription());
					}
				}
			}
		}
	}			
		
	private static Boolean setCompositeAnnotationForModelComponent(SBase modelcomp, Model sbmlmodel, SemSimModel semsimmodel, 
			Hashtable<String,MIRIAMannotation> resourcesandanns, boolean isonline, Map<Compartment,PhysicalEntity> compsandphysents,
			Map<Species,PhysicalEntity> speciesandphysents, Hashtable<String, String[]> ontologytermsandnamescache) {
		
		// If we're annotating a chemical species
		if(modelcomp instanceof Species){
			Boolean hasisannotation = false;

			Species species = (Species)modelcomp;
			Set<MIRIAMannotation> isversionofmas = new HashSet<MIRIAMannotation>();
			
			for(int h=0;h<species.getNumCVTerms();h++){
				CVTerm cvterm = species.getCVTerm(h);
				if(cvterm.getBiologicalQualifierType()==libsbmlConstants.BQB_IS_VERSION_OF){
					for(int r=0; r<cvterm.getNumResources(); r++){
						MIRIAMannotation ma = collectMiriamAnnotation(cvterm.getResourceURI(r),
								resourcesandanns, ontologytermsandnamescache);
						if(ma.fulluri!=null) isversionofmas.add(ma);
					}
				}
			}
			PhysicalEntity speciesent = null;
			for(int h=0;h<species.getNumCVTerms();h++){
				CVTerm cvterm = species.getCVTerm(h);
				if(cvterm.getBiologicalQualifierType()==libsbmlConstants.BQB_IS){ 
					for(int r=0; r<cvterm.getNumResources(); r++){
						
						MIRIAMannotation ma = collectMiriamAnnotation(cvterm.getResourceURI(r),
								resourcesandanns, ontologytermsandnamescache);
		
						// If we have a context for the annotation and a human-readable name, create a new reference physical entity
						if(ma!=null){
							if(ma.fulluri!=null){
								resourcesandanns.put(cvterm.getResourceURI(r), ma);
								speciesent = semsimmodel.getPhysicalEntityByReferenceURI(ma.fulluri);
								if(speciesent == null)
									speciesent = semsimmodel.addReferencePhysicalEntity(ma.fulluri, ma.rdflabel);
								// If the species has already been annotated against a reference term
								// and there is another reference term that is annotated against, add the annotation
								else if(hasisannotation) speciesent.addReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, ma.fulluri, ma.rdflabel);
								hasisannotation = true;
							}
						}
					}
				}
			}
			// Otherwise we need to create a custom physical entity for the species
			if(!hasisannotation && isonline){
				if(species.getName()!=null && !species.getName().equals(""))
					speciesent = semsimmodel.addCustomPhysicalEntity(species.getName(), species.getName());
				else
					speciesent = semsimmodel.addCustomPhysicalEntity(species.getId(), species.getId());
				
				if(!isversionofmas.isEmpty()){
					MIRIAMannotation appliedann = isversionofmas.toArray(new MIRIAMannotation[]{})[0];
					semsimmodel.addReferencePhysicalEntity(appliedann.fulluri, appliedann.rdflabel);
					speciesent.addReferenceOntologyAnnotation(SemSimConstants.BQB_IS_VERSION_OF_RELATION, appliedann.fulluri, appliedann.rdflabel);
				}
			}
			
			String conccdwd = xxx(species.getId());
			String amtcdwd = xxx(species.getId() + ".amt");
			
			// If we're using an old version of JSim and we're appending differently with .conc and .amt
			if (semsimmodel.containsDataStructure(xxx(species.getId() + ".conc"))){
					conccdwd = xxx(species.getId() + ".conc");
					amtcdwd = xxx(species.getId());
			}
			if(semsimmodel.containsDataStructure(amtcdwd)) annotateSpecies(species, amtcdwd, speciesent, sbmlmodel, semsimmodel, isonline, compsandphysents, speciesandphysents);
			
			if(semsimmodel.containsDataStructure(conccdwd)) annotateSpecies(species, conccdwd, speciesent, sbmlmodel, semsimmodel, isonline, compsandphysents, speciesandphysents);
			return true;
		}
			
		// If the SBML element is a reaction
		else if(modelcomp instanceof Reaction){
			Reaction rxn = (Reaction)modelcomp;
			Boolean gotisversionofannotation = false;	
			// If we are applying annotations
			Set<MIRIAMannotation> isversionofmas = new HashSet<MIRIAMannotation>();
			
			boolean hasusableisversionofann = false;
			
			for(int h=0;h<rxn.getNumCVTerms();h++){
				CVTerm cvterm = rxn.getCVTerm(h);
				if(cvterm.getBiologicalQualifierType()==libsbmlConstants.BQB_IS_VERSION_OF){
					hasusableisversionofann = true;
					for(int r=0; r<cvterm.getNumResources(); r++){
						MIRIAMannotation ma = collectMiriamAnnotation(cvterm.getResourceURI(r),
								resourcesandanns, ontologytermsandnamescache);
						if(ma.fulluri!=null){
							isversionofmas.add(ma);
							gotisversionofannotation = true;
						}
					}
				}
			}
			
			PhysicalProcess pproc = null;
			boolean hasusableisann = false;
			Boolean gotisannotation = false;
			for(int h=0;h<rxn.getNumCVTerms();h++){
				CVTerm cvterm = rxn.getCVTerm(h);
				if(cvterm.getBiologicalQualifierType()==libsbmlConstants.BQB_IS){ // 
					hasusableisann = true;

					for(int r=0; r<cvterm.getNumResources(); r++){

						MIRIAMannotation ma = collectMiriamAnnotation(cvterm.getResourceURI(r),
								resourcesandanns, ontologytermsandnamescache);
						// If we have a context for the annotation and a human-readable name
						if(ma.fulluri!=null){
							gotisannotation = true;
							resourcesandanns.put(cvterm.getResourceURI(r), ma);
							pproc = semsimmodel.getPhysicalProcessByReferenceURI(ma.fulluri);
							if(pproc == null){
								pproc = semsimmodel.addReferencePhysicalProcess(ma.fulluri, ma.rdflabel);
							}
							break;
						}
					}
				}
			}
			
			// If no usable annotations, or if we are online and couldn't get a usable annotation, create custom process
			if((!hasusableisann && !hasusableisversionofann) || 
					(isonline && ((hasusableisann && !gotisannotation) || (hasusableisversionofann && !gotisversionofannotation)
							|| (!hasusableisann && hasusableisversionofann && gotisversionofannotation)))){
				if(rxn.getName()!=null && !rxn.getName().equals("")){
					pproc = semsimmodel.addCustomPhysicalProcess(rxn.getName(), "Custom process: " + rxn.getName());
				}
				else{
					pproc = semsimmodel.addCustomPhysicalProcess(rxn.getId(), "Custom process: " + rxn.getId());
				}
				if(!isversionofmas.isEmpty()){
					MIRIAMannotation appliedann = isversionofmas.toArray(new MIRIAMannotation[]{})[0];
					semsimmodel.addReferencePhysicalProcess(appliedann.fulluri, appliedann.rdflabel);
					pproc.addReferenceOntologyAnnotation(SemSimConstants.BQB_IS_VERSION_OF_RELATION, appliedann.fulluri, appliedann.rdflabel);
				}
			}
			
			String ratecdwd = xxx(rxn.getId());
			if(semsimmodel.containsDataStructure(xxx(rxn.getId() + ".rate"))){
				ratecdwd = ratecdwd + ".rate";
			}
			DataStructure ds = semsimmodel.getDataStructure(ratecdwd);
			ds.getPhysicalProperty().addReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_00592"), "Chemical molar flow rate");
			ds.getPhysicalProperty().setPhysicalPropertyOf(pproc);
		}
		return true;
	}
	
	private static void annotateSpecies(Species species, String cdwd, PhysicalEntity pe, Model sbmlmodel, SemSimModel semsimmodel, boolean isonline,
			Map<Compartment,PhysicalEntity> compsandphysents, Map<Species,PhysicalEntity> speciesandphysents){
		
		// Exit routine if we're not online
		if(!isonline) return;
		
		DataStructure ds = semsimmodel.getDataStructure(cdwd);
		ReferenceOntologyAnnotation propann = null;
		if(!species.getHasOnlySubstanceUnits()){
			 propann = new ReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_00340"), "Chemical concentration");
		}
		else{
			propann = new ReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, URI.create(SemSimConstants.OPB_NAMESPACE + "OPB_00425"), "Chemical molar amount");
		}
		ds.getPhysicalProperty().addAnnotation(propann);

		ArrayList<PhysicalEntity> entlist = new ArrayList<PhysicalEntity>();
		ArrayList<StructuralRelation> relationlist = new ArrayList<StructuralRelation>();
		
		// Add the physical entity annotation components, if there is an annotated compartment for the species, add it, too
		Compartment cmptmnt = sbmlmodel.getCompartment(species.getCompartment());
		PhysicalEntity compent = compsandphysents.get(cmptmnt);  
		
		// set the composite entity annotation
		entlist.add(pe);
		entlist.add(compent);
		relationlist.add(SemSimConstants.PART_OF_RELATION);
		CompositePhysicalEntity propertytarget = semsimmodel.addCompositePhysicalEntity(entlist, relationlist);
		
		ds.getPhysicalProperty().setPhysicalPropertyOf(propertytarget);

		// Even if annotation has been used before, add the species and annotation to species and anns
		speciesandphysents.put(species, propertytarget);
	}
	

	public static void setFreeTextDefinitionsForDataStructuresAndSubmodels(SemSimModel themodel){		
		for(DataStructure ds : themodel.getDataStructures()){
			if(ds.getDescription()==null){
				
				// Maybe create multiple of these for species with multiple annotations?
				String freetext = null;
				
				if(ds.getPhysicalProperty().getPhysicalPropertyOf()!=null){
					PhysicalModelComponent pmc = ds.getPhysicalProperty().getPhysicalPropertyOf();
					freetext = ds.getPhysicalProperty().getFirstRefersToReferenceOntologyAnnotation().getValueDescription();
					freetext = freetext + " of ";
					if(pmc instanceof CompositePhysicalEntity){
						String compentname = ((CompositePhysicalEntity)pmc).makeName();
						((CompositePhysicalEntity)pmc).setName(compentname);
						freetext = freetext + compentname;
					}
					else if(pmc.hasRefersToAnnotation())
						freetext = freetext + pmc.getFirstRefersToReferenceOntologyAnnotation().getValueDescription();
					else
						freetext = freetext + "\"" + pmc.getName() + "\"";
				}
				ds.setDescription(freetext);
				
				// UNCOMMENT TO Create a custom accumulation process for the cdwd's time derivative
				if(ds.hasSolutionDomain()){
					if(themodel.getDataStructure(ds.getName() + ":" + ds.getSolutionDomain().getName())!=null){
						DataStructure tds = themodel.getDataStructure(ds.getName() + ":" + ds.getSolutionDomain().getName());
						if(ds.getDescription()!=null)
							tds.setDescription("Change in \"" + ds.getDescription().toLowerCase() + "\" with respect to " + ds.getSolutionDomain().getName());
					}
				}
			}
		}
		
		// Do submodels
		for(Submodel sub : themodel.getSubmodels()){
			if(sub.hasRefersToAnnotation()){
				ReferenceOntologyAnnotation refann = sub.getFirstRefersToReferenceOntologyAnnotation();
				sub.setDescription(refann.getValueDescription());
			}
		}
	}

	private static MIRIAMannotation collectMiriamAnnotation(String resource, Hashtable<String,MIRIAMannotation> resourcesandanns, 
			Hashtable<String, String[]> ontologytermsandnamescache){
		if(resourcesandanns.containsKey(resource)) {
			return resourcesandanns.get(resource);
		}
		return getMiriamAnnotation(resource, ontologytermsandnamescache);
	}
		
	
	private static MIRIAMannotation getMiriamAnnotation(String resource, 
			Hashtable<String, String[]> ontologytermsandnamescache){
				
		String rdflabel = null;
		
		if(ontologytermsandnamescache.containsKey(resource))
			rdflabel = ontologytermsandnamescache.get(resource)[0];
		
		return new MIRIAMannotation(resource, rdflabel);
	}
	
	// This compensates for JSim's renaming of species, reaction & compartment names that start with the character "_" 
	private static String xxx(String name){
		if(name.startsWith("_")) name = ("xxx" + name);
		if(name.toLowerCase().startsWith("js")) name = name.charAt(0) + "_" + name.substring(1, name.length());
		return name;
	}
}
