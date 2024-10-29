package fi.livi.digitraffic.tie.model.trafficmessage.datex2;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Schema(name = "SituationRecordGeneralPublicComment", description = "Datex2 situation record general public comment")
@JsonPropertyOrder({ "lang", "value"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@DynamicUpdate
@Table(name = "SITUATION_RECORD_COMMENT_I18N")
public class SituationRecordCommentI18n {

    @JsonIgnore
    @Id
    @SequenceGenerator(name = "SEQ_SITUATION_RECORD_COMMENT", sequenceName = "SEQ_SITUATION_RECORD_COMMENT", allocationSize = 1)
    @GeneratedValue(generator = "SEQ_SITUATION_RECORD_COMMENT")
    private Long id;

    @Schema(description = "Comment language", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Length(min = 2, max = 2)
    private String lang;

    @Schema(description = "Comment value", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String value;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="DATEX2_SITUATION_RECORD_ID", nullable = false)
    @Fetch(FetchMode.JOIN)
    private Datex2SituationRecord situationRecord;

    public SituationRecordCommentI18n() {}

    public SituationRecordCommentI18n(final String lang) {
        this.lang = lang;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(final String lang) {
        this.lang = lang;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public Datex2SituationRecord getSituationRecord() {
        return situationRecord;
    }

    public void setSituationRecord(final Datex2SituationRecord situationRecord) {
        this.situationRecord = situationRecord;
    }

}
