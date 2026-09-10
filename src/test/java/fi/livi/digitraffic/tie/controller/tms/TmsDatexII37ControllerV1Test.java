package fi.livi.digitraffic.tie.controller.tms;

import static fi.livi.digitraffic.common.util.TimeUtil.getGreatest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.xml.transform.StringSource;

import fi.livi.digitraffic.common.util.StringUtil;
import fi.livi.digitraffic.common.util.TimeUtil;
import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.conf.LastModifiedAppenderControllerAdvice;
import fi.livi.digitraffic.tie.controller.ApiConstants;
import fi.livi.digitraffic.tie.converter.tms.datex2.TmsDatex2Common;
import fi.livi.digitraffic.tie.dao.roadstation.SensorValueRepository;
import fi.livi.digitraffic.tie.dao.tms.TmsSensorConstantValueDtoV1Repository;
import fi.livi.digitraffic.tie.dao.tms.TmsStationRepository;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.model.roadstation.SensorValue;
import fi.livi.digitraffic.tie.model.tms.TmsSensorConstant;
import fi.livi.digitraffic.tie.model.tms.TmsSensorConstantValue;
import fi.livi.digitraffic.tie.model.tms.TmsStation;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.ConfidentialityValueEnum;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.InformationStatusEnum;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.MeasuredDataPublication;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.MeasuredOrDerivedDataTypeEnum;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.MeasurementSiteTablePublication;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.PointLocation;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;

/**
 * Test for {@link TmsDatexII37ControllerV1}
 */
public class TmsDatexII37ControllerV1Test extends AbstractRestWebTest {

    @Autowired
    private TmsSensorConstantValueDtoV1Repository tmsSensorConstantValueDtoV1Repository;

    @Autowired
    private TmsStationRepository tmsStationRepository;

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @Autowired
    private SensorValueRepository sensorValueRepository;

    private TmsStation tmsStation;

    @BeforeEach
    public void initData() {
        final TmsStation tms = TestUtils.generateDummyTmsStation();
        tmsStationRepository.save(tms);

        final List<RoadStationSensor> publishable =
                roadStationSensorService.findAllPublishableRoadStationSensors(RoadStationType.TMS_STATION);

        final RoadStationSensor sensor1 = publishable.stream()
                .filter(s -> s.getNameFi().equals("KESKINOPEUS_60MIN_KIINTEA_SUUNTA1")).findFirst().orElseThrow();

        final RoadStationSensor sensor2 = publishable.stream()
                .filter(s -> s.getNameFi().equals("OHITUKSET_60MIN_KIINTEA_SUUNTA1")).findFirst().orElseThrow();

        roadStationSensorService.updateSensorsOfRoadStation(tms.getRoadStationId(),
                RoadStationType.TMS_STATION,
                publishable.stream().map(RoadStationSensor::getLotjuId).collect(Collectors.toList()));

        final Instant measured = Instant.now().minus(2, ChronoUnit.MINUTES);
        sensorValueRepository.save(new SensorValue(tms.getRoadStation(), sensor1, 10.0, measured, null));
        sensorValueRepository.save(new SensorValue(tms.getRoadStation(), sensor2, 10.0, measured.minus(1,
                ChronoUnit.MINUTES), null));

        // Reload entities/statuses from DB so timestamps used by controller and test are aligned.
        TestUtils.entityManagerFlushAndClear(entityManager);

        this.tmsStation = entityManager.find(TmsStation.class, tms.getId());
    }

    @AfterEach
    public void cleanDb() {
        TestUtils.truncateTmsData(entityManager);
    }

    @Test
    public void tmsStationsDatexII37XmlRestApi() throws Exception {
        insertSensorConstant();

        final String xmlResponse =
                mockMvc.perform(get(TmsControllerV1.API_TMS_V1 + TmsControllerV1.STATIONS + TmsControllerV1.DATEX2_3_7))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();

        checkXmlXsiType(xmlResponse, MeasurementSiteTablePublication.class);

        final MeasurementSiteTablePublication publication = unmarshalXml(xmlResponse, MeasurementSiteTablePublication.class);
        final PointLocation location = (PointLocation) publication.getMeasurementSiteTables().getFirst().getMeasurementSites().getFirst().getMeasurementSiteLocation();

        assertEquals(42, location.getPointByCoordinates().getBearing());
        assertEquals("FI", publication.getPublicationCreator().getCountry());
        assertEquals(TmsDatex2Common.MEASUREMENT_SITE_NATIONAL_IDENTIFIER, publication.getPublicationCreator().getNationalIdentifier());
        assertEquals("fi", publication.getLang());
        assertEquals(ConfidentialityValueEnum.NO_RESTRICTION, publication.getHeaderInformation().getConfidentiality().getValue());
        assertEquals(InformationStatusEnum.REAL, publication.getHeaderInformation().getInformationStatus().getValue());
        assertTrue(publication.getMeasurementSiteTables().getFirst().getMeasurementSites().getFirst().getMeasurementSpecificCharacteristics().stream()
                .allMatch(characteristics -> characteristics.getMeasurementSpecificCharacteristics().getSpecificMeasurementValueType() != null));
        assertTrue(publication.getMeasurementSiteTables().getFirst().getMeasurementSites().getFirst().getMeasurementSpecificCharacteristics().stream()
                .map(characteristics -> characteristics.getMeasurementSpecificCharacteristics().getSpecificMeasurementValueType().getValue())
                .anyMatch(MeasuredOrDerivedDataTypeEnum.TRAFFIC_SPEED::equals));
        assertTrue(publication.getMeasurementSiteTables().getFirst().getMeasurementSites().getFirst().getMeasurementSpecificCharacteristics().stream()
                .map(characteristics -> characteristics.getMeasurementSpecificCharacteristics().getSpecificMeasurementValueType().getValue())
                .anyMatch(MeasuredOrDerivedDataTypeEnum.TRAFFIC_FLOW::equals));
    }

    @Test
    public void tmsStationByIdDatexII37XmlRestApi() throws Exception {
        insertSensorConstant();

        final String xmlResponse =
                mockMvc.perform(get(TmsControllerV1.API_TMS_V1 + TmsControllerV1.STATIONS + "/" + tmsStation.getRoadStationNaturalId() + TmsControllerV1.DATEX2_3_7))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();

        checkXmlXsiType(xmlResponse, MeasurementSiteTablePublication.class);

        final MeasurementSiteTablePublication publication = unmarshalXml(xmlResponse, MeasurementSiteTablePublication.class);
        final PointLocation location = (PointLocation) publication.getMeasurementSiteTables().getFirst().getMeasurementSites().getFirst().getMeasurementSiteLocation();

        assertEquals(42, location.getPointByCoordinates().getBearing());
        assertEquals("FI", publication.getPublicationCreator().getCountry());
        assertEquals(TmsDatex2Common.MEASUREMENT_SITE_NATIONAL_IDENTIFIER, publication.getPublicationCreator().getNationalIdentifier());
        assertEquals("fi", publication.getLang());
        assertEquals(ConfidentialityValueEnum.NO_RESTRICTION, publication.getHeaderInformation().getConfidentiality().getValue());
        assertEquals(InformationStatusEnum.REAL, publication.getHeaderInformation().getInformationStatus().getValue());
        assertTrue(publication.getMeasurementSiteTables().getFirst().getMeasurementSites().getFirst().getMeasurementSpecificCharacteristics().stream()
                .allMatch(characteristics -> characteristics.getMeasurementSpecificCharacteristics().getSpecificMeasurementValueType() != null));
    }

    @Test
    public void tmsDataDatexII37XmlRestApi() throws Exception {
        final String xmlResponse =
                mockMvc.perform(get(TmsControllerV1.API_TMS_V1 + TmsControllerV1.STATIONS + TmsControllerV1.DATA + TmsControllerV1.DATEX2_3_7))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();

        final MeasuredDataPublication publication = unmarshalXml(xmlResponse, MeasuredDataPublication.class);
        assertNotNull(publication);
        assertNotNull(publication.getSiteMeasurements());
    }

    @Test
    public void tmsDataByIdDatexII37XmlRestApi() throws Exception {
        final String xmlResponse =
                mockMvc.perform(get(TmsControllerV1.API_TMS_V1 + TmsControllerV1.STATIONS + "/" + tmsStation.getRoadStationNaturalId() + TmsControllerV1.DATA + TmsControllerV1.DATEX2_3_7))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();

        final MeasuredDataPublication publication = unmarshalXml(xmlResponse, MeasuredDataPublication.class);
        assertNotNull(publication);
        assertNotNull(publication.getSiteMeasurements());
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

        tmsSensorConstantValueDtoV1Repository.save(cv);
    }

    private void checkXmlXsiType(final String xmlResponse, final Class<?> xsiType) {
        assertTrue(Strings.CS.contains(xmlResponse, "<d2:payload"));
        final String typeString = StringUtil.format("xsi:type=\"roa:{}\"", xsiType.getSimpleName());
        assertTrue(Strings.CS.contains(xmlResponse, typeString), StringUtil.format("Xml message didn't contain: {}", typeString));
    }

    private <T> T unmarshalXml(final String xml, final Class<T> clazz) {
        try {
            final JAXBContext jc = JAXBContext.newInstance(clazz);
            final Unmarshaller unmarshaller = jc.createUnmarshaller();
            final JAXBElement<T> result = unmarshaller.unmarshal(new StringSource(StringUtils.trim(xml)), clazz);
            return result.getValue();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
