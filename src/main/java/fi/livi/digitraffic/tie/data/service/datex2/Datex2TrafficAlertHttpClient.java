package fi.livi.digitraffic.tie.data.service.datex2;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class Datex2TrafficAlertHttpClient {
    private static final Logger log = LoggerFactory.getLogger(Datex2TrafficAlertHttpClient.class);

    // Old ftp format  InfoXML_2017-04-26-07-50-58-913.xml
    // New http format  Datex2_2017-04-26-07-51-04-245.xml
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("'Datex2_'yyyy-MM-dd-HH-mm-ss-SSS'.xml'");

    private static final Pattern fileNamePattern = Pattern.compile("href=\"(Datex2_[0-9-]*\\.xml)\"");

    private final String url;
    private final RestTemplate restTemplate;
    private final RetryTemplate retryTemplate;
    private static final String AUTO_INDEX_QUERY_ARGUMENTS = "?F=0&C=N&O=D";

    @Autowired
    public Datex2TrafficAlertHttpClient(@Value("${Datex2MessageUrl}") final String url, final RestTemplate restTemplate, final RetryTemplate retryTemplate) {
        this.url = url;
        this.restTemplate = restTemplate;
        this.retryTemplate = retryTemplate;
    }

    public List<Pair<String, Instant>> getTrafficAlertMessages(final Instant from) {
        log.info("Read datex2 traffic alert messages from {}", from);

        final String html = getContent(url + AUTO_INDEX_QUERY_ARGUMENTS);

        final List<Pair<String, Instant>> newFiles = getNewFiles(from, html);

        return getDatex2Messages(newFiles);
    }

    private List<Pair<String, Instant>> getNewFiles(final Instant from, final String html) {
        final Matcher m = fileNamePattern.matcher(html);
        final List<Pair<String, Instant>> filenames = new ArrayList<>();

        while (m.find()) {
            final String filename = m.group(1);
            final Instant fileTimestamp = parseDate(filename);
            if (!isNewFile(from, fileTimestamp)) { // Links in html are ordered by filename which contains file date
                break;
            }
            if (filename != null && filenames.stream().noneMatch(f -> f.getLeft().equals(filename))) {
                filenames.add(Pair.of(filename, fileTimestamp));
            }
        }
        // Sort files from oldest to newest
        return filenames.stream().sorted(Comparator.comparing(Pair::getLeft)).collect(Collectors.toList());
    }

    private boolean isNewFile(final Instant from, final Instant fileDate) {
        return from == null || (fileDate != null && from.isBefore(fileDate));
    }

    private List<Pair<String, Instant>> getDatex2Messages(final List<Pair<String, Instant>> filenames) {
        final List<Pair<String, Instant>> messages = new ArrayList<>();

        for (final Pair<String, Instant> filename : filenames) {
            final String datex2Url = url + filename.getLeft();
            log.info("Reading Datex2 message: {}", datex2Url);

            final String content = getContent(datex2Url);
            messages.add(Pair.of(content, filename.getRight()));
        }
        return messages;
    }

    protected String getContent(final String url) {
        return retryTemplate.execute(context -> restTemplate.getForObject(url, String.class));
    }

    private Instant parseDate(final String filename) {
        try {
            return TimeFromFilenameParser.parseDate(filename);
        } catch (final DateTimeParseException ex) {
            log.error("Unable to parse date: " + filename, ex);
        }
        return null;
    }
}
