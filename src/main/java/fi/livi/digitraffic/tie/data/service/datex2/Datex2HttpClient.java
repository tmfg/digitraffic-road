package fi.livi.digitraffic.tie.data.service.datex2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class Datex2HttpClient {

    private static final Logger log = LoggerFactory.getLogger(Datex2HttpClient.class);

    // Old ftp format  InfoXML_2017-04-26-07-50-58-913.xml
    // New http format  Datex2_2017-04-26-07-51-04-245.xml
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("'Datex2_'yyyy-MM-dd-HH-mm-ss-SSS'.xml'");

    private static final Pattern fileNamePattern = Pattern.compile("href=\"(Datex2_[0-9-]*\\.xml)\"");

    private final String url;
    private static final String autoindexQueryArguments = "?F=0&C=N&O=D";

    public Datex2HttpClient(@Value("${Datex2MessageUrl}") final String url) {
        this.url = url;
    }

    public List<Pair<String, Timestamp>> getDatex2MessagesFrom(final Timestamp from) {
        try {
            log.info("Read datex2 messages from " + from);
            final String html = getContent(url + autoindexQueryArguments);

            final List<Pair<String, Timestamp>> newFiles = getNewFiles(from, html);

            return getDatex2Messages(newFiles);
        } catch (IOException e) {
            log.error("Datex2s read failed", e);
        }
        return null;
    }

    private List<Pair<String, Timestamp>> getNewFiles(final Timestamp from, final String html) {
        final Matcher m = fileNamePattern.matcher(html);
        final List<Pair<String, Timestamp>> filenames = new ArrayList<>();

        while (m.find()) {
            final String filename = m.group(1);
            final Timestamp fileTimestamp = parseDate(filename);
            if (!isNewFile(from, fileTimestamp)) { // Links in html are ordered by filename which contains file date
                break;
            }
            if (filename != null && !filenames.stream().anyMatch(f -> f.getLeft().equals(filename))) {
                filenames.add(Pair.of(filename, fileTimestamp));
            }
        }
        // Sort files from oldest to newest
        return filenames.stream().sorted().collect(Collectors.toList());
    }

    private boolean isNewFile(final Timestamp from, final Timestamp fileDate) {
        return from == null || (fileDate != null && from.before(fileDate));
    }

    private List<Pair<String, Timestamp>> getDatex2Messages(final List<Pair<String, Timestamp>> filenames) {
        final List<Pair<String, Timestamp>> messages = new ArrayList<>();

        for (Pair<String, Timestamp> filename : filenames) {
            try {
                final String datex2Url = url + filename.getLeft();
                log.info("Reading Datex2 message: " + datex2Url);

                final String content = getContent(datex2Url);
                messages.add(Pair.of(content, filename.getRight()));
            } catch (IOException e) {
                log.error("Read content failed from " + filename, e);
            }
        }
        return messages;
    }

    private String getContent(final String url) throws IOException {
        final URL d2Url = new URL(url);
        final URLConnection con = d2Url.openConnection();
        con.setConnectTimeout(10000);
        con.setReadTimeout(10000);
        return new BufferedReader(new InputStreamReader(con.getInputStream())).lines().collect(Collectors.joining("\n"));
    }

    private Timestamp parseDate(final String filename) {
        try {
            final Date date = dateFormat.parse(filename);
            return new Timestamp(date.getTime());
        } catch (ParseException ex) {
            log.error("Unable to parse date: " + filename, ex);
        }
        return null;
    }
}
