package fi.livi.digitraffic.tie.service.v1;

import static fi.ely.lotju.lam.proto.LAMRealtimeProtos.Lam;
import static fi.ely.lotju.tiesaa.proto.TiesaaProtos.TiesaaMittatieto;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v1.SensorValueDao;
import fi.livi.digitraffic.tie.dto.v1.SensorValueUpdateParameterDto;
import fi.livi.digitraffic.tie.helper.SensorValueBuffer;
import fi.livi.digitraffic.tie.helper.TimestampCache;
import fi.livi.digitraffic.tie.dao.v1.RoadStationDao;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.RoadStationSensor;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.DataStatusService;

@Service
public class SensorDataUpdateService {
    private static final Logger log = LoggerFactory.getLogger(SensorDataUpdateService.class);
    private static final long ALLOWED_SENSOR_EXPIRATION_MILLIS = 300000; // 5min

    private final Map<RoadStationType, Set<Long>> allowedSensorsLotjuIds = new EnumMap<RoadStationType, Set<Long>>(RoadStationType.class);
    private final Map<RoadStationType, Long> allowedSensorsLastUpdatedTimeMillis = new EnumMap<RoadStationType, Long>(RoadStationType.class);

    private final SensorValueDao sensorValueDao;
    private final RoadStationSensorService roadStationSensorService;
    private final RoadStationDao roadStationDao;
    private final DataStatusService dataStatusService;

    private final SensorValueBuffer<Lam.Anturi> lamValueBuffer = new SensorValueBuffer<>();
    private final SensorValueBuffer<TiesaaMittatieto.Anturi> weatherValueBuffer = new SensorValueBuffer<>();

    @Autowired
    public SensorDataUpdateService(final SensorValueDao sensorValueDao, final RoadStationSensorService roadStationSensorService,
                                   final RoadStationDao roadStationDao, final DataStatusService dataStatusService) {
        this.sensorValueDao = sensorValueDao;
        this.roadStationSensorService = roadStationSensorService;
        this.roadStationDao = roadStationDao;
        this.dataStatusService = dataStatusService;
    }

    private Set<Long> getAllowedRoadStationSensorsLotjuIds(final RoadStationType roadStationType) {
        if (allowedSensorsLotjuIds.get(roadStationType) == null || allowedSensorsLastUpdatedTimeMillis.get(roadStationType) < System.currentTimeMillis() - ALLOWED_SENSOR_EXPIRATION_MILLIS) {
            final List<RoadStationSensor> allowedTmsSensors = roadStationSensorService.findAllPublishableRoadStationSensors(roadStationType);

            allowedSensorsLotjuIds.put(roadStationType, allowedTmsSensors.stream().map(RoadStationSensor::getLotjuId).collect(Collectors.toSet()));

            allowedSensorsLastUpdatedTimeMillis.put(roadStationType, System.currentTimeMillis());

            log.info("method=getAllowedRoadStationSensorsLotjuIds fetched sensorCount={} for roadStationType={}",
                allowedSensorsLotjuIds.get(roadStationType).size(),
                roadStationType);
        }

        return allowedSensorsLotjuIds.get(roadStationType);
    }

    @Scheduled(fixedRate = 20000)
    @Transactional
    protected void persistLamSensorValues() {
        final StopWatch stopWatch = StopWatch.createStarted();

        final TimestampCache timestampCache = new TimestampCache();

        List<SensorValueUpdateParameterDto> updates = lamValueBuffer.getValues().stream()
            .map(wrapper -> new SensorValueUpdateParameterDto(wrapper, timestampCache))
            .collect(Collectors.toList());

        updateSensorData(updates, RoadStationType.TMS_STATION);

        // NOTE! not used
        stopWatch.stop();

        log.info("lamBuffer db updates {} / {} incomings", updates.size(), lamValueBuffer.getIncomingCount());
        lamValueBuffer.resetIncomingCount();
    }

    @Scheduled(fixedRate = 30000)
    @Transactional
    protected void persistWeatherSensorValues() {
        final TimestampCache timestampCache = new TimestampCache();

        List<SensorValueUpdateParameterDto> updates = weatherValueBuffer.getValues().stream()
            .map(wrapper -> new SensorValueUpdateParameterDto(timestampCache, wrapper))
            .collect(Collectors.toList());

        updateSensorData(updates, RoadStationType.WEATHER_STATION);

        log.info("weatherBuffer db updates {} / {} incomings", updates.size(), weatherValueBuffer.getIncomingCount());
        weatherValueBuffer.resetIncomingCount();
    }

    /**
     * Buffer tms sensors data
     * @param data
     * @return count of buffered elements
     */
    public int updateLamData(final List<Lam> data) {
        final StopWatch stopWatch = StopWatch.createStarted();

        // From road_station-table: <lotju_id, id> TODO! store cache or something
        final Map<Long, Long> allowedStationsLotjuIdtoIds = roadStationDao.findPublishableRoadStationsIdsMappedByLotjuId(RoadStationType.TMS_STATION);
        //
        final Set<Long> allowedSensorsLotjuIds = getAllowedRoadStationSensorsLotjuIds(RoadStationType.TMS_STATION);

        // Get total incoming anturi count
        final long initialDataRowCount = data.stream().mapToLong(lam -> lam.getAnturiList().size()).sum();

        // Process incoming data set and store filtered items
        lamValueBuffer.putValues(data.stream()
            // Filter only allowed stations
            .filter(lamAsema -> allowedStationsLotjuIdtoIds.containsKey(lamAsema.getAsemaId()))
            // Collect all sensors from the station
            .flatMap(lamAsema -> lamAsema.getAnturiList().stream()
                // Filter only allowed sensors
                .filter(anturi -> allowedSensorsLotjuIds.contains(anturi.getLaskennallinenAnturiId()))
                // Map Lam-anturi to wrapper
                .map(anturi -> new LotjuAnturiWrapper<Lam.Anturi>(
                    lamAsema.getAsemaId(),
                    anturi.getLaskennallinenAnturiId(),
                    anturi,
                    lamAsema.getAika(),
                    allowedStationsLotjuIdtoIds.get(lamAsema.getAsemaId()))
                )
            )
            .collect(Collectors.toList())
        );

        // NOTE! not used
        stopWatch.stop();

        return lamValueBuffer.getUpdateElementCounter();
    }

    /**
     * Buffer weather data
     * @param data
     * @return count of buffered elements
     */
    public int updateWeatherData(final List<TiesaaMittatieto> data) {
        final StopWatch stopWatch = StopWatch.createStarted();

        final Map<Long, Long> allowedStationsLotjuIdtoIds = roadStationDao.findPublishableRoadStationsIdsMappedByLotjuId(RoadStationType.WEATHER_STATION);

        final long initialDataRowCount = data.stream().mapToLong(tiesaa -> tiesaa.getAnturiList().size()).sum();

        // Process incoming data set and store filtered items
        weatherValueBuffer.putValues(data.stream()
            // Filter only allowed stations
            .filter(tiesaa -> allowedStationsLotjuIdtoIds.containsKey(tiesaa.getAsemaId()))
            // Collect all sensors from the station
            .flatMap(saaAsema -> saaAsema.getAnturiList().stream()
                // Map saa-anturi to wrapper
                .map(anturi -> new LotjuAnturiWrapper<TiesaaMittatieto.Anturi>(
                    saaAsema.getAsemaId(),
                    anturi.getLaskennallinenAnturiId(),
                    anturi,
                    saaAsema.getAika(),
                    allowedStationsLotjuIdtoIds.get(saaAsema.getAsemaId())
                ))
            )
            .collect(Collectors.toList())
        );

        // NOTE! not used
        stopWatch.stop();

        return weatherValueBuffer.getUpdateElementCounter();
    }

    /**
     *  Update sensor values to database
     *
     * @param params
     * @param roadStationType
     * @return Pair<updateCount, insertCount>
     */
    private void updateSensorData(final List<SensorValueUpdateParameterDto> params, final RoadStationType roadStationType) {
        if (CollectionUtils.isEmpty(params)) {
            log.info("method=updateSensorData for {} stations updateCount=0 insertCount=0 tookMs=0", roadStationType);

            return;
        }

        final StopWatch stopWatch = StopWatch.createStarted();
        final OffsetDateTime maxMeasuredTime = getMaxMeasured(params);

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

    private void updateDataUpdatedTime(RoadStationType roadStationType) {
        dataStatusService.updateDataUpdated(DataType.getSensorValueUpdatedDataType(roadStationType), dataStatusService.getTransactionStartTime());
    }

    private void updateDataMeasuredTime(RoadStationType roadStationType, OffsetDateTime maxMeasuredTime) {
        dataStatusService.updateDataUpdated(DataType.getSensorValueMeasuredDataType(roadStationType), maxMeasuredTime.toInstant());
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

    private OffsetDateTime getMaxMeasured(final List<SensorValueUpdateParameterDto> params) {
        return params.stream()
                .map(SensorValueUpdateParameterDto::getMeasured)
                .max(OffsetDateTime::compareTo)
                .orElse(null);
    }
}
