package fi.livi.digitraffic.tie.service.trafficmessage.location;

import java.util.List;

/**
 * Holds both the successfully parsed items and any per-row parse errors
 * collected during a single reader run.
 *
 * <p>When {@link #hasErrors()} is {@code true} the caller should treat the
 * import as failed: no version should be persisted and the transaction should
 * be rolled back so the DB stays clean for a retry.
 */
public record ParseResult<T>(List<T> items, List<String> parseErrors) {

    public boolean hasErrors() {
        return !parseErrors.isEmpty();
    }
}

