package fi.livi.digitraffic.tie.dto.weathercam.v1.history;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weather camera preset's image history details.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeathercamPresetHistoryItemDtoV1 {

    @Schema(description = "Last modified date of the image.")
    public final Instant lastModified;

    @Schema(description = "Url to read the image.")
    public final String imageUrl;

    @Schema(description = "Image size in bytes.")
    public final int sizeBytes;

    public WeathercamPresetHistoryItemDtoV1(final Instant lastModified, final String imageUrl, final int sizeBytes) {
        this.lastModified = lastModified;
        this.imageUrl = imageUrl;
        this.sizeBytes = sizeBytes;
    }
}
