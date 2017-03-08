package fi.livi.digitraffic.tie.data.sftp;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.util.Base64;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.lotju.xsd.kamera.Kuva;

public abstract class AbstractSftpTest extends AbstractTest {

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
        testSftpServer.setPort(port);
        testSftpServer.setHost(host);
        SimpleGeneratorHostKeyProvider kp = getOrCreateKeyPairProvider();
        testSftpServer.setKeyPairProvider(kp);
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
        return new AuthorizedKeysAuthenticator(resourceLoader.getResource(authorizedKeysPath).getFile());
    }

    public SimpleGeneratorHostKeyProvider getOrCreateKeyPairProvider() throws IOException {
        log.info("Load or generate private key {}", idRsaPrivatePath);

        String filePath = resolveResourceFilePath(idRsaPrivatePath);

        File file = new File(filePath);
        log.info("Private Key absolute path {}", file.getAbsolutePath());

        boolean generate = !file.exists();
        SimpleGeneratorHostKeyProvider kpProvider = new SimpleGeneratorHostKeyProvider(file);
        kpProvider.setAlgorithm(KeyUtils.RSA_ALGORITHM);
        // This will generate new key if key file doesn't exist
        KeyPair keyPair = kpProvider.loadKey("ssh-rsa");
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
     * @throws IOException
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

    protected String getSftpPath(final Kuva kuva) {
        return getSftpPath(kuva.getNimi());
    }

    protected String getSftpPath(final String presetId) {
        return StringUtils.appendIfMissing(sftpUploadFolder, "/") + presetId + ".jpg";
    }

    protected String getImageUrl(final String presetId) {
        return "http://localhost:" + httpPort + getImageUrlPath(presetId);
    }

    protected String getImageUrlPath(final String presetId) {
        return REQUEST_PATH + presetId + ".jpg";
    }
}
