package fi.livi.digitraffic.tie.service.v1.lotju;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

class HostWithHealthCheck {
    private static final Logger log = LoggerFactory.getLogger(HostWithHealthCheck.class);

    private final RequestHeadersSpec<?> healthRequest;
    private final URI dataUrl;
    private final String baseUrl;
    private final String healthUrl;
    private final int healthTtlSeconds;

    private boolean healthy = true;
    private Instant nextHealthCheckTime = Instant.now();

    public HostWithHealthCheck(final String baseUrl, final String healthPath, final String dataPath, int healthTtlSeconds) {
        this.baseUrl = baseUrl;
        this.healthTtlSeconds = healthTtlSeconds;
        this.healthUrl = baseUrl + healthPath;
        this.dataUrl = URI.create(baseUrl + dataPath);
        healthRequest = createHealthWebClient(baseUrl, healthPath);
        log.info("Created HostWithHealthCheck healthCheckUrl={} dataUrl={}", healthUrl, dataUrl.toString());
    }

    /**
     * Checks if health check is needed (ttl exceeded) and returns either health check status or cached value.
     * @return health status
     */
    public boolean doHealthCheck() {
        if (!isHealthCheckNeeded()) {
            return healthy;
        }

        final String healthString = doRequestHealthString();

        if ( StringUtils.trimToEmpty(healthString).equalsIgnoreCase("ok!") ) {
            log.info("method=doHealthCheck Health check for healthCheckUrl={} returned healthCheckValue={} returnStatus=true", healthUrl, healthString);
            setHealthy(true);
            return true;
        }
        log.info("method=doHealthCheck Health check for healthCheckUrl={} returned healthCheckValue={} returnStatus=false", healthUrl, healthString);
        setHealthy(false);
        return false;
    }

    /**
     * Requests health from server.
     * @return Null if there is error.
     */
    private String doRequestHealthString() {
        try {
            return healthRequest.retrieve().bodyToMono(String.class).block();
        } catch (final Exception e) {
            log.warn(String.format("method=doRequestHealthStatus Health check for healthCheckUrl=%s failed", healthUrl), e);
            return null;
        }
    }

    private boolean isHealthCheckNeeded() {
        return !Instant.now().isBefore(nextHealthCheckTime);
    }

    public void setHealthy(final boolean healthy) {
        final boolean changed = this.healthy != healthy;
        this.healthy = healthy;
        final Instant now = Instant.now();
        nextHealthCheckTime = now.plusSeconds(healthTtlSeconds);
        if (changed) {
            log.info("method=setHealthy Change server baseUrl={} dataUrl={} fromHealthy={} toHealthy={} healthChecked={}", baseUrl, dataUrl.toString(), !this.healthy, this.healthy, now);
        }
    }
    public URI getDataUrl() {
        return dataUrl;
    }

    private static RequestHeadersSpec<?> createHealthWebClient(final String baseUrl, String healthPath) {
        final TcpClient tcpClient = TcpClient
            .create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
            .doOnConnected(connection -> {
                connection.addHandlerLast(new ReadTimeoutHandler(1000, TimeUnit.MILLISECONDS));
                connection.addHandlerLast(new WriteTimeoutHandler(1000, TimeUnit.MILLISECONDS));
            });

        final WebClient client = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE, MediaType.TEXT_HTML_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT_CHARSET, StandardCharsets.UTF_8.name())
            .build();

        return client.get().uri(healthPath);
    }
}