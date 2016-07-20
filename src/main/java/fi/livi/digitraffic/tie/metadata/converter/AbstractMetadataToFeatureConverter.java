package fi.livi.digitraffic.tie.metadata.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.livi.digitraffic.tie.metadata.geojson.Crs;
import fi.livi.digitraffic.tie.metadata.geojson.CrsType;
import fi.livi.digitraffic.tie.metadata.geojson.roadstation.RoadStationProperties;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;

public class AbstractMetadataToFeatureConverter {

    private static final Logger log = LoggerFactory.getLogger(AbstractMetadataToFeatureConverter.class);

    protected static final Crs crs;

    protected AbstractMetadataToFeatureConverter() {
    }

    static {
        crs = new Crs();
        crs.setType(CrsType.name);
        /*
         * http://docs.jhs-suositukset.fi/jhs-suositukset/JHS180_liite1/JHS180_liite1.html#H2
         * http://www.opengis.net/def/crs/EPSG/0/[code]
         * ETRS89 / TM35-FIN / EUREF-FIN (EPSG:3067)
         * http://www.opengis.net/def/crs/EPSG/0/3067
         * http://spatialreference.org/ref/epsg/3067/
         * named: urn:ogc:def:crs:EPSG::3067
         * Link: href="http://www.opengis.net/def/crs/EPSG/0/3067");
         *       type="proj4"
         * WGS84 (EPSG:4326)
         * http://www.opengis.net/def/crs/EPSG/0/4326
         * http://spatialreference.org/ref/epsg/wgs-84/
         * Named CRS: urn:ogc:def:crs:EPSG::4326
         */
        crs.getProperties().setName("urn:ogc:def:crs:EPSG::3067");
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
    }


}
