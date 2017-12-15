package semsim.writing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import semsim.fileaccessors.OMEXAccessor;

public class OMEXArchiveWriter {
	ModelWriter writer;
	
	public OMEXArchiveWriter(ModelWriter writer) {
		this.writer = writer;
	}

	public void appendOMEXArchive(OMEXAccessor archive) {
        String inputFileName = archive.getFilePath();
        try {
			//FileOutputStream outstream = new FileOutputStream(archive.getFile());
			//writer.writeToStream(outstream);
        	
	        //ZipOutputStream zipstream = new ZipOutputStream(outstream);
	        Map<String, String> env = new HashMap<>(); 
	        env.put("create", "true");
	        Path path = Paths.get(archive.getDirectoryPath());
	        URI uri = URI.create("jar:" + path.toUri());
	        
	        FileSystem fs = FileSystems.newFileSystem(uri, env);
	            //Path nf = fs.getPath("new.txt");
	            	Writer zwriter = Files.newBufferedWriter(fs.getPath(archive.getFileName()), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
	                zwriter.write(writer.encodeModel());

	        }
			 catch (IOException e1) {
					e1.printStackTrace();
				}
	        
//	        ZipFile zip = new ZipFile(archive.getDirectoryPath());
//	        
//	        // first, copy contents from existing war
//	        Enumeration<? extends ZipEntry> entries = zip.entries();
//	        while (entries.hasMoreElements()) {
//	            ZipEntry e = entries.nextElement();
//	            System.out.println("copy: " + e.getName());
//	            zipstream.putNextEntry(e);
//	            if (!e.isDirectory()) {
//	                //copy(war.getInputStream(e), append);
//	            }
//	            zipstream.closeEntry();
//	        }

//	        
//	        try (FileInputStream inputStream = new FileInputStream(inputFileName)) {
//	
//	            // create a new ZipEntry, which is basically another file
//	            // within the archive. We omit the path from the filename
//	            ZipEntry entry = archive.getEntry();
//	            entry.setTime(archive.getFile().lastModified());
//	            entry.setComment("Created by SemGen");
//	            zipstream.putNextEntry(entry);
//	//
//	//            //LOG.info("Generated new entry for: " + inputFileName);
//	//
//	//            // Now we copy the existing file into the zip archive. To do
//	//            // this we write into the zip stream, the call to putNextEntry
//	//            // above prepared the stream, we now write the bytes for this
//	//            // entry. For another source such as an in memory array, you'd
//	//            // just change where you read the information from.
//	            byte[] readBuffer = new byte[2048];
//	            int amountRead;
//	            int written = 0;
//	
//	            while ((amountRead = inputStream.read(readBuffer)) > 0) {
//	                zipstream.write(readBuffer, 0, amountRead);
//	                written += amountRead;
//	            }
//	
//	            //LOG.info("Stored " + written + " bytes to " + inputFileName);
//
//	            zipstream.close();
//	        }
//	        catch(IOException e) {
//	            throw new ZipParsingException("Unable to process " + inputFileName, e);
//	        }
//        

	}
	
    private void createArchive(OMEXAccessor archive) {
        // the directory to be zipped

        // the zip file name that we will create
        File zipFileName = archive.getFile();

        // open the zip stream in a try resource block, no finally needed
        try( ZipOutputStream zipStream = new ZipOutputStream(
                        new FileOutputStream(zipFileName)) ) {
//
            // traverse every file in the selected directory and add them
            // to the zip file by calling addToZipFile(..)
           // DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory);
           // dirStream.forEach(path -> addToZipFile(path, zipStream));

            //LOG.info("Zip file created in " + directory.toFile().getPath());
        }
        catch(IOException|ZipParsingException e) {
            //LOG.log(Level.SEVERE, "Error while zipping.", e);
        }
    }

    
    /**
     * We want to let a checked exception escape from a lambda that does not
     * allow exceptions. The only way I can see of doing this is to wrap the
     * exception in a RuntimeException. This is a somewhat unfortunate side
     * effect of lambda's being based off of interfaces.
     */
    private class ZipParsingException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public ZipParsingException(String reason, Exception inner) {
            super(reason, inner);
        }
    }

}
