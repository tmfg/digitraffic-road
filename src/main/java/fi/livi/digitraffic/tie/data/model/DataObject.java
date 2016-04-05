package fi.livi.digitraffic.tie.data.model;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

// TODO: correct format, correct timezone
@JsonPropertyOrder({"localTime", "utc"})
public class DataObject {
    private final ZonedDateTime timestamp;

    public DataObject(final ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public DataObject() {
        this.timestamp = ZonedDateTime.now();
    }

    public String getLocalTime() {
        return timestamp.toString();
    }

    public String getUtc() {
        return ZonedDateTime.ofInstant(timestamp.toInstant(), ZoneOffset.UTC).toString();
    }
}
