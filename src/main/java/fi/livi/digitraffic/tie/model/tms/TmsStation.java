package fi.livi.digitraffic.tie.model.tms;

import java.time.LocalDate;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.ReadOnlyCreatedAndModifiedFields;
import fi.livi.digitraffic.tie.model.roadstation.RoadStation;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "TMS_STATION")
@DynamicUpdate
public class TmsStation extends ReadOnlyCreatedAndModifiedFields {
    private static final Logger log = LoggerFactory.getLogger(TmsStation.class);

    @Id
    @SequenceGenerator(name = "SEQ_TMS_STATION", sequenceName = "SEQ_TMS_STATION", allocationSize = 1)
    @GeneratedValue(generator = "SEQ_TMS_STATION")
    private Long id;

    private long naturalId;

    @NotNull
    private Long lotjuId;

    private String name;

    private LocalDate obsoleteDate;

    @Column(name="DIRECTION_1_MUNICIPALITY")
    private String direction1Municipality;

    @Column(name="DIRECTION_1_MUNICIPALITY_CODE")
    private Integer direction1MunicipalityCode;

    @Column(name="DIRECTION_2_MUNICIPALITY")
    private String direction2Municipality;

    @Column(name="DIRECTION_2_MUNICIPALITY_CODE")
    private Integer direction2MunicipalityCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "TMS_STATION_TYPE")
    private TmsStationType tmsStationType;

    @Enumerated(EnumType.STRING)
    private CalculatorDeviceType calculatorDeviceType;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name="road_station_id", nullable = false)
    @Fetch(FetchMode.JOIN)
    private RoadStation roadStation;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public long getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(final long naturalId) {
        this.naturalId = naturalId;
    }

    public Long getLotjuId() {
        return lotjuId;
    }

    public void setLotjuId(final Long lotjuId) {
        this.lotjuId = lotjuId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public LocalDate getObsoleteDate() {
        return obsoleteDate;
    }

    public void setObsoleteDate(final LocalDate obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }

    public RoadStation getRoadStation() {
        return roadStation;
    }

    public void setRoadStation(final RoadStation roadStation) {
        this.roadStation = roadStation;
    }

    /**
     * Makes station obsolete if it's not already
     *
     * @return true is state was changed
     */
    public boolean makeObsolete() {
        if (roadStation == null) {
            log.error("Cannot obsolete TmsStation (" + getId() + ", lotjuId " + getLotjuId() + ") with null roadstation");
            if (getObsoleteDate() == null) {
                setObsoleteDate(LocalDate.now());
                return true;
            }
        } else {
            final boolean changed = roadStation.makeObsolete();
            final LocalDate prevValue = getObsoleteDate();
            setObsoleteDate(roadStation.getObsoleteDate());
            return changed || !prevValue.equals(getObsoleteDate());
        }
        return false;
    }

    public String getDirection1Municipality() {
        return direction1Municipality;
    }

    public void setDirection1Municipality(final String direction1Municipality) {
        this.direction1Municipality = direction1Municipality;
    }

    public Integer getDirection1MunicipalityCode() {
        return direction1MunicipalityCode;
    }

    public void setDirection1MunicipalityCode(final Integer direction1MunicipalityCode) {
        this.direction1MunicipalityCode = direction1MunicipalityCode;
    }

    public String getDirection2Municipality() {
        return direction2Municipality;
    }

    public void setDirection2Municipality(final String direction2Municipality) {
        this.direction2Municipality = direction2Municipality;
    }

    public Integer getDirection2MunicipalityCode() {
        return direction2MunicipalityCode;
    }

    public void setDirection2MunicipalityCode(final Integer direction2MunicipalityCode) {
        this.direction2MunicipalityCode = direction2MunicipalityCode;
    }

    public TmsStationType getTmsStationType() {
        return tmsStationType;
    }

    public void setTmsStationType(final TmsStationType tmsStationType) {
        this.tmsStationType = tmsStationType;
    }

    public void setCalculatorDeviceType(final CalculatorDeviceType calculatorDeviceType) {
        this.calculatorDeviceType = calculatorDeviceType;
    }

    public CalculatorDeviceType getCalculatorDeviceType() {
        return calculatorDeviceType;
    }

    @Override
    public String toString() {
        return new ToStringHelper(this)
                .appendField("id", getId())
                .appendField("lotjuId", this.getLotjuId())
                .appendField("naturalId", getNaturalId())
                .appendField("roadStationId", getRoadStationId())
                .appendField("roadStationNaturalId", getRoadStationNaturalId())
                .toString();
    }

    public Long getRoadStationId() {
        return roadStation != null ? roadStation.getId() : null;
    }

    public Long getRoadStationNaturalId() {
        return roadStation != null ? roadStation.getNaturalId() : null;
    }
}
