package fi.livi.digitraffic.tie.service.jms.marshaller;

import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.TmsMetadataUpdatedMessageDto.EntityType;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.external.lotju.metatietomuutos.lam.tietovirta.Metatietomuutos;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.MetadataUpdatedMessageDto.UpdateType;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.TmsMetadataUpdatedMessageDto;

public class TmsMetadataJMSMessageMarshaller extends TextJMSMessageMarshaller<TmsMetadataUpdatedMessageDto> {
    private static final Logger log = LoggerFactory.getLogger(TmsMetadataJMSMessageMarshaller.class);

    public TmsMetadataJMSMessageMarshaller(final Jaxb2Marshaller jaxb2Marshaller) {
        super(jaxb2Marshaller);
    }

    @Override
    protected List<TmsMetadataUpdatedMessageDto> transform(final Object object, final String text) {
        log.debug("method=transform text={} object={}", text, ToStringHelper.toStringFull(object));
        final Metatietomuutos muutos = (Metatietomuutos) object;
        final TmsMetadataUpdatedMessageDto dto =
            new TmsMetadataUpdatedMessageDto(muutos.getId(),
                                             new HashSet<>(muutos.getAsemat().getId()),
                                             UpdateType.fromExternalValue(muutos.getTyyppi()),
                                             DateHelper.toInstant(muutos.getAika()),
                                             EntityType.fromExternalValue(muutos.getEntiteetti()));
        return Collections.singletonList(dto);
    }
}
