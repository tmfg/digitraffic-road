package fi.livi.digitraffic.tie.controller.weathercam;

import static fi.livi.digitraffic.tie.TestUtils.generateDummyPreset;
import static fi.livi.digitraffic.tie.helper.DateHelperTest.ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.conf.LastModifiedAppenderControllerAdvice;
import fi.livi.digitraffic.tie.controller.DtMediaType;
import fi.livi.digitraffic.tie.dto.roadstation.v1.StationRoadAddressV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamPresetDirectionV1;
import fi.livi.digitraffic.common.util.TimeUtil;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.roadstation.RoadStation;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationState;
import fi.livi.digitraffic.tie.model.weathercam.CameraPreset;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.weathercam.CameraPresetService;

/** Test for {@link WeathercamControllerV1} */
public class WeathercamControllerV1Test extends AbstractRestWebTest {

    @Autowired
    private CameraPresetService cameraPresetService;

    @Autowired
    private DataStatusService dataStatusService;

    private final Instant imageUpdateTime1 = Instant.now().with(ChronoField.MILLI_OF_SECOND, 0);
    private final Instant imageUpdateTimeDb1 = imageUpdateTime1.plusSeconds(15);
    private final Instant imageUpdateTime2 = imageUpdateTime1.minusSeconds(1);
    private final Instant imageUpdateTimeDb2 = imageUpdateTime2.plusSeconds(10);
    private final Instant metadataCheckedTime = imageUpdateTime1.minusSeconds(60);
    private final Instant metadataUpdateTime = imageUpdateTime1.minusSeconds(120);

    private CameraPreset preset1;
    private CameraPreset preset2;
    private Instant stationModified;

    @BeforeEach
    public void initData() {
        final CameraPreset p1 = generateDummyPreset();
        final CameraPreset p2 = generateDummyPreset();
        p1.setPresetId(p1.getPresetId().substring(0,6) + "01");
        p2.setRoadStation(p1.getRoadStation());
        p2.setCameraId(p1.getCameraId());
        p2.setPresetId(p1.getCameraId() + "09");
        p2.setDirection("9");
        p1.setPresetId(p1.getCameraId() + "01");
        p1.setDirection("1");
        p1.setPictureLastModified(TimeUtil.toZonedDateTimeAtUtc(imageUpdateTime1));
        p1.setPictureLastModifiedDb(TimeUtil.toZonedDateTimeAtUtc(imageUpdateTimeDb1));
        p2.setPictureLastModified(TimeUtil.toZonedDateTimeAtUtc(imageUpdateTime2));
        p2.setPictureLastModifiedDb(TimeUtil.toZonedDateTimeAtUtc(imageUpdateTimeDb2));

        preset1 = cameraPresetService.save(p1);
        preset2 = cameraPresetService.save(p2);

        // Persist to db and clear context to force saved data re-read from db
        entityManager.flush();
        entityManager.clear();

        // Reload to get modified fields from db
        preset1 = entityManager.find(CameraPreset.class, preset1.getId());
        preset2 = entityManager.find(CameraPreset.class, preset2.getId());

        stationModified = TimeUtil.getGreatest(preset1.getRoadStation().getModified(),
                                               TimeUtil.getGreatest(preset1.getModified(), preset2.getModified()));

        dataStatusService.updateDataUpdated(DataType.CAMERA_STATION_METADATA, metadataUpdateTime);
        dataStatusService.updateDataUpdated(DataType.CAMERA_STATION_METADATA_CHECK, metadataCheckedTime);
        dataStatusService.updateDataUpdated(DataType.CAMERA_STATION_IMAGE_UPDATED, imageUpdateTime1);
    }

    @Test
    public void weathercamStationsRestApi() throws Exception {

        mockMvc.perform(get(WeathercamControllerV1.API_WEATHERCAM_V1_STATIONS))
                .andExpect(status().isOk())
                .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.dataUpdatedTime", is(stationModified.toString())))

                .andExpect(jsonPath("$.features[0].type", is("Feature")))
                .andExpect(jsonPath("$.features[0].id", isA(String.class)))
                .andExpect(jsonPath("$.features[0].id", isA(String.class)))
                .andExpect(jsonPath("$.features[0].id", hasLength(6)))
                .andExpect(jsonPath("$.features[0].geometry.type", is("Point")))
                .andExpect(jsonPath("$.features[0].geometry.coordinates", hasSize(3)))
                .andExpect(jsonPath("$.features[0].properties.id", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.id", startsWith("C")))
                .andExpect(jsonPath("$.features[0].properties.id", hasLength(6)))
                .andExpect(jsonPath("$.features[0].properties.name", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.collectionStatus", is(in(new String[] {"GATHERING", "REMOVED_TEMPORARILY"}))))
                .andExpect(jsonPath("$.features[0].properties.state", is(RoadStationState.OK.name())))
                .andExpect(jsonPath("$.features[0].properties.dataUpdatedTime", is(stationModified.toString())))
                .andExpect(jsonPath("$.features[0].properties.presets", hasSize(2)))

                .andExpect(jsonPath("$.features[0].properties.presets[0].id", is(preset1.getPresetId())))
                .andExpect(jsonPath("$.features[0].properties.presets[0].inCollection", is(preset1.isInCollection())))

                .andExpect(jsonPath("$.features[0].properties.presets[1].id", is(preset2.getPresetId())))
                .andExpect(jsonPath("$.features[0].properties.presets[1].inCollection", is(preset2.isInCollection())))

                .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER)
                .andExpect(header().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER))
                .andExpect(header().dateValue(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, stationModified.toEpochMilli()));

    }

    @Test
    public void weathercamStationRestApi() throws Exception {
        final String cameraId = preset1.getCameraId();
        final RoadStation cameraStation = preset1.getRoadStation();

        mockMvc.perform(get(WeathercamControllerV1.API_WEATHERCAM_V1_STATIONS + "/" + cameraId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
                .andExpect(jsonPath("$.type", is("Feature")))
                .andExpect(jsonPath("$.id", is(cameraId)))
                .andExpect(jsonPath("$.geometry.type", is("Point")))
                .andExpect(jsonPath("$.geometry.coordinates", hasSize(3)))
                .andExpect(jsonPath("$.properties.id", is(cameraId)))
                .andExpect(jsonPath("$.properties.name", is(cameraStation.getName())))
                .andExpect(jsonPath("$.properties.nearestWeatherStationId").hasJsonPath())

                .andExpect(jsonPath("$.properties.collectionStatus", is(cameraStation.getCollectionStatus().name())))
                .andExpect(jsonPath("$.properties.state", is(RoadStationState.OK.name())))
                .andExpect(jsonPath("$.properties.dataUpdatedTime", is(stationModified.toString())))
                .andExpect(jsonPath("$.properties.collectionInterval", isA(Integer.class)))
                .andExpect(jsonPath("$.properties.names", aMapWithSize(3)))
                .andExpect(jsonPath("$.properties.roadAddress").exists())
                .andExpect(jsonPath("$.properties.roadAddress.roadNumber", is(cameraStation.getRoadAddress().getRoadNumber())))
                .andExpect(jsonPath("$.properties.roadAddress.roadSection", is(cameraStation.getRoadAddress().getRoadSection())))
                .andExpect(jsonPath("$.properties.roadAddress.distanceFromRoadSectionStart", is(cameraStation.getRoadAddress().getDistanceFromRoadSectionStart())))
                .andExpect(jsonPath("$.properties.roadAddress.carriageway", is(StationRoadAddressV1.RoadAddressCarriageway.getByCode(cameraStation.getRoadAddress().getCarriagewayCode()).name())))
                .andExpect(jsonPath("$.properties.roadAddress.side", is(cameraStation.getRoadAddress().getSide().name())))
                .andExpect(jsonPath("$.properties.roadAddress.contractArea", is(cameraStation.getRoadAddress().getContractArea())))
                .andExpect(jsonPath("$.properties.roadAddress.contractAreaCode", is(cameraStation.getRoadAddress().getContractAreaCode())))
                .andExpect(jsonPath("$.properties.liviId", is(cameraStation.getLiviId())))
                .andExpect(jsonPath("$.properties.country", is(cameraStation.getCountry())))
                .andExpect(jsonPath("$.properties.startTime", is(getIsoDateWithoutMillis(cameraStation.getStartDate()))))
                .andExpect(jsonPath("$.properties.repairMaintenanceTime", is(getIsoDateWithoutMillis(cameraStation.getRepairMaintenanceDate()))))
                .andExpect(jsonPath("$.properties.annualMaintenanceTime", is(getIsoDateWithoutMillis(cameraStation.getAnnualMaintenanceDate()))))
                .andExpect(jsonPath("$.properties.purpose", is(cameraStation.getPurpose())))
                .andExpect(jsonPath("$.properties.municipality", is(cameraStation.getMunicipality())))
                .andExpect(jsonPath("$.properties.municipalityCode", is(Integer.parseInt(cameraStation.getMunicipalityCode()))))
                .andExpect(jsonPath("$.properties.province", is(cameraStation.getProvince())))
                .andExpect(jsonPath("$.properties.provinceCode", is(Integer.parseInt(cameraStation.getProvinceCode()))))
                .andExpect(jsonPath("$.properties.presets", hasSize(2)))

                .andExpect(jsonPath("$.properties.presets[0].id", is(preset1.getPresetId())))
                .andExpect(jsonPath("$.properties.presets[0].presentationName", is(preset1.getPresetName1())))
                .andExpect(jsonPath("$.properties.presets[0].inCollection", is(preset1.isInCollection())))
                .andExpect(jsonPath("$.properties.presets[0].resolution", is(preset1.getResolution())))
                .andExpect(jsonPath("$.properties.presets[0].directionCode", is(preset1.getDirection())))
                .andExpect(jsonPath("$.properties.presets[0].direction", is(WeathercamPresetDirectionV1.INCREASING_DIRECTION.name())))
                .andExpect(jsonPath("$.properties.presets[0].imageUrl", isA(String.class)))

                .andExpect(jsonPath("$.properties.presets[1].id", is(preset2.getPresetId())))
                .andExpect(jsonPath("$.properties.presets[1].presentationName", is(preset2.getPresetName1())))
                .andExpect(jsonPath("$.properties.presets[1].inCollection", is(preset2.isInCollection())))
                .andExpect(jsonPath("$.properties.presets[1].resolution", is(preset2.getResolution())))
                .andExpect(jsonPath("$.properties.presets[1].directionCode", is(preset2.getDirection())))
                .andExpect(jsonPath("$.properties.presets[1].direction", is(WeathercamPresetDirectionV1.SPECIAL_DIRECTION.name())))
                .andExpect(jsonPath("$.properties.presets[1].imageUrl", isA(String.class)))

                .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER)
                .andExpect(header().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER))
                .andExpect(header().dateValue(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, stationModified.toEpochMilli()));
    }

    @Test
    public void testCameraDataRestApi() throws Exception {

        mockMvc.perform(get(WeathercamControllerV1.API_WEATHERCAM_V1_STATIONS + WeathercamControllerV1.DATA))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DtMediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.dataUpdatedTime", Matchers.equalTo(imageUpdateTime1.toString())))
            .andExpect(jsonPath("$.stations", hasSize(1)))
            .andExpect(jsonPath("$.stations[0].id", is(preset1.getCameraId())))

            .andExpect(jsonPath("$.stations[0].presets", hasSize(2)))
            .andExpect(jsonPath("$.stations[0].presets[0].id", is(preset1.getPresetId())))
            .andExpect(jsonPath("$.stations[0].presets[0].measuredTime", is(imageUpdateTime1.toString())))

            .andExpect(jsonPath("$.stations[0].presets[1].id", is(preset2.getPresetId())))
            .andExpect(jsonPath("$.stations[0].presets[1].measuredTime", is(imageUpdateTime2.toString())))

            .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER)
            .andExpect(header().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER))
            .andExpect(header().dateValue(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, imageUpdateTime1.toEpochMilli()));
    }

    @Test
    public void testCameraDataRestApiById() throws Exception {

        mockMvc.perform(get(WeathercamControllerV1.API_WEATHERCAM_V1_STATIONS + "/" + preset1.getCameraId() + WeathercamControllerV1.DATA))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DtMediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id", is(preset1.getCameraId())))

            .andExpect(jsonPath("$.presets", hasSize(2)))
            .andExpect(jsonPath("$.presets[0].id", is(preset1.getPresetId())))
            .andExpect(jsonPath("$.presets[0].measuredTime", is(imageUpdateTime1.toString())))

            .andExpect(jsonPath("$.presets[1].id", is(preset2.getPresetId())))
            .andExpect(jsonPath("$.presets[1].measuredTime", is(imageUpdateTime2.toString())))

            .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER)
            .andExpect(header().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER))
            .andExpect(header().dateValue(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, imageUpdateTimeDb1.toEpochMilli()));
    }

    private static String getIsoDateWithoutMillis(final ZonedDateTime time) {
        final Instant i = TimeUtil.toInstant(time);
        if (i == null) {
            return null;
        } else if (i.getNano() >= 500_000_000) {
            return i.truncatedTo(ChronoUnit.SECONDS).plusSeconds(1).toString();
        }
        return i.truncatedTo(ChronoUnit.SECONDS).toString();
    }


}
