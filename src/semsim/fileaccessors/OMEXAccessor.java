package semsim.fileaccessors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import semsim.reading.ModelClassifier.ModelType;

public class OMEXAccessor extends ModelAccessor {
	
	protected ModelAccessor archivedfile;
	protected ZipFile archive;
	
	public OMEXAccessor(File omexarchive, File file, ModelType type) {
		super(omexarchive, ModelType.OMEX_ARCHIVE);
		this.file = omexarchive;
		archivedfile = new ModelAccessor(file, type);
	}
	
	public OMEXAccessor(File omexarchive, File file, String fragment) {
		super(omexarchive, ModelType.OMEX_ARCHIVE);
		archivedfile = new JSIMProjectAccessor(file, fragment);
	}

	// Copy constructor
	public OMEXAccessor(OMEXAccessor matocopy) {
		super(matocopy);

		archivedfile = new ModelAccessor(matocopy);
	}
	
	@Override
	public InputStream modelInStream() throws IOException {
		archive = new ZipFile(filepath);
		String path = archivedfile.getFilePath().replace('\\', '/');
		Enumeration<? extends ZipEntry> entries = archive.entries();

		while (entries.hasMoreElements()) {
			ZipEntry current = entries.nextElement();
			System.out.println(current.getName());
		}
		path = path.substring(2, path.length());
		ZipEntry entry = archive.getEntry(path);
		return archive.getInputStream(entry);
		
	}
	
	@Override
	public String getModelasString() throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(modelInStream(), writer, Charset.defaultCharset());
		return writer.toString();
	}
	
	public Document getJDOMDocument() {		
		Document doc = null;
		try{ 
			InputStream instream = modelInStream();
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(instream);
		}
		catch(JDOMException | IOException e) {
			e.printStackTrace();
		}
		return doc;
	}
	
	public String getModelName() {
		return archivedfile.getFileName();
	}
	
	public void closeStream() {		
		try {
			if (archive != null) {
				archive.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// If the model is in a standalone file, the name of the file is returned
	// otherwise a string with format [name of archive] > [name of model] is returned
	public String getShortLocation(){
		
		return getFileName() + '>'  + getModelName();

	}
	
	public String getDirectoryPath() {
		return file.getPath();
	}
	
	public ZipEntry getEntry() {
		return new ZipEntry(file.getPath());
	}
	
	public ModelType getModelType() {
		return archivedfile.getModelType();
	}
	
}
