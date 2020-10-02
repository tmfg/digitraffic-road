package fi.livi.digitraffic.tie.service.jms.marshaller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_0.ImsMessage;

public class ImsMessageMarshaller extends TextMessageMarshaller<ImsMessage> {
    private static final Logger log = LoggerFactory.getLogger(ImsMessageMarshaller.class);

    public ImsMessageMarshaller(final Jaxb2Marshaller jaxb2Marshaller) {
        super(jaxb2Marshaller);
    }

    @Override
    protected List<ImsMessage> transform(final Object object, final String text) {
        log.debug("method=transform messageText={}", text);
        return super.transform(object, text);
    }
}
