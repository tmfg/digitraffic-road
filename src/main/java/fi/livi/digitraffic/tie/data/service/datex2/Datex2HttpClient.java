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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.data.model.Datex2;

@Service
public class Datex2HttpClient {

    private static final Logger log = LoggerFactory.getLogger(Datex2HttpClient.class);

    // Old ftp format  InfoXML_2017-04-26-07-50-58-913.xml
    // New http format  Datex2_2017-04-26-07-51-04-245.xml
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("'Datex2_'yyyy-MM-dd-HH-mm-ss-SSS'.xml'");

    private static final Pattern fileNamePattern = Pattern.compile("href=\"(Datex2_[0-9-]*\\.xml)\"");

    private final String baseUrl;
    private final String datex2Uri;

    public Datex2HttpClient(final String baseUrl, final String datex2Uri) {
        this.baseUrl = baseUrl;
        this.datex2Uri = datex2Uri;
    }

    public Collection<Datex2> getDatex2MessagesFrom(final ZonedDateTime from) {
        try {
            log.info("Read datex2 messages from " + from);
            final String html = getContent(baseUrl + datex2Uri);

            final Set<String> filenames = parseFilenames(html);

            final List<String> newFiles = filenames.stream().filter(filename -> {
                final ZonedDateTime fileDate = parseDate(filename);
                return from == null || (fileDate != null && from.isBefore(fileDate));
            }).sorted().collect(Collectors.toList());

            final List<String> urls = newFiles.parallelStream().map(f -> baseUrl + datex2Uri + f).collect(Collectors.toList());

            return getMessages(urls);
        } catch (IOException e) {
            log.error("Datex2s read failed", e);
        }
        return null;
    }

    private List<Datex2> getMessages(final List<String> datex2MessageUrls) {
        final List<Datex2> messages = new ArrayList<>();

        for (String datex2MessageUrl : datex2MessageUrls) {
            try {
                log.info("Read Datex2 message: " + datex2MessageUrl);
                final String content = getContent(datex2MessageUrl);
                final Datex2 datex2 = new Datex2();
                final String name = StringUtils.substringAfterLast(datex2MessageUrl, "/");
                final ZonedDateTime date = parseDate(name);
                datex2.setImportTime(date);
                datex2.setMessage(content);
                messages.add(datex2);
                log.info("Datex2 message read done");
            } catch (IOException e) {
                log.error("Read content failed from " + datex2MessageUrl, e);
            }
        }
        return messages;
    }

    private String getContent(final String url) throws IOException {
        log.info("Reading Datex2 html file from {}", url);
        final URL d2Url = new URL(url);
        final URLConnection con = d2Url.openConnection();
        con.setConnectTimeout(1000);
        con.setReadTimeout(10000);
        return new BufferedReader(new InputStreamReader(con.getInputStream())).lines().collect(Collectors.joining("\n"));
    }

    private Set<String> parseFilenames(final CharSequence html) {
        final Matcher m = fileNamePattern.matcher(html);
        final HashSet<String> filenames = new HashSet<>();

        while (m.find()) {
            final String filename = m.group(1);
            if (filename != null) {
                filenames.add(filename);
            }
        }
        return filenames;
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
