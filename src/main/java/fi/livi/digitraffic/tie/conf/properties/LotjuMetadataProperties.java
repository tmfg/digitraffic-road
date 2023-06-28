package fi.livi.digitraffic.tie.conf.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "metadata.server")
public class LotjuMetadataProperties {
    private final String[] addresses;
    private final Path path;
    private final Health health;
    private final Sender sender;

    public LotjuMetadataProperties(final String[] addresses, final Path path, final Health health, final Sender sender) {
        this.addresses = addresses;
        this.path = path;
        this.health = health;
        this.sender = sender;
    }

    public String[] getAddresses() {
        return addresses;
    }

    public Path getPath() {
        return path;
    }

    public Health getHealth() {
        return health;
    }

    public Sender getSender() {
        return sender;
    }

    public static final class Path {
        public final String health;
        public final String camera;
        public final String tms;
        public final String weather;
        public final String image;

        public Path(final String health, final String camera, final String tms, final String weather, final String image) {
            this.health = health;
            this.camera = camera;
            this.tms = tms;
            this.weather = weather;
            this.image = image;
        }
    }

    public static final class Health {
        public final int ttlInSeconds;
        public final String value;

        public Health(final int ttlInSeconds, final String value) {
            this.ttlInSeconds = ttlInSeconds;
            this.value = value;
        }
    }

    public static final class Sender {
        public final int connectionTimeout;
        public final int readTimeout;

        public Sender(final int connectionTimeout, final int readTimeout) {
            this.connectionTimeout = connectionTimeout;
            this.readTimeout = readTimeout;
        }
    }
}
