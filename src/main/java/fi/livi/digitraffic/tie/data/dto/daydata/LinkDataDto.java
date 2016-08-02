package fi.livi.digitraffic.tie.data.dto.daydata;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.MeasuredDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Link", description = "Link data")
@JsonPropertyOrder(value = { "id", "measuredLocalTime", "measuredUtc", "linkMeasurements"})
public class LinkDataDto implements MeasuredDataObjectDto {

    @ApiModelProperty(value = "Link id", required = true)
    @JsonProperty("id")
    private final int linkNumber;

    @ApiModelProperty(value = "Meadured link data", required = true)
    private final List<LinkMeasurementDataDto> linkMeasurements;

    @JsonIgnore
    private LocalDateTime measured;

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
    public LocalDateTime getMeasured() {
        return measured;
    }

    public void setMeasured(final LocalDateTime measured) {
        this.measured = measured;
    }
}
