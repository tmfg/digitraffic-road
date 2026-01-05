package fi.livi.digitraffic.tie.conf.jaxb2;

import org.apache.commons.lang3.ObjectUtils;
import org.glassfish.jaxb.runtime.marshaller.NamespacePrefixMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class DatexII_3_NamespacePrefixMapper extends NamespacePrefixMapper {
    private static final Logger log = LoggerFactory.getLogger(DatexII_3_NamespacePrefixMapper.class);

    // https://datex2.eu/schema/3/
    static final Map<String, String> NAMESPACES = Map.ofEntries(
            Map.entry("http://www.w3.org/2001/XMLSchema", "xs"),
            Map.entry("http://datex2.eu/schema/3/cisInformation", "cis"),
            Map.entry("http://datex2.eu/schema/3/common", "com"),
            Map.entry("http://datex2.eu/schema/3/commonExtension", "comx"),
            Map.entry("http://datex2.eu/schema/3/d2Payload", "d2"),
            Map.entry("http://datex2.eu/schema/3/energyInfrastructure", "egi"),
            Map.entry("http://datex2.eu/schema/3/exchangeInformation", "ex"),
            Map.entry("http://datex2.eu/schema/3/facilities", "fac"),
            Map.entry("http://datex2.eu/schema/3/informationManagement", "inf"),
            Map.entry("http://datex2.eu/schema/3/locationExtension", "locx"),
            Map.entry("http://datex2.eu/schema/3/locationReferencing", "loc"),
            Map.entry("http://datex2.eu/schema/3/messageContainer", "con"),
            Map.entry("http://datex2.eu/schema/3/parking", "prk"),
            Map.entry("http://datex2.eu/schema/3/roadTrafficData", "roa"),
            Map.entry("http://datex2.eu/schema/3/situation", "sit"),
            Map.entry("http://datex2.eu/schema/3/trafficRegulation", "tro"),
            Map.entry("http://datex2.eu/schema/3/vms", "vms"),
            Map.entry("http://datex2.eu/schema/3/trafficManagementPlan", "tmp"),
            Map.entry("http://datex2.eu/schema/3/urbanExtensions", "ubx"),
            Map.entry("http://datex2.eu/schema/3/reroutingManagementEnhanced", "rer")
    );

    @Override
    public String getPreferredPrefix(final String namespaceUri, final String suggestion, final boolean requirePrefix) {
        final String maybePrefix = NAMESPACES.get(namespaceUri);
        if (maybePrefix == null && suggestion == null) {
            if (requirePrefix) {
                log.error("method=getPreferredPrefix No namespace prefix found for uri={}", namespaceUri);
            } else {
                log.warn("method=getPreferredPrefix No namespace prefix found for uri={}", namespaceUri);
            }
        }
        return ObjectUtils.firstNonNull(maybePrefix, suggestion);
    }


}
