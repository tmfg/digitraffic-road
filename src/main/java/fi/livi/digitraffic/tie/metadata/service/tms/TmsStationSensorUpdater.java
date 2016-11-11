package fi.livi.digitraffic.tie.metadata.service.tms;

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

import fi.livi.digitraffic.tie.helper.DataValidyHelper;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.AbstractRoadStationSensorUpdater;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuTmsStationClient;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamLaskennallinenAnturiVO;

@Service
public class TmsStationSensorUpdater extends AbstractRoadStationSensorUpdater {
    private static final Logger log = LoggerFactory.getLogger(TmsStationSensorUpdater.class);

    private final LotjuTmsStationClient lotjuTmsStationClient;

    @Autowired
    public TmsStationSensorUpdater(final RoadStationSensorService roadStationSensorService,
                                   final LotjuTmsStationClient lotjuTmsStationClient) {
        super(roadStationSensorService);
        this.lotjuTmsStationClient = lotjuTmsStationClient;
    }

    /**
     * Updates all available tms road station sensors
     */
    @Transactional
    public boolean updateRoadStationSensors() {
        log.info("Update TMS RoadStationSensors start");

        if (lotjuTmsStationClient == null) {
            log.warn("Not updating TMS stations sensors because no lotjuTmsStationClient defined");
            return false;
        }

        // Update available RoadStationSensors types to db
        List<LamLaskennallinenAnturiVO> allLamLaskennallinenAnturis =
                lotjuTmsStationClient.getAllLamLaskennallinenAnturis();

        boolean updated = updateAllRoadStationSensors(allLamLaskennallinenAnturis);
        log.info("Update TMS RoadStationSensors end");
        return updated;
    }

    private boolean updateAllRoadStationSensors(final List<LamLaskennallinenAnturiVO> allLamLaskennallinenAnturis) {
        final Map<Long, RoadStationSensor> currentNaturalIdToSensorMap =
                roadStationSensorService.findAllRoadStationSensorsMappedByNaturalId(RoadStationType.TMS_STATION);

        final List<RoadStationSensor> obsolete = new ArrayList<>(); // obsolete WeatherStations
        final List<Pair<LamLaskennallinenAnturiVO, RoadStationSensor>> update = new ArrayList<>(); // WeatherStations to update
        final List<LamLaskennallinenAnturiVO> insert = new ArrayList<>(); // new WeatherStations

        int invalid = 0;
        for (final LamLaskennallinenAnturiVO anturi : allLamLaskennallinenAnturis) {
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
            obsoleteRoadStationSensor.isStatusSensor();
        }

        if (invalid > 0) {
            log.warn("Found " + invalid + " LamLaskennallinenAnturi from LOTJU");
        }

        final int obsoleted = obsoleteRoadStationSensors(obsolete);
        final int updated = updateRoadStationSensors(update);
        final int inserted = insertRoadStationSensors(insert);

        log.info("Obsoleted " + obsoleted + " RoadStationSensors");
        log.info("Updated " + updated + " RoadStationSensors");
        log.info("Inserted " + inserted + " RoadStationSensors");

        if (insert.size() > inserted) {
            log.warn("Insert failed for " + (insert.size()-inserted) + " RoadStationSensors");
        }

        return obsoleted > 0 || inserted > 0 || updated > 0;
    }

    private static boolean validate(LamLaskennallinenAnturiVO anturi) {
        return anturi.getId() != null && anturi.getVanhaId() != null;
    }

    private int insertRoadStationSensors(final List<LamLaskennallinenAnturiVO> insert) {

        int counter = 0;
        for (final LamLaskennallinenAnturiVO anturi : insert) {
            RoadStationSensor sensor = new RoadStationSensor();
            updateRoadStationSensorAttributes(anturi, sensor);
            sensor = roadStationSensorService.saveRoadStationSensor(sensor);
            log.info("Created new " + sensor);
            counter++;
        }
        return counter;
    }

    private static int updateRoadStationSensors(final List<Pair<LamLaskennallinenAnturiVO, RoadStationSensor>> update) {

        int counter = 0;
        for (final Pair<LamLaskennallinenAnturiVO, RoadStationSensor> pair : update) {

            final LamLaskennallinenAnturiVO anturi = pair.getLeft();
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

    private static boolean updateRoadStationSensorAttributes(final LamLaskennallinenAnturiVO from, final RoadStationSensor to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        to.setRoadStationType(RoadStationType.TMS_STATION);
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
        to.setUnit(DataValidyHelper.nullifyUnknownValue(from.getYksikko()));

        return HashCodeBuilder.reflectionHashCode(to) != hash;
    }
}
