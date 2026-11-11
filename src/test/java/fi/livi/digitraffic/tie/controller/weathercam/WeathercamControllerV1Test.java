package fi.livi.digitraffic.tie.controller.weathercam;

import static fi.livi.digitraffic.common.util.TimeUtil.roundInstantSeconds;
import static fi.livi.digitraffic.tie.TestUtils.generateDummyPreset;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.startsWith;

import java.time.Instant;
import java.time.temporal.ChronoField;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.common.util.TimeUtil;
import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.conf.LastModifiedAppenderControllerAdvice;
import fi.livi.digitraffic.tie.controller.DtMediaType;
import fi.livi.digitraffic.tie.dto.roadstation.v1.StationRoadAddressV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamPresetDirectionV1;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.roadstation.RoadStation;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationState;
import fi.livi.digitraffic.tie.model.weathercam.CameraPreset;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.weathercam.CameraPresetService;

/**
 * Test for {@link WeathercamControllerV1}
 */
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
        final CameraPreset p2 = generateDummyPreset(p1.getRoadStation());
        p1.setPresetId(p1.getPresetId().substring(0, 6) + "01");
        p2.setCameraId(p1.getCameraId());
        p2.setPresetId(p1.getCameraId() + "09");
        p2.setDirection("9");
        p1.setPresetId(p1.getCameraId() + "01");
        p1.setDirection("1");
        p1.setPictureLastModified(imageUpdateTime1);
        p1.setPictureLastModifiedDb(imageUpdateTimeDb1);
        p2.setPictureLastModified(imageUpdateTime2);
        p2.setPictureLastModifiedDb(imageUpdateTimeDb2);

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

        executeGet(WeathercamControllerV1.API_WEATHERCAM_V1_STATIONS)
                .expectStatus().isOk()
                .expectHeader().contentType(DT_JSON_CONTENT_TYPE)
                .expectHeader().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER)
                .expectHeader().valueEqualsDate(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER,
                        stationModified.toEpochMilli())
                .expectBody()
                .jsonPath("$.type").value(is("FeatureCollection"))
                .jsonPath("$.dataUpdatedTime").value(is(stationModified.toString()))

                .jsonPath("$.features[0].type").value(is("Feature"))
                .jsonPath("$.features[0].id").value(isA(String.class))
                .jsonPath("$.features[0].id").value(isA(String.class))
                .jsonPath("$.features[0].id").value(hasLength(6))
                .jsonPath("$.features[0].geometry.type").value(is("Point"))
                .jsonPath("$.features[0].geometry.coordinates").value(hasSize(3))
                .jsonPath("$.features[0].properties.id").value(isA(String.class))
                .jsonPath("$.features[0].properties.id").value(startsWith("C"))
                .jsonPath("$.features[0].properties.id").value(hasLength(6))
                .jsonPath("$.features[0].properties.name").value(isA(String.class))
                .jsonPath("$.features[0].properties.collectionStatus").value(is(in(new String[] { "GATHERING", "REMOVED_TEMPORARILY" })))
                .jsonPath("$.features[0].properties.state").value(is(RoadStationState.OK.name()))
                .jsonPath("$.features[0].properties.dataUpdatedTime").value(is(stationModified.toString()))
                .jsonPath("$.features[0].properties.presets").value(hasSize(2))

                .jsonPath("$.features[0].properties.presets[0].id").value(is(preset1.getPresetId()))
                .jsonPath("$.features[0].properties.presets[0].inCollection").value(is(preset1.isInCollection()))

                .jsonPath("$.features[0].properties.presets[1].id").value(is(preset2.getPresetId()))
                .jsonPath("$.features[0].properties.presets[1].inCollection").value(is(preset2.isInCollection()))

                .consumeWith(this::assertBodyTimesFormatMatchesIsoDateTimeWithZ);

    }

    @Test
    public void weathercamStationRestApi() throws Exception {
        final String cameraId = preset1.getCameraId();
        final RoadStation cameraStation = preset1.getRoadStation();

        executeGet(WeathercamControllerV1.API_WEATHERCAM_V1_STATIONS + "/" + cameraId)
                .expectStatus().isOk()
                .expectHeader().contentType(DT_JSON_CONTENT_TYPE)
                .expectHeader().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER)
                .expectHeader().valueEqualsDate(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER,
                        stationModified.toEpochMilli())
                .expectBody()
                .jsonPath("$.type").value(is("Feature"))
                .jsonPath("$.id").value(is(cameraId))
                .jsonPath("$.geometry.type").value(is("Point"))
                .jsonPath("$.geometry.coordinates").value(hasSize(3))
                .jsonPath("$.properties.id").value(is(cameraId))
                .jsonPath("$.properties.name").value(is(cameraStation.getName()))
                .jsonPath("$.properties.nearestWeatherStationId").hasJsonPath()

                .jsonPath("$.properties.collectionStatus").value(is(cameraStation.getCollectionStatus().name()))
                .jsonPath("$.properties.state").value(is(RoadStationState.OK.name()))
                .jsonPath("$.properties.dataUpdatedTime").value(is(stationModified.toString()))
                .jsonPath("$.properties.collectionInterval").value(isA(Integer.class))
                .jsonPath("$.properties.names").value(aMapWithSize(3))
                .jsonPath("$.properties.roadAddress").exists()
                .jsonPath("$.properties.roadAddress.roadNumber").value(
                        is(cameraStation.getRoadAddress().getRoadNumber()))
                .jsonPath("$.properties.roadAddress.roadSection").value(
                        is(cameraStation.getRoadAddress().getRoadSection()))
                .jsonPath("$.properties.roadAddress.distanceFromRoadSectionStart").value(
                        is(cameraStation.getRoadAddress().getDistanceFromRoadSectionStart()))
                .jsonPath("$.properties.roadAddress.carriageway").value(
                        is(StationRoadAddressV1.RoadAddressCarriageway.getByCode(
                                cameraStation.getRoadAddress().getCarriagewayCode()).name()))
                .jsonPath("$.properties.roadAddress.side").value(is(cameraStation.getRoadAddress().getSide().name()))
                .jsonPath("$.properties.roadAddress.contractArea").value(
                        is(cameraStation.getRoadAddress().getContractArea()))
                .jsonPath("$.properties.roadAddress.contractAreaCode").value(
                        is(cameraStation.getRoadAddress().getContractAreaCode()))
                .jsonPath("$.properties.liviId").value(is(cameraStation.getLiviId()))
                .jsonPath("$.properties.country").value(is(cameraStation.getCountry()))
                .jsonPath("$.properties.startTime").value(
                        is(roundInstantSeconds(cameraStation.getStartDate()).toString()))
                .jsonPath("$.properties.repairMaintenanceTime").value(
                        is(roundInstantSeconds(cameraStation.getRepairMaintenanceDate()).toString()))
                .jsonPath("$.properties.annualMaintenanceTime").value(
                        is(roundInstantSeconds(cameraStation.getAnnualMaintenanceDate()).toString()))
                .jsonPath("$.properties.purpose").value(is(cameraStation.getPurpose()))
                .jsonPath("$.properties.municipality").value(is(cameraStation.getMunicipality()))
                .jsonPath("$.properties.municipalityCode").value(
                        is(Integer.parseInt(cameraStation.getMunicipalityCode())))
                .jsonPath("$.properties.province").value(is(cameraStation.getProvince()))
                .jsonPath("$.properties.provinceCode").value(is(Integer.parseInt(cameraStation.getProvinceCode())))
                .jsonPath("$.properties.presets").value(hasSize(2))

                .jsonPath("$.properties.presets[0].id").value(is(preset1.getPresetId()))
                .jsonPath("$.properties.presets[0].presentationName").value(is(preset1.getPresetName1()))
                .jsonPath("$.properties.presets[0].inCollection").value(is(preset1.isInCollection()))
                .jsonPath("$.properties.presets[0].resolution").value(is(preset1.getResolution()))
                .jsonPath("$.properties.presets[0].directionCode").value(is(preset1.getDirection()))
                .jsonPath("$.properties.presets[0].direction").value(
                        is(WeathercamPresetDirectionV1.INCREASING_DIRECTION.name()))
                .jsonPath("$.properties.presets[0].imageUrl").value(isA(String.class))

                .jsonPath("$.properties.presets[1].id").value(is(preset2.getPresetId()))
                .jsonPath("$.properties.presets[1].presentationName").value(is(preset2.getPresetName1()))
                .jsonPath("$.properties.presets[1].inCollection").value(is(preset2.isInCollection()))
                .jsonPath("$.properties.presets[1].resolution").value(is(preset2.getResolution()))
                .jsonPath("$.properties.presets[1].directionCode").value(is(preset2.getDirection()))
                .jsonPath("$.properties.presets[1].direction").value(
                        is(WeathercamPresetDirectionV1.SPECIAL_DIRECTION.name()))
                .jsonPath("$.properties.presets[1].imageUrl").value(isA(String.class))

                .consumeWith(this::assertBodyTimesFormatMatchesIsoDateTimeWithZ);
    }

    @Test
    public void testCameraDataRestApi() throws Exception {

        executeGet(WeathercamControllerV1.API_WEATHERCAM_V1_STATIONS + WeathercamControllerV1.DATA)
                .expectStatus().isOk()
                .expectHeader().contentType(DtMediaType.APPLICATION_JSON_VALUE)
                .expectHeader().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER)
                .expectHeader().valueEqualsDate(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER,
                        imageUpdateTime1.toEpochMilli())
                .expectBody()
                .jsonPath("$.dataUpdatedTime").value(Matchers.equalTo(imageUpdateTime1.toString()))
                .jsonPath("$.stations").value(hasSize(1))
                .jsonPath("$.stations[0].id").value(is(preset1.getCameraId()))

                .jsonPath("$.stations[0].presets").value(hasSize(2))
                .jsonPath("$.stations[0].presets[0].id").value(is(preset1.getPresetId()))
                .jsonPath("$.stations[0].presets[0].measuredTime").value(is(imageUpdateTime1.toString()))

                .jsonPath("$.stations[0].presets[1].id").value(is(preset2.getPresetId()))
                .jsonPath("$.stations[0].presets[1].measuredTime").value(is(imageUpdateTime2.toString()))

                .consumeWith(this::assertBodyTimesFormatMatchesIsoDateTimeWithZ);
    }

    @Test
    public void testCameraDataRestApiById() throws Exception {

        executeGet(WeathercamControllerV1.API_WEATHERCAM_V1_STATIONS + "/" + preset1.getCameraId() +
                        WeathercamControllerV1.DATA)
                .expectStatus().isOk()
                .expectHeader().contentType(DtMediaType.APPLICATION_JSON_VALUE)
                .expectHeader().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER)
                .expectHeader().valueEqualsDate(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER,
                        imageUpdateTimeDb1.toEpochMilli())
                .expectBody()
                .jsonPath("$.id").value(is(preset1.getCameraId()))

                .jsonPath("$.presets").value(hasSize(2))
                .jsonPath("$.presets[0].id").value(is(preset1.getPresetId()))
                .jsonPath("$.presets[0].measuredTime").value(is(imageUpdateTime1.toString()))

                .jsonPath("$.presets[1].id").value(is(preset2.getPresetId()))
                .jsonPath("$.presets[1].measuredTime").value(is(imageUpdateTime2.toString()))

                .consumeWith(this::assertBodyTimesFormatMatchesIsoDateTimeWithZ);
    }

 }
