package fi.livi.digitraffic.tie.conf.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "metadata.server")
@ConstructorBinding
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

    @ConstructorBinding
    public static final class Path {
        public final String health;
        public final String camera;

        public Path(final String health, final String camera) {
            this.health = health;
            this.camera = camera;
        }
    }

    @ConstructorBinding
    public static final class Health {
        public final int ttlInSeconds;
        public final String value;

        public Health(final int ttlInSeconds, final String value) {
            this.ttlInSeconds = ttlInSeconds;
            this.value = value;
        }
    }

    @ConstructorBinding
    public static final class Sender {
        public final int connectionTimeout;
        public final int readTimeout;

        public Sender(final int connectionTimeout, final int readTimeout) {
            this.connectionTimeout = connectionTimeout;
            this.readTimeout = readTimeout;
        }
    }
}
