package fi.livi.digitraffic.tie.model;


import org.apache.log4j.Logger;

public enum CollectionStatus {

    KERUUSSA(fi.livi.digitraffic.tie.wsdl.lam.KeruunTILA.KERUUSSA.value()),
    POISTETTU_TILAPAISESTI(fi.livi.digitraffic.tie.wsdl.lam.KeruunTILA.POISTETTU_TILAPAISESTI.value()),
    POISTETTU_PYSYVASTI(fi.livi.digitraffic.tie.wsdl.lam.KeruunTILA.POISTETTU_PYSYVASTI.value());

    private static final Logger LOG = Logger.getLogger(CollectionStatus.class);

    private final String fiValue;

    CollectionStatus(String fiValue) {
        this.fiValue = fiValue;
    }

    public String getFiValue() {
        return fiValue;
    }

    public static CollectionStatus convertKeruunTila(fi.livi.digitraffic.tie.wsdl.lam.KeruunTILA keruunTila) {
        if (keruunTila != null) {
            return getStatus(keruunTila.value());
        }
        return null;
    }

    public static CollectionStatus convertKeruunTila(fi.livi.digitraffic.tie.wsdl.kamera.KeruunTILA keruunTila) {
        if (keruunTila != null) {
            return getStatus(keruunTila.value());
        }
        return null;
    }

    public static CollectionStatus convertKeruunTila(fi.livi.digitraffic.tie.wsdl.tiesaa.KeruunTILA keruunTila) {
        if (keruunTila != null) {
            return getStatus(keruunTila.value());
        }
        return null;
    }

    private static CollectionStatus getStatus(String fiValue) {
        for (CollectionStatus collectionStatus : CollectionStatus.values()) {
            if (collectionStatus.getFiValue().equals(fiValue)) {
                return collectionStatus;
            }
        }
        LOG.error("CollectionStatus for KeruunTILA " + fiValue + " not found");
        return null;
    }
}
