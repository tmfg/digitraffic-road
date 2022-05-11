package fi.livi.digitraffic.tie.dto.v1;

import java.time.ZonedDateTime;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

@Immutable
@JsonPropertyOrder({ "dataUpdatedTime", "dataLastCheckedTime", "type", "features" })
public class RootMetadataObjectDto extends RootDataObjectDto {

    @Schema(description = "Data last checked date time", required = true)
    public final ZonedDateTime dataLastCheckedTime;

    public RootMetadataObjectDto(final ZonedDateTime dataUpdatedTime,
                                 final ZonedDateTime dataLastCheckedTime) {
        super(dataUpdatedTime);
        this.dataLastCheckedTime = dataLastCheckedTime;
    }
}
