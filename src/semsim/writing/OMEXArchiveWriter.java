package semsim.writing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import semsim.fileaccessors.OMEXAccessor;

public class OMEXArchiveWriter {
	ModelWriter writer;
	
	public OMEXArchiveWriter(ModelWriter writer) {
		this.writer = writer;
	}

	public void appendArchive(OMEXAccessor archive) {
        String inputFileName = archive.getFilePath();
        //ZipOutPutStream stream = archive.
        try (FileInputStream inputStream = new FileInputStream(inputFileName)) {

            // create a new ZipEntry, which is basically another file
            // within the archive. We omit the path from the filename
//            ZipEntry entry = new ZipEntry(archive.get);
//            entry.setCreationTime(FileTime.fromMillis(file.toFile().lastModified()));
//            entry.setComment("Created by TheCodersCorner");
//            zipStream.putNextEntry(entry);
//
//            //LOG.info("Generated new entry for: " + inputFileName);
//
//            // Now we copy the existing file into the zip archive. To do
//            // this we write into the zip stream, the call to putNextEntry
//            // above prepared the stream, we now write the bytes for this
//            // entry. For another source such as an in memory array, you'd
//            // just change where you read the information from.
//            byte[] readBuffer = new byte[2048];
//            int amountRead;
//            int written = 0;
//
//            while ((amountRead = inputStream.read(readBuffer)) > 0) {
//                //zipStream.write(readBuffer, 0, amountRead);
//                written += amountRead;
//            }

            //LOG.info("Stored " + written + " bytes to " + inputFileName);


        }
        catch(IOException e) {
            throw new ZipParsingException("Unable to process " + inputFileName, e);
        }
	}
	
    private void createArchive(String dirName) {
        // the directory to be zipped
        Path directory = Paths.get(dirName);

        // the zip file name that we will create
//        File zipFileName = Paths.get(OUTPUT_ZIP).toFile();
//
//        // open the zip stream in a try resource block, no finally needed
//        try( ZipOutputStream zipStream = new ZipOutputStream(
//                        new FileOutputStream(zipFileName)) ) {
//
//            // traverse every file in the selected directory and add them
//            // to the zip file by calling addToZipFile(..)
//            DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory);
//            dirStream.forEach(path -> addToZipFile(path, zipStream));
//
//            //LOG.info("Zip file created in " + directory.toFile().getPath());
//        }
//        catch(IOException|ZipParsingException e) {
//            //LOG.log(Level.SEVERE, "Error while zipping.", e);
//        }
    }
    
    private void addToOMEXFile(Path file, ZipOutputStream zipStream) {
        String inputFileName = file.toFile().getPath();
        try (FileInputStream inputStream = new FileInputStream(inputFileName)) {

            // create a new ZipEntry, which is basically another file
            // within the archive. We omit the path from the filename
            ZipEntry entry = new ZipEntry(file.toFile().getName());
            //entry.setCreationTime(FileTime.fromMillis(file.toFile().lastModified()));
            entry.setComment("Created by TheCodersCorner");
            zipStream.putNextEntry(entry);

            //LOG.info("Generated new entry for: " + inputFileName);

            // Now we copy the existing file into the zip archive. To do
            // this we write into the zip stream, the call to putNextEntry
            // above prepared the stream, we now write the bytes for this
            // entry. For another source such as an in memory array, you'd
            // just change where you read the information from.
            byte[] readBuffer = new byte[2048];
            int amountRead;
            int written = 0;

            while ((amountRead = inputStream.read(readBuffer)) > 0) {
                zipStream.write(readBuffer, 0, amountRead);
                written += amountRead;
            }

            //LOG.info("Stored " + written + " bytes to " + inputFileName);


        }
        catch(IOException e) {
            throw new ZipParsingException("Unable to process " + inputFileName, e);
        }
    }
    
    /**
     * We want to let a checked exception escape from a lambda that does not
     * allow exceptions. The only way I can see of doing this is to wrap the
     * exception in a RuntimeException. This is a somewhat unfortunate side
     * effect of lambda's being based off of interfaces.
     */
    private class ZipParsingException extends RuntimeException {
        public ZipParsingException(String reason, Exception inner) {
            super(reason, inner);
        }
    }

}
