package fi.livi.digitraffic.tie.service.trafficmessage.location;

import java.nio.file.Path;

/**
 * A single extracted zip entry: the local temp file path and the original entry name.
 *
 * <p>Example: {@code new MetadataFileEntry(Path.of("/tmp/TYPES.DAT123"), "TYPES.DAT")}
 */
public record MetadataFileEntry(Path path, String entryName) {}

