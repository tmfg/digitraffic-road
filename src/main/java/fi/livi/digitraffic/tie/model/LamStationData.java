package fi.livi.digitraffic.tie.model;

public class LamStationData {
    private long lamNumber;

    private String rwsName;
    private String name;

    private long x, y, z;

    private String province;

    public LamStationData() {}

    public String getRwsName() {
        return rwsName;
    }

    public void setRwsName(final String rwsName) {
        this.rwsName = rwsName;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
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

    public String getProvince() {
        return province;
    }

    public void setProvince(final String province) {
        this.province = province;
    }

    public long getZ() {
        return z;
    }

    public void setZ(final long z) {
        this.z = z;
    }

    public long getLamNumber() {
        return lamNumber;
    }

    public void setLamNumber(final long lamNumber) {
        this.lamNumber = lamNumber;
    }
}
