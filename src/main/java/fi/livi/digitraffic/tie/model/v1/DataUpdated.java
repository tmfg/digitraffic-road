package fi.livi.digitraffic.tie.model.v1;

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

import fi.livi.digitraffic.tie.model.DataType;

@Entity
@DynamicUpdate
public class DataUpdated {
    @Id
    @GenericGenerator(name = "SEQ_DATA_UPDATED", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_DATA_UPDATED"))
    @GeneratedValue(generator = "SEQ_DATA_UPDATED")
    private Long id;

    @Enumerated(EnumType.STRING)
    private DataType dataType;

    @Column(name = "UPDATED")
    private ZonedDateTime updatedTime;

    /**
     * SubType-field is saved to version-column in database for historical reasons.
     * If this will be changed in future from version to sub_type in db,
     * it will need to be fixed also in cdk-projects and sync installation to test/production.
     */
    @Column(name = "VERSION")
    private String subtype;

    public DataUpdated() {
        // For JPA
    }
}
