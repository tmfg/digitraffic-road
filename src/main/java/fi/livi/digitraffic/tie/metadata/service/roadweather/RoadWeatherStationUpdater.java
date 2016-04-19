package fi.livi.digitraffic.tie.metadata.service.roadweather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
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
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.RoadWeatherSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadWeatherStation;
import fi.livi.digitraffic.tie.metadata.model.RoadWeatherStationType;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.digitraffic.tie.wsdl.tiesaa.KeruunTILA;
import fi.livi.digitraffic.tie.wsdl.tiesaa.TiesaaAnturi;
import fi.livi.digitraffic.tie.wsdl.tiesaa.TiesaaAsema;

@Service
public class RoadWeatherStationUpdater {
    private static final Logger log = Logger.getLogger(RoadWeatherStationUpdater.class);

    private final RoadStationService roadStationService;
    private final RoadWeatherStationService roadWeatherStationService;
    private final StaticDataStatusService staticDataStatusService;
    private final RoadWeatherStationClient roadWeatherStationClient;

    private static final EnumSet<KeruunTILA> POISTETUT = EnumSet.of(KeruunTILA.POISTETTU_PYSYVASTI, KeruunTILA.POISTETTU_TILAPAISESTI);

    @Autowired
    public RoadWeatherStationUpdater(final RoadStationService roadStationService,
                                     final RoadWeatherStationService roadWeatherStationService,
                                     final StaticDataStatusService staticDataStatusService,
                                     final RoadWeatherStationClient roadWeatherStationClient) {
        this.roadStationService = roadStationService;
        this.roadWeatherStationService = roadWeatherStationService;
        this.staticDataStatusService = staticDataStatusService;
        this.roadWeatherStationClient = roadWeatherStationClient;
    }

    @Transactional
    public void updateWeatherStations() {
        log.info("Update RoadWeatherStations start");

        if (roadWeatherStationClient == null) {
            log.warn("Not updating WeatherStations metadatas because no roadWeatherStationClient defined");
            return;
        }

        final List<TiesaaAsema> tiesaaAsemas = roadWeatherStationClient.getTiesaaAsemmas();

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

    @Transactional
    public void updateRoadWeatherSensors() {
        log.info("Update RoadWeatherSensors start");

        if (roadWeatherStationClient == null) {
            log.warn("Not updating RoadWeatherSensors metadatas because no roadWeatherStationClient defined");
            return;
        }

        final Map<Long, RoadWeatherStation> currentLotjuIdToRoadWeatherStationsMap =
                roadWeatherStationService.findAllRoadWeatherStationsMappedByLotjuId();
        Set<Long> rwsLotjuIds = currentLotjuIdToRoadWeatherStationsMap.keySet();

        Map<Long, List<TiesaaAnturi>> currentRoadWeatherStationLotjuIdToTiesaaAnturiMap =
                roadWeatherStationClient.getTiesaaAnturis(rwsLotjuIds);

        Map<Long, List<RoadWeatherSensor>> currentRoadWeatherSensorsMappedByRoadStationLotjuId =
                roadWeatherStationService.findAllRoadStationSensorsMappedByRoadStationLotjuId();

        if (log.isDebugEnabled()) {
            for (List<TiesaaAnturi> tsAnturis : currentRoadWeatherStationLotjuIdToTiesaaAnturiMap.values()) {
                for (final TiesaaAnturi tsAnturi : tsAnturis) {
                    log.debug(ToStringBuilder.reflectionToString(tsAnturi));
                }
            }
        }

        final boolean updateStaticDataStatus = updateRoadWeatherSensors(
                currentRoadWeatherStationLotjuIdToTiesaaAnturiMap,
                currentRoadWeatherSensorsMappedByRoadStationLotjuId);
        updateRoasWeatherSensorStaticDataStatus(updateStaticDataStatus);


        log.info("Update RoadWeatherSensors end");
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

    private int updateRoadWeatherStations(final List<Pair<TiesaaAsema, RoadWeatherStation>> update) {

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

    private static int updateRoadWeatherSensors(List<Pair<TiesaaAnturi, RoadWeatherSensor>> update) {
        int counter = 0;
        for (final Pair<TiesaaAnturi, RoadWeatherSensor> pair : update) {

            final TiesaaAnturi tsa = pair.getLeft();
            final RoadWeatherSensor rws = pair.getRight();
            log.debug("Updating RoadWeatherSensor " + rws.getId() + " lotjuId " + rws.getLotjuId());

            if ( updateRoadWeatherSensorAttributes(tsa, rws) ) {
                counter++;
            }
        }
        return counter;
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
            RoadStation rs = orphanNaturalIdToRoadStationMap.remove(tsa.getVanhaId().longValue());
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

    private int insertRoadWeatherSensors(final List<TiesaaAnturi> insert) {

        Map<Long, RoadWeatherStation> currentRoadWeatherStationsMappedByLotjuId =
                roadWeatherStationService.findAllRoadWeatherStationsMappedByLotjuId();

        int counter = 0;
        for (final TiesaaAnturi tsa : insert) {
            RoadWeatherStation rws = currentRoadWeatherStationsMappedByLotjuId.get(tsa.getTiesaaAsemaId());
            if (rws != null) {
                RoadWeatherSensor rwSensor = new RoadWeatherSensor();
                rwSensor.setRoadWeatherStation(rws);
                updateRoadWeatherSensorAttributes(tsa, rwSensor);
                roadWeatherStationService.save(rwSensor);
                log.info("Created new " + rwSensor);
                counter++;
            } else {
                log.error("Creating of new RoadWeatherSensor failed for tsa with lotjuId: " + tsa.getId() + " because RoadWeatherStation was not found with lotjuId " + tsa.getTiesaaAsemaId());
            }
        }
        return counter;
    }

    private static boolean validate(final TiesaaAsema tsa) {
        if (tsa.getVanhaId() == null) {
            log.error(ToStringHelpper.toString(tsa) + " is invalid: has null vanhaId");
            return false;
        }
        return true;
    }

    private static boolean validate(final TiesaaAnturi tsa) {
        return tsa.getId() != null;
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
        to.setDistance(from.getTieosoite().getEtaisyysTieosanAlusta());
        to.setCollectionInterval(from.getKeruuVali());
        to.setCollectionStatus(CollectionStatus.convertKeruunTila(from.getKeruunTila()));
        to.setMunicipality(from.getKunta());
        to.setMunicipalityCode(from.getKuntaKoodi());
        to.setProvince(from.getMaakunta());
        to.setProvinceCode(from.getMaakuntaKoodi());
        return HashCodeBuilder.reflectionHashCode(to) != hash;
    }

    private static boolean updateRoadWeatherSensorAttributes(final TiesaaAnturi from, final RoadWeatherSensor to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);
        to.setLotjuId(from.getId());
        to.setAltitude(from.getKorkeus());
        to.setDescription(from.getKuvaus());
        to.setName(from.getNimi());
        to.setSensorTypeId(from.getAnturityyppiId());

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

    private static int obsoleteRoadWeatherSensors(final List<RoadWeatherSensor> obsolete) {
        int counter = 0;
        for (final RoadWeatherSensor rws : obsolete) {
            if (rws.obsolete()) {
                log.debug("Obsolete " + rws);
                counter++;
            }
        }
        return counter;
    }


    private boolean updateRoadWeatherSensors(final Map<Long, List<TiesaaAnturi>> currentRoadWeatherStationLotjuIdToTiesaaAnturiMap,
                                             final Map<Long, List<RoadWeatherSensor>> currentRoadWeatherStationLotjuIdRoadWeatherSensorsMap) {

        final List<RoadWeatherSensor> obsolete = new ArrayList<>(); // obsolete RoadWeatherSensor
        final List<Pair<TiesaaAnturi, RoadWeatherSensor>> update = new ArrayList<>(); // RoadWeatherSensor to update
        final List<TiesaaAnturi> insert = new ArrayList<>(); // new RoadWeatherSensor

        int invalid = 0;
        for ( Long rwsLotjuId : currentRoadWeatherStationLotjuIdToTiesaaAnturiMap.keySet() ) {

            final List<TiesaaAnturi> tsas = currentRoadWeatherStationLotjuIdToTiesaaAnturiMap.get(rwsLotjuId);
            final List<RoadWeatherSensor> rwsensors = currentRoadWeatherStationLotjuIdRoadWeatherSensorsMap.get(rwsLotjuId);

            for (TiesaaAnturi tsa : tsas) {

                if (validate(tsa)) {

                    if (rwsensors != null) {
                        RoadWeatherSensor foundRwsSensor =
                                rwsensors.stream().filter(s -> s.getLotjuId() == tsa.getId().longValue()).findFirst().orElse(null);
                        if (foundRwsSensor != null) {
                            update.add(Pair.of(tsa, foundRwsSensor));
                            rwsensors.remove(foundRwsSensor);
                        } else {
                            insert.add(tsa);
                        }
                    } else {
                        insert.add(tsa);
                    }
                } else {
                    invalid++;
                }
            }

        }

        // rw sensors in database, but not in server
        for (List<RoadWeatherSensor> obsoleteRoadWeatherSensors : currentRoadWeatherStationLotjuIdRoadWeatherSensorsMap.values()) {
            for (RoadWeatherSensor obsoleteRoadWeatherSensor : obsoleteRoadWeatherSensors) {
                obsolete.add(obsoleteRoadWeatherSensor);
            }
        }

        if (invalid > 0) {
            log.warn("Found " + invalid + " TiesaaAsema from LOTJU");
        }

        final int obsoleted = obsoleteRoadWeatherSensors(obsolete);
        log.info("Obsoleted " + obsoleted + " RoadWeatherSensors");

        final int uptaded = updateRoadWeatherSensors(update);
        log.info("Uptaded " + uptaded + " RoadWeatherSensors");

        final int inserted = insertRoadWeatherSensors(insert);
        log.info("Inserted " + inserted + " RoadWeatherSensors");
        if (insert.size() > inserted) {
            log.warn("Insert failed for " + (insert.size()-inserted) + " RoadWeatherStations");
        }
        return obsoleted > 0 || inserted > 0;
    }
}
