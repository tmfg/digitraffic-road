package fi.livi.digitraffic.tie.metadata.converter;

import fi.livi.digitraffic.tie.metadata.geojson.Crs;
import fi.livi.digitraffic.tie.metadata.geojson.RoadStationProperties;
import fi.livi.digitraffic.tie.metadata.geojson.jackson.CrsType;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;

public class AbstractMetadataToFeatureConverter {

    protected static final Crs crs;

    static {
        crs = new Crs();
        crs.setType(CrsType.name);
        // http://docs.jhs-suositukset.fi/jhs-suositukset/JHS180_liite1/JHS180_liite1.html#H2
        // http://www.opengis.net/def/crs/EPSG/0/[code]
        // ETRS89 / TM35-FIN / EUREF-FIN (EPSG:3067)
        // http://www.opengis.net/def/crs/EPSG/0/3067
        // http://spatialreference.org/ref/epsg/3067/
        // named: urn:ogc:def:crs:EPSG::3067
        // Link: href="http://www.opengis.net/def/crs/EPSG/0/3067");
        //       type="proj4"
        // WGS84 (EPSG:4326)
        // http://www.opengis.net/def/crs/EPSG/0/4326
        // http://spatialreference.org/ref/epsg/wgs-84/
        // Named CRS: urn:ogc:def:crs:EPSG::4326
        crs.getProperties().setName("urn:ogc:def:crs:EPSG::3067");
    }

    protected static void setRoadStationProperties(final RoadStationProperties properties, final RoadStation roadStation) {
        properties.setNaturalId(roadStation.getNaturalId());
        properties.setCollectionInterval(roadStation.getCollectionInterval());
        properties.setCollectionStatus(roadStation.getCollectionStatus());
        properties.setDescription(roadStation.getDescription());
        properties.setDistance(roadStation.getDistance());
        properties.setMunicipality(roadStation.getMunicipality());
        properties.setMunicipalityCode(roadStation.getMunicipalityCode());

        properties.setProvince(roadStation.getProvince());
        properties.setProvinceCode(roadStation.getProvinceCode());
        properties.setRoadNumber(roadStation.getRoadNumber());
        properties.setRoadPart(roadStation.getRoadPart());

        properties.setName(roadStation.getName());
        properties.addName("fi", roadStation.getNameFi());
        properties.addName("sv", roadStation.getNameSv());
        properties.addName("en", roadStation.getNameEn());
    }


}
