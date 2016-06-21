package fi.livi.digitraffic.tie.metadata.model;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import fi.livi.digitraffic.tie.lotju.wsdl.kamera.KeruunTILA;

public enum CollectionStatus {

    GATHERING(fi.livi.digitraffic.tie.lotju.wsdl.lam.KeruunTILA.KERUUSSA.value()),
    REMOVED_TEMPORARILY(fi.livi.digitraffic.tie.lotju.wsdl.lam.KeruunTILA.POISTETTU_TILAPAISESTI.value()),
    REMOVED_PERMANENTLY(fi.livi.digitraffic.tie.lotju.wsdl.lam.KeruunTILA.POISTETTU_PYSYVASTI.value());

    private static final Set<String> UNACTIVE_KERUUN_TILAS =
            new HashSet<>(Arrays.asList(KeruunTILA.POISTETTU_PYSYVASTI.name(),
                    KeruunTILA.POISTETTU_TILAPAISESTI.name()));

    private static final Logger LOG = Logger.getLogger(CollectionStatus.class);

    private final String fiValue;

    CollectionStatus(final String fiValue) {
        this.fiValue = fiValue;
    }

    public String getFiValue() {
        return fiValue;
    }

    public static CollectionStatus convertKeruunTila(final fi.livi.digitraffic.tie.lotju.wsdl.lam.KeruunTILA keruunTila) {
        if (keruunTila != null) {
            return getStatus(keruunTila.value());
        }
        return null;
    }

    public static CollectionStatus convertKeruunTila(final fi.livi.digitraffic.tie.lotju.wsdl.kamera.KeruunTILA keruunTila) {
        if (keruunTila != null) {
            return getStatus(keruunTila.value());
        }
        return null;
    }

    public static CollectionStatus convertKeruunTila(final fi.livi.digitraffic.tie.lotju.wsdl.tiesaa.KeruunTILA keruunTila) {
        if (keruunTila != null) {
            return getStatus(keruunTila.value());
        }
        return null;
    }

    private static CollectionStatus getStatus(final String fiValue) {
        for (final CollectionStatus collectionStatus : CollectionStatus.values()) {
            if (collectionStatus.getFiValue().equals(fiValue)) {
                return collectionStatus;
            }
        }
        LOG.error("CollectionStatus for KeruunTILA " + fiValue + " not found");
        return null;
    }

    // Keruuntila enum is practically same fo all Kamera, TiesaaAsem and Lamasema, but because
    // it's not sure which version code generation uses, it's safer to test enum's string values
    public static boolean isUnactiveKeruunTila(KeruunTILA keruunTila) {
        return UNACTIVE_KERUUN_TILAS.contains(keruunTila.name());
    }

    public static boolean isPermanentlyDeletedKeruunTila(final fi.livi.digitraffic.tie.lotju.wsdl.lam.KeruunTILA keruunTila) {
        return KeruunTILA.POISTETTU_PYSYVASTI.name().equals(keruunTila.name());
    }
    public static boolean isPermanentlyDeletedKeruunTila(final fi.livi.digitraffic.tie.lotju.wsdl.kamera.KeruunTILA keruunTila) {
        return KeruunTILA.POISTETTU_PYSYVASTI.name().equals(keruunTila.name());
    }
    public static boolean isPermanentlyDeletedKeruunTila(final fi.livi.digitraffic.tie.lotju.wsdl.tiesaa.KeruunTILA keruunTila) {
        return KeruunTILA.POISTETTU_PYSYVASTI.name().equals(keruunTila.name());
    }

}
