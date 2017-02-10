package fi.livi.digitraffic.tie.data.sftp;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Arrays;

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

import fi.livi.digitraffic.tie.base.MetadataIntegrationTest;


public class AbstractSftpTest extends MetadataIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractSftpTest.class);

    @Value("${camera-image-uploader.sftp.port}")
    protected Integer port;

    @Value("${camera-image-uploader.sftp.uploadFolder}")
    String sftpUploadFolder;

    @Autowired
    private ResourceLoader resourceLoader;

    String host = "localhost";

    private final String idRsaPrivatePath = "classpath:sftp/server_id_rsa";
    private final String authorizedKeysPath = "classpath:sftp/server_authorized_keys";
    private SshServer testSftpServer;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void initSftpServer() throws IOException {

        log.info("Init Sftp Server with temporary root folder {}", testFolder.getRoot());

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

}
