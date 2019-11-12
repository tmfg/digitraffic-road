package fi.livi.digitraffic.tie.data.dto;

import java.time.ZonedDateTime;

import io.swagger.annotations.ApiModelProperty;

public interface MeasuredDataObjectDto {

    @ApiModelProperty(value = "Value measured date time")
    ZonedDateTime getMeasuredTime();
}
