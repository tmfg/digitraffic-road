package fi.livi.digitraffic.tie.converter.feature;

import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.metadata.geojson.roadstation.RoadStationProperties;
import fi.livi.digitraffic.tie.model.v1.RoadAddress;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractMetadataToFeatureConverter {
    private static final Logger log = LoggerFactory.getLogger(AbstractMetadataToFeatureConverter.class);

    protected final CoordinateConverter coordinateConverter;

    protected AbstractMetadataToFeatureConverter(final CoordinateConverter coordinateConverter) {
        this.coordinateConverter = coordinateConverter;
    }

    protected static void setRoadStationProperties(final RoadStationProperties properties, final RoadStation roadStation) {
        properties.setNaturalId(roadStation.getNaturalId());
        properties.setCollectionInterval(roadStation.getCollectionInterval());
        properties.setCollectionStatus(roadStation.getCollectionStatus());
        properties.setMunicipality(roadStation.getMunicipality());
        properties.setMunicipalityCode(roadStation.getMunicipalityCode());

        properties.setProvince(roadStation.getProvince());
        properties.setProvinceCode(roadStation.getProvinceCode());

        properties.setName(roadStation.getName());
        properties.addName("fi", roadStation.getNameFi());
        properties.addName("sv", roadStation.getNameSv());
        properties.addName("en", roadStation.getNameEn());

        if (roadStation.getRoadAddress() != null) {
            properties.setRoadAddress(copyToProperties(roadStation.getRoadAddress()));
        }

        properties.setLiviId(roadStation.getLiviId());
        properties.setCountry(roadStation.getCountry());
        properties.setStartTime(roadStation.getStartDate());
        properties.setRepairMaintenanceTime(roadStation.getRepairMaintenanceDate());
        properties.setAnnualMaintenanceTime(roadStation.getAnnualMaintenanceDate());
        properties.setLocation(roadStation.getLocation());
        properties.setState(roadStation.getState());
        properties.setCoordinatesETRS89(getETRS89CoordinatesPoint(roadStation));
        properties.setPurpose(roadStation.getPurpose());
    }

    private static RoadAddress copyToProperties(final RoadAddress source) {
        final RoadAddress copy = new RoadAddress();

        // does not copy id or sidecode, they are not used in json
        copy.setCarriagewayCode(source.getCarriagewayCode());
        copy.setContractArea(source.getContractArea());
        copy.setContractAreaCode(source.getContractAreaCode());
        copy.setDistanceFromRoadSectionStart(source.getDistanceFromRoadSectionStart());
        copy.setRoadMaintenanceClass(source.getRoadMaintenanceClass());
        copy.setRoadNumber(source.getRoadNumber());
        copy.setRoadSection(source.getRoadSection());

        return copy;
    }


    public static Point getETRS89CoordinatesPoint(RoadStation rs) {
        if (rs.getLatitude() != null && rs.getLongitude() != null) {
            if (rs.getAltitude() != null) {
                return new Point(
                        rs.getLongitude().doubleValue(),
                        rs.getLatitude().doubleValue(),
                        rs.getAltitude().doubleValue());
            } else {
                return new Point(
                        rs.getLongitude().doubleValue(),
                        rs.getLatitude().doubleValue());
            }
        }
        return null;
    }

    protected Point getGeometry(final RoadStation rs) {
        final Point etrS89 = getETRS89CoordinatesPoint(rs);
        if (etrS89 != null) {
            return CoordinateConverter.convertFromETRS89ToWGS84(etrS89);
        }
        return null;
    }

}
