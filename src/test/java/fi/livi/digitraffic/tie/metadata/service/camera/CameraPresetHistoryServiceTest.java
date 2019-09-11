package fi.livi.digitraffic.tie.metadata.service.camera;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.model.CameraPresetHistory;

public class CameraPresetHistoryServiceTest extends AbstractServiceTest {

    private static final Logger log = LoggerFactory.getLogger(CameraPresetHistoryServiceTest.class);

    @Autowired
    private CameraPresetService cameraPresetService;

    @Autowired
    private CameraPresetHistoryService cameraPresetHistoryService;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void testSaveHistory() {
        final Optional<CameraPreset> cp = cameraPresetService.findAllPublishableCameraPresets().stream().findFirst();
        Assert.assertTrue(cp.isPresent());

        final CameraPreset preset = cp.get();
        final ZonedDateTime now = ZonedDateTime.now();
        final CameraPresetHistory history = generateHistory(preset, now.minusMinutes(1));
        cameraPresetHistoryService.saveHistory(history);

        final CameraPresetHistory found = cameraPresetHistoryService.findHistory(preset.getPresetId(), history.getVersionId());
        Assert.assertNotNull(found);
        Assert.assertFalse("Can't be same instanse to test", history.equals(found));
        Assert.assertEquals(history.getPresetId(), found.getPresetId());
        Assert.assertEquals(history.getVersionId(), found.getVersionId());
        Assert.assertEquals(history.getCameraPresetId(), found.getCameraPresetId());
        Assert.assertEquals(history.getLastModified(), found.getLastModified());
        Assert.assertEquals(history.getSize(), found.getSize());
        Assert.assertEquals(history.getCreated(), found.getCreated());
    }


    @Test
    public void testHistoryVersions() {

        Set<String> presetIds = new HashSet<>();
        cameraPresetService.findAllPublishableCameraPresets().stream().limit(2).forEach(cp -> {
            final ZonedDateTime lastModified = ZonedDateTime.now();
            IntStream.range(0, 5).forEach(i -> {
                log.info("Create history nr. {} for preset {}", i, cp.getPresetId());
                final CameraPresetHistory history = generateHistory(cp, lastModified.plusSeconds(i * 10));
                cameraPresetHistoryService.saveHistory(history);
                presetIds.add(cp.getPresetId());
            });
        });

        presetIds.forEach(id -> {
            log.info("Check history for preset {}", id);
            List<CameraPresetHistory> histories = cameraPresetHistoryService.findAllByPresetId(id);
            assertTrue(histories.size() == 5);

            ZonedDateTime prevDate = null;
            for (CameraPresetHistory h : histories) {
                assertEquals(id, h.getPresetId());
                if (prevDate != null) {
                    Assert.assertTrue("Previous history date must be before next", prevDate.isBefore(h.getLastModified()));
                }
                prevDate = h.getLastModified();
            }
        });
    }

    private static CameraPresetHistory generateHistory(final CameraPreset preset, final ZonedDateTime lastModified) {
        final String versionId = RandomStringUtils.randomAlphanumeric(32);
        return new CameraPresetHistory(preset.getPresetId(), versionId, preset.getId(), lastModified, true, 10, ZonedDateTime.now());
    }
}
