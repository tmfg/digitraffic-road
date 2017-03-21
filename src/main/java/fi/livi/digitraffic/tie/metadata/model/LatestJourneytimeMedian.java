package fi.livi.digitraffic.tie.metadata.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;

@Entity
@Table(name = "LATEST_JOURNEYTIME_MEDIAN")
@NamedQueries({ @NamedQuery(name = "LatestJourneytimeMedian.findBelowRatio",
                            query = "select o from LatestJourneytimeMedian o, Link l LEFT JOIN l.fluencyAlertBlacklist f "
                                    + "where o.ratioToFreeFlowSpeed < :ratio "
                                    + "and o.endTimestamp >= :limit "
                                    + "and o.link = l "
                                    + "and l.obsolete = false "
                                    + "and l.special = 0 "
                                    + "and (f.blacklisted = null or f.blacklisted = false) "
                                    + "order by o.fluencyAlertStarted desc"),
                @NamedQuery(name = "LatestJourneytimeMedian.findBelowRatioWithoutTimelimit",
                            query = "select o from LatestJourneytimeMedian o, Link l LEFT JOIN l.fluencyAlertBlacklist f "
                                    + "where o.ratioToFreeFlowSpeed < :ratio "
                                    + "and o.link = l "
                                    + "and l.obsolete = false "
                                    + "and l.special = 0 "
                                    + "and (f.blacklisted = null or f.blacklisted = false) "
                                    + "order by o.fluencyAlertStarted desc") })
public class LatestJourneytimeMedian implements Serializable {

    private BigDecimal averageSpeed;
    private Date endTimestamp;
    private Date fluencyAlertStarted;
    private Long id;
    private Long medianTravelTime;
    private BigDecimal ratioToFreeFlowSpeed;
    private Integer nobs;
    private Link link;

    public LatestJourneytimeMedian() {
    }

    @Column(name = "AVERAGE_SPEED",
            nullable = false)
    public BigDecimal getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(BigDecimal averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    @Column(name = "END_TIMESTAMP",
            nullable = false)
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    public Date getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(Date endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    @Id
    @Column(nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "MEDIAN_TRAVEL_TIME",
            nullable = false)
    public Long getMedianTravelTime() {
        return medianTravelTime;
    }

    public void setMedianTravelTime(Long medianTravelTime) {
        this.medianTravelTime = medianTravelTime;
    }

    @Column(name = "RATIO_TO_FREE_FLOW_SPEED",
            nullable = false)
    public BigDecimal getRatioToFreeFlowSpeed() {
        return ratioToFreeFlowSpeed;
    }

    public void setRatioToFreeFlowSpeed(BigDecimal ratioToFreeFlowSpeed) {
        this.ratioToFreeFlowSpeed = ratioToFreeFlowSpeed;
    }

    @ManyToOne
    @JoinColumn(name = "LINK_ID",
                referencedColumnName = "ID")
    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    @Column(name = "FLUENCY_ALERT_STARTED")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    public Date getFluencyAlertStarted() {
        return fluencyAlertStarted;
    }

    public void setFluencyAlertStarted(Date fluencyAlertStarted) {
        this.fluencyAlertStarted = fluencyAlertStarted;
    }

    public Integer getNobs() {
        return nobs;
    }

    public void setNobs(Integer nobs) {
        this.nobs = nobs;
    }

}
