package fi.livi.digitraffic.tie.data.jms;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.transaction.TestTransaction;

import fi.ely.lotju.lam.proto.LAMRealtimeProtos;
import fi.livi.digitraffic.tie.conf.jms.listener.TmsJMSMessageListener;
import fi.livi.digitraffic.tie.data.dto.SensorValueDto;
import fi.livi.digitraffic.tie.data.service.SensorDataUpdateService;
import fi.livi.digitraffic.tie.lotju.xsd.lam.Lam;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.SensorValue;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationService;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TmsJmsMessageListenerTest extends AbstractJmsMessageListenerTest {

    static {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final DatatypeFactory datatypeFactory;
    private static final XMLGregorianCalendar aikaikkunaAlku = datatypeFactory.newXMLGregorianCalendar("2016-02-16T10:00:00Z");
    private static final XMLGregorianCalendar aikaikkunaLoppu = datatypeFactory.newXMLGregorianCalendar("2016-06-16T11:00:00Z");
    private static final ZonedDateTime timeWindowStart = ZonedDateTime.parse("2016-02-16T12:00:00+02:00[Europe/Helsinki]");
    private static final ZonedDateTime timeWindowEnd = ZonedDateTime.parse("2016-06-16T14:00:00+03:00[Europe/Helsinki]");

    private static final Logger log = LoggerFactory.getLogger(TmsJmsMessageListenerTest.class);

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @Autowired
    private TmsStationService tmsStationService;

    @Autowired
    private SensorDataUpdateService sensorDataUpdateService;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    private Marshaller jaxbMarshaller;

    @Before
    public void initData() throws JAXBException {
        jaxbMarshaller = JAXBContext.newInstance(Lam.class).createMarshaller();

        if (!TestTransaction.isActive()) {
            TestTransaction.start();
        }

        log.info("Add available sensors for TMS Stations");
        final String merge =
                "MERGE INTO ROAD_STATION_SENSORS TGT\n" +
                "USING (\n" +
                "  SELECT RS.ID ROAD_STATION_ID, S.ID ROAD_STATION_SENSOR_ID\n" +
                "  FROM ROAD_STATION_SENSOR S, ROAD_STATION RS\n" +
                "  WHERE S.OBSOLETE = 0\n" +
                "    AND S.ROAD_STATION_TYPE = 'TMS_STATION'\n" +
                "    AND EXISTS (\n" +
                "      SELECT NULL\n" +
                "      FROM ALLOWED_ROAD_STATION_SENSOR ALLOWED\n" +
                "      WHERE ALLOWED.NATURAL_ID = S.NATURAL_ID\n" +
                "        AND ALLOWED.ROAD_STATION_TYPE = S.ROAD_STATION_TYPE\n" +
                "   )\n" +
                "   AND RS.ROAD_STATION_TYPE = 'TMS_STATION'\n" +
                "   AND RS.OBSOLETE_DATE IS NULL\n" +
                ") SRC\n" +
                "ON (SRC.ROAD_STATION_ID = TGT.ROAD_STATION_ID AND SRC.ROAD_STATION_SENSOR_ID = TGT.ROAD_STATION_SENSOR_ID)\n" +
                "WHEN NOT MATCHED THEN INSERT (TGT.ROAD_STATION_ID, TGT.ROAD_STATION_SENSOR_ID)\n" +
                "     VALUES (SRC.ROAD_STATION_ID, SRC.ROAD_STATION_SENSOR_ID)";
        jdbcTemplate.execute(merge);

        log.info("Commit changes");
        TestTransaction.flagForCommit();
        TestTransaction.end();
        assertFalse(TestTransaction.isActive());
        log.info("Commit done");
        // Now we have stations with sensors and lotjuid:s that we can update
    }

    public static BytesMessage createBytesMessage(final LAMRealtimeProtos.Lam lam) throws JMSException, IOException {
        final ByteArrayOutputStream bous = new ByteArrayOutputStream(0);
        lam.writeDelimitedTo(bous);
        final byte[] lamBytes = bous.toByteArray();

        final BytesMessage bytesMessage = mock(BytesMessage.class);

        when(bytesMessage.getBodyLength()).thenReturn((long)lamBytes.length);
        when(bytesMessage.readBytes(any(byte[].class))).then(invocation -> {
            final byte[] bytes = (byte[]) invocation.getArguments()[0];
            System.arraycopy(lamBytes, 0, bytes, 0, lamBytes.length);

            return lamBytes.length;
        });

        return bytesMessage;
    }

        /**
         * Send some data bursts to jms handler and test performance of database updates.
         * @throws JAXBException
         * @throws DatatypeConfigurationException
         */
    @Test
    public void test1PerformanceForReceivedMessages() throws JAXBException, DatatypeConfigurationException, JMSException, IOException {
        final Map<Long, TmsStation> lamsWithLotjuId = tmsStationService.findAllPublishableTmsStationsMappedByLotjuId();

        final JMSMessageListener.JMSDataUpdater<LAMRealtimeProtos.Lam> dataUpdater = (data) -> {
            final long start = System.currentTimeMillis();
            if (TestTransaction.isActive()) {
                TestTransaction.flagForCommit();
                TestTransaction.end();
            }
            TestTransaction.start();
            assertTrue("Update failed", sensorDataUpdateService.updateLamData(data));
            TestTransaction.flagForCommit();
            TestTransaction.end();
            long end = System.currentTimeMillis();
            log.info("handleData took " + (end-start) + " ms");
        };
        final TmsJMSMessageListener tmsJmsMessageListener = new TmsJMSMessageListener(dataUpdater, true, log);

        Instant time = Instant.now();

        // Generate update-data
        final float minX = 0.0f;
        final float maxX = 100.0f;
        final Random rand = new Random();
        int arvo = (int)(rand.nextFloat() * (maxX - minX) + minX);
        log.info("Start with arvo " + arvo);

        final List<RoadStationSensor> availableSensors =
                roadStationSensorService.findAllNonObsoleteRoadStationSensors(RoadStationType.TMS_STATION);

        Iterator<TmsStation> stationsIter = lamsWithLotjuId.values().iterator();

        int testBurstsLeft = 10;
        long handleDataTotalTime = 0;
        long maxHandleTime = testBurstsLeft * 1000;
        final List<LAMRealtimeProtos.Lam> data = new ArrayList<>(lamsWithLotjuId.size());
        while(testBurstsLeft > 0) {
            testBurstsLeft--;

            final long start = System.currentTimeMillis();
            data.clear();
            while (true) {
                if (!stationsIter.hasNext()) {
                    stationsIter = lamsWithLotjuId.values().iterator();
                }
                final TmsStation currentStation = stationsIter.next();
                final LAMRealtimeProtos.Lam.Builder lamBuilder = LAMRealtimeProtos.Lam.newBuilder();

                lamBuilder.setAsemaId(currentStation.getLotjuId());
                lamBuilder.setAika(time.toEpochMilli());
                lamBuilder.setIsRealtime(false);
                lamBuilder.setIsNollaOhitus(false);

                for (final RoadStationSensor availableSensor : availableSensors) {
                    final LAMRealtimeProtos.Lam.Anturi.Builder anturiBuilder = LAMRealtimeProtos.Lam.Anturi.newBuilder();
                    anturiBuilder.setArvo(arvo);
                    anturiBuilder.setLaskennallinenAnturiId(availableSensor.getLotjuId());

                    anturiBuilder.setAikaikkunaAlku(aikaikkunaAlku.toGregorianCalendar().toInstant().toEpochMilli());
                    anturiBuilder.setAikaikkunaLoppu(aikaikkunaLoppu.toGregorianCalendar().toInstant().toEpochMilli());

                    lamBuilder.addAnturi(anturiBuilder.build());

                    arvo += 1f;
                }

                final LAMRealtimeProtos.Lam lam = lamBuilder.build();

                data.add(lam);
                time = time.plusMillis(1000);

                tmsJmsMessageListener.onMessage(createBytesMessage(lam));

                if (data.size() >= 100 || lamsWithLotjuId.values().size() <= data.size()) {
                    break;
                }
            }
            long end = System.currentTimeMillis();
            long duration = (end - start);
            log.info("Data generation took {} ms", duration);
            long startHandle = System.currentTimeMillis();
            tmsJmsMessageListener.drainQueueScheduled();
            long endHandle = System.currentTimeMillis();
            handleDataTotalTime = handleDataTotalTime + (endHandle-startHandle);

            try {
                // send data with 1 s interval
                long sleep = 1000 - duration;
                if (sleep < 0) {
                    log.error("Data generation took too long");
                } else {
                    Thread.sleep(sleep);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        log.info("End with arvo {}", arvo);
        log.info("Handle tms data total took {} ms and max was {} ms {}",
            handleDataTotalTime <= maxHandleTime ? "(OK)" : "(FAIL)", handleDataTotalTime, maxHandleTime);

        log.info("Check data validy");
        // Assert sensor values are updated to db
        final List<Long> lamLotjuIds = data.stream().map(p -> p.getAsemaId()).collect(Collectors.toList());
        final Map<Long, List<SensorValue>> valuesMap =
                roadStationSensorService.findNonObsoleteSensorvaluesListMappedByTmsLotjuId(lamLotjuIds, RoadStationType.TMS_STATION);

        boolean timeWindowsFound = false;
        for (final LAMRealtimeProtos.Lam lam : data) {
            long asemaLotjuId = lam.getAsemaId();
            final List<SensorValue> sensorValues = valuesMap.get(asemaLotjuId);
            final List<LAMRealtimeProtos.Lam.Anturi> anturit = lam.getAnturiList();

            for (final LAMRealtimeProtos.Lam.Anturi anturi : anturit) {
                final Optional<SensorValue> found =
                        sensorValues
                                .stream()
                                .filter(sensorValue -> sensorValue.getRoadStationSensor().getLotjuId() != null)
                                .filter(sensorValue -> anturi.getLaskennallinenAnturiId() == sensorValue.getRoadStationSensor()
                                    .getLotjuId())
                                .findFirst();
                assertTrue(found.isPresent());

                final SensorValue sv = found.get();
                Assert.assertEquals(sv.getValue(), (double) anturi.getArvo(), 0.05d);
                if (found.get().getTimeWindowStart() != null) {
                    Assert.assertEquals("Time window start not equal", timeWindowStart, sv.getTimeWindowStart());
                    Assert.assertEquals("Time window end not equal", timeWindowEnd, sv.getTimeWindowEnd());
                    timeWindowsFound = true;
                }
            }
        }
        Assert.assertTrue("Time window was set to zero sensors", timeWindowsFound);
        log.info("Data is valid");
        Assert.assertTrue("Handle data took too much time " + handleDataTotalTime + " ms and max was " + maxHandleTime + " ms", handleDataTotalTime <= maxHandleTime);
    }

    @Test
    public void test2LastUpdated() {
        final ZonedDateTime lastUpdated = roadStationSensorService.getSensorValueLastUpdated(RoadStationType.TMS_STATION);
        log.info("lastUpdated {} vs {}", lastUpdated, LocalDateTime.now().minusMinutes(2));
        assertTrue("LastUpdated not fresh " + lastUpdated, lastUpdated.isAfter(ZonedDateTime.now().minusMinutes(2)));
        final List<SensorValueDto> updated = roadStationSensorService.findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter
            (lastUpdated.minusSeconds(1), RoadStationType.TMS_STATION);
        assertFalse(updated.isEmpty());
    }
}
