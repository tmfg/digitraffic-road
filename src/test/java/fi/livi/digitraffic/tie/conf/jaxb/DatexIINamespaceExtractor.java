package fi.livi.digitraffic.tie.conf.jaxb;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DatexIINamespaceExtractor {

    private static final XMLInputFactory XML_FACTORY = XMLInputFactory.newFactory();

    static {
        XML_FACTORY.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
    }

    /**
     * Extracts namespaces from the given resource pattern (e.g. classpath*:schemas/datex2/announcements/3_5/*.xsd)
     *
     * @param resourcePattern the resource pattern to read schemas from
     * @return map of namespace URI to xmlns declaration (e.g. "<a href="http://datex2.eu/schema/3/common">...</a>" -> "xmlns:com")
     * @throws Exception if reading or parsing fails
     */
    public static Map<String, String> extractNamespaces(final String resourcePattern)
            throws Exception {

        final Map<String, String> namespaces = new HashMap<>();

        final Resource[] schemas =
                new PathMatchingResourcePatternResolver()
                        .getResources(resourcePattern);

        for (final Resource schema : schemas) {
            try (final InputStream in = schema.getInputStream()) {
                final XMLStreamReader reader =
                        XML_FACTORY.createXMLStreamReader(in);

                while (reader.hasNext()) {
                    if (reader.next() == XMLStreamConstants.START_ELEMENT) {
                        for (int i = 0; i < reader.getNamespaceCount(); i++) {
                            final String prefix = reader.getNamespacePrefix(i);
                            final String uri = reader.getNamespaceURI(i);

                            final String xmlns =
                                    (prefix == null) ? "xmlns" : "xmlns:" + prefix;

                            namespaces.put(uri, xmlns);
                        }
                        break; // only root <xs:schema>
                    }
                }
            }
        }
        return namespaces;
    }

}
