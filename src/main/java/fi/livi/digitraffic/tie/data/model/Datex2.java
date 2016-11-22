package fi.livi.digitraffic.tie.data.model;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Datex2", description = "Traffic disorders")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@DynamicUpdate
@Table(name = "DATEX2")
public class Datex2 {

    @JsonIgnore
    @Id
    @GenericGenerator(name = "SEQ_DATEX2", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_DATEX2"))
    @GeneratedValue(generator = "SEQ_DATEX2")
    private Long id;

    @ApiModelProperty(value = "Message import " + ToStringHelpper.ISO_8601_OFFSET_TIMESTAMP_EXAMPLE)
    @Column(name = "IMPORT_DATE")
    private ZonedDateTime importTime;

    @ApiModelProperty(value = "Datex2 message")
    @JsonProperty("datex2")
    private String message;

    @ApiModelProperty(value = "Message publication " + ToStringHelpper.ISO_8601_OFFSET_TIMESTAMP_EXAMPLE)
    private ZonedDateTime publicationTime;

    @ApiModelProperty(value = "Date2 situations")
    @OneToMany(mappedBy = "datex2", cascade = CascadeType.ALL)
    private List<Datex2Situation> situations;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Datex2 xml message", required = true)
    @JsonProperty(value = "datex2", required = true)
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ZonedDateTime getImportTime() {
        return importTime;
    }

    public void setImportTime(ZonedDateTime importTime) {
        this.importTime = importTime;
    }

    public ZonedDateTime getPublicationTime() {
        return publicationTime;
    }


    public void setPublicationTime(ZonedDateTime publicationTime) {
        this.publicationTime = publicationTime;
    }

    public List<Datex2Situation> getSituations() {
        return situations;
    }

    public void setSituations(List<Datex2Situation> situations) {
        this.situations = situations;
    }

    public void addSituation(Datex2Situation situation) {
        if (this.situations == null) {
            this.situations = new ArrayList<>();
        }
        situations.add(situation);
        situation.setDatex2(this);
    }
}
