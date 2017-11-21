package semsim.reading;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

import semsim.reading.ModelClassifier.ModelType;

public class OMEXAccessor extends ModelAccessor {
	
	protected ModelAccessor archivedfile;
	protected ZipFile archive;
	
	public OMEXAccessor(File omexarchive, File file, ModelType type) {
		super();
		filepath = omexarchive.getPath();
		archivedfile = new ModelAccessor(file, type);
	}
	
	public OMEXAccessor(File omexarchive, File file, String fragment) {
		super();
		filepath = omexarchive.getPath();
		archivedfile = new ModelAccessor(file, fragment);
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
		
		ZipEntry entry = archive.getEntry(path);
		return archive.getInputStream(entry);
		
	}
	
	@Override
	public String getModelasString() throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(modelInStream(), writer, Charset.defaultCharset());
		return writer.toString();
	}
	
	public String getName() {
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
	
	public ModelType getFileType() {
		return ModelType.OMEX_ARCHIVE;
	}
	
}
