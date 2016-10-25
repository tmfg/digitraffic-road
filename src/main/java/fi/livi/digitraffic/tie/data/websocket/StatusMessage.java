package fi.livi.digitraffic.tie.data.websocket;

import org.hibernate.annotations.Immutable;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;

@Immutable
public class StatusMessage {

    public static final StatusMessage OK = new StatusMessage("OK");

    public final String status;

    private StatusMessage(final String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return ToStringHelpper.toStringFull(this);
    }
}
