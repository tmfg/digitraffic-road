package fi.livi.digitraffic.tie.conf;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
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
    private final String maxConnections;
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
            @Value("${camera-image-uploader.sftp.maxConnections}")
            final String maxConnections,
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
        this.maxConnections = maxConnections;
        this.poolSize = poolSize;
        this.sessionWaitTimeout = sessionWaitTimeout;
        this.resourceLoader = resourceLoader;
    }

    @Bean
    public DefaultSftpSessionFactory getDefaultSftpSessionFactory() throws IOException {
        DefaultSftpSessionFactory defaultSftpSessionFactory = new DefaultSftpSessionFactory();

        Optional.ofNullable(host).ifPresent(value -> defaultSftpSessionFactory.setHost(value));
        Optional.ofNullable(port).ifPresent(value -> defaultSftpSessionFactory.setPort(value));
        Optional.ofNullable(getPrivateKey()).ifPresent(value -> defaultSftpSessionFactory.setPrivateKey(value));
        Optional.ofNullable(privateKeyPassphrase).ifPresent(value -> defaultSftpSessionFactory.setPrivateKeyPassphrase(value));
        Optional.ofNullable(user).ifPresent(value -> defaultSftpSessionFactory.setUser(value));
        Optional.ofNullable(knownHostsPath).ifPresent(value -> defaultSftpSessionFactory.setKnownHosts(value));
        Optional.ofNullable(allowUnknownKeys).ifPresent(value -> defaultSftpSessionFactory.setAllowUnknownKeys(value));
        Optional.ofNullable(host).ifPresent(value -> defaultSftpSessionFactory.setHost(value));
        Optional.ofNullable(host).ifPresent(value -> defaultSftpSessionFactory.setHost(value));

        return defaultSftpSessionFactory;
    }

    @Bean
    public CachingSessionFactory<ChannelSftp.LsEntry> getCachingSessionFactory() throws IOException {
        log.info("Init CachingSessionFactory for sftp with host {}, poolSize {} and sessionWaitTimeout {}", host, poolSize, sessionWaitTimeout);
        CachingSessionFactory<ChannelSftp.LsEntry> cachingSessionFactory = new CachingSessionFactory<>(getDefaultSftpSessionFactory());
        Optional.ofNullable(poolSize).ifPresent(value -> cachingSessionFactory.setPoolSize(value));
        Optional.ofNullable(sessionWaitTimeout).ifPresent(value -> cachingSessionFactory.setSessionWaitTimeout(value));
        return cachingSessionFactory;
    }

    public Resource getPrivateKey() throws IOException {
        log.info("Load private key {}", privateKeyPath);
        Resource resource = loadResource(privateKeyPath);
        log.debug(FileUtils.readFileToString(resource.getFile(), UTF_8));
        return resource;
    }

    public String getKnownHosts() throws IOException {
        log.info("Load KnownHosts {}", knownHostsPath);
        File file = loadResource(knownHostsPath).getFile();
        final String content = FileUtils.readFileToString(file, UTF_8);
        log.debug(content);
        return content;
    }

    private Resource loadResource(String resourcePath) {
        log.info("Load resource {}", resourcePath);
        if (resourcePath.contains("classpath:")) {
            return resourceLoader.getResource(resourcePath);
        } else {
            return new FileSystemResource(resourcePath.replace("file:", ""));
        }
    }
}
