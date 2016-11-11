package fi.livi.digitraffic.tie.metadata.dto;

import java.time.LocalDateTime;
import java.util.List;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import fi.livi.digitraffic.tie.metadata.model.ForecastSection;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Weather forecast sections")
public class ForecastSectionsMetadata extends RootDataObjectDto {
    @ApiModelProperty(value = "Weather forecast sections", required = true)
    public final List<ForecastSection> forecastSections;

    public ForecastSectionsMetadata(final List<ForecastSection> forecastSections, final LocalDateTime lastUpdated) {
        super(lastUpdated);
        this.forecastSections = forecastSections;
    }
}
