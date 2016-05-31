package fi.livi.digitraffic.tie.metadata.converter;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraPresetFeature;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraPresetFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraPresetProperties;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;

@Component
public final class CameraPresetMetadata2FeatureConverter extends AbstractMetadataToFeatureConverter {

    private static final Log log = LogFactory.getLog( CameraPresetMetadata2FeatureConverter.class );

    private CameraPresetMetadata2FeatureConverter() {}

    public static CameraPresetFeatureCollection convert(final List<CameraPreset> stations) {
        final CameraPresetFeatureCollection collection = new CameraPresetFeatureCollection();

        for(final CameraPreset cp : stations) {
            CameraPresetFeature feature = convert(cp);
            if (feature != null) {
                collection.add(feature);
            }
        }
        return collection;
    }

    private static CameraPresetFeature convert(final CameraPreset cp) {
        try {
            final CameraPresetFeature f = new CameraPresetFeature();
            if (log.isDebugEnabled()) {
                log.debug("Convert: " + cp);
            }
            f.setId(cp.getPresetId());

            final CameraPresetProperties properties = f.getProperties();

            // Lam station properties
            properties.setId(cp.getId());
            properties.setLotjuId(cp.getLotjuId());
            properties.setCameraId(cp.getCameraId());
            properties.setPresetId(cp.getPresetId());
            properties.setCameraType(cp.getCameraType());

            if ( CameraPresetProperties.isUnknownPresentationName(cp.getPresetName1()) ) {
                properties.setPresentationName(null);
            } else {
                properties.setPresentationName(cp.getPresetName1());
            }

            properties.setPresetDescription(cp.getDescription());
            properties.setNameOnDevice(cp.getPresetName2());
            properties.setPresetOrder(cp.getPresetOrder());
            properties.setPublic(cp.isPublicInternal() && cp.isPublicExternal());
            properties.setInCollection(cp.isInCollection() != null ? cp.isInCollection() : false);
            properties.setCompression(cp.getCompression());
            properties.setDefaultDirection(cp.getDefaultDirection());
            properties.setResolution(cp.getResolution());
            properties.setDirectionCode(cp.getDirection());
            properties.setDelay(cp.getDelay());
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
            log.error("Cold not convert " + cp + " to " + CameraPresetFeature.class.getSimpleName());
            return null;
        }
    }
}
