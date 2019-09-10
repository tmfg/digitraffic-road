package fi.livi.digitraffic.tie.data.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.data.dao.DeviceDataRepository;
import fi.livi.digitraffic.tie.data.dao.DeviceRepository;
import fi.livi.digitraffic.tie.data.dto.trafficsigns.TrafficSignHistory;
import fi.livi.digitraffic.tie.data.model.trafficsigns.Device;
import fi.livi.digitraffic.tie.data.model.trafficsigns.DeviceData;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.metadata.geojson.variablesigns.VariableSignFeature;
import fi.livi.digitraffic.tie.metadata.geojson.variablesigns.VariableSignProperties;
import fi.livi.digitraffic.tie.metadata.geojson.variablesigns.VariableSignFeatureCollection;

@Service
public class VariableSignService {
    private final DeviceRepository deviceRepository;
    private final DeviceDataRepository deviceDataRepository;

    public VariableSignService(final DeviceRepository deviceRepository, final DeviceDataRepository deviceDataRepository) {
        this.deviceRepository = deviceRepository;
        this.deviceDataRepository = deviceDataRepository;
    }

    public VariableSignFeatureCollection listLatestValues() {
        final List<Device> devices = deviceRepository.findAll();
        final List<DeviceData> data = deviceDataRepository.findLatestData();
        final Map<String, DeviceData> dataMap = data.stream().collect(Collectors.toMap(DeviceData::getDeviceId, d -> d));

        return new VariableSignFeatureCollection(devices.stream().map(d -> convert(d, dataMap)).collect(Collectors.toList()));
    }

    private VariableSignFeature convert(final Device device, final Map<String, DeviceData> dataMap) {
        final DeviceData data = dataMap.get(device.getId());

        return convert(device, data);
    }

    private VariableSignFeature convert(final Device device, final DeviceData data) {
        final VariableSignProperties properties = new VariableSignProperties(
            device.getId(), device.getType(), device.getRoadAddress(),
            VariableSignProperties.Direction.byValue(device.getDirection()),
            VariableSignProperties.Carriageway.byValue(device.getCarriageway()),
            data == null ? null : data.getDisplayValue(),
            data == null ? null : data.getAdditionalInformation(),
            data == null ? null : data.getEffectDate(),
            data == null ? null : data.getCause());
        final Point point = CoordinateConverter.convertFromETRS89ToWGS84(new Point(device.getEtrsTm35FinX(), device.getEtrsTm35FinY()));

        return new VariableSignFeature(point, properties);
    }

    public List<TrafficSignHistory> listVariableSignHistory(final String deviceId) {
        return deviceDataRepository.getDeviceDataByDeviceIdOrderByEffectDateDesc(deviceId);
    }

    public VariableSignFeatureCollection listLatestValue(final String deviceId) {
        final Optional<Device> device = deviceRepository.findById(deviceId);

        if(device.isPresent()) {
            return new VariableSignFeatureCollection(deviceDataRepository.findLatestData(deviceId).stream()
                .map(d -> convert(device.get(), d))
                .collect(Collectors.toList()));
        }

        throw new ObjectNotFoundException(Device.class, deviceId);
    }
}
