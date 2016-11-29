package fi.livi.digitraffic.tie.data.dto;

import java.time.ZonedDateTime;

import org.hibernate.annotations.Immutable;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import io.swagger.annotations.ApiModelProperty;

@Immutable
public class RootDataObjectDto {

    @ApiModelProperty(value = "Data last updated " + ToStringHelpper.ISO_8601_OFFSET_TIMESTAMP_EXAMPLE, required = true)
    private final ZonedDateTime dataUpdatedTime;

    public RootDataObjectDto(final ZonedDateTime dataUpdatedTime) {
        this.dataUpdatedTime = dataUpdatedTime;
    }

    public ZonedDateTime getDataUpdatedTime() {
        return dataUpdatedTime;
    }
}
