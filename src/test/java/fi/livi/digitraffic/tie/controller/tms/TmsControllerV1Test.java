

package fi.livi.digitraffic.tie.controller.tms;

import static fi.livi.digitraffic.common.util.TimeUtil.getGreatest;
import static fi.livi.digitraffic.tie.TestUtils.getRandomId;
import static fi.livi.digitraffic.tie.helper.DateHelperTest.ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import fi.livi.digitraffic.tie.dao.tms.TmsSensorConstantValueDtoV1Repository;
import fi.livi.digitraffic.tie.tms.datex2.v3_5.PointLocation;

import fi.livi.digitraffic.tie.model.tms.TmsSensorConstant;
import fi.livi.digitraffic.tie.model.tms.TmsSensorConstantValue;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.xml.transform.StringSource;

import fi.livi.digitraffic.common.util.StringUtil;
import fi.livi.digitraffic.common.util.TimeUtil;
import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.conf.LastModifiedAppenderControllerAdvice;
import fi.livi.digitraffic.tie.controller.ApiConstants;
import fi.livi.digitraffic.tie.controller.DtMediaType;
import fi.livi.digitraffic.tie.converter.tms.datex2.TmsDatex2Common;
import fi.livi.digitraffic.tie.dao.roadstation.SensorValueRepository;
import fi.livi.digitraffic.tie.dao.tms.TmsStationRepository;
import fi.livi.digitraffic.tie.tms.datex2.v3_5.ConfidentialityValueEnum;
import fi.livi.digitraffic.tie.tms.datex2.v3_5.InformationStatusEnum;
import fi.livi.digitraffic.tie.tms.datex2.v3_5.MeasuredDataPublication;
import fi.livi.digitraffic.tie.tms.datex2.v3_5.MeasurementSiteTablePublication;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioVO;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.roadstation.CollectionStatus;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.model.roadstation.SensorValue;
import fi.livi.digitraffic.tie.model.tms.CalculatorDeviceType;
import fi.livi.digitraffic.tie.model.tms.TmsStation;
import fi.livi.digitraffic.tie.model.tms.TmsStationType;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.TmsTestHelper;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;

/**
 * Test for {@link TmsControllerV1}
 */
public class TmsControllerV1Test extends AbstractRestWebTest {
    @Autowired
    private TmsSensorConstantValueDtoV1Repository tmsSensorConstantValueDtoV1Repository;

    @Autowired
    private TmsStationRepository tmsStationRepository;

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @Autowired
    private DataStatusService dataStatusService;

    @Autowired
    private SensorValueRepository sensorValueRepository;

    @Autowired
    private TmsTestHelper tmsTestHelper;

    private TmsStation tmsStation;
    private long metadataLastModifiedMillis;
    private Instant metadataLastModified;
    private long dataLastUpdatedMillis;

    @BeforeEach
    public void initData() {
        final TmsStation tms = TestUtils.generateDummyTmsStation();
        tmsStationRepository.save(tms);

        final List<RoadStationSensor> publishable =
            roadStationSensorService.findAllPublishableRoadStationSensors(RoadStationType.TMS_STATION);

        assertFalse(publishable.isEmpty());

        final RoadStationSensor sensor1 = publishable.stream()
                .filter(s -> s.getNameFi().equals("KESKINOPEUS_60MIN_KIINTEA_SUUNTA1")).findFirst().orElseThrow();

        final RoadStationSensor sensor2 = publishable.stream()
                .filter(s -> s.getNameFi().equals("OHITUKSET_60MIN_KIINTEA_SUUNTA1")).findFirst().orElseThrow();

        roadStationSensorService.updateSensorsOfRoadStation(tms.getRoadStationId(),
            RoadStationType.TMS_STATION,
            publishable.stream().map(RoadStationSensor::getLotjuId).collect(Collectors.toList()));

        dataStatusService.updateDataUpdated(DataType.TMS_STATION_METADATA);
        dataStatusService.updateDataUpdated(DataType.TMS_STATION_METADATA_CHECK);

        dataStatusService.updateDataUpdated(DataType.TMS_STATION_SENSOR_METADATA);
        dataStatusService.updateDataUpdated(DataType.TMS_STATION_SENSOR_METADATA_CHECK);

        final Instant measured = Instant.now().minus(2, ChronoUnit.MINUTES);
        final SensorValue sv1 = new SensorValue(tms.getRoadStation(), sensor1, 10.0, measured, null);
        final SensorValue sv2 = new SensorValue(tms.getRoadStation(), sensor2, 10.0, measured.minus(1,
                ChronoUnit.MINUTES), null);
        sensorValueRepository.save(sv1);
        sensorValueRepository.save(sv2);
        this.dataLastUpdatedMillis =  TimeUtil.roundInstantSeconds(getTransactionTimestampRoundedToSeconds()).toEpochMilli();

        dataStatusService.updateDataUpdated(DataType.getSensorValueUpdatedDataType(RoadStationType.TMS_STATION));

        // Force to reload updated timestamp from db
        TestUtils.entityManagerFlushAndClear(entityManager);

        this.tmsStation = entityManager.find(TmsStation.class, tms.getId());
        final Instant sensorsUpdated = dataStatusService.findDataUpdatedInstant(DataType.TMS_STATION_SENSOR_METADATA);
        final Instant stationsUpdated = dataStatusService.findDataUpdatedInstant(DataType.TMS_STATION_METADATA);
        this.metadataLastModified = getGreatest(sensorsUpdated, stationsUpdated);
        this.metadataLastModifiedMillis = TimeUtil.roundInstantSeconds(metadataLastModified).toEpochMilli();
    }

    private void insertSensorConstant() {
        final TmsSensorConstant tsc = new TmsSensorConstant();
        tsc.setLotjuId(1L);
        tsc.setName("Tien_suunta");
        tsc.setRoadStation(tmsStation.getRoadStation());

        final TmsSensorConstantValue cv = new TmsSensorConstantValue();
        cv.setLotjuId(1L);
        cv.setSensorConstant(tsc);
        cv.setValue(42);
        cv.setValidFrom(101);
        cv.setValidTo(1231);

        final TmsSensorConstantValue cv2 = new TmsSensorConstantValue();
        cv2.setLotjuId(2L);
        cv2.setSensorConstant(tsc);
        cv2.setValue(43);
        cv2.setValidFrom(101);
        cv2.setValidTo(1231);

        tmsSensorConstantValueDtoV1Repository.save(cv);
        tmsSensorConstantValueDtoV1Repository.save(cv2);
    }

    @AfterEach
    public void clenDb() {
        TestUtils.truncateTmsData(entityManager);
    }

    /* METADATA */

    @Test
    public void tmsStationsRestApi() throws Exception {
        mockMvc.perform(get(TmsControllerV1.API_TMS_V1 + TmsControllerV1.STATIONS))
                .andExpect(status().isOk())
                .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.features[0].id", Matchers.is(tmsStation.getRoadStationNaturalId().intValue())))
                .andExpect(jsonPath("$.features[0].type", is("Feature")))
                .andExpect(jsonPath("$.features[0].geometry.type", is("Point")))
                .andExpect(jsonPath("$.features[0].geometry.coordinates", Matchers.hasSize(3)))
                .andExpect(jsonPath("$.features[0].properties", Matchers.anything()))
                .andExpect(jsonPath("$.features[0].properties.id", Matchers.is(tmsStation.getRoadStationNaturalId().intValue())))
                .andExpect(jsonPath("$.features[0].properties.tmsNumber", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.name", Matchers.notNullValue()))
                .andExpect(jsonPath("$.features[0].properties.name", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.collectionStatus",
                    Matchers.oneOf(CollectionStatus.GATHERING.name(), CollectionStatus.REMOVED_PERMANENTLY.name(), CollectionStatus.REMOVED_TEMPORARILY.name())))
                .andExpect(jsonPath("$.features[0].properties.state", Matchers.anything()))
                .andExpect(jsonPath("$.features[0].properties.dataUpdatedTime", is(tmsStation.getRoadStation().getModified().toString())))

                .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER)
                .andExpect(header().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER))
                .andExpect(header().dateValue(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, metadataLastModifiedMillis));
    }

    @Test
    public void tmsStationRestApi() throws Exception {
        final Integer vvapaas1Arvo = getRandomId(80, 100);
        final Integer vvapaas2Arvo = getRandomId(101, 120);

        final LamAnturiVakioVO vakio1 = tmsTestHelper.createAndSaveLamAnturiVakio(tmsStation.getLotjuId(), "VVAPAAS1");
        tmsTestHelper.createAndSaveLamAnturiVakioArvo(vakio1, vvapaas1Arvo);
        final LamAnturiVakioVO vakio2 = tmsTestHelper.createAndSaveLamAnturiVakio(tmsStation.getLotjuId(), "VVAPAAS2");
        tmsTestHelper.createAndSaveLamAnturiVakioArvo(vakio2, vvapaas2Arvo);


        mockMvc.perform(get(TmsControllerV1.API_TMS_V1 + TmsControllerV1.STATIONS + "/" + tmsStation.getRoadStationNaturalId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
                .andExpect(jsonPath("$.type", is("Feature")))
                .andExpect(jsonPath("$.id", Matchers.is(tmsStation.getRoadStationNaturalId().intValue())))
                .andExpect(jsonPath("$.type", is("Feature")))
                .andExpect(jsonPath("$.geometry.type", is("Point")))
                .andExpect(jsonPath("$.geometry.coordinates", Matchers.hasSize(3)))
                .andExpect(jsonPath("$.properties", Matchers.anything()))
                .andExpect(jsonPath("$.properties.id", Matchers.is(tmsStation.getRoadStationNaturalId().intValue())))
                .andExpect(jsonPath("$.properties.tmsNumber", isA(Integer.class)))
                .andExpect(jsonPath("$.properties.name", Matchers.notNullValue()))
                .andExpect(jsonPath("$.properties.name", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.properties.collectionStatus",
                    Matchers.oneOf(CollectionStatus.GATHERING.name(), CollectionStatus.REMOVED_PERMANENTLY.name(), CollectionStatus.REMOVED_TEMPORARILY.name())))
                .andExpect(jsonPath("$.properties.state", Matchers.anything()))
                .andExpect(jsonPath("$.properties.dataUpdatedTime", is(tmsStation.getRoadStation().getModified().toString())))
                .andExpect(jsonPath("$.properties.municipality", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.properties.municipalityCode", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.properties.province", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.properties.provinceCode", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.properties.stationType", Matchers.is(TmsStationType.DSL_6.name())))
                .andExpect(jsonPath("$.properties.roadAddress.roadSection", isA(Integer.class)))
                .andExpect(jsonPath("$.properties.roadAddress.roadNumber", isA(Integer.class)))
                .andExpect(jsonPath("$.properties.names.fi", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.properties.names.sv", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.properties.names.en", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.properties.purpose", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.properties.municipality", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.properties.municipalityCode", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.properties.province", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.properties.provinceCode", Matchers.isA(Integer.class)))

                .andExpect(jsonPath("$.properties.direction1Municipality", is("Vihti")))
                .andExpect(jsonPath("$.properties.direction1MunicipalityCode", is(927)))
                .andExpect(jsonPath("$.properties.direction2Municipality", is("Helsinki")))
                .andExpect(jsonPath("$.properties.direction2MunicipalityCode", is(91)))
                .andExpect(jsonPath("$.properties.calculatorDeviceType", Matchers.is(CalculatorDeviceType.DSL_5.name())))
                .andExpect(jsonPath("$.properties.freeFlowSpeed1", is(vvapaas1Arvo.doubleValue())))
                .andExpect(jsonPath("$.properties.freeFlowSpeed2", is(vvapaas2Arvo.doubleValue())))

                .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER)
                .andExpect(header().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER))
                .andExpect(header().dateValue(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, metadataLastModifiedMillis));
    }

    @Test
    public void tmsSensorsRestApi() throws Exception {
        mockMvc.perform(get(TmsControllerV1.API_TMS_V1 + TmsControllerV1.SENSORS))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
            .andExpect(jsonPath("$.sensors[0].id", isA(Integer.class)))
            .andExpect(jsonPath("$.sensors[0].name", isA(String.class)))
            .andExpect(jsonPath("$.sensors[0].shortName", isA(String.class)))
            .andExpect(jsonPath("$.sensors[0].unit", isA(String.class)))
            .andExpect(jsonPath("$.sensors[0].description", isA(String.class)))
            .andExpect(jsonPath("$.sensors[0].descriptions.fi", isA(String.class)))
            .andExpect(jsonPath("$.sensors[0].direction", isA(String.class)))
            .andExpect(jsonPath("$.sensors[0].accuracy").hasJsonPath())
            .andExpect(jsonPath("$.sensors[0].sensorValueDescriptions").hasJsonPath())
            .andExpect(jsonPath("$.sensors[0].presentationNames").hasJsonPath())
            .andExpect(jsonPath("$.sensors[0].presentationNames.fi").hasJsonPath())

            .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER)
            .andExpect(header().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER))
            .andExpect(header().dateValue(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, metadataLastModifiedMillis));
    }

    /* DATA */

    @Test
    public void tmsDataRestApi() throws Exception {
        final ResultActions tmp =
            mockMvc.perform(get(TmsControllerV1.API_TMS_V1 + TmsControllerV1.STATIONS + TmsControllerV1.DATA));
        // log.info(tmp.andReturn().getResponse().getContentAsString());
        tmp
            .andExpect(status().isOk())
            .andExpect(content().contentType(DtMediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.dataUpdatedTime", Matchers.notNullValue()))
            .andExpect(jsonPath("$.stations", Matchers.notNullValue()))
            .andExpect(jsonPath("$.stations[0].id", Matchers.is(tmsStation.getRoadStationNaturalId().intValue())))
            .andExpect(jsonPath("$.stations[0].tmsNumber", isA(Integer.class)))
            .andExpect(jsonPath("$.stations[0].dataUpdatedTime", Matchers.notNullValue()))
            .andExpect(jsonPath("$.stations[0].sensorValues", Matchers.notNullValue()))
            .andExpect(jsonPath("$.stations[0].sensorValues[0].id", isA(Integer.class)))
            .andExpect(jsonPath("$.stations[0].sensorValues[0].stationId", Matchers.is(tmsStation.getRoadStationNaturalId().intValue())))
            .andExpect(jsonPath("$.stations[0].sensorValues[0].name", isA(String.class)))
            .andExpect(jsonPath("$.stations[0].sensorValues[0].shortName", isA(String.class)))
            .andExpect(jsonPath("$.stations[0].sensorValues[0].value", isA(Number.class)))
            .andExpect(jsonPath("$.stations[0].sensorValues[0].unit", isA(String.class)))
            .andExpect(jsonPath("$.stations[0].sensorValues[0].measuredTime", isA(String.class)))
            // todo test measured
            .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER)
            .andExpect(header().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER))
            .andExpect(header().dateValue(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, dataLastUpdatedMillis));
    }

    @Test
    public void tmsDataByIdRestApi() throws Exception {
        mockMvc.perform(get(TmsControllerV1.API_TMS_V1 + TmsControllerV1.STATIONS + "/" + tmsStation.getRoadStationNaturalId() + "/" + TmsControllerV1.DATA))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DtMediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", Matchers.is(tmsStation.getRoadStationNaturalId().intValue())))
            .andExpect(jsonPath("$.tmsNumber", isA(Integer.class)))
            .andExpect(jsonPath("$.dataUpdatedTime", Matchers.notNullValue()))
            .andExpect(jsonPath("$.sensorValues", Matchers.notNullValue()))
            .andExpect(jsonPath("$.sensorValues[0].id", isA(Integer.class)))
            .andExpect(jsonPath("$.sensorValues[0].stationId", Matchers.is(tmsStation.getRoadStationNaturalId().intValue())))
            .andExpect(jsonPath("$.sensorValues[0].name", isA(String.class)))
            .andExpect(jsonPath("$.sensorValues[0].shortName", isA(String.class)))
            .andExpect(jsonPath("$.sensorValues[0].value", isA(Number.class)))
            .andExpect(jsonPath("$.sensorValues[0].unit", isA(String.class)))
            .andExpect(jsonPath("$.sensorValues[0].measuredTime", isA(String.class)))
            // todo test measured
            .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER)
            .andExpect(header().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER))
            .andExpect(header().dateValue(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, dataLastUpdatedMillis));
    }

    @Test
    public void tmsSensorConstantsRestApi() throws Exception {
        final String vakioNimi = "VVAPAAS1";
        final Integer vakioArvo = getRandomId(0, 20);

        final LamAnturiVakioVO vakio = tmsTestHelper.createAndSaveLamAnturiVakio(tmsStation.getLotjuId(), vakioNimi);
        tmsTestHelper.createAndSaveLamAnturiVakioArvo(vakio, vakioArvo);
        dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_VALUE_DATA);
        final long constantsUpdated = TimeUtil.roundInstantSeconds(
            dataStatusService.findDataUpdatedInstant(DataType.TMS_SENSOR_CONSTANT_VALUE_DATA)).toEpochMilli();
        mockMvc.perform(get(TmsControllerV1.API_TMS_V1 + TmsControllerV1.STATIONS + TmsControllerV1.SENSOR_CONSTANTS))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DtMediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.stations[0].id", Matchers.is(tmsStation.getRoadStationNaturalId().intValue())))
            .andExpect(jsonPath("$.dataUpdatedTime", Matchers.notNullValue()))
            .andExpect(jsonPath("$.stations[0].sensorConstantValues", Matchers.notNullValue()))
            .andExpect(jsonPath("$.stations[0].sensorConstantValues[0].name", is(vakioNimi)))
            .andExpect(jsonPath("$.stations[0].sensorConstantValues[0].value", is(vakioArvo)))
            .andExpect(jsonPath("$.stations[0].sensorConstantValues[0].validFrom", is("01-01")))
            .andExpect(jsonPath("$.stations[0].sensorConstantValues[0].validTo", is("12-31")))
            .andExpect(jsonPath("$.stations[0].sensorConstantValues[0]..dataUpdatedTime", Matchers.notNullValue()))

            .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER)
            .andExpect(header().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER))
            .andExpect(header().dateValue(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, constantsUpdated));
    }

    @Test
    public void tmsSensorConstantsByStationIdRestApi() throws Exception {
        final String vakioNimi = "VVAPAAS1";
        final Integer vakioArvo = getRandomId(0, 20);

        final LamAnturiVakioVO vakio = tmsTestHelper.createAndSaveLamAnturiVakio(tmsStation.getLotjuId(), vakioNimi);
        tmsTestHelper.createAndSaveLamAnturiVakioArvo(vakio, vakioArvo);
        dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_VALUE_DATA);
        final long constantsUpdated = TimeUtil.roundInstantSeconds(
            dataStatusService.findDataUpdatedInstant(DataType.TMS_SENSOR_CONSTANT_VALUE_DATA)).toEpochMilli();

        // log.info(mockMvc.perform(get(TmsControllerV1.API_TMS_V1 + TmsControllerV1.STATIONS + "/" + tmsStation.getRoadStationNaturalId() +  "/" + TmsControllerV1.SENSOR_CONSTANTS))
        // .andReturn().getResponse().getContentAsString());
        mockMvc.perform(get(TmsControllerV1.API_TMS_V1 + TmsControllerV1.STATIONS + "/" + tmsStation.getRoadStationNaturalId() +  "/" + TmsControllerV1.SENSOR_CONSTANTS))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DtMediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", Matchers.is(tmsStation.getRoadStationNaturalId().intValue())))
            .andExpect(jsonPath("$.dataUpdatedTime", Matchers.notNullValue()))
            .andExpect(jsonPath("$.sensorConstantValues", Matchers.notNullValue()))
            .andExpect(jsonPath("$.sensorConstantValues[0].name", is(vakioNimi)))
            .andExpect(jsonPath("$.sensorConstantValues[0].value", is(vakioArvo)))
            .andExpect(jsonPath("$.sensorConstantValues[0].validFrom", is("01-01")))
            .andExpect(jsonPath("$.sensorConstantValues[0].validTo", is("12-31")))

            .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER)
            .andExpect(header().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER))
            .andExpect(header().dateValue(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, constantsUpdated));
    }

    @Test
    public void tmsStationsDatex2XmlRestApi() throws Exception {
        insertSensorConstant();

        final String xmlResponse =
                mockMvc.perform(get(TmsControllerV1.API_TMS_V1 + TmsControllerV1.STATIONS + TmsControllerV1.DATEX2 + ApiConstants.XML))
                        .andReturn().getResponse().getContentAsString();

        checkXmlXsiType(xmlResponse, MeasurementSiteTablePublication.class);

        final MeasurementSiteTablePublication publication = unmarshalXml(xmlResponse, MeasurementSiteTablePublication.class);

        final PointLocation location = (PointLocation) publication.getMeasurementSiteTables().getFirst().getMeasurementSites().getFirst().getMeasurementSiteLocation();
        assertEquals(42, location.getPointByCoordinates().getBearing());

        assertEquals(metadataLastModified, publication.getPublicationTime());
        assertEquals("FI", publication.getPublicationCreator().getCountry());
        assertEquals(TmsDatex2Common.MEASUREMENT_SITE_NATIONAL_IDENTIFIER, publication.getPublicationCreator().getNationalIdentifier());
        assertEquals("fi", publication.getLang());
        assertEquals(ConfidentialityValueEnum.NO_RESTRICTION, publication.getHeaderInformation().getConfidentiality().getValue());
        assertEquals(InformationStatusEnum.REAL, publication.getHeaderInformation().getInformationStatus().getValue());
    }

    @Test
    public void tmsStationsByIdDatex2XmlRestApi() throws Exception {
        insertSensorConstant();

        final String xmlResponse =
                mockMvc.perform(get(TmsControllerV1.API_TMS_V1 + TmsControllerV1.STATIONS + "/" + tmsStation.getRoadStationNaturalId() + TmsControllerV1.DATEX2 + ApiConstants.XML))
                        .andReturn().getResponse().getContentAsString();

        checkXmlXsiType(xmlResponse, MeasurementSiteTablePublication.class);

        final MeasurementSiteTablePublication publication = unmarshalXml(xmlResponse, MeasurementSiteTablePublication.class);

        final PointLocation location = (PointLocation) publication.getMeasurementSiteTables().getFirst().getMeasurementSites().getFirst().getMeasurementSiteLocation();
        assertEquals(42, location.getPointByCoordinates().getBearing());

        assertEquals(metadataLastModified, publication.getPublicationTime());
        assertEquals("FI", publication.getPublicationCreator().getCountry());
        assertEquals(TmsDatex2Common.MEASUREMENT_SITE_NATIONAL_IDENTIFIER, publication.getPublicationCreator().getNationalIdentifier());
        assertEquals("fi", publication.getLang());
        assertEquals(ConfidentialityValueEnum.NO_RESTRICTION, publication.getHeaderInformation().getConfidentiality().getValue());
        assertEquals(InformationStatusEnum.REAL, publication.getHeaderInformation().getInformationStatus().getValue());
    }

    @Test
    public void tmsDataDatex2RestApi() throws Exception {

        final String xmlResponse =
                mockMvc.perform(get(TmsControllerV1.API_TMS_V1 + TmsControllerV1.STATIONS + TmsControllerV1.DATA + TmsControllerV1.DATEX2 + ApiConstants.XML))
                        .andReturn().getResponse().getContentAsString();

        checkXmlXsiType(xmlResponse, MeasuredDataPublication.class);

        final MeasuredDataPublication publication = unmarshalXml(xmlResponse, MeasuredDataPublication.class);

        assertEquals(metadataLastModified, publication.getPublicationTime());
        assertEquals("FI", publication.getPublicationCreator().getCountry());
        assertEquals(TmsDatex2Common.MEASUREMENT_SITE_NATIONAL_IDENTIFIER, publication.getPublicationCreator().getNationalIdentifier());
        assertEquals("fi", publication.getLang());
        assertEquals(ConfidentialityValueEnum.NO_RESTRICTION, publication.getHeaderInformation().getConfidentiality().getValue());
        assertEquals(InformationStatusEnum.REAL, publication.getHeaderInformation().getInformationStatus().getValue());
    }

    @Test
    public void tmsDataByIdDatex2RestApi() throws Exception {

        final String xmlResponse =
                mockMvc.perform(get(TmsControllerV1.API_TMS_V1 + TmsControllerV1.STATIONS + "/" + tmsStation.getRoadStationNaturalId() + TmsControllerV1.DATA + TmsControllerV1.DATEX2 + ApiConstants.XML))
                        .andReturn().getResponse().getContentAsString();

        checkXmlXsiType(xmlResponse, MeasuredDataPublication.class);

        final MeasuredDataPublication publication = unmarshalXml(xmlResponse, MeasuredDataPublication.class);

        assertEquals(metadataLastModified, publication.getPublicationTime());
        assertEquals("FI", publication.getPublicationCreator().getCountry());
        assertEquals(TmsDatex2Common.MEASUREMENT_SITE_NATIONAL_IDENTIFIER, publication.getPublicationCreator().getNationalIdentifier());
        assertEquals("fi", publication.getLang());
        assertEquals(ConfidentialityValueEnum.NO_RESTRICTION, publication.getHeaderInformation().getConfidentiality().getValue());
        assertEquals(InformationStatusEnum.REAL, publication.getHeaderInformation().getInformationStatus().getValue());
    }

    private void checkXmlXsiType(final String xmlResponse, final Class<?> xsiType) {
        assertTrue(StringUtils.contains(xmlResponse, "<d2:payload"));
        final String typeString = StringUtil.format("xsi:type=\"roa:{}\"", xsiType.getSimpleName());
        assertTrue(StringUtils.contains(xmlResponse, typeString), StringUtil.format("Xml message didn't contain: {}"));
    }

    private <T> T unmarshalXml(final String xml, final Class<T> clazz) {
        try {

            final JAXBContext jc = JAXBContext.newInstance(clazz);
            final Unmarshaller unmarshaller = jc.createUnmarshaller();
            final JAXBElement<T> result =
                    unmarshaller.unmarshal(new StringSource(StringUtils.trim(xml)), clazz);
            return result.getValue();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
