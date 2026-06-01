package fi.livi.digitraffic.tie.service.trafficmessage.location;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

@ConditionalOnNotWebApplication
@Component
public class MetadataFileFetcher {
    private final String tmcUrl;

    private static final String LATEST_FILENAME = "latest.txt";
    private static final String LOCATIONS_FILENAME = "locations.csv";
    private static final String LOCATION_TYPES_FILENAME = "TYPES.DAT";
    private static final String LOCATION_SUBTYPES_FILENAME = "SUBTYPES.DAT";

    private static final Logger log = LoggerFactory.getLogger(MetadataFileFetcher.class);

    public MetadataFileFetcher(@Value("${metadata.tmc.url}") final String tmcUrl) {
        this.tmcUrl = tmcUrl;
    }

    public MetadataPathCollection getFilePaths(final MetadataVersions latestVersions) throws IOException {
        final URL locationsUrl = getUrl(latestVersions.getLocationsVersion().filename);
        final Path locationsPath = getLocationsFile(locationsUrl);

        final URL typesUrl = getUrl(latestVersions.getLocationTypeVersion().filename);
        final Pair<Path, Path> pathPair = getTypefiles(typesUrl);

        return new MetadataPathCollection(
            locationsPath, locationsUrl + "!" + LOCATIONS_FILENAME,
            pathPair.getRight(), typesUrl + "!" + LOCATION_SUBTYPES_FILENAME,
            pathPair.getLeft(), typesUrl + "!" + LOCATION_TYPES_FILENAME
        );
    }

    public MetadataVersions getLatestVersions() throws MalformedURLException {
        final URL url = getLatestUrl();
        final LatestReader reader = new LatestReader();

        log.info("method=getLatestVersions reading latest from url={}", url);

        try {
            reader.read(createStreamFromUrl(url), url.getFile());
        } catch (final IOException e) {
            log.error("method=getLatestVersions error reading latest versions from url={}", url, e);
        }

        return reader.getLatestMetadataVersions();
    }

    public Path getLocationsFile(final MetadataVersions.MetadataVersion latestVersion) throws IOException {
        return getLocationsFile(getUrl(latestVersion.filename));
    }

    private Path getLocationsFile(final URL url) throws IOException {
        final File destination = getLocationsZipDestination();

        log.info("method=getLocationsFile reading locations from url={}", url);

        try {
            FileUtils.copyToFile(createStreamFromUrl(url), destination);

            return getLocationsFileFromZip(destination);
        } finally {
            FileUtils.deleteQuietly(destination);
        }
    }

    public Pair<Path, Path> getTypefiles(final MetadataVersions.MetadataVersion latestVersion) throws IOException {
        return getTypefiles(getUrl(latestVersion.filename));
    }

    private Pair<Path, Path> getTypefiles(final URL url) throws IOException {
        final File destination = getCcLtnZipDestination();

        log.info("method=getTypefiles reading types from url={}", url);

        try {
            FileUtils.copyToFile(createStreamFromUrl(url), destination);

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
        try (final ZipFile z = new ZipFile(zipfile)) {
            final File entryDestination = Files.createTempFile(destinationName, null).toFile();
            final ZipEntry e = findEntry(z, entryName); // always non-null — throws if not found
            try (final InputStream is = z.getInputStream(e)) {
                doCopy(is, entryDestination);
            }
            return entryDestination.toPath();
        }
    }

    private void doCopy(final InputStream is, final File entryDestination) throws IOException {
        try(final OutputStream os = new FileOutputStream(entryDestination)) {
            IOUtils.copy(is, os);
        }
    }

    private static ZipEntry findEntry(final ZipFile z, final String name) {
        final Enumeration<? extends ZipEntry> entries = z.entries();

        // if given name is empty, return first entry
        if (StringUtils.isEmpty(name)) {
            if (!entries.hasMoreElements()) {
                throw new IllegalArgumentException("method=findEntry Zip file is empty zipFile=" + z.getName());
            }
            return entries.nextElement();
        }

        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();
            if (Strings.CS.equals(entry.getName(), name)) {
                return entry;
            }
        }

        throw new IllegalArgumentException("method=findEntry Entry not found in zip entryName=" + name + " zipFile=" + z.getName());
    }

    private static File getLocationsZipDestination() throws IOException {
        return Files.createTempFile("locations", "zip").toFile();
    }

    private static File getCcLtnZipDestination() throws IOException {
        return Files.createTempFile("cc_ltn", "zip").toFile();
    }

    public URL getLatestUrl() throws MalformedURLException {
        return getUrl(LATEST_FILENAME);
    }

    public URL getUrl(final String filename) throws MalformedURLException {
        return URI.create(tmcUrl + filename).toURL();
    }

    private InputStream createStreamFromUrl(final URL url) throws IOException {
        final URLConnection connection = url.openConnection();
        connection.setRequestProperty("Accept-Encoding", "gzip");
        connection.setRequestProperty("Connection", "close");

        return connection.getInputStream();
    }
}
