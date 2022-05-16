package fi.livi.digitraffic.tie.dao;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractJpaTest;
import fi.livi.digitraffic.tie.dao.v1.DataUpdatedRepository;
import fi.livi.digitraffic.tie.model.DataType;

public class DataUpdatedRepositoryTest extends AbstractJpaTest {

    @Autowired
    private DataUpdatedRepository dataUpdatedRepository;

    @Test
    public void upsertDataUpdated() {
        dataUpdatedRepository.upsertDataUpdated(DataType.TMS_STATION_SENSOR_METADATA);
        final Instant result = dataUpdatedRepository.findUpdatedTime(DataType.TMS_STATION_SENSOR_METADATA);
        assertPlusMinusMillis(dataUpdatedRepository.getTransactionStartTime(), result, 500);
    }

    @Test
    public void upsertDataUpdatedWithNullExtension() {
        dataUpdatedRepository.upsertDataUpdated(DataType.TMS_STATION_SENSOR_METADATA, null);
        final Instant result = dataUpdatedRepository.findUpdatedTime(DataType.TMS_STATION_SENSOR_METADATA);
        assertPlusMinusMillis(dataUpdatedRepository.getTransactionStartTime(), result, 500);
    }

    @Test
    public void upsertDataUpdatedWithExtension() {
        final String extension = RandomStringUtils.randomAlphabetic(5);
        dataUpdatedRepository.upsertDataUpdated(DataType.TMS_STATION_SENSOR_METADATA, extension);
        final Instant result = dataUpdatedRepository.findUpdatedTime(DataType.TMS_STATION_SENSOR_METADATA, Collections.singletonList(extension));
        assertPlusMinusMillis(dataUpdatedRepository.getTransactionStartTime(), result, 500);
    }

    @Test
    public void upsertDataUpdatedWithMultipleExtensionsAndTimes() {
        final String extension1 = RandomStringUtils.randomAlphabetic(5);
        final Instant extension1Time = Instant.now().minusSeconds(10);
        final String extension2 = RandomStringUtils.randomAlphabetic(5);
        final Instant extension2Time = Instant.now().minusSeconds(20);
        dataUpdatedRepository.upsertDataUpdated(DataType.TMS_STATION_SENSOR_METADATA, extension1, extension1Time);
        dataUpdatedRepository.upsertDataUpdated(DataType.TMS_STATION_SENSOR_METADATA, extension2, extension2Time);

        final Instant result = dataUpdatedRepository.findUpdatedTime(DataType.TMS_STATION_SENSOR_METADATA, Arrays.asList(extension1, extension2));
        assertPlusMinusMillis(extension1Time, result, 500);
    }

    @Test
    public void upsertDataUpdatedWithExtensionAndTime() {
        final Instant setTime = Instant.now().minusSeconds(10);
        final String extension = RandomStringUtils.randomAlphabetic(5);
        dataUpdatedRepository.upsertDataUpdated(DataType.TMS_STATION_SENSOR_METADATA, extension, setTime);
        final Instant result = dataUpdatedRepository.findUpdatedTime(DataType.TMS_STATION_SENSOR_METADATA, Collections.singletonList(extension));
        assertPlusMinusMillis(setTime, result, 500);
    }

    @Test
    public void upsertDataUpdatedWithMultipleTimes() {
        final Instant setTime1 = Instant.now().minusSeconds(100);
        final Instant setTime2 = Instant.now().minusSeconds(10);
        final String extension = RandomStringUtils.randomAlphabetic(5);
        dataUpdatedRepository.upsertDataUpdated(DataType.TMS_STATION_SENSOR_METADATA, extension, setTime1);
        dataUpdatedRepository.upsertDataUpdated(DataType.TMS_STATION_SENSOR_METADATA, extension, setTime2);
        final Instant result = dataUpdatedRepository.findUpdatedTime(DataType.TMS_STATION_SENSOR_METADATA, Collections.singletonList(extension));
        assertPlusMinusMillis(setTime2, result, 500);
    }

    private void assertPlusMinusMillis(final Instant expected, final Instant actual, long deltaMillis) {
        MatcherAssert.assertThat(actual.toEpochMilli(),
            CoreMatchers.allOf(Matchers.greaterThanOrEqualTo(expected.minusMillis(deltaMillis).toEpochMilli()),
                Matchers.lessThanOrEqualTo(expected.plusMillis(deltaMillis).toEpochMilli())));
    }

}
