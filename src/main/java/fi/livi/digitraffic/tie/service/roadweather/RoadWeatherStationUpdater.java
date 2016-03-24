package fi.livi.digitraffic.tie.service.roadweather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.RoadStation;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.RoadWeatherStation;
import fi.livi.digitraffic.tie.model.RoadWeatherStationType;
import fi.livi.digitraffic.tie.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.service.roadstation.RoadStationService;
import fi.livi.digitraffic.tie.wsdl.tiesaa.KeruunTILA;
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

    // 5 min
    @Scheduled(fixedRate = 5*60*1000)
    @Transactional
    public void updateWeatherStations() {
        log.info("Update WeatherStations start");

        if (roadWeatherStationClient == null) {
            log.warn("Not updating WeatherStations metadatas because no roadWeatherStationClient defined");
            return;
        }

        final List<TiesaaAsema> tiesaaAsemas = roadWeatherStationClient.getTiesaaAsemmas();

        if (log.isDebugEnabled()) {
            log.debug("Fetched TiesaaAsemmas:");
            for (final TiesaaAsema tsa : tiesaaAsemas) {
                log.debug(ToStringBuilder.reflectionToString(tsa));
            }
        }

        final Map<Long, RoadWeatherStation> currentLotjuIdToRoadWeatherStationsMap =
                roadWeatherStationService.findAllRoadWeatherStationsMappedByLotjuId();

        final boolean updateStaticDataStatus = updateWeatherStations(tiesaaAsemas, currentLotjuIdToRoadWeatherStationsMap);
        updateStaticDataStatus(updateStaticDataStatus);

        log.info("Update WeatherStations end");
    }

    private void updateStaticDataStatus(final boolean updateStaticDataStatus) {
        staticDataStatusService.updateStaticDataStatus(StaticDataStatusService.StaticStatusType.ROAD_WEATHER, updateStaticDataStatus);
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

                if (currentSaved != null) {
                    if (POISTETUT.contains(tsa.getKeruunTila())) {
                        obsolete.add(currentSaved);
                    } else {
                        update.add(Pair.of(tsa, currentSaved));
                    }
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
        log.info("Osoleted " + obsoleted + " RoadWeatherStations");

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
            log.debug("Updating RoadWeatherStation " + rws.getId() + " naturalID " + rws.getRoadStation().getNaturalId());

            if ( updateRoadWeatherStationAttributes(tsa, rws) ) {
                counter++;
            }
        }
        return counter;
    }

    private int insertRoadWeatherStations(final List<TiesaaAsema> insert) {

        final List<RoadStation> currentOrphanRoadStations = !insert.isEmpty() ?
                    roadStationService.findOrphansByType(RoadStationType.WEATHER_STATION) : Collections.emptyList();

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

    private boolean validate(final TiesaaAsema tsa) {
        if (tsa.getVanhaId() == null) {
            log.error(ToStringHelpper.toString(tsa) + " is invalid: has null vanhaId");
            return false;
        }
        return true;
    }

    private boolean updateRoadWeatherStationAttributes(final TiesaaAsema from, final RoadWeatherStation to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);
        to.setLotjuId(from.getId());
        to.setRoadWeatherStationType(RoadWeatherStationType.fromTiesaaAsemaTyyppi(from.getTyyppi()));

        // Update RoadStation
        return updateRoadStationAttributes(from, to.getRoadStation()) ||
                HashCodeBuilder.reflectionHashCode(to) != hash;
    }

    private static boolean updateRoadStationAttributes(final TiesaaAsema from, final RoadStation to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);
        to.setNaturalId(from.getVanhaId());
        to.setType(RoadStationType.WEATHER_STATION);
        to.setObsolete(false);
        to.setObsoleteDate(null);
        to.setName(from.getNimi());
        to.setNameFi(from.getNimiFi());
        to.setNameSv(from.getNimiSe());
        to.setNameEn(from.getNimiEn());
        to.setDescription(from.getKuvaus());
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
}
