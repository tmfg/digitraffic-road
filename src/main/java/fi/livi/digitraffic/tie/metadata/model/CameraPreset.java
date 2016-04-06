package fi.livi.digitraffic.tie.metadata.model;

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
import javax.persistence.SequenceGenerator;

import org.apache.log4j.Logger;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.metadata.converter.CameraTypeConverter;

@Entity
@DynamicUpdate
@NamedEntityGraph(name = "camera", attributeNodes = @NamedAttributeNode("roadStation"))
public class CameraPreset {
    private static final Logger log = Logger.getLogger(CameraPreset.class);

    @Id
    @SequenceGenerator(name = "SEQ_CAMERA_PRESET", sequenceName = "SEQ_CAMERA_PRESET")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CAMERA_PRESET")
    private long id;

    private String cameraId;

    private String presetId;

    private Long lotjuId;

    private Long lotjuCameraId;

    /** Old field, this station id ? */
    @Column(name="ROADSTATION_ID")
    private Long roadStationId;
    /** Old field, means road weather station? */
    @Column(name="NEAREST_ROADSTATION_ID")
    private Long nearestRoadstationIid;

    @Convert(converter = CameraTypeConverter.class)
    private CameraType cameraType;

    @Column(name="PRESET_NAME_1")
    private String presetName1;

    @Column(name="PRESET_NAME_2")
    private String presetName2;

    private Integer presetOrder;

    private boolean publicInternal;

    private boolean publicExternal;

    private Boolean inCollection;
    private Integer compression;
    private String description;
    private String nameOnDevice;
    private Boolean defaultDirection;
    private String resolution;
    private String direction;
    private Integer delay;

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
    private RoadWeatherStation nearestRoadWeatherStation;

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

    public Long getLotjuCameraId() {
        return lotjuCameraId;
    }

    public void setLotjuCameraId(final Long lotjuCameraId) {
        this.lotjuCameraId = lotjuCameraId;
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

    public boolean isPublicExternal() {
        return publicExternal;
    }

    public void setPublicExternal(final Boolean publicExternal) {
        this.publicExternal = publicExternal;
    }

    public boolean isInCollection() {
        return inCollection;
    }

    public void setInCollection(final boolean inCollection) {
        this.inCollection = inCollection;
    }

    public Integer getCompression() {
        return compression;
    }

    public void setCompression(final Integer compression) {
        this.compression = compression;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getNameOnDevice() {
        return nameOnDevice;
    }

    public void setNameOnDevice(final String nameOnDevice) {
        this.nameOnDevice = nameOnDevice;
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

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(final Integer delay) {
        this.delay = delay;
    }

    public RoadStation getRoadStation() {
        return roadStation;
    }

    public void setRoadStation(final RoadStation roadStation) {
        this.roadStation = roadStation;
    }

    public RoadWeatherStation getNearestRoadWeatherStation() {
        return nearestRoadWeatherStation;
    }

    public void setNearestRoadWeatherStation(final RoadWeatherStation nearestRoadWeatherStation) {
        this.nearestRoadWeatherStation = nearestRoadWeatherStation;
    }

    /**
     * @return true if state changed
     */
    public boolean obsolete() {
        if (roadStation == null) {
            log.error("Cannot obsolete " + toString() + " with null roadstation");
            return false;
        }
        return roadStation.obsolete();
    }

    public boolean isPublicInternal() {
        return publicInternal;
    }

    public void setPublicInternal(final boolean publicInternal) {
        this.publicInternal = publicInternal;
    }

    @Override
    public String toString() {
        return new ToStringHelpper(this)
                .appendField("id", getId())
                .appendField("lotjuId", this.getLotjuId())
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
