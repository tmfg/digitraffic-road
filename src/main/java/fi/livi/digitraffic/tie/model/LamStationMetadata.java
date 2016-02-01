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
    private String nameSv;
    private String nameEn;

    /**
     * Coordinates in format x?
     */
    private long x, y, z;

    private String province;

    public LamStationMetadata() {}

    public long getLamId() {
        return lamId;
    }

    public void setLamId(final long lamId) {
        this.lamId = lamId;
    }

    public String getRwsName() {
        return rwsName;
    }

    public void setRwsName(final String rwsName) {
        this.rwsName = rwsName;
    }

    public String getNameFi() {
        return nameFi;
    }

    public void setNameFi(final String nameFi) {
        this.nameFi = nameFi;
    }

    public String getNameSv() {
        return nameSv;
    }

    public void setNameSv(String nameSv) {
        this.nameSv = nameSv;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameEn() {
        return nameEn;
    }

    public long getX() {
        return x;
    }

    public void setX(final long x) {
        this.x = x;
    }

    public long getY() {
        return y;
    }

    public void setY(final long y) {
        this.y = y;
    }

    public long getZ() {
        return z;
    }

    public void setZ(final long z) {
        this.z = z;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(final String province) {
        this.province = province;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
