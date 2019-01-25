package fi.livi.digitraffic.tie.metadata.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;


/*
    protected Long asemaId;
    protected String nimi;
    protected Long id;
    protected String luonut;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar luotu;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar muokattu;
    protected String muokkaaja;
* */

@Entity
public class TmsSensorConstant {

    @Id
    @GenericGenerator(name = "SEQ_TMS_SENSOR_CONSTANT", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_ROAD"))
    @GeneratedValue(generator = "SEQ_ROAD")
    private Long id;

    private Long lotjuId;

//    private Long stationLotjuId;

    private String name;

    /**
     * RoadStation is same for multiple constants
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="ROAD_STATION_ID")
    @Fetch(FetchMode.SELECT)
    private RoadStation roadStation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLotjuId() {
        return lotjuId;
    }

    public void setLotjuId(Long lotjuId) {
        this.lotjuId = lotjuId;
    }

//    public Long getStationLotjuId() {
//        return stationLotjuId;
//    }
//
//    public void setStationLotjuId(Long stationLotjuId) {
//        this.stationLotjuId = stationLotjuId;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoadStation getRoadStation() {
        return roadStation;
    }

    public void setRoadStation(RoadStation roadStation) {
        this.roadStation = roadStation;
    }
}
