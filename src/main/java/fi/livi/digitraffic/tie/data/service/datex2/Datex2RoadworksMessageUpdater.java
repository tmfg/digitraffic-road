package fi.livi.digitraffic.tie.data.service.datex2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.service.Datex2DataService;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;

@Service
public class Datex2RoadworksMessageUpdater {
    private final Datex2RoadworksHttpClient datex2RoadworksHttpClient;
    private final Datex2DataService datex2DataService;

    private final StringToObjectMarshaller stringToObjectMarshaller;

    @Autowired
    public Datex2RoadworksMessageUpdater(final Datex2RoadworksHttpClient datex2RoadworksHttpClient,
        final Datex2DataService datex2DataService, final StringToObjectMarshaller stringToObjectMarshaller) {
        this.datex2RoadworksHttpClient = datex2RoadworksHttpClient;
        this.stringToObjectMarshaller = stringToObjectMarshaller;
        this.datex2DataService = datex2DataService;
    }

    @Transactional
    public void updateDatex2RoadworksMessages() {
        final String message = datex2RoadworksHttpClient.getRoadWorksMessage();

        datex2DataService.updateRoadworks(convert(message));
    }

    private Datex2MessageDto convert(final String message) {
        final D2LogicalModel model = stringToObjectMarshaller.convertToObject(message);

        return new Datex2MessageDto(message, null, model);
    }
}
