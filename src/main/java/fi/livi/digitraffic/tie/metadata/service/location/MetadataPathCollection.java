package fi.livi.digitraffic.tie.metadata.service.location;

import java.nio.file.Path;

public class MetadataPathCollection {
    public final Path locationsPath;
    public final Path subtypesPath;
    public final Path typesPath;

    public final String typesVersion;
    public final String locationsVersion;

    public MetadataPathCollection(final Path locationsPath, final Path subtypesPath, final Path typesPath, String typesVersion,
                                  String locationsVersion) {
        this.locationsPath = locationsPath;
        this.subtypesPath = subtypesPath;
        this.typesPath = typesPath;
        this.typesVersion = typesVersion;
        this.locationsVersion = locationsVersion;
    }
}
