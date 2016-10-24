package fi.livi.digitraffic.tie.metadata.model;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.util.Date;

@Entity
public class RoadSection {

    @Id
    @GenericGenerator(name = "SEQ_ROAD_SECTION", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = @Parameter(name = "sequence_name", value = "SEQ_ROAD_SECTION"))
    @GeneratedValue(generator = "SEQ_ROAD_SECTION")
    private Long id;

    private String naturalId;

    private boolean obsolete;

    private Integer beginDistance;

    private Integer endDistance;

    @ManyToOne
    @JoinColumn(name = "road_district_id")
    @Fetch(FetchMode.JOIN)
    private RoadDistrict roadDistrict;

    @ManyToOne
    @JoinColumn(name = "road_id")
    @Fetch(FetchMode.JOIN)
    private Road road;

    private Date obsoleteDate;

    public RoadSection() {
    }

    public String getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(String naturalId) {
        this.naturalId = naturalId;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    public Integer getBeginDistance() {
        return beginDistance;
    }

    public void setBeginDistance(Integer beginDistance) {
        this.beginDistance = beginDistance;
    }

    public Integer getEndDistance() {
        return endDistance;
    }

    public void setEndDistance(Integer endDistance) {
        this.endDistance = endDistance;
    }

    public RoadDistrict getRoadDistrict() {
        return roadDistrict;
    }

    public void setRoadDistrict(RoadDistrict roadDistrict) {
        this.roadDistrict = roadDistrict;
    }

    public Road getRoad() {
        return road;
    }

    public void setRoad(Road road) {
        this.road = road;
    }

    public Date getObsoleteDate() {
        return obsoleteDate;
    }

    public void setObsoleteDate(Date obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }
}
