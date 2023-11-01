package fi.livi.digitraffic.tie.service;

import static fi.livi.digitraffic.tie.TestUtils.entityManagerFlushAndClear;
import static fi.livi.digitraffic.tie.TestUtils.readResourceContent;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.xml.transform.StringSource;

import fi.livi.digitraffic.tie.conf.jms.ExternalIMSMessage;
import fi.livi.digitraffic.tie.dao.trafficmessage.datex2.Datex2Repository;
import fi.livi.digitraffic.tie.service.trafficmessage.Datex2UpdateService;
import fi.livi.digitraffic.tie.service.trafficmessage.Datex2XmlStringToObjectMarshaller;
import fi.livi.digitraffic.tie.service.trafficmessage.TrafficMessageImsJsonConverterV1;
import fi.livi.digitraffic.tie.service.trafficmessage.v1.RegionGeometryDataServiceV1;
import fi.livi.digitraffic.tie.service.trafficmessage.v1.TrafficMessageDataServiceV1;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class TrafficMessageTestHelper {
    private static final Logger log = getLogger(TrafficMessageTestHelper.class);

    public final static String GUID_WITH_JSON = "GUID50001238";
    public final static String GUID_WITH_ACTIVE_ANDPASSIVE_RECORD = "GUID90000001";

    // Version of incoming ims message schema
    public enum ImsXmlVersion {
        V1_2_1;

        public static ImsXmlVersion getLatestVersion() {
            return ImsXmlVersion.values()[ImsXmlVersion.values().length-1];
        }
    }

    public enum ImsJsonVersion {
        V0_2_4(2.04, 204),
        V0_2_5(2.05, 205),
        V0_2_6(2.06, 206),
        V0_2_8(2.08, 208),
        V0_2_9(2.09, 209),
        V0_2_10(2.10, 210),
        V0_2_12(2.12, 212),
        V0_2_13(2.13, 213),
        V0_2_14(2.14, 214),
        V0_2_15(2.15, 215),
        V0_2_16(2.16, 216),
        V0_2_17(2.17, 217);

        public final double version;
        public final int intVersion;

        ImsJsonVersion(final double version, final int intVersion) {
            this.version = version;
            this.intVersion = intVersion;
        }

        public static ImsJsonVersion getLatestVersion() {
            return ImsJsonVersion.values()[ImsJsonVersion.values().length-1];
        }
    }

    private static final String START_DATE_TIME_PLACEHOLDER = "START_DATE_TIME";
    private static final String END_DATE_TIME_PLACEHOLDER = "END_DATE_TIME";
    public static final String JSON_MESSAGE_PLACEHOLDER = "JSON_MESSAGE";
    public static final String D2_MESSAGE_PLACEHOLDER = "D2_MESSAGE";
    public static final String SITUATION_RELEASE_DATE_TIME_PLACEHOLDER = "SITUATION_RELEASE_DATE_TIME";
    public static final String SITUATION_VERSION_DATE_TIME_PLACEHOLDER = "SITUATION_VERSION_DATE_TIME";
    public static final String SITUATION_VERSION_PLACEHOLDER = "SITUATION_VERSION";

    @Autowired
    protected JdbcTemplate jdbcTemplate;
    @PersistenceContext
    protected EntityManager entityManager;
    @Autowired
    protected GenericApplicationContext applicationContext;

    private Datex2UpdateService v2Datex2UpdateService;
    private TrafficMessageDataServiceV1 trafficMessageDataServiceV1;

    @Autowired
    private Datex2Repository datex2Repository;
    @Autowired
    @Qualifier("imsJaxb2Marshaller")
    private Jaxb2Marshaller imsJaxb2Marshaller;

    @BeforeEach
    public void cleanDb() {
        datex2Repository.deleteAll();
    }

    public Datex2UpdateService getV2Datex2UpdateService() {
        if (v2Datex2UpdateService == null) {
            v2Datex2UpdateService = applicationContext.getAutowireCapableBeanFactory().createBean(Datex2UpdateService.class);
        }
        return v2Datex2UpdateService;
    }

    public TrafficMessageDataServiceV1 getTrafficMessageDataServiceV1() {
        if (trafficMessageDataServiceV1 == null) {
            final RegionGeometryDataServiceV1 regionGeometryDataServiceV1 =
                applicationContext.getAutowireCapableBeanFactory().createBean(RegionGeometryDataServiceV1.class);
            applicationContext.getBeanFactory().registerSingleton(
                regionGeometryDataServiceV1.getClass().getCanonicalName(), regionGeometryDataServiceV1);

            trafficMessageDataServiceV1 = new TrafficMessageDataServiceV1(
                applicationContext.getBean(Datex2Repository.class),
                applicationContext.getBean(Datex2XmlStringToObjectMarshaller.class),
                applicationContext.getBean(TrafficMessageImsJsonConverterV1.class)
            );
        }
        return trafficMessageDataServiceV1;
    }


    public static String getSituationIdForSituationType(final String situationTypeName) {
        switch (situationTypeName) {
        case "TRAFFIC_ANNOUNCEMENT":
            return "GUID10000001";
        case "EXEMPTED_TRANSPORT":
            return "GUID10000002";
        case "WEIGHT_RESTRICTION":
            return "GUID10000003";
        case "ROAD_WORK":
            return "GUID10000004";
        }
        throw new IllegalStateException("Unknown SituationType " + situationTypeName);
    }

    public void initImsDataFromFile(final String file, final ImsJsonVersion jsonVersion, final ZonedDateTime startTime,
                                    final ZonedDateTime endTime, final boolean lifeCycleCanceled) throws IOException {
        final String raw = readResourceContent("classpath:tloik/ims/" + file);
        final String rawWithJsonOk = replaceSimpleJsonPlaceholders(raw, jsonVersion, startTime, endTime, lifeCycleCanceled);
        final String rawWithJsonAndDatexOk = replaceDatex2Placeholders(rawWithJsonOk, startTime, endTime, jsonVersion.intVersion, lifeCycleCanceled);
        final ExternalIMSMessage ims = (ExternalIMSMessage) imsJaxb2Marshaller.unmarshal(new StringSource(rawWithJsonAndDatexOk));
        getV2Datex2UpdateService().updateTrafficDatex2ImsMessages(Collections.singletonList(ims));
        entityManagerFlushAndClear(entityManager);
    }

    public void initDataFromFile(final String file) throws IOException {
        final String xmlImsMessage = readResourceContent("classpath:tloik/ims/" + file);
        final ExternalIMSMessage ims = (ExternalIMSMessage) imsJaxb2Marshaller.unmarshal(new StringSource(xmlImsMessage));
        getV2Datex2UpdateService().updateTrafficDatex2ImsMessages(Collections.singletonList(ims));
        entityManagerFlushAndClear(entityManager);
    }

    public static String readImsMessageResourceContent(final ImsXmlVersion xmlVersion) throws IOException {
        return readResourceContent("classpath:tloik/ims/versions/ImsMessage" + xmlVersion + ".xml");
    }

    public static String readImsMessageResourceContent(final ImsXmlVersion xmlVersion, final ImsJsonVersion jsonVersion) throws IOException {
        final String xmlImsMessage = readImsMessageResourceContent(xmlVersion);
        final String jsonImsMessage = readResourceContent("classpath:tloik/ims/Json" + jsonVersion + ".json");
        final String datex2ImsMessage = readResourceContent("classpath:tloik/ims/d2Message.xml");
        // Insert datex2 and message contents
        return xmlImsMessage.replace(D2_MESSAGE_PLACEHOLDER, datex2ImsMessage).replace(JSON_MESSAGE_PLACEHOLDER, jsonImsMessage);
    }

    public void initDataFromStaticImsResourceContent(final ImsXmlVersion xmlVersion, final String situationTypeName,
                                                     final ImsJsonVersion jsonVersion)
        throws IOException {
        initDataFromStaticImsResourceContent(xmlVersion, situationTypeName, jsonVersion, ZonedDateTime.now().minusHours(1), null);
    }

    public void initDataFromStaticImsResourceContent(final ImsXmlVersion xmlVersion, final String situationTypeName,
                                                     final ImsJsonVersion jsonVersion,
                                                     final ZonedDateTime startTime, final ZonedDateTime endTime) throws IOException {
        initDataFromStaticImsResourceContent(xmlVersion, situationTypeName, jsonVersion, startTime, endTime, false);
    }

    public void initDataFromStaticImsResourceContent(final ImsXmlVersion xmlVersion, final String situationTypeName,
                                                     final ImsJsonVersion jsonVersion,
                                                     final ZonedDateTime startTime, final ZonedDateTime endTime,
                                                     final boolean lifeCycleCanceled) throws IOException {
        final String xmlImsMessage = readImsMessageResourceContent(xmlVersion, situationTypeName, jsonVersion, startTime, endTime, lifeCycleCanceled);
        final ExternalIMSMessage ims = (ExternalIMSMessage) imsJaxb2Marshaller.unmarshal(new StringSource(xmlImsMessage));
        getV2Datex2UpdateService().updateTrafficDatex2ImsMessages(Collections.singletonList(ims));
        entityManagerFlushAndClear(entityManager);
    }

    public static String readImsMessageResourceContent(final ImsXmlVersion xmlVersion, final String situationTypeName, final ImsJsonVersion jsonVersion,
                                                       final ZonedDateTime startTime, final ZonedDateTime endTime, final boolean lifeCycleCanceled) throws IOException {
        final String xmlImsMessageTemplate = readImsMessageResourceContent(xmlVersion);
        final String json = readStaticImsJmessageResourceContent(jsonVersion, situationTypeName, startTime, endTime, lifeCycleCanceled);
        final String d2 = readStaticD2MessageResourceContent(situationTypeName, startTime, endTime, jsonVersion.intVersion, lifeCycleCanceled);
        return xmlImsMessageTemplate.replace(D2_MESSAGE_PLACEHOLDER, d2).replace(JSON_MESSAGE_PLACEHOLDER, json);
    }

    public static String readStaticImsJmessageResourceContent(final ImsJsonVersion jsonVersion, final String situationTypeName,
                                                              final ZonedDateTime startTime, final ZonedDateTime endTime,
                                                              final boolean lifeCycleCanceled) throws IOException {
        final String path =
            "classpath:tloik/ims/versions/" +
            getJsonVersionString(jsonVersion) + "/"+
            situationTypeName + ".json";
        return readStaticImsJmessageResourceContent(path, jsonVersion, startTime, endTime, lifeCycleCanceled);
    }

    public static String readStaticImsJmessageResourceContent(final String path, final ImsJsonVersion jsonVersion,
                                                              final ZonedDateTime startTime, final ZonedDateTime endTime, final boolean lifeCycleCanceled) throws IOException {
        log.info("Reading Jmessage resource: {}", path);
        final String content = readResourceContent(path);
        return replaceSimpleJsonPlaceholders(content, jsonVersion, startTime, endTime, lifeCycleCanceled);

    }

    public static String readStaticD2MessageResourceContent(final String situationTypeName, final ZonedDateTime startTime,
                                                            final ZonedDateTime endTime, final int situationVersion,
                                                            final boolean lifeCycleCanceled) throws IOException {
        final String path = "classpath:tloik/ims/versions/d2Message_" + situationTypeName + ".xml";
        log.info("Reading D2Message resource: {}", path);
        final String content = readResourceContent(path);
        return replaceDatex2Placeholders(content, startTime, endTime, situationVersion, lifeCycleCanceled);
    }

    private static String replaceSimpleJsonPlaceholders(final String content, final ImsJsonVersion jsonVersion, final ZonedDateTime startTime,
                                                        final ZonedDateTime endTime, final boolean lifeCycleCanceled) {
        return content.replace(START_DATE_TIME_PLACEHOLDER, startTime.toOffsetDateTime().toString())
            .replace(SITUATION_RELEASE_DATE_TIME_PLACEHOLDER, getVersionTime(startTime, jsonVersion.intVersion).toOffsetDateTime().toString())
            .replace(SITUATION_VERSION_DATE_TIME_PLACEHOLDER, getVersionTime(startTime, jsonVersion.intVersion).toOffsetDateTime().toString())
            .replace(SITUATION_VERSION_PLACEHOLDER, jsonVersion.intVersion + "")
            .replace(END_DATE_TIME_PLACEHOLDER, endTime != null ? endTime.toOffsetDateTime().toString() : "" )
            .replace(lifeCycleCanceled ? "\"timeAndDuration\"" : "RANDOMNOMATCHXYZ", "\"earlyClosing\": \"CANCELED\",\n\"timeAndDuration\"");
    }

    private static String replaceDatex2Placeholders(final String content, final ZonedDateTime startTime, final ZonedDateTime endTime,
                                                    final int situationVersion, final boolean lifeCycleCanceled) {
        return content.replace(SITUATION_RELEASE_DATE_TIME_PLACEHOLDER, getVersionTime(startTime, situationVersion).toOffsetDateTime().toString())
                      .replace(SITUATION_VERSION_DATE_TIME_PLACEHOLDER, getVersionTime(startTime, situationVersion).toOffsetDateTime().toString())
                      .replace(SITUATION_VERSION_PLACEHOLDER, situationVersion + "")
                      .replace(START_DATE_TIME_PLACEHOLDER, startTime.toOffsetDateTime().toString())
                      .replace(endTime != null ? END_DATE_TIME_PLACEHOLDER : "RANDOMNOMATCHXYZ", (endTime != null ? endTime.toOffsetDateTime().toString() : ""))
                      .replace(endTime != null ? "<overallEndTime>" + END_DATE_TIME_PLACEHOLDER + "</overallEndTime>" : "RANDOMNOMATCHXYZ", "")
                      .replace(lifeCycleCanceled ? "<cancel>false</cancel>" : "RANDOMNOMATCHXYZ", "<cancel>true</cancel>");
    }

    /**
     * Converts i.e. V0_2_12 to 0.2.12
     */
    public static String getJsonVersionString(final ImsJsonVersion jsonVersion) {
        return jsonVersion.toString().replace("V", "").replace("_", ".");
    }

    public static ZonedDateTime getVersionTime(final ZonedDateTime situationStartTime, final ImsJsonVersion imsJsonVersion) {
        return getVersionTime(situationStartTime, imsJsonVersion.intVersion);
    }
    public static ZonedDateTime getVersionTime(final ZonedDateTime situationStartTime, final int jsonIntVersion) {
        return situationStartTime.plusMinutes(jsonIntVersion);
    }

}