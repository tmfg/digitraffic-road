package fi.livi.digitraffic.tie.conf;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;

import com.jcraft.jsch.ChannelSftp;

@Configuration
public class CameraImageUploaderSftpConnectionFactoryBuilder {
    private static final Logger log = LoggerFactory.getLogger(CameraImageUploaderSftpConnectionFactoryBuilder.class);

    private final String host;
    private final Integer port;
    private final String privateKeyPath;
    private final String knownHostsPath;
    private final String privateKeyPassphrase;
    private final Boolean allowUnknownKeys;
    private final String user;
    private final Integer poolSize;
    private final Long sessionWaitTimeout;

    private final ResourceLoader resourceLoader;
    @Autowired
    public CameraImageUploaderSftpConnectionFactoryBuilder(
            @Value("${camera-image-uploader.sftp.host}")
            final String host,
            @Value("${camera-image-uploader.sftp.port}")
            final Integer port,
            @Value("${camera-image-uploader.sftp.privateKeyPath}")
            final String privateKeyPath,
            @Value("${camera-image-uploader.sftp.privateKeyPassphrase}")
            final String privateKeyPassphrase,
            @Value("${camera-image-uploader.sftp.knownHostsPath}")
            final String knownHostsPath,
            @Value("${camera-image-uploader.sftp.allowUnknownKeys}")
            final Boolean allowUnknownKeys,
            @Value("${camera-image-uploader.sftp.user}")
            final String user,
            @Value("${camera-image-uploader.sftp.poolSize}")
            final Integer poolSize,
            @Value("${camera-image-uploader.sftp.sessionWaitTimeout}")
            final Long sessionWaitTimeout,
            final ResourceLoader resourceLoader) {

        this.host = host;
        this.port = port;
        this.privateKeyPath = privateKeyPath;
        this.privateKeyPassphrase = privateKeyPassphrase;
        this.knownHostsPath = knownHostsPath;
        this.allowUnknownKeys = allowUnknownKeys;
        this.user = user;
        this.poolSize = poolSize;
        this.sessionWaitTimeout = sessionWaitTimeout;
        this.resourceLoader = resourceLoader;
    }

    private DefaultSftpSessionFactory getDefaultSftpSessionFactory() throws IOException {
        // Create "shared session" session factory then it must be used with cached session factory
        DefaultSftpSessionFactory defaultSftpSessionFactory = new DefaultSftpSessionFactory(true);
        defaultSftpSessionFactory.setHost(host);
        defaultSftpSessionFactory.setPort(port);
        defaultSftpSessionFactory.setPrivateKey(getPrivateKey());
        defaultSftpSessionFactory.setPrivateKeyPassphrase(privateKeyPassphrase);
        defaultSftpSessionFactory.setUser(user);
        defaultSftpSessionFactory.setKnownHosts(resolveResourcePath(knownHostsPath));
        defaultSftpSessionFactory.setAllowUnknownKeys(allowUnknownKeys);
        log.info("Initialized DefaultSftpSessionFactory host:{}, port:{}, privateKey:{}, user:{}, knownHostsPath:{}, allowUnknownKeys;{}",
                 host, port, resolveResourcePath(privateKeyPath), user, resolveResourcePath(knownHostsPath), allowUnknownKeys);
        return defaultSftpSessionFactory;
    }

    @Bean(name = "sftpSessionFactory")
    public CachingSessionFactory<ChannelSftp.LsEntry> getCachingSessionFactory() throws IOException {
        log.info("Init CachingSessionFactory for sftp with poolSize {} and sessionWaitTimeout {}", poolSize, sessionWaitTimeout);
        CachingSessionFactory<ChannelSftp.LsEntry> cachingSessionFactory = new CachingSessionFactory<>(getDefaultSftpSessionFactory(), poolSize);
        Optional.ofNullable(sessionWaitTimeout).ifPresent(value -> cachingSessionFactory.setSessionWaitTimeout(value));
        return cachingSessionFactory;
    }

    public Resource getPrivateKey() throws IOException {
        log.info("Load private key {}", privateKeyPath);
        Resource resource = loadResource(privateKeyPath);
        log.debug(FileUtils.readFileToString(resource.getFile(), UTF_8));
        return resource;
    }

    private Resource loadResource(String resourcePath) {
        log.info("Load resource {}", resourcePath);
        if (resourcePath.contains("classpath:")) {
            return resourceLoader.getResource(resourcePath);
        } else {
            FileSystemResource fsRes = new FileSystemResource(resourcePath.replace("file:", ""));
            log.info("Resource path {}", fsRes.getPath());
            return fsRes;
        }
    }

    private String resolveResourcePath(final String resourcePath) {
        if (resourcePath.contains("classpath:")) {
            log.info("Classpath resource path {}", resourcePath);
            return  resourcePath;
        } else {
            String absolutePath = new FileSystemResource(resourcePath.replace("file:_", "")).getFile().getAbsolutePath();
            log.info("File system resource path {}", absolutePath);
            return absolutePath;
        }
    }
}
