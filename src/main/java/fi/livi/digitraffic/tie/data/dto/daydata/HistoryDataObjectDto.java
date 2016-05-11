package fi.livi.digitraffic.tie.data.dto.daydata;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.DataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Average median data calculated for the previous day")
@JsonPropertyOrder({ "dataLocalTime", "dataUtc", "linkDynamicData"})
public class HistoryDataObjectDto extends DataObjectDto {

    @ApiModelProperty(value = "Links data", required = true)
    private final List<LinkDynamicData> linkDynamicData;

    public HistoryDataObjectDto(List<LinkDynamicData> linkDynamicData) {
        this.linkDynamicData = linkDynamicData;
    }

    public List<LinkDynamicData> getLinkDynamicData() {
        return linkDynamicData;
    }
}
