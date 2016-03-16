package fi.livi.digitraffic.tie.model;

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

import fi.livi.digitraffic.tie.converter.CameraTypeConverter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@DynamicUpdate
@NamedEntityGraph(name = "camera", attributeNodes = @NamedAttributeNode("roadStation"))
public class CameraPreset {

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

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public String getPresetId() {
        return presetId;
    }

    public void setPresetId(String presetId) {
        this.presetId = presetId;
    }

    public Long getLotjuId() {
        return lotjuId;
    }

    public void setLotjuId(Long lotjuId) {
        this.lotjuId = lotjuId;
    }

    public Long getLotjuCameraId() {
        return lotjuCameraId;
    }

    public void setLotjuCameraId(Long lotjuCameraId) {
        this.lotjuCameraId = lotjuCameraId;
    }

    public CameraType getCameraType() {
        return cameraType;
    }

    public void setCameraType(CameraType cameraType) {
        this.cameraType = cameraType;
    }

    public String getPresetName1() {
        return presetName1;
    }

    public void setPresetName1(String presetName1) {
        this.presetName1 = presetName1;
    }

    public String getPresetName2() {
        return presetName2;
    }

    public void setPresetName2(String presetName2) {
        this.presetName2 = presetName2;
    }

    public Integer getPresetOrder() {
        return presetOrder;
    }

    public void setPresetOrder(Integer presetOrder) {
        this.presetOrder = presetOrder;
    }

    public boolean isPublicExternal() {
        return publicExternal;
    }

    public void setPublicExternal(Boolean publicExternal) {
        this.publicExternal = publicExternal;
    }

    public boolean isInCollection() {
        return inCollection;
    }

    public void setInCollection(boolean inCollection) {
        this.inCollection = inCollection;
    }

    public Integer getCompression() {
        return compression;
    }

    public void setCompression(Integer compression) {
        this.compression = compression;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNameOnDevice() {
        return nameOnDevice;
    }

    public void setNameOnDevice(String nameOnDevice) {
        this.nameOnDevice = nameOnDevice;
    }

    public Boolean getDefaultDirection() {
        return defaultDirection;
    }

    public void setDefaultDirection(Boolean defaultDirection) {
        this.defaultDirection = defaultDirection;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
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

    public void setNearestRoadWeatherStation(RoadWeatherStation nearestRoadWeatherStation) {
        this.nearestRoadWeatherStation = nearestRoadWeatherStation;
    }

    public void obsolete() {
        if (roadStation != null) {
            roadStation.obsolete();
        }
    }

    public boolean isPublicInternal() {
        return publicInternal;
    }

    public void setPublicInternal(boolean publicInternal) {
        this.publicInternal = publicInternal;
    }
}
