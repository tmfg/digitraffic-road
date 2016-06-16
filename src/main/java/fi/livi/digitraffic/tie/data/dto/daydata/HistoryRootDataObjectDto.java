package fi.livi.digitraffic.tie.data.dto.daydata;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "HistoryData", description = "Average median data calculated for the previous day", parent = RootDataObjectDto.class)
@JsonPropertyOrder({ "dataUptadedLocalTime", "dataUptadedUtc", "links"})
public class HistoryRootDataObjectDto extends RootDataObjectDto {

    @ApiModelProperty(value = "Links", required = true)
    private final List<LinkDataDto> links;

    public HistoryRootDataObjectDto(List<LinkDataDto> links,
                                    LocalDateTime uptaded) {
        super(uptaded);
        this.links = links;
    }

    public List<LinkDataDto> getLinks() {
        return links;
    }
}
