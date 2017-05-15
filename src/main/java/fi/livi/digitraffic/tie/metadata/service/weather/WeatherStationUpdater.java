package fi.livi.digitraffic.tie.metadata.service.weather;

import static fi.livi.digitraffic.tie.metadata.model.CollectionStatus.isPermanentlyDeletedKeruunTila;

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
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
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
    public static final String WEATHER_STATIONS = " WeatherStations";

    private final WeatherStationService weatherStationService;
    private final StaticDataStatusService staticDataStatusService;
    private final LotjuWeatherStationMetadataService lotjuWeatherStationMetadataService;

    @Autowired
    public WeatherStationUpdater(final RoadStationService roadStationService,
                                 final WeatherStationService weatherStationService,
                                 final StaticDataStatusService staticDataStatusService,
                                 final LotjuWeatherStationMetadataService lotjuWeatherStationMetadataService) {
        super(roadStationService, LoggerFactory.getLogger(WeatherStationUpdater.class));
        this.weatherStationService = weatherStationService;
        this.staticDataStatusService = staticDataStatusService;
        this.lotjuWeatherStationMetadataService = lotjuWeatherStationMetadataService;
    }

    /**
     * Updates Weather Stations
     */
    @PerformanceMonitor(maxWarnExcecutionTime = 60000, maxErroExcecutionTime = 90000)
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

        final boolean updateStaticDataStatus = updateWeatherStationsMetadata(tiesaaAsemas);
        updateRoasWeatherStationStaticDataStatus(updateStaticDataStatus);

        log.info("Update WeatherStations end");
        return updateStaticDataStatus;
    }

    private void updateRoasWeatherStationStaticDataStatus(final boolean updateStaticDataStatus) {
        staticDataStatusService.updateStaticDataStatus(StaticDataStatusService.StaticStatusType.ROAD_WEATHER, updateStaticDataStatus);
    }

    private boolean updateWeatherStationsMetadata(final List<TiesaaAsemaVO> tiesaaAsemas) {

        weatherStationService.fixOrphanRoadStations();
        weatherStationService.fixNullLotjuIds(tiesaaAsemas);

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
        final long obsoleted = currentLotjuIdToWeatherStationMap.values().stream().filter(ws -> weatherStationService.obsoleteStation(ws)).count();

        final int updated = updateWeatherStations(update);
        final int inserted = insertWeatherStations(insert);

        log.info("Obsoleted {} {}", obsoleted,  WEATHER_STATIONS);
        log.info("Updated {} {}", updated, WEATHER_STATIONS);
        log.info("Inserted {} {}", inserted, WEATHER_STATIONS);
        if (insert.size() > inserted) {
            log.warn("Insert failed for {} {} ", (insert.size()-inserted), WEATHER_STATIONS);
        }
        return obsoleted > 0 || inserted > 0 || updated > 0;
    }

    private int updateWeatherStations(final List<Pair<TiesaaAsemaVO, WeatherStation>> update) {

        final AtomicInteger counter = new AtomicInteger();
        update.stream().forEach(pair -> {

            final TiesaaAsemaVO tsa = pair.getLeft();
            final WeatherStation rws = pair.getRight();

            final int hash = HashCodeBuilder.reflectionHashCode(rws);
            final String before = ReflectionToStringBuilder.toString(rws);

            final RoadStation rs = rws.getRoadStation();
            setRoadAddressIfNotSet(rs);

            if ( updateWeatherStationAttributes(tsa, rws) ||
                 hash != HashCodeBuilder.reflectionHashCode(rws) ) {
                counter.addAndGet(1);
                log.info("Updated WeatherStation:\n{} -> \n{}", before, ReflectionToStringBuilder.toString(rws));
            }
            weatherStationService.save(rws);
        });
        return counter.get();
    }

    private int insertWeatherStations(final List<TiesaaAsemaVO> insert) {

        final List<RoadStation> currentOrphanRoadStations = !insert.isEmpty() ?
                roadStationService.findOrphanWeatherStationRoadStations() : Collections.emptyList();

        final Map<Long, RoadStation> orphanNaturalIdToRoadStationMap = new HashMap<>();
        for (final RoadStation orphanRoadStation : currentOrphanRoadStations) {
            orphanNaturalIdToRoadStationMap.put(orphanRoadStation.getNaturalId(), orphanRoadStation);
        }

        insert.forEach(tsa -> {
            final WeatherStation rws = new WeatherStation();

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

            weatherStationService.save(rws);

            if (orphan) {
                log.info("Created new " + rws + ", using existing orphan " + rws.getRoadStation());
            } else {
                log.info("Created new " + rws + " and " + rws.getRoadStation());
            }
        });
        return insert.size();
    }

    private boolean validate(final TiesaaAsemaVO tsa) {
        final boolean valid = tsa.getVanhaId() != null;
        logErrorIf(!valid && !isPermanentlyDeletedKeruunTila(tsa.getKeruunTila()),
                   "{} is invalid: has null vanhaId",
                   ToStringHelper.toString(tsa));
        return valid;
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
