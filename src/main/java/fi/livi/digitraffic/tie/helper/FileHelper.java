package fi.livi.digitraffic.tie.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileHelper {
    private static final Logger log = LoggerFactory.getLogger(FileHelper.class);
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private static final String OPEN_OS_FAIL_MESSAGE_START = "File '";

    private FileHelper() {}

    /**
     * Loads data from given url and writes it to given file.
     * Implementation done based org.apache.commons.io.FileUtils implementation
     *
     * @param source url where to read data
     * @param destination file to write data
     * @throws IOException
     */
    public static void copyURLToFile(URL source, File destination) throws IOException {

        long start = System.currentTimeMillis();

        String tempFileName = destination.getName() + ".tmp";
        File tempTargetFile = new File(destination.getParentFile().getPath(), tempFileName);

        if (tempTargetFile.exists()) {
            log.debug("Delete old tmp file " + tempTargetFile);
            FileUtils.deleteQuietly(tempTargetFile);
            tempTargetFile = new File(destination.getPath(), tempFileName);
        }
        log.info("Load picture from " + source +  " and write to " + tempTargetFile.getAbsolutePath());

        long timeReadMs = 0L;
        long timeWriteMs = 0L;
        long bytesTotal = 0;

        InputStream input = null;
        FileOutputStream output = null;
        long startOpenStreams = 0;
        long endOpenStreams = 0;
        try {
            startOpenStreams = System.currentTimeMillis();
            input = source.openStream();
            output = openOutputStream(tempTargetFile);
            IOUtils.copy(input, output);
            endOpenStreams = System.currentTimeMillis();
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            while (true) {
                long startRead = System.currentTimeMillis();
                int bytesRead = input.read(buffer);
                long endInStartWrite = System.currentTimeMillis();
                timeReadMs += endInStartWrite-startRead;
                if (bytesRead != -1) {
                    output.write(buffer, 0, bytesRead);
                    bytesTotal += bytesRead;
                    long endWrite = System.currentTimeMillis();
                    timeWriteMs += endWrite-endInStartWrite;
                } else {
                    break;
                }
            }
        } finally {
            IOUtils.closeQuietly(output);
            IOUtils.closeQuietly(input);
        }

        long startMove = System.currentTimeMillis();
        FileUtils.copyFile(tempTargetFile, destination);
        FileUtils.deleteQuietly(tempTargetFile);
        long endMove = System.currentTimeMillis();
        final long timeMove = endMove - startMove;
        log.info(String.format("File handling took %1$d ms (%2$d bytes, read %3$d ms, write to disk %4$d ms, and move to dst %5$d ms, open streams %6$d ms",
                endMove-start, bytesTotal, timeReadMs, timeWriteMs, timeMove, endOpenStreams-startOpenStreams));
    }

    private static FileOutputStream openOutputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException(OPEN_OS_FAIL_MESSAGE_START + file + "' exists but is a directory");
            }
            if (!file.canWrite()) {
                throw new IOException(OPEN_OS_FAIL_MESSAGE_START + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IOException(OPEN_OS_FAIL_MESSAGE_START + file + "' could not be created");
            }
        }
        return new FileOutputStream(file);
    }
}
