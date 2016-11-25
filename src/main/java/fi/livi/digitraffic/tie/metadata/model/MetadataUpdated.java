package fi.livi.digitraffic.tie.metadata.model;

import java.time.ZonedDateTime;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import fi.livi.digitraffic.tie.metadata.converter.MetadataTypeConverter;

@Entity
@Table(name = "METADATA_UPDATED")
@DynamicUpdate
public class MetadataUpdated {

    @Id
    @GenericGenerator(name = "SEQ_METAD_UPDATED", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_METAD_UPDATED"))
    @GeneratedValue(generator = "SEQ_METAD_UPDATED")
    private Long id;

    @Convert(converter = MetadataTypeConverter.class)
    private MetadataType metadataType;

    private ZonedDateTime updated;

    private MetadataUpdated() {
        // Empty for repository
    }

    public MetadataUpdated(MetadataType type, ZonedDateTime updated) {
        setMetadataType(type);
        setUpdated(updated);
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

    public ZonedDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(ZonedDateTime updated) {
        this.updated = updated;
    }
}
