package fi.livi.digitraffic.tie.data.dto;

import java.time.ZonedDateTime;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import io.swagger.annotations.ApiModelProperty;

public interface MeasuredDataObjectDto {

    @ApiModelProperty(value = "Value measured " + ToStringHelpper.ISO_8601_OFFSET_TIMESTAMP_EXAMPLE)
    ZonedDateTime getMeasuredTime();
}
