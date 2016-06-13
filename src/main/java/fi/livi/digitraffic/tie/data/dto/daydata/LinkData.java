package fi.livi.digitraffic.tie.data.dto.daydata;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Link", description = "Link data")
@JsonPropertyOrder(value = { "linkNumber", "linkMeasurements"})
public class LinkData {

    @ApiModelProperty(value = "Link id", required = true)
    private final int linkNumber;

    @ApiModelProperty(value = "Meadured link data", required = true)
    private final List<LinkMeasurementDataDto> linkMeasurements;

    public LinkData(int linkNumber, List<LinkMeasurementDataDto> linkMeasurements) {
        this.linkNumber = linkNumber;
        this.linkMeasurements = linkMeasurements;
    }

    public int getLinkNumber() {
        return linkNumber;
    }

    public List<LinkMeasurementDataDto> getLinkMeasurements() {
        return linkMeasurements;
    }
}
