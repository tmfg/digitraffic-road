package fi.livi.digitraffic.tie.model.trafficmessage.datex2;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Schema(name = "Situation", description = "Datex2 situation")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@DynamicUpdate
@Table(name = "DATEX2_SITUATION")
public class Datex2Situation {

    @JsonIgnore
    @Id
    @SequenceGenerator(name = "SEQ_DATEX2SITUATION", sequenceName = "SEQ_DATEX2SITUATION", allocationSize = 1)
    @GeneratedValue(generator = "SEQ_DATEX2SITUATION")
    private Long id;

    @Schema(description = "Situation id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String situationId;

    @JsonIgnore
    @ManyToOne()
    @JoinColumn(name = "DATEX2_ID", nullable = false)
    private Datex2 datex2;

    @Schema(description = "Situation records", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @OneToMany(mappedBy = "situation", cascade = CascadeType.ALL)
    private List<Datex2SituationRecord> situationRecords;

    @Schema(description = "Situation version date time")
    private ZonedDateTime versionTime;

    public Long getId() {
        return id;
    }

    public List<Datex2SituationRecord> getSituationRecords() {
        return situationRecords;
    }

    public void setSituationRecords(final List<Datex2SituationRecord> situationRecords) {
        this.situationRecords = situationRecords;
    }

    public String getSituationId() {
        return situationId;
    }

    public void setSituationId(final String situationId) {
        this.situationId = situationId;
    }

    public void setVersionTime(final ZonedDateTime versionTime) {
        this.versionTime = versionTime;
    }

    public ZonedDateTime getVersionTime() {
        return versionTime;
    }

    public void addSituationRecord(final Datex2SituationRecord situationRecord) {
        if (situationRecords == null) {
            situationRecords = new ArrayList<>();
        }
        situationRecords.add(situationRecord);
        situationRecord.setSituation(this);
    }

    public Datex2 getDatex2() {
        return datex2;
    }

    public void setDatex2(final Datex2 datex2) {
        this.datex2 = datex2;
    }
}
