package fi.livi.digitraffic.tie.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fi.livi.digitraffic.tie.service.v1.LotjuAnturiWrapper;

public class SensorValueBuffer<T> {
    private final Object LOCK = new Object();
    // <roadStationId, Map<sensorId, object>
    private Map<Long, Map<Long, LotjuAnturiWrapper<T>>> map = new HashMap<>();

    private int internalIncomingCounter = 0;
    private int internalUpdateCounter = 0;

    private int incomingCount = 0;

    public int putValues(final List<LotjuAnturiWrapper<T>> values) {
        synchronized (LOCK) {
            internalUpdateCounter = 0;

            values.forEach(this::putWrapper);

            return internalUpdateCounter;
        }
    }

    private void putWrapper(LotjuAnturiWrapper<T> candidate) {
        final long roadStationId = candidate.getRoadStationId();
        final long sensorId = candidate.getAnturiLotjuId();

        internalIncomingCounter++;

        // <sensor_id, wrapper>
        Map<Long, LotjuAnturiWrapper<T>> roadStationMap = map.get(roadStationId);

        if (roadStationMap == null) {
            roadStationMap = new HashMap<>();
            map.put(roadStationId, roadStationMap);
        }

        final LotjuAnturiWrapper<T> current = roadStationMap.get(sensorId);

        // Check if new or timestamp is changed
        if (current == null || current.getAika() < candidate.getAika()) {
            roadStationMap.put(sensorId, candidate);

            internalUpdateCounter++;
        }
    }

    public List<LotjuAnturiWrapper<T>> getValues() {
        synchronized (LOCK) {
            try {
                return map.values().stream()
                    .map(Map::values)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            } finally {
                incomingCount = internalIncomingCounter;
                internalIncomingCounter = 0;

                map.clear();
            }
        }
    }

    public int getIncomingElementCount() { return incomingCount; }
}
