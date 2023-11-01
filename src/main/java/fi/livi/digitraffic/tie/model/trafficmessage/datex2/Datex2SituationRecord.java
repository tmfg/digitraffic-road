package fi.livi.digitraffic.tie.model.trafficmessage.datex2;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Schema(name = "SituationRecord", description = "Datex2 situation record")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@DynamicUpdate
@Table(name = "DATEX2_SITUATION_RECORD")
public class Datex2SituationRecord {

    @JsonIgnore
    @Id
    @SequenceGenerator(name = "SEQ_DATEX2SITUATIONRECORD", sequenceName = "SEQ_DATEX2SITUATIONRECORD", allocationSize = 1)
    @GeneratedValue(generator = "SEQ_DATEX2SITUATIONRECORD")
    private Long id;

    @Schema(description = "Record id", required = true)
    @NotNull
    private String situationRecordId;

    @JsonIgnore
    @ManyToOne()
    @JoinColumn(name = "DATEX2_SITUATION_ID", nullable = false)
    private Datex2Situation situation;

    @Schema(description = "Record validy status", required = true)
    @NotNull
    @Enumerated(EnumType.STRING)
    private Datex2SituationRecordValidyStatus validyStatus;

    @Schema(description = "Record creation date time", required = true)
    @NotNull
    private ZonedDateTime creationTime;

    @Schema(description = "Record version date time", required = true)
    @NotNull
    private ZonedDateTime versionTime;

    @Schema(description = "Record observation date time")
    private ZonedDateTime observationTime;

    @Schema(description = "Record overall start date time", required = true)
    @NotNull
    private ZonedDateTime overallStartTime;

    @Schema(description = "Record overall end date time")
    private ZonedDateTime overallEndTime;

    @Schema(description = "Record type", required = true)
    @Enumerated(EnumType.STRING)
    @NotNull
    private Datex2SituationRecordType type;

    @Schema(description = "Record comments", required = true)
    @OneToMany(mappedBy = "situationRecord", cascade = CascadeType.ALL)
    private List<SituationRecordCommentI18n> publicComments;

    @NotNull
    private boolean lifeCycleManagementCanceled;

    @Column(nullable = false, updatable = false, insertable = false) // auto updated
    private Instant effectiveEndTime;

    @Column(nullable = false, updatable = false, insertable = false) // auto generated
    private Instant created;

    @Column(nullable = false, updatable = false, insertable = false) // auto updated
    private Instant modified;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Datex2Situation getSituation() {
        return situation;
    }

    public void setSituation(final Datex2Situation situation) {
        this.situation = situation;
    }

    public String getSituationRecordId() {
        return situationRecordId;
    }

    public void setSituationRecordId(final String situationRecordId) {
        this.situationRecordId = situationRecordId;
    }

    public void setCreationTime(final ZonedDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public ZonedDateTime getCreationTime() {
        return creationTime;
    }

    public void setVersionTime(final ZonedDateTime versionTime) {
        this.versionTime = versionTime;
    }

    public ZonedDateTime getVersionTime() {
        return versionTime;
    }

    public void setObservationTime(final ZonedDateTime observationTime) {
        this.observationTime = observationTime;
    }

    public ZonedDateTime getObservationTime() {
        return observationTime;
    }

    public Datex2SituationRecordValidyStatus getValidyStatus() {
        return validyStatus;
    }

    public void setValidyStatus(final Datex2SituationRecordValidyStatus validyStatus) {
        this.validyStatus = validyStatus;
    }

    public void setOverallStartTime(final ZonedDateTime overallStartTime) {
        this.overallStartTime = overallStartTime;
    }

    public ZonedDateTime getOverallStartTime() {
        return overallStartTime;
    }

    public void setOverallEndTime(final ZonedDateTime overallEndTime) {
        this.overallEndTime = overallEndTime;
    }

    public ZonedDateTime getOverallEndTime() {
        return overallEndTime;
    }

    public Datex2SituationRecordType getType() {
        return type;
    }

    public void setType(final Datex2SituationRecordType type) {
        this.type = type;
    }

    public List<SituationRecordCommentI18n> getPublicComments() {
        return publicComments;
    }

    public void setPublicComments(final List<SituationRecordCommentI18n> publicComments) {
        this.publicComments = publicComments;
        publicComments.forEach(k-> k.setSituationRecord(this));
    }

    public void setLifeCycleManagementCanceled(final boolean lifeCycleManagementCanceled) {
        this.lifeCycleManagementCanceled = lifeCycleManagementCanceled;
    }

    public boolean getLifeCycleManagementCanceled() {
        return lifeCycleManagementCanceled;
    }

    public Instant getEffectiveEndTime() {
        return effectiveEndTime;
    }

//    public void setEffectiveEndTime(final Instant effectiveEndTime) {
//        this.effectiveEndTime = effectiveEndTime;
//    }
}
