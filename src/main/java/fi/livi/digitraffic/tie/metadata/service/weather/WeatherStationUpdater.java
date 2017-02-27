package fi.livi.digitraffic.tie.metadata.service.weather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;
import fi.livi.digitraffic.tie.metadata.model.WeatherStationType;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuWeatherStationMetadataService;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.TiesaaAsemaVO;

@Service
public class WeatherStationUpdater extends AbstractWeatherStationAttributeUpdater {
    private static final Logger log = LoggerFactory.getLogger(WeatherStationUpdater.class);
    public static final String WEATHER_STATIONS = " WeatherStations";

    private final WeatherStationService weatherStationService;
    private final StaticDataStatusService staticDataStatusService;
    private final LotjuWeatherStationMetadataService lotjuWeatherStationMetadataService;

    @Autowired
    public WeatherStationUpdater(final RoadStationService roadStationService,
                                 final WeatherStationService weatherStationService,
                                 final StaticDataStatusService staticDataStatusService,
                                 final LotjuWeatherStationMetadataService lotjuWeatherStationMetadataService) {
        super(roadStationService);
        this.weatherStationService = weatherStationService;
        this.staticDataStatusService = staticDataStatusService;
        this.lotjuWeatherStationMetadataService = lotjuWeatherStationMetadataService;
    }

    /**
     * Updates Weather Stations
     */
    @Transactional
    public boolean updateWeatherStations() {
        log.info("Update WeatherStations start");

        if (!lotjuWeatherStationMetadataService.isEnabled()) {
            log.warn("Not updating WeatherStations metadata because LotjuWeatherStationService not enabled");
            return false;
        }

        final List<TiesaaAsemaVO> tiesaaAsemas = lotjuWeatherStationMetadataService.getTiesaaAsemmas();

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

    private void fixNullLotjuIds(final List<TiesaaAsemaVO> tiesaaAsemas) {
        Map<Long, WeatherStation> naturalIdToWeatherStationMap =
                weatherStationService.findAllWeatherStationsWithoutLotjuIdMappedByByRoadStationNaturalId();

        tiesaaAsemas.stream().forEach(tiesaaAsema -> {

            WeatherStation ws = tiesaaAsema.getVanhaId() != null ?
                                naturalIdToWeatherStationMap.get(tiesaaAsema.getVanhaId().longValue()) : null;
            if (ws != null) {
                ws.setLotjuId(tiesaaAsema.getId());
                ws.getRoadStation().setLotjuId(tiesaaAsema.getId());
            }
        });
    }

    private void fixOrphanRoadStations() {
        List<WeatherStation> weatherStations =
                weatherStationService.findAllWeatherStationsWithoutRoadStation();

        final Map<Long, RoadStation> orphansNaturalIdToRoadStationMap =
                roadStationService.findOrphansByTypeMappedByNaturalId(RoadStationType.WEATHER_STATION);

        weatherStations.stream().forEach(ws -> {
            setRoadStationIfNotSet(ws, ws.getRoadStationNaturalId(), orphansNaturalIdToRoadStationMap);
            if (ws.getRoadStation().getId() == null) {
                roadStationService.save(ws.getRoadStation());
                log.info("Created new RoadStation " + ws.getRoadStation());
            }
        });
    }
    private boolean updateWeatherStations(final List<TiesaaAsemaVO> tiesaaAsemas) {

        fixOrphanRoadStations();
        fixNullLotjuIds(tiesaaAsemas);

        final Map<Long, WeatherStation> currentLotjuIdToWeatherStationMap =
                weatherStationService.findAllWeatherStationsMappedByLotjuId();

        final List<Pair<TiesaaAsemaVO, WeatherStation>> update = new ArrayList<>(); // WeatherStations to update
        final List<TiesaaAsemaVO> insert = new ArrayList<>(); // new WeatherStations

        final AtomicInteger invalid = new AtomicInteger();
        tiesaaAsemas.stream().forEach(tsa -> {

            if (validate(tsa)) {
                final WeatherStation currentSaved = currentLotjuIdToWeatherStationMap.remove(tsa.getId());

                if (currentSaved != null) {
                    update.add(Pair.of(tsa, currentSaved));
                } else {
                    insert.add(tsa);
                }
            } else {
                invalid.addAndGet(1);
            }
        });

        if (invalid.get() > 0) {
            log.warn("Found {} invalid TiesaaAsema from LOTJU", invalid);
        }

        // rws in database, but not in server
        final long obsoleted = currentLotjuIdToWeatherStationMap.values().stream().filter(ws -> ws.obsolete()).count();

        final int updated = updateWeatherStationsAttributes(update);
        final int inserted = insertWeatherStations(insert);

        log.info("Obsoleted {} {}", obsoleted,  WEATHER_STATIONS);
        log.info("Updated {} {}", updated, WEATHER_STATIONS);
        log.info("Inserted {} {}", inserted, WEATHER_STATIONS);
        if (insert.size() > inserted) {
            log.warn("Insert failed for {} {} ", (insert.size()-inserted), WEATHER_STATIONS);
        }
        return obsoleted > 0 || inserted > 0 || updated > 0;
    }

    private int updateWeatherStationsAttributes(final List<Pair<TiesaaAsemaVO, WeatherStation>> update) {

        final AtomicInteger counter = new AtomicInteger();
        update.stream().forEach(pair -> {

            final TiesaaAsemaVO tsa = pair.getLeft();
            final WeatherStation rws = pair.getRight();

            final int hash = HashCodeBuilder.reflectionHashCode(rws);
            final String before = ReflectionToStringBuilder.toString(rws);

            RoadStation rs = rws.getRoadStation();
            setRoadAddressIfNotSet(rs);

            if ( updateWeatherStationAttributes(tsa, rws) ||
                 hash != HashCodeBuilder.reflectionHashCode(rws) ) {
                counter.addAndGet(1);
                log.info("Updated WeatherStation:\n{} -> \n{}", before, ReflectionToStringBuilder.toString(rws));
            }

            if (rs.getRoadAddress().getId() == null) {
                roadStationService.save(rs.getRoadAddress());
                log.info("Created new RoadAddress " + rs.getRoadAddress());
            }
            if (rws.getRoadStation().getId() == null) {
                roadStationService.save(rws.getRoadStation());
                log.info("Created new RoadStation " + rws.getRoadStation());
            }
        });
        return counter.get();
    }

    private static void setRoadStationIfNotSet(WeatherStation rws, Long tsaVanhaId, Map<Long, RoadStation> orphansNaturalIdToRoadStationMap) {
        RoadStation rs = rws.getRoadStation();

        if (rs == null) {
            rs = tsaVanhaId != null ? orphansNaturalIdToRoadStationMap.remove(tsaVanhaId) : null;
            if (rs == null) {
                rs = new RoadStation(RoadStationType.WEATHER_STATION);
            }
            rws.setRoadStation(rs);
        }
    }

    private int insertWeatherStations(final List<TiesaaAsemaVO> insert) {

        final List<RoadStation> currentOrphanRoadStations = !insert.isEmpty() ?
                roadStationService.findOrphanWeatherStationRoadStations() : Collections.emptyList();

        final Map<Long, RoadStation> orphanNaturalIdToRoadStationMap = new HashMap<>();
        for (final RoadStation orphanRoadStation : currentOrphanRoadStations) {
            orphanNaturalIdToRoadStationMap.put(orphanRoadStation.getNaturalId(), orphanRoadStation);
        }

        insert.forEach(tsa -> {
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

            if (rs.getRoadAddress().getId() == null) {
                roadStationService.save(rs.getRoadAddress());
                log.info("Created new RoadAddress " + rs.getRoadAddress());
            }
            roadStationService.save(rws.getRoadStation());
            rws = weatherStationService.save(rws);

            if (orphan) {
                log.info("Created new " + rws + ", using existing orphan " + rws.getRoadStation());
            } else {
                log.info("Created new " + rws + " and " + rws.getRoadStation());
            }
        });
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
}
