package fi.livi.digitraffic.tie.service.lotju;

public class LotjuAnturiWrapper<T> {

    private final T anturi;
    private final long anturiLotjuId;
    private final long aika;
    private final long asemaLotjuId;
    private final long roadStationId;

    public LotjuAnturiWrapper(final long asemaLotjuId, final long anturiLotjuId, final T anturi, final long aika, final long roadStationId) {
        this.asemaLotjuId = asemaLotjuId;
        this.anturiLotjuId = anturiLotjuId;
        this.anturi = anturi;
        this.aika = aika;
        this.roadStationId = roadStationId;
    }

    public long getAsemaLotjuId() {
        return asemaLotjuId;
    }

    public long getAnturiLotjuId() {
        return anturiLotjuId;
    }

    public T getAnturi() {
        return anturi;
    }

    public long getAika() {
        return aika;
    }

    public long getRoadStationId() { return  roadStationId; }
}