package fi.livi.digitraffic.tie.data.model.daydata;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.model.DataObject;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "Average median data calculated for the previous day")
@JsonPropertyOrder({ "dataLocalTime", "dataUtc", "linkDynamicData"})
public class HistoryDataObject extends DataObject {

    private final List<LinkDynamicData> linkDynamicData;

    public HistoryDataObject(List<LinkDynamicData> linkDynamicData) {
        this.linkDynamicData = linkDynamicData;
    }

    public List<LinkDynamicData> getLinkDynamicData() {
        return linkDynamicData;
    }
}
