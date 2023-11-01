package fi.livi.digitraffic.tie.service.weather;

import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.MetadataUpdatedMessageDto.UpdateType.UPDATE;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
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

    private final WeatherStationUpdater weatherStationUpdater;
    private final WeatherStationSensorUpdater weatherStationSensorUpdater;

    public WeatherMetadataUpdateMessageHandler(final WeatherStationUpdater weatherStationUpdater,
                                               final WeatherStationSensorUpdater weatherStationSensorUpdater) {
        this.weatherStationUpdater = weatherStationUpdater;
        this.weatherStationSensorUpdater = weatherStationSensorUpdater;
    }

    // Disable info logging as it can be normally over 1 s. Log only if over default warning level 5 s.
    @PerformanceMonitor(maxInfoExcecutionTime = 100000)
    public int updateMetadataFromJms(final List<WeatherMetadataUpdatedMessageDto> weatherMetadataUpdates) {
        int updateCount = 0;

        for (final WeatherMetadataUpdatedMessageDto message : weatherMetadataUpdates) {
            log.info("method=updateMetadataFromJms roadStationType={} data: {}", RoadStationType.WEATHER_STATION.name(), ToStringHelper.toStringFull(message));
            final EntityType type = message.getEntityType();

            // Skip messages that are older than 24 hours as metadata update job is running every 12 hours
            // so this could also be 12 h but for safety margin lets keep it in 24h. This could happen in case
            // of JMS connection problems for over 24 hours.
            if ( message.getUpdateTime().plus(1, ChronoUnit.DAYS).isAfter(Instant.now()) ) {
                try {
                    switch (type) {
                    case WEATHER_STATION:
                        updateCount += updateStations(message.getAsemaLotjuIds(), message.getUpdateType());
                        break;
                    case WEATHER_STATION_COMPUTATIONAL_SENSOR: // Update sensors of stations
                    case ROAD_ADDRESS: // We don't update specific addresses but stations using them
                        updateCount += updateStations(message.getAsemaLotjuIds());
                        break;
                    case WEATHER_COMPUTATIONAL_SENSOR: // insert/delete/update of sensor
                        // Update sensor
                        weatherStationSensorUpdater.updateWeatherSensor(message.getLotjuId(), message.getUpdateType());
                        // When we insert, update or delete sensors, we always call update for the affected stations. It will update stations sensors
                        updateCount += updateStations(message.getAsemaLotjuIds());
                        break;
                    // These we don't care as we don't use these
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

    private int updateStations(final Set<Long> asemaLotjuIds) {
        return updateStations(asemaLotjuIds, UPDATE);
    }
    private int updateStations(final Set<Long> asemaLotjuIds, final MetadataUpdatedMessageDto.UpdateType updateType) {
        return (int) asemaLotjuIds.stream().filter(lotjuId -> weatherStationUpdater.updateWeatherStationAndSensors(lotjuId, updateType)).count();
    }
}
