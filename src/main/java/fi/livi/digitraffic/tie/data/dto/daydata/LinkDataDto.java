package fi.livi.digitraffic.tie.data.dto.daydata;

import java.time.ZonedDateTime;
import java.util.List;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.MeasuredDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Immutable
@ApiModel(value = "Link", description = "Link data", parent = MeasuredDataObjectDto.class)
@JsonPropertyOrder(value = { "id", "dataUpdatedTime", "linkMeasurements"})
public class LinkDataDto implements MeasuredDataObjectDto {

    @ApiModelProperty(value = "Link id", required = true)
    @JsonProperty("id")
    private final int linkNumber;

    @ApiModelProperty(value = "Link measurement data", required = true)
    private final List<LinkMeasurementDataDto> linkMeasurements;

    private ZonedDateTime measuredTime;

    public LinkDataDto(final int linkNumber, final List<LinkMeasurementDataDto> linkMeasurements) {
        this.linkNumber = linkNumber;
        this.linkMeasurements = linkMeasurements;
    }

    public int getLinkNumber() {
        return linkNumber;
    }

    public List<LinkMeasurementDataDto> getLinkMeasurements() {
        return linkMeasurements;
    }

    @Override
    public ZonedDateTime getMeasuredTime() {
        return measuredTime;
    }

    public void setMeasuredTime(final ZonedDateTime measured) {
        this.measuredTime = measured;
    }
}
