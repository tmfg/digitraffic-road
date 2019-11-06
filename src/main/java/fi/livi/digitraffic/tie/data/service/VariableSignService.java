package fi.livi.digitraffic.tie.data.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.DeviceDataRepository;
import fi.livi.digitraffic.tie.data.dao.DeviceRepository;
import fi.livi.digitraffic.tie.data.dto.trafficsigns.TrafficSignHistory;
import fi.livi.digitraffic.tie.data.model.trafficsigns.Device;
import fi.livi.digitraffic.tie.data.model.trafficsigns.DeviceData;
import fi.livi.digitraffic.tie.metadata.dao.CodeDescriptionRepository;
import fi.livi.digitraffic.tie.metadata.dto.CodeDescriptionJson;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.metadata.geojson.variablesigns.VariableSignFeature;
import fi.livi.digitraffic.tie.metadata.geojson.variablesigns.VariableSignFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.variablesigns.VariableSignProperties;

@Service
public class VariableSignService {
    private final DeviceRepository deviceRepository;
    private final DeviceDataRepository deviceDataRepository;
    private final CodeDescriptionRepository codeDescriptionRepository;

    public VariableSignService(final DeviceRepository deviceRepository, final DeviceDataRepository deviceDataRepository,
        final CodeDescriptionRepository codeDescriptionRepository) {
        this.deviceRepository = deviceRepository;
        this.deviceDataRepository = deviceDataRepository;
        this.codeDescriptionRepository = codeDescriptionRepository;
    }

    @Transactional(readOnly = true)
    public VariableSignFeatureCollection listLatestValues() {
        final Stream<Device> devices = deviceRepository.streamAll();
        final Stream<DeviceData> data = deviceDataRepository.streamLatestData();
        final Map<String, DeviceData> dataMap = data.collect(Collectors.toMap(DeviceData::getDeviceId, d -> d));

        return new VariableSignFeatureCollection(devices.map(d -> convert(d, dataMap)).collect(Collectors.toList()));
    }

    private VariableSignFeature convert(final Device device, final Map<String, DeviceData> dataMap) {
        final DeviceData data = dataMap.get(device.getId());

        return convert(device, data);
    }

    private VariableSignFeature convert(final Device device, final DeviceData data) {
        final VariableSignProperties properties = new VariableSignProperties(
            device.getId(),
            VariableSignProperties.SignType.byValue(device.getType()),
            device.getRoadAddress(),
            VariableSignProperties.Direction.byValue(device.getDirection()),
            VariableSignProperties.Carriageway.byValue(device.getCarriageway()),
            data == null ? null : data.getDisplayValue(),
            data == null ? null : data.getAdditionalInformation(),
            data == null ? null : data.getEffectDate(),
            data == null ? null : data.getCause(),
            data == null ? null : VariableSignProperties.Reliability.byValue(data.getReliability()));
        final Point point = CoordinateConverter.convertFromETRS89ToWGS84(new Point(device.getEtrsTm35FinX(), device.getEtrsTm35FinY()));

        return new VariableSignFeature(point, properties);
    }

    @Transactional(readOnly = true)
    public List<TrafficSignHistory> listVariableSignHistory(final String deviceId) {
        return deviceDataRepository.getDeviceDataByDeviceIdOrderByEffectDateDesc(deviceId);
    }

    @Transactional(readOnly = true)
    public VariableSignFeatureCollection listLatestValue(final String deviceId) {
        final Optional<Device> device = deviceRepository.findById(deviceId);

        if(device.isPresent()) {
            return new VariableSignFeatureCollection(deviceDataRepository.findLatestData(deviceId).stream()
                .map(d -> convert(device.get(), d))
                .collect(Collectors.toList()));
        }

        throw new ObjectNotFoundException(Device.class, deviceId);
    }

    @Transactional(readOnly = true)
    public List<CodeDescriptionJson> listCodeDescriptions() {
        return codeDescriptionRepository.listAllVariableSignTypes();
    }
}
