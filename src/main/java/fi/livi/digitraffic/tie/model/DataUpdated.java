package fi.livi.digitraffic.tie.model;

import java.time.ZonedDateTime;

import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;

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
