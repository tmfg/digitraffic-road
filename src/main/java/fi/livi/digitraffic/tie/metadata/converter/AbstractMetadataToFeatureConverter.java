package fi.livi.digitraffic.tie.metadata.converter;

import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.metadata.geojson.roadstation.RoadStationProperties;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;

public class AbstractMetadataToFeatureConverter {

    protected final CoordinateConverter coordinateConverter;

    protected AbstractMetadataToFeatureConverter(final CoordinateConverter coordinateConverter) {
        this.coordinateConverter = coordinateConverter;
    }

    /**
     *
     * @param properties
     * @param roadStation
     * @throws NonPublicRoadStationException If road station is non public exception is thrown
     */
    protected static void setRoadStationProperties(final RoadStationProperties properties, final RoadStation roadStation)
            throws NonPublicRoadStationException {
        if (!roadStation.isPublic()) {
            throw new NonPublicRoadStationException("Non public RoadStation fetched for api: " + roadStation);
        }
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

        properties.setRoadAddress(roadStation.getRoadAddress());

        properties.setLiviId(roadStation.getLiviId());
        properties.setCountry(roadStation.getCountry());
        properties.setStartDate(roadStation.getStartDate());
        properties.setRepairMaintenanceDate(roadStation.getRepairMaintenanceDate());
        properties.setAnnualMaintenanceDate(roadStation.getAnnualMaintenanceDate());
        properties.setLocation(roadStation.getLocation());
        properties.setState(roadStation.getState());
        properties.setCoordinatesETRS89(getETRS89CoordinatesPoint(roadStation));
    }

    private static Point getETRS89CoordinatesPoint(RoadStation rs) {
        if (rs.getLatitude() != null && rs.getLongitude() != null) {
            if (rs.getAltitude() != null) {
                return new Point(
                        rs.getLongitude().longValue(),
                        rs.getLatitude().longValue(),
                        rs.getAltitude().longValue());
            } else {
                return new Point(
                        rs.getLongitude().longValue(),
                        rs.getLatitude().longValue());
            }
        }
        return null;
    }

    protected void setCoordinates(Feature feature, RoadStation rs) {
        Point etrS89 = getETRS89CoordinatesPoint(rs);
        if (etrS89 != null) {
                feature.setGeometry(
                        coordinateConverter.convertFromETRS89ToWGS84(etrS89));
        }
    }

}
