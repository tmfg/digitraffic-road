package fi.livi.digitraffic.tie.metadata.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.metadata.converter.CameraTypeConverter;

@Entity
@DynamicUpdate
@NamedEntityGraph(name = "camera", attributeNodes = @NamedAttributeNode("roadStation"))
public class CameraPreset {

    @Id
    @GenericGenerator(name = "SEQ_CAMERA_PRESET", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_CAMERA_PRESET"))
    @GeneratedValue(generator = "SEQ_CAMERA_PRESET")
    private long id;

    private String presetId;

    private Long lotjuId;


    /** Old field, this station id ? */
    @Column(name="ROADSTATION_ID")
    private Long roadStationId;
    /** Old field, means weather station? */
    @Column(name="NEAREST_ROADSTATION_ID")
    private Long nearestRoadstationIid;


    /**
     * presetName1 == presentationName == nimiEsitys
     */
    @Column(name="PRESET_NAME_1")
    private String presetName1;

    /**
     * presetName2 == nameOnDevice == nimiLaitteella
     * Not for public use
     */
    @Column(name="PRESET_NAME_2")
    private String presetName2;

    private Integer presetOrder;

    /**
     * Web application's public value
     */
    private boolean publicInternal;

    /**
     * Lotju's Esiasento#isJulkinen()
     */
    private boolean publicExternal;

    private Boolean inCollection;
    private Integer compression;
    private Boolean defaultDirection;
    private String resolution;
    private String direction;

    @Column(name="PIC_LAST_MODIFIED")
    private LocalDateTime pictureLastModified;

    private LocalDate obsoleteDate;

    // Camera properties
    private Long cameraLotjuId;

    private String cameraId;

    @Convert(converter = CameraTypeConverter.class)
    private CameraType cameraType;

    /**
     * RoadStation is same for one camera all presets
     */
    @ManyToOne
    @JoinColumn(name="ROAD_STATION_ID")
    @Fetch(FetchMode.JOIN)
    private RoadStation roadStation;

    @ManyToOne
    @JoinColumn(name="NEAREST_RD_WEATHER_STATION_ID")
    @Fetch(FetchMode.JOIN)
    private WeatherStation nearestWeatherStation;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getCameraId() {
        return cameraId;
    }

    public void setCameraId(final String cameraId) {
        this.cameraId = cameraId;
    }

    public String getPresetId() {
        return presetId;
    }

    public void setPresetId(final String presetId) {
        this.presetId = presetId;
    }

    public Long getLotjuId() {
        return lotjuId;
    }

    public void setLotjuId(final Long lotjuId) {
        this.lotjuId = lotjuId;
    }

    public Long getCameraLotjuId() {
        return cameraLotjuId;
    }

    public void setCameraLotjuId(final Long cameraLotjuId) {
        this.cameraLotjuId = cameraLotjuId;
    }

    public CameraType getCameraType() {
        return cameraType;
    }

    public void setCameraType(final CameraType cameraType) {
        this.cameraType = cameraType;
    }

    public String getPresetName1() {
        return presetName1;
    }

    public void setPresetName1(final String presetName1) {
        this.presetName1 = presetName1;
    }

    public String getPresetName2() {
        return presetName2;
    }

    public void setPresetName2(final String presetName2) {
        this.presetName2 = presetName2;
    }

    public Integer getPresetOrder() {
        return presetOrder;
    }

    public void setPresetOrder(final Integer presetOrder) {
        this.presetOrder = presetOrder;
    }

    /**
     * Lotju's Esiasento#isJulkinen()
     */
    public boolean isPublicExternal() {
        return publicExternal;
    }

    /**
     * Lotju's Esiasento#isJulkinen()
     */
    public void setPublicExternal(final Boolean publicExternal) {
        this.publicExternal = publicExternal;
    }

    public Boolean isInCollection() {
        return inCollection;
    }

    public void setInCollection(final Boolean inCollection) {
        this.inCollection = inCollection;
    }

    public Integer getCompression() {
        return compression;
    }

    public void setCompression(final Integer compression) {
        this.compression = compression;
    }

    public Boolean getDefaultDirection() {
        return defaultDirection;
    }

    public void setDefaultDirection(final Boolean defaultDirection) {
        this.defaultDirection = defaultDirection;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(final String resolution) {
        this.resolution = resolution;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(final String direction) {
        this.direction = direction;
    }

    public RoadStation getRoadStation() {
        return roadStation;
    }

    public void setRoadStation(final RoadStation roadStation) {
        this.roadStation = roadStation;
    }

    public WeatherStation getNearestWeatherStation() {
        return nearestWeatherStation;
    }

    public void setNearestWeatherStation(final WeatherStation nearestWeatherStation) {
        this.nearestWeatherStation = nearestWeatherStation;
    }

    /**
     * @return true if state changed
     */
    public boolean obsolete() {
        if (obsoleteDate == null) {
            obsoleteDate = LocalDate.now();
            return true;
        }
        return false;
    }

    /**
     * Web application's public value
     */
    public boolean isPublicInternal() {
        return publicInternal;
    }

    /**
     * Web application's public value
     */
    public void setPublicInternal(final boolean publicInternal) {
        this.publicInternal = publicInternal;
    }

    public Long getRoadStationId() {
        return roadStation != null ? roadStation.getId() : null;
    }

    public Long getRoadStationNaturalId() {
        return roadStation != null ? roadStation.getNaturalId() : null;
    }

    public Long getNearestWeatherStationNaturalId() {
        return nearestWeatherStation != null ? nearestWeatherStation.getRoadStationNaturalId() : null;
    }

    public LocalDateTime getPictureLastModified() {
        return pictureLastModified;
    }

    public void setPictureLastModified(final LocalDateTime pictureLastModified) {
        this.pictureLastModified = pictureLastModified;
    }

    public LocalDate getObsoleteDate() {
        return obsoleteDate;
    }

    public void setObsoleteDate(final LocalDate obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }

    public boolean isPublic() {
        return isPublicInternal() && isPublicExternal();
    }

    @Override
    public String toString() {
        return new ToStringHelpper(this)
                .appendField("presetId", presetId)
                .appendField("id", id)
                .appendField("cameraId", cameraId)
                .appendField("lotjuId", lotjuId)
                .appendField("roadStationId", getRoadStationId())
                .appendField("roadStationNaturalId", getRoadStationNaturalId())
                .toString();
    }
}
