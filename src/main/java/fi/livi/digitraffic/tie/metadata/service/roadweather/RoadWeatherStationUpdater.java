package fi.livi.digitraffic.tie.metadata.service.roadweather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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
import fi.livi.digitraffic.tie.wsdl.tiesaa.KeruunTILA;
import fi.livi.digitraffic.tie.wsdl.tiesaa.TiesaaAsema;
import fi.livi.digitraffic.tie.wsdl.tiesaa.TiesaaLaskennallinenAnturi;

@Service
public class RoadWeatherStationUpdater {
    private static final Logger log = Logger.getLogger(RoadWeatherStationUpdater.class);

    private final RoadStationService roadStationService;
    private final RoadWeatherStationService roadWeatherStationService;
    private final StaticDataStatusService staticDataStatusService;
    private final LotjuRoadWeatherStationClient lotjuRoadWeatherStationClient;

    private static final EnumSet<KeruunTILA> POISTETUT = EnumSet.of(KeruunTILA.POISTETTU_PYSYVASTI, KeruunTILA.POISTETTU_TILAPAISESTI);

    @Autowired
    public RoadWeatherStationUpdater(final RoadStationService roadStationService,
                                     final RoadWeatherStationService roadWeatherStationService,
                                     final StaticDataStatusService staticDataStatusService,
                                     final LotjuRoadWeatherStationClient lotjuRoadWeatherStationClient) {
        this.roadStationService = roadStationService;
        this.roadWeatherStationService = roadWeatherStationService;
        this.staticDataStatusService = staticDataStatusService;
        this.lotjuRoadWeatherStationClient = lotjuRoadWeatherStationClient;
    }

    /**
     * Updates road weather stations
     */
    @Transactional
    public void updateWeatherStations() {
        log.info("Update RoadWeatherStations start");

        if (lotjuRoadWeatherStationClient == null) {
            log.warn("Not updating WeatherStations metadatas because no lotjuRoadWeatherStationClient defined");
            return;
        }

        final List<TiesaaAsema> tiesaaAsemas = lotjuRoadWeatherStationClient.getTiesaaAsemmas();

        if (log.isDebugEnabled()) {
            for (final TiesaaAsema tsa : tiesaaAsemas) {
                log.debug(ToStringBuilder.reflectionToString(tsa));
            }
        }

        final Map<Long, RoadWeatherStation> currentLotjuIdToRoadWeatherStationsMap =
                roadWeatherStationService.findAllRoadWeatherStationsMappedByLotjuId();

        final boolean updateStaticDataStatus = updateWeatherStations(tiesaaAsemas, currentLotjuIdToRoadWeatherStationsMap);
        updateRoasWeatherStationStaticDataStatus(updateStaticDataStatus);

        log.info("Update RoadWeatherStations end");
    }

    /**
     * Updates all available road station sensors ("types") and sensors of road stations
     */
    @Transactional
    public void updateRoadStationSensors() {
        log.info("Update RoadStationSensors start");

        if (lotjuRoadWeatherStationClient == null) {
            log.warn("Not updating RoadStationSensors metadatas because no lotjuRoadWeatherStationClient defined");
            return;
        }

        // Update available RoadStationSensors types to db
        List<TiesaaLaskennallinenAnturi> allTiesaaLaskennallinenAnturis =
                lotjuRoadWeatherStationClient.getAllTiesaaLaskennallinenAnturis();

        updateAllRoadStationSensors(allTiesaaLaskennallinenAnturis);

        // Update sensors of road stations
        // Get current RoadWeatherStations
        final Map<Long, RoadWeatherStation> currentLotjuIdToRoadWeatherStationsMap =
                roadWeatherStationService.findAllRoadWeatherStationsMappedByLotjuId();
        Set<Long> rwsLotjuIds = currentLotjuIdToRoadWeatherStationsMap.keySet();
        // Get sensors for current RoadWeatherStations
        Map<Long, List<TiesaaLaskennallinenAnturi>> currentLRoadWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap =
                        lotjuRoadWeatherStationClient.getTiesaaLaskennallinenAnturis(rwsLotjuIds);
        // Update sensros of road stations
        final boolean updateStaticDataStatus =
                updateSensorsOfRoadStations(currentLRoadWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap,
                                            currentLotjuIdToRoadWeatherStationsMap);
        updateRoasWeatherSensorStaticDataStatus(updateStaticDataStatus);

        log.info("Update RoadStationSensors end");
    }

    private boolean updateAllRoadStationSensors(List<TiesaaLaskennallinenAnturi> allTiesaaLaskennallinenAnturis) {
        Map<Long, RoadStationSensor> currentNaturalIdToSensorMap =
                roadWeatherStationService.findAllRoadStationSensorsMappedByNaturalId();

        final List<RoadStationSensor> obsolete = new ArrayList<>(); // obsolete RoadWeatherStations
        final List<Pair<TiesaaLaskennallinenAnturi, RoadStationSensor>> update = new ArrayList<>(); // RoadWeatherStations to update
        final List<TiesaaLaskennallinenAnturi> insert = new ArrayList<>(); // new RoadWeatherStations

        int invalid = 0;
        for (TiesaaLaskennallinenAnturi anturi : allTiesaaLaskennallinenAnturis) {
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
        log.info("Obsoleted " + obsoleted + " RoadStationSensors");

        final int uptaded = updateRoadStationSensors(update);
        log.info("Uptaded " + uptaded + " RoadStationSensors");

        final int inserted = insertRoadStationSensors(insert);
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

    private boolean updateWeatherStations(final List<TiesaaAsema> tiesaaAsemas,
                                          final Map<Long, RoadWeatherStation> currentLotjuIdToRoadWeatherStationMap) {

        final List<RoadWeatherStation> obsolete = new ArrayList<>(); // obsolete RoadWeatherStations
        final List<Pair<TiesaaAsema, RoadWeatherStation>> update = new ArrayList<>(); // RoadWeatherStations to update
        final List<TiesaaAsema> insert = new ArrayList<>(); // new RoadWeatherStations

        int invalid = 0;
        for (final TiesaaAsema tsa : tiesaaAsemas) {

            if (validate(tsa)) {
                final RoadWeatherStation currentSaved = currentLotjuIdToRoadWeatherStationMap.remove(tsa.getId());

                if ( currentSaved != null && POISTETUT.contains(tsa.getKeruunTila()) ) {
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
        log.info("Obsoleted " + obsoleted + " RoadWeatherStations");

        final int uptaded = updateRoadWeatherStations(update);
        log.info("Uptaded " + uptaded + " RoadWeatherStations");

        final int inserted = insertRoadWeatherStations(insert);
        log.info("Inserted " + inserted + " RoadWeatherStations");
        if (insert.size() > inserted) {
            log.warn("Insert failed for " + (insert.size()-inserted) + " RoadWeatherStations");
        }
        return obsoleted > 0 || inserted > 0;
    }

    private static int updateRoadWeatherStations(final List<Pair<TiesaaAsema, RoadWeatherStation>> update) {

        int counter = 0;
        for (final Pair<TiesaaAsema, RoadWeatherStation> pair : update) {

            final TiesaaAsema tsa = pair.getLeft();
            final RoadWeatherStation rws = pair.getRight();
            log.debug("Updating RoadWeatherStation " + rws.getId() + " naturalId " + rws.getRoadStation().getNaturalId());

            if ( updateRoadWeatherStationAttributes(tsa, rws) ) {
                counter++;
            }
        }
        return counter;
    }

    private boolean updateSensorsOfRoadStations(
            Map<Long, List<TiesaaLaskennallinenAnturi>> currentRoadWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap,
            Map<Long, RoadWeatherStation> currentLotjuIdToRoadWeatherStationsMap) {

        Map<Long, RoadStationSensor> allSensors = roadWeatherStationService.findAllRoadStationSensorsMappedByNaturalId();

        Iterator<Long> iter = currentRoadWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap.keySet().iterator();

        int countAdd = 0;
        int countRemove = 0;
        while (iter.hasNext()) {
            Long rwsLotjuId = iter.next();
            List<TiesaaLaskennallinenAnturi> rwsAnturis = currentRoadWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap.get(rwsLotjuId);
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

            for (TiesaaLaskennallinenAnturi rwsAnturi : rwsAnturis) {
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
        for (List<TiesaaLaskennallinenAnturi> values : currentRoadWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap.values()) {
            notFound =+ values.size();
        }
        if (notFound > 0) {
            log.info("RoadStation not found for " + notFound + " TiesaaLaskennallinenAnturis");
        }
        if (countRemove > 0) {
            log.info("Sensor removed from road stations " + countRemove);
        }
        if (countRemove > 0) {
            log.info("Sensor added to road stations " + countAdd);
        }
        return countAdd > 0 || countRemove > 0;
    }

    private int insertRoadWeatherStations(final List<TiesaaAsema> insert) {

        final List<RoadStation> currentOrphanRoadStations = !insert.isEmpty() ?
                roadStationService.findOrphanWeatherStationRoadStations() : Collections.emptyList();

        final Map<Long, RoadStation> orphanNaturalIdToRoadStationMap = new HashMap<>();
        for (final RoadStation orphanRoadStation : currentOrphanRoadStations) {
            orphanNaturalIdToRoadStationMap.put(orphanRoadStation.getNaturalId(), orphanRoadStation);
        }

        for (final TiesaaAsema tsa : insert) {

            RoadWeatherStation rws = new RoadWeatherStation();

            boolean orphan = false;
            RoadStation rs = orphanNaturalIdToRoadStationMap.remove(Long.valueOf(tsa.getVanhaId()));
            if (rs == null) {
                rs = new RoadStation();
            } else {
                orphan = true;
            }
            rws.setRoadStation(rs);
            updateRoadWeatherStationAttributes(tsa, rws);

            roadStationService.save(rws.getRoadStation());
            rws = roadWeatherStationService.save(rws);

            if (orphan) {
                log.info("Created new " + rws + ", using existing orphan RoadStation");
            } else {
                log.info("Created new " + rws);
            }
        }
        return insert.size();
    }

    private static boolean validate(final TiesaaAsema tsa) {
        if (tsa.getVanhaId() == null) {
            log.error(ToStringHelpper.toString(tsa) + " is invalid: has null vanhaId");
            return false;
        }
        return true;
    }

    private static boolean validate(final TiesaaLaskennallinenAnturi tsa) {
        return tsa.getId() != null && tsa.getVanhaId() != null;
    }

    private static boolean updateRoadWeatherStationAttributes(final TiesaaAsema from,
                                                              final RoadWeatherStation to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);
        to.setLotjuId(from.getId());
        to.setRoadWeatherStationType(RoadWeatherStationType.fromTiesaaAsemaTyyppi(from.getTyyppi()));

        // Update RoadStation
        return updateRoadStationAttributes(from, to.getRoadStation()) ||
                HashCodeBuilder.reflectionHashCode(to) != hash;
    }

    private static boolean updateRoadStationAttributes(final TiesaaAsema from, final RoadStation to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        // Can insert obsolete stations
        if (POISTETUT.contains(from.getKeruunTila())) {
            to.obsolete();
        } else {
            to.setObsolete(false);
            to.setObsoleteDate(null);
        }

        to.setNaturalId(from.getVanhaId());
        to.setType(RoadStationType.WEATHER_STATION);
        to.setName(from.getNimi());
        to.setNameFi(from.getNimiFi());
        to.setNameSv(from.getNimiSe());
        to.setNameEn(from.getNimiEn());
        to.setDescription(from.getKuvaus());
        to.setAdditionalInformation(StringUtils.trimToNull(StringUtils.join(from.getLisatieto(), " ", from.getLisakuvaus())));
        to.setLatitude(from.getLatitudi());
        to.setLongitude(from.getLongitudi());
        to.setAltitude(from.getKorkeus());
        to.setRoadNumber(from.getTieosoite().getTienumero());
        to.setRoadPart(from.getTieosoite().getTieosa());
        to.setDistanceFromRoadPartStart(from.getTieosoite().getEtaisyysTieosanAlusta());
        to.setCollectionInterval(from.getKeruuVali());
        to.setCollectionStatus(CollectionStatus.convertKeruunTila(from.getKeruunTila()));
        to.setMunicipality(from.getKunta());
        to.setMunicipalityCode(from.getKuntaKoodi());
        to.setProvince(from.getMaakunta());
        to.setProvinceCode(from.getMaakuntaKoodi());
        return HashCodeBuilder.reflectionHashCode(to) != hash;
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

    private int insertRoadStationSensors(List<TiesaaLaskennallinenAnturi> insert) {

        int counter = 0;
        for (TiesaaLaskennallinenAnturi anturi : insert) {
            RoadStationSensor sensor = new RoadStationSensor();
            updateRoadStationSensorAttributes(anturi, sensor);
            sensor = roadWeatherStationService.saveRoadStationSensor(sensor);
            log.info("Created new " + sensor);
            counter++;
        }
        return counter;
    }

    private static int updateRoadStationSensors(final List<Pair<TiesaaLaskennallinenAnturi, RoadStationSensor>> update) {

        int counter = 0;
        for (final Pair<TiesaaLaskennallinenAnturi, RoadStationSensor> pair : update) {

            final TiesaaLaskennallinenAnturi anturi = pair.getLeft();
            final RoadStationSensor sensor = pair.getRight();
            log.debug("Updating " + sensor);

            if ( updateRoadStationSensorAttributes(anturi, sensor) ) {
                counter++;
            }
        }
        return counter;
    }

    private static boolean updateRoadStationSensorAttributes(TiesaaLaskennallinenAnturi from, RoadStationSensor to) {
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
        to.setCalculationFormula(from.getLaskentaKaava());
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
