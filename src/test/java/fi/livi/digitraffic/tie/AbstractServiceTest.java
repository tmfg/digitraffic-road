package fi.livi.digitraffic.tie;

import org.springframework.context.annotation.Import;

import fi.livi.digitraffic.tie.conf.jaxb2.MetadataMarshallerConfiguration;
import fi.livi.digitraffic.tie.data.service.datex2.StringToObjectMarshaller;

@Import({StringToObjectMarshaller.class, MetadataMarshallerConfiguration.class})
public class AbstractServiceTest extends AbstractJpaTest {
}
