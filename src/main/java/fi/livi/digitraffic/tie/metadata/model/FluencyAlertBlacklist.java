package fi.livi.digitraffic.tie.metadata.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 * Persistent entity providing access to blacklisted links that produce somehow invalid or unwanted
 * data. These links will not generate alerts on bad link fluency.
 *
 * @author Perttu Taskinen
 */
@Entity
@Table(name = "FLUENCY_ALERT_BLACKLIST")
public class FluencyAlertBlacklist implements Serializable {

    private Long id;
    private boolean blacklisted;
    private Date changed;
    private Link link;

    /**
     * Constructor.
     */
    public FluencyAlertBlacklist() {
    }

    /**
     * Returns the database id of the fluency alert blacklist entity.
     *
     * @return the database id of the fluency alert blacklist entity.
     */
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
                    generator = "FLUENCY_ALERT_BLACKLIST_SEQ")
    @SequenceGenerator(name = "FLUENCY_ALERT_BLACKLIST_SEQ",
                       sequenceName = "seq_fluency_alert_blacklist")
    public Long getId() {
        return id;
    }

    /**
     * Sets the database id of the fluency alert blacklist entity.
     *
     * @param id Id to be set as the database id of the fluency alert blacklist entity.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns true if this entity is on the blacklist.
     *
     * @return true if this entity is on the blacklist.
     */
    @Column(nullable = false)
    public boolean isBlacklisted() {
        return this.blacklisted;
    }

    /**
     * Sets blacklisting state of the entity.
     *
     * @param blacklisted The blacklisting state of the entity. Set to true to blacklist.
     */
    public void setBlacklisted(boolean blacklisted) {
        this.blacklisted = blacklisted;
    }

    /**
     * Returns the date this blacklist entity was changed.
     *
     * @return the date this blacklist entry was changed.
     */
    @Column(nullable = true)
    @Temporal(javax.persistence.TemporalType.DATE)
    public Date getChanged() {
        return this.changed;
    }

    /**
     * Sets the date the blacklist entity was changed.
     *
     * @param changedDate the date the blacklist entity was changed.
     */
    @Column(nullable = false)
    public void setChanged(Date changedDate) {
        this.changed = changedDate;
    }

    /**
     * Returns the link the blacklist entity refers to.
     *
     * @return the link the blacklist entity refers to.
     */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "LINK_ID")
    public Link getLink() {
        return link;
    }

    /**
     * Sets the link the blacklist entity refers to.
     *
     * @param link the link the blacklist entity refers to.
     */
    public void setLink(Link link) {
        this.link = link;
    }
}
