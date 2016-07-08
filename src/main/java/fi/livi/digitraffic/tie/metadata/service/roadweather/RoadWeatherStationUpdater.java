package fi.livi.digitraffic.tie.metadata.service.roadweather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.RoadWeatherStation;
import fi.livi.digitraffic.tie.metadata.model.RoadWeatherStationType;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuRoadWeatherStationClient;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.TiesaaAsemaVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.TiesaaLaskennallinenAnturiVO;

@Service
public class RoadWeatherStationUpdater extends AbstractRoadWeatherRoadStationUpdater {
    private static final Logger log = Logger.getLogger(RoadWeatherStationUpdater.class);

    private final RoadWeatherStationService roadWeatherStationService;
    private final StaticDataStatusService staticDataStatusService;
    private final LotjuRoadWeatherStationClient lotjuRoadWeatherStationClient;

    @Autowired
    public RoadWeatherStationUpdater(final RoadStationService roadStationService,
                                     final RoadWeatherStationService roadWeatherStationService,
                                     final StaticDataStatusService staticDataStatusService,
                                     final LotjuRoadWeatherStationClient lotjuRoadWeatherStationClient) {
        super(roadStationService);
        this.roadWeatherStationService = roadWeatherStationService;
        this.staticDataStatusService = staticDataStatusService;
        this.lotjuRoadWeatherStationClient = lotjuRoadWeatherStationClient;
    }

    /**
     * Updates road weather stations
     */
    @Transactional
    public void updateRoadWeatherStations() {
        log.info("Update RoadWeatherStations start");

        if (lotjuRoadWeatherStationClient == null) {
            log.warn("Not updating WeatherStations metadatas because no lotjuRoadWeatherStationClient defined");
            return;
        }

        final List<TiesaaAsemaVO> tiesaaAsemas = lotjuRoadWeatherStationClient.getTiesaaAsemmas();

        if (log.isDebugEnabled()) {
            for (final TiesaaAsemaVO tsa : tiesaaAsemas) {
                log.debug(ToStringBuilder.reflectionToString(tsa));
            }
        }

        final boolean updateStaticDataStatus = updateRoadWeatherStations(tiesaaAsemas);
        updateRoasWeatherStationStaticDataStatus(updateStaticDataStatus);

        log.info("Update RoadWeatherStations end");
    }

    /**
     * Updates all available road station sensors
     */
    @Transactional
    public void updateRoadStationSensors() {
        log.info("Update RoadStationSensors start");

        if (lotjuRoadWeatherStationClient == null) {
            log.warn("Not updating RoadStationSensor metadatas because no lotjuRoadWeatherStationClient defined");
            return;
        }

        // Update available RoadStationSensors types to db
        List<TiesaaLaskennallinenAnturiVO> allTiesaaLaskennallinenAnturis =
                lotjuRoadWeatherStationClient.getAllTiesaaLaskennallinenAnturis();

        updateAllRoadStationSensors(allTiesaaLaskennallinenAnturis);
        log.info("Update RoadStationSensors end");
    }

    /**
     * Updates all available sensors of road stations
     */
    @Transactional
    public void updateRoadWeatherStationsRoadStationSensors() {
        log.info("Update RoadWeatherStationsRoadStationSensors start");

        if (lotjuRoadWeatherStationClient == null) {
            log.warn("Not updating RoadWeatherStations metadatas because no lotjuRoadWeatherStationClient defined");
            return;
        }

        // Update sensors of road stations
        // Get current RoadWeatherStations
        final Map<Long, RoadWeatherStation> currentLotjuIdToRoadWeatherStationsMap =
                roadWeatherStationService.findAllRoadWeatherStationsMappedByLotjuId();
        Set<Long> rwsLotjuIds = currentLotjuIdToRoadWeatherStationsMap.keySet();
        // Get sensors for current RoadWeatherStations
        Map<Long, List<TiesaaLaskennallinenAnturiVO>> currentLRoadWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap =
                        lotjuRoadWeatherStationClient.getTiesaaLaskennallinenAnturis(rwsLotjuIds);
        // Update sensros of road stations
        final boolean updateStaticDataStatus =
                updateSensorsOfRoadStations(currentLRoadWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap,
                                            currentLotjuIdToRoadWeatherStationsMap);
        updateRoasWeatherSensorStaticDataStatus(updateStaticDataStatus);

        log.info("Update RoadWeatherStationsRoadStationSensors end");
    }

    private boolean updateAllRoadStationSensors(List<TiesaaLaskennallinenAnturiVO> allTiesaaLaskennallinenAnturis) {
        Map<Long, RoadStationSensor> currentNaturalIdToSensorMap =
                roadWeatherStationService.findAllRoadStationSensorsMappedByNaturalId();

        final List<RoadStationSensor> obsolete = new ArrayList<>(); // obsolete RoadWeatherStations
        final List<Pair<TiesaaLaskennallinenAnturiVO, RoadStationSensor>> update = new ArrayList<>(); // RoadWeatherStations to update
        final List<TiesaaLaskennallinenAnturiVO> insert = new ArrayList<>(); // new RoadWeatherStations

        int invalid = 0;
        for (TiesaaLaskennallinenAnturiVO anturi : allTiesaaLaskennallinenAnturis) {
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
        for (RoadStationSensor obsoleteRoadStationSensor : currentNaturalIdToSensorMap.values()) {
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

        return obsoleted > 0 || inserted > 0;
    }


    private void updateRoasWeatherStationStaticDataStatus(final boolean updateStaticDataStatus) {
        staticDataStatusService.updateStaticDataStatus(StaticDataStatusService.StaticStatusType.ROAD_WEATHER, updateStaticDataStatus);
    }

    private void updateRoasWeatherSensorStaticDataStatus(final boolean updateStaticDataStatus) {
        staticDataStatusService.updateStaticDataStatus(StaticDataStatusService.StaticStatusType.ROAD_WEATHER_SENSOR, updateStaticDataStatus);
    }

    private boolean updateRoadWeatherStations(final List<TiesaaAsemaVO> tiesaaAsemas) {

        final Map<Long, RoadWeatherStation> currentLotjuIdToRoadWeatherStationMap =
                roadWeatherStationService.findAllRoadWeatherStationsMappedByLotjuId();

        final List<RoadWeatherStation> obsolete = new ArrayList<>(); // obsolete RoadWeatherStations
        final List<Pair<TiesaaAsemaVO, RoadWeatherStation>> update = new ArrayList<>(); // RoadWeatherStations to update
        final List<TiesaaAsemaVO> insert = new ArrayList<>(); // new RoadWeatherStations

        int invalid = 0;
        for (final TiesaaAsemaVO tsa : tiesaaAsemas) {

            if (validate(tsa)) {
                final RoadWeatherStation currentSaved = currentLotjuIdToRoadWeatherStationMap.remove(tsa.getId());

                if ( currentSaved != null &&
                     (CollectionStatus.isPermanentlyDeletedKeruunTila(tsa.getKeruunTila()) ||
                             Objects.equals(tsa.isJulkinen(), false) ) ) {
                    // if removed permanently or not public -> obsolete
                    obsolete.add(currentSaved);
                } else if ( currentSaved != null) {
                    update.add(Pair.of(tsa, currentSaved));
                } else {
                    insert.add(tsa);
                }
            } else {
                invalid++;
            }

        }

        if (invalid > 0) {
            log.warn("Found " + invalid + " TiesaaAsema from LOTJU");
        }

        // rws in database, but not in server
        obsolete.addAll(currentLotjuIdToRoadWeatherStationMap.values());

        final int obsoleted = obsoleteWeatherStations(obsolete);
        final int uptaded = updateRoadWeatherStationsRoadStationSensors(update);
        final int inserted = insertRoadWeatherStations(insert);

        log.info("Obsoleted " + obsoleted + " RoadWeatherStations");
        log.info("Uptaded " + uptaded + " RoadWeatherStations");
        log.info("Inserted " + inserted + " RoadWeatherStations");
        if (insert.size() > inserted) {
            log.warn("Insert failed for " + (insert.size()-inserted) + " RoadWeatherStations");
        }
        return obsoleted > 0 || inserted > 0;
    }

    private int updateRoadWeatherStationsRoadStationSensors(final List<Pair<TiesaaAsemaVO, RoadWeatherStation>> update) {

        Map<Long, RoadStation> orphansNaturalIdToRoadStationMap =
                roadStationService.findOrphansByTypeMappedByNaturalId(RoadStationType.LAM_STATION);

        int counter = 0;
        for (final Pair<TiesaaAsemaVO, RoadWeatherStation> pair : update) {

            final TiesaaAsemaVO tsa = pair.getLeft();
            final RoadWeatherStation rws = pair.getRight();

            int hash = HashCodeBuilder.reflectionHashCode(rws);
            String before = ReflectionToStringBuilder.toString(rws);

            RoadStation rs = rws.getRoadStation();
            if (rs == null) {
                final Integer naturalId = tsa.getVanhaId();

                rs = naturalId != null ? orphansNaturalIdToRoadStationMap.get(naturalId.longValue()) : null;
                if (rs == null) {
                    rs = new RoadStation(RoadStationType.WEATHER_STATION);
                    if (naturalId != null) {
                        orphansNaturalIdToRoadStationMap.put(naturalId.longValue(), rs);
                    }
                }
                rws.setRoadStation(rs);
            }

            setRoadAddressIfNotSet(rs);

            if ( updateRoadWeatherStationAttributes(tsa, rws) ||
                 hash != HashCodeBuilder.reflectionHashCode(rws) ) {
                counter++;
                log.info("Updated RoadWeatherStation:\n" + before + " -> \n" + ReflectionToStringBuilder.toString(rws));
            }

            if (rs.getRoadAddress() != null && rs.getRoadAddress().getId() == null) {
                roadStationService.save(rs.getRoadAddress());
                log.info("Created new RoadAddress " + rs.getRoadAddress());
            }
            if (rws.getRoadStation().getId() == null) {
                roadStationService.save(rws.getRoadStation());
                log.info("Created new RoadStation " + rws.getRoadStation());
            }
         }
        return counter;
    }

    private boolean updateSensorsOfRoadStations(
            Map<Long, List<TiesaaLaskennallinenAnturiVO>> currentRoadWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap,
            Map<Long, RoadWeatherStation> currentLotjuIdToRoadWeatherStationsMap) {

        Map<Long, RoadStationSensor> allSensors = roadWeatherStationService.findAllRoadStationSensorsMappedByNaturalId();

        Iterator<Long> iter = currentRoadWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap.keySet().iterator();

        int countAdd = 0;
        int countRemove = 0;
        while (iter.hasNext()) {
            Long rwsLotjuId = iter.next();
            List<TiesaaLaskennallinenAnturiVO> rwsAnturis = currentRoadWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap.get(rwsLotjuId);
            iter.remove();

            RoadWeatherStation rws = currentLotjuIdToRoadWeatherStationsMap.get(rwsLotjuId);

            if (rws == null) {
                log.error("No RoadWeatherStation found for lotjuId " + rwsLotjuId);
                continue;
            }

            RoadStation rs = rws.getRoadStation();

            List<RoadStationSensor> sensors = rws.getRoadStation().getRoadStationSensors();
            Map<Long, RoadStationSensor> naturalIdToSensorMap = new HashMap<>();
            for (RoadStationSensor sensor : sensors) {
                naturalIdToSensorMap.put(sensor.getNaturalId(), sensor);
            }

            for (TiesaaLaskennallinenAnturiVO rwsAnturi : rwsAnturis) {
                Long sensorNaturalId = Long.valueOf(rwsAnturi.getVanhaId());
                RoadStationSensor sensor = naturalIdToSensorMap.remove(sensorNaturalId);
                // road station doesn't have mapping for sensor -> add it
                if ( sensor == null ) {
                    RoadStationSensor add = allSensors.get(sensorNaturalId);
                    if (add == null) {
                        log.error("No RoadStationSensorDto found with naturalId " + sensorNaturalId);
                    } else {
                        rs.getRoadStationSensors().add(add);
                        countAdd++;
                        log.info("Add sensor " + add + " for " + rs);
                    }
                }
            }

            // Remove non existing sensors that are left in map
            for (RoadStationSensor remove : naturalIdToSensorMap.values()) {
                rs.getRoadStationSensors().remove(remove);
                countRemove++;
                log.info("Removed " + remove + " from " + rs);
            }
        }

        int notFound = 0;
        for (List<TiesaaLaskennallinenAnturiVO> values : currentRoadWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap.values()) {
            notFound =+ values.size();
        }
        log.info("RoadStation not found for " + notFound + " TiesaaLaskennallinenAnturis");
        log.info("Sensor removed from road stations " + countRemove);
        log.info("Sensor added to road stations " + countAdd);

        return countAdd > 0 || countRemove > 0;
    }

    private int insertRoadWeatherStations(final List<TiesaaAsemaVO> insert) {

        final List<RoadStation> currentOrphanRoadStations = !insert.isEmpty() ?
                roadStationService.findOrphanWeatherStationRoadStations() : Collections.emptyList();

        final Map<Long, RoadStation> orphanNaturalIdToRoadStationMap = new HashMap<>();
        for (final RoadStation orphanRoadStation : currentOrphanRoadStations) {
            orphanNaturalIdToRoadStationMap.put(orphanRoadStation.getNaturalId(), orphanRoadStation);
        }

        for (final TiesaaAsemaVO tsa : insert) {

            RoadWeatherStation rws = new RoadWeatherStation();

            boolean orphan = false;
            RoadStation rs = orphanNaturalIdToRoadStationMap.remove(Long.valueOf(tsa.getVanhaId()));
            if (rs == null) {
                rs = new RoadStation(RoadStationType.WEATHER_STATION);
            } else {
                orphan = true;
            }
            rws.setRoadStation(rs);

            setRoadAddressIfNotSet(rs);

            updateRoadWeatherStationAttributes(tsa, rws);

            if (rs.getRoadAddress() != null) {
                roadStationService.save(rs.getRoadAddress());
            }
            roadStationService.save(rws.getRoadStation());
            rws = roadWeatherStationService.save(rws);

            if (orphan) {
                log.info("Created new " + rws + ", using existing orphan " + rws.getRoadStation());
            } else {
                log.info("Created new " + rws + " and " + rws.getRoadStation());
            }
        }
        return insert.size();
    }

    private static boolean validate(final TiesaaAsemaVO tsa) {
        if (tsa.getVanhaId() == null) {
            log.error(ToStringHelpper.toString(tsa) + " is invalid: has null vanhaId");
            return false;
        }
        return true;
    }

    private static boolean validate(final TiesaaLaskennallinenAnturiVO tsa) {
        return tsa.getId() != null && tsa.getVanhaId() != null;
    }

    private static boolean updateRoadWeatherStationAttributes(final TiesaaAsemaVO from,
                                                              final RoadWeatherStation to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);
        String before = ReflectionToStringBuilder.toString(to);

        to.setLotjuId(from.getId());
        to.setMaster(from.isMaster() != null ? from.isMaster() : true);
        to.setPublic(from.isJulkinen() != null ? from.isJulkinen() : true);
        to.setRoadWeatherStationType(RoadWeatherStationType.fromTiesaaAsemaTyyppi(from.getTyyppi()));

        // Update RoadStation
        return updateRoadStationAttributes(from, to.getRoadStation()) ||
                HashCodeBuilder.reflectionHashCode(to) != hash;
    }


    private static int obsoleteWeatherStations(final List<RoadWeatherStation> obsolete) {
        int counter = 0;
        for (final RoadWeatherStation rws : obsolete) {
            if (rws.obsolete()) {
                log.debug("Obsolete " + rws);
                counter++;
            }
        }
        return counter;
    }

    private int insertRoadStationSensors(List<TiesaaLaskennallinenAnturiVO> insert) {

        int counter = 0;
        for (TiesaaLaskennallinenAnturiVO anturi : insert) {
            RoadStationSensor sensor = new RoadStationSensor();
            updateRoadStationSensorAttributes(anturi, sensor);
            sensor = roadWeatherStationService.saveRoadStationSensor(sensor);
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

            String before = ReflectionToStringBuilder.toString(sensor);
            if ( updateRoadStationSensorAttributes(anturi, sensor) ) {
                counter++;
                log.info("Updated RoadStationSensor:\n" + before + " -> \n" + ReflectionToStringBuilder.toString(sensor));
            }
        }
        return counter;
    }

    private static boolean updateRoadStationSensorAttributes(TiesaaLaskennallinenAnturiVO from, RoadStationSensor to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

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

    private static int obsoleteRoadStationSensors(final List<RoadStationSensor> obsolete) {
        int counter = 0;
        for (final RoadStationSensor s : obsolete) {
            if (s.obsolete()) {
                log.debug("Obsolete " + s);
                counter++;
            }
        }
        return counter;
    }

}
