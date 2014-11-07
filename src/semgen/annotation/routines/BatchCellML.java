package semgen.annotation.routines;


import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import semgen.SemGenGUI;
import semgen.annotation.AnnotatorTab;
import semgen.annotation.dialog.textminer.TextMinerDialog;
import semgen.resource.file.SemGenOpenFileChooser;
import semsim.SemSimConstants;
import semsim.model.annotation.Annotation;
import semsim.reading.ModelClassifier;


public class BatchCellML{
	public AnnotatorTab ann;
	
	public Namespace docns = Namespace.getNamespace("http://cellml.org/tmp-documentation");
	public Namespace cellmlns = Namespace.getNamespace("http://www.cellml.org/cellml/1.0#");
	public Namespace cmetans = Namespace.getNamespace("cmeta", "http://www.cellml.org/metadata/1.0#");
	public Namespace rdfns = Namespace.getNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
	public Namespace bqsns = Namespace.getNamespace("bqs", "http://www.cellml.org/bqs/1.0#");
	public int numidsfromrdf = 0;
	
	public BatchCellML() throws Exception{
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setCurrentDirectory(SemGenOpenFileChooser.currentdirectory);
		fc.setDialogTitle("Select directory containing cellml models");
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = new File(fc.getSelectedFile().getAbsolutePath());
			SemGenOpenFileChooser.currentdirectory = fc.getCurrentDirectory();
			if (file != null) {
				processmodels(file);
				JOptionPane.showMessageDialog(null, "Finished processing CellML models");
			}
		}
	}
	
	
	public void processmodels(File file) throws Exception{
		File[] cellmlfiles = file.listFiles();
		String lastpubmedid = "";
		String lastabstract = "";
		
		for(int x=0; x<cellmlfiles.length; x++){
			File cellmlfile = cellmlfiles[x];
	        
			if(cellmlfile.getAbsolutePath().toLowerCase().endsWith(".cellml")){
				System.out.println("Processing " + cellmlfile.getName());
				
				try {
					// Make this into a task
					ann = SemGenGUI.AnnotateAction(cellmlfile, true);
					TextMinerDialog tmd = new TextMinerDialog(ann);
					
					tmd.pubmedid = getModelIDAndPubMedIdFromCellMLModel(cellmlfile);
					// If we don't have a proper pubmed ID
					if(tmd.pubmedid.matches(".*\\D.*") || tmd.pubmedid.equals("") || tmd.pubmedid.matches("\\s")){
						System.out.println("PubMed ID not formatted properly: " + tmd.pubmedid);
						tmd.inputtextarea.setText(scrapeParaTags(cellmlfile));
						// If the scraping actually gave us text, annotate it and collect the ontology terms
						if(!tmd.inputtextarea.getText().trim().equals("") && tmd.inputtextarea.getText()!=null){
							tmd.parse();
							tmd.collect();	
							lastpubmedid = tmd.pubmedid;
							lastabstract = tmd.inputtextarea.getText();
						}
					}
					// If we DO have a proper pubmed ID
					else{
						Annotation dpa = new Annotation(SemSimConstants.REFERENCE_PUBLICATION_PUBMED_ID_RELATION, tmd.pubmedid);
						ann.semsimmodel.addAnnotation(dpa);
						// If ID is same as last model, use it
						if(tmd.pubmedid.equals(lastpubmedid)){
							tmd.inputtextarea.setText(lastabstract);
						}
						else{
							tmd.findpubmedabstract();
						}
						
						// If there's a pubmed id AND abstract text
						if(!tmd.inputtextarea.getText().equals("")){
							ann.semsimmodel.addAnnotation(new Annotation(SemSimConstants.REFERENCE_PUBLICATION_ABSTRACT_TEXT_RELATION, tmd.inputtextarea.getText()));
						}
						// If there's a pubmed id but no abstract, do a scrape of the <para> tags in the cellml file
						else{
							tmd.inputtextarea.setText(scrapeParaTags(cellmlfile));
						}
						if(!tmd.inputtextarea.getText().trim().equals("") && tmd.inputtextarea.getText()!=null){
							// Parse the text, collect the ontology terms
							tmd.parse();
							tmd.collect();	
							lastpubmedid = tmd.pubmedid;
							lastabstract = tmd.inputtextarea.getText();
						}
					}
					
					tmd.setVisible(false);
					SemGenGUI.SaveAction(ann, ModelClassifier.CELLML_MODEL);
					SemGenGUI.closeTabAction(ann);
				} catch (IOException | JDOMException e) {
					e.printStackTrace();
				} 
			}
		}
		System.out.println(numidsfromrdf + " models had pubmed ids in their rdf tags");
	}
	
	public String getModelIDAndPubMedIdFromCellMLModel(File cellmlfile){
		String ab = "";
		SAXBuilder builder = new SAXBuilder();
		try {
			Document doc = builder.build(cellmlfile);
			
			if(doc.getRootElement()!=null){
				System.out.println("Root " + doc.getRootElement().toString());
				String name = doc.getRootElement().getAttributeValue("name");
				String id = doc.getRootElement().getAttribute("id",cmetans).getValue();
				System.out.println("ID: " + id);
				System.out.println(ann.manager.toString());
				ann.semsimmodel.addAnnotation(new Annotation(SemSimConstants.MODEL_ID_RELATION, id));
				ann.semsimmodel.addAnnotation(new Annotation(SemSimConstants.MODEL_NAME_RELATION, name));
				
				// Try to get pubmed ID from RDF tags
				if(doc.getRootElement().getChild("RDF",rdfns).getChildren("Description", rdfns)!=null){
					System.out.println("RDF");
					Iterator<Element> descit = doc.getRootElement().getChild("RDF",rdfns).getChildren("Description",rdfns).iterator();
					while(descit.hasNext()){
						Element nextdesc = descit.next();
						if(nextdesc.getChildren("reference", bqsns)!=null){
							Iterator<Element> refit = nextdesc.getChildren("reference", bqsns).iterator();
							while(refit.hasNext()){
								Element nextref = refit.next();
								if(nextref.getChild("Pubmed_id", bqsns)!=null){
									String pubmedid = nextref.getChild("Pubmed_id", bqsns).getText();
									if(!pubmedid.equals("") && pubmedid!=null){
										System.out.println("pubmed id from RDF: " + pubmedid);
										numidsfromrdf++;
										return pubmedid;
									}
								}
							}
						}
					}
				}
				
				
				// Try to get ID from documentation tags if not in RDF
				if(doc.getRootElement().getChild("documentation",docns)!=null){
					System.out.println("documentation");
					if(doc.getRootElement().getChild("documentation",docns).getChild("article",docns)!=null){
						System.out.println("article");
						if(doc.getRootElement().getChild("documentation",docns).getChild("article",docns).getChildren("sect1",docns)!=null){
							Iterator<Element> sect1it = doc.getRootElement().getChild("documentation",docns).getChild("article",docns).getChildren("sect1",docns).iterator();
							while(sect1it.hasNext()){
								Element nextsect1 = sect1it.next();
								if(nextsect1.getAttributeValue("id").equals("sec_structure")){
									System.out.println("sect1 sec sructure");
									if(nextsect1.getChildren("para",docns)!=null){
										Iterator<Element> it = nextsect1.getChildren("para",docns).iterator();
										System.out.println("paras");
										while(it.hasNext()){
											Element nextel = it.next();
											System.out.println("one para " + nextel.getText());
											if(nextel.getChildren("ulink",docns).size()!=0){
												System.out.println("ulink");
												Iterator<Element> ulinkit = nextel.getChildren("ulink",docns).iterator();
												while(ulinkit.hasNext()){
													Element nextulink = ulinkit.next();
													if(nextulink.getText().toLowerCase().contains("pubmed id") || nextulink.getText().toLowerCase().contains("pubmedid")){
														ab = nextulink.getText();
														ab = ab.substring(ab.indexOf(":") + 1, ab.length()).trim();
														System.out.println("ab: " + ab);
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
				// end documentation processing
			}
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		} 
		System.out.println("pubmed id: " + ab);
		return ab;
	}
	
	
	public String scrapeParaTags(File cellmlfile) throws JDOMException, IOException{
		String text = "";
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(cellmlfile);
		if(doc.getRootElement().getChild("documentation",docns)!=null){
			System.out.println("documentation");
			if(doc.getRootElement().getChild("documentation",docns).getChild("article",docns)!=null){
				System.out.println("article");
				if(doc.getRootElement().getChild("documentation",docns).getChild("article",docns).getChildren("sect1",docns)!=null){
					Iterator<Element> sect1it = doc.getRootElement().getChild("documentation",docns).getChild("article",docns).getChildren("sect1",docns).iterator();
					while(sect1it.hasNext()){
						Element nextsect1 = sect1it.next();
						if(nextsect1.getAttributeValue("id").equals("sec_structure")){
							System.out.println("sect1 sec sructure");
							if(nextsect1.getChildren("para",docns)!=null){
								Iterator<Element> it = nextsect1.getChildren("para",docns).iterator();
								System.out.println("paras");
								while(it.hasNext()){
									text = text + " " + it.next().getText();
								}
							}
						}
					}
				}
			}
		}
		System.out.println("Scraped text: " + text);
		return text;
	}
}
