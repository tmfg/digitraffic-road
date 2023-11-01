package fi.livi.digitraffic.tie.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class AssertHelper {
    private AssertHelper() {}

    public static void assertCollectionSize(final int expectedSize, final Collection<?> collection) {
        assertCollectionSize(null, expectedSize, collection);
    }

    public static void assertCollectionSize(final String message, final int expectedSize, final Collection<?> collection) {
        final int collectionSize = collection.size();
        assertEquals(expectedSize, collectionSize, String.format("%sCollection size was expected to be %d but was %s.",
            message != null ? message + " " : "", expectedSize, collectionSize));
    }

    public static void assertEmpty(final Collection<?> col) {
        assertCollectionSize(0, col);
    }

    public static void assertTimesEqual(final ZonedDateTime t1, final ZonedDateTime t2) {
        assertTimesEqual(DateHelper.toInstant(t1), DateHelper.toInstant(t2), 0);
    }

    public static void assertTimesEqual(final Instant t1, final Instant t2) {
        assertTimesEqual(t1, t2, 0);
    }
    public static void assertTimesEqual(final Instant t1, final Instant t2, final int maxDiffMs) {
        if(t1 == null && t2 == null) return;

        if(t1 == null) {
            fail("was asserted to be null, was not");
        }

        if(t2 == null) {
            fail("given value was null");
        }

        final Duration timeElapsed = Duration.between(t1, t2);
        final long diffMillis = timeElapsed.toMillis();
        assertTrue(Math.abs(diffMillis) <= maxDiffMs, "Difference between times was " + diffMillis + " ms and allowed diff was " + maxDiffMs + " ms");
    }

    public static void collectionContains(final Object objectToFind, List<?> collection) {
        final Optional<?> first = collection.stream().filter(e -> e.equals(objectToFind)).findFirst();
        assertTrue(first.isPresent(), "Element " + objectToFind + " not found in collection");
    }

    public static void assertGe(final long compare, final int to) {
        assertGe(compare, to, -1);
    }

    public static void assertGe(final long compare, final int to, final int maxDiff) {
        assertTrue(compare >= to, MessageFormat.format("{0} >= {1} was false", compare, to));
        if (maxDiff >= 0) {
            final long diff = compare - to;
            System.out.println(diff);
            assertTrue(diff <= maxDiff, MessageFormat.format("{0} - {1} == {2} > {3} (maxDiff)", compare, to, diff, maxDiff));
        }
    }

    public static void assertLe(final long compare, final int to) {
        assertGe(compare, to, -1);
    }

    public static void assertLe(final long compare, final int to, final int maxDiff) {
        assertTrue(compare <= to, MessageFormat.format("{0} <= {1} was false", compare, to));
        if (maxDiff >= 0) {
            final long diff = to - compare;
            assertTrue(diff <= maxDiff, MessageFormat.format("{0} - {1} == {2} > {3} (maxDiff)",  to, compare, diff, maxDiff));
        }
    }

}
