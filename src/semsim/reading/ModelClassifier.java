package semsim.reading;

import java.io.File;
import java.io.IOException;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.JDOMParseException;
import org.jdom.input.SAXBuilder;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;

import semsim.fileaccessors.ModelAccessor;

public class ModelClassifier {
	
	public static enum ModelType{
		SEMSIM_MODEL(".owl", new String[]{}, new FileNameExtensionFilter("SemSim (*.owl)", "owl")), 
		SBML_MODEL(".sbml", new String[]{"http://identifiers.org/combine.specifications/sbml.level-2.version-1"}, new FileNameExtensionFilter("SBML (*.sbml, *.xml)", "sbml", "xml")), 
		CELLML_MODEL(".cellml", new String[]{"http://identifiers.org/combine.specifications/cellml"},new FileNameExtensionFilter("CellML (*.cellml, *.xml)", "cellml", "xml")), 
		MML_MODEL(".m", new String[]{},new FileNameExtensionFilter("MML (*.mod)", "mod")), 
		MML_MODEL_IN_PROJ(".proj",  new String[]{},new FileNameExtensionFilter("JSim project file model (*.proj)", "proj")), 
		OMEX_ARCHIVE(".omex", new String[]{"http://identifiers.org/combine.specifications/omex"},new FileNameExtensionFilter("Combine Archive (*.omex)", "omex")), 
		CASA_FILE(".casa", new String[]{},null),
		UNKNOWN("null", new String[]{"null/null"}, null);
		
		private String extension;
		private String[] format;
		private FileFilter filefilter;
		ModelType(String ext, String[] format, FileNameExtensionFilter filter) {
			extension = ext;
			this.format = format;
			filefilter = filter;
		}
		
		public String getExtension() {
			return extension;
		}
		
		public boolean matchesFormat(String teststring) {
			for (String frmt : format) {
				if (frmt.matches(teststring)) return true;
			}
			return false;
			
		}
		
		public FileFilter getFileFilter() {
			return filefilter;
		}
		
		public boolean fileFilterMatches(FileFilter tomatch) {
			return filefilter.equals(tomatch);
		}
	}
	
	public static ModelType classify(ModelAccessor accessor) throws JDOMException, IOException{
		return classify(accessor.getFile());
	}
	
	
	public static ModelType classify(File file) throws JDOMException, IOException {
		return classify(file.getPath());
	}
	
	/**
	 * Return the type of the model based on the file extension
	 * @throws IOException 
	 * @throws JDOMException 
	 **/
	public static ModelType classify(String file) throws JDOMException, IOException{
		ModelType type = ModelType.UNKNOWN;
				if (file.toLowerCase().endsWith(".omex")){
					type = ModelType.OMEX_ARCHIVE;
				}
				else if(file.toLowerCase().endsWith(".proj")){
					type = ModelType.MML_MODEL_IN_PROJ;
				}
				else if (file.toLowerCase().endsWith(".mod")){
					type = ModelType.MML_MODEL;
				}
				else if (file.toLowerCase().endsWith(".owl")) {
					type =  ModelType.SEMSIM_MODEL;
				}
				else if(file.toLowerCase().endsWith(".cellml")){
					type =  ModelType.CELLML_MODEL;
				}
				else if(file.toLowerCase().endsWith(".xml") || file.endsWith(".sbml")){
					type =  ModelType.SBML_MODEL;
				}
				else if(file.toLowerCase().endsWith(".rdf")){
					type = ModelType.CASA_FILE;
				}

		return type;
	}
	
	/**
	 * Verifies the the indicated document is well-formed SBML
	 * @param file to check
	 * @return
	 */
	
	public static Boolean isValidSBML(File file){
		System.out.println("Testing SBML validity");
		try{
			SBMLDocument sbmldoc = SBMLReader.read(file);
			if (sbmldoc.getNumErrors()>0){
			      System.err.println(file.getName() + " is not valid SBML");
			      sbmldoc.printErrors(System.err);
			}
			else {
				return true;
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Verifies the the indicated document is well-formed CellML
	 * @param file to check
	 * @return
	 */
	public static Boolean isValidCellMLmodel(File file) throws JDOMException, IOException{
		SAXBuilder builder = new SAXBuilder();
		try{
			Document doc = builder.build(file);
			String nsuri = doc.getRootElement().getNamespace().getURI();
			if(nsuri.contains("http://www.cellml.org/cellml/")){
				return true;
			}
		}
		catch(JDOMParseException e){
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean hasValidFileExtension(String filename, String format) {
		for (ModelType type : ModelType.values()) {
			if (filename.endsWith(".xml")) {
				if (type.matchesFormat(format)) return true;
			}
					
			else if ( filename.endsWith(type.extension)) return true;
		}
		
		return false;
	}
	
	public static ModelType getTypebyFormat(String formatstring) {
		for (ModelType type : ModelType.values()) {
			if (type.matchesFormat(formatstring)) return type;
		}
		return ModelType.UNKNOWN;
	}

	public static ModelType getTypebyFilter(FileFilter filter) {
		for (ModelType type : ModelType.values()) {
			if (type.fileFilterMatches(filter)) return type;
		}
		return ModelType.UNKNOWN;
	}
	
	public static ModelType getTypebyExtension(String extension) {
		for (ModelType type : ModelType.values()) {
			if (type.extension.matches(extension)) return type;
		}
		return ModelType.UNKNOWN;
	}
	
	public static boolean hasValidOMEXmodelFileFormat(String format) {
		return format.matches(".*/sbml.*$") || format.endsWith("cellml");
	}
	
	public static boolean hasValidOMEXannotationFileFormat(String format) {
		return format.endsWith("rdf+xml");
	}
}
