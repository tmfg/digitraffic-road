package fi.livi.digitraffic.tie.metadata.dto;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import fi.livi.digitraffic.tie.metadata.geojson.roadconditions.ForecastSectionFeatureCollection;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

@ApiModel(description = "Weather forecast sections")
public class ForecastSectionsMetadata extends RootDataObjectDto {

    @ApiModelProperty(value = "Weather forecast sections", required = true)
    private final ForecastSectionFeatureCollection forecastSections;

    public ForecastSectionsMetadata(final ForecastSectionFeatureCollection forecastSections, final LocalDateTime lastUpdated) {
        super(lastUpdated);
        this.forecastSections = forecastSections;
    }

    public ForecastSectionFeatureCollection getForecastSections() {
        return forecastSections;
    }
}
