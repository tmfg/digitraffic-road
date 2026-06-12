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
    public void upsertDataUpdatedWithTime() {
        final Instant time = Instant.now().minusSeconds(10);
        dataUpdatedRepository.upsertDataUpdated(DataType.TMS_STATION_SENSOR_METADATA, time);
        final Instant result = dataUpdatedRepository.findUpdatedTime(DataType.TMS_STATION_SENSOR_METADATA);
        assertPlusMinusMillis(time, result, 500);
    }

    @Test
    public void upsertDataUpdatedWithSubtype() {
        final String subtype = RandomStringUtils.secure().nextAlphanumeric(5);
        dataUpdatedRepository.upsertDataUpdated(DataType.TMS_STATION_SENSOR_METADATA, subtype);
        final Instant result = dataUpdatedRepository.findUpdatedTime(DataType.TMS_STATION_SENSOR_METADATA, Collections.singletonList(subtype));
        assertPlusMinusMillis(dataUpdatedRepository.getTransactionStartTime(), result, 500);
    }

    @Test
    public void upsertDataUpdatedWithMultipleSubtypesAndTimes() {
        final String subtype1 = RandomStringUtils.secure().nextAlphanumeric(5);
        final Instant subtype1Time = Instant.now().minusSeconds(10);
        final String subtype2 = RandomStringUtils.secure().nextAlphanumeric(5);
        final Instant subtype2Time = Instant.now().minusSeconds(20);
        dataUpdatedRepository.upsertDataUpdated(DataType.TMS_STATION_SENSOR_METADATA, subtype1, subtype1Time);
        dataUpdatedRepository.upsertDataUpdated(DataType.TMS_STATION_SENSOR_METADATA, subtype2, subtype2Time);

        final Instant result = dataUpdatedRepository.findUpdatedTime(DataType.TMS_STATION_SENSOR_METADATA, Arrays.asList(subtype1, subtype2));
        assertPlusMinusMillis(subtype1Time, result, 500);
    }

    @Test
    public void upsertDataUpdatedWithSubtypeAndTime() {
        final Instant setTime = Instant.now().minusSeconds(10);
        final String subtype = RandomStringUtils.secure().nextAlphanumeric(5);
        dataUpdatedRepository.upsertDataUpdated(DataType.TMS_STATION_SENSOR_METADATA, subtype, setTime);
        final Instant result = dataUpdatedRepository.findUpdatedTime(DataType.TMS_STATION_SENSOR_METADATA, Collections.singletonList(subtype));
        assertPlusMinusMillis(setTime, result, 500);
    }

    @Test
    public void upsertDataUpdatedWithMultipleTimes() {
        final Instant setTime1 = Instant.now().minusSeconds(100);
        final Instant setTime2 = Instant.now().minusSeconds(10);
        final String subtype = RandomStringUtils.secure().nextAlphanumeric(5);
        dataUpdatedRepository.upsertDataUpdated(DataType.TMS_STATION_SENSOR_METADATA, subtype, setTime1);
        dataUpdatedRepository.upsertDataUpdated(DataType.TMS_STATION_SENSOR_METADATA, subtype, setTime2);
        final Instant result = dataUpdatedRepository.findUpdatedTime(DataType.TMS_STATION_SENSOR_METADATA, Collections.singletonList(subtype));
        assertPlusMinusMillis(setTime2, result, 500);
    }

    @Test
    public void upsertNoSubtypeConflictResolution() {
        // Upserting the same DataType twice without subtype should result in ON CONFLICT update, not duplicate rows
        final Instant firstTime = Instant.now().minusSeconds(100);
        final Instant secondTime = Instant.now().minusSeconds(10);
        dataUpdatedRepository.upsertDataUpdated(DataType.TMS_STATION_SENSOR_METADATA, firstTime);
        dataUpdatedRepository.upsertDataUpdated(DataType.TMS_STATION_SENSOR_METADATA, secondTime);
        final long rowCount = ((Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM data_updated WHERE data_type = ?1 AND subtype IS NULL")
                .setParameter(1, DataType.TMS_STATION_SENSOR_METADATA.name())
                .getSingleResult()).longValue();
        MatcherAssert.assertThat(rowCount, CoreMatchers.is(1L));
        final Instant result = dataUpdatedRepository.findUpdatedTime(DataType.TMS_STATION_SENSOR_METADATA);
        assertPlusMinusMillis(secondTime, result, 500);
    }

    @Test
    public void findUpdatedTimeDoesNotReturnSubtypedRows() {
        // findUpdatedTime(DataType) must return only the no-subtype row, not rows with real subtypes
        final String subtype = RandomStringUtils.secure().nextAlphanumeric(5);
        final Instant subtypeTime = Instant.now().minusSeconds(10);
        final Instant noSubtypeTime = Instant.now().minusSeconds(50);
        dataUpdatedRepository.upsertDataUpdated(DataType.TMS_STATION_SENSOR_METADATA, subtype, subtypeTime);
        dataUpdatedRepository.upsertDataUpdated(DataType.TMS_STATION_SENSOR_METADATA, noSubtypeTime);
        final Instant result = dataUpdatedRepository.findUpdatedTime(DataType.TMS_STATION_SENSOR_METADATA);
        assertPlusMinusMillis(noSubtypeTime, result, 500);
    }

    private void assertPlusMinusMillis(final Instant expected, final Instant actual, final long deltaMillis) {
        MatcherAssert.assertThat(actual.toEpochMilli(),
            CoreMatchers.allOf(Matchers.greaterThanOrEqualTo(expected.minusMillis(deltaMillis).toEpochMilli()),
                Matchers.lessThanOrEqualTo(expected.plusMillis(deltaMillis).toEpochMilli())));
    }

}
