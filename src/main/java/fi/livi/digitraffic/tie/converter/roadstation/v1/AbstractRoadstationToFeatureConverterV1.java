package fi.livi.digitraffic.tie.converter.roadstation.v1;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import fi.livi.digitraffic.tie.dto.geojson.v1.RoadStationPropertiesDetailedV1;
import fi.livi.digitraffic.tie.dto.geojson.v1.RoadStationPropertiesSimpleV1;
import fi.livi.digitraffic.tie.dto.roadstation.v1.StationRoadAddressV1;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.model.v1.RoadAddress;
import fi.livi.digitraffic.tie.model.v1.RoadStation;

public abstract class AbstractRoadstationToFeatureConverterV1 {

    protected final CoordinateConverter coordinateConverter;

    protected AbstractRoadstationToFeatureConverterV1(final CoordinateConverter coordinateConverter) {
        this.coordinateConverter = coordinateConverter;
    }

    protected static void setRoadStationPropertiesSimple(final RoadStationPropertiesSimpleV1<?> properties,
                                                         final RoadStation roadStation, final Instant childModified) {
        properties.setName(roadStation.getName());
        properties.setCollectionStatus(roadStation.getCollectionStatus());
        properties.setState(roadStation.getState());
        properties.setDataUpdatedTime(DateHelper.getGreatest(roadStation.getModified(), childModified));
    }

    protected static void setRoadStationPropertiesDetailed(final RoadStationPropertiesDetailedV1<?> properties,
                                                           final RoadStation roadStation, final Instant childModified) {
        setRoadStationPropertiesSimple(properties, roadStation, childModified);

        properties.setCollectionInterval(roadStation.getCollectionInterval());

        properties.addName("fi", roadStation.getNameFi());
        properties.addName("sv", roadStation.getNameSv());
        properties.addName("en", roadStation.getNameEn());

        properties.setLiviId(roadStation.getLiviId());
        properties.setCountry(roadStation.getCountry());
        properties.setStartTime(DateHelper.toInstant(roadStation.getStartDate()));
        properties.setRepairMaintenanceTime(DateHelper.toInstant(roadStation.getRepairMaintenanceDate()));
        properties.setAnnualMaintenanceTime(DateHelper.toInstant(roadStation.getAnnualMaintenanceDate()));
        // HOX: Removed temporary until LOTJU data is fixed in 2016
        // properties.setLocation(roadStation.getLocation());
        properties.setPurpose(roadStation.getPurpose());
        properties.setMunicipality(roadStation.getMunicipality());
        properties.setMunicipalityCode(parseIntegerOrNull(roadStation.getMunicipalityCode()));
        properties.setProvince(roadStation.getProvince());
        properties.setProvinceCode(parseIntegerOrNull(roadStation.getProvinceCode()));

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
            source.getSideCode(),
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

    private static Integer parseIntegerOrNull(final String municipalityCode) {
        try {
            return Integer.parseInt(municipalityCode);
        } catch (final Exception e) {
            return null;
        }
    }
}
