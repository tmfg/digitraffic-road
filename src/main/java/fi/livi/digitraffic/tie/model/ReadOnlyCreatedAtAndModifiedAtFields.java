package fi.livi.digitraffic.tie.model;

import java.time.Instant;

import jakarta.persistence.MappedSuperclass;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Inherited fields are read only from db. Use for tables with created_at and modified_at columns.
 */
@MappedSuperclass
public abstract class ReadOnlyCreatedAtAndModifiedAtFields implements CreatedAt, ModifiedAt {

    @Generated
    private Instant createdAt;

    @Generated(event = { EventType.INSERT, EventType.UPDATE })
    private Instant modifiedAt;

    @JsonIgnore
    @Override
    public Instant getCreatedAt() {
        return createdAt;
    }

    @JsonIgnore
    @Override
    public Instant getModifiedAt() {
        return modifiedAt;
    }
}
