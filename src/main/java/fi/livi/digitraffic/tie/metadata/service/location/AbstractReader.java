package fi.livi.digitraffic.tie.metadata.service.location;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractReader<T> {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected static final String DELIMETER = ";";

    public List<T> readLocationTypes(final Path path) {
        try (final Stream<String> stream = Files.lines(path, StandardCharsets.ISO_8859_1)) {
            // skip the first line, then convert others
            return stream.skip(1)
                .map(this::convert)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } catch(final IOException ioe) {
            log.error("error opening file", ioe);
        }

        return Collections.emptyList();
    }

    protected abstract T convert(final String line);
}
