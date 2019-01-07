package fi.livi.digitraffic.tie.data.jms;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.jms.JMSException;

import org.apache.commons.lang3.time.StopWatch;
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
public class WeatherJmsMessageListenerTest extends AbstractWeatherJmsMessageListenerTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractJmsMessageListenerTest.class);

    private static final long NON_EXISTING_STATION_LOTJU_ID = -123456789L;

    /**
     * Send some data bursts to jms handler and test performance of database updates.
     * @throws JMSException
     * @throws IOException
     */
    @Test
    public void testPerformanceForReceivedMessages() throws JMSException, IOException {
        final Map<Long, WeatherStation> weatherStationsWithLotjuId = weatherStationService
            .findAllPublishableWeatherStationsMappedByLotjuId();

        final JMSMessageListener.JMSDataUpdater<TiesaaProtos.TiesaaMittatieto> dataUpdater = createTiesaaMittatietoJMSDataUpdater();

        final JMSMessageListener<TiesaaProtos.TiesaaMittatieto> jmsMessageListener =
            createTiesaaMittatietoJMSMessageListener(dataUpdater);


        final List<RoadStationSensor> availableSensors = getAvailableRoadStationSensors();

        Iterator<WeatherStation> stationsIter = weatherStationsWithLotjuId.values().iterator();

        int testBurstsLeft = 10;
        long handleDataTotalTime = 0;
        final long maxHandleTime = testBurstsLeft * (long)(1000 * 2.5);
        final List<TiesaaProtos.TiesaaMittatieto> data = new ArrayList<>();
        Instant time = Instant.now();

        while(testBurstsLeft > 0) {
            testBurstsLeft--;

            final StopWatch sw = StopWatch.createStarted();
            data.clear();

            while (true) {
                if (!stationsIter.hasNext()) {
                    stationsIter = weatherStationsWithLotjuId.values().iterator();
                }
                final WeatherStation currentStation = stationsIter.next();

                TiesaaProtos.TiesaaMittatieto tiesaa =
                    generateTiesaaMittatieto(time, availableSensors, currentStation.getLotjuId());

                data.add(tiesaa);

                time = time.plusMillis(1000);

                jmsMessageListener.onMessage(createBytesMessage(tiesaa));

                if (data.size() >= 100 || weatherStationsWithLotjuId.size() <= data.size()) {
                    break;
                }
            }

            sw.stop();
            log.info("Data generation tookMs={}", sw.getTime());
            StopWatch swHandle = StopWatch.createStarted();
            jmsMessageListener.drainQueueScheduled();
            handleDataTotalTime += swHandle.getTime();

            try {
                // send data with 1 s intervall
                long sleep = 1000 - sw.getTime();

                if (sleep < 0) {
                    log.error("Data generation took too long");
                } else {
                    Thread.sleep(sleep);
                }
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("Handle weather data total tookMs={} and max was maxMs={} result={}",
                 handleDataTotalTime, maxHandleTime, handleDataTotalTime <= maxHandleTime ? "(OK)" : "(FAIL)");
        log.info("Check data validy");

        entityManager.clear();

        // Assert sensor values are updated to db
        final List<Long> tiesaaLotjuIds = data.stream().map(p -> p.getAsemaId()).collect(Collectors.toList());
        final Map<Long, List<SensorValue>> valuesMap =
                    roadStationSensorService.findNonObsoleteSensorvaluesListMappedByTmsLotjuId(tiesaaLotjuIds, RoadStationType.WEATHER_STATION);

        assertData(data, valuesMap);
        assertDataIsJustUpdated();
        entityManager.clear();

        assertTrue("Handle data took too much time " + handleDataTotalTime + " ms and max was " + maxHandleTime + " ms", handleDataTotalTime <= maxHandleTime);
    }
}
