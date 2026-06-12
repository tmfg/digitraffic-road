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
import java.util.ArrayList;
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

import fi.livi.digitraffic.common.util.StringUtil;

/**
 * Base class for reading TMC metadata CSV/DAT files.
 *
 * <p>Subclasses implement {@link #convert(String[])} or {@link #convert(String[], String)}
 * to parse a single CSV row into a domain object. The {@code source} parameter carries the
 * original remote URL + zip entry name so parse errors can be traced back to the exact file,
 * e.g. {@code https://tmc.digitraffic.fi/tmc/4.6.zip!locations.csv}.
 *
 * <p>On parse failure a single entry is added to {@link #getParseErrors()}:
 * <pre>
 * line=8300 file=FI_LC_4.6.zip!locations.csv cause=Could not find subtype L1.0
 * </pre>
 */
public abstract class AbstractReader<T> {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected static final char DELIMITER_COMMA = ',';
    protected static final char DELIMITER_SEMICOLON = ';';
    protected static final char DELIMITER_TAB = '\t';
    protected static final char QUOTE = '\"';

    private final Charset charset;
    protected final String version;
    private final CSVParser parser;

    private final List<String> parseErrors = new ArrayList<>();

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
        return read(url.openStream(), url.getFile());
    }

    /**
     * Reads and parses all rows from a file, using the file name as the source label in logs.
     */
    public List<T> read(final File file) {
        return read(file, file.getName());
    }

    /**
     * Reads and parses all rows from a file.
     *
     * @param source human-readable origin for logging, e.g.
     *               {@code https://tmc.digitraffic.fi/tmc/4.6.zip!locations.csv}
     */
    public List<T> read(final File file, final String source) {
        try (final FileInputStream fis = new FileInputStream(file)) {
            return read(fis, source);
        } catch (final FileNotFoundException e) {
            log.error("method=read File not found source={}", source, e);
        } catch (final IOException e) {
            log.error("method=read IO error reading source={}", source, e);
        }

        return Collections.emptyList();
    }

    public List<T> read(final InputStream inputStream, final String filename) {
        parseErrors.clear();
        final AtomicInteger counter = new AtomicInteger(0);
        final AtomicReference<String[]> ref = new AtomicReference<>();

        try (final CSVReader reader = new CSVReaderBuilder(new InputStreamReader(inputStream, charset))
                .withCSVParser(parser)
                .build()) {
            return StreamSupport.stream(reader.spliterator(), false).skip(1)
                .map(item -> {
                    counter.getAndIncrement();
                    ref.set(item);
                    try {
                        return convert(item, filename);
                    } catch (final Exception e) {
                        final String shortSource = StringUtil.fileBaseName(filename);
                        parseErrors.add(StringUtil.format("line={} file={} row={} cause={}",
                                counter.get(), shortSource, Arrays.toString(item), e.getMessage()));
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } catch (final Exception e) {
            log.error("method=read IO/CSV error file={} lineNumber={} line=\"{}\" {}",
                    filename, counter.get(), Arrays.toString(ref.get()), e.getMessage(), e);
            if (e instanceof final RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns all per-row parse errors collected during the last {@link #read} call.
     * Each entry contains the line number, source file name, raw CSV row, and the error message.
     * The list is cleared at the start of each {@link #read} call, so it always reflects only
     * the most recent invocation.
     */
    public List<String> getParseErrors() {
        return Collections.unmodifiableList(parseErrors);
    }

    /**
     * Converts one parsed CSV row to a domain object.
     * Override this variant to get the source file name for richer error messages.
     *
     * @param filename source label, e.g. {@code https://tmc.digitraffic.fi/tmc/4.6.zip!locations.csv}
     */
    protected T convert(final String[] line, final String filename) {
        return convert(line);
    }

    /**
     * Converts one parsed CSV row to a domain object.
     * Subclasses must override this or {@link #convert(String[], String)}.
     */
    protected T convert(final String[] line) {
        throw new UnsupportedOperationException("Subclass must override convert(line) or convert(line, filename)");
    }
}
