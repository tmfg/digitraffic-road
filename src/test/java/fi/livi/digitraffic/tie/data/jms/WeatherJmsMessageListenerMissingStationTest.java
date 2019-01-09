package fi.livi.digitraffic.tie.data.jms;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.jms.JMSException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.ely.lotju.tiesaa.proto.TiesaaProtos;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.SensorValue;
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WeatherJmsMessageListenerMissingStationTest extends AbstractWeatherJmsMessageListenerTest {
    private static final Logger log = LoggerFactory.getLogger(WeatherJmsMessageListenerMissingStationTest.class);

    /**
     * Send some data bursts to jms handler including sensor data for non existing road station.
     * That should not fail all the updates.
     *
     * @throws JMSException
     * @throws IOException
     */
    @Test
    public void testDataForNonExistingStation() throws JMSException, IOException {

        final Map<Long, WeatherStation> weatherStationsWithLotjuId = weatherStationService
            .findAllPublishableWeatherStationsMappedByLotjuId();

        final JMSMessageListener.JMSDataUpdater<TiesaaProtos.TiesaaMittatieto> dataUpdater =
            createTiesaaMittatietoJMSDataUpdater();

        final JMSMessageListener<TiesaaProtos.TiesaaMittatieto> jmsMessageListener =
            createTiesaaMittatietoJMSMessageListener(dataUpdater);

        final List<RoadStationSensor> availableSensors = getAvailableRoadStationSensors();

        Iterator<WeatherStation> stationsIter = weatherStationsWithLotjuId.values().iterator();

        final List<TiesaaProtos.TiesaaMittatieto> data = new ArrayList<>();

        while (true) {
            if (!stationsIter.hasNext()) {
                stationsIter = weatherStationsWithLotjuId.values().iterator();
            }
            final WeatherStation currentStation = stationsIter.next();

            TiesaaProtos.TiesaaMittatieto tiesaa = generateTiesaaMittatieto(Instant.now(), availableSensors, currentStation.getLotjuId());

            data.add(tiesaa);

            jmsMessageListener.onMessage(createBytesMessage(tiesaa));

            if (data.size() >= 10 || weatherStationsWithLotjuId.size() <= data.size()) {
                break;
            }
        }

        // Create data for non existing station
        TiesaaProtos.TiesaaMittatieto tiesaa = generateTiesaaMittatieto(Instant.now(), availableSensors, NON_EXISTING_STATION_LOTJU_ID);
        data.add(tiesaa);
        jmsMessageListener.onMessage(createBytesMessage(tiesaa));

        jmsMessageListener.drainQueueScheduled();

        // Clear because data has been changed by jmsMessageListener in db and entity manager doesn't know about it
        entityManager.clear();

        log.info("Check data validy");
        // Assert sensor values are updated to db
        final List<Long> tiesaaLotjuIds = data.stream().map(p -> p.getAsemaId()).collect(Collectors.toList());

        final Map<Long, List<SensorValue>> valuesMap =
            roadStationSensorService.findNonObsoleteSensorvaluesListMappedByTmsLotjuId(tiesaaLotjuIds, RoadStationType.WEATHER_STATION);

        assertData(data, valuesMap);
        assertDataIsJustUpdated();
    }
}
