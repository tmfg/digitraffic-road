package fi.livi.digitraffic.tie.service.v1.lotju;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.ws.client.support.destination.DestinationProvider;

import fi.livi.digitraffic.tie.service.IllegalArgumentException;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

/**
 * Provides multi host support for SOAP-requests with health-test.
 */
public class MultiDestinationProvider implements DestinationProvider {

    private static final Logger log = LoggerFactory.getLogger(MultiDestinationProvider.class);
    private final List<HostWithHealtCheck> hosts;

    /**
     *
     * @param baseUrls ie. https://server1.com,https://server2.com
     * @param healthPath ie. /health
     * @param dataPath ie. /data/service
     */
    public MultiDestinationProvider(final String baseUrls, final String healthPath, final String dataPath, final int healtTtlSeconds) {
        final String[] addresses = StringUtils.split(baseUrls, ",");
        if ( addresses == null || addresses.length == 0 ) {
            log.warn("Creation failed because no addresses in baseUrls={}", baseUrls);
            throw new IllegalArgumentException("MultiDestinationProvider creation failed because no addresses given: " + baseUrls);
        }

        hosts = new ArrayList<>();

        for (String baseUrl : addresses) {
            hosts.add(new HostWithHealtCheck(baseUrl, healthPath, dataPath, healtTtlSeconds));
        }
    }

    @Override
    public URI getDestination() {
        for (HostWithHealtCheck host : hosts) {
            if ( host.doHealtcheck() ) {
                return host.getDataUri();
            }
        }
        final String urls = hosts.stream()
            .map(host -> host.getDataUri().toString())
            .collect(Collectors.joining(","));
        log.error("method=getDestination No healthy hosts for dataUrls={}", urls);
        throw new IllegalStateException(String.format("No healthy hosts for dataUrls=%s", urls));
    }


    public List<URI> getDestinations() {
        return hosts.stream().map(HostWithHealtCheck::getDataUri).collect(Collectors.toList());
    }

    public String getDestinationsAsString() {
        return hosts.stream().map(h -> h.getDataUri().toString()).collect(Collectors.joining(","));
    }

    public int getDestinationsCount() {
        return hosts.size();
    }

    public void setHostHealthy(final URI dest) {
        setHostHealthy(dest, true);
    }

    public void setHostNotHealthy(final URI dest) {
        setHostHealthy(dest, false);
    }

    private void setHostHealthy(final URI dest, boolean healthy) {
        Optional<HostWithHealtCheck> host = hosts.stream().filter(h -> h.getDataUri().equals(dest)).findFirst();
        if (host.isPresent()) {
            host.get().setHealthy(healthy);
        } else {
            throw new IllegalArgumentException(String.format("No host found for dataUrl=%s", dest.toString()));
        }
    }

    private static RequestHeadersSpec<?> createHealtWebClient(final String baseUrl, String healthPath) {
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

    private class HostWithHealtCheck {

        private final RequestHeadersSpec<?> healthRequest;
        private final URI dataUri;
        private final String baseUrl;
        private final String healthUrl;
        private final int healtTtlSeconds;

        private Instant healthChecked = Instant.now().minusSeconds(1000);
        private boolean healty = true;

        public HostWithHealtCheck(final String baseUrl, final String healthPath, final String dataPath, int healtTtlSeconds) {
            this.baseUrl = baseUrl;
            this.healtTtlSeconds = healtTtlSeconds;
            this.healthUrl = baseUrl + healthPath;
            this.dataUri = URI.create(baseUrl + dataPath);
            healthRequest = createHealtWebClient(baseUrl, healthPath);
            log.info("Created HostWithHealtCheck healthCheckUrl={} dataUrl={}", healthUrl, baseUrl+dataPath);
        }

        /**
         * Checks if health check is needed (ttl exceeded) and returns either health check status or cached value.
         * @return healt status
         */
        public boolean doHealtcheck() {
            try {
                if (!isHealtCheckNeeded()) {
                    return healty;
                }

                final String responseContent = healthRequest.retrieve().bodyToMono(String.class).block();
                if ( StringUtils.trimToEmpty(responseContent).equalsIgnoreCase("ok!") ) {
                    log.info("Health check for healthCheckUrl={} returned healthCheckValue={} returnStatus=true", healthUrl, responseContent);
                    setHealthy(true);
                    return true;
                }
                log.info("Health check for healthCheckUrl={} returned healthCheckValue={} returnStatus=false", healthUrl, responseContent);
                setHealthy(false);
                return false;
            } catch (final Exception e) {
                log.warn(String.format("Health check for healthCheckUrl=%s failed returnStatus=false", healthUrl), e);
                setHealthy(false);
                return false;
            }
        }

        private boolean isHealtCheckNeeded() {
            log.debug("healthTimeToLiveSeconds={} healthChecked={} now={}", healtTtlSeconds, healthChecked, Instant.now());
            return Instant.now().minus(healtTtlSeconds, ChronoUnit.SECONDS).isAfter(healthChecked);
        }

        public void setHealthy(final boolean healty) {
            this.healty = healty;
            healthChecked = Instant.now();
        }
        public URI getDataUri() {
            return dataUri;
        }

    }
}
