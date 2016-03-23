package fi.livi.digitraffic.tie.converter;

import java.util.List;

import fi.livi.digitraffic.tie.geojson.Point;
import fi.livi.digitraffic.tie.geojson.camera.CameraPresetFeature;
import fi.livi.digitraffic.tie.geojson.camera.CameraPresetFeatureCollection;
import fi.livi.digitraffic.tie.geojson.camera.CameraPresetProperties;
import fi.livi.digitraffic.tie.model.CameraPreset;
import fi.livi.digitraffic.tie.model.RoadStation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

@Component
public final class CameraPresetMetadata2FeatureConverter extends AbstractMetadataToFeatureConverter {

    private static final Log log = LogFactory.getLog( CameraPresetMetadata2FeatureConverter.class );

    private CameraPresetMetadata2FeatureConverter() {}


    public static CameraPresetFeatureCollection convert(final List<CameraPreset> stations) {
        final CameraPresetFeatureCollection collection = new CameraPresetFeatureCollection();

        for(final CameraPreset cp : stations) {
            collection.add(convert(cp));
        }
        return collection;
    }

    private static CameraPresetFeature convert(final CameraPreset cp) {
        final CameraPresetFeature f = new CameraPresetFeature();
        if (log.isDebugEnabled()) {
            log.debug("Convert: " + cp.toString());
        }
        f.setId(Long.toString(cp.getId()));

        CameraPresetProperties properties = f.getProperties();

        // Lam station properties
        properties.setId(cp.getId());
        properties.setCameraId(cp.getCameraId());
        properties.setPresetId(cp.getPresetId());
        properties.setCameraType(cp.getCameraType());
        properties.setPresetName1(cp.getPresetName1());
        properties.setPresetName2(cp.getPresetName2());
        properties.setPresetOrder(cp.getPresetOrder());
        properties.setPublic(cp.isPublicInternal() && cp.isPublicExternal());
        properties.setInCollection(cp.isInCollection());
        properties.setCompression(cp.getCompression());
        properties.setNameOnDevice(cp.getNameOnDevice());
        properties.setDefaultDirection(cp.getDefaultDirection());
        properties.setResolution(cp.getResolution());
        properties.setDirection(cp.getDirection());
        properties.setDelay(cp.getDelay());

        // RoadStation properties
        RoadStation rs = cp.getRoadStation();
        setRoadStationProperties(properties, rs);
        properties.setNaturalId(rs.getNaturalId());
        properties.setCollectionInterval(rs.getCollectionInterval());
        properties.setCollectionStatus(rs.getCollectionStatus());
        properties.setDescription(rs.getDescription());
        properties.setDistance(rs.getDistance());
        properties.setMunicipality(rs.getMunicipality());
        properties.setMunicipalityCode(rs.getMunicipalityCode());

        properties.setProvince(rs.getProvince());
        properties.setProvinceCode(rs.getProvinceCode());
        properties.setRoadNumber(rs.getRoadNumber());
        properties.setRoadPart(rs.getRoadPart());

        properties.addName("fi", rs.getNameFi());
        properties.addName("sv", rs.getNameSv());
        properties.addName("en", rs.getNameEn());


        if (rs.getLatitude() != null && rs.getLongitude() != null) {
            if (rs.getAltitude() != null) {
                f.setGeometry(new Point(rs.getLatitude().longValue(),
                                        rs.getLongitude().longValue(),
                                        rs.getAltitude().longValue()));
            } else {
                f.setGeometry(new Point(rs.getLatitude().longValue(),
                                        rs.getLongitude().longValue()));
            }
            f.getGeometry().setCrs(crs);
        }

        return f;
    }
}
