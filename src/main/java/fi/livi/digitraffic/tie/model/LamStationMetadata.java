package fi.livi.digitraffic.tie.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
public class LamStationMetadata {

    /**
     * lamId = natural_id
     */
    @Id
    private long lamId;

    /**
     * RWS_NAME = TSA_NIMI
     */
    private String rwsName;

    private String nameFi;
    private String nameSe;
    private String nameEn;

    /**
     * Coordinates in format x?
     */
    private long latitude, longitude, elevation;

    private String province;

    public long getLamId() {
        return lamId;
    }

    public void setLamId(final long lamId) {
        this.lamId = lamId;
    }

    public String getNameFi() {
        return nameFi;
    }

    public void setNameFi(final String nameFi) {
        this.nameFi = nameFi;
    }

    public String getNameSe() {
        return nameSe;
    }

    public void setNameSe(final String nameSe) {
        this.nameSe = nameSe;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameEn() {
        return nameEn;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public String getRwsName() {
        return rwsName;
    }

    public void setRwsName(final String rwsName) {
        this.rwsName = rwsName;
    }

    public long getLatitude() {
        return latitude;
    }

    public void setLatitude(final long latitude) {
        this.latitude = latitude;
    }

    public long getLongitude() {
        return longitude;
    }

    public void setLongitude(final long longitude) {
        this.longitude = longitude;
    }

    public long getElevation() {
        return elevation;
    }

    public void setElevation(final long elevation) {
        this.elevation = elevation;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(final String province) {
        this.province = province;
    }
}
