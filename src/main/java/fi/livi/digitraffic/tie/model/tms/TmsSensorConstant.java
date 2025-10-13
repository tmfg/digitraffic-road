package fi.livi.digitraffic.tie.model.tms;

import java.time.LocalDate;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.roadstation.RoadStation;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

///  this is used only by tests
@Entity
public class TmsSensorConstant {

    @Id
    private Long lotjuId;

    @NotNull
    private String name;

//    @NotNull
    private LocalDate modified;

    private LocalDate obsoleteDate;
    /**
     * RoadStation is same for multiple constants
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="ROAD_STATION_ID")
    @Fetch(FetchMode.SELECT)
    private RoadStation roadStation;

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

    public LocalDate getModified() {
        return modified;
    }

    public LocalDate getObsoleteDate() {
        return obsoleteDate;
    }

    public RoadStation getRoadStation() {
        return roadStation;
    }

    public void setRoadStation(final RoadStation roadStation) {
        this.roadStation = roadStation;
    }

    @Override
    public String toString() {
        return ToStringHelper.toString(this);
    }
}
