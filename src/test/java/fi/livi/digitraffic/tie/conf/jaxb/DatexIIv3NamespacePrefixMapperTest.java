package fi.livi.digitraffic.tie.conf.jaxb;

import fi.livi.digitraffic.common.util.StringUtil;
import fi.livi.digitraffic.tie.conf.jaxb2.DatexII_3_NamespacePrefixMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatexIIv3NamespacePrefixMapperTest {

    private static final DatexII_3_NamespacePrefixMapper mapper = new DatexII_3_NamespacePrefixMapper();

    @Test
    public void DatexII_v3_5_Announcements() throws Exception {
        validateMapperAgainstResourceDirSchemas("classpath*:schemas/datex2/announcements/3_5/*.xsd");
    }

    @Test
    public void DatexII_v3_5_Tms() throws Exception {
        validateMapperAgainstResourceDirSchemas("classpath*:schemas/datex2/tms/3_5/xml/*.xsd");
    }

    private void validateMapperAgainstResourceDirSchemas(final String resourceDir) throws Exception {
        final Map<String, String> namespaces =
                DatexIINamespaceExtractor.extractNamespaces(resourceDir);

        for (final var entry : namespaces.entrySet()) {
            final String prefix = StringUtil.format("xmlns:{}", mapper.getPreferredPrefix(entry.getKey(), null, true));
            assertEquals(entry.getValue(), prefix,
                    StringUtil.format("Namespace uri={} should have prefix={} but found prefix={}", entry.getKey(), entry.getValue(), prefix));
        }
    }
}
