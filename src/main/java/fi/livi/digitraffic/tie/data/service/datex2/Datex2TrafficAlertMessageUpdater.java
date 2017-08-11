package fi.livi.digitraffic.tie.data.service.datex2;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.Datex2Repository;
import fi.livi.digitraffic.tie.data.model.Datex2MessageType;
import fi.livi.digitraffic.tie.data.service.Datex2DataService;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;

@Service
public class Datex2TrafficAlertMessageUpdater {
    private final Datex2Repository datex2Repository;
    private final Datex2TrafficAlertHttpClient datex2TrafficAlertHttpClient;
    private final Datex2DataService datex2DataService;

    private final StringToObjectMarshaller stringToObjectMarshaller;

    @Autowired
    public Datex2TrafficAlertMessageUpdater(final Datex2Repository datex2Repository, final Datex2TrafficAlertHttpClient datex2TrafficAlertHttpClient,
        final Datex2DataService datex2DataService, final StringToObjectMarshaller stringToObjectMarshaller) {
        this.datex2Repository = datex2Repository;
        this.datex2TrafficAlertHttpClient = datex2TrafficAlertHttpClient;
        this.stringToObjectMarshaller = stringToObjectMarshaller;
        this.datex2DataService = datex2DataService;
    }

    @Transactional
    public void updateDatex2TrafficAlertMessages() {
        final LocalDateTime latest = datex2Repository.findLatestImportTime(Datex2MessageType.TRAFFIC_DISORDER.name());
        final Timestamp from = latest == null ? null : Timestamp.valueOf(latest);
        final List<Pair<String, Timestamp>> messages = datex2TrafficAlertHttpClient.getTrafficAlertMessages(from);
        final ArrayList<Datex2MessageDto> unmarshalled = new ArrayList<>();

        for (final Pair<String, Timestamp> message : messages) {
            final D2LogicalModel d2 = stringToObjectMarshaller.convertToObject(message.getLeft());

            unmarshalled.add(new Datex2MessageDto(message.getLeft(),
                                                  ZonedDateTime.ofInstant(message.getRight().toInstant(), ZoneId.systemDefault()),
                                                  d2));
        }
        datex2DataService.updateTrafficAlerts(unmarshalled);
    }
}
