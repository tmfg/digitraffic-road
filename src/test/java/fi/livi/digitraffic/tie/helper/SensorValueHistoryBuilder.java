package fi.livi.digitraffic.tie.helper;

import static fi.livi.digitraffic.tie.helper.MathUtils.roundToScale;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

import org.slf4j.Logger;

import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.dao.roadstation.SensorValueHistoryRepository;
import fi.livi.digitraffic.tie.model.roadstation.SensorValueHistory;
import fi.livi.digitraffic.tie.model.roadstation.SensorValueReliability;

public class SensorValueHistoryBuilder {
    private final Logger log;

    private final SensorValueHistoryRepository repository;
    private final List<SensorValueHistory> list;
    private final List<Integer> createdCounts;
    private Instant refTime;
    private ChronoUnit chronoUnit;
    private final Random random;

    public SensorValueHistoryBuilder(final SensorValueHistoryRepository repository, final Logger log) {
        this.repository = repository;
        this.log = log;
        this.random = new Random();
        list = new ArrayList<>();
        createdCounts = new ArrayList<>();
    }

    public SensorValueHistoryBuilder setReferenceTime(final Instant refTime) {
        this.refTime = refTime;

        return this;
    }

    public SensorValueHistoryBuilder setTimeTruncateUnit(final ChronoUnit chronoUnit) {
        this.chronoUnit = chronoUnit;

        return this;
    }

    /**
     * Create random n elements with random station and sensor id. Elements measured time is between (current_time - start minutes) and
     * (current_time - stop minutes)
     *
     * @param maxAmount    Max element count
     * @param maxStationId Max station id
     * @param maxSensorId  Max sensor id
     * @param start        Start offset in minutes from current time
     * @param stop         End offset in minutes from current time
     * @return the builder
     */
    public SensorValueHistoryBuilder buildRandom(final int maxAmount, final int maxStationId, final int maxSensorId,
                                                 final int start,
                                                 final int stop) {
        final int count = random.nextInt(1, maxAmount);

        IntStream.range(0, count).forEach(i ->
                list.add(createDummyModel(random.nextLong(1, maxStationId),
                        random.nextLong(1, maxSensorId),
                        random.nextDouble(0, 10),
                        getTime(start, stop))));

        createdCounts.add(count);

        log.info("{} elements with random ids", count);
        return this;
    }

    public SensorValueHistoryBuilder buildWithStationId(final int maxAmount, final long stationId,
                                                        final Set<Long> sensorIds, final int start,
                                                        final int stop) {
        final int count = random.nextInt(1, maxAmount);

        if (maxAmount > sensorIds.size()) {
            throw new IllegalArgumentException(
                    "Sensor count " + maxAmount + " too large, maximum allowed is " + sensorIds.size());
        }
        final Iterator<Long> iter = sensorIds.iterator();
        IntStream.range(0, count).forEach(i ->
                list.add(createDummyModel(stationId,
                        iter.next(),
                        random.nextDouble(0, 10),
                        getTime(start, stop))));

        createdCounts.add(count);

        log.info("{} elements with station={}", count, stationId);
        return this;
    }

    public SensorValueHistoryBuilder save() {
        log.info("Total {} elements created", list.size());
        list.forEach(
                i -> log.info("rsId: {}, sensorId: {}, value: {}, measured {}", i.getRoadStationId(), i.getSensorId(),
                        i.getSensorValue(), i.getMeasuredTime()));

        repository.saveAll(list);

        return this;
    }

    public SensorValueHistoryBuilder truncate() {
        repository.deleteAll();
        return this;
    }

    public int getElementCountAt(final int index) {
        return createdCounts.get(index);
    }

    public List<SensorValueHistory> getGeneratedHistory() {
        return list;
    }

    private Instant getTime(final int start, final int stop) {
        final Instant time = (refTime != null ? refTime : Instant.now())
                .minus(random.nextInt(start, stop), ChronoUnit.MINUTES);

        if (chronoUnit != null) {
            return time.truncatedTo(chronoUnit);
        }

        return time;
    }

    private SensorValueHistory createDummyModel(final long roadStationId, final long sensorId, final double value,
                                                final Instant time) {
        final SensorValueHistory model = new SensorValueHistory();
        model.setRoadStationId(roadStationId);
        model.setSensorId(sensorId);
        model.setSensorValue(roundToScale(value, 2));
        model.setMeasuredTime(time);
        if (TestUtils.getRandomBoolean()) {
            model.setReliability(TestUtils.getRandomEnum(SensorValueReliability.class));
        }
        return model;
    }
}
