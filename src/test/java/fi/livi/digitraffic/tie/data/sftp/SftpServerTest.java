package fi.livi.digitraffic.tie.data.sftp;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.util.PoolItemNotAvailableException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.ReflectionUtils;

// Dirty but S3 must be cleared every time as port changes
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class SftpServerTest extends AbstractSftpTest {
    private static final Logger log = LoggerFactory.getLogger(SftpServerTest.class);

    @Autowired
    private SessionFactory sftpSessionFactory;

    @Value("${camera-image-uploader.sftp.poolSize}")
    private Integer poolSize;

    @Value("${camera-image-uploader.sftp.sessionWaitTimeout}")
    private Long sessionWaitTimeout;

    @Test
    public void testPutAndGetFile() throws Exception {
        try(final Session session = sftpSessionFactory.getSession()) {
            final String testFileContents = "some file contents";
            final String uploadedFileName = "uploadFile";

            log.info("Upload file={} with content={}", uploadedFileName, testFileContents);
            session.write(new ByteArrayInputStream(testFileContents.getBytes()), uploadedFileName);

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            log.info("Read file back from server");
            session.read(uploadedFileName, out);

            final String fileData = out.toString(UTF_8.toString());

            log.info("Downloaded file={} with content={}", uploadedFileName, fileData);

            assertEquals("Read file contents not equal with written content", testFileContents, fileData);
        }
    }

    @Test
    public void testSessionCachingPoolLimit() {
        final HashSet<Session> sessions = new HashSet<>();

        while(sessions.size() < poolSize+1) {
            log.info("Get sessionCount={}", sessions.size()+1);
            if (sessions.size() < poolSize) {
                sessions.add(this.sftpSessionFactory.getSession());
            } else {
                // getting session out of pool fails
                log.info("Getting session of full pool should fail after timeout");
                StopWatch time = StopWatch.createStarted();
                boolean fail = false;
                try {
                    Session session = this.sftpSessionFactory.getSession();
                    session.close();
                } catch (final PoolItemNotAvailableException e) {
                    fail = true;
                    time.stop();
                    log.info("Timeout tookMs={}", time.getTime());
                    assertTrue(time.getTime() >= sessionWaitTimeout);
                    assertTrue(time.getTime() <= sessionWaitTimeout+100);
                }
                assertTrue("Get session should have failed after timeout " + sessionWaitTimeout, fail);
                break;
            }
        }
        sessions.forEach(Session::close);
    }

    @Test
    public void testSessionCaching() {
        final HashSet<Session> newSessions = new HashSet<>();

        while(newSessions.size() < poolSize) {
            log.info("Get new sessionCount={}", newSessions.size()+1);
            newSessions.add(this.sftpSessionFactory.getSession());
        }
        // relase sessions to pool
        newSessions.forEach(Session::close);

        Field sessionField = ReflectionUtils.findField(CachingSessionFactory.CachedSession.class, "targetSession");
        sessionField.setAccessible(true);
        Set<Session> newRealSessions = new HashSet<>();
        newSessions.forEach(s -> {
            newRealSessions.add((Session) ReflectionUtils.getField(sessionField, s));
        });

        HashSet<Session> cachedSessions = new HashSet<>();
        while(cachedSessions.size() < poolSize) {
            log.info("Get cached sessionCount={}", cachedSessions.size()+1);
            cachedSessions.add(this.sftpSessionFactory.getSession());
        }
        Set<Session> cachedRealSessions = new HashSet<>();
        cachedSessions.forEach(s -> {
            cachedRealSessions.add((Session) ReflectionUtils.getField(sessionField, s));
        });

        assertTrue("All sessions should be found from cachedSessions", cachedRealSessions.containsAll(newRealSessions));
        cachedSessions.forEach(Session::close);
    }
}
