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
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamFeatureCollectionV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamFeatureV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamPresetV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamPropertiesV1;
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
    private CameraPresetService cameraPresetService;

    @Autowired
    public WeathercamPresetToFeatureConverter(@Value("${weathercam.baseUrl}")
                                              final String weathercamBaseurl,
                                              final CoordinateConverter coordinateConverter,
                                              final CameraPresetService cameraPresetService) {
        super(coordinateConverter);
        this.weathercamBaseurl = weathercamBaseurl;
        this.cameraPresetService = cameraPresetService;
    }

    public WeathercamFeatureCollectionV1 convert(final List<CameraPreset> cameraPresets, final Instant dataLastUpdated, final Instant dataLastCheckedTime) {

        // Cameras mapped with cameraId
        final Map<String, WeathercamFeatureV1> weathercamFeatureMappedByCameraId = new HashMap<>();
        final List<WeathercamFeatureV1> weathercamFeatureV1s = new ArrayList<>();
        final Map<String, Long> weatherStationsMapByCameraId = cameraPresetService.getNearestWeatherStationNaturalIdMappedByCameraId();
        // CameraPreset contains camera and preset informations and
        // camera info is duplicated on every preset db line
        // So we take camera only once
        cameraPresets
            .forEach(cp -> {
                WeathercamFeatureV1 weathercamFeatureV1 = weathercamFeatureMappedByCameraId.get(cp.getCameraId());
                if (weathercamFeatureV1 == null) {
                    weathercamFeatureV1 = convert(weatherStationsMapByCameraId, cp);
                    weathercamFeatureMappedByCameraId.put(cp.getCameraId(), weathercamFeatureV1);
                    weathercamFeatureV1s.add(weathercamFeatureV1);
                }
                weathercamFeatureV1.getProperties().addPreset(convertPreset(cp));
            });

        return new WeathercamFeatureCollectionV1(dataLastUpdated, dataLastCheckedTime, weathercamFeatureV1s);
    }

    private WeathercamPresetV1 convertPreset(final CameraPreset cp) {
        final WeathercamPresetV1 dto = new WeathercamPresetV1();
        dto.setCameraId(cp.getCameraId());
        dto.setId(cp.getPresetId());
        dto.setPresentationName(DataValidityHelper.nullifyUnknownValue(cp.getPresetName1()));
        dto.setResolution(cp.getResolution());
        dto.setDirectionCode(cp.getDirection());
        dto.setInCollection(cp.isInCollection());
        dto.setImageUrl(StringUtils.appendIfMissing(weathercamBaseurl, "/") + cp.getPresetId() + ".jpg");
        return dto;
    }

    private WeathercamFeatureV1 convert(final Map<String, Long> nearestWeatherStationMap, final CameraPreset cp) {

            if (log.isDebugEnabled()) {
                log.debug("Convert: " + cp);
            }

            // Camera properties
            final WeathercamPropertiesV1 properties = new WeathercamPropertiesV1();
            properties.setId(cp.getCameraId());
            properties.setCameraType(cp.getCameraType());

            if(cp.getNearestWeatherStation() != null) {
                properties.setNearestWeatherStationId(nearestWeatherStationMap.get(cp.getCameraId()));
            }

            // RoadStation properties
            final RoadStation rs = cp.getRoadStation();
            setRoadStationProperties(properties, rs);

            return new WeathercamFeatureV1(getGeometry(rs), properties);
    }
}
