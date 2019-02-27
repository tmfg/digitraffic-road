package fi.livi.digitraffic.tie.data.service;

public class LotjuAnturiWrapper<T> {

    private final T anturi;
    private final long aika;
    private final long asemaId;

    public LotjuAnturiWrapper(final T anturi, final long aika, final long asemaLotjuId) {
        this.anturi = anturi;
        this.aika = aika;
        this.asemaId = asemaLotjuId;
    }

    public T getAnturi() {
        return anturi;
    }

    public long getAika() {
        return aika;
    }

    public long getAsemaLotjuId() {
        return asemaId;
    }
}