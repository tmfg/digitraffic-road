package fi.livi.digitraffic.tie.data.service.datex2;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.Datex2Repository;
import fi.livi.digitraffic.tie.data.model.Datex2MessageType;
import fi.livi.digitraffic.tie.data.service.Datex2UpdateService;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;

@Service
public class Datex2TrafficAlertMessageUpdater {
    private final Datex2Repository datex2Repository;
    private final Datex2TrafficAlertHttpClient datex2TrafficAlertHttpClient;
    private final Datex2UpdateService datex2UpdateService;

    private final StringToObjectMarshaller stringToObjectMarshaller;

    @Autowired
    public Datex2TrafficAlertMessageUpdater(final Datex2Repository datex2Repository, final Datex2TrafficAlertHttpClient datex2TrafficAlertHttpClient,
        final Datex2UpdateService datex2UpdateService, final StringToObjectMarshaller stringToObjectMarshaller) {
        this.datex2Repository = datex2Repository;
        this.datex2TrafficAlertHttpClient = datex2TrafficAlertHttpClient;
        this.datex2UpdateService = datex2UpdateService;
        this.stringToObjectMarshaller = stringToObjectMarshaller;
    }

    @Transactional
    public void updateDatex2TrafficAlertMessages() {
        final Instant latest = datex2Repository.findLatestImportTime(Datex2MessageType.TRAFFIC_DISORDER.name());
        final List<Pair<String, Instant>> messages = datex2TrafficAlertHttpClient.getTrafficAlertMessages(latest);
        final ArrayList<Datex2MessageDto> unmarshalled = new ArrayList<>();

        for (final Pair<String, Instant> message : messages) {
            final D2LogicalModel d2 = stringToObjectMarshaller.convertToObject(message.getLeft());

            unmarshalled.add(new Datex2MessageDto(message.getLeft(), DateHelper.toZonedDateTimeAtUtc(message.getRight()), d2));
        }
        datex2UpdateService.updateTrafficAlerts(unmarshalled);
    }
}
