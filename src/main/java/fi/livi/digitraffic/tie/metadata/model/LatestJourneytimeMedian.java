package fi.livi.digitraffic.tie.metadata.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@Table(name = "LATEST_JOURNEYTIME_MEDIAN")
public class LatestJourneytimeMedian implements Serializable {

    @Id
    @GenericGenerator(name = "SEQ_LATEST_JOURNEYTIME_MEDIAN", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_LATEST_JOURNEYTIME_MEDIAN"))
    @GeneratedValue(generator = "SEQ_LATEST_JOURNEYTIME_MEDIAN")
    private Long id;

    @Column(name = "END_TIMESTAMP", nullable = false)
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date endTimestamp;

    @Column(name = "AVERAGE_SPEED", nullable = false)
    private BigDecimal averageSpeed;

    @Column(name = "MEDIAN_TRAVEL_TIME", nullable = false)
    private Long medianTravelTime;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date fluencyAlertStarted;

    @Column(name = "RATIO_TO_FREE_FLOW_SPEED", nullable = false)
    private BigDecimal ratioToFreeFlowSpeed;

    private Integer nobs;

    @ManyToOne
    @JoinColumn(name = "LINK_ID", referencedColumnName = "ID")
    private Link link;

    public LatestJourneytimeMedian() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(Date endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public BigDecimal getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(BigDecimal averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public Long getMedianTravelTime() {
        return medianTravelTime;
    }

    public void setMedianTravelTime(Long medianTravelTime) {
        this.medianTravelTime = medianTravelTime;
    }

    public Date getFluencyAlertStarted() {
        return fluencyAlertStarted;
    }

    public void setFluencyAlertStarted(Date fluencyAlertStarted) {
        this.fluencyAlertStarted = fluencyAlertStarted;
    }

    public BigDecimal getRatioToFreeFlowSpeed() {
        return ratioToFreeFlowSpeed;
    }

    public void setRatioToFreeFlowSpeed(BigDecimal ratioToFreeFlowSpeed) {
        this.ratioToFreeFlowSpeed = ratioToFreeFlowSpeed;
    }

    public Integer getNobs() {
        return nobs;
    }

    public void setNobs(Integer nobs) {
        this.nobs = nobs;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }
}
