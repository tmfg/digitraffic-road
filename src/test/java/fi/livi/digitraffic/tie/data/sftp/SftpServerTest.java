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
import org.springframework.messaging.MessagingException;
import org.springframework.util.ReflectionUtils;

import fi.livi.digitraffic.tie.base.MetadataIntegrationTest;


public class SftpServerTest extends AbstractSftpTest {
    private static final Logger log = LoggerFactory.getLogger(MetadataIntegrationTest.class);

    @Autowired
    private SessionFactory sftpSessionFactory;

    @Value("${camera-image-uploader.sftp.poolSize}")
    Integer poolSize;

    @Value("${camera-image-uploader.sftp.sessionWaitTimeout}")
    Long sessionWaitTimeout;

    @Test
    public void testPutAndGetFile() throws Exception {

        Session session = sftpSessionFactory.getSession();

        final String testFileContents = "some file contents";
        String uploadedFileName = "uploadFile";
        log.info("Upload file {} with content {}", uploadedFileName, testFileContents);
        session.write(new ByteArrayInputStream(testFileContents.getBytes()), uploadedFileName);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        log.info("Read file back from server");
        session.read(uploadedFileName, out);

        String fileData = out.toString(UTF_8.toString());

        log.info("Downloaded file {} with content {}", uploadedFileName, fileData);

        assertEquals("Read file contents not equal with written content", testFileContents, fileData);

        session.close();
    }

    @Test
    public void testSessionCachingPoolLimit() {

        HashSet<Session> sessions = new HashSet<>();
        while(sessions.size() < poolSize+1) {
            log.info("Get session {}", sessions.size()+1);
            if (sessions.size() < poolSize) {
                sessions.add(this.sftpSessionFactory.getSession());
            } else {
                // getting session out of pool fails
                log.info("Getting session of full should fail after timeout");
                StopWatch time = StopWatch.createStarted();
                boolean fail = false;
                try {
                    Session session = this.sftpSessionFactory.getSession();
                    session.close();
                } catch (MessagingException e) {
                    fail = true;
                    time.stop();
                    log.info("Timeout took {} ms", time.getTime());
                    assertTrue(time.getTime() >= sessionWaitTimeout);
                    assertTrue(time.getTime() <= sessionWaitTimeout+100);
                }
                assertTrue("Get session should have failed after timeout " + sessionWaitTimeout, fail);
                break;
            }
        }
        sessions.stream().forEach(Session::close);
    }

    @Test
    public void testSessionCaching() {

        HashSet<Session> newSessions = new HashSet<>();
        while(newSessions.size() < poolSize) {
            log.info("Get new session {}", newSessions.size()+1);
            newSessions.add(this.sftpSessionFactory.getSession());
        }
        // relase sessions to pool
        newSessions.stream().forEach(Session::close);

        Field sessionField = ReflectionUtils.findField(CachingSessionFactory.CachedSession.class, "targetSession");
        sessionField.setAccessible(true);
        Set<Session> newRealSessions = new HashSet<>();
        newSessions.stream().forEach(s -> {
            newRealSessions.add((Session) ReflectionUtils.getField(sessionField, s));
        });

        HashSet<Session> cachedSessions = new HashSet<>();
        while(cachedSessions.size() < poolSize) {
            log.info("Get cached session {}", cachedSessions.size()+1);
            cachedSessions.add(this.sftpSessionFactory.getSession());
        }
        Set<Session> cachedRealSessions = new HashSet<>();
        cachedSessions.stream().forEach(s -> {
            cachedRealSessions.add((Session) ReflectionUtils.getField(sessionField, s));
        });

        assertTrue("All sessions should be found from cachedSessions", cachedRealSessions.containsAll(newRealSessions));
        cachedSessions.stream().forEach(Session::close);
    }
}
