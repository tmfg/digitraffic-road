package fi.livi.digitraffic.tie.model.trafficmessage.datex2;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;

import fi.livi.digitraffic.tie.model.ReadOnlyCreatedAndModifiedFields;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@DynamicUpdate
@Table(name = "DATEX2")
public class Datex2 extends ReadOnlyCreatedAndModifiedFields {

    @Id
    @SequenceGenerator(name = "SEQ_DATEX2", sequenceName = "SEQ_DATEX2", allocationSize = 1)
    @GeneratedValue(generator = "SEQ_DATEX2")
    private Long id;

    @Column(name = "IMPORT_DATE")
    @NotNull
    private Instant importTime;

    @NotNull
    private String message;

    @NotNull
    @Enumerated(EnumType.STRING)
    private SituationType situationType;

    // Only for SituationType.TRAFFIC_ANNOUNCEMENT
    @Enumerated(EnumType.STRING)
    private TrafficAnnouncementType trafficAnnouncementType;

    private Instant publicationTime;

    @OneToMany(mappedBy = "datex2", cascade = CascadeType.ALL)
    private List<Datex2Situation> situations;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private String jsonMessage;

    /** If geometry is not valid the original jsonMessage will be saved here and fixed version to jsonMessage-field */
    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private String originalJsonMessage;

    public Datex2() {
        // For JPA
    }

    public Datex2(final SituationType situationType, final TrafficAnnouncementType trafficAnnouncementType) {
        this.situationType = situationType;
        this.trafficAnnouncementType = trafficAnnouncementType;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public Instant getImportTime() {
        return importTime;
    }

    public void setImportTime(final Instant importTime) {
        this.importTime = importTime;
    }

    public Instant getPublicationTime() {
        return publicationTime;
    }


    public void setPublicationTime(final Instant publicationTime) {
        this.publicationTime = publicationTime;
    }

    public List<Datex2Situation> getSituations() {
        return situations;
    }

    public void setSituations(final List<Datex2Situation> situations) {
        this.situations = situations;
    }

    public void addSituation(final Datex2Situation situation) {
        if (this.situations == null) {
            this.situations = new ArrayList<>();
        }
        situations.add(situation);
        situation.setDatex2(this);
    }

    public void setJsonMessage(final String jsonMessage) {
        this.jsonMessage = jsonMessage;
    }

    public String getJsonMessage() {
        return jsonMessage;
    }

    public SituationType getSituationType() {
        return situationType;
    }

    public TrafficAnnouncementType getTrafficAnnouncementType() {
        return trafficAnnouncementType;
    }

    public void setTrafficAnnouncementType(final TrafficAnnouncementType trafficAnnouncementType) {
        this.trafficAnnouncementType = trafficAnnouncementType;
    }

    public void setOriginalJsonMessage(final String originalJsonMessage) {
        this.originalJsonMessage = originalJsonMessage;
    }
}
