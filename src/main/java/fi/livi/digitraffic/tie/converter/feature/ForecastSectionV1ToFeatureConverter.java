package fi.livi.digitraffic.tie.converter.feature;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;
import fi.livi.digitraffic.tie.metadata.geojson.LineString;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionFeature;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionProperties;
import fi.livi.digitraffic.tie.model.v1.forecastsection.ForecastSection;

@Component
public class ForecastSectionV1ToFeatureConverter extends AbstractMetadataToFeatureConverter {

    protected ForecastSectionV1ToFeatureConverter(CoordinateConverter coordinateConverter) {
        super(coordinateConverter);
    }

    public ForecastSectionFeatureCollection convert(List<ForecastSection> forecastSections, final ZonedDateTime lastUpdated, final ZonedDateTime dataLastCheckedTime) {

        final List<ForecastSectionFeature> features =
            forecastSections.stream()
                .map(this::convert)
                .collect(Collectors.toList());

        return new ForecastSectionFeatureCollection(lastUpdated, dataLastCheckedTime, features);
    }

    private ForecastSectionFeature convert(final ForecastSection fs) {
        final LineString lineString = PostgisGeometryUtils.convertToGeoJSONLineString(fs.getGeometry());
        return new ForecastSectionFeature(fs.getId(), lineString, createProperties(fs));
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
