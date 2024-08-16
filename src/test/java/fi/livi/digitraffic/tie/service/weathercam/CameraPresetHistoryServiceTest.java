package fi.livi.digitraffic.tie.service.weathercam;

import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;
import static fi.livi.digitraffic.common.util.TimeUtil.getZonedDateTimeNowWithoutMillisAtUtc;
import static fi.livi.digitraffic.tie.service.weathercam.CameraPresetHistoryDataService.MAX_IDS_SIZE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.internal.verification.VerificationModeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.Rollback;

import fi.livi.digitraffic.common.util.ThreadUtil;
import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.conf.amazon.WeathercamS3Properties;
import fi.livi.digitraffic.tie.dao.weathercam.CameraPresetHistoryRepository;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryChangesDto;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryDto;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryPresencesDto;
import fi.livi.digitraffic.tie.dto.v1.camera.PresetHistoryChangeDto;
import fi.livi.digitraffic.tie.dto.v1.camera.PresetHistoryDto;
import fi.livi.digitraffic.tie.dto.v1.camera.PresetHistoryPresenceDto;
import fi.livi.digitraffic.tie.helper.AssertHelper;
import fi.livi.digitraffic.common.util.TimeUtil;
import fi.livi.digitraffic.tie.model.roadstation.RoadStation;
import fi.livi.digitraffic.tie.model.weathercam.CameraPreset;
import fi.livi.digitraffic.tie.model.weathercam.CameraPresetHistory;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.service.weathercam.CameraPresetHistoryDataService.HistoryStatus;
import jakarta.persistence.EntityManager;

public class CameraPresetHistoryServiceTest extends AbstractDaemonTest {

    private static final Logger log = LoggerFactory.getLogger(CameraPresetHistoryServiceTest.class);

    @SpyBean
    private CameraPresetService cameraPresetService;

    @Autowired
    private WeathercamS3Properties weathercamS3Properties;

    @Autowired
    private CameraPresetHistoryRepository cameraPresetHistoryRepository;

    @Autowired
    private CameraPresetHistoryUpdateService cameraPresetHistoryUpdateService;

    @Autowired
    private CameraPresetHistoryDataService cameraPresetHistoryDataService;

    @SpyBean
    private CameraImageUpdateHandler cameraImageUpdateHandler;

    @Autowired
    private EntityManager entityManager;

    @Value("${dt.amazon.s3.weathercam.bucketName}")
    private String s3WeathercamBucketName;

    @Value("${dt.amazon.s3.weathercam.region}")
    private String s3WeathercamRegion;

    @BeforeEach
    public void initData() {
        cameraPresetHistoryRepository.deleteAll();
        TestUtils.generateDummyCameraStations(4,2)
            .forEach(camera -> camera.forEach(preset -> entityManager.persist(preset)));
        entityManager.flush();
    }

    @AfterEach
    public void clearData() {
        TestUtils.commitAndEndTransactionAndStartNew();
        TestUtils.truncateCameraData(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew(); // make sure db is cleaned
    }

    @Test
    public void saveHistory() {
        final Optional<CameraPreset> cp = cameraPresetService.findAllPublishableCameraPresets().stream().findFirst();
        assertTrue(cp.isPresent());

        final CameraPreset preset = cp.get();
        final ZonedDateTime now = getZonedDateTimeNowWithoutMillisAtUtc();
        final CameraPresetHistory history = generateHistory(preset, now.minusMinutes(1));
        cameraPresetHistoryUpdateService.saveHistory(history);

        final CameraPresetHistory found = cameraPresetHistoryDataService.findHistoryVersionInclSecretInternal(preset.getPresetId(), history.getVersionId());
        assertNotNull(found);
        assertNotEquals(history, found, "Can't be same instance to test");
        assertEquals(history.getPresetId(), found.getPresetId());
        assertEquals(history.getVersionId(), found.getVersionId());
        assertEquals(history.getCameraPresetId(), found.getCameraPresetId());
        assertEquals(history.getLastModified(), found.getLastModified());
        assertEquals(history.getSize(), found.getSize());
        assertEquals(history.getCreated(), found.getCreated());
    }


    @Test
    public void historyVersions() {
        // Create 5 history item for 2 presets
        final List<String> presetIds = generateHistoryForPublicPresets(2, 5);

        presetIds.forEach(presetId -> {

            log.info("Check history for preset {}", presetId);
            final List<CameraPresetHistory> histories = cameraPresetHistoryDataService.findAllByPresetIdInclSecretAscInternal(presetId);
            assertEquals(5,histories.size());

            ZonedDateTime prevDate = null;
            for (final CameraPresetHistory h : histories) {
                assertEquals(presetId, h.getPresetId());
                if (prevDate != null) {
                    assertTrue(prevDate.isBefore(h.getLastModified()), "Previous history date must be before next");
                }
                prevDate = h.getLastModified();
            }
        });
    }

    @Test
    public void illegalIdParameter() {
        assertThrows(IllegalArgumentException.class, () ->
            cameraPresetHistoryDataService.findCameraOrPresetPublicHistory(Arrays.asList("C12345", "C1234"), (Instant) null));
        assertThrows(IllegalArgumentException.class, () ->
            cameraPresetHistoryDataService.findCameraOrPresetPublicHistory(Arrays.asList("C12345", "C1234"), (ZonedDateTime) null));
    }

    @Test
    public void tooLongListOfIdParameters() {
        assertThrows(IllegalArgumentException.class, () ->
            cameraPresetHistoryDataService.findCameraOrPresetPublicHistory(generateCameraIds(MAX_IDS_SIZE + 1), (Instant) null));
        assertThrows(IllegalArgumentException.class, () ->
            cameraPresetHistoryDataService.findCameraOrPresetPublicHistory(generateCameraIds(MAX_IDS_SIZE + 1), (ZonedDateTime) null));
    }

    @Test
    public void maxListOfIdParameters() {
        assertDoesNotThrow(() ->
            cameraPresetHistoryDataService.findCameraOrPresetPublicHistory(generateCameraIds(MAX_IDS_SIZE), (Instant) null));
        assertDoesNotThrow(() ->
            cameraPresetHistoryDataService.findCameraOrPresetPublicHistory(generateCameraIds(MAX_IDS_SIZE), (ZonedDateTime) null));
    }

    @Test
    public void historyUpdateInPast() {
        doNothing().when(cameraImageUpdateHandler).hideCurrentImageForPreset(any(CameraPreset.class));

        final List<String> presetIds = generateHistoryForPublicPresets(2, 5);
        final String modifiedPresetId = presetIds.get(0);
        final String notModifiedPresetId = presetIds.get(1);

        final List<CameraPresetHistory> all = cameraPresetHistoryDataService.findAllByPresetIdInclSecretAscInternal(modifiedPresetId);
        final CameraPreset cp = cameraPresetService.findCameraPresetByPresetId(modifiedPresetId);
        final RoadStation rs = cp.getRoadStation();
        all.forEach(e -> entityManager.detach(e));
        // Public 0. and 1., secret 2., 3. and 4.
        final CameraPresetHistory middle = all.get(2);
        rs.updatePublicity(true);
        rs.updatePublicity(false, middle.getLastModified()); // -> previous public

        cameraPresetHistoryUpdateService.updatePresetHistoryPublicityForCamera(rs);
        entityManager.flush();

        // camera secret -> presets to secret
        verify(cameraImageUpdateHandler, VerificationModeFactory.atLeast(1)).hideCurrentImageForPreset(any(CameraPreset.class));
        verify(cameraImageUpdateHandler, VerificationModeFactory.times(1)).hideCurrentImagesForCamera(argThat(r -> r.getLotjuId().equals(rs.getLotjuId())));
        verify(cameraImageUpdateHandler, VerificationModeFactory.times(0)).hideCurrentImagesForCamera(argThat(r -> !r.getLotjuId().equals(rs.getLotjuId())));

        final List<CameraPresetHistory> allUpdated = cameraPresetHistoryDataService.findAllByPresetIdInclSecretAscInternal(modifiedPresetId);
        assertEquals(true, allUpdated.get(0).getPublishable());
        assertEquals(true, allUpdated.get(1).getPublishable());
        assertEquals(false, allUpdated.get(2).getPublishable());
        assertEquals(false, allUpdated.get(3).getPublishable());
        assertEquals(false, allUpdated.get(4).getPublishable());


        final List<CameraPresetHistory> allNotUpdated = cameraPresetHistoryDataService.findAllByPresetIdInclSecretAscInternal(notModifiedPresetId);
        assertEquals(true, allNotUpdated.get(0).getPublishable());
        assertEquals(true, allNotUpdated.get(1).getPublishable());
        assertEquals(true, allNotUpdated.get(2).getPublishable());
        assertEquals(true, allNotUpdated.get(3).getPublishable());
        assertEquals(true, allNotUpdated.get(4).getPublishable());
    }

    @Test
    public void historyUpdateInFuture() {
        final List<String> presetIds = generateHistoryForPublicPresets(1, 5);
        final String presetId = presetIds.get(0);

        final CameraPreset cp = cameraPresetService.findCameraPresetByPresetId(presetId);
        final RoadStation rs = cp.getRoadStation();
        rs.updatePublicity(true);
        rs.updatePublicity(false, getZonedDateTimeNowWithoutMillisAtUtc().plusDays(1)); // -> previous/now public, future secret
        cameraPresetHistoryUpdateService.updatePresetHistoryPublicityForCamera(rs);
        entityManager.flush();
        final List<CameraPresetHistory> allUpdated = cameraPresetHistoryDataService.findAllByPresetIdInclSecretAscInternal(presetId);
        assertEquals(true, allUpdated.get(0).getPublishable());
        assertEquals(true, allUpdated.get(1).getPublishable());
        assertEquals(true, allUpdated.get(2).getPublishable());
        assertEquals(true, allUpdated.get(3).getPublishable());
        assertEquals(true, allUpdated.get(4).getPublishable());
    }

    @Test
    public void cameraOrPresetPublicHistory() {
        final int historySize = TestUtils.getRandomId(21, 27);
        final ZonedDateTime lastModified = getZonedDateTimeNowWithoutMillisAtUtc();
        final String cameraId = generateHistoryForCamera(historySize, lastModified);
        log.info("Generated history for camera {} from {} to {} (size {})", cameraId, lastModified, lastModified.minusHours(historySize-1), historySize);
        // Get history for last 24 h
        final List<CameraHistoryDto> allHistory =
            cameraPresetHistoryDataService.findCameraOrPresetPublicHistory(Collections.singletonList(cameraId), (Instant) null);
        assertEquals(1, allHistory.size());
        final CameraHistoryDto history = allHistory.get(0);
        assertEquals(cameraId, history.cameraId);
        final List<PresetHistoryDto> presetsHistories = history.cameraHistory;
        assertTrue(presetsHistories.size() >= 2);

        // Every preset only once
        final Set<String> cameraPesetIds = new HashSet<>();
        presetsHistories.forEach(ph -> {
            assertFalse(cameraPesetIds.contains(ph.getPresetId()));
            cameraPesetIds.add(ph.getPresetId());
        });

        final int expectedHistorySize = Math.min(24, historySize);
        // Check history size/preset and history timestamps
        presetsHistories.forEach(ph -> {
            // Generation creates history for ever hour and oldest is 24h -> max 24 stk.
            assertEquals(Math.min(historySize, 24), ph.presetHistory.size());
            IntStream.range(0, expectedHistorySize).forEach(i ->
                assertEquals(lastModified.minusHours(i), ph.presetHistory.get(i).getLastModified()));
        });

        // Check that every url to image is unique
        final Set<String> versions = new HashSet<>();
        presetsHistories.forEach(ph -> ph.presetHistory.forEach(h -> {
            assertFalse(versions.contains(h.getImageUrl()));
            versions.add(h.getImageUrl());
        }));
        // Check history versions size
        assertEquals(versions.size(), expectedHistorySize * presetsHistories.size());
    }

    @Test
    public void findCameraOrPresetHistoryPresencesNullId() {
        final List<String> presetIds = generateHistoryForPublicPresets(5, 1);
        final CameraPresetHistory secret = cameraPresetHistoryDataService.findAllByPresetIdInclSecretAscInternal(presetIds.get(0)).get(0);
        secret.setPublishable(false);
        entityManager.flush();

        final CameraHistoryPresencesDto allCameraPresences =
            cameraPresetHistoryDataService.findCameraOrPresetHistoryPresences(null, (Instant) null, null);
        final List<PresetHistoryPresenceDto> allPresetPresences =
            allCameraPresences.cameraHistoryPresences.stream().flatMap(c -> c.presetHistoryPresences.stream()).toList();

        // Secret can't be found
        final Optional<PresetHistoryPresenceDto> secretPresence =
            allPresetPresences.stream().filter(h -> h.getPresetId().equals(secret.getPresetId())).findFirst();
        assertTrue(secretPresence.isPresent());
        assertFalse(secretPresence.get().isHistoryPresent());

        allPresetPresences.stream().filter(h -> !h.getPresetId().equals(secret.getPresetId())).forEach(h -> assertTrue(h.isHistoryPresent()));
    }

    @Test
    public void findCameraOrPresetHistoryPresences() {
        final List<String> presetIds = generateHistoryForPublicPresets(10, 1);
        final String cameraId = presetIds.get(0).substring(0, 6);
        // Make sure there is other cameras too in history
        assertTrue(presetIds.stream().anyMatch(id -> !id.substring(0,6).equals(cameraId)));

        final CameraHistoryPresencesDto cameraPresences =
            cameraPresetHistoryDataService.findCameraOrPresetHistoryPresences(cameraId, (Instant) null, null);
        final List<PresetHistoryPresenceDto> allPresetPresences =
            cameraPresences.cameraHistoryPresences.stream().flatMap(c -> c.presetHistoryPresences.stream()).toList();

        // history should exist for camera
        assertFalse(allPresetPresences.isEmpty());
        allPresetPresences.stream().filter(h -> h.getCameraId().equals(cameraId)).forEach(h -> assertTrue(h.isHistoryPresent()));

        // Other cameras presences should not exist
        assertFalse(allPresetPresences.stream().anyMatch(h -> !h.getCameraId().equals(cameraId)));
    }

    @Test
    public void findPresetHistoryPresences() {
        final List<String> presetIds = generateHistoryForPublicPresets(10, 2);
        final String presetId = presetIds.get(0);
        // Make sure there is other presets
        assertTrue(presetIds.stream().anyMatch(id -> !id.equals(presetId)));

        final CameraHistoryPresencesDto cameraPresences =
            cameraPresetHistoryDataService.findCameraOrPresetHistoryPresences(presetId, (Instant) null, null);
        final List<PresetHistoryPresenceDto> presetPresences =
            cameraPresences.cameraHistoryPresences.stream().flatMap(c -> c.presetHistoryPresences.stream()).toList();

        // history should exist only for given preset
        assertEquals(1, presetPresences.size());
        assertEquals(presetId, presetPresences.get(0).getPresetId());
        assertTrue(presetPresences.get(0).isHistoryPresent());
    }

    @Test
    public void findCameraPresetHistoryPresenceTimeLimit() {
        final Optional<CameraPreset> cp = cameraPresetService.findAllPublishableCameraPresets().stream().findFirst();
        assertTrue(cp.isPresent());
        final CameraPreset preset = cp.get();
        final ZonedDateTime now = getZonedDateTimeNowWithoutMillisAtUtc();
        final CameraPresetHistory history0 = generateHistory(preset, now);
        final CameraPresetHistory history1 = generateHistory(preset, now.minusMinutes(1), false);
        final CameraPresetHistory history2 = generateHistory(preset, now.minusMinutes(2), false);
        final CameraPresetHistory history3 = generateHistory(preset, now.minusMinutes(3));
        cameraPresetHistoryUpdateService.saveHistory(history0);
        cameraPresetHistoryUpdateService.saveHistory(history1);
        cameraPresetHistoryUpdateService.saveHistory(history2);
        cameraPresetHistoryUpdateService.saveHistory(history3);
        entityManager.flush();

        // Only secret at given time interval
        final CameraHistoryPresencesDto cameraPresences1 =
            cameraPresetHistoryDataService.findCameraOrPresetHistoryPresences(preset.getPresetId(), now.minusMinutes(2), now.minusMinutes(1));
        assertEquals(1, cameraPresences1.cameraHistoryPresences.size());
        assertEquals(1, cameraPresences1.cameraHistoryPresences.get(0).presetHistoryPresences.size());
        assertFalse(cameraPresences1.cameraHistoryPresences.get(0).presetHistoryPresences.get(0).isHistoryPresent());

        // One public at given time interval
        final CameraHistoryPresencesDto cameraPresences2 =
            cameraPresetHistoryDataService.findCameraOrPresetHistoryPresences(preset.getPresetId(), now.minusMinutes(2), now);
        assertEquals(1, cameraPresences2.cameraHistoryPresences.size());
        assertEquals(1, cameraPresences2.cameraHistoryPresences.get(0).presetHistoryPresences.size());
        assertTrue(cameraPresences2.cameraHistoryPresences.get(0).presetHistoryPresences.get(0).isHistoryPresent());

        // One public at given time interval
        final CameraHistoryPresencesDto cameraPresences3 =
            cameraPresetHistoryDataService.findCameraOrPresetHistoryPresences(preset.getPresetId(), now.minusMinutes(3), now.minusMinutes(1));
        assertEquals(1, cameraPresences3.cameraHistoryPresences.size());
        assertEquals(1, cameraPresences3.cameraHistoryPresences.get(0).presetHistoryPresences.size());
        assertTrue(cameraPresences3.cameraHistoryPresences.get(0).presetHistoryPresences.get(0).isHistoryPresent());
    }

    @Test
    public void findLatestWithPresetIdIncSecret() {
        final Optional<CameraPreset> cp = cameraPresetService.findAllPublishableCameraPresets().stream().findFirst();
        assertTrue(cp.isPresent());
        final CameraPreset preset = cp.get();
        final ZonedDateTime now = getZonedDateTimeNowWithoutMillisAtUtc();
        generateHistory(preset, now.minusMinutes(1));
        generateHistory(preset, now.minusMinutes(2), false);

        // Make sure there is other cameras too in history
        final CameraPresetHistory h1 = cameraPresetHistoryDataService.findLatestWithPresetIdIncSecretInternal(preset.getPresetId());
        assertEquals(now.minusMinutes(1), h1.getLastModified());
        assertTrue(h1.getPublishable());

        generateHistory(preset, now, false);
        final CameraPresetHistory h2 = cameraPresetHistoryDataService.findLatestWithPresetIdIncSecretInternal(preset.getPresetId());
        assertEquals(now, h2.getLastModified());
        assertFalse(h2.getPublishable());
    }

    @Test
    public void findAllByPresetIdInclSecretAsc() {
        final Optional<CameraPreset> cp = cameraPresetService.findAllPublishableCameraPresets().stream().findFirst();
        assertTrue(cp.isPresent());
        final CameraPreset preset = cp.get();
        final ZonedDateTime now = getZonedDateTimeNowWithoutMillisAtUtc();
        generateHistory(preset, now, false);
        generateHistory(preset, now.minusMinutes(1));
        generateHistory(preset, now.minusMinutes(2), false);

        final List<CameraPresetHistory> history = cameraPresetHistoryDataService.findAllByPresetIdInclSecretAscInternal(preset.getPresetId());

        assertEquals(now.minusMinutes(2), history.get(0).getLastModified());
        assertFalse(history.get(0).getPublishable());

        assertEquals(now.minusMinutes(1), history.get(1).getLastModified());
        assertTrue(history.get(1).getPublishable());

        assertEquals(now, history.get(2).getLastModified());
        assertFalse(history.get(2).getPublishable());

    }

    @Test
    public void deleteAllWithPresetId() {
        final List<String> presetIds = generateHistoryForPublicPresets(10, 2);
        final String presetIdToDelete = presetIds.get(0);
        final String presetIdNotToDelete = presetIds.get(2);
        // Make sure there is other presets
        assertTrue(presetIds.stream().anyMatch(id -> !id.equals(presetIdNotToDelete)));

        final CameraHistoryPresencesDto presetPresencesToDelete =
            cameraPresetHistoryDataService.findCameraOrPresetHistoryPresences(presetIdToDelete, (Instant) null, null);
        final CameraHistoryPresencesDto presetPresencesNotToDelete =
            cameraPresetHistoryDataService.findCameraOrPresetHistoryPresences(presetIdNotToDelete, (Instant) null, null);

        final PresetHistoryPresenceDto deleter = presetPresencesToDelete.cameraHistoryPresences.get(0).presetHistoryPresences.get(0);
        assertEquals(presetIdToDelete, deleter.getPresetId());
        assertTrue(deleter.isHistoryPresent(), presetIdToDelete);

        final PresetHistoryPresenceDto notDelete = presetPresencesNotToDelete.cameraHistoryPresences.get(0).presetHistoryPresences.get(0);
        assertEquals(presetIdNotToDelete, notDelete.getPresetId());
        assertTrue(notDelete.isHistoryPresent(), presetIdNotToDelete);

        cameraPresetHistoryUpdateService.deleteAllWithPresetId(presetIdToDelete);

        boolean exception = false;
        try {
            cameraPresetHistoryDataService.findCameraOrPresetHistoryPresences(presetIdToDelete, (Instant) null, null);
        } catch (final ObjectNotFoundException e) {
            exception = true;
        }
        assertTrue(exception);
        final CameraHistoryPresencesDto notDeleted =
            cameraPresetHistoryDataService.findCameraOrPresetHistoryPresences(presetIdNotToDelete, (Instant) null, null);
        assertTrue(notDeleted.cameraHistoryPresences.get(0).presetHistoryPresences.get(0).isHistoryPresent(), presetIdNotToDelete);

    }
    @Test
    public void resolveHistoryStatusForVersion() {
        final Optional<CameraPreset> cp = cameraPresetService.findAllPublishableCameraPresets().stream().findFirst();
        assertTrue(cp.isPresent());
        final CameraPreset preset = cp.get();
        final String presetId = preset.getPresetId();
        final ZonedDateTime now = getZonedDateTimeNowWithoutMillisAtUtc();
        final CameraPresetHistory publicHistory = generateHistory(preset, now.minusMinutes(1));
        final CameraPresetHistory publicHistoryTooOld = generateHistory(preset, now.minusDays(2));
        final CameraPresetHistory secretHistory = generateHistory(preset, now, false);

        final HistoryStatus statusPublic =
            cameraPresetHistoryDataService.resolveHistoryStatusForVersion(presetId + ".jpg", publicHistory.getVersionId());
        assertEquals(HistoryStatus.PUBLIC, statusPublic);

        final HistoryStatus statusOld =
            cameraPresetHistoryDataService.resolveHistoryStatusForVersion(presetId + ".jpg", publicHistoryTooOld.getVersionId());
        assertEquals(HistoryStatus.TOO_OLD, statusOld);

        final HistoryStatus statusSecret =
            cameraPresetHistoryDataService.resolveHistoryStatusForVersion(presetId + ".jpg", secretHistory.getVersionId());
        assertEquals(HistoryStatus.SECRET, statusSecret);

        final HistoryStatus statusNotFound =
            cameraPresetHistoryDataService.resolveHistoryStatusForVersion(presetId + ".jpg", "FooBar123");
        assertEquals(HistoryStatus.NOT_FOUND, statusNotFound);

        final HistoryStatus statusIllegalKey =
            cameraPresetHistoryDataService.resolveHistoryStatusForVersion(presetId.substring(0,7) + ".jpg", publicHistory.getVersionId());
        assertEquals(HistoryStatus.ILLEGAL_KEY, statusIllegalKey);
    }

    @Test
    public void findcameraOrPresetChanges() {
        // Generate preset history
        final int historySize = TestUtils.getRandomId(21, 27);
        final List<String> presetIds = generateHistoryForPublicPresets(2, historySize);
        final CameraPreset cp1 = cameraPresetService.findCameraPresetByPresetId(presetIds.get(1));
        final RoadStation rs1 = cp1.getRoadStation();
        final List<CameraPresetHistory> allBefore = cameraPresetHistoryDataService.findAllByPresetIdInclSecretAscInternal(cp1.getPresetId());

        // Must end transaction to save different timestamps to db
        TestUtils.commitAndEndTransactionAndStartNew();
        ThreadUtil.delayMs(10);

        // Generate 3 changes in the history
        IntStream.range(1,4).forEach(i -> {
            final boolean changeTo = i % 2 == 0; // 1:false -> 2:true -> 3:false
            final int index = historySize * i/4;
            log.info("Index {}/{} to {}", index, historySize, changeTo);
            final CameraPresetHistory h = allBefore.get(index);
            rs1.updatePublicity(changeTo, h.getLastModified());
            cameraPresetHistoryUpdateService.updatePresetHistoryPublicityForCamera(rs1);
            // Must end transaction to save different timestamps to db
            TestUtils.commitAndEndTransactionAndStartNew();
            ThreadUtil.delayMs(1000);
        });
        TestUtils.entityManagerFlushAndClear(entityManager);

        final List<CameraPresetHistory> allAfter = cameraPresetHistoryDataService.findAllByPresetIdInclSecretAscInternal(cp1.getPresetId());

        IntStream.range(1,4).forEach(i -> {
            final boolean changeTo = i % 2 == 0; // 1:false -> 2:true -> 3:false
            final int changesCount = 4 - i;
            final int index = historySize * i/4;
            final ZonedDateTime changedOn = TimeUtil.toZonedDateTimeAtUtc(allAfter.get(index).getModified());
            log.info("Find changes from index {}/{} change to {} on {}", index, historySize, changeTo, changedOn);

            final List<PresetHistoryChangeDto> changesAfter =
                cameraPresetHistoryDataService.findCameraOrPresetHistoryChangesAfter(changedOn, Collections.singletonList(cp1.getPresetId()))
                    .changes;
            final CameraHistoryChangesDto changesBeforeDto =
                cameraPresetHistoryDataService.findCameraOrPresetHistoryChangesAfter(changedOn.minus(600, ChronoUnit.MILLIS), Collections.singletonList(cp1.getPresetId()));
            final List<PresetHistoryChangeDto> changesBefore = changesBeforeDto.changes;


            // When fetching changes before and after change, there should be one more change in former
            assertEquals(changesAfter.size() + 1, changesBefore.size());
            assertCollectionSize(changesCount, changesBefore);

            final PresetHistoryChangeDto oldestChange = changesBefore.get(0);
            log.info("Found {} change to {}, lastModified on {} and modified on {}",
                     oldestChange.getPresetId(), oldestChange.getPublishableTo(), oldestChange.getLastModified(), oldestChange.getModified());
            // First change in result should match change status
            assertEquals(changeTo, changesBefore.get(0).getPublishableTo());
            // Last change in result should have the latest change time
            assertEquals(changesBefore.get(changesBefore.size()-1).getModified(), changesBeforeDto.latestChange);
        });
    }

    @Test
    public void createS3UriForVersion() {
        final String image = "C1234567.jpg";
        final String imageVersions = "C1234567-versions.jpg";
        final String version = "ABCDEFG123456";
        final URI url = weathercamS3Properties.getS3UriForVersion(image, version);
        final String expected = "http://" + s3WeathercamBucketName + ".s3-" + s3WeathercamRegion + ".amazonaws.com/" + imageVersions + "?versionId=" + version;
        assertEquals(expected, url.toString());
    }

    @Test
    public void presetNotPublicInPast() {
        final CameraPreset preset = cameraPresetService.save(TestUtils.generateDummyPreset());
        final ZonedDateTime T1 = getZonedDateTimeNowWithoutMillisAtUtc().minusHours(3);
        final ZonedDateTime T2 = T1.plusHours(1);
        final ZonedDateTime T3 = T1.plusHours(2);
        final ZonedDateTime T4 = T1.plusHours(3);

        // Set preset to be secret in T2
        preset.setPublic(true);
        generateHistory(preset, T1, false);
        preset.setPublic(false);
        generateHistory(preset, T2, false);
        preset.setPublic(true);
        generateHistory(preset, T3, false);
        generateHistory(preset, T4, true);

        final List<CameraPresetHistory> historyBefore = cameraPresetHistoryDataService.findAllByPresetIdInclSecretAscInternal(preset.getPresetId());
        assertCollectionSize(4, historyBefore);
        assertFalse(historyBefore.get(0).getPublishable()); // T1
        assertFalse(historyBefore.get(1).getPublishable()); // T2
        assertFalse(historyBefore.get(2).getPublishable()); // T3
        assertTrue(historyBefore.get(3).getPublishable()); // T4

        // Set camera to public from the T1. Preset history at T2 should remain secret. Others should be public.
        final RoadStation rs = preset.getRoadStation();
        rs.updatePublicity(true, T1);
        TestUtils.entityManagerFlushAndClear(entityManager);

        cameraPresetHistoryUpdateService.updatePresetHistoryPublicityForCamera(rs);

        final List<CameraPresetHistory> historyAfter = cameraPresetHistoryDataService.findAllByPresetIdInclSecretAscInternal(preset.getPresetId());
        assertTrue(historyAfter.get(0).getPublishable()); // T1
        assertFalse(historyAfter.get(1).getPublishable()); // T2
        assertTrue(historyAfter.get(2).getPublishable()); // T3
        assertTrue(historyAfter.get(3).getPublishable()); // T4
    }

    @Test
    public void deleteOlderThanHours() {
        final int historySize = TestUtils.getRandomId(40, 80);
        // handle possible gap between server and db times
        final ZonedDateTime lastModified = getZonedDateTimeNowWithoutMillisAtUtc().plusMinutes(1);
        // History for historySize-1 hours backwards
        final String cameraId = generateHistoryForCamera(historySize, lastModified);
        final List<CameraHistoryDto> history = cameraPresetHistoryDataService.findCameraOrPresetPublicHistory(Collections.singletonList(cameraId), (Instant) null);
        final long presetCount = history.get(0).cameraHistory.stream().map(PresetHistoryDto::getPresetId).distinct().count();
        final List<CameraPresetHistory> allBeforeDelete = cameraPresetHistoryRepository.findAll();
        log.info("allBeforeDeleteSize {}, presetCount {}, historySize {}", allBeforeDelete.size(), presetCount, historySize);
        assertEquals(historySize*presetCount, allBeforeDelete.size());
        TestUtils.entityManagerFlushAndClear(entityManager);

        // after delete there should be left only newer than 24 hours -> 25 left/preset
        cameraPresetHistoryUpdateService.deleteOlderThanHoursHistory(24);
        final List<CameraPresetHistory> allAfterDelete = cameraPresetHistoryRepository.findAll();
        log.info("Before {} after delete {}", allBeforeDelete.size(), allAfterDelete.size());
        assertEquals(25*presetCount, allAfterDelete.size());

        // All presets should have history of 25
        final Map<String, List<CameraPresetHistory>> historyPerPreset =
            allAfterDelete.stream().collect(Collectors.groupingBy(CameraPresetHistory::getPresetId));
        historyPerPreset.values().forEach(h -> AssertHelper.assertCollectionSize(25, h));

        // All history should be equal or newer than 24 h
        final ZonedDateTime oldestLimit = lastModified.minusHours(24).minusSeconds(1);
        allAfterDelete.forEach(h -> assertTrue(h.getLastModified().isAfter(oldestLimit)));
    }

    @Disabled("Internal testing")
    @Rollback(false)
    @Test
    public void generateHistoryForInternalTesting() {
        final int historySize = TestUtils.getRandomId(40, 80);
        // handle possible gap between server and db times
        final ZonedDateTime lastModified = getZonedDateTimeNowWithoutMillisAtUtc().plusSeconds(10);
        // History for 39 hours backwards
        assertDoesNotThrow(() -> {
            generateHistoryForCamera(historySize, lastModified);
        });
    }

    /**
     * Generates camera preset history for one camera. Last one timestamp will be
     * lastModified - (historySize-1) hours.
     * @param historySize How many history items to generate
     * @param lastModified Latest history item lastModified time. Others will have times with 1h decrement of previous.
     * @return Camera id
     */
    private String generateHistoryForCamera(final int historySize, final ZonedDateTime lastModified) {
        final List<Map.Entry<String, List<CameraPreset>>> all =
            cameraPresetService.findAllPublishableCameraPresets().stream()
                .collect(Collectors.groupingBy(CameraPreset::getCameraId))
                .entrySet().stream().filter(e -> e.getValue().size() > 1).toList();

        // Get random camera
        final Map.Entry<String, List<CameraPreset>> camera =
            all.stream().skip((int) (all.size() * Math.random())).findAny().orElseThrow();

        camera.getValue().forEach(cameraPreset -> IntStream.range(0,historySize)
            .forEach(i -> generateHistory(cameraPreset, lastModified.minusHours(i))));
        return camera.getKey();
    }

    private List<String> generateHistoryForPublicPresets(final int presetCount, final int historyCountPerPreset) {
        final List<String> presetIds = new ArrayList<>();
        cameraPresetService.findAllPublishableCameraPresets().stream().limit(presetCount).forEach(cp -> {
            presetIds.add(cp.getPresetId());
            final ZonedDateTime lastModified = getZonedDateTimeNowWithoutMillisAtUtc();
            IntStream.range(0, historyCountPerPreset).map(i -> historyCountPerPreset - i - 1).forEach(i -> {
                log.info("Create history nr. {} for preset {}", i, cp.getPresetId());
                generateHistory(cp, lastModified.minusMinutes(i));
            });
        });
        return presetIds;
    }

    private CameraPresetHistory generateHistory(final CameraPreset preset, final ZonedDateTime lastModified) {
        return generateHistory(preset, lastModified, true);
    }
    private CameraPresetHistory generateHistory(final CameraPreset preset, final ZonedDateTime lastModified, final boolean publishable) {
        final String versionId = RandomStringUtils.randomAlphanumeric(32);
        final CameraPresetHistory history =
            new CameraPresetHistory(preset.getPresetId(), versionId, preset.getId(), lastModified, publishable, 10, preset.isPublic());
        cameraPresetHistoryUpdateService.saveHistory(history);
        entityManager.flush();
        return history;
    }

    private static List<String> generateCameraIds(final int count) {
        return IntStream.range(0, count).mapToObj(i -> String.format("C%s", StringUtils.leftPad(String.valueOf(i), 5, "0"))).collect(Collectors.toList());
    }
}
