package fi.livi.digitraffic.tie.metadata.model;

import javax.xml.bind.annotation.XmlEnumValue;

import fi.livi.digitraffic.tie.lotju.wsdl.lam.LaiteTyyppi;

public enum CalculatorDeviceType {

    DSL_3("DSL_3"),
    @XmlEnumValue("DSL_4L")
    DSL_4_L("DSL_4L"),
    @XmlEnumValue("DSL_4G")
    DSL_4_G("DSL_4G"),
    DSL_5("DSL_5"),
    MUU("MUU");
    private final String value;

    CalculatorDeviceType(String v) {
        value = v;
    }

    public static CalculatorDeviceType convertFromLaiteTyyppi(LaiteTyyppi laskinlaite) {
        if (laskinlaite != null) {
            return valueOf(laskinlaite.value());
        }
        return null;
    }
}