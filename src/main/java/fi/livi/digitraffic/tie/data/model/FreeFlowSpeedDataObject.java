package fi.livi.digitraffic.tie.data.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Current free flow speed values for links and LAM stations")
@JsonPropertyOrder({ "dataLocalTime", "dataUtc", "linkData", "lamData"})
public class FreeFlowSpeedDataObject extends DataObject {
    private final List<LinkFreeFlowSpeed> linkData;
    private final List<LamFreeFlowSpeed> lamData;

    public FreeFlowSpeedDataObject(final List<LinkFreeFlowSpeed> linkData, final List<LamFreeFlowSpeed> lamData) {
        this.linkData = linkData;
        this.lamData = lamData;
    }

    public List<LinkFreeFlowSpeed> getLinkData() {
        return linkData;
    }

    public List<LamFreeFlowSpeed> getLamData() {
        return lamData;
    }
}
