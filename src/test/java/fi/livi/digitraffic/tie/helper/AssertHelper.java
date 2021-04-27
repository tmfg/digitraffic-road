package fi.livi.digitraffic.tie.helper;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
        if(t1 == null && t2 == null) return;

        if(t1 == null) {
            fail("was asserted to be null, was not");
        }

        if(t2 == null) {
            fail("given value was null");
        }

        final ZonedDateTime tz1 = t1.withZoneSameInstant(UTC);
        final ZonedDateTime tz2 = t2.withZoneSameInstant(UTC);

        assertEquals(tz1, tz2);
    }

    public static void collectionContains(final Object objectToFind, List<?> collection) {
        final Optional<?> first = collection.stream().filter(e -> e.equals(objectToFind)).findFirst();
        assertTrue(first.isPresent(), "Element " + objectToFind + " not found in collection");
    }
}
