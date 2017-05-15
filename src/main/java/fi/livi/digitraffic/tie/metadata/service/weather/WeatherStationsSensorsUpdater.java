package fi.livi.digitraffic.tie.metadata.service.weather;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuWeatherStationMetadataService;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.TiesaaLaskennallinenAnturiVO;

@Service
public class WeatherStationsSensorsUpdater extends AbstractWeatherStationAttributeUpdater {

    private RoadStationSensorService roadStationSensorService;
    private final WeatherStationService weatherStationService;
    private final StaticDataStatusService staticDataStatusService;
    private final LotjuWeatherStationMetadataService lotjuWeatherStationMetadataService;

    @Autowired
    public WeatherStationsSensorsUpdater(final RoadStationService roadStationService,
                                         final RoadStationSensorService roadStationSensorService,
                                         final WeatherStationService weatherStationService,
                                         final StaticDataStatusService staticDataStatusService,
                                         final LotjuWeatherStationMetadataService lotjuWeatherStationMetadataService) {
        super(roadStationService, LoggerFactory.getLogger(WeatherStationsSensorsUpdater.class));
        this.roadStationSensorService = roadStationSensorService;
        this.weatherStationService = weatherStationService;
        this.staticDataStatusService = staticDataStatusService;
        this.lotjuWeatherStationMetadataService = lotjuWeatherStationMetadataService;
    }

    /**
     * Updates all available sensors of weather road stations
     */
    @PerformanceMonitor(maxErroExcecutionTime = 900000, maxWarnExcecutionTime = 600000)
    public boolean updateWeatherStationsSensors() {
        log.info("Update WeatherStations RoadStationSensors start");

        if (!lotjuWeatherStationMetadataService.isEnabled()) {
            log.warn("Not updating WeatherStations Sensors metadata because LotjuWeatherStationService not enabled");
            return false;
        }

        // Update sensors of road stations
        // Get current WeatherStations
        final Map<Long, WeatherStation> currentLotjuIdToWeatherStationsMap =
                weatherStationService.findAllWeatherStationsMappedByLotjuId();
        final Set<Long> rwsLotjuIds = currentLotjuIdToWeatherStationsMap.keySet();

        // Get sensors for current WeatherStations
        final Map<Long, List<TiesaaLaskennallinenAnturiVO>> currentWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap =
                lotjuWeatherStationMetadataService.getTiesaaLaskennallinenAnturisMappedByAsemaLotjuId(rwsLotjuIds);

        final List<Pair<WeatherStation,  List<TiesaaLaskennallinenAnturiVO>>> stationAnturisPair = new ArrayList<>();
        currentLotjuIdToWeatherStationsMap.values().stream().forEach(weatherStation -> {
            final List<TiesaaLaskennallinenAnturiVO> anturis = currentWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap.remove(weatherStation.getLotjuId());
            stationAnturisPair.add(Pair.of(weatherStation, anturis));
        });

        log.info("RoadStation not found for {} TiesaaLaskennallinenAnturis", currentWeatherStationLotjuIdToTiesaaLaskennallinenAnturiMap.size());

        // Update sensors of road stations
        final boolean updateStaticDataStatus =
                updateSensorsOfWeatherStations(stationAnturisPair);
        updateRoasWeatherSensorStaticDataStatus(updateStaticDataStatus);

        log.info("Update WeatherStations RoadStationSensors end");
        return updateStaticDataStatus;
    }

    private boolean updateSensorsOfWeatherStations(
            final List<Pair<WeatherStation,  List<TiesaaLaskennallinenAnturiVO>>> stationAnturisPairs) {

        final AtomicInteger countAdded = new AtomicInteger();
        final AtomicInteger countRemoved = new AtomicInteger();

        stationAnturisPairs.stream().forEach(pair -> {
            Pair<Integer, Integer> deletedInserted = roadStationSensorService.updateSensorsOfWeatherStations(pair.getKey(), pair.getRight());
            countRemoved.addAndGet(deletedInserted.getLeft());
            countAdded.addAndGet(deletedInserted.getRight());
        });

        log.info("Sensor removed from road stations {}", countRemoved);
        log.info("Sensor added to road stations {}", countAdded);

        return countRemoved.get() > 0 || countAdded.get() > 0;
    }


    private void updateRoasWeatherSensorStaticDataStatus(final boolean updateStaticDataStatus) {
        staticDataStatusService.updateStaticDataStatus(StaticDataStatusService.StaticStatusType.ROAD_WEATHER_SENSOR, updateStaticDataStatus);
    }
}
