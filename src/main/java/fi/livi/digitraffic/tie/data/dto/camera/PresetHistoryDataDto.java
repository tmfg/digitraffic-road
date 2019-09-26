package fi.livi.digitraffic.tie.data.dto.camera;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PresetHistoryDataDto {

    private final ZonedDateTime lastModified;
    private final String imageUrl;
    private int sizeBytes;

    public PresetHistoryDataDto(final ZonedDateTime lastModified, final String imageUrl, final int sizeBytes) {
        this.lastModified = lastModified;
        this.imageUrl = imageUrl;
        this.sizeBytes = sizeBytes;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public ZonedDateTime getLastModified() {
        return lastModified;
    }

    public int getSizeBytes() {
        return sizeBytes;
    }
}
