package fi.livi.digitraffic.tie.metadata.dto;

import java.time.ZonedDateTime;
import java.util.List;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import fi.livi.digitraffic.tie.metadata.model.ForecastSection;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Weather forecast sections")
public class ForecastSectionsMetadata extends RootDataObjectDto {

    @ApiModelProperty(value = "Weather forecast sections", required = true)
    private final List<ForecastSection> forecastSections;

    public ForecastSectionsMetadata(final List<ForecastSection> forecastSections, final ZonedDateTime lastUpdated) {
        super(lastUpdated);
        this.forecastSections = forecastSections;
    }

    public List<ForecastSection> getForecastSections() {
        return forecastSections;
    }
}
