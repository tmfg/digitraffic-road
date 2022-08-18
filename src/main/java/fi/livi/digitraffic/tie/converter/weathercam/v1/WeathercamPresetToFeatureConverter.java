package fi.livi.digitraffic.tie.converter.weathercam.v1;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.roadstation.v1.AbstractRoadstationToFeatureConverterV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamPresetDetailedV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamPresetSimpleV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamStationFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamStationFeatureSimpleV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamStationFeatureV1Detailed;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamStationPropertiesDetailedV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamStationPropertiesSimpleV1;
import fi.livi.digitraffic.tie.helper.DataValidityHelper;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetService;

@ConditionalOnWebApplication
@Component
public class WeathercamPresetToFeatureConverter extends AbstractRoadstationToFeatureConverterV1 {
    private static final Logger log = LoggerFactory.getLogger( WeathercamPresetToFeatureConverter.class );

    private final String weathercamBaseurl;
    private final CameraPresetService cameraPresetService;

    @Autowired
    public WeathercamPresetToFeatureConverter(@Value("${weathercam.baseUrl}")
                                              final String weathercamBaseurl,
                                              final CoordinateConverter coordinateConverter,
                                              final CameraPresetService cameraPresetService) {
        super(coordinateConverter);
        this.weathercamBaseurl = weathercamBaseurl;
        this.cameraPresetService = cameraPresetService;
    }

    public WeathercamStationFeatureCollectionSimpleV1 convertToSimpleFeatureCollection(final List<CameraPreset> cameraPresets, final Instant dataLastUpdated, final Instant dataLastCheckedTime) {

        // Cameras mapped with cameraId
        final Map<String, WeathercamStationFeatureSimpleV1> weathercamFeatureMappedByCameraId = new HashMap<>();
        final List<WeathercamStationFeatureSimpleV1> weathercamStationFeatureSimpleV1s = new ArrayList<>();
        //final Map<String, Long> weatherStationsIdsMapByCameraId = cameraPresetService.getNearestWeatherStationNaturalIdMappedByCameraId();
        // CameraPreset contains camera and preset informations and
        // camera info is duplicated on every preset db line
        // So we take camera only once and append presets to it
        cameraPresets
            .forEach(cp -> {
                WeathercamStationFeatureSimpleV1 weathercamStationFeatureSimpleV1 = weathercamFeatureMappedByCameraId.get(cp.getCameraId());
                if (weathercamStationFeatureSimpleV1 == null) {
                    weathercamStationFeatureSimpleV1 = convertToSimpleFeature(cp);
                    weathercamFeatureMappedByCameraId.put(cp.getCameraId(), weathercamStationFeatureSimpleV1);
                    weathercamStationFeatureSimpleV1s.add(weathercamStationFeatureSimpleV1);
                }
                weathercamStationFeatureSimpleV1.getProperties().addPreset(convertToSimplePreset(cp));
            });

        return new WeathercamStationFeatureCollectionSimpleV1(dataLastUpdated, dataLastCheckedTime, weathercamStationFeatureSimpleV1s);
    }

    private WeathercamPresetSimpleV1 convertToSimplePreset(final CameraPreset cp) {
        return new WeathercamPresetSimpleV1(cp.getPresetId(), cp.getCameraId());
    }

    private WeathercamPresetDetailedV1 convertToDetailedPreset(final CameraPreset cp) {
        return new WeathercamPresetDetailedV1(
            cp.getPresetId(),
            cp.getCameraId(),
            DataValidityHelper.nullifyUnknownValue(cp.getPresetName1()), // dto.setPresentationName
            cp.getResolution(),
            cp.getDirection(),
            cp.isInCollection(),
            getImageUrl(cp));
    }

    private String getImageUrl(final CameraPreset cp) {
        return StringUtils.appendIfMissing(weathercamBaseurl, "/") + cp.getPresetId() + ".jpg";
    }

    private WeathercamStationFeatureSimpleV1 convertToSimpleFeature(final CameraPreset cp) {

            if (log.isDebugEnabled()) {
                log.debug("Convert: " + cp);
            }

            // Camera properties
            final WeathercamStationPropertiesSimpleV1 properties =
                new WeathercamStationPropertiesSimpleV1(cp.getCameraId());

            // RoadStation properties
            final RoadStation rs = cp.getRoadStation();
            setRoadStationPropertiesSimple(properties, rs);

            return new WeathercamStationFeatureSimpleV1(getGeometry(rs), properties);
    }

    public WeathercamStationFeatureV1Detailed convertToDetailedFeature(final List<CameraPreset> cameraPresets) {

        if (cameraPresets.isEmpty()) {
            throw new IllegalArgumentException("Empty collection");
        }

        final WeathercamStationFeatureV1Detailed feature = createWeathercamFeatureDetailedV1(cameraPresets.get(0));

        cameraPresets
            .forEach(cp -> feature.getProperties().addPreset(convertToDetailedPreset(cp)));
        return feature;
    }

    private WeathercamStationFeatureV1Detailed createWeathercamFeatureDetailedV1(final CameraPreset cp) {
        if (log.isDebugEnabled()) {
            log.debug("method=createWeathercamFeatureDetailedV1 " + cp);
        }

        // Camera properties
        final WeathercamStationPropertiesDetailedV1 properties =
            new WeathercamStationPropertiesDetailedV1(cp.getCameraId(), cp.getCameraType(),
                                                      cameraPresetService.getNearestWeatherStationNaturalIdByCameraNatualId(cp.getCameraId()));

        // RoadStation properties
        final RoadStation rs = cp.getRoadStation();
        setRoadStationPropertiesDetailed(properties, rs);

        return new WeathercamStationFeatureV1Detailed(getGeometry(rs), properties);
    }
}
