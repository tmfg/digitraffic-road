package fi.livi.digitraffic.tie.service.jms.marshaller.dto;

import java.time.Instant;
import java.util.Set;

import fi.livi.digitraffic.tie.service.IllegalArgumentException;

public class MetadataUpdatedMessageDto {

    public enum UpdateType {
        UPDATE("PAIVITYS"), // PAIVITYS
        INSERT("LISAYS"), // LISAYS
        DELETE("POISTO"); // POISTO

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
    private final Set<Long> asemmaLotjuIds;
    private final UpdateType updateType;
    private Instant updateTime;

    public MetadataUpdatedMessageDto(final Long lotjuId, final Set<Long> asemmaLotjuIds, final UpdateType updateType, final Instant updateTime) {
        this.lotjuId = lotjuId;
        this.asemmaLotjuIds = asemmaLotjuIds;
        this.updateType = updateType;
        this.updateTime = updateTime;
    }

    public Long getLotjuId() {
        return lotjuId;
    }

    public Set<Long> getAsemmaLotjuIds() {
        return asemmaLotjuIds;
    }

    public UpdateType getUpdateType() {
        return updateType;
    }

    public Instant getUpdateTime() {
        return updateTime;
    }
}
