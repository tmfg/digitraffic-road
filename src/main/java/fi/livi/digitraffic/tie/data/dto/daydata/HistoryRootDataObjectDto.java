package fi.livi.digitraffic.tie.data.dto.daydata;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "HistoryData", description = "Average median data calculated for the previous day", parent = RootDataObjectDto.class)
@JsonPropertyOrder({ "dataUpdatedLocalTime", "dataUpdatedUtc", "links"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistoryRootDataObjectDto extends RootDataObjectDto {

    @ApiModelProperty(value = "Links")
    private final List<LinkDataDto> links;

    public HistoryRootDataObjectDto(final List<LinkDataDto> links,
                                    final LocalDateTime updated) {
        super(updated);
        this.links = links;
    }

    public HistoryRootDataObjectDto(final LocalDateTime updated) {
        super(updated);
        this.links = null;
    }

    public List<LinkDataDto> getLinks() {
        return links;
    }
}
