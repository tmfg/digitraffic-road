package fi.livi.digitraffic.tie.data.sftp;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.config.keys.AuthorizedKeysAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.junit.After;
import org.junit.Before;
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

    @Autowired
    private ResourceLoader resourceLoader;

    String host = "localhost";

    private final String idRsaPrivatePath = "classpath:sftp/server_id_rsa";
    private final String authorizedKeysPath = "classpath:sftp/server_authorized_keys";
    private SshServer testSftpServer;

    @Before
    public void initSftpServer() throws IOException {

        testSftpServer = SshServer.setUpDefaultServer();
        testSftpServer.setKeyPairProvider(getKeyPairProvider());
        testSftpServer.setPort(port);
        testSftpServer.setHost(host);
        testSftpServer.setPublickeyAuthenticator(getAuthorizedKeysAuthenticator());

        //        testFolder = new TemporaryFolder();
        //        testFolder.create();
        //        String path = testFolder.getRoot().getPath();

        //        new Path()
        //        testSftpServer.setFileSystemFactory( new VirtualFileSystemFactory(new Path("digitraffic") ) );

        CommandFactory myCommandFactory = command -> {
            log.info("Command: " + command);
            return null;
        };
        testSftpServer.setCommandFactory(myCommandFactory);
        List<NamedFactory<Command>> namedFactoryList = new ArrayList<NamedFactory<Command>>();
        namedFactoryList.add(new SftpSubsystemFactory());
        testSftpServer.setSubsystemFactories(namedFactoryList);
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
