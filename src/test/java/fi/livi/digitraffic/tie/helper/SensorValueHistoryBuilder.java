package fi.livi.digitraffic.tie.helper;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;

import fi.livi.digitraffic.tie.dao.SensorValueHistoryRepository;
import fi.livi.digitraffic.tie.model.SensorValueHistory;

public class SensorValueHistoryBuilder {
    private final Logger log;
    private final SensorValueHistoryRepository repository;

    private List<SensorValueHistory> list;
    private List<Integer> createdCounts;
    private ZonedDateTime refTime;
    private ChronoUnit chronoUnit;

    public SensorValueHistoryBuilder(final SensorValueHistoryRepository repository, final Logger log) {
        this.repository = repository;
        this.log = log;
        list = new ArrayList<>();
        createdCounts = new ArrayList<>();
    }

    public SensorValueHistoryBuilder setReferenceTime(ZonedDateTime refTime) {
        this.refTime = refTime;

        return this;
    }

    public SensorValueHistoryBuilder setTimeTruncateUnit(ChronoUnit chronoUnit) {
        this.chronoUnit = chronoUnit;

        return this;
    }

    /**
     * Create random n elements with random station and sensor id. Elements measured time is between (current_time - start minutes) and
     * (current_time - stop minutes)
     *
     * @param maxAmount     Max element count
     * @param maxStationId  Max station id
     * @param maxSensorId   Max sensor id
     * @param start         Start offset in minutes from current time
     * @param stop          End offset in minuts from current time
     * @return
     */
    public SensorValueHistoryBuilder buildRandom(int maxAmount, int maxStationId, int maxSensorId, int start, int stop) {
        int count = RandomUtils.nextInt(1, maxAmount);

        IntStream.range(0, count).forEach(i -> {
            list.add(createDummyModel(RandomUtils.nextLong(1, maxStationId),
                RandomUtils.nextLong(1, maxSensorId),
                RandomUtils.nextDouble(0, 10),
                getTime(start, stop)));
        });

        createdCounts.add(count);

        log.info("{} elements with random ids", count);
        return this;
    }

    public SensorValueHistoryBuilder buildWithStationId(int maxAmount, int stationId, int maxSensorId, int start, int stop) {
        int count = RandomUtils.nextInt(1, maxAmount);

        IntStream.range(0, count).forEach(i -> {
            list.add(createDummyModel((long)stationId,
                RandomUtils.nextLong(1, maxSensorId),
                RandomUtils.nextDouble(0, 10),
                getTime(start, stop)));
        });

        createdCounts.add(count);

        log.info("{} elements with station={}", count, stationId);
        return this;
    }

    public SensorValueHistoryBuilder buildWithStationIdAndSensorId(int maxAmount, int stationId, int sensorId, int start, int stop) {
        int count = RandomUtils.nextInt(1, maxAmount);

        IntStream.range(0, count).forEach(i -> {
            list.add(createDummyModel((long)stationId,
                (long)sensorId,
                RandomUtils.nextDouble(0, 10),
                getTime(start, stop)));
        });

        createdCounts.add(count);

        log.info("{} elements with station={} and sensor={}", count, stationId, sensorId);
        return this;
    }

    public SensorValueHistoryBuilder save() {
        log.info("Total {} elements created", list.size());
        list.forEach(i -> log.info("elem: {}, meas {}", i.getSensorValue(), i.getMeasuredTime()));
        repository.saveAll(list);

        return this;
    }

    public int getElementCountAt(int index) {
        return createdCounts.get(index);
    }

    private ZonedDateTime getTime(int start, int stop) {
        ZonedDateTime time = refTime != null ?
                             refTime.minusMinutes(RandomUtils.nextInt(start, stop)) :
                             ZonedDateTime.now().minusMinutes(RandomUtils.nextInt(start, stop));

        if (chronoUnit != null) {
            return time.truncatedTo(chronoUnit);
        }

        return time;
    }

    private SensorValueHistory createDummyModel(long roadStation, long sensor, double value, ZonedDateTime time) {
        SensorValueHistory model = new SensorValueHistory();
        model.setRoadStationId(roadStation);
        model.setSensorId(sensor);
        model.setSensorValue(value);
        model.setMeasuredTime(time);

        return model;
    }
}
