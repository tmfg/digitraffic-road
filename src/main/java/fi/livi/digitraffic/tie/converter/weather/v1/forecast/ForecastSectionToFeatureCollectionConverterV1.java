package fi.livi.digitraffic.tie.converter.weather.v1.forecast;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.dao.weather.forecast.ForecastSectionDto;
import fi.livi.digitraffic.tie.dto.LastModifiedSupport;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionFeatureCollectionV1;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionFeatureSimpleV1;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionFeatureV1;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionPropertiesSimpleV1;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionPropertiesV1;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.RoadSegmentDtoV1;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;
import fi.livi.digitraffic.tie.metadata.geojson.LineString;
import fi.livi.digitraffic.tie.model.weather.forecast.ForecastSection;

@Component
public class ForecastSectionToFeatureCollectionConverterV1 {

    protected ForecastSectionToFeatureCollectionConverterV1() {
    }

    public ForecastSectionFeatureCollectionSimpleV1 convertToSimpleFeatureCollection(final List<ForecastSection> forecastSections, final Instant lastModifiedFallback) {

        final List<ForecastSectionFeatureSimpleV1> features =
            forecastSections.stream()
                .map(this::convertToSimpleFeature)
                .collect(Collectors.toList());
        final Instant lastModified = getLastModified(features, lastModifiedFallback);
        return new ForecastSectionFeatureCollectionSimpleV1(lastModified, features);
    }


    public ForecastSectionFeatureSimpleV1 convertToSimpleFeature(final ForecastSection fs) {
        final LineString lineString = PostgisGeometryUtils.convertToGeoJSONLineString(fs.getGeometry());
        return new ForecastSectionFeatureSimpleV1(lineString, createSimpleProperties(fs));
    }

    private ForecastSectionPropertiesSimpleV1 createSimpleProperties(final ForecastSection fs) {
        return new ForecastSectionPropertiesSimpleV1(
                fs.getNaturalId(),
                fs.getDescription(),
                fs.getRoadSectionNumber(),
                fs.getRoadSectionVersionNumber(),
                fs.getRoadNumber(),
                fs.getModified()
            );
    }

    public ForecastSectionFeatureCollectionV1 convertToFeatureCollection(final List<ForecastSectionDto> forecastSections, final Instant lastModifiedFallback, final boolean simplified) {
        final List<ForecastSectionFeatureV1> features =
            forecastSections.stream()
                .map((ForecastSectionDto fs) -> convertToFeature(fs, simplified))
                .collect(Collectors.toList());
        final Instant lastModified = getLastModified(features, lastModifiedFallback);
        return new ForecastSectionFeatureCollectionV1(lastModified, features);
    }

    public ForecastSectionFeatureV1 convertToFeature(final ForecastSectionDto fs, final boolean simplified) {
        return new ForecastSectionFeatureV1(simplified ? fs.getGeometrySimplified() : fs.getGeometry(), createProperties(fs));
    }

    private ForecastSectionPropertiesV1 createProperties(final ForecastSectionDto fs) {
        return new ForecastSectionPropertiesV1(
            fs.getNaturalId(),
            fs.getDescription(),
            fs.getRoadSectionNumber(),
            fs.getRoadNumber(),
            fs.getLength(),
            createRoadSegments(fs),
            fs.getLinkIds(),
            fs.getModified()
        );
    }

    private List<RoadSegmentDtoV1> createRoadSegments(final ForecastSectionDto forecastSection) {
        if (forecastSection.getRoadSegments() == null) {
            return Collections.emptyList();
        }
        return forecastSection.getRoadSegments().stream()
            .map(rs -> new RoadSegmentDtoV1(rs.startDistance, rs.endDistance, rs.carriageway))
            .collect(Collectors.toList());
    }

    private Instant getLastModified(final List<? extends LastModifiedSupport> features, final Instant lastModifiedFallback) {
        return DateHelper.getGreatest(
            features.stream().map(LastModifiedSupport::getLastModified).max(Comparator.naturalOrder()).orElse(null),
            lastModifiedFallback);
    }
}
