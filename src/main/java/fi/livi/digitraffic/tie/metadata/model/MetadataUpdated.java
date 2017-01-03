package fi.livi.digitraffic.tie.metadata.model;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@DynamicUpdate
public class MetadataUpdated {
    @Id
    @GenericGenerator(name = "SEQ_METAD_UPDATED", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_METAD_UPDATED"))
    @GeneratedValue(generator = "SEQ_METAD_UPDATED")
    private Long id;

    @Enumerated(EnumType.STRING)
    private MetadataType metadataType;

    @Column(name = "UPDATED")
    private ZonedDateTime updatedTime;

    private String version;

    private MetadataUpdated() {
        // Empty for repository
    }

    public MetadataUpdated(final MetadataType type, final ZonedDateTime updatedTime, final String version) {
        setMetadataType(type);
        setUpdatedTime(updatedTime);
        setVersion(version);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MetadataType getMetadataType() {
        return metadataType;
    }

    public void setMetadataType(MetadataType metadataType) {
        this.metadataType = metadataType;
    }

    public ZonedDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(ZonedDateTime updated) {
        this.updatedTime = updated;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
