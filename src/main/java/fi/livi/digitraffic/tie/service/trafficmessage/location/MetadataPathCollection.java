package fi.livi.digitraffic.tie.service.trafficmessage.location;

import java.nio.file.Path;

/**
 * Holds the local temp file paths for downloaded TMC metadata files along with their
 * original source URLs for logging purposes.
 *
 * <p>Files are downloaded from the TMC server as zip archives and extracted to temp files.
 * The source strings carry the original URL + zip entry so errors can be traced back to
 * the exact remote file, e.g. {@code https://tmc.digitraffic.fi/tmc/4.6.zip!locations.csv}.
 */
public class MetadataPathCollection {
    /** Temp path of the extracted locations CSV, e.g. {@code /tmp/locations.csv12345null} */
    public final Path locationsPath;
    /** Temp path of the extracted subtypes DAT file */
    public final Path subtypesPath;
    /** Temp path of the extracted types DAT file */
    public final Path typesPath;
    /** Source URL + entry for logging, e.g. {@code https://tmc.digitraffic.fi/tmc/4.6.zip!locations.csv} */
    public final String locationsSource;
    /** Source URL + entry for logging, e.g. {@code https://tmc.digitraffic.fi/tmc/4.6.zip!SUBTYPES.DAT} */
    public final String subtypesSource;
    /** Source URL + entry for logging, e.g. {@code https://tmc.digitraffic.fi/tmc/4.6.zip!TYPES.DAT} */
    public final String typesSource;

    public MetadataPathCollection(final Path locationsPath, final String locationsSource,
                                  final Path subtypesPath, final String subtypesSource,
                                  final Path typesPath, final String typesSource) {
        this.locationsPath = locationsPath;
        this.locationsSource = locationsSource;
        this.subtypesPath = subtypesPath;
        this.subtypesSource = subtypesSource;
        this.typesPath = typesPath;
        this.typesSource = typesSource;
    }
}
