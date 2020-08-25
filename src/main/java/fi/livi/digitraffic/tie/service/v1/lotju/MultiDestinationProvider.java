package fi.livi.digitraffic.tie.service.v1.lotju;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.support.destination.DestinationProvider;

import fi.livi.digitraffic.tie.service.IllegalArgumentException;

/**
 * Provides multi host support for SOAP-requests with health-test.
 */
public class MultiDestinationProvider implements DestinationProvider {

    private static final Logger log = LoggerFactory.getLogger(MultiDestinationProvider.class);
    private final List<HostWithHealthCheck> hosts;


    public MultiDestinationProvider(final List<HostWithHealthCheck> hosts) {
        this.hosts = hosts;
    }

    @Override
    public URI getDestination() {
        if (hosts.size() == 1) {
            return hosts.get(0).getDataUrl();
        }
        for (HostWithHealthCheck host : hosts) {
            if ( host.doHealthCheck() ) {
                return host.getDataUrl();
            }
        }
        final String urls = hosts.stream()
            .map(host -> host.getDataUrl().toString())
            .collect(Collectors.joining(","));
        log.error("method=getDestination No healthy hosts for dataUrls={}", urls);
        throw new IllegalStateException(String.format("No healthy hosts for dataUrls=%s", urls));
    }


    public List<URI> getDestinations() {
        return hosts.stream().map(HostWithHealthCheck::getDataUrl).collect(Collectors.toList());
    }

    public String getDestinationsAsString() {
        return hosts.stream().map(h -> h.getDataUrl().toString()).collect(Collectors.joining(","));
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

    private void setHostHealthy(final URI dest, final boolean healthy) {
        final Optional<HostWithHealthCheck> host = hosts.stream().filter(h -> h.getDataUrl().equals(dest)).findFirst();
        if (host.isPresent()) {
            host.get().setHealthy(healthy);
        } else {
            throw new IllegalArgumentException(String.format("No host found for dataUrl=%s", dest.toString()));
        }
    }
}
