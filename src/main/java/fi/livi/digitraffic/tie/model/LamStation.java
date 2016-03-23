package fi.livi.digitraffic.tie.model;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

import fi.livi.digitraffic.tie.converter.LamStationTypeConverter;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import org.apache.log4j.Logger;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@DynamicUpdate
@NamedEntityGraph(name = "lamStation", attributeNodes = {@NamedAttributeNode("roadStation"), @NamedAttributeNode("roadDistrict")})
public class LamStation implements Stringifiable {

    private static final Logger log = Logger.getLogger(LamStation.class);

    @Id
    @SequenceGenerator(name = "LS_SEQ", sequenceName = "SEQ_LAM_STATION")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LS_SEQ")
    private long id;

    private long naturalId;

    private Long lotjuId;

    private String name;

    private boolean obsolete;

    private LocalDate obsoleteDate;

    @Column(name="summer_free_flow_speed_1")
    private double summerFreeFlowSpeed1;
    @Column(name="summer_free_flow_speed_2")
    private double summerFreeFlowSpeed2;

    @Column(name="winter_free_flow_speed_1")
    private double winterFreeFlowSpeed1;
    @Column(name="winter_free_flow_speed_2")
    private double winterFreeFlowSpeed2;

    @Column(name="DIRECTION_1_MUNICIPALITY")
    private String direction1Municipality;

    @Column(name="DIRECTION_1_MUNICIPALITY_CODE")
    private Integer direction1MunicipalityCode;

    @Column(name="DIRECTION_2_MUNICIPALITY")
    private String direction2Municipality;

    @Column(name="DIRECTION_2_MUNICIPALITY_CODE")
    private Integer direction2MunicipalityCode;

    @Convert(converter = LamStationTypeConverter.class)
    private LamStationType lamStationType;

    @ManyToOne
    @JoinColumn(name="road_district_id", nullable = false)
    @Fetch(FetchMode.JOIN)
    private RoadDistrict roadDistrict;

    @OneToOne
    @JoinColumn(name="road_station_id", nullable = false)
    @Fetch(FetchMode.JOIN)
    private RoadStation roadStation;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
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

    public void setLotjuId(Long lotjuId) {
        this.lotjuId = lotjuId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(final boolean obsolete) {
        this.obsolete = obsolete;
    }

    public LocalDate getObsoleteDate() {
        return obsoleteDate;
    }

    public void setObsoleteDate(final LocalDate obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }

    public double getSummerFreeFlowSpeed1() {
        return summerFreeFlowSpeed1;
    }

    public void setSummerFreeFlowSpeed1(final double summerFreeFlowSpeed1) {
        this.summerFreeFlowSpeed1 = summerFreeFlowSpeed1;
    }

    public double getSummerFreeFlowSpeed2() {
        return summerFreeFlowSpeed2;
    }

    public void setSummerFreeFlowSpeed2(final double summerFreeFlowSpeed2) {
        this.summerFreeFlowSpeed2 = summerFreeFlowSpeed2;
    }

    public double getWinterFreeFlowSpeed1() {
        return winterFreeFlowSpeed1;
    }

    public void setWinterFreeFlowSpeed1(final double winterFreeFlowSpeed1) {
        this.winterFreeFlowSpeed1 = winterFreeFlowSpeed1;
    }

    public RoadStation getRoadStation() {
        return roadStation;
    }

    public void setRoadStation(final RoadStation roadStation) {
        this.roadStation = roadStation;
    }

    /**
     * @return true if state changed
     */
    public boolean obsolete() {
        if (roadStation == null) {
            log.error("Cannot obsolete LamStation (" + getId() + ", lotjuId " + getLotjuId() + ") with null roadstation");
            if (obsoleteDate == null || !obsolete) {
                obsoleteDate = LocalDate.now();
                obsolete = true;
            }
        }
        boolean obsoleted = roadStation.obsolete();
        obsoleteDate = roadStation.getObsoleteDate();
        obsolete = roadStation.isObsolete();
        return obsoleted;
    }

    public double getWinterFreeFlowSpeed2() {
        return winterFreeFlowSpeed2;
    }

    public void setWinterFreeFlowSpeed2(final double winterFreeFlowSpeed2) {
        this.winterFreeFlowSpeed2 = winterFreeFlowSpeed2;
    }

    public RoadDistrict getRoadDistrict() {
        return roadDistrict;
    }

    public void setRoadDistrict(final RoadDistrict roadDistrict) {
        this.roadDistrict = roadDistrict;
    }

    public String getDirection1Municipality() {
        return direction1Municipality;
    }

    public void setDirection1Municipality(String direction1Municipality) {
        this.direction1Municipality = direction1Municipality;
    }

    public Integer getDirection1MunicipalityCode() {
        return direction1MunicipalityCode;
    }

    public void setDirection1MunicipalityCode(Integer direction1MunicipalityCode) {
        this.direction1MunicipalityCode = direction1MunicipalityCode;
    }

    public String getDirection2Municipality() {
        return direction2Municipality;
    }

    public void setDirection2Municipality(String direction2Municipality) {
        this.direction2Municipality = direction2Municipality;
    }

    public Integer getDirection2MunicipalityCode() {
        return direction2MunicipalityCode;
    }

    public void setDirection2MunicipalityCode(Integer direction2MunicipalityCode) {
        this.direction2MunicipalityCode = direction2MunicipalityCode;
    }

    public LamStationType getLamStationType() {
        return lamStationType;
    }

    public void setLamStationType(LamStationType lamStationType) {
        this.lamStationType = lamStationType;
    }

    @Override
    public String toString() {
        return new ToStringHelpper(this)
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