package fi.livi.digitraffic.tie.service.weather;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.common.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.model.weather.WeatherStation;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.lotju.LotjuWeatherStationMetadataClientWrapper;

@ConditionalOnNotWebApplication
@Component
public class WeatherStationsSensorsUpdater {
    private static final Logger log = LoggerFactory.getLogger(WeatherStationsSensorsUpdater.class);

    private final RoadStationSensorService roadStationSensorService;
    private final WeatherStationService weatherStationService;
    private final LotjuWeatherStationMetadataClientWrapper lotjuWeatherStationMetadataClientWrapper;

    @Autowired
    public WeatherStationsSensorsUpdater(final RoadStationSensorService roadStationSensorService,
                                         final WeatherStationService weatherStationService,
                                         final LotjuWeatherStationMetadataClientWrapper lotjuWeatherStationMetadataClientWrapper) {
        this.roadStationSensorService = roadStationSensorService;
        this.weatherStationService = weatherStationService;
        this.lotjuWeatherStationMetadataClientWrapper = lotjuWeatherStationMetadataClientWrapper;
    }

    /**
     * Updates all available sensors of weather road stations
     */
    @PerformanceMonitor(maxErrorExcecutionTime = 900000, maxWarnExcecutionTime = 600000)
    public boolean updateWeatherStationsSensors() {
        log.info("method=updateWeatherStationsSensors Update WeatherStations RoadStationSensors start");

        // Update sensors of road stations
        // Get current WeatherStations
        final Map<Long, WeatherStation> currentLotjuIdToWeatherStationsMap =
                weatherStationService.findAllWeatherStationsMappedByLotjuId();
        final Set<Long> rwsLotjuIds = currentLotjuIdToWeatherStationsMap.keySet();

        // Get sensors for current WeatherStations
        final Map<Long, List<TiesaaLaskennallinenAnturiVO>> currentWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap =
                lotjuWeatherStationMetadataClientWrapper.getTiesaaLaskennallinenAnturisMappedByAsemaLotjuId(rwsLotjuIds);

        final List<Pair<WeatherStation,  List<TiesaaLaskennallinenAnturiVO>>> stationAnturisPair = new ArrayList<>();
        currentLotjuIdToWeatherStationsMap.values().forEach(weatherStation -> {
            final List<TiesaaLaskennallinenAnturiVO> anturis = currentWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap.remove(weatherStation.getLotjuId());
            stationAnturisPair.add(Pair.of(weatherStation, anturis));
        });

        log.info("method=updateWeatherStationsSensors RoadStation not found for rsNotFoundCount={} TiesaaLaskennallinenAnturis", currentWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap.size());

        // Update sensors of road stations
        final boolean updateStaticDataStatus = updateSensorsOfWeatherStations(stationAnturisPair);

        log.info("method=updateWeatherStationsSensors Update WeatherStations RoadStationSensors end");
        return updateStaticDataStatus;
    }

    private boolean updateSensorsOfWeatherStations(
            final List<Pair<WeatherStation,  List<TiesaaLaskennallinenAnturiVO>>> stationAnturisPairs) {

        int countAdded = 0;
        int countRemoved = 0;

        for (final Pair<WeatherStation, List<TiesaaLaskennallinenAnturiVO>> pair : stationAnturisPairs) {
            final WeatherStation ws = pair.getKey();
            final List<TiesaaLaskennallinenAnturiVO> anturis = pair.getRight();
            final List<Long> sensorslotjuIds = anturis.stream().map(TiesaaLaskennallinenAnturiVO::getId).collect(Collectors.toList());
            final Pair<Integer, Integer> deletedInserted = roadStationSensorService.updateSensorsOfRoadStation(ws.getRoadStationId(),
                                                                                                             RoadStationType.WEATHER_STATION,
                                                                                                             sensorslotjuIds);
            countRemoved += deletedInserted.getLeft();
            countAdded += deletedInserted.getRight();
        }

        log.info("method=updateSensorsOfWeatherStations Sensor removed from road stations countRemoved={}", countRemoved);
        log.info("method=updateSensorsOfWeatherStations Sensor added to road stations countAdded={}", countAdded);

        return countRemoved > 0 || countAdded > 0;
    }
}
