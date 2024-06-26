package fi.livi.digitraffic.tie.service.jms.marshaller.dto;

import java.time.Instant;
import java.util.Set;

import fi.livi.digitraffic.tie.service.IllegalArgumentException;

public class MetadataUpdatedMessageDto {

    public enum UpdateType {
        UPDATE("PAIVITYS"),
        INSERT("LISAYS"),
        DELETE("POISTO");

        private final String externalValue;

        UpdateType(final String externalValue) {
            this.externalValue = externalValue;
        }

        public static UpdateType fromExternalValue(final String name) {
            switch(name) {
            case "PAIVITYS":
                return UPDATE;
            case "LISAYS":
                return INSERT;
            case "POISTO":
                return DELETE;
            default:
                throw new IllegalArgumentException("Unknown metadata UpdateType " + name);
            }
        }

        public boolean isDelete() {
            return this.equals(DELETE);
        }

        public String getExternalValue() {
            return externalValue;
        }
    }

    private final Long lotjuId;
    private final Set<Long> asemaLotjuIds;
    private final UpdateType updateType;
    private final Instant updateTime;

    public MetadataUpdatedMessageDto(final Long lotjuId, final Set<Long> asemaLotjuIds, final UpdateType updateType, final Instant updateTime) {
        this.lotjuId = lotjuId;
        this.asemaLotjuIds = asemaLotjuIds;
        this.updateType = updateType;
        this.updateTime = updateTime;
    }

    public Long getLotjuId() {
        return lotjuId;
    }

    public Set<Long> getAsemaLotjuIds() {
        return asemaLotjuIds;
    }

    public UpdateType getUpdateType() {
        return updateType;
    }

    public Instant getUpdateTime() {
        return updateTime;
    }
}
