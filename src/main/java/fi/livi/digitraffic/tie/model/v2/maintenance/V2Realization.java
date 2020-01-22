package fi.livi.digitraffic.tie.model.v2.maintenance;

import java.time.ZonedDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@Entity
@Table(name = "V2_REALIZATION")
public class V2Realization {

    @Id
    @GenericGenerator(name = "SEQ_V2_REALIZATION", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_V2_REALIZATION"))
    @GeneratedValue(generator = "SEQ_V2_REALIZATION")
    private Long id;

    @Column
    private Long jobId;

    @Column
    private String sendingSystem;

    @Column
    private Integer messageId;

    @Column
    private ZonedDateTime sendingTime;

    @Column(insertable = false, updatable = false) // auto generated
    private ZonedDateTime created;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="REALIZATION_DATA_ID", referencedColumnName = "ID", nullable = false, updatable = false)
    private V2RealizationData realizationData;

    @OneToMany(mappedBy = "realization", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderColumn(name = "ORDER_NUMBER", nullable = false, updatable = false)
    private List<V2RealizationPoint> realizationPoints;

    public V2Realization() {
        // For Hibernate
    }

    public V2Realization(final V2RealizationData wmrd, final String sendingSystem, final Integer messageId, final ZonedDateTime sendingTime) {
        this.realizationData = wmrd;
        this.sendingSystem = sendingSystem;
        this.messageId = messageId;
        this.sendingTime = sendingTime;
        this.jobId = wmrd.getJobId();
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
