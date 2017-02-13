package fi.livi.digitraffic.tie.data.sftp;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.config.keys.AuthorizedKeysAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.scp.ScpCommandFactory;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import fi.livi.digitraffic.tie.base.MetadataIntegrationTest;
import fi.livi.digitraffic.tie.lotju.xsd.kamera.Kuva;

public class AbstractSftpTest extends MetadataIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractSftpTest.class);

    protected static final String REQUEST_PATH = "/kamerakuva/";

    @Value("${camera-image-uploader.sftp.port}")
    protected Integer port;

    @Value("${camera-image-uploader.sftp.uploadFolder}")
    String sftpUploadFolder;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    protected int httpPort;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    protected SessionFactory sftpSessionFactory;

    String host = "localhost";


    private final String idRsaPrivatePath = "classpath:sftp/server_id_rsa";
    private final String authorizedKeysPath = "classpath:sftp/server_authorized_keys";
    private SshServer testSftpServer;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void initSftpServer() throws IOException {

        log.info("Init Sftp Server with temporary root folder {}", testFolder.getRoot());
        httpPort = wireMockRule.port();

        testSftpServer = SshServer.setUpDefaultServer();
        testSftpServer.setKeyPairProvider(getKeyPairProvider());
        testSftpServer.setPort(port);
        testSftpServer.setHost(host);
        testSftpServer.setPublickeyAuthenticator(getAuthorizedKeysAuthenticator());
        VirtualFileSystemFactory fsFactory = new VirtualFileSystemFactory(testFolder.getRoot().toPath());
        testSftpServer.setFileSystemFactory(fsFactory);

        testSftpServer.setCommandFactory(new ScpCommandFactory());
        testSftpServer.setSubsystemFactories(Arrays.asList(new SftpSubsystemFactory()));
        testSftpServer.start();

        Session session = sftpSessionFactory.getSession();
        if (!session.exists(sftpUploadFolder)) {
            session.mkdir(sftpUploadFolder);
        }
        session.close();
    }

    @After
    public void onShutdown() throws IOException {
        log.info("Shutdown testSftpServer");
        testSftpServer.close();
    }

    public PublickeyAuthenticator getAuthorizedKeysAuthenticator() throws IOException {
        return new AuthorizedKeysAuthenticator(resourceLoader.getResource(authorizedKeysPath).getFile()) {
            @Override
            public boolean authenticate(String username, PublicKey key, ServerSession session) {
                return super.authenticate(username, key, session);
            }
        };
    }

    public KeyPairProvider getKeyPairProvider() throws IOException {
        log.info("Load private key {}", idRsaPrivatePath);
        SimpleGeneratorHostKeyProvider kp = new SimpleGeneratorHostKeyProvider(resourceLoader.getResource(idRsaPrivatePath).getFile());
        kp.setAlgorithm("RSA");
        return kp;
    }

    protected String getSftpPath(final Kuva kuva) {
        return getSftpPath(kuva.getNimi());
    }

    protected String getSftpPath(final String presetId) {
        return StringUtils.appendIfMissing(sftpUploadFolder, "/") + presetId + ".jpg";
    }

    protected String getImageUrl(final String presetId) {
        return "http://localhost:" + httpPort + getImagePath(presetId);
    }

    protected String getImagePath(final String presetId) {
        return REQUEST_PATH + presetId + ".jpg";
    }
}
