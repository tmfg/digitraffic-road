package fi.livi.digitraffic.tie.metadata.dto;

import java.time.ZonedDateTime;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionFeatureCollection;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Weather forecast sections")
public class ForecastSectionsMetadata extends RootDataObjectDto {

    @ApiModelProperty(value = "Weather forecast sections", required = true)
    private final ForecastSectionFeatureCollection forecastSections;

    public ForecastSectionsMetadata(final ForecastSectionFeatureCollection forecastSections, final ZonedDateTime lastUpdated) {
        super(lastUpdated);
        this.forecastSections = forecastSections;
    }

    public ForecastSectionFeatureCollection getForecastSections() {
        return forecastSections;
    }
}
