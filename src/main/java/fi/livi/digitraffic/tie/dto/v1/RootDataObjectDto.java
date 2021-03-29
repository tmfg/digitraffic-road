package fi.livi.digitraffic.tie.dto.v1;

import java.io.Serializable;
import java.time.ZonedDateTime;

import org.hibernate.annotations.Immutable;

import io.swagger.annotations.ApiModelProperty;

@Immutable
public class RootDataObjectDto implements Serializable {

    @ApiModelProperty(value = "Data last updated date time", required = true)
    public final ZonedDateTime dataUpdatedTime;

    public RootDataObjectDto(final ZonedDateTime dataUpdatedTime) {
        this.dataUpdatedTime = dataUpdatedTime;
    }
}
