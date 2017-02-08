package fi.livi.digitraffic.tie.conf;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;

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
        this.user = user;
        this.maxConnections = maxConnections;
        this.poolSize = poolSize;
        this.sessionWaitTimeout = sessionWaitTimeout;
        this.resourceLoader = resourceLoader;
    }

    //@Bean(name = "cachedSftpSessionFactory")
    @Bean
    public DefaultSftpSessionFactory getDefaultSftpSessionFactory() throws IOException {
        DefaultSftpSessionFactory defaultSftpSessionFactory = new DefaultSftpSessionFactory();

        defaultSftpSessionFactory.setHost(host);
        defaultSftpSessionFactory.setPort(port);
        defaultSftpSessionFactory.setPrivateKey(getPrivateKey());
        defaultSftpSessionFactory.setPrivateKeyPassphrase(privateKeyPassphrase);
        defaultSftpSessionFactory.setUser(user);
        defaultSftpSessionFactory.setKnownHosts(getKnownHosts());
        return defaultSftpSessionFactory;
    }

    @Bean
    public CachingSessionFactory<ChannelSftp.LsEntry> getCachedSftpSessionFactory() throws IOException {
        log.info("Init CachingSessionFactory for sftp to host {}", host);
        CachingSessionFactory<ChannelSftp.LsEntry> cachedFtpSessionFactory = new CachingSessionFactory<>(getDefaultSftpSessionFactory());
        cachedFtpSessionFactory.setPoolSize(poolSize);
        cachedFtpSessionFactory.setSessionWaitTimeout(sessionWaitTimeout);
        return cachedFtpSessionFactory;
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
        if (knownHostsPath.contains("classpath:")) {
            return resourceLoader.getResource(resourcePath);
        } else {
            return new FileSystemResource(resourcePath.replace("file:", ""));
        }
    }
}
