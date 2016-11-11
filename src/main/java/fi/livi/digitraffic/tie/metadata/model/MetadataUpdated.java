package fi.livi.digitraffic.tie.metadata.model;

import java.time.LocalDateTime;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import fi.livi.digitraffic.tie.metadata.converter.MetadataTypeConverter;

@Entity
@DynamicUpdate
public class MetadataUpdated {
    @Id
    @GenericGenerator(name = "SEQ_METAD_UPDATED", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_METAD_UPDATED"))
    @GeneratedValue(generator = "SEQ_METAD_UPDATED")
    private Long id;

    @Convert(converter = MetadataTypeConverter.class)
    private MetadataType metadataType;

    private LocalDateTime updated;

    private String version;

    private MetadataUpdated() {
        // Empty for repository
    }

    public MetadataUpdated(final MetadataType type, final LocalDateTime updated, final String version) {
        setMetadataType(type);
        setUpdated(updated);
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

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
