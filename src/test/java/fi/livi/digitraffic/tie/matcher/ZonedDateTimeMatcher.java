package fi.livi.digitraffic.tie.matcher;

import java.time.ZonedDateTime;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class ZonedDateTimeMatcher extends BaseMatcher<ZonedDateTime> {

    private final ZonedDateTime time;

    private ZonedDateTimeMatcher(final ZonedDateTime time) {
        this.time = time;
    }

    public static ZonedDateTimeMatcher of(final ZonedDateTime time) {
        return new ZonedDateTimeMatcher(time);
    }

    @Override
    public boolean matches(final Object item) {
        if(item instanceof java.lang.String) {
            final ZonedDateTime other = ZonedDateTime.parse((String) item);
            return other.isEqual(time);
        }
        return false;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText(time.toString());
    }
}