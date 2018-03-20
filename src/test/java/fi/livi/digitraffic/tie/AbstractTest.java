package fi.livi.digitraffic.tie;

import static java.time.ZoneOffset.UTC;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MetadataApplication.class,
                properties = { "config.test=true" },
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public abstract class AbstractTest {
    @Autowired
    protected ResourceLoader resourceLoader;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @PersistenceContext
    protected EntityManager entityManager;

    protected Path getPath(final String filename) {
        return new File(getClass().getResource(filename).getFile()).toPath();
    }

    protected List<Resource> loadResources(final String pattern) throws IOException {
        return Arrays.asList(ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(pattern));
    }

    protected Resource loadResource(final String pattern) throws IOException {
        return resourceLoader.getResource(pattern);
    }

    protected ArrayList<String> readResourceContents(final String resourcePattern) throws IOException {
        final List<Resource> datex2Resources = loadResources(resourcePattern);
        final ArrayList<String> contents = new ArrayList<>();

        for (Resource datex2Resource : datex2Resources) {
            contents.add(FileUtils.readFileToString(datex2Resource.getFile(), StandardCharsets.UTF_8));
        }
        return contents;
    }

    protected String readResourceContent(final String resourcePattern) throws IOException {
        final Resource datex2Resource = loadResource(resourcePattern);

        return FileUtils.readFileToString(datex2Resource.getFile(), StandardCharsets.UTF_8);
    }

    protected static void assertCollectionSize(final int expectedSize, final Collection<?> collection) {
        final int collectionSize = collection.size();

        Assert.assertTrue(String.format("Collection size was expected to be %d, was %s", expectedSize, collectionSize),
            collectionSize == expectedSize);
    }

    protected static void assertEmpty(final Collection<?> col) {
        assertCollectionSize(0, col);
    }

    protected static void assertTimesEqual(final ZonedDateTime t1, final ZonedDateTime t2) {
        if(t1 == null && t2 == null) return;

        if(t1 == null && t2 != null) {
            Assert.fail("was asserted to be null, was not");
        }

        if(t1 != null && t2 == null) {
            Assert.fail("given value was null");
        }

        final ZonedDateTime tz1 = t1.withZoneSameInstant(UTC);
        final ZonedDateTime tz2 = t2.withZoneSameInstant(UTC);

        Assert.assertEquals(tz1, tz2);
    }
}
