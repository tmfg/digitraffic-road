package fi.livi.digitraffic.tie.metadata.service.weather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;
import fi.livi.digitraffic.tie.metadata.model.WeatherStationType;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuWeatherStationClient;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.TiesaaAsemaVO;

@Service
public class WeatherStationUpdater extends AbstractWeatherStationUpdater {
    private static final Logger log = LoggerFactory.getLogger(WeatherStationUpdater.class);

    private final WeatherStationService weatherStationService;
    private final StaticDataStatusService staticDataStatusService;
    private final LotjuWeatherStationClient lotjuWeatherStationClient;

    @Autowired
    public WeatherStationUpdater(final RoadStationService roadStationService,
                                 final WeatherStationService weatherStationService,
                                 final StaticDataStatusService staticDataStatusService,
                                 final LotjuWeatherStationClient lotjuWeatherStationClient) {
        super(roadStationService);
        this.weatherStationService = weatherStationService;
        this.staticDataStatusService = staticDataStatusService;
        this.lotjuWeatherStationClient = lotjuWeatherStationClient;
    }

    /**
     * Updates Weather Stations
     */
    @Transactional
    public boolean updateWeatherStations() {
        log.info("Update WeatherStations start");

        if (lotjuWeatherStationClient == null) {
            log.warn("Not updating WeatherStations metadatas because no lotjuWeatherStationClient defined");
            return false;
        }

        final List<TiesaaAsemaVO> tiesaaAsemas = lotjuWeatherStationClient.getTiesaaAsemmas();

        if (log.isDebugEnabled()) {
            for (final TiesaaAsemaVO tsa : tiesaaAsemas) {
                log.debug(ToStringBuilder.reflectionToString(tsa));
            }
        }

        final boolean updateStaticDataStatus = updateWeatherStations(tiesaaAsemas);
        updateRoasWeatherStationStaticDataStatus(updateStaticDataStatus);

        log.info("Update WeatherStations end");
        return updateStaticDataStatus;
    }

    private void updateRoasWeatherStationStaticDataStatus(final boolean updateStaticDataStatus) {
        staticDataStatusService.updateStaticDataStatus(StaticDataStatusService.StaticStatusType.ROAD_WEATHER, updateStaticDataStatus);
    }

    private boolean updateWeatherStations(final List<TiesaaAsemaVO> tiesaaAsemas) {

        final Map<Long, WeatherStation> currentLotjuIdToWeatherStationMap =
                weatherStationService.findAllWeatherStationsMappedByLotjuId();

        final List<WeatherStation> obsolete = new ArrayList<>(); // obsolete WeatherStations
        final List<Pair<TiesaaAsemaVO, WeatherStation>> update = new ArrayList<>(); // WeatherStations to update
        final List<TiesaaAsemaVO> insert = new ArrayList<>(); // new WeatherStations

        int invalid = 0;
        for (final TiesaaAsemaVO tsa : tiesaaAsemas) {

            if (validate(tsa)) {
                final WeatherStation currentSaved = currentLotjuIdToWeatherStationMap.remove(tsa.getId());

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
            log.warn("Found " + invalid + " invalid TiesaaAsema from LOTJU");
        }

        // rws in database, but not in server
        obsolete.addAll(currentLotjuIdToWeatherStationMap.values());

        final int obsoleted = obsoleteWeatherStations(obsolete);
        final int uptaded = updateWeatherStationsRoadStationSensors(update);
        final int inserted = insertWeatherStations(insert);

        log.info("Obsoleted " + obsoleted + " WeatherStations");
        log.info("Uptaded " + uptaded + " WeatherStations");
        log.info("Inserted " + inserted + " WeatherStations");
        if (insert.size() > inserted) {
            log.warn("Insert failed for " + (insert.size()-inserted) + " WeatherStations");
        }
        return obsoleted > 0 || inserted > 0 || uptaded > 0;
    }

    private int updateWeatherStationsRoadStationSensors(final List<Pair<TiesaaAsemaVO, WeatherStation>> update) {

        final Map<Long, RoadStation> orphansNaturalIdToRoadStationMap =
                roadStationService.findOrphansByTypeMappedByNaturalId(RoadStationType.WEATHER_STATION);

        int counter = 0;
        for (final Pair<TiesaaAsemaVO, WeatherStation> pair : update) {

            final TiesaaAsemaVO tsa = pair.getLeft();
            final WeatherStation rws = pair.getRight();

            final int hash = HashCodeBuilder.reflectionHashCode(rws);
            final String before = ReflectionToStringBuilder.toString(rws);

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

            if ( updateWeatherStationAttributes(tsa, rws) ||
                 hash != HashCodeBuilder.reflectionHashCode(rws) ) {
                counter++;
                log.info("Updated WeatherStation:\n" + before + " -> \n" + ReflectionToStringBuilder.toString(rws));
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

    private int insertWeatherStations(final List<TiesaaAsemaVO> insert) {

        final List<RoadStation> currentOrphanRoadStations = !insert.isEmpty() ?
                roadStationService.findOrphanWeatherStationRoadStations() : Collections.emptyList();

        final Map<Long, RoadStation> orphanNaturalIdToRoadStationMap = new HashMap<>();
        for (final RoadStation orphanRoadStation : currentOrphanRoadStations) {
            orphanNaturalIdToRoadStationMap.put(orphanRoadStation.getNaturalId(), orphanRoadStation);
        }

        for (final TiesaaAsemaVO tsa : insert) {

            WeatherStation rws = new WeatherStation();

            boolean orphan = false;
            RoadStation rs = orphanNaturalIdToRoadStationMap.remove(Long.valueOf(tsa.getVanhaId()));
            if (rs == null) {
                rs = new RoadStation(RoadStationType.WEATHER_STATION);
            } else {
                orphan = true;
            }
            rws.setRoadStation(rs);

            setRoadAddressIfNotSet(rs);

            updateWeatherStationAttributes(tsa, rws);

            if (rs.getRoadAddress() != null) {
                roadStationService.save(rs.getRoadAddress());
            }
            roadStationService.save(rws.getRoadStation());
            rws = weatherStationService.save(rws);

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

    private static boolean updateWeatherStationAttributes(final TiesaaAsemaVO from,
                                                          final WeatherStation to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        to.setLotjuId(from.getId());
        to.setMaster(from.isMaster() != null ? from.isMaster() : true);
        to.setWeatherStationType(WeatherStationType.fromTiesaaAsemaTyyppi(from.getTyyppi()));

        // Update RoadStation
        return updateRoadStationAttributes(from, to.getRoadStation()) ||
                HashCodeBuilder.reflectionHashCode(to) != hash;
    }

    private static int obsoleteWeatherStations(final List<WeatherStation> obsolete) {
        int counter = 0;
        for (final WeatherStation rws : obsolete) {
            if (rws.obsolete()) {
                log.debug("Obsolete " + rws);
                counter++;
            }
        }
        return counter;
    }
}
