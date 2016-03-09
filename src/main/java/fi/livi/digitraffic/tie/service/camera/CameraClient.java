package fi.livi.digitraffic.tie.service.camera;

import java.util.List;

import javax.xml.bind.JAXBElement;

import fi.livi.digitraffic.tie.wsdl.kamera.HaeKaikkiKamerat;
import fi.livi.digitraffic.tie.wsdl.kamera.HaeKaikkiKameratResponse;
import fi.livi.digitraffic.tie.wsdl.kamera.Kamera;
import fi.livi.digitraffic.tie.wsdl.kamera.ObjectFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

public class CameraClient extends WebServiceGatewaySupport {
    private String address;

    final ObjectFactory objectFactory = new ObjectFactory();

    public List<Kamera> getCameras() {
        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeKaikkiKamerat request = new HaeKaikkiKamerat();

        final JAXBElement<HaeKaikkiKameratResponse> response = (JAXBElement<HaeKaikkiKameratResponse>)
                getWebServiceTemplate().marshalSendAndReceive(address, objectFactory.createHaeKaikkiKamerat(request));

        return response.getValue().getKamerat();
    }

    public List<Kamera> getPresetsCameras() {

        final HaeKaikkiKamerat request = new HaeKaikkiKamerat();

        final JAXBElement<HaeKaikkiKameratResponse> response = (JAXBElement<HaeKaikkiKameratResponse>)
                getWebServiceTemplate().marshalSendAndReceive(address, objectFactory.createHaeKaikkiKamerat(request));

        return response.getValue().getKamerat();
    }

    public void setAddress(final String address) {
        this.address = address;
    }
}
