package fi.livi.digitraffic.tie.conf;

import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConditionalOnNotWebApplication
@ConfigurationProperties("camera-image-uploader.sftp")
public class CameraImageUploaderSftpConnectionConfigurationProperties {

    private String host;
    private Integer port;
    private String privateKeyPath;
    private String knownHostsPath;
    private String privateKeyPassphrase;
    private Boolean allowUnknownKeys;
    private String user;
    private Integer poolSize;
    private Long sessionWaitTimeout;
    private Integer connectionTimeout;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public String getKnownHostsPath() {
        return knownHostsPath;
    }

    public void setKnownHostsPath(String knownHostsPath) {
        this.knownHostsPath = knownHostsPath;
    }

    public String getPrivateKeyPassphrase() {
        return privateKeyPassphrase;
    }

    public void setPrivateKeyPassphrase(String privateKeyPassphrase) {
        this.privateKeyPassphrase = privateKeyPassphrase;
    }

    public Boolean getAllowUnknownKeys() {
        return allowUnknownKeys;
    }

    public void setAllowUnknownKeys(Boolean allowUnknownKeys) {
        this.allowUnknownKeys = allowUnknownKeys;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Integer getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(Integer poolSize) {
        this.poolSize = poolSize;
    }

    public Long getSessionWaitTimeout() {
        return sessionWaitTimeout;
    }

    public void setSessionWaitTimeout(Long sessionWaitTimeout) {
        this.sessionWaitTimeout = sessionWaitTimeout;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

}
