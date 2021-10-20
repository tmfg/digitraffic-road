package fi.livi.digitraffic.tie.service.v1.weather;

import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.MetadataUpdatedMessageDto.UpdateType.UPDATE;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.MetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.WeatherMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.WeatherMetadataUpdatedMessageDto.EntityType;

/**
 * Service to handle JMS metadata updated messages
 */
@ConditionalOnNotWebApplication
@Component
public class WeatherMetadataUpdateMessageHandler {
    private static final Logger log = LoggerFactory.getLogger(WeatherMetadataUpdateMessageHandler.class);

    private WeatherStationUpdater weatherStationUpdater;
    private WeatherStationSensorUpdater weatherStationSensorUpdater;

    public WeatherMetadataUpdateMessageHandler(final WeatherStationUpdater weatherStationUpdater,
                                               final WeatherStationSensorUpdater weatherStationSensorUpdater) {
        this.weatherStationUpdater = weatherStationUpdater;
        this.weatherStationSensorUpdater = weatherStationSensorUpdater;
    }

    // Disable info logging as it can be normally over 1 s. Log only if over default warning level 5 s.
    @PerformanceMonitor(maxInfoExcecutionTime = 100000)
    public int updateWeatherMetadataFromJms(final List<WeatherMetadataUpdatedMessageDto> weatherMetadataUpdates) {
        int updateCount = 0;

        for (WeatherMetadataUpdatedMessageDto message : weatherMetadataUpdates) {
            log.info("method=updateWeatherMetadataFromJms {}", ToStringHelper.toStringFull(message));
            final EntityType type = message.getEntityType();
            final MetadataUpdatedMessageDto.UpdateType updateType = message.getUpdateType();

            // Skip messages that are older than 24 hours as metadata update job is running every 12 hours
            // so this could also be 12 h but for safety margin lets keep it in 24h. This could happen in case
            // of JMS connection problems for over 24 hours.
            if ( message.getUpdateTime().plus(1, ChronoUnit.DAYS).isAfter(Instant.now()) ) {
                try {
                    switch (type) {
                    case WEATHER_STATION:
                        if (weatherStationUpdater.updateWeatherStationAndSensors(message.getLotjuId(), updateType)) {
                            updateCount++;
                        }
                        break;
                    case WEATHER_STATION_COMPUTATIONAL_SENSOR:
                        if (updateType.isDelete()) {
                            weatherStationUpdater.updateWeatherStationAndSensors(message.getLotjuId(), UPDATE);
                        } else if (weatherStationUpdater.updateWeatherStationAndSensors(message.getLotjuId(), updateType)) {
                            updateCount++;
                        }
                        break;
                    case WEATHER_COMPUTATIONAL_SENSOR:
                        if (weatherStationSensorUpdater.updateWeatherSensor(message.getLotjuId(), updateType)) {
                            updateCount++;
                        }
                        // Even when updateType would be delete, this means we also have to update the station
                        updateCount += message.getAsemmaLotjuIds().stream()
                            .filter(asemaId -> weatherStationUpdater.updateWeatherStationAndSensors(asemaId, UPDATE)).count();
                        break;
                    case ROAD_ADDRESS:
                        // All ROAD_ADDRESS update types just calls update for road station
                        updateCount += message.getAsemmaLotjuIds().stream()
                            .filter(asemaId -> weatherStationUpdater.updateWeatherStationAndSensors(asemaId, UPDATE)).count();
                        if (message.getAsemmaLotjuIds().isEmpty()) {
                            log.warn("method=updateWeatherMetadataFromJms message had no station id's {}", ToStringHelper.toStringFull(message));
                        }
                        break;

                    case WEATHER_SENSOR:
                    case WEATHER_SENSOR_TYPE:
                    case SENSOR_MESSAGE:
                    case PREPROSESSING:
                    case VALUE_EQUIVALENCE:
                    case WEATHER_COMPUTATIONAL_SENSOR_FORMULA:
                        // no handle as won't affect us.
                        break;
                    default:
                        log.error(String.format("method=updateWeatherMetadataFromJms Unknown EntityType %s", type));
                    }
                } catch (final Exception e) {
                    log.error(String.format("method=updateWeatherMetadataFromJms Error with %s", ToStringHelper.toStringFull(message)), e);
                }
            }
        }

        return updateCount;
    }
}
