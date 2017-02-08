package fi.livi.digitraffic.tie.data.sftp;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpSession;

import fi.livi.digitraffic.tie.base.MetadataIntegrationTest;


public class TestFtpServer extends MetadataIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(MetadataIntegrationTest.class);

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    DefaultSftpSessionFactory defaultSftpSessionFactory;

    private SshServer sshd;

    @Value("${camera-image-uploader.sftp.port}")
    Integer port;
    String host = "localhost";

    private final String idRsaPrivatePath = "classpath:sftp/server_id_rsa";
    private final String authorizedKeysPath = "classpath:sftp/server_authorized_keys";

    @Before
    public void beforeTestSetup() throws Exception {

        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(port);
        sshd.setKeyPairProvider(getKeyPairProvider());
        sshd.setHost(host);
        sshd.setPublickeyAuthenticator(getAuthorizedKeysAuthenticator());

//        testFolder = new TemporaryFolder();
//        testFolder.create();
//        String path = testFolder.getRoot().getPath();

//        new Path()
//        sshd.setFileSystemFactory( new VirtualFileSystemFactory(new Path("digitraffic") ) );

        CommandFactory myCommandFactory = command -> {
            log.info("Command: " + command);
            return null;
        };
        sshd.setCommandFactory(myCommandFactory);
        List<NamedFactory<Command>> namedFactoryList = new ArrayList<NamedFactory<Command>>();
        namedFactoryList.add(new SftpSubsystemFactory());
        sshd.setSubsystemFactories(namedFactoryList);
        sshd.start();
    }

    @After
    public void teardown() throws Exception {
        log.info("Shutdown ssh-server");
        sshd.stop();
    }

    @Test
    public void testPutAndGetFile() throws Exception {

        SftpSession session = defaultSftpSessionFactory.getSession();
        final String testFileContents = "some file contents";

        String uploadedFileName = "uploadFile";
        log.info("Upload file {} with content {}", uploadedFileName, testFileContents);
        session.write(new ByteArrayInputStream(testFileContents.getBytes()), uploadedFileName);

        String downloadedFileName = "downLoadFile";

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        log.info("Read file back from server");
        session.read(uploadedFileName, out);

        String fileData = out.toString(UTF_8.toString());

        log.info("Downloaded file {} with content {}", uploadedFileName, fileData);

        assertEquals(testFileContents, fileData);

        session.close();
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
        log.info("Load key {}" + idRsaPrivatePath);
        SimpleGeneratorHostKeyProvider kp = new SimpleGeneratorHostKeyProvider(resourceLoader.getResource(idRsaPrivatePath).getFile());
        kp.setAlgorithm("RSA");
        return kp;
    }
}
