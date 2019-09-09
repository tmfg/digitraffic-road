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
import fi.livi.digitraffic.tie.metadata.geojson.trafficsigns.TrafficSignFeature;
import fi.livi.digitraffic.tie.metadata.geojson.trafficsigns.TrafficSignProperties;
import fi.livi.digitraffic.tie.metadata.geojson.trafficsigns.TrafficSignsFeatureCollection;

@Service
public class TrafficSignsService {
    private final DeviceRepository deviceRepository;
    private final DeviceDataRepository deviceDataRepository;

    public TrafficSignsService(final DeviceRepository deviceRepository, final DeviceDataRepository deviceDataRepository) {
        this.deviceRepository = deviceRepository;
        this.deviceDataRepository = deviceDataRepository;
    }

    public TrafficSignsFeatureCollection listLatestValues() {
        final List<Device> devices = deviceRepository.findAll();
        final List<DeviceData> data = deviceDataRepository.findLatestData();
        final Map<String, DeviceData> dataMap = data.stream().collect(Collectors.toMap(DeviceData::getDeviceId, d -> d));

        return new TrafficSignsFeatureCollection(devices.stream().map(d -> convert(d, dataMap)).collect(Collectors.toList()));
    }

    private TrafficSignFeature convert(final Device device, final Map<String, DeviceData> dataMap) {
        final DeviceData data = dataMap.get(device.getId());

        return convert(device, data);
    }

    private TrafficSignFeature convert(final Device device, final DeviceData data) {
        final TrafficSignProperties properties = new TrafficSignProperties(
            device.getId(), device.getType(), device.getRoadAddress(),
            device.getDirection(),
            device.getCarriageway(),
            data == null ? null : data.getDisplayValue(),
            data == null ? null : data.getAdditionalInformation(),
            data == null ? null : data.getEffectDate(),
            data == null ? null : data.getCause());
        final Point point = CoordinateConverter.convertFromETRS89ToWGS84(new Point(device.getEtrsTm35FinX(), device.getEtrsTm35FinY()));

        return new TrafficSignFeature(point, properties);
    }

    public List<TrafficSignHistory> listTrafficSignHistory(final String deviceId) {
        return deviceDataRepository.getDeviceDataByDeviceIdOrderByEffectDateDesc(deviceId);
    }

    public TrafficSignsFeatureCollection listLatestValue(final String deviceId) {
        final Optional<Device> device = deviceRepository.findById(deviceId);

        if(device.isPresent()) {
            return new TrafficSignsFeatureCollection(deviceDataRepository.findLatestData(deviceId).stream()
                .map(d -> convert(device.get(), d))
                .collect(Collectors.toList()));
        }

        throw new ObjectNotFoundException(Device.class, deviceId);
    }
}
