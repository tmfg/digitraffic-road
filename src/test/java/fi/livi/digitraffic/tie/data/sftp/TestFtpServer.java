package fi.livi.digitraffic.tie.data.sftp;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;

import fi.livi.digitraffic.tie.base.MetadataIntegrationTest;


public class TestFtpServer extends AbstractSftpTest {
    private static final Logger log = LoggerFactory.getLogger(MetadataIntegrationTest.class);

    @Autowired
    private DefaultSftpSessionFactory defaultSftpSessionFactory;

    @Autowired
    private CachingSessionFactory cachingSessionFactory;

    @Test
    public void testPutAndGetFile() throws Exception {

//        SftpSession session = defaultSftpSessionFactory.getSession();
        Session session = cachingSessionFactory.getSession();
        final String testFileContents = "some file contents";

        String uploadedFileName = "uploadFile";
        log.info("Upload file {} with content {}", uploadedFileName, testFileContents);
        session.write(new ByteArrayInputStream(testFileContents.getBytes()), uploadedFileName);

        String downloadedFileName = "downLoadFile";

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        log.info("Read file back from server");
        session.read(uploadedFileName, out);

        String fileData = out.toString(UTF_8.toString());

        log.info("Downloaded file {} with content {}", uploadedFileName, fileData);

        assertEquals(testFileContents, fileData);

        session.close();
    }

}
