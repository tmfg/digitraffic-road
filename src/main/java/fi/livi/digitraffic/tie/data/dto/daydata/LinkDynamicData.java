package fi.livi.digitraffic.tie.data.dto.daydata;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class LinkDynamicData {

    @ApiModelProperty(value = "Link id", required = true)
    private final int linkNumber;

    @ApiModelProperty(value = "Meadured link data", required = true)
    private final List<LinkDataDto> linkData;

    public LinkDynamicData(int linkNumber, List<LinkDataDto> linkData) {
        this.linkNumber = linkNumber;
        this.linkData = linkData;
    }

    public int getLinkNumber() {
        return linkNumber;
    }

    public List<LinkDataDto> getLinkData() {
        return linkData;
    }
}
