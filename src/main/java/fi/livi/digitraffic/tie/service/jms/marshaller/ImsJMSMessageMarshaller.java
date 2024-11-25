package fi.livi.digitraffic.tie.service.jms.marshaller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.conf.kca.artemis.jms.message.ExternalIMSMessage;

public class ImsJMSMessageMarshaller extends TextJMSMessageMarshaller<ExternalIMSMessage> {
    private static final Logger log = LoggerFactory.getLogger(ImsJMSMessageMarshaller.class);

    public ImsJMSMessageMarshaller(final Jaxb2Marshaller imsJaxb2Marshaller) {
        super(imsJaxb2Marshaller);
    }

    @Override
    protected List<ExternalIMSMessage> transform(final Object object, final String text) {
        log.debug("method=transform messageText={}", text);
        return super.transform(object, text);
    }
}
