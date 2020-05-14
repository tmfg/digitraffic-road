package fi.livi.digitraffic.tie.conf;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.base.Splitter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import net.logstash.logback.composite.AbstractJsonProvider;

/**
 * Provider to log key=value -pairs in messages as json key-values.
 */
public class LoggerMessageKeyValuePairJsonProvider extends AbstractJsonProvider<ILoggingEvent> {

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) {

        final String message = event.getFormattedMessage();

        final List<Pair<String, String>> kvPairs = Splitter
            .on(' ') // split message by spaces
            .omitEmptyStrings()
            .trimResults()
            .splitToList(message)
            .stream()
            .map(kv -> kv.split("=")) // split message chunks by =
            // Filter empty key or value pairs
            .filter(kv -> kv.length > 1 && StringUtils.isNotBlank(kv[0]) && StringUtils.isNotBlank(kv[1]))
            .map(kv -> Pair.of(kv[0], kv[1]))
            .collect(Collectors.toList());

        if (kvPairs.isEmpty()) {
            return;
        }

        final Set<String> hasWrittenFieldNames = new HashSet<>();
        kvPairs.forEach(e -> {
            if (!hasWrittenFieldNames.contains(e.getKey())) {
                try {
                    generator.writeObjectField(e.getKey(), e.getValue());
                    hasWrittenFieldNames.add(e.getKey());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }
}
