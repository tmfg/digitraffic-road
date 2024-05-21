package fi.livi.digitraffic.tie.service.variablesign.v1;


import fi.livi.digitraffic.tie.converter.waze.WazeDatex2Converter;
import fi.livi.digitraffic.tie.dto.variablesigns.v1.TrafficSignHistoryV1;
import fi.livi.digitraffic.tie.dto.variablesigns.v1.VariableSignFeatureV1;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
/**
 * Component for filtering out unreliable data that contains test data for set of devices
 * and for two time slices.
 */
public class TestDataFilteringService {
    private static final Logger logger = LoggerFactory.getLogger(TestDataFilteringService.class);

    public static final Set<String> testDevices = Set.of(
        "VME/TIO01K502",
        "VME/TIO01K500",
        "VME/TIO015111",
        "VME/TIO015152"
    );

    public static final Set<Interval> testTimes = Set.of(
      interval("2024-05-20T19:00:00.00Z", "2024-05-21T02:00:00.00Z"),
      interval("2024-05-21T19:00:00.00Z", "2024-05-22T02:00:00.00Z")
    );

    public List<TrafficSignHistoryV1> filter(final String deviceId, final List<TrafficSignHistoryV1> history) {
        return history.stream()
            .filter(data -> isProductionData(deviceId, data))
            .collect(Collectors.toList());
    }

    public boolean isProductionData(final VariableSignFeatureV1 feature) {
        try {
            if (testDevices.contains(feature.getProperties().id)) {
                return !isTestTime(feature.getProperties().createDate);
            }
        } catch(final Exception e) {
            logger.error(String.format("Error in isProductionData for %s", feature.getProperties().id), e);

            return true;
        }

        return true;
    }
    private boolean isProductionData(final String deviceId, final TrafficSignHistoryV1 data) {
        if(testDevices.contains(deviceId)) {
            // test device!, now check the time
            return !isTestTime(data.getCreated());
        }

        return true;
    }

    private boolean isTestTime(final Instant time) {
            return testTimes.stream()
            .anyMatch(i -> i.contains(time.toEpochMilli()));
    }

    private static Interval interval(final String instant1, final String instant2) {
        return new Interval(
            Instant.parse(instant1).toEpochMilli(),
            Instant.parse(instant2).toEpochMilli()
        );
    }
}
