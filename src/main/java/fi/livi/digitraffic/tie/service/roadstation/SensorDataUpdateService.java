package fi.livi.digitraffic.tie.service.roadstation;

import static fi.ely.lotju.lam.proto.LAMRealtimeProtos.Lam;
import static fi.ely.lotju.tiesaa.proto.TiesaaProtos.TiesaaMittatieto;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.annotation.NotTransactionalServiceMethod;
import fi.livi.digitraffic.tie.dao.roadstation.RoadStationDao;
import fi.livi.digitraffic.tie.dao.roadstation.SensorValueDao;
import fi.livi.digitraffic.tie.dao.roadstation.SensorValueHistoryDao;
import fi.livi.digitraffic.tie.dto.v1.SensorValueUpdateParameterDto;
import fi.livi.digitraffic.tie.helper.SensorValueBuffer;
import fi.livi.digitraffic.tie.helper.TimestampCache;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.lotju.LotjuAnturiWrapper;

@ConditionalOnNotWebApplication
@Service
public class SensorDataUpdateService {
    private static final Logger log = LoggerFactory.getLogger(SensorDataUpdateService.class);

    //private final Map<DataType, ZonedDateTime> lastMetadataUpdates = new HashMap<DataType, ZonedDateTime>();

    private final SensorValueDao sensorValueDao;
    private final RoadStationSensorService roadStationSensorService;
    private final RoadStationDao roadStationDao;
    private final DataStatusService dataStatusService;
    private final SensorValueHistoryDao sensorValueHistoryDao;

    private final SensorValueBuffer<Lam.Anturi> lamValueBuffer = new SensorValueBuffer<>();
    private final SensorValueBuffer<TiesaaMittatieto.Anturi> weatherValueBuffer = new SensorValueBuffer<>();

    // <lotju_id>
    private Set<Long> allowedTmsRoadStationSensors;
    //private Set<Long> allowedWeatherRoadStationSensors;
    // <lotju_id, id>
    private Map<Long, Long> allowedTmsStationsLotjuIdtoIds;
    private Map<Long, Long> allowedWeatherStationsLotjuIdtoIds;

    @Autowired
    public SensorDataUpdateService(final SensorValueDao sensorValueDao, final RoadStationSensorService roadStationSensorService,
                                   final RoadStationDao roadStationDao, final DataStatusService dataStatusService,
                                   final SensorValueHistoryDao sensorValueHistoryDao) {
        this.sensorValueDao = sensorValueDao;
        this.roadStationSensorService = roadStationSensorService;
        this.roadStationDao = roadStationDao;
        this.dataStatusService = dataStatusService;
        this.sensorValueHistoryDao = sensorValueHistoryDao;

        // Do init
        //lastMetadataUpdates(DataType.TMS_STATION_METADATA, null);
        //lastMetadataUpdates(DataType.TMS_STATION_SENSOR_METADATA, null);
        //lastMetadataUpdates(DataType.WEATHER_STATION_SENSOR_METADATA, null);
        // //lastMetadataUpdates(DataType.WEATHER_STATION_METADATA, null);
        updateStationsAndSensorsMetadata();
    }

    private Set<Long> getAllowedRoadStationSensorsLotjuIds(final RoadStationType roadStationType) {
        return roadStationSensorService.findAllPublishableRoadStationSensors(roadStationType).stream()
            .map(RoadStationSensor::getLotjuId)
            .collect(Collectors.toSet());
    }

    private Map<Long, Long> getAllowedStationsLotjuIdtoIds(final RoadStationType roadStationType) {
        // From road_station-table: <lotju_id, id>
        return roadStationDao.findPublishableRoadStationsIdsMappedByLotjuId(roadStationType);
    }

    @Scheduled(fixedRate = 300000)
    protected void updateStationsAndSensorsMetadata() {
        /* TODO! Pitää selvittaa ekaksi oikeat tyypit tuolta datastatuksesta jotta homma ei mene puihin
        lastMetadataUpdates.replaceAll((type, current) -> {
            ZonedDateTime candidate = dataStatusService.findDataUpdatedTime(type);

            if (current == null || candidate.isAfter(current)) {
                // Update list
                switch (type) {
                case TMS_STATION_SENSOR_METADATA:
                    allowedTmsStationsLotjuIdtoIds = getAllowedStationsLotjuIdtoIds(RoadStationType.TMS_STATION);

                    break;
                case TMS_STATION_METADATA:
                    allowedTmsRoadStationSensors = getAllowedRoadStationSensorsLotjuIds(RoadStationType.TMS_STATION);

                    break;
                case WEATHER_STATION_SENSOR_METADATA:
                    allowedWeatherStationsLotjuIdtoIds = getAllowedStationsLotjuIdtoIds(RoadStationType.WEATHER_STATION);

                    break;
                //case WEATHER_STATION_METADATA:
                    //break;
                }
            }

            return candidate;
        });
         */

        allowedTmsRoadStationSensors = getAllowedRoadStationSensorsLotjuIds(RoadStationType.TMS_STATION);
        // NOTE! no sensor filtering for weather station (check updateLamData)
        //allowedWeatherRoadStationSensors = getAllowedRoadStationSensorsLotjuIds(RoadStationType.WEATHER_STATION);

        allowedTmsStationsLotjuIdtoIds = getAllowedStationsLotjuIdtoIds(RoadStationType.TMS_STATION);
        allowedWeatherStationsLotjuIdtoIds = getAllowedStationsLotjuIdtoIds(RoadStationType.WEATHER_STATION);
    }

    @Scheduled(fixedRate = 20000)
    @Transactional
    public void persistLamSensorValues() {
        final TimestampCache timestampCache = new TimestampCache();

        final List<SensorValueUpdateParameterDto> updates = lamValueBuffer.getValues().stream()
            .map(wrapper -> new SensorValueUpdateParameterDto(wrapper, timestampCache))
            .collect(Collectors.toList());

        updateSensorData(updates, timestampCache.getMaxTime(), RoadStationType.TMS_STATION);

        log.info("method=persistLamSensorValues tmsBuffer for db update {} / {} incomings", updates.size(), lamValueBuffer.getIncomingElementCount());
    }

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void persistWeatherSensorValues() {
        final TimestampCache timestampCache = new TimestampCache();

        final List<SensorValueUpdateParameterDto> updates = weatherValueBuffer.getValues().stream()
            .map(wrapper -> new SensorValueUpdateParameterDto(timestampCache, wrapper))
            .collect(Collectors.toList());

        updateSensorData(updates, timestampCache.getMaxTime(), RoadStationType.WEATHER_STATION);

        updateSensorHistoryData(updates, RoadStationType.WEATHER_STATION);

        log.info("method=persistWeatherSensorValues weatherBuffer for db update {} / {} incomings", updates.size(), weatherValueBuffer.getIncomingElementCount());
    }

    /**
     * Buffer tms sensors data
     * @param data tms data to be updated into buffer
     * @return count of buffered elements
     */
    @NotTransactionalServiceMethod
    public int updateLamValueBuffer(final List<Lam> data) {
        // Process incoming data set and store filtered items
        return lamValueBuffer.putValues(data.stream()
            // Filter only allowed stations
            .filter(lamAsema -> allowedTmsStationsLotjuIdtoIds.containsKey(lamAsema.getAsemaId()))
            // Collect all sensors from the station
            .flatMap(lamAsema -> lamAsema.getAnturiList().stream()
                // Filter only allowed sensors
                .filter(anturi -> allowedTmsRoadStationSensors.contains(anturi.getLaskennallinenAnturiId()))
                // Map Lam-anturi to wrapper
                .map(anturi -> new LotjuAnturiWrapper<>(
                    lamAsema.getAsemaId(),
                    anturi.getLaskennallinenAnturiId(),
                    anturi,
                    lamAsema.getAika(),
                    allowedTmsStationsLotjuIdtoIds.get(lamAsema.getAsemaId()))
                )
            )
            .collect(Collectors.toList())
        );
    }

    /**
     * Buffer weather data
     * @param data weather data to be updated into buffer
     * @return count of buffered elements
     */
    @NotTransactionalServiceMethod
    public int updateWeatherValueBuffer(final List<TiesaaMittatieto> data) {
        // NOTE! no sensor filtering (check updateLamData)

        // Process incoming data set and store filtered items
        return weatherValueBuffer.putValues(data.stream()
            // Filter only allowed stations
            .filter(tiesaa -> allowedWeatherStationsLotjuIdtoIds.containsKey(tiesaa.getAsemaId()))
            // Collect all sensors from the station
            .flatMap(saaAsema -> saaAsema.getAnturiList().stream()
                // Map saa-anturi to wrapper
                .map(anturi -> new LotjuAnturiWrapper<>(
                    saaAsema.getAsemaId(),
                    anturi.getLaskennallinenAnturiId(),
                    anturi,
                    saaAsema.getAika(),
                    allowedWeatherStationsLotjuIdtoIds.get(saaAsema.getAsemaId())
                ))
            )
            .collect(Collectors.toList())
        );
    }

    /**
     *  Update sensor values to database
     *
     * @param params values
     * @param maxMeasuredTime latest measurement time
     * @param roadStationType type of road station
     */
    private void updateSensorData(final List<SensorValueUpdateParameterDto> params, final OffsetDateTime maxMeasuredTime, final RoadStationType roadStationType) {
        if (CollectionUtils.isEmpty(params)) {
            log.info("method=updateSensorData for {} stations updateCount=0 insertCount=0 tookMs=0", roadStationType);

            return;
        }

        final StopWatch stopWatch = StopWatch.createStarted();

        // First try to update with given parameters data. 0 value in return array means that parameter in question didn't cause update -> should be inserted.
        final int[] updated = sensorValueDao.updateSensorData(params);
        // Resolve parameters that didn't cause update and do insert with those parameters.
        final ArrayList<SensorValueUpdateParameterDto> toInsert = getSensorValueInsertParameters(params, updated);
        final int[] inserted = sensorValueDao.insertSensorData(toInsert);

        updateDataMeasuredTime(roadStationType, maxMeasuredTime);
        updateDataUpdatedTime(roadStationType);

        final int updatedCount = countSum(updated);
        final int insertedCount = countSum(inserted);

        stopWatch.stop();

        log.info("method=updateSensorData for {} stations updateCount={} insertCount={} tookMs={}", roadStationType, updatedCount, insertedCount, stopWatch.getTime());
    }

    private void updateSensorHistoryData(final List<SensorValueUpdateParameterDto> params, final RoadStationType roadStationType) {
        if (CollectionUtils.isEmpty(params)) {
            log.info("method=updateSensorHistoryData for {} stations insertCount=0 tookMs=0", roadStationType);

            return;
        }

        final StopWatch stopWatch = StopWatch.createStarted();

        final int[] inserted = sensorValueHistoryDao.insertSensorData(params);

        log.info("method=updateSensorHistoryData for {} stations insertCount={} tookMs={}", roadStationType, inserted.length, stopWatch.getTime());
    }

    private void updateDataUpdatedTime(final RoadStationType roadStationType) {
        dataStatusService.updateDataUpdated(DataType.getSensorValueUpdatedDataType(roadStationType), dataStatusService.getTransactionStartTime());
    }

    private void updateDataMeasuredTime(final RoadStationType roadStationType, final OffsetDateTime maxMeasuredTime) {
        dataStatusService.updateDataUpdated(DataType.getSensorValueMeasuredDataType(roadStationType), maxMeasuredTime.toInstant());
    }

    @Transactional
    public int cleanWeatherHistoryData(final ZonedDateTime before) {
        final StopWatch stopWatch = StopWatch.createStarted();

        final int removeCount = sensorValueHistoryDao.cleanSensorData(before);

        log.info("method=cleanSensorHistoryData for {} stations older than {} removeCount={} tookMs={}", RoadStationType.WEATHER_STATION, before, removeCount, stopWatch.getTime());

        return removeCount;
    }

    /**
     * @param params list of parameters used in update
     * @param updated list of return values for each update parameter
     * @return list of parameters that had zero update count, meaning that insert should be performed with those parameters.
     */
    private static ArrayList<SensorValueUpdateParameterDto> getSensorValueInsertParameters(final List<SensorValueUpdateParameterDto> params, final int[] updated) {
        final ArrayList<SensorValueUpdateParameterDto> toInsert = new ArrayList<>(updated.length);
        for (int i = 0; i < updated.length; i++) {
            if (updated[i] == 0) {
                toInsert.add(params.get(i));
            }
        }
        return toInsert;
    }

    private int countSum(final int[] values) {
        return IntStream.of(values).sum();
    }
}
