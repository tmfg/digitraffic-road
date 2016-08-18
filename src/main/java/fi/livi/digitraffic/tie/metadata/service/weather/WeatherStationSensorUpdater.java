package fi.livi.digitraffic.tie.metadata.service.weather;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.AbstractRoadStationSensorUpdater;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuWeatherStationClient;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.TiesaaLaskennallinenAnturiVO;

@Service
public class WeatherStationSensorUpdater extends AbstractRoadStationSensorUpdater {
    private static final Logger log = LoggerFactory.getLogger(WeatherStationSensorUpdater.class);

    private final LotjuWeatherStationClient lotjuWeatherStationClient;

    @Autowired
    public WeatherStationSensorUpdater(final RoadStationSensorService roadStationSensorService,
                                       final LotjuWeatherStationClient lotjuWeatherStationClient) {
        super(roadStationSensorService);
        this.lotjuWeatherStationClient = lotjuWeatherStationClient;
    }

    /**
     * Updates all available weather road station sensors
     */
    @Transactional
    public boolean updateRoadStationSensors() {
        log.info("Update weather RoadStationSensors start");

        if (lotjuWeatherStationClient == null) {
            log.warn("Not updating RoadStationSensor metadatas because no lotjuWeatherStationClient defined");
            return false;
        }

        // Update available RoadStationSensors types to db
        final List<TiesaaLaskennallinenAnturiVO> allTiesaaLaskennallinenAnturis =
                lotjuWeatherStationClient.getAllTiesaaLaskennallinenAnturis();

        boolean uptaded = updateAllRoadStationSensors(allTiesaaLaskennallinenAnturis);
        log.info("Update weather RoadStationSensors end");
        return uptaded;
    }

    private boolean updateAllRoadStationSensors(final List<TiesaaLaskennallinenAnturiVO> allTiesaaLaskennallinenAnturis) {
        final Map<Long, RoadStationSensor> currentNaturalIdToSensorMap =
                roadStationSensorService.findAllRoadStationSensorsMappedByNaturalId(RoadStationType.WEATHER_STATION);

        final List<RoadStationSensor> obsolete = new ArrayList<>(); // obsolete WeatherStations
        final List<Pair<TiesaaLaskennallinenAnturiVO, RoadStationSensor>> update = new ArrayList<>(); // WeatherStations to update
        final List<TiesaaLaskennallinenAnturiVO> insert = new ArrayList<>(); // new WeatherStations

        int invalid = 0;
        for (final TiesaaLaskennallinenAnturiVO anturi : allTiesaaLaskennallinenAnturis) {
            if (validate(anturi)) {
                final RoadStationSensor currentSaved = currentNaturalIdToSensorMap.remove(Long.valueOf(anturi.getVanhaId()));

                if ( currentSaved != null ) {
                    update.add(Pair.of(anturi, currentSaved));
                } else {
                    insert.add(anturi);
                }
            } else {
                invalid++;
            }
        }

        // road station sensors in database, but not in server
        for (final RoadStationSensor obsoleteRoadStationSensor : currentNaturalIdToSensorMap.values()) {
            obsolete.add(obsoleteRoadStationSensor);
        }

        if (invalid > 0) {
            log.warn("Found " + invalid + " TiesaaLaskennallinenAnturi from LOTJU");
        }

        final int obsoleted = obsoleteRoadStationSensors(obsolete);
        final int uptaded = updateRoadStationSensors(update);
        final int inserted = insertRoadStationSensors(insert);

        log.info("Obsoleted " + obsoleted + " RoadStationSensors");
        log.info("Uptaded " + uptaded + " RoadStationSensors");
        log.info("Inserted " + inserted + " RoadStationSensors");

        if (insert.size() > inserted) {
            log.warn("Insert failed for " + (insert.size()-inserted) + " RoadStationSensors");
        }

        return obsoleted > 0 || inserted > 0 || uptaded > 0;
    }

    private static boolean validate(final TiesaaLaskennallinenAnturiVO anturi) {
        return anturi.getId() != null && anturi.getVanhaId() != null;
    }

    private int insertRoadStationSensors(final List<TiesaaLaskennallinenAnturiVO> insert) {

        int counter = 0;
        for (final TiesaaLaskennallinenAnturiVO anturi : insert) {
            RoadStationSensor sensor = new RoadStationSensor();
            updateRoadStationSensorAttributes(anturi, sensor);
            sensor = roadStationSensorService.saveRoadStationSensor(sensor);
            log.info("Created new " + sensor);
            counter++;
        }
        return counter;
    }

    private static int updateRoadStationSensors(final List<Pair<TiesaaLaskennallinenAnturiVO, RoadStationSensor>> update) {

        int counter = 0;
        for (final Pair<TiesaaLaskennallinenAnturiVO, RoadStationSensor> pair : update) {

            final TiesaaLaskennallinenAnturiVO anturi = pair.getLeft();
            final RoadStationSensor sensor = pair.getRight();
            log.debug("Updating " + sensor);

            final String before = ReflectionToStringBuilder.toString(sensor);
            if ( updateRoadStationSensorAttributes(anturi, sensor) ) {
                counter++;
                log.info("Updated RoadStationSensor:\n" + before + " -> \n" + ReflectionToStringBuilder.toString(sensor));
            }
        }
        return counter;
    }

    private static boolean updateRoadStationSensorAttributes(final TiesaaLaskennallinenAnturiVO from, final RoadStationSensor to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        to.setRoadStationType(RoadStationType.WEATHER_STATION);
        to.setObsolete(false);
        to.setObsoleteDate(null);

        to.setLotjuId(from.getId());
        to.setNaturalId(from.getVanhaId());
        if (to.getName() == null) {
            to.setName(from.getNimi());
        }
        to.setNameFi(from.getNimi());
        to.setShortNameFi(from.getLyhytNimi());
        to.setDescription(from.getKuvaus());
        to.setAccuracy(from.getTarkkuus());
        to.setUnit(from.getYksikko());

        return HashCodeBuilder.reflectionHashCode(to) != hash;
    }
}