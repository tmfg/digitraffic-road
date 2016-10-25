package fi.livi.digitraffic.tie.metadata.converter;

import fi.livi.digitraffic.tie.metadata.geojson.LineString;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.metadata.geojson.roadconditions.ForecastSectionFeature;
import fi.livi.digitraffic.tie.metadata.geojson.roadconditions.ForecastSectionFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.roadconditions.ForecastSectionProperties;
import fi.livi.digitraffic.tie.metadata.model.ForecastSection;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ForecastSection2FeatureConverter extends AbstractMetadataToFeatureConverter {

    protected ForecastSection2FeatureConverter(CoordinateConverter coordinateConverter) {
        super(coordinateConverter);
    }

    public ForecastSectionFeatureCollection convert(List<ForecastSection> forecastSections, final LocalDateTime lastUpdated) {

        final ForecastSectionFeatureCollection forecastSectionFeatures = new ForecastSectionFeatureCollection(lastUpdated);

        for (ForecastSection fs : forecastSections) {
            forecastSectionFeatures.add(convert(fs));
        }
        return forecastSectionFeatures;
    }

    private ForecastSectionFeature convert(ForecastSection fs) {
        return new ForecastSectionFeature(fs.getId(), new LineString(fs.getForecastSectionCoordinates()), createProperties(fs));
    }

    private ForecastSectionProperties createProperties(ForecastSection fs) {
        final ForecastSectionProperties properties = new ForecastSectionProperties();

        properties.setNaturalId(fs.getNaturalId());
        properties.setDescription(fs.getDescription());
        return properties;
    }
}
