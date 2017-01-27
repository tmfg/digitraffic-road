package fi.livi.digitraffic.tie.metadata.model;

import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.KameraTyyppi;

public enum CameraType {

    VAPIX,
    VMX_MPC,
    VMX_MPH,
    D_LINK,
    ZAVIO,
    ENEO,
    BOSCH,
    SONY;

    public static CameraType convertFromKameraTyyppi(final KameraTyyppi kameraTyyppi) {
        if (kameraTyyppi != null) {
            return valueOf(kameraTyyppi.name());
        }
        return null;
    }

}
