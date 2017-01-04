package fi.livi.digitraffic.tie.metadata.converter;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.metadata.geojson.LineString;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionFeature;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionProperties;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSection;

@Component
public class ForecastSection2FeatureConverter extends AbstractMetadataToFeatureConverter {

    protected ForecastSection2FeatureConverter(CoordinateConverter coordinateConverter) {
        super(coordinateConverter);
    }

    public ForecastSectionFeatureCollection convert(List<ForecastSection> forecastSections, final ZonedDateTime lastUpdated) {

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
        properties.setRoadSectionNumber(fs.getRoadSectionNumber());
        properties.setRoadNumber(fs.getRoadNumber());
        properties.setRoadSectionVersionNumber(fs.getRoadSectionVersionNumber());
        properties.setStartDistance(fs.getStartDistance());
        properties.setEndDistance(fs.getEndDistance());
        properties.setLength(fs.getLength());
        properties.setRoad(fs.getRoad());
        properties.setStartRoadSection(fs.getStartRoadSection());
        properties.setEndRoadSection(fs.getEndRoadSection());
        return properties;
    }
}
