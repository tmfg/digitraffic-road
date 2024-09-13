package fi.livi.digitraffic.tie.service.lotju;

import fi.livi.digitraffic.tie.conf.properties.LotjuMetadataProperties;
import fi.livi.digitraffic.tie.service.IllegalArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HostWithHealthCheck {
    private static final Logger log = LoggerFactory.getLogger(HostWithHealthCheck.class);

    private final WebClient webClient;
    private final URI dataUrl;
    private final String baseUrl;
    private final String healthUrl;
    private final int healthTtlSeconds;
    private final String healthOkValue;

    private boolean healthy = true;
    private Instant nextHealthCheckTime = Instant.now();

    /**
     *
     * @param baseUrl ie. https://example.com
     * @param dataPath ie. /service/data1
     * @param healthPath ie. /healthcheck. If empty no healt check is performed
     * @param healthTtlSeconds Health check time to live
     */
    public HostWithHealthCheck(final String baseUrl, final String dataPath, final String healthPath, final int healthTtlSeconds, final String healthOkValue) {
        this.baseUrl = baseUrl;
        this.healthTtlSeconds = healthTtlSeconds;
        this.healthOkValue = healthOkValue;
        this.healthUrl = StringUtils.isNotEmpty(healthPath) ? (baseUrl + healthPath) : null;
        this.dataUrl = URI.create(baseUrl + dataPath);
        this.webClient = createWebClient();

        if (StringUtils.isBlank(baseUrl)) {
            throw new IllegalArgumentException(String.format("Param baseUrl:\"%s\" can't be empty value", baseUrl));
        }
        if (StringUtils.isBlank(dataPath)) {
            throw new IllegalArgumentException(String.format("Param dataPath:\"%s\" can't be empty value", dataPath));
        }
        if (StringUtils.isNotBlank(healthUrl) && StringUtils.isBlank(healthOkValue)) {
            throw new IllegalArgumentException(String.format("Param healthOkValue:\"%s\" can't be empty value", healthOkValue));
        }

        log.info("Created HostWithHealthCheck healthCheckUrl={} dataUrl={} healthTtlSeconds={} healthOkValue={}",
            healthUrl, dataUrl, healthTtlSeconds, healthOkValue);
    }

    private WebClient createWebClient() {
        final HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(10));

        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    }

    /**
     * Checks if health check is needed (ttl exceeded) and returns either health check status or cached value.
     * @return health status
     */
    public boolean doHealthCheck() {
        // If healthUrl is not set, then recover host if ttl timeout is passed
        // Host will be marked as not healthy externally by calling setHealthy(false);
        if (healthUrl == null && isHealthCheckNeeded()) {
            log.info("method=doHealthCheck healthCheckUrl={} dataUrl={} healthCheckValue={} healthCheckExpectedValue={} returnStatus=true not performed as there is no health url", healthUrl, dataUrl, "not_performed_no_health_check", null);
            healthy = true;
            return true;
        } else if (!isHealthCheckNeeded()) {
            log.info("method=doHealthCheck healthCheckUrl={} dataUrl={} healthCheckValue={} healthCheckExpectedValue={} returnStatus=true not performed as ttl not exceeded", healthUrl, dataUrl, "not_performed_ttl", null);
            return healthy;
        }

        final String healthString = doRequestHealthString();

        if (healthString != null && StringUtils.trimToEmpty(healthString).toUpperCase().startsWith(healthOkValue.toUpperCase()) ) {
            log.info("method=doHealthCheck healthCheckUrl={} dataUrl={} healthCheckValue={} healthCheckExpectedValue={} returnStatus=true",
                     healthUrl, dataUrl, healthString, healthOkValue);
            setHealthy(true);
            return true;
        }
        log.info("method=doHealthCheck healthCheckUrl={} dataUrl={} healthCheckValue={} healthCheckExpectedValue={} returnStatus=false",
                 healthUrl, dataUrl, healthString, healthOkValue);
        setHealthy(false);
        return false;
    }

    /**
     * Requests health status message from server.
     * @return Error message or null if there is error.
     */
    private String doRequestHealthString() {
        try {
            return webClient.get().uri(healthUrl).retrieve().bodyToMono(String.class).block();
        } catch (final Exception e) {
            log.warn(String.format("method=doRequestHealthStatus Health check for healthCheckUrl=%s failed", healthUrl), e);
            return e.getMessage(); // Can be null
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
            log.info("method=setHealthy Change server baseUrl={} dataUrl={} fromHealthy={} toHealthy={} healthChecked={}", baseUrl, dataUrl, !this.healthy, this.healthy, now);
        }
    }
    public URI getDataUrl() {
        return dataUrl;
    }

    public static List<HostWithHealthCheck> createHostsWithHealthCheck(final LotjuMetadataProperties lmp, final String dataPath) {
        if ( lmp.getAddresses() == null || lmp.getAddresses().length == 0 ) {
            throw new IllegalArgumentException(String.format("method=createHostsWithHealthCheck failed because no addresses in baseUrls=%s:", Arrays.toString(lmp.getAddresses())));
        }
        return Arrays.stream(lmp.getAddresses())
            .map(baseUrl -> new HostWithHealthCheck(baseUrl, dataPath, lmp.getPath().health, lmp.getHealth().ttlInSeconds, lmp.getHealth().value))
            .collect(Collectors.toList());
    }
}