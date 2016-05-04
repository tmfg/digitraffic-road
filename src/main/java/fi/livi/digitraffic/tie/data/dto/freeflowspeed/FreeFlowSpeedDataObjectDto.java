package fi.livi.digitraffic.tie.data.dto.freeflowspeed;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.DataObjectDto;
import fi.livi.digitraffic.tie.data.dto.lam.LamFreeFlowSpeedDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Current free flow speed values for links and LAM stations")
@JsonPropertyOrder({ "dataLocalTime", "dataUtc", "linkData", "lamData"})
public class FreeFlowSpeedDataObjectDto extends DataObjectDto {

    @ApiModelProperty(value = "Free flow speeds for links", required = true)
    private final List<LinkFreeFlowSpeedDto> linkData;

    @ApiModelProperty(value = "Free flow speeds for LAM stations", required = true)
    private final List<LamFreeFlowSpeedDto> lamData;

    public FreeFlowSpeedDataObjectDto(final List<LinkFreeFlowSpeedDto> linkData, final List<LamFreeFlowSpeedDto> lamData) {
        this.linkData = linkData;
        this.lamData = lamData;
    }

    public List<LinkFreeFlowSpeedDto> getLinkData() {
        return linkData;
    }

    public List<LamFreeFlowSpeedDto> getLamData() {
        return lamData;
    }
}
