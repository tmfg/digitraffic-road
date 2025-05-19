package fi.livi.digitraffic.tie.service.weathercam.v1;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.common.util.TimeUtil;
import fi.livi.digitraffic.tie.conf.amazon.WeathercamS3Properties;
import fi.livi.digitraffic.tie.dao.weathercam.CameraPresetHistoryRepository;
import fi.livi.digitraffic.tie.dto.v1.camera.PresetHistoryChangeDto;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamPresetPublicityHistoryV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamStationPresetsPublicityHistoryV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamStationsPresetsPublicityHistoryV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.history.WeathercamPresetHistoryDtoV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.history.WeathercamPresetHistoryItemDtoV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.history.WeathercamPresetsHistoryDtoV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.history.WeathercamsHistoryDtoV1;
import fi.livi.digitraffic.tie.model.weathercam.CameraPresetHistory;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;

@Service
public class WeathercamPresetHistoryDataWebServiceV1 {
    private static final Logger log = LoggerFactory.getLogger(WeathercamPresetHistoryDataWebServiceV1.class);
    private final CameraPresetHistoryRepository cameraPresetHistoryRepository;
    private final WeathercamS3Properties weathercamS3Properties;

    @Autowired
    public WeathercamPresetHistoryDataWebServiceV1(final CameraPresetHistoryRepository cameraPresetHistoryRepository,
                                                   final WeathercamS3Properties weathercamS3Properties) {
        this.cameraPresetHistoryRepository = cameraPresetHistoryRepository;
        this.weathercamS3Properties = weathercamS3Properties;
    }

    @Transactional(readOnly = true)
    public WeathercamsHistoryDtoV1 getWeathercamsHistory() {
        final StopWatch time = StopWatch.createStarted();

        final List<CameraPresetHistory> history =
                cameraPresetHistoryRepository.findAllPublishableHistoryByCameraIdOrNullOrderByPresetIdAndLastModified(
                        null, getOldestTimeLimit());
        log.debug(
                "method=getWeathercamsHistory db query findAllPublishableHistoryByCameraIdOrNullOrderByPresetIdAndLastModified tookMs={} ms",
                time.getDuration().toMillis());
        final WeathercamsHistoryDtoV1 dto = convertToWeathercamsHistory(history);
        log.debug("method=getWeathercamsHistory tookMs={} ms", time.getDuration().toMillis());
        return dto;
    }

    @Transactional(readOnly = true)
    public WeathercamPresetsHistoryDtoV1 findCameraOrPresetPublicHistory(final String cameraOrPresetId) {
        final StopWatch time = StopWatch.createStarted();
        if (cameraOrPresetId == null || !isCameraId(cameraOrPresetId) && !isPresetId(cameraOrPresetId)) {
            throw new IllegalArgumentException(
                    "Id parameter should be string starting with C. Id should be 6 characters long for camera and 8 characters long for preset id");
        }

        final List<CameraPresetHistory> history =
                isPresetId(cameraOrPresetId) ?
                cameraPresetHistoryRepository.findAllPublishableHistoryByPresetIdOrderByPresetIdAndLastModified(
                        cameraOrPresetId, getOldestTimeLimit()) :
                cameraPresetHistoryRepository.findAllPublishableHistoryByCameraIdOrNullOrderByPresetIdAndLastModified(
                        cameraOrPresetId, getOldestTimeLimit());

        if (history.isEmpty()) {
            if (isCameraId(cameraOrPresetId)) {
                throw new ObjectNotFoundException("Weathercam", cameraOrPresetId);
            }
            throw new ObjectNotFoundException("Weathercam preset", cameraOrPresetId);
        }

        log.debug("method=findCameraOrPresetPublicHistory db query findAllPublishableHistory for {} tookMs={} ms",
                cameraOrPresetId, time.getDuration().toMillis());
        final String cameraId = cameraOrPresetId.substring(0, 6);
        final WeathercamPresetsHistoryDtoV1 dto = convertToWeathercamPresetsHistory(cameraId, history);

        log.debug("method=findCameraOrPresetPublicHistory for {} tookMs={} ms", cameraOrPresetId, time.getDuration().toMillis());
        return dto;
    }

    /**
     * @param after Return changes after timestamp
     * @return History changes ordered by weathercam id and modified time in ascending order
     */
    @Transactional(readOnly = true)
    public WeathercamStationsPresetsPublicityHistoryV1 findWeathercamPresetPublicityChangesAfter(final Instant after) {

        final Instant latestChange = cameraPresetHistoryRepository.getLatestChangesTime();
        final List<PresetHistoryChangeDto> changes =
                cameraPresetHistoryRepository.findCameraPresetHistoryChangesAfter(after);

        final List<WeathercamStationPresetsPublicityHistoryV1> weathercamHistoryChanges =
                changes.stream()
                        // 1. Group changes by weathercam station
                        .collect(Collectors.groupingBy(PresetHistoryChangeDto::getCameraId, LinkedHashMap::new,
                                Collectors.toList()))
                        .entrySet().stream()
                        // Map data/station to WeathercamStationHistoryDataV1
                        .map(e -> {
                            // 2. Map data/station to WeathercamPresetHistoryDataV1 list and calculate latest modification time for station
                            final AtomicReference<Instant> stationDataModified = new AtomicReference<>();
                            final List<WeathercamPresetPublicityHistoryV1> list = e.getValue().stream().map(h -> {
                                stationDataModified.set(
                                        TimeUtil.getGreatest(stationDataModified.get(), h.getModified()));
                                return new WeathercamPresetPublicityHistoryV1(h.getPresetId(), h.getLastModified(),
                                        h.getModified(), h.getPublishableTo());
                            }).collect(Collectors.toList());
                            return new WeathercamStationPresetsPublicityHistoryV1(e.getKey(), list,
                                    stationDataModified.get());
                        }).collect(Collectors.toList());

        return new WeathercamStationsPresetsPublicityHistoryV1(latestChange, weathercamHistoryChanges);
    }

    private WeathercamsHistoryDtoV1 convertToWeathercamsHistory(final List<CameraPresetHistory> history) {
        final StopWatch time = StopWatch.createStarted();

        final List<WeathercamPresetsHistoryDtoV1> stations =
                history.stream()
                        // Group by camera id
                        .collect(Collectors.groupingBy(CameraPresetHistory::getCameraId))
                        // List<WeathercamPresetsHistoryDtoV1> history per weathercam
                        .entrySet().stream().map(e -> convertToWeathercamPresetsHistory(e.getKey(), e.getValue()))
                        .collect(Collectors.toList());

        // Sort by camera id, slighly faster than in-stream sort
        stations.sort(Comparator.comparing(o -> o.id));

        final Instant dataUpdatedTime = stations.stream()
                .filter(h -> h.dataUpdatedTime != null)
                .map(h -> h.dataUpdatedTime).max(Comparator.naturalOrder()).orElse(null);

        log.debug("method=convertToWeathercamsHistory tookMs={}", time.getDuration().toMillis());
        return new WeathercamsHistoryDtoV1(dataUpdatedTime, stations);
    }

    private WeathercamPresetsHistoryDtoV1 convertToWeathercamPresetsHistory(final String cameraId,
                                                                            final List<CameraPresetHistory> history) {
        final AtomicReference<Instant> historyModified = new AtomicReference<>();

        final List<WeathercamPresetHistoryDtoV1> presets = history.stream()
                // Map<presetId, List<CameraPresetHistory>
                .collect(Collectors.groupingBy(CameraPresetHistory::getPresetId))
                // Map<cameraId, List<PresetHistoryDto>>
                .entrySet().stream().map(e -> convertToWeathercamPresetHistoryDtoV1(historyModified, e))
                .collect(Collectors.toList());

        // Sort by preset id, slighly faster than in-stream sort
        presets.sort(Comparator.comparing(ph -> ph.id));

        return new WeathercamPresetsHistoryDtoV1(cameraId, historyModified.get(), presets);
    }

    private WeathercamPresetHistoryDtoV1 convertToWeathercamPresetHistoryDtoV1(
            final AtomicReference<Instant> historyModified,
            final Map.Entry<String, List<CameraPresetHistory>> e) {
        final WeathercamPresetHistoryDtoV1 presetHistory = convertToWeathercamPresetHistory(e.getKey(), e.getValue());
        historyModified.set(TimeUtil.getGreatest(historyModified.get(), presetHistory.dataUpdatedTime));
        return presetHistory;
    }

    private WeathercamPresetHistoryDtoV1 convertToWeathercamPresetHistory(final String presetId,
                                                                          final List<CameraPresetHistory> history) {

        final AtomicReference<Instant> historyModified = new AtomicReference<>();
        final List<WeathercamPresetHistoryItemDtoV1> historyItems = history.stream()
                .map(h -> convertToWeathercamPresetHistoryItemDtoV1(historyModified, h))
                .collect(Collectors.toList());
        // Sort by preset id, slighly faster than in-stream sort
        historyItems.sort(Comparator.comparing(ph -> ph.lastModified));
        return new WeathercamPresetHistoryDtoV1(presetId, historyModified.get(), historyItems);
    }

    private WeathercamPresetHistoryItemDtoV1 convertToWeathercamPresetHistoryItemDtoV1(
            final AtomicReference<Instant> historyModified,
            final CameraPresetHistory historyItem) {
        historyModified.set(TimeUtil.getGreatest(historyModified.get(), historyItem.getModified()));
        return new WeathercamPresetHistoryItemDtoV1(historyItem.getLastModified(),
                weathercamS3Properties.getPublicUrlForVersion(historyItem.getPresetId(), historyItem.getVersionId()),
                historyItem.getSize());
    }

    private Instant getOldestTimeLimit() {
        return Instant.now().minus(weathercamS3Properties.getHistoryMaxAgeHours(), ChronoUnit.HOURS);
    }

    private static boolean isCameraId(final String cameraId) {
        return cameraId != null && cameraId.length() == 6 && cameraId.startsWith("C");
    }

    private static boolean isPresetId(final String presetId) {
        return presetId != null && presetId.length() == 8 && presetId.startsWith("C");
    }
}
