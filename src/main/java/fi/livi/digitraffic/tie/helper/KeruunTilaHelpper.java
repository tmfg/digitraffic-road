package fi.livi.digitraffic.tie.helper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import fi.livi.digitraffic.tie.lotju.wsdl.kamera.KeruunTILA;

public class KeruunTilaHelpper {

    // Keruuntila enum is practically same fo all Kamera, TiesaaAsem and Lamasema, but because
    // it's not sure which version code generation uses, it's safer to test enum's string values
    private static final Set<String> POISTETUT =
            new HashSet<>(Arrays.asList(KeruunTILA.POISTETTU_PYSYVASTI.name(),
                                        KeruunTILA.POISTETTU_TILAPAISESTI.name()));

    private KeruunTilaHelpper() {};

    public static boolean isUnactiveKeruunTila(KeruunTILA keruunTila) {
        return POISTETUT.contains(keruunTila.name());
    }
}
