package fi.livi.digitraffic.tie.dto.v1.freeflowspeed;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.RootDataObjectDto;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsFreeFlowSpeedDto;

import io.swagger.v3.oas.annotations.media.Schema;

@Immutable
@Schema(name = "FreeFlowSpeedData", description = "Current free flow speed values for links and TMS stations")
@JsonPropertyOrder({ "dataUpdatedTime", "linkFreeFlowSpeeds", "tmsFreeFlowSpeeds"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FreeFlowSpeedRootDataObjectDto extends RootDataObjectDto {

    @Schema(description = "Free flow speeds for links(deprecated)")
    private final List<String> linkFreeFlowSpeeds;

    @Schema(description = "Free flow speeds for TMS stations")
    @JsonProperty(value = "tmsFreeFlowSpeeds")
    private final List<TmsFreeFlowSpeedDto> tmsFreeFlowSpeeds;

    public FreeFlowSpeedRootDataObjectDto(final List<TmsFreeFlowSpeedDto> tmsFreeFlowSpeeds,
                                          final ZonedDateTime updated) {
        super(updated);
        this.linkFreeFlowSpeeds = Collections.emptyList();
        this.tmsFreeFlowSpeeds = tmsFreeFlowSpeeds;
    }

    public FreeFlowSpeedRootDataObjectDto(final ZonedDateTime updated) {
        this(null,  updated);
    }

    public List<String> getLinkFreeFlowSpeeds() {
        return linkFreeFlowSpeeds;
    }

    public List<TmsFreeFlowSpeedDto> getTmsFreeFlowSpeeds() {
        return tmsFreeFlowSpeeds;
    }

}
