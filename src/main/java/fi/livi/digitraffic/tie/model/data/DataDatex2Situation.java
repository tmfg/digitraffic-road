package fi.livi.digitraffic.tie.model.data;

import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.TrafficAnnouncementProperties;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "data_datex2_situation")
public class DataDatex2Situation {
    @Id
    @Column(name = "datex2_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long datex2Id;

    private String situationId;
    @Enumerated(EnumType.STRING)
    private TrafficAnnouncementProperties.SituationType situationType;
    private long situationVersion;

    private Instant publicationTime;
    private Instant startTime;
    private Instant endTime;

    private Geometry geometry;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "situation")
    private List<DataDatex2SituationMessage> messages = new ArrayList<>();

    public DataDatex2Situation(final String situationId,
                               final long situationVersion,
                               final TrafficAnnouncementProperties.SituationType situationType,
                               final Geometry geometry,
                               final Instant publicationTime,
                               final Instant startTime,
                               final Instant endTime) {
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
