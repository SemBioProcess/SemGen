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

/**
 * Class for determining the format in which models and associated files are stored 
 * @author mneal
 *
 */
public class ModelClassifier {
	
	/**
	 * Enum of the different file types supported by SemSim
	 * @author mneal
	 */
	public static enum ModelType{
		SEMSIM_MODEL(".owl", new String[]{"http://www.bhi.washington.edu/semsim"}, new FileNameExtensionFilter("SemSim (*.owl)", "owl")), 
		SBML_MODEL(".sbml", new String[]{"https://identifiers.org/combine.specifications/sbml"}, new FileNameExtensionFilter("SBML (*.sbml, *.xml)", "sbml", "xml", "omex")), 
		CELLML_MODEL(".cellml", new String[]{"https://identifiers.org/combine.specifications/cellml"},new FileNameExtensionFilter("CellML (*.cellml, *.xml)", "cellml", "xml", "omex")), 
		MML_MODEL(".m", new String[]{"MML"},new FileNameExtensionFilter("MML (*.mod)", "mod")), 
		MML_MODEL_IN_PROJ(".proj",  new String[]{"proj"},new FileNameExtensionFilter("JSim project file model (*.proj)", "proj")), 
		OMEX_ARCHIVE(".omex", new String[]{"https://identifiers.org/combine.specifications/omex"},new FileNameExtensionFilter("Combine Archive (*.omex)", "omex")), 
		CASA_FILE(".casa", new String[]{"https://identifiers.org/combine.specifications/omex-metadata"},null),
		UNKNOWN("null", new String[]{"null/null"}, null);
		
		private String extension;
		private String[] format;
		private FileFilter filefilter;
		
		ModelType(String ext, String[] format, FileNameExtensionFilter filter) {
			extension = ext;
			this.format = format;
			filefilter = filter;
		}
		
		/** @return The extension of the ModelType */
		public String getExtension() {
			return extension;
		}
		
		/** @return The format of the ModelType (for use in OMEX manifest files)*/
		public String getFormat() {
			return format[0];
		}
		
		/**
		 * @param teststring An input String
		 * @return Whether the input String matches the format of the instantiated
		 * ModelType
		 */
		public boolean matchesFormat(String teststring) {
			for (String frmt : format) {
				if (teststring.contains(frmt)) return true;
			}
			return false;
			
		}
		
		/** @return FileFilter of the ModelType */
		public FileFilter getFileFilter() {
			return filefilter;
		}
		
		/**
		 * @param tomatch An input FileFilter
		 * @return Whether an input FileFilter matches the FileFilter
		 * for the instantiated ModelType
		 */
		public boolean fileFilterMatches(FileFilter tomatch) {
			if(filefilter != null)
				return filefilter.equals(tomatch);
			else return false;
		}
	}
	
	
	/**
	 * Classify the model referred to by a {@link ModelAccessor} location
	 * @param accessor A {@link ModelAccessor}
	 * @return The ModelType of the model referred to by the input {@link ModelAccessor}
	 * @throws JDOMException
	 * @throws IOException
	 */
	public static ModelType classify(ModelAccessor accessor) throws JDOMException, IOException{
		return classify(accessor.getFile());
	}
	
	
	/**
	 * Classify a model referred to by a File
	 * @param file An input file location
	 * @return The ModelType of the file
	 * @throws JDOMException
	 * @throws IOException
	 */
	public static ModelType classify(File file) throws JDOMException, IOException {
		return classify(file.getPath());
	}
	
	/**
	 * Return the type of the model based on the file extension
	 * @param file A file path
	 * @return Model type based on file extension
	 * @throws IOException 
	 * @throws JDOMException 
	 */
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
	 * Verifies whether the indicated document is well-formed SBML
	 * @param file An input File
	 * @return Whether the input file is well-formed SBML
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
	 * Verifies whether the indicated document is well-formed CellML
	 * @param file An input File
	 * @return Whether the File is a well-formed CellML model
	 * @throws JDOMException
	 * @throws IOException
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
	
	
	/**
	 * Verify that a given file name ends with an extension that has a corresponding
	 * OMEX format identifier
	 * @param filename A filename
	 * @param format An OMEX format (e.g. "https://identifiers.org/combine.specifications/sbml")
	 * @return Whether the extension on the file name is associated with the input OMEX format
	 */
	public static boolean hasValidFileExtension(String filename, String format) {
		for (ModelType type : ModelType.values()) {
			if (filename.endsWith(".xml")) {
				if (type.matchesFormat(format)) return true;
			}
					
			else if ( filename.endsWith(type.extension)) return true;
		}
		
		return false;
	}
	
	
	/**
	 * @param formatstring An OMEX file format (e.g. "https://identifiers.org/combine.specifications/sbml")
	 * @return The ModelType associated with the input format
	 */
	public static ModelType getTypebyFormat(String formatstring) {
		for (ModelType type : ModelType.values()) {
			if (type.matchesFormat(formatstring)) return type;
		}
		return ModelType.UNKNOWN;
	}

	
	/**
	 * @param filter A FileFilter
	 * @return The ModelType associated with the input FileFilter
	 */
	public static ModelType getTypebyFilter(FileFilter filter) {
		for (ModelType type : ModelType.values()) {
			if (type.fileFilterMatches(filter)) return type;
		}
		return ModelType.UNKNOWN;
	}
	
	
	/**
	 * @param extension A file extension (e.g. ".sbml")
	 * @return The ModelType associated with the input file extension
	 */
	public static ModelType getTypebyExtension(String extension) {
		for (ModelType type : ModelType.values()) {
			if (type.extension.matches(extension)) return type;
		}
		return ModelType.UNKNOWN;
	}
	
	
	/**
	 * @param format An OMEX file format
	 * @return Whether the input file format is a valid OMEX computational model file format
	 */ 
	public static boolean hasValidOMEXmodelFileFormat(String format) {
		return format.matches(".*/sbml.*$") || format.endsWith("cellml") || ModelType.SEMSIM_MODEL.matchesFormat(format);
	}
	
	
	/**
	 * @param format An OMEX file format
	 * @return Whether the input file format is a valid OMEX annotation file format
	 */
	public static boolean hasValidOMEXannotationFileFormat(String format) {
		return format.endsWith("casa") || 
				format.equals("http://identifiers.org/combine.specifications/omex-metadata") || 
				format.equals("https://identifiers.org/combine.specifications/omex-metadata");
	}
}
