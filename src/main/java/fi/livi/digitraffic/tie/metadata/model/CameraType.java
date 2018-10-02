package fi.livi.digitraffic.tie.metadata.model;

import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._03._12.KameraTyyppi;

public enum CameraType {

    VAPIX,
    VMX_MPC,
    VMX_MPH,
    D_LINK,
    ZAVIO,
    ENEO,
    BOSCH,
    SONY,
    HIKVISION,
    OLD;

    public static CameraType convertFromKameraTyyppi(final KameraTyyppi kameraTyyppi) {
        if (kameraTyyppi != null) {
            return valueOf(kameraTyyppi.name());
        }
        return null;
    }

}
