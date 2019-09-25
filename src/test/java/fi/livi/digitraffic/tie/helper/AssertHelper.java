package fi.livi.digitraffic.tie.helper;

import static java.time.ZoneOffset.UTC;

import java.time.ZonedDateTime;
import java.util.Collection;

import org.junit.Assert;

public final class AssertHelper {
    private AssertHelper() {}

    public static void assertCollectionSize(final int expectedSize, final Collection<?> collection) {
        final int collectionSize = collection.size();

        Assert.assertTrue(String.format("Collection size was expected to be %d, was %s", expectedSize, collectionSize),
            collectionSize == expectedSize);
    }

    public static void assertEmpty(final Collection<?> col) {
        assertCollectionSize(0, col);
    }

    public static void assertTimesEqual(final ZonedDateTime t1, final ZonedDateTime t2) {
        if(t1 == null && t2 == null) return;

        if(t1 == null && t2 != null) {
            Assert.fail("was asserted to be null, was not");
        }

        if(t1 != null && t2 == null) {
            Assert.fail("given value was null");
        }

        final ZonedDateTime tz1 = t1.withZoneSameInstant(UTC);
        final ZonedDateTime tz2 = t2.withZoneSameInstant(UTC);

        Assert.assertEquals(tz1, tz2);
    }

}
