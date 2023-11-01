package fi.livi.digitraffic.tie.service.trafficmessage.location;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public abstract class AbstractReader<T> {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected static final char DELIMITER_COMMA = ',';
    protected static final char DELIMITER_SEMICOLON = ';';
    protected static final char DELIMITER_TAB = '\t';
    protected static final char QUOTE = '\"';

    private final Charset charset;
    protected final String version;
    private final CSVParser parser;

    protected AbstractReader(final Charset charset, final char delimeterCharacter, final String version) {
        this.charset = charset;
        this.version = version;
        parser = new CSVParserBuilder()
            .withSeparator(delimeterCharacter)
            .withQuoteChar(QUOTE)
            .build();

    }

    protected AbstractReader(final String version) {
        this(StandardCharsets.ISO_8859_1, DELIMITER_TAB, version);
    }

    public List<T> read(final Path path) {
        return read(path.toFile());
    }

    public List<T> read(final URL url) throws IOException {
        return read(url.openStream());
    }

    public List<T> read(final File file) {
        try {
            return read(new FileInputStream(file));
        } catch (final FileNotFoundException e) {
            log.error("error reading file", e);
        }

        return Collections.emptyList();
    }

    public List<T> read(final InputStream inputStream) {
        final CSVReader reader =
            new CSVReaderBuilder(new InputStreamReader(inputStream, charset))
                .withCSVParser(parser)
                .build();

        final AtomicInteger counter = new AtomicInteger(0);
        final AtomicReference<String[]> ref = new AtomicReference<>();

        try {
            return StreamSupport.stream(reader.spliterator(), false).skip(1)
                .map(item -> {
                    counter.getAndIncrement();
                    ref.set(item);
                    return convert(item);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } catch (final Exception e) {
            log.error("Line: " + Arrays.toString(ref.get()));
            log.error("Read or parse error occured at line: " + counter.get(), e);

            throw e;
        }
    }

    protected abstract T convert(final String[] line);
}
