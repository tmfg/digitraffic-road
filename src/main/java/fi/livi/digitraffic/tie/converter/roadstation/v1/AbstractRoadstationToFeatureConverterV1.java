package fi.livi.digitraffic.tie.converter.roadstation.v1;

import java.math.BigDecimal;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.livi.digitraffic.tie.dto.data.v1.StationRoadAddressV1;
import fi.livi.digitraffic.tie.dto.geojson.v1.RoadStationPropertiesDetailedV1;
import fi.livi.digitraffic.tie.dto.geojson.v1.RoadStationPropertiesSimpleV1;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.model.v1.RoadAddress;
import fi.livi.digitraffic.tie.model.v1.RoadStation;

public abstract class AbstractRoadstationToFeatureConverterV1 {
    private static final Logger log = LoggerFactory.getLogger(AbstractRoadstationToFeatureConverterV1.class);

    protected final CoordinateConverter coordinateConverter;

    protected AbstractRoadstationToFeatureConverterV1(final CoordinateConverter coordinateConverter) {
        this.coordinateConverter = coordinateConverter;
    }

    protected static void setRoadStationProperties(final RoadStationPropertiesSimpleV1<?> properties,
                                                   final RoadStation roadStation) {
        properties.setName(roadStation.getName());
        properties.setCollectionStatus(roadStation.getCollectionStatus());
        properties.setState(roadStation.getState());
        properties.setMunicipality(roadStation.getMunicipality());
        properties.setMunicipalityCode(roadStation.getMunicipalityCode());
        properties.setProvince(roadStation.getProvince());
        properties.setProvinceCode(roadStation.getProvinceCode());
    }

    protected static void setRoadStationPropertiesDetailed(final RoadStationPropertiesDetailedV1<?> properties,
                                                           final RoadStation roadStation) {
        setRoadStationProperties(properties, roadStation);

        properties.setCollectionInterval(roadStation.getCollectionInterval());

        properties.addName("fi", roadStation.getNameFi());
        properties.addName("sv", roadStation.getNameSv());
        properties.addName("en", roadStation.getNameEn());

        properties.setLiviId(roadStation.getLiviId());
        properties.setCountry(roadStation.getCountry());
        properties.setStartTime(roadStation.getStartDate());
        properties.setRepairMaintenanceTime(roadStation.getRepairMaintenanceDate());
        properties.setAnnualMaintenanceTime(roadStation.getAnnualMaintenanceDate());
        properties.setLocation(roadStation.getLocation());
        properties.setPurpose(roadStation.getPurpose());
        properties.setRoadAddress(createRoaddAddress(roadStation.getRoadAddress()));
    }

    private static StationRoadAddressV1 createRoaddAddress(final RoadAddress source) {
        if (source == null) {
            return null;
        }
        return new StationRoadAddressV1(
            source.getRoadNumber(),
            source.getRoadSection(),
            source.getDistanceFromRoadSectionStart(),
            source.getCarriagewayCode(),
            source.getContractArea(),
            source.getContractAreaCode(),
            source.getRoadMaintenanceClass());
    }

    protected Point getGeometry(final RoadStation rs) {
        final Point etrS89 = getETRS89CoordinatesPoint(rs);
        if (etrS89 != null) {
            return CoordinateConverter.convertFromETRS89ToWGS84(etrS89);
        }
        return null;
    }

    private static Point getETRS89CoordinatesPoint(RoadStation rs) {
        if (rs.getLatitude() != null && rs.getLongitude() != null) {
            return new Point(
                rs.getLongitude().doubleValue(),
                rs.getLatitude().doubleValue(),
                Optional.ofNullable(rs.getAltitude()).orElse(BigDecimal.valueOf(0)).doubleValue());
        }
        return null;
    }
}
