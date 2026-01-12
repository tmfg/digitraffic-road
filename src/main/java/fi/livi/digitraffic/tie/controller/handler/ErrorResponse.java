package fi.livi.digitraffic.tie.controller.handler;

import java.time.Instant;

public class ErrorResponse {
    public final Instant timestamp;

    public final int status;

    public final String error;

    public final String message;

    public final String path;

    private final String queryString;

    public ErrorResponse(final int status, final String error, final String message, final String path, final String queryString) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.queryString = queryString;
    }
}