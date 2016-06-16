package fi.livi.digitraffic.tie.metadata.dto;

import java.util.List;

import fi.livi.digitraffic.tie.metadata.model.ForecastSection;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Road weather forecast sections")
public class ForecastSectionsMetadata {

    @ApiModelProperty(value = "Road weather forecast sections", required = true)
    private final List<ForecastSection> forecastSections;

    public ForecastSectionsMetadata(List<ForecastSection> forecastSections) {
        this.forecastSections = forecastSections;
    }

    public List<ForecastSection> getForecastSections() {
        return forecastSections;
    }
}
