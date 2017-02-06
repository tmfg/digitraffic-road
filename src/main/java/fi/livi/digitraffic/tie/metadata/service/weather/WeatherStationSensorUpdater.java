package fi.livi.digitraffic.tie.metadata.service.weather;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuWeatherStationService;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.TiesaaLaskennallinenAnturiVO;

@Service
public class WeatherStationSensorUpdater extends AbstractRoadStationSensorUpdater {
    private static final Logger log = LoggerFactory.getLogger(WeatherStationSensorUpdater.class);

    private final LotjuWeatherStationService lotjuWeatherStationService;

    @Autowired
    public WeatherStationSensorUpdater(final RoadStationSensorService roadStationSensorService,
                                       final LotjuWeatherStationService lotjuWeatherStationService) {
        super(roadStationSensorService);
        this.lotjuWeatherStationService = lotjuWeatherStationService;
    }

    /**
     * Updates all available weather road station sensors
     */
    @Transactional
    public boolean updateRoadStationSensors() {
        log.info("Update weather RoadStationSensors start");

        if (!lotjuWeatherStationService.isEnabled()) {
            log.warn("Not updating RoadStationSensor metadata because LotjuWeatherStationService not enabled");
            return false;
        }

        // Update available RoadStationSensors types to db
        final List<TiesaaLaskennallinenAnturiVO> allTiesaaLaskennallinenAnturis =
                lotjuWeatherStationService.getAllTiesaaLaskennallinenAnturis();

        boolean fixedLotjuIds = fixRoadStationSensorsWithoutLotjuId(allTiesaaLaskennallinenAnturis);

        boolean updated = updateAllRoadStationSensors(allTiesaaLaskennallinenAnturis);
        log.info("Update weather RoadStationSensors end");
        return fixedLotjuIds || updated;
    }

    private boolean fixRoadStationSensorsWithoutLotjuId(final List<TiesaaLaskennallinenAnturiVO> allTiesaaLaskennallinenAnturis) {
        final Map<Long, RoadStationSensor> currentSensorsMappedByNaturalId =
                roadStationSensorService.findAllRoadStationSensorsWithOutLotjuIdMappedByNaturalId(RoadStationType.WEATHER_STATION);

        final AtomicInteger updated = new AtomicInteger();
            allTiesaaLaskennallinenAnturis.stream().filter(anturi -> validate(anturi) ).forEach(anturi -> {
            final RoadStationSensor currentSaved = currentSensorsMappedByNaturalId.remove(Long.valueOf(anturi.getVanhaId()));
            if ( currentSaved != null ) {
                currentSaved.setLotjuId(anturi.getId());
            }
        });

        final long obsoleted = obsoleteRoadStationSensors(currentSensorsMappedByNaturalId.values());

        log.info("Obsoleted {} RoadStationSensor", obsoleted);
        log.info("Fixed {} RoadStationSensor without lotjuId", updated);
        return obsoleted > 0 || updated.get() > 0;
    }

    private boolean updateAllRoadStationSensors(final List<TiesaaLaskennallinenAnturiVO> allTiesaaLaskennallinenAnturis) {
        final Map<Long, RoadStationSensor> currentSensorsMappedByLotjuId =
                roadStationSensorService.findAllRoadStationSensorsMappedByLotjuId(RoadStationType.WEATHER_STATION);

        final List<Pair<TiesaaLaskennallinenAnturiVO, RoadStationSensor>> update = new ArrayList<>(); // WeatherStations to update
        final List<TiesaaLaskennallinenAnturiVO> insert = new ArrayList<>(); // new WeatherStations

        AtomicInteger invalid = new AtomicInteger();
        allTiesaaLaskennallinenAnturis.stream().forEach(anturi -> {
            if (validate(anturi)) {
                final RoadStationSensor currentSaved = currentSensorsMappedByLotjuId.remove(anturi.getId());

                if ( currentSaved != null ) {
                    update.add(Pair.of(anturi, currentSaved));
                } else {
                    insert.add(anturi);
                }
            } else {
                invalid.addAndGet(1);
            }
        });

        if (invalid.get() > 0) {
            log.warn("Found {} invalid TiesaaLaskennallinenAnturis from LOTJU", invalid);
        }

        // tms-stations in database, but not in server -> obsolete
        final long obsoleted = obsoleteRoadStationSensors(currentSensorsMappedByLotjuId.values());
        final int updated = updateRoadStationSensors(update);
        final int inserted = insertRoadStationSensors(insert);

        log.info("Obsoleted {} RoadStationSensors", obsoleted);
        log.info("Updated {} RoadStationSensors", updated);
        log.info("Inserted {} RoadStationSensors", inserted);

        if (insert.size() > inserted) {
            log.warn("Insert failed for {} RoadStationSensors", (insert.size()-inserted));
        }

        return obsoleted > 0 || inserted > 0 || updated > 0;
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
            log.info("Created new {}", sensor);
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
                log.info("Updated RoadStationSensor:\n{} -> \n{}",  before , ReflectionToStringBuilder.toString(sensor));
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
