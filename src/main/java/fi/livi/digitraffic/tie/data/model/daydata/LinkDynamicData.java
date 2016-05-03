package fi.livi.digitraffic.tie.data.model.daydata;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class LinkDynamicData {

    @ApiModelProperty(value = "Link id", required = true)
    private final int linkNumber;

    @ApiModelProperty(value = "Meadured link data", required = true)
    private final List<LinkData> linkData;

    public LinkDynamicData(int linkNumber, List<LinkData> linkData) {
        this.linkNumber = linkNumber;
        this.linkData = linkData;
    }

    public int getLinkNumber() {
        return linkNumber;
    }

    public List<LinkData> getLinkData() {
        return linkData;
    }
}
