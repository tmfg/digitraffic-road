package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public abstract class LotjuServiceEndpoint {

    private static final Logger log = LoggerFactory.getLogger(LotjuServiceEndpoint.class);
    private final String metadataServerAddress;
    private final Class<?> metatiedotClass;
    private final QName serviceName;

    private boolean stateAfterChange = false;

    protected final ResourceLoader resourceLoader;
    private boolean inited;

    public LotjuServiceEndpoint(final ResourceLoader resourceLoader,
                                final String metadataServerAddress,
                                final Class<?> metatiedotClass,
                                final QName serviceName) {
        this.resourceLoader = resourceLoader;
        this.metadataServerAddress = metadataServerAddress;
        this.metatiedotClass = metatiedotClass;
        this.serviceName = serviceName;
    }

    /**
     * Initiaize data and call initService-method here.
     * Call this before tests.
     */
    public abstract void initDataAndService();

    /**
     * Must be called before api-operations
     */
    protected void initService() {
        log.info("Init LotjuServiceEndpoint with address " + metadataServerAddress + " and serviceClass " + metatiedotClass);
        final JaxWsServerFactoryBean svrFactory = new JaxWsServerFactoryBean();
        svrFactory.setServiceClass(metatiedotClass);
        svrFactory.setAddress(metadataServerAddress);
        svrFactory.setServiceBean(this);
        svrFactory.setServiceName(serviceName);
        svrFactory.create();
        inited = true;
    }

    public boolean isInited() {
        return inited;
    }

    public boolean isStateAfterChange() {
        return stateAfterChange;
    }

    public void setStateAfterChange(final boolean stateChanged) {
        this.stateAfterChange = stateChanged;
    }

    /**
     * Read given lotju xml and returns response value
     * @param filePath
     * @param objectFactoryClass
     * @return response value Object returned from JAXBElement<?>.getValue()
     */
    protected Object readLotjuMetadataXml(final String filePath, final Class<?> objectFactoryClass) {
        try {
            final Resource resource = resourceLoader.getResource("classpath:" + filePath);
            final File xmlFile = resource.getFile();
            final JAXBContext jaxbContext = JAXBContext.newInstance(objectFactoryClass);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            final JAXBElement<?> response =
                    (JAXBElement<?>) jaxbUnmarshaller.unmarshal(xmlFile);
            return response.getValue();
        } catch (final IOException e) {
            throw new LotjuTestException(e);
        } catch (final JAXBException e) {
            throw new LotjuTestException(e);
        }
    }

    private class LotjuTestException extends RuntimeException {
        public LotjuTestException(Exception e) {
            super(e);
        }
    }
}
