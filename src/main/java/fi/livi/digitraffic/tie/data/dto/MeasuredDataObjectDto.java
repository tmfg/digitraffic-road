package fi.livi.digitraffic.tie.data.dto;

import java.time.ZonedDateTime;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModelProperty;

public interface MeasuredDataObjectDto {

    @ApiModelProperty(value = "Value measured " + ToStringHelper.ISO_8601_OFFSET_TIMESTAMP_EXAMPLE)
    ZonedDateTime getMeasuredTime();
}
