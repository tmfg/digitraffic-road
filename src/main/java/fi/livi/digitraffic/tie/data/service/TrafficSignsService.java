package fi.livi.digitraffic.tie.data.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.data.dao.DeviceRepository;
import fi.livi.digitraffic.tie.data.dto.trafficsigns.*;
import fi.livi.digitraffic.tie.data.model.trafficsigns.*;

@Service
public class TrafficSignsService {
    private static final Logger log = LoggerFactory.getLogger(TrafficSignsService.class);

    private final DeviceRepository deviceRepository;

    public TrafficSignsService(final DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public void saveMetadata(final MetadataSchema metadata) {
        final Map<String, DeviceMetadataSchema> idMap = metadata.laitteet.stream().collect(Collectors.toMap(l -> l.tunnus, l -> l));

        final List<Device> devices = deviceRepository.findAllById(idMap.keySet());

        log.debug(idMap.toString());
    }

    public void saveData(final DataSchema data) {

    }
}
