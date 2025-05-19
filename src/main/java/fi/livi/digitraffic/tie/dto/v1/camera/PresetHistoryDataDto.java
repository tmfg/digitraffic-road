package fi.livi.digitraffic.tie.dto.v1.camera;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PresetHistoryData", description = "Weather camera preset's image history details.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PresetHistoryDataDto {

    @Schema(description = "Last modified date of the image.")
    private final Instant lastModified;
    @Schema(description = "Url to read the image.")
    private final String imageUrl;
    @Schema(description = "Image size in bytes.")
    private final int sizeBytes;

    public PresetHistoryDataDto(final Instant lastModified, final String imageUrl, final int sizeBytes) {
        this.lastModified = lastModified;
        this.imageUrl = imageUrl;
        this.sizeBytes = sizeBytes;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public int getSizeBytes() {
        return sizeBytes;
    }
}
