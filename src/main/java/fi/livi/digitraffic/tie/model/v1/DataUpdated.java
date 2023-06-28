package fi.livi.digitraffic.tie.model.v1;

import java.time.ZonedDateTime;

import jakarta.persistence.*;

import org.hibernate.annotations.DynamicUpdate;

import fi.livi.digitraffic.tie.model.DataType;

/**
 *
 */
@Entity
@DynamicUpdate
public class DataUpdated {
    @Id
    @SequenceGenerator(name = "SEQ_DATA_UPDATED", sequenceName = "SEQ_DATA_UPDATED", allocationSize = 1)
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
