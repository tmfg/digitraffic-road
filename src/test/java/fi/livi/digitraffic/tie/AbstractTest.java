package fi.livi.digitraffic.tie;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
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
                properties = { "config.test=true","javamelody.enabled=false" },
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

    protected void assertCollectionSize(final Collection<?> collection, final int expectedSize) {
        final int collectionSize = collection.size();

        Assert.assertTrue(String.format("Collection size was expected to be %d, was %s", expectedSize, collectionSize),
            collectionSize == expectedSize);
    }
}
