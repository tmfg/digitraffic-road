package fi.livi.digitraffic.tie.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.livi.digitraffic.tie.service.v1.LotjuAnturiWrapper;

public class SensorValueBuffer<T> {
    private static final Object LOCK = new Object();
    private Map<Long, Map<Long, LotjuAnturiWrapper<T>>> map = new HashMap<>();

    private List<String> changes = new ArrayList<>();

    private int updateElementCounter = 0;

    public void putValues(final List<LotjuAnturiWrapper<T>> values) {
        synchronized (LOCK) {
            updateElementCounter = 0;

            values.forEach(this::putWrapper);
        }
    }

    private void putWrapper(LotjuAnturiWrapper<T> candidate) {
        final long roadStationId = candidate.getRoadStationId();
        final long sensorId = candidate.getAnturiLotjuId();

        // <sensor_id, wrapper>
        final Map<Long, LotjuAnturiWrapper<T>> roadStationMap = map.get(roadStationId);

        if (roadStationMap == null) {
            final Map<Long, LotjuAnturiWrapper<T>> tmp = new HashMap<>();
            tmp.put(sensorId, candidate);

            map.put(roadStationId, tmp);

            updateElementCounter++;

            return;
        }

        if (!roadStationMap.containsKey(sensorId)) {
            roadStationMap.put(sensorId, candidate);

            updateElementCounter++;

            return;
        } else {
            LotjuAnturiWrapper<T> current = roadStationMap.get(sensorId);

            // Is timestamp changed
            if (current.getAika() < candidate.getAika()) {
                // Replace existing
                roadStationMap.put(sensorId, candidate);

                updateElementCounter++;
            }

                /**
                // Is value changed
                if (!current.getValue().equals(candidateDto.getValue())) {
                    changes.add("rsid: " + current.getRoadStationId()
                        + " sid: " + current.getSensorLotjuId()
                        + " val: " + current.getValue() + " <> " + candidateDto.getValue()
                        + " meas: " + current.getMeasured() + " <> " + candidateDto.getMeasured());
                 }
                 */
        }
    }

    public int getUpdateElementCounter() {
        return updateElementCounter;
    }

    public List<LotjuAnturiWrapper<T>> getValues() {
        List<LotjuAnturiWrapper<T>> result = new ArrayList<>();

        synchronized (LOCK) {
            map.values().stream()
                .forEach(roadStation ->
                    result.addAll(roadStation.values())
                );

            map.clear();
        }

        return result;
    }

    /**
     public void status() {
     System.out.println("***\nstations: " + map.size() + "\nanturit: " + map.values().stream().mapToInt(m -> m.size()).sum());
     }
     */
}
