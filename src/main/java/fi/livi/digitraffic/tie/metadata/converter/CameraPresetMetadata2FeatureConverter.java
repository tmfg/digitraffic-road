package fi.livi.digitraffic.tie.metadata.converter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraFeature;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraPresetDto;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraProperties;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;

@Component
public final class CameraPresetMetadata2FeatureConverter extends AbstractMetadataToFeatureConverter {

    private static final Log log = LogFactory.getLog( CameraPresetMetadata2FeatureConverter.class );

    private final String weathercamBaseurl;

    @Autowired
    public CameraPresetMetadata2FeatureConverter(@Value("${weathercam.baseurl}")
                                                 String weathercamBaseurl) {
        this.weathercamBaseurl = weathercamBaseurl;
    }

    public CameraFeatureCollection convert(final List<CameraPreset> stations) {
        final CameraFeatureCollection collection = new CameraFeatureCollection();

        // Cameras mapped with cameraId
        Map<String, CameraFeature> cameraMap = new HashMap<>();

        for(final CameraPreset cp : stations) {
            // CameraPreset contains camera and preset informations and
            // camera info is duplicated on every preset db line
            // So we take camera only once
            CameraFeature feature = cameraMap.get(cp.getCameraId());
            if (feature == null) {
                feature = convert(cp);
                cameraMap.put(cp.getCameraId(), feature);
            }
            // Camera can have multiple presets, so we gather them together
            if (feature != null) {
                collection.add(feature);
                feature.getProperties().addPreset(convertPreset(cp));
            }
        }
        return collection;
    }

    private CameraPresetDto convertPreset(CameraPreset cp) {
        CameraPresetDto dto = new CameraPresetDto();
        dto.setCameraId(cp.getCameraId());
        dto.setPresetId(cp.getPresetId());
        dto.setDescription(cp.getDescription());
        dto.setNameOnDevice(cp.getPresetName2());
        dto.setPresetOrder(cp.getPresetOrder());
        dto.setPublic(cp.isPublicInternal() && cp.isPublicExternal());
        dto.setCompression(cp.getCompression());
        dto.setResolution(cp.getResolution());
        dto.setDirectionCode(cp.getDirection());
        dto.setLotjuId(cp.getLotjuId());
        dto.setCameraLotjuId(cp.getCameraLotjuId());
        dto.setPublic(cp.isPublicExternal());
        dto.setId(cp.getId());
        dto.setInCollection(cp.isInCollection());
        dto.setImageUrl(StringUtils.appendIfMissing(weathercamBaseurl, "/") + cp.getPresetId() + ".jpg");
        if ( CameraProperties.isUnknownPresentationName(cp.getPresetName1()) ) {
            dto.setPresentationName(null);
        } else {
            dto.setPresentationName(cp.getPresetName1());
        }
        return dto;
    }

    private static CameraFeature convert(final CameraPreset cp) {
        try {
            final CameraFeature f = new CameraFeature();
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
            properties.setDescription(cp.getCameraDescription());
            properties.setDefaultDirection(cp.getDefaultDirection());
            properties.setNearestRoadWeatherStationNaturalId(cp.getNearestRoadWeatherStationNaturalId());

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
        } catch (RuntimeException e) {
            log.error("Cold not convert " + cp + " to " + CameraFeature.class.getSimpleName());
            return null;
        }
    }
}
