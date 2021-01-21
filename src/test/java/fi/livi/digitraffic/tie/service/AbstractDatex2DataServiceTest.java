package fi.livi.digitraffic.tie.service;

import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.conf.jaxb2.XmlMarshallerConfiguration;
import fi.livi.digitraffic.tie.service.datex2.Datex2JsonConverterService;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2DataService;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2UpdateService;
import fi.livi.digitraffic.tie.service.v3.datex2.V3Datex2DataService;

@Import({ TrafficMessageTestHelper.class, V3Datex2DataService.class, V2Datex2DataService.class, V2Datex2UpdateService.class, Datex2JsonConverterService.class, XmlMarshallerConfiguration.class, JacksonAutoConfiguration.class})
public abstract class AbstractDatex2DataServiceTest extends AbstractServiceTest {

}