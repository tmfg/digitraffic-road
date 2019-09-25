package fi.livi.digitraffic.tie.data.dto.camera;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PresetHistoryDataDto {

    private final ZonedDateTime lastModified;
    private final String url;

    public PresetHistoryDataDto(final ZonedDateTime lastModified, final String url) {
        this.lastModified = lastModified;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public ZonedDateTime getLastModified() {
        return lastModified;
    }
}
