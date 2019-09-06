package fi.livi.digitraffic.tie.data.sftp;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.config.keys.AuthorizedKeysAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.scp.ScpCommandFactory;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.test.context.TestPropertySource;

import com.amazonaws.services.s3.AmazonS3;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jcraft.jsch.SftpException;

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.RoadApplication;
import fi.livi.digitraffic.tie.conf.amazon.SpringLocalstackDockerRunnerWithVersion;
import xyz.fabiano.spring.localstack.LocalstackService;
import xyz.fabiano.spring.localstack.annotation.SpringLocalstackProperties;

@RunWith(SpringLocalstackDockerRunnerWithVersion.class)
@SpringLocalstackProperties(services = { LocalstackService.S3 }, region = "eu-west-1")
@SpringBootTest(classes = RoadApplication.class)
public abstract class AbstractSftpTest extends AbstractDaemonTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractSftpTest.class);

    protected static final String REQUEST_PATH = "/kamerakuva/";

    @Value("${camera-image-uploader.sftp.port}")
    protected Integer port;

    @Value("${camera-image-uploader.sftp.uploadFolder}")
    String sftpUploadFolder;

    @Value("${camera-image-uploader.sftp.knownHostsPath}")
    String knownHostsPath;

    @Value("${camera-image-uploader.sftp.user}")
    String user;

    private Integer testPort = 62859;

    // NOTE! Rules uses fixed port. see DPO-489
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(testPort));

    private int httpPort;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    protected SessionFactory sftpSessionFactory;

    @Autowired
    protected AmazonS3 s3;

    @Value("${dt.amazon.s3.weathercamBucketName}")
    protected String weathercamBucketName;

    private String host = "localhost";

    private final String idRsaPrivatePath = "classpath:sftp/server_id_rsa";
    private final String authorizedKeysPath = "classpath:sftp/server_authorized_keys";
    private SshServer testSftpServer;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void initS3Bucket() {
        try {
            log.info("Init S3 Bucket {}", weathercamBucketName);
            s3.createBucket(weathercamBucketName);
        } catch (Exception e) {
            log.error("Failed to create bucket", e);
            throw e;
        }
    }

    @Before
    public void initSftpServer() throws IOException, GeneralSecurityException {
        log.info("Init Sftp Server with temporary root folder={}, port={}", testFolder.getRoot(), wireMockRule.port());
        httpPort = wireMockRule.port();

        testSftpServer = SshServer.setUpDefaultServer();
        testSftpServer.setPort(port);
        testSftpServer.setHost(host);
        SimpleGeneratorHostKeyProvider kp = getOrCreateKeyPairProvider();
        testSftpServer.setKeyPairProvider(kp);
        testSftpServer.setPublickeyAuthenticator(getAuthorizedKeysAuthenticator());
        VirtualFileSystemFactory fsFactory = new VirtualFileSystemFactory(testFolder.getRoot().toPath());
        testSftpServer.setFileSystemFactory(fsFactory);

        testSftpServer.setCommandFactory(new ScpCommandFactory());
        testSftpServer.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));
        log.info("Start Sftp Server on port {}", port);
        testSftpServer.start();
        log.info("Sftp Server started");
        final Session session = sftpSessionFactory.getSession();
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
        return new AuthorizedKeysAuthenticator(Paths.get(resourceLoader.getResource(authorizedKeysPath).getURI()));
    }

    public SimpleGeneratorHostKeyProvider getOrCreateKeyPairProvider() throws IOException, GeneralSecurityException {
        log.info("Load or generate private key={}", idRsaPrivatePath);

        String filePath = resolveResourceFilePath(idRsaPrivatePath);

        File file = new File(filePath);
        log.info("Private Key absolute path={}", file.getAbsolutePath());

        boolean generate = !file.exists();
        SimpleGeneratorHostKeyProvider kpProvider = new SimpleGeneratorHostKeyProvider(file.toPath());
        kpProvider.setAlgorithm(KeyUtils.RSA_ALGORITHM);
        // This will generate new key if key file doesn't exist
        KeyPair keyPair = kpProvider.loadKey(null,"ssh-rsa");
        PublicKey pub = keyPair.getPublic();
        if (generate) {
            savePublicKey( filePath + ".pub", pub);
        }
        // Always generate known hosts
        generateKnownHostForClient(pub);

        return kpProvider;
    }

    private String resolveResourceFilePath(String idRsaPrivatePath) throws IOException {
        String folderLocation = StringUtils.substringBeforeLast(idRsaPrivatePath, "/");
        String fileName = StringUtils.substringAfterLast(idRsaPrivatePath, "/");
        Resource rootResource = resourceLoader.getResource(folderLocation);
        String rootPath = rootResource.getFile().getAbsolutePath();
        return rootPath + File.separator + fileName;
    }

    private void generateKnownHostForClient(PublicKey publicKey) throws IOException {
        // Store Public Key in OpenSSH known_hosts format for client
        String knownHosts = resolveResourceFilePath(knownHostsPath);
        String knownHostsString = "[localhost]:" + port + " " + encodePublicKey(publicKey, user);
        writeFile(knownHosts, knownHostsString);
    }

    public void savePublicKey(final String path, final PublicKey publicKey) throws IOException {
        // Store Public Key
        String pubKeyString = encodePublicKey(publicKey, user);
        writeFile(path, pubKeyString);
    }

    private void writeFile(final String path, final String content) throws IOException {
        FileOutputStream fos = new FileOutputStream(path);
        fos.write(content.getBytes());
        fos.close();
    }

    /**
     * Copied from http://stackoverflow.com/questions/3531506/using-public-key-from-authorized-keys-with-java-security/14582408#14582408
     *
     * Encode PublicKey (DSA or RSA encoded) to authorized_keys like string
     *
     * @param publicKey DSA or RSA encoded
     * @param user username for output authorized_keys like string
     * @return authorized_keys like string
     * @throws IOException when write fails.
     */
    private static String encodePublicKey(PublicKey publicKey, String user)
            throws IOException {
        String publicKeyEncoded;
        if(publicKey.getAlgorithm().equals("RSA")){
            RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
            ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(byteOs);
            dos.writeInt("ssh-rsa".getBytes().length);
            dos.write("ssh-rsa".getBytes());
            dos.writeInt(rsaPublicKey.getPublicExponent().toByteArray().length);
            dos.write(rsaPublicKey.getPublicExponent().toByteArray());
            dos.writeInt(rsaPublicKey.getModulus().toByteArray().length);
            dos.write(rsaPublicKey.getModulus().toByteArray());
            publicKeyEncoded = new String(
                    Base64.encodeBase64(byteOs.toByteArray()));
            return "ssh-rsa " + publicKeyEncoded + (user != null ? " " + user : "");
        }
        else if(publicKey.getAlgorithm().equals("DSA")){
            DSAPublicKey dsaPublicKey = (DSAPublicKey) publicKey;
            DSAParams dsaParams = dsaPublicKey.getParams();

            ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(byteOs);
            dos.writeInt("ssh-dss".getBytes().length);
            dos.write("ssh-dss".getBytes());
            dos.writeInt(dsaParams.getP().toByteArray().length);
            dos.write(dsaParams.getP().toByteArray());
            dos.writeInt(dsaParams.getQ().toByteArray().length);
            dos.write(dsaParams.getQ().toByteArray());
            dos.writeInt(dsaParams.getG().toByteArray().length);
            dos.write(dsaParams.getG().toByteArray());
            dos.writeInt(dsaPublicKey.getY().toByteArray().length);
            dos.write(dsaPublicKey.getY().toByteArray());
            publicKeyEncoded = new String(
                    Base64.encodeBase64(byteOs.toByteArray()));
            return "ssh-dss " + publicKeyEncoded + (user != null ? " " + user : "");
        }
        else{
            throw new IllegalArgumentException(
                    "Unknown public key encoding: " + publicKey.getAlgorithm());
        }
    }

    String getSftpPath(final KuvaProtos.Kuva kuva) {
        return getSftpPath(kuva.getNimi());
    }

    protected String getSftpPath(final String presetId) {
        return StringUtils.appendIfMissing(sftpUploadFolder, "/") + presetId + ".jpg";
    }

    String getImageUrlPath(final Long imageId) {
        return REQUEST_PATH + imageId;
    }
}
