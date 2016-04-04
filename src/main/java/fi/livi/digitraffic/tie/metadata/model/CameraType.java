package fi.livi.digitraffic.tie.metadata.model;

import fi.livi.digitraffic.tie.wsdl.kamera.KameraTyyppi;

public enum CameraType {

    VAPIX,
    VMX_MPC,
    VMX_MPH,
    D_LINK,
    ZAVIO,
    ENEO;

    public static CameraType convertFromKameraTyyppi(final KameraTyyppi kameraTyyppi) {
        if (kameraTyyppi != null) {
            return valueOf(kameraTyyppi.name());
        }
        return null;
    }

}
