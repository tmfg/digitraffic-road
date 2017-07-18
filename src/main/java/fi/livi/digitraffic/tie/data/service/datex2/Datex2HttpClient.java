package fi.livi.digitraffic.tie.data.service.datex2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public List<String> getDatex2MessagesFrom(final ZonedDateTime from) {
        try {
            log.info("Read datex2 messages from " + from);
            final String html = getContent(url + autoindexQueryArguments);

            final List<String> newFiles = getNewFiles(from, html);

            final List<String> urls = newFiles.parallelStream().map(f -> url + f).collect(Collectors.toList());

            return getDatex2Messages(urls);
        } catch (IOException e) {
            log.error("Datex2s read failed", e);
        }
        return null;
    }

    private List<String> getNewFiles(final ZonedDateTime from, final String html) {
        final Matcher m = fileNamePattern.matcher(html);
        final List<String> filenames = new ArrayList<>();

        while (m.find()) {
            final String filename = m.group(1);
            final ZonedDateTime fileDate = parseDate(filename);
            if (!isNewFile(from, fileDate)) { // Links in html are ordered by filename which contains file date
                break;
            }
            if (filename != null && !filenames.contains(filename)) {
                filenames.add(filename);
            }
        }
        // Sort files from oldest to newest
        return filenames.stream().sorted().collect(Collectors.toList());
    }

    private boolean isNewFile(final ZonedDateTime from, final ZonedDateTime fileDate) {
        return from == null || (fileDate != null && from.isBefore(fileDate));
    }

    private List<String> getDatex2Messages(final List<String> datex2MessageUrls) {
        final List<String> messages = new ArrayList<>();

        for (String datex2MessageUrl : datex2MessageUrls) {
            try {
                log.info("Reading Datex2 message: " + datex2MessageUrl);

                final String content = getContent(datex2MessageUrl);
                messages.add(content);

                log.info("Datex2 message read done");
            } catch (IOException e) {
                log.error("Read content failed from " + datex2MessageUrl, e);
            }
        }
        return messages;
    }

    private String getContent(final String url) throws IOException {
        final URL d2Url = new URL(url);
        final URLConnection con = d2Url.openConnection();
        con.setConnectTimeout(1000);
        con.setReadTimeout(10000);
        return new BufferedReader(new InputStreamReader(con.getInputStream())).lines().collect(Collectors.joining("\n"));
    }

    private ZonedDateTime parseDate(final String filename) {
        try {
            final Date date = dateFormat.parse(filename);
            return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        } catch (ParseException ex) {
            log.error("Unable to parse date: " + filename, ex);
        }
        return null;
    }
}
