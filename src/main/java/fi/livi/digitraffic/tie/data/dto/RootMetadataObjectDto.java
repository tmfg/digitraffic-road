package fi.livi.digitraffic.tie.data.dto;

import java.time.ZonedDateTime;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModelProperty;

@Immutable
@JsonPropertyOrder({ "dataUpdatedTime", "dataLastCheckedTime" })
public class RootMetadataObjectDto extends RootDataObjectDto {

    @ApiModelProperty(value = "Data last checked " + ToStringHelper.ISO_8601_OFFSET_TIMESTAMP_EXAMPLE, required = true)
    private final ZonedDateTime dataLastCheckedTime;

    public RootMetadataObjectDto(final ZonedDateTime dataUpdatedTime, final ZonedDateTime dataLastCheckedTime) {
        super(dataUpdatedTime);
        this.dataLastCheckedTime = dataLastCheckedTime;
    }

    public ZonedDateTime getDataLastCheckedTime() {
        return dataLastCheckedTime;
    }
}
