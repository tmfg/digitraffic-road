package fi.livi.digitraffic.tie.metadata.service.location;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MetadataFileFetcher {
    private final String tmsUrl;

    private static final String LOCATIONS_FILENAME = "locations.csv";
    private static final String LOCATION_TYPES_FILENAME = "TYPES.DAT";
    private static final String LOCATION_SUBTYPES_FILENAME = "SUBTYPES.DAT";

    private static final String LOCATIONS_ZIPNAME = "FI_LC_noncertified_simple_1_11_30.zip";
    private static final String LOCATION_TYPES_ZIPNAME = "FIN_LC_noncertified_1_11_30.zip";

    public MetadataFileFetcher(@Value("${metadata.tms.url}") final String tmsUrl) {
        this.tmsUrl = tmsUrl;
    }

    public MetadataPathCollection getFilePaths() throws IOException {
        final Path locationsPath = getLocationsFile();
        final Pair<Path, Path> pathPair = getTypefiles();

        return new MetadataPathCollection(locationsPath, pathPair.getRight(), pathPair.getLeft(), LOCATION_TYPES_ZIPNAME, LOCATIONS_ZIPNAME);
    }

    public Path getLocationsFile() throws IOException {
        final URL url = getLocationsZip();
        final File destination = getLocationsZipDestination();

        try {
            FileUtils.copyURLToFile(url, destination);

            return getLocationsFileFromZip(destination);
        } finally {
            FileUtils.deleteQuietly(destination);
        }
    }

    public Pair<Path, Path> getTypefiles() throws IOException {
        final URL url = getCcLtnZip();
        final File destination = getCcLtnZipDestination();

        try {
            FileUtils.copyURLToFile(url, destination);

            final Path typesPath = getTypesPathFromZip(destination);
            final Path subtypesPath = getSubtypesPathFromZip(destination);

            return Pair.of(typesPath, subtypesPath);
        } finally {
            FileUtils.deleteQuietly(destination);
        }
    }

    private Path getTypesPathFromZip(final File destination) throws IOException {
        return getFileFromZip(destination, LOCATION_TYPES_FILENAME, LOCATION_TYPES_FILENAME);
    }

    private Path getSubtypesPathFromZip(final File destination) throws IOException {
        return getFileFromZip(destination, LOCATION_SUBTYPES_FILENAME, LOCATION_SUBTYPES_FILENAME);
    }

    private Path getLocationsFileFromZip(final File zipfile) throws IOException {
        return getFileFromZip(zipfile, null, LOCATIONS_FILENAME);
    }

    private Path getFileFromZip(final File zipfile, final String entryName, final String destinationName) throws IOException {
        try(final ZipFile z = new ZipFile(zipfile)) {
            final File entryDestination = new File(destinationName);
            final ZipEntry e = findEntry(z, entryName);
            final InputStream is = z.getInputStream(e);
            final OutputStream os = new FileOutputStream(entryDestination);

            IOUtils.copy(is, os);
            IOUtils.closeQuietly(os);

            return entryDestination.toPath();
        }
    }

    private ZipEntry findEntry(final ZipFile z, final String name) {
        final Enumeration<? extends ZipEntry> entries = z.entries();

        // if given name is empty, return first entry
        if(StringUtils.isEmpty(name)) {
            return entries.nextElement();
        }

        while(entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();

            if(StringUtils.equals(entry.getName(), name)) {
                return entry;
            }
        }

        return null;
    }

    private File getLocationsZipDestination() throws IOException {
        return File.createTempFile("locations", "zip");
    }

    private File getCcLtnZipDestination() throws IOException {
        return File.createTempFile("cc_ltn", "zip");
    }

    public URL getLocationsZip() throws MalformedURLException {
        return new URL(tmsUrl + LOCATIONS_ZIPNAME);
    }

    public URL getCcLtnZip() throws MalformedURLException {
        return new URL(tmsUrl + LOCATION_TYPES_ZIPNAME);
    }
}
