package fi.livi.digitraffic.tie.data.dto.freeflowspeed;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import fi.livi.digitraffic.tie.data.dto.lam.LamFreeFlowSpeedDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Immutable
@ApiModel(value = "FreeFlowSpeedData", description = "Current free flow speed values for links and TMS stations", parent = RootDataObjectDto.class)
@JsonPropertyOrder({ "dataUpdatedLocalTime", "dataUpdatedUtc", "linkFreeFlowSpeeds", "tmsFreeFlowSpeeds"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FreeFlowSpeedRootDataObjectDto extends RootDataObjectDto {

    @ApiModelProperty(value = "Free flow speeds for links")
    private final List<LinkFreeFlowSpeedDto> linkFreeFlowSpeeds;

    @ApiModelProperty(value = "Free flow speeds for TMS stations")
    @JsonProperty(value = "tmsFreeFlowSpeeds")
    private final List<LamFreeFlowSpeedDto> lamFreeFlowSpeeds;

    public FreeFlowSpeedRootDataObjectDto(final List<LinkFreeFlowSpeedDto> linkFreeFlowSpeeds,
                                          final List<LamFreeFlowSpeedDto> lamFreeFlowSpeeds,
                                          final LocalDateTime updated) {
        super(updated);
        this.linkFreeFlowSpeeds = linkFreeFlowSpeeds;
        this.lamFreeFlowSpeeds = lamFreeFlowSpeeds;
    }

    public FreeFlowSpeedRootDataObjectDto(final LocalDateTime updated) {
        this(null, null, updated);
    }

    public List<LinkFreeFlowSpeedDto> getLinkFreeFlowSpeeds() {
        return linkFreeFlowSpeeds;
    }

    public List<LamFreeFlowSpeedDto> getLamFreeFlowSpeeds() {
        return lamFreeFlowSpeeds;
    }

}
