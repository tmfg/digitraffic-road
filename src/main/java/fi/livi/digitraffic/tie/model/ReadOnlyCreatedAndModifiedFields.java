package fi.livi.digitraffic.tie.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Inherited fields are read only from db
 */
@MappedSuperclass
public abstract class ReadOnlyCreatedAndModifiedFields {

    @Column(nullable = false, updatable = false, insertable = false)
    private Instant created;

    @Column(nullable = false, updatable = false, insertable = false)
    private Instant modified;

    public Instant getCreated() {
        return created;
    }

    public Instant getModified() {
        return modified;
    }
}
