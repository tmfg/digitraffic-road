package fi.livi.digitraffic.tie.service.variablesign.v1;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.dao.variablesign.v1.CodeDescriptionRepositoryV1;
import fi.livi.digitraffic.tie.dao.variablesign.v1.DeviceDataRepositoryV1;
import fi.livi.digitraffic.tie.dao.variablesign.v1.DeviceRepositoryV1;
import fi.livi.digitraffic.tie.dto.v1.VariableSignDescriptions;
import fi.livi.digitraffic.tie.dto.variablesigns.v1.SignTextRowV1;
import fi.livi.digitraffic.tie.dto.variablesigns.v1.TrafficSignHistoryV1;
import fi.livi.digitraffic.tie.dto.variablesigns.v1.VariableSignFeatureCollectionV1;
import fi.livi.digitraffic.tie.dto.variablesigns.v1.VariableSignFeatureV1;
import fi.livi.digitraffic.tie.dto.variablesigns.v1.VariableSignPropertiesV1;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.model.v2.variablesign.Device;
import fi.livi.digitraffic.tie.model.v2.variablesign.DeviceData;
import fi.livi.digitraffic.tie.model.v2.variablesign.DeviceDataRow;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;

@Service
public class VariableSignDataServiceV1 {
    private final DeviceRepositoryV1 deviceRepositoryV1;
    private final DeviceDataRepositoryV1 deviceDataRepositoryV1;
    private final CodeDescriptionRepositoryV1 codeDescriptionRepositoryV1;

    public VariableSignDataServiceV1(final DeviceRepositoryV1 deviceRepositoryV1, final DeviceDataRepositoryV1 deviceDataRepositoryV1,
                                     final CodeDescriptionRepositoryV1 codeDescriptionRepositoryV1) {
        this.deviceRepositoryV1 = deviceRepositoryV1;
        this.deviceDataRepositoryV1 = deviceDataRepositoryV1;
        this.codeDescriptionRepositoryV1 = codeDescriptionRepositoryV1;
    }

    @PerformanceMonitor(maxInfoExcecutionTime = 100000, maxWarnExcecutionTime = 3000)
    @Transactional(readOnly = true)
    public VariableSignFeatureCollectionV1 listLatestValues() {
        final List<Device> devices = deviceRepositoryV1.findAllByDeletedDateIsNull();
        final List<Long> dataIds = deviceDataRepositoryV1.findLatestData();
        final List<DeviceData> data = deviceDataRepositoryV1.findDistinctByIdIn(dataIds);
        final Instant dataLastUpdated = getDataLastUpdated(data);

        final Map<String, DeviceData> dataMap = data.stream().collect(Collectors.toMap(DeviceData::getDeviceId, d -> d));
        final List<VariableSignFeatureV1> features = devices.stream()
            .map(d -> convert(d, dataMap))
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableList());

        return new VariableSignFeatureCollectionV1(dataLastUpdated, features);
    }

    private VariableSignFeatureV1 convert(final Device device, final Map<String, DeviceData> dataMap) {
        final DeviceData data = dataMap.get(device.getId());

        return data == null ? null : convert(device, data);
    }

    private VariableSignFeatureV1 convert(final Device device, final DeviceData data) {
        final List<SignTextRowV1> textRows = CollectionUtils.isEmpty(data.getRows()) ? Collections.emptyList() : convert(data.getRows());

        final VariableSignPropertiesV1 properties = new VariableSignPropertiesV1(
            device.getId(),
            VariableSignPropertiesV1.SignType.byValue(device.getType()),
            device.getRoadAddress(),
            VariableSignPropertiesV1.Direction.byValue(device.getDirection()),
            VariableSignPropertiesV1.Carriageway.byValue(device.getCarriageway()),
            data.getDisplayValue(),
            data.getAdditionalInformation(),
            data.getEffectDate(),
            data.getCause(),
            VariableSignPropertiesV1.Reliability.byValue(data.getReliability()),
            textRows,
            data.getModified());
        final Point point = CoordinateConverter.convertFromETRS89ToWGS84(new Point(device.getEtrsTm35FinX(), device.getEtrsTm35FinY()));

        return new VariableSignFeatureV1(point, properties);
    }

    private List<SignTextRowV1> convert(final List<DeviceDataRow> textRows) {
        return textRows.stream()
            .map(r -> new SignTextRowV1(r.getScreen(), r.getRowNumber(), r.getText()))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TrafficSignHistoryV1> listVariableSignHistory(final String deviceId) {
        return deviceDataRepositoryV1.getDeviceDataByDeviceIdOrderByEffectDateDesc(deviceId);
    }

    @Transactional(readOnly = true)
    public VariableSignFeatureCollectionV1 listLatestValue(final String deviceId) {
        final Optional<Device> device = deviceRepositoryV1.findById(deviceId);

        if(device.isPresent()) {

            final Instant deviceLastUpdated = device.get().getModified();
            final List<Long> latest = deviceDataRepositoryV1.findLatestData(deviceId);
            final List<DeviceData> data = deviceDataRepositoryV1.findDistinctByIdIn(latest);
            final Instant dataLastUpdated = getDataLastUpdated(data, deviceLastUpdated);

            return new VariableSignFeatureCollectionV1(
                dataLastUpdated,
                data.stream()
                    .map(d -> convert(device.get(), d))
                    .collect(Collectors.toList()));
        }

        throw new ObjectNotFoundException(Device.class, deviceId);
    }

    private Instant getDataLastUpdated(final List<DeviceData> data) {
        return getDataLastUpdated(data, null);
    }

    private Instant getDataLastUpdated(final List<DeviceData> data, final Instant deviceLastUpdated) {
        return DateHelper.getGreatest(
            data.stream().map(DeviceData::getCreated).max(Comparator.naturalOrder()).orElse(Instant.EPOCH),
            deviceLastUpdated);
    }

    @Transactional(readOnly = true)
    public VariableSignDescriptions getCodeDescriptions() {
        final Instant lastModified = codeDescriptionRepositoryV1.getLastUpdated();
        return new VariableSignDescriptions(codeDescriptionRepositoryV1.listAllVariableSignTypes(), lastModified);
    }
}
