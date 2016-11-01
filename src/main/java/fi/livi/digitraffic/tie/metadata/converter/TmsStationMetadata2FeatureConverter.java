package fi.livi.digitraffic.tie.metadata.converter;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationProperties;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;

@Component
public final class TmsStationMetadata2FeatureConverter extends AbstractMetadataToFeatureConverter {

    private static final Log log = LogFactory.getLog( TmsStationMetadata2FeatureConverter.class );

    @Autowired
    private TmsStationMetadata2FeatureConverter(final CoordinateConverter coordinateConverter) {
        super(coordinateConverter);
    }

    public TmsStationFeatureCollection convert(final List<TmsStation> stations, final LocalDateTime lastUpdated) {
        final TmsStationFeatureCollection collection = new TmsStationFeatureCollection(lastUpdated);

        for(final TmsStation tms : stations) {
            try {
                collection.add(convert(tms));
            } catch (final NonPublicRoadStationException nprse) {
                //Skip non public roadstation
                log.warn("Skipping: " + nprse.getMessage());
            }
        }
        return collection;
    }

    /**
     *
     * @param tms
     * @return
     * @throws NonPublicRoadStationException If road station is non public exception is thrown
     */
    private TmsStationFeature convert(final TmsStation tms) throws NonPublicRoadStationException {
        final TmsStationFeature f = new TmsStationFeature();
        if (log.isDebugEnabled()) {
            log.debug("Convert: " + tms);
        }
        f.setId(tms.getRoadStationNaturalId());

        final TmsStationProperties properties = f.getProperties();

        // Tms station properties
        properties.setId(tms.getId());
        properties.setLotjuId(tms.getLotjuId());
        properties.setTmsNaturalId(tms.getNaturalId());
        properties.setDirection1Municipality(tms.getDirection1Municipality());
        properties.setDirection1MunicipalityCode(tms.getDirection1MunicipalityCode());
        properties.setDirection2Municipality(tms.getDirection2Municipality());
        properties.setDirection2MunicipalityCode(tms.getDirection2MunicipalityCode());
        properties.setTmsStationType(tms.getTmsStationType());
        properties.setCalculatorDeviceType(tms.getCalculatorDeviceType());
        properties.setName(tms.getName());

        // RoadStation properties
        final RoadStation rs = tms.getRoadStation();
        setRoadStationProperties(properties, rs);

        setCoordinates(f, rs);

        return f;
    }
}
