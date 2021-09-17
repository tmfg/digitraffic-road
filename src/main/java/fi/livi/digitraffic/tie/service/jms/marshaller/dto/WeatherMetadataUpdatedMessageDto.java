package fi.livi.digitraffic.tie.service.jms.marshaller.dto;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.service.IllegalArgumentException;

public class WeatherMetadataUpdatedMessageDto extends MetadataUpdatedMessageDto {

    public enum EntityType {
        WEATHER_STATION("TIESAA_ASEMA"),
        WEATHER_SENSOR("TIESAA_ANTURI"),
        WEATHER_SENSOR_TYPE("TIESAA_ANTURITYYPPI"),
        SENSOR_MESSAGE("ANTURISANOMA"),
        PREPROSESSING("ESIPROSESSOINTI"),
        WEATHER_COMPUTATIONAL_SENSOR("TIESAA_LASKENNALLINENANTURI"),
        WEATHER_STATION_COMPUTATIONAL_SENSOR("TIESAA_ASEMA_LASKENNALLINENANTURI"),
        VALUE_EQUIVALENCE("ARVOVASTAAVUUS"),
        ROAD_ADDRESS("TIEOSOITE"),
        WEATHER_COMPUTATIONAL_SENSOR_FORMULA("TIESAA_LASKENNALLINENANTURI_KAAVA");

        private final String externalValue;

        EntityType(final String externalValue) {
            this.externalValue = externalValue;
        }

        public static EntityType fromExternalValue(final String value) {
            final Optional<EntityType> found = Arrays.stream(values()).filter(v -> v.externalValue.equals(value)).findFirst();
            if (found.isPresent()) {
                return found.get();
            }
            throw new IllegalArgumentException("Unknown EntityType " + value);
        }
    }

    private final EntityType entityType;

    public WeatherMetadataUpdatedMessageDto(final Long lotjuId, final Set<Long> asemmaLotjuIds, final UpdateType updateType, final Instant updateTime, final EntityType entityType) {
        super(lotjuId, asemmaLotjuIds, updateType, updateTime);
        this.entityType = entityType;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
