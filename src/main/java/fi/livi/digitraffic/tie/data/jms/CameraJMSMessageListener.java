package fi.livi.digitraffic.tie.data.jms;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import javax.jms.JMSException;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.data.service.CameraDataUpdateService;
import fi.livi.digitraffic.tie.lotju.xsd.kamera.Kuva;

@ConditionalOnProperty(name = "jms.camera.enabled")
@Component
public class CameraJMSMessageListener extends AbstractJMSMessageListener<Kuva> {

    private static final Logger log = LoggerFactory.getLogger(CameraJMSMessageListener.class);

    private final CameraDataUpdateService cameraDataUpdateService;

    @Autowired
    public CameraJMSMessageListener(final CameraDataUpdateService cameraDataUpdateService) throws JMSException, JAXBException {
        super(Kuva.class, log);
        this.cameraDataUpdateService = cameraDataUpdateService;
    }

    @Override
    protected void handleData(List<Pair<Kuva, String>> data) {
        try {
            List<Kuva> kuvaData = data.stream().map(o -> o.getLeft()).collect(Collectors.toList());
            cameraDataUpdateService.updateCameraData(kuvaData);
        } catch (SQLException e) {
            log.error("Error while handling Camera data", e);
        }
    }
}
