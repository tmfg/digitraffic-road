package fi.livi.digitraffic.tie.model.v1;

import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import fi.livi.digitraffic.tie.model.DataType;

/**
 *
 */
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

    /** Time of the update */
    private ZonedDateTime updated;

    /** Possible subtype of the data type */
    private String subtype;

    public DataUpdated() {
        // For JPA
    }
}
