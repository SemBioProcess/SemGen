package semsim.fileaccessors;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.jdom.Document;
import org.jdom.JDOMException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Resource;

import semgen.SemGen;
import semsim.SemSimLibrary;
import semsim.model.collection.SemSimModel;
import semsim.reading.AbstractRDFreader;
import semsim.reading.CASAreader;
import semsim.reading.ModelClassifier;
import semsim.reading.ModelReader;
import semsim.reading.OMEXManifestreader;
import semsim.reading.SemSimRDFreader;
import semsim.reading.ModelClassifier.ModelType;
import semsim.writing.CellMLwriter;
import semsim.writing.MMLwriter;
import semsim.writing.ModelWriter;
import semsim.writing.SBMLwriter;
import semsim.writing.SemSimOWLwriter;
import semsim.writing.SemSimRDFwriter;

public class ModelAccessor {

	protected String filepath;
	protected File file;
	protected String fragment = "";

	public static final String separator = "#";
	protected ModelType modeltype;	

	// Use this constructor for models that are stored as standalone files
	protected ModelAccessor(File standAloneFile){
			try {
				modeltype = ModelClassifier.classify(standAloneFile);
			} catch (JDOMException | IOException e) {
				e.printStackTrace();
			}
			filepath = standAloneFile.getPath();
			file = new File(filepath);
	}
	
	// Use this constructor for models that are stored as standalone files
	protected ModelAccessor(File standAloneFile, ModelType type){
			modeltype = type;
			filepath = standAloneFile.getPath();
			file = standAloneFile;
	}
	
	// Use this constructor for models that are stored as standalone files
	protected ModelAccessor(File standAloneFile, String frag, ModelType type){
			modeltype = type;
			fragment = frag;
			filepath = standAloneFile.getPath();
			file = standAloneFile;
	}
	
	// Copy constructor
	public ModelAccessor(ModelAccessor matocopy) {
		filepath = matocopy.filepath;
		file = new File(matocopy.file.toString());
		modeltype = matocopy.modeltype;
	}

	public String getFilePath(){
		return new String(filepath);
	}
	
	public String getFullPath() {
		if (!fragment.isEmpty()) return new String(filepath + separator + fragment);
		return new String(filepath);
	}

	// This returns the archive uri if the model is part of an archive, 
	// or returns the standalone uri if the model is in a standalone file.
	public URI getFileThatContainsModelAsURI(){
		
		return file.toURI();
	}
	
	public String getDirectoryPath() {
		return file.getParent();
	}
	
	public boolean isLocalFile(){
		if(modelIsOnline()) return false;
		return existingModel();
	}
	
	public File getFile() {
		return new File(filepath);
	}

	public boolean modelIsOnline(){
		return filepath.startsWith("http");
	}
	
	public boolean existingModel() {
		return file.exists();
	}
	
	public String getModelasString() throws IOException {
		StringWriter writer = new StringWriter();
		InputStream instream = modelInStream();
		IOUtils.copy(instream, writer, Charset.defaultCharset());
		return writer.toString();
	}

	// Retrieve model text as a string (only for locally-stored models)
	public InputStream modelInStream() throws IOException{
		InputStream returnstring = null;
		
		if(modelIsOnline()) return null;
		else {
			try {
				returnstring = new FileInputStream(file);
			} 
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
			
		return returnstring;
	}
	
		protected ModelWriter makeWriter(SemSimModel semsimmodel) {
			
			ModelWriter writer = null;
			try {
				if(modeltype==ModelType.SEMSIM_MODEL) {
					writer = new SemSimOWLwriter(semsimmodel);
				}
				else if(modeltype==ModelType.SBML_MODEL){
					writer = new SBMLwriter(semsimmodel);
				}
				else if(modeltype==ModelType.CELLML_MODEL){
					writer = new CellMLwriter(semsimmodel);
				}
				else if(modeltype==ModelType.MML_MODEL){
					writer = new MMLwriter(semsimmodel);
				}
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
			return writer;
			
	}

		public void writetoFile(SemSimModel model) {
			ModelWriter writer = makeWriter(model);
			FileOutputStream outstream;
			try {
				outstream = new FileOutputStream(getFile());
				writer.writeToStream(outstream);
				PrintWriter pwriter = new PrintWriter(new FileWriter(file));
				pwriter.print(outstream);
				pwriter.close();
			} catch (IOException e) {
				e.printStackTrace();
				SemGen.logfilewriter.println(getShortLocation() + " write failed.");
			}
			
			SemGen.logfilewriter.println(getShortLocation() + " was saved");
			
		}


	public String getFileName(){
			return file.getName();
	}
	
	public Document getJDOMDocument() {
		return ModelReader.getJDOMdocumentFromFile(file);
	}
	
	// If the model is in a standalone file, the name of the file is returned
	// otherwise a string with format [name of archive] > [name of model] is returned
	public String getShortLocation(){		
		return new File(this.filepath).getName();
	}
		
	public ModelType getModelType() {
		return modeltype;
	}
	
	public boolean sharesDirectory(String name) {
		File samedirfile = new File (
				getDirectoryPath() + 
				"/" + name);
		
		return samedirfile.exists();
	}
	
	
	public boolean equals(ModelAccessor ma){
		return getFile().getPath().contentEquals(ma.getFile().getPath());
	}
	
	public CASAreader getAssociatedCASAFile() throws ZipException, IOException, JDOMException {
		return null;
	}
	
	public String getModelName() {
		String filename = file.getName();
		return filename.substring(0, filename.indexOf('.'));
	}

	public AbstractRDFreader getRDFreaderForModel(SemSimModel thesemsimmodel, String curationalrdf, SemSimLibrary sslib) 
			throws ZipException, IOException, JDOMException{
			
			ZipFile archive = new ZipFile(file);
	
			ArrayList<ModelAccessor> accs = OMEXManifestreader.getAnnotationFilesInArchive(archive, file);
			
			for(ModelAccessor acc : accs){
				
				if(acc.getModelType()==ModelType.CASA_FILE){
					
				    ZipEntry entry = archive.getEntry(acc.getFileName());
			        InputStream stream = archive.getInputStream(entry);
	
			        Model casardf = ModelFactory.createDefaultModel();
			        
			        RDFReader casardfreader = casardf.getReader();
					casardfreader.setProperty("relativeURIs","same-document,relative");
					casardfreader.read(casardf, stream, AbstractRDFreader.TEMP_NAMESPACE);
						
					Resource casamodelres = casardf.getResource(AbstractRDFreader.TEMP_NAMESPACE + "#" + getFileName());
					
					if(casardf.containsResource(casamodelres)){
						
						Model curationalrdfmodel = ModelFactory.createDefaultModel();
	
				        if(curationalrdf != null && ! curationalrdf.equals("")){
				        	RDFReader curationalrdfreader = curationalrdfmodel.getReader();
				        	curationalrdfreader.setProperty("relativeURIs","same-document,relative");
				        	InputStream curationalrdfstream = new ByteArrayInputStream(curationalrdf.getBytes());
				        	curationalrdfreader.read(casardf, curationalrdfstream, AbstractRDFreader.TEMP_NAMESPACE);
				        }
				        
				        curationalrdfmodel = AbstractRDFreader.stripSemSimRelatedContentFromRDFblock(curationalrdfmodel, thesemsimmodel); // when read in rdfblock in CellML file there may be annotations that we want to ignore
						casardf.add(curationalrdfmodel.listStatements()); // Add curatorial statements to rdf model. When instantiate CASA reader, need to provide all RDF statements as string.
						
						String combinedrdf = SemSimRDFwriter.getRDFmodelAsString(casardf);
						return new CASAreader(acc, thesemsimmodel, sslib, combinedrdf);
				    }
				}
			}
	
			archive.close();
		
		return new SemSimRDFreader(this, thesemsimmodel, curationalrdf, sslib);
		
	}
}
