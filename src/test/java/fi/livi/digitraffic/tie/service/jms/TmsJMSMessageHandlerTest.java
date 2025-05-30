package fi.livi.digitraffic.tie.service.jms;


import static fi.livi.digitraffic.test.util.AssertUtil.assertTimesEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.activemq.artemis.jms.client.ActiveMQBytesMessage;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.ely.lotju.lam.proto.LAMRealtimeProtos;
import fi.livi.digitraffic.common.util.ThreadUtil;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDtoV1;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.model.roadstation.SensorValue;
import fi.livi.digitraffic.tie.model.tms.TmsStation;
import fi.livi.digitraffic.tie.service.jms.marshaller.TmsDataJMSMessageMarshaller;
import fi.livi.digitraffic.tie.service.tms.TmsStationService;
import jakarta.jms.JMSException;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TmsJMSMessageHandlerTest extends AbstractJMSMessageHandlerTest {

    private static final Logger log = LoggerFactory.getLogger(TmsJMSMessageHandlerTest.class);

    static {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final DatatypeFactory datatypeFactory;
    private static final XMLGregorianCalendar aikaikkunaAlku =
            datatypeFactory.newXMLGregorianCalendar("2016-02-16T10:00:00Z");
    private static final XMLGregorianCalendar aikaikkunaLoppu =
            datatypeFactory.newXMLGregorianCalendar("2016-06-16T11:00:00Z");
    private static final Instant timeWindowStart =
            Instant.parse("2016-02-16T12:00:00+02:00");
    private static final Instant timeWindowEnd =
            Instant.parse("2016-06-16T14:00:00+03:00");
    private static int sensorValueToSet = new Random().nextInt(1000);

    @Autowired
    private TmsStationService tmsStationService;

    @BeforeEach
    public void initData() {
        TestUtils.truncateTmsData(entityManager);
        entityManager.flush();
        TestUtils.generateDummyTmsStations(50).forEach(s -> entityManager.persist(s));
        entityManager.flush();
        TestUtils.commitAndEndTransactionAndStartNew();
        sensorDataUpdateService.updateStationsAndSensorsMetadata();
    }

    @AfterEach
    protected void cleanDb() {
        TestUtils.commitAndEndTransactionAndStartNew();
        TestUtils.truncateTmsData(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew();
    }

    /**
     * Send some data bursts to jms handler and test performance of database updates.
     */
    @Test
    public void testPerformanceForReceivedMessages()
            throws JMSException, IOException {

        final Map<Long, TmsStation> lamsWithLotjuId = tmsStationService.findAllPublishableTmsStationsMappedByLotjuId();
        final JMSMessageHandler.JMSDataUpdater<LAMRealtimeProtos.Lam> dataUpdater = createLamJMSDataUpdater();
        final JMSMessageHandler<LAMRealtimeProtos.Lam> tmsJmsMessageListener = createTmsJmsMessageListener(dataUpdater);
        final List<RoadStationSensor> publishableSensors =
                findPublishableRoadStationSensors(RoadStationType.TMS_STATION);

        Iterator<TmsStation> stationsIter = lamsWithLotjuId.values().iterator();

        int testBurstsLeft = 10;
        long handleDataTotalTime = 0;
        // This just an value got by running tests. Purpose is only to notice if there is big change in performance.
        final long maxHandleTime = testBurstsLeft * 2000;
        final List<LAMRealtimeProtos.Lam> data = new ArrayList<>(lamsWithLotjuId.size());
        Instant time = Instant.now();

        while (testBurstsLeft > 0) {
            testBurstsLeft--;

            final StopWatch sw = StopWatch.createStarted();

            data.clear();
            do {
                if (!stationsIter.hasNext()) {
                    stationsIter = lamsWithLotjuId.values().iterator();
                }
                final TmsStation currentStation = stationsIter.next();

                final List<LAMRealtimeProtos.Lam> lams =
                        generateLams(time, publishableSensors, currentStation.getLotjuId());

                for (final LAMRealtimeProtos.Lam lam : lams) {
                    data.add(lam);
                    final ActiveMQBytesMessage message = createBytesMessage(lam);
                    tmsJmsMessageListener.onMessage(message);
                }

                time = time.plusMillis(2000);

            } while (data.size() < 100 && lamsWithLotjuId.size() > data.size());

            log.info("Data generation tookMs={}", sw.getDuration().toMillis());
            final StopWatch swHandle = StopWatch.createStarted();

            tmsJmsMessageListener.drainQueueScheduled();
            handleDataTotalTime += swHandle.getDuration().toMillis();

            // send data with 1 s interval
            final long sleep = 1000 - sw.getDuration().toMillis();
            if (sleep < 0) {
                log.warn("Data generation and handle took {} ms and should use maximum 1000 ms", sw.getDuration().toMillis());
            } else {
                ThreadUtil.delayMs(sleep);
            }

        }

        flushSensorBuffer(true);

        log.info("Handle tms data total tookMs={} and maxMs={} result={}",
                handleDataTotalTime, maxHandleTime, handleDataTotalTime <= maxHandleTime ? "(OK)" : "(FAIL)");

        checkDataValidity(data);
        assertTrue(handleDataTotalTime <= maxHandleTime,
                "Handle data took too much time " + handleDataTotalTime + " ms and max was " + maxHandleTime + " ms");

        checkLastUpdated();
    }

    private void checkLastUpdated() {
        final Instant lastUpdated =
                roadStationSensorService.getLatestSensorValueUpdatedTime(RoadStationType.TMS_STATION);
        final Instant timeInPast2Minutes = Instant.now().minusSeconds(2 * 60);

        log.info("lastUpdated={} vs now={}", lastUpdated, timeInPast2Minutes);
        assertTrue(lastUpdated.isAfter(timeInPast2Minutes),
                "LastUpdated not fresh " + lastUpdated + " <= " + timeInPast2Minutes);

        final List<SensorValueDtoV1> updated =
                roadStationSensorService.findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter
                        (lastUpdated.minusSeconds(1), RoadStationType.TMS_STATION);
        assertFalse(updated.isEmpty());
    }

    private List<LAMRealtimeProtos.Lam> generateLams(final Instant time, final List<RoadStationSensor> availableSensors,
                                                     final Long stationLotjuId) {
        // Generate two different messages for same station to test filtering newest sensor data from both
        final LAMRealtimeProtos.Lam.Builder lamBuilder1 = LAMRealtimeProtos.Lam.newBuilder();
        final LAMRealtimeProtos.Lam.Builder lamBuilder2 = LAMRealtimeProtos.Lam.newBuilder();

        lamBuilder1.setAsemaId(stationLotjuId);
        lamBuilder1.setAika(time.toEpochMilli());
        lamBuilder1.setIsRealtime(false);
        lamBuilder1.setIsNollaOhitus(false);

        lamBuilder2.setAsemaId(stationLotjuId);
        lamBuilder2.setAika(time.plusMillis(1000).toEpochMilli());
        lamBuilder2.setIsRealtime(false);
        lamBuilder2.setIsNollaOhitus(false);

        log.debug("Start with arvo " + sensorValueToSet);
        boolean odd = true;
        for (final RoadStationSensor availableSensor : availableSensors) {
            final LAMRealtimeProtos.Lam.Anturi.Builder anturiBuilder = LAMRealtimeProtos.Lam.Anturi.newBuilder();
            anturiBuilder.setArvo(sensorValueToSet);
            anturiBuilder.setLaskennallinenAnturiId(availableSensor.getLotjuId());
            anturiBuilder.setAikaikkunaAlku(aikaikkunaAlku.toGregorianCalendar().toInstant().toEpochMilli());
            anturiBuilder.setAikaikkunaLoppu(aikaikkunaLoppu.toGregorianCalendar().toInstant().toEpochMilli());

            if (odd) {
                lamBuilder1.addAnturi(anturiBuilder.build());
            } else {
                lamBuilder2.addAnturi(anturiBuilder.build());
            }
            odd = !odd;
            // Increase value for every sensor to validate correct updates
            sensorValueToSet++;
        }
        log.debug("End with arvo={}", sensorValueToSet - 1);
        return Arrays.asList(lamBuilder1.build(), lamBuilder2.build());

    }

    private void checkDataValidity(final List<LAMRealtimeProtos.Lam> data) {
        log.info("Check data validity");
        // Assert sensor values are updated to db
        final List<Long> lamLotjuIds = data.stream().map(LAMRealtimeProtos.Lam::getAsemaId).distinct().toList();
        final Map<Long, List<SensorValue>> valuesMap =
                roadStationSensorService.findNonObsoleteSensorvaluesListMappedByTmsLotjuId(lamLotjuIds,
                        RoadStationType.TMS_STATION);

        boolean timeWindowsFound = false;
        for (final LAMRealtimeProtos.Lam lam : data) {
            final long asemaLotjuId = lam.getAsemaId();
            final List<SensorValue> sensorValues = valuesMap.get(asemaLotjuId);

            assertNotNull(sensorValues);
            final List<LAMRealtimeProtos.Lam.Anturi> anturit = lam.getAnturiList();

            for (final LAMRealtimeProtos.Lam.Anturi anturi : anturit) {
                final Optional<SensorValue> found =
                        sensorValues
                                .stream()
                                .filter(sensorValue -> anturi.getLaskennallinenAnturiId() ==
                                        sensorValue.getRoadStationSensor().getLotjuId())
                                .findFirst();
                assertTrue(found.isPresent());

                final SensorValue sv = found.get();
                assertEquals(sv.getValue(), anturi.getArvo(), 0.05d);

                if (found.get().getTimeWindowStart() != null) {
                    assertTimesEqual(timeWindowStart, sv.getTimeWindowStart());
                    assertTimesEqual(timeWindowEnd, sv.getTimeWindowEnd());

                    timeWindowsFound = true;
                }
            }
        }
        assertTrue(timeWindowsFound, "Time window was set to zero sensors");
        log.info("Data is valid");
    }

    private JMSMessageHandler<LAMRealtimeProtos.Lam> createTmsJmsMessageListener(
            final JMSMessageHandler.JMSDataUpdater<LAMRealtimeProtos.Lam> dataUpdater) {
        return new JMSMessageHandler<>(JMSMessageHandler.JMSMessageType.TMS_DATA_REALTIME, dataUpdater,
                new TmsDataJMSMessageMarshaller(), lockingService.getInstanceId());
    }

    private JMSMessageHandler.JMSDataUpdater<LAMRealtimeProtos.Lam> createLamJMSDataUpdater() {
        return (data) -> {
            final StopWatch sw = StopWatch.createStarted();

            final int updated = sensorDataUpdateService.updateLamValueBuffer(data);

            log.info("handleData tookMs={}", sw.getDuration().toMillis());
            return updated;
        };
    }
}
