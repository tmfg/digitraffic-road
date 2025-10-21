package fi.livi.digitraffic.tie.model.data;

import fi.livi.digitraffic.tie.model.trafficmessage.datex2.SituationType;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.TrafficAnnouncementType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.locationtech.jts.geom.Geometry;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "data_datex2_situation")
public class DataDatex2Situation {
    @Id
    @Column(name = "datex2_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long datex2Id;

    private String situationId;
    @Enumerated(EnumType.STRING)
    private SituationType situationType;
    private long situationVersion;

    private ZonedDateTime publicationTime;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;

    private Geometry geometry;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "situation")
    private List<DataDatex2SituationMessage> messages = new ArrayList<>();

    public DataDatex2Situation(final String situationId,
                               final long situationVersion,
                               final SituationType situationType,
                               final Geometry geometry,
                               final ZonedDateTime publicationTime,
                               final ZonedDateTime startTime,
                               final ZonedDateTime endTime) {
        this.situationId = situationId;
        this.situationVersion = situationVersion;
        this.situationType = situationType;
        this.geometry = geometry;
        this.publicationTime = publicationTime;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public DataDatex2Situation() {
    }

    public void addMessage(final DataDatex2SituationMessage message) {
        this.messages.add(message);
        message.setSituation(this);
    }

    public List<DataDatex2SituationMessage> getMessages() {
        return messages;
    }
}
