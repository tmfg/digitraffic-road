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
    private static final String LOCATION_TYPES_FILENAME = "TYPES.DAT";
    private static final String LOCATION_SUBTYPES_FILENAME = "SUBTYPES.DAT";

    private static final Logger log = LoggerFactory.getLogger(MetadataFileFetcher.class);

    public MetadataFileFetcher(@Value("${metadata.tmc.url}") final String tmcUrl) {
        this.tmcUrl = tmcUrl;
    }

    public MetadataPathCollection getFilePaths(final MetadataVersions latestVersions) throws IOException {
        final URL locationsUrl = getUrl(latestVersions.getLocationsVersion().filename);
        final MetadataFileEntry locationsEntry = getLocationsFileEntry(locationsUrl);

        final URL typesUrl = getUrl(latestVersions.getLocationTypeVersion().filename);
        final File typesZip = downloadToTemp(typesUrl, getCcLtnZipDestination());
        try {
            final MetadataFileEntry typesEntry = getFileFromZip(typesZip, LOCATION_TYPES_FILENAME);
            final MetadataFileEntry subtypesEntry = getFileFromZip(typesZip, LOCATION_SUBTYPES_FILENAME);

            return new MetadataPathCollection(
                locationsEntry.path(), locationsUrl + "!" + locationsEntry.entryName(),
                subtypesEntry.path(), typesUrl + "!" + subtypesEntry.entryName(),
                typesEntry.path(), typesUrl + "!" + typesEntry.entryName()
            );
        } finally {
            FileUtils.deleteQuietly(typesZip);
        }
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


    private MetadataFileEntry getLocationsFileEntry(final URL url) throws IOException {
        final File destination = getLocationsZipDestination();

        log.info("method=getLocationsFileEntry reading locations from url={}", url);

        try {
            downloadToTemp(url, destination);
            return getFileFromZip(destination, null);
        } finally {
            FileUtils.deleteQuietly(destination);
        }
    }

    private File downloadToTemp(final URL url, final File destination) throws IOException {
        log.info("method=downloadToTemp url={}", url);
        FileUtils.copyToFile(createStreamFromUrl(url), destination);
        return destination;
    }

    /**
     * Extracts a zip entry to a temp file.
     *
     * @param entryName the entry to extract, or null to take the first entry
     * @return {@link MetadataFileEntry} with the temp path and the actual entry name,
     *         e.g. {@code FI_LC_noncertified_simple_1_11_45.csv}
     */
    private MetadataFileEntry getFileFromZip(final File zipfile, final String entryName) throws IOException {
        try (final ZipFile z = new ZipFile(zipfile)) {
            final ZipEntry e = findEntry(z, entryName);
            // Strip directory components and ensure prefix is at least 3 chars for createTempFile
            final String baseName = Path.of(e.getName()).getFileName().toString();
            final String prefix = baseName.length() >= 3 ? baseName : baseName + "___";
            final File entryDestination = Files.createTempFile(prefix, null).toFile();
            try (final InputStream is = z.getInputStream(e)) {
                doCopy(is, entryDestination);
            }
            return new MetadataFileEntry(entryDestination.toPath(), e.getName());
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
