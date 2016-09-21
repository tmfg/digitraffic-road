package fi.livi.digitraffic.tie.metadata.converter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.helper.DataValidyHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraPresetDto;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraProperties;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;

@Component
public final class CameraPresetMetadata2FeatureConverter extends AbstractMetadataToFeatureConverter {

    private static final Log log = LogFactory.getLog( CameraPresetMetadata2FeatureConverter.class );

    private final String weathercamBaseurl;

    @Autowired
    public CameraPresetMetadata2FeatureConverter(@Value("${weathercam.base-url}") final
                                                 String weathercamBaseurl) {
        this.weathercamBaseurl = weathercamBaseurl;
    }

    public CameraStationFeatureCollection convert(final List<CameraPreset> cameraPresets, final LocalDateTime lastUpdated) {
        final CameraStationFeatureCollection collection = new CameraStationFeatureCollection(lastUpdated);

        // Cameras mapped with cameraId
        final Map<String, CameraStationFeature> cameraStationMap = new HashMap<>();

        for(final CameraPreset cp : cameraPresets) {
            // CameraPreset contains camera and preset informations and
            // camera info is duplicated on every preset db line
            // So we take camera only once
            CameraStationFeature cameraStationFeature = cameraStationMap.get(cp.getCameraId());
            if (cameraStationFeature == null) {
                try {
                    cameraStationFeature = convert(cp);
                    cameraStationMap.put(cp.getCameraId(), cameraStationFeature);
                    collection.add(cameraStationFeature);
                } catch (final NonPublicRoadStationException nprse) {
                    //Skip non public roadstation
                    log.warn("Skipping: " + nprse.getMessage());
                    continue;
                }
            }
            cameraStationFeature.getProperties().addPreset(convertPreset(cp));
        }

        return collection;
    }

    private CameraPresetDto convertPreset(final CameraPreset cp) {
        final CameraPresetDto dto = new CameraPresetDto();
        dto.setCameraId(cp.getCameraId());
        dto.setPresetId(cp.getPresetId());
        dto.setPresentationName(DataValidyHelper.nullifyUnknownValue(cp.getPresetName1()));
        dto.setNameOnDevice(DataValidyHelper.nullifyUnknownValue(cp.getPresetName2()));
        dto.setPresetOrder(cp.getPresetOrder());
        dto.setResolution(cp.getResolution());
        dto.setDirectionCode(cp.getDirection());
        dto.setLotjuId(cp.getLotjuId());
        dto.setCameraLotjuId(cp.getCameraLotjuId());
        dto.setId(cp.getId());
        dto.setInCollection(cp.isInCollection());
        dto.setImageUrl(StringUtils.appendIfMissing(weathercamBaseurl, "/") + cp.getPresetId() + ".jpg");
        return dto;
    }

    /**
     *
     * @param cp
     * @return
     * @throws NonPublicRoadStationException If road station is non public exception is thrown
     */
    private static CameraStationFeature convert(final CameraPreset cp) throws NonPublicRoadStationException {
            final CameraStationFeature f = new CameraStationFeature();
            if (log.isDebugEnabled()) {
                log.debug("Convert: " + cp);
            }
            f.setId(cp.getCameraId());

            final CameraProperties properties = f.getProperties();

            // Camera properties
            properties.setId(cp.getId());

            properties.setLotjuId(cp.getCameraLotjuId());
            properties.setCameraId(cp.getCameraId());
            properties.setCameraType(cp.getCameraType());
            properties.setNearestWeatherStationNaturalId(cp.getNearestWeatherStationNaturalId());

            // RoadStation properties
            final RoadStation rs = cp.getRoadStation();
            setRoadStationProperties(properties, rs);

            if (rs.getLatitude() != null && rs.getLongitude() != null) {
                if (rs.getAltitude() != null) {
                    f.setGeometry(new Point(rs.getLongitude().longValue(),
                                            rs.getLatitude().longValue(),
                                            rs.getAltitude().longValue()));
                } else {
                    f.setGeometry(new Point(rs.getLongitude().longValue(),
                                            rs.getLatitude().longValue()));
                }
                f.getGeometry().setCrs(crs);
            }

            return f;
    }
}
