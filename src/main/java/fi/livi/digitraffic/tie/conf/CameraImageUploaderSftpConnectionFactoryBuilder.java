package fi.livi.digitraffic.tie.conf;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;

import com.jcraft.jsch.ChannelSftp;

@ConditionalOnNotWebApplication
@Configuration
@EnableConfigurationProperties(CameraImageUploaderSftpConnectionConfigurationProperties.class)
public class CameraImageUploaderSftpConnectionFactoryBuilder {
    private static final Logger log = LoggerFactory.getLogger(CameraImageUploaderSftpConnectionFactoryBuilder.class);

    private final CameraImageUploaderSftpConnectionConfigurationProperties config;
    private final ResourceLoader resourceLoader;

    @Autowired
    public CameraImageUploaderSftpConnectionFactoryBuilder(
        CameraImageUploaderSftpConnectionConfigurationProperties config,
        ResourceLoader resourceLoader) {
        this.config = config;
        this.resourceLoader = resourceLoader;
    }

    private DefaultSftpSessionFactory getDefaultSftpSessionFactory() throws IOException {
        // Create "shared session" session factory then it must be used with cached session factory
        DefaultSftpSessionFactory defaultSftpSessionFactory = new DefaultSftpSessionFactory(false);
        defaultSftpSessionFactory.setHost(config.getHost());
        defaultSftpSessionFactory.setPort(config.getPort());
        defaultSftpSessionFactory.setPrivateKey(getPrivateKey());
        defaultSftpSessionFactory.setPrivateKeyPassphrase(config.getPrivateKeyPassphrase());
        defaultSftpSessionFactory.setUser(config.getUser());
        defaultSftpSessionFactory.setKnownHosts(resolveResourceAbsolutePath(config.getKnownHostsPath()));
        defaultSftpSessionFactory.setAllowUnknownKeys(config.getAllowUnknownKeys());
        defaultSftpSessionFactory.setTimeout(config.getConnectionTimeout());
        log.info("Initialized DefaultSftpSessionFactory host={}, port={}, privateKey={}, user={}, knownHostsPath={}, allowUnknownKeys={}",
            config.getHost(), config.getPort(), resolveResourceAbsolutePath(config.getPrivateKeyPath()), config.getUser(),
            resolveResourceAbsolutePath(config.getKnownHostsPath()), config.getAllowUnknownKeys());
        return defaultSftpSessionFactory;
    }

    // TODO Explain why the name is set?
    @Bean(name = "sftpSessionFactory")
    public CachingSessionFactory<ChannelSftp.LsEntry> getCachingSessionFactory() throws IOException {
        try {
            log.info("Init CachingSessionFactory for sftp with poolSize={} and sessionWaitTimeoutMs={}", config.getPoolSize(), config.getSessionWaitTimeout());
            CachingSessionFactory<ChannelSftp.LsEntry> cachingSessionFactory = new CachingSessionFactory<>(getDefaultSftpSessionFactory(),
                config.getPoolSize());
            cachingSessionFactory.setSessionWaitTimeout(config.getSessionWaitTimeout());
            return cachingSessionFactory;
        } catch (final Exception e) {
            log.error("error initializing", e);
        }

        return null;
    }

    private Resource getPrivateKey() throws IOException {
        log.info("Load private keyPath={}", config.getPrivateKeyPath());
        String absolutePath = resolveResourceAbsolutePath(config.getPrivateKeyPath());
        return loadResource(absolutePath);
    }

    private Resource loadResource(String resourcePath) {
        log.info("Load resource resourcePath={}", resourcePath);
        if (resourcePath.contains("classpath:")) {
            return resourceLoader.getResource(resourcePath);
        } else {
            FileSystemResource fsRes = new FileSystemResource(resourcePath.replace("file:", ""));
            log.info("Resource path resourcePath={}", fsRes.getPath());
            return fsRes;
        }
    }

    private String resolveResourceAbsolutePath(String resource) throws IOException {
        try {
            final String folderLocation = StringUtils.substringBeforeLast(resource, "/");
            final String fileName = StringUtils.substringAfterLast(resource, "/");
            final Resource rootResource = resourceLoader.getResource(folderLocation);
            final String rootPath = rootResource.getFile().getAbsolutePath();
            final String absolutePath = rootPath + File.separator + fileName;
            log.info("Resolved resource resource={} to absolutePath={}", resource, absolutePath);
            return absolutePath;
        } catch (Exception e) {
            log.error("Could not resolve resource=" + resource + " absolute path");
            throw e;
        }
    }
}
