package fi.livi.digitraffic.tie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * The AWS SDK modules share interfaces that change between versions, so mixing versions breaks the
 * application at startup with an AbstractMethodError. One module coming from a transitive dependency
 * is enough to break it, which is why this is checked here rather than left to a later review of the
 * dependency tree.
 */
public class AwsSdkVersionTest {

    private static final String AWS_SDK_GROUP_ID = "software.amazon.awssdk";

    @Test
    public void allAwsSdkModulesHaveTheSameVersion() {
        final Map<String, String> versionByArtifact = readAwsSdkModulesOnClasspath();

        assertFalse(versionByArtifact.isEmpty(), "No AWS SDK modules found on the classpath");

        final var versions = versionByArtifact.values().stream().distinct().sorted().collect(Collectors.toList());

        assertEquals(1, versions.size(),
                "AWS SDK modules have different versions: " + versionByArtifact);
    }

    private Map<String, String> readAwsSdkModulesOnClasspath() {
        final Map<String, String> versionByArtifact = new TreeMap<>();

        for (final String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
            if (!entry.endsWith(".jar")) {
                continue;
            }
            readModuleFromJar(entry, versionByArtifact);
        }
        return versionByArtifact;
    }

    private void readModuleFromJar(final String path, final Map<String, String> versionByArtifact) {
        final String prefix = "META-INF/maven/" + AWS_SDK_GROUP_ID + "/";

        try (final JarFile jar = new JarFile(path)) {
            final Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                if (!entry.getName().startsWith(prefix) || !entry.getName().endsWith("/pom.properties")) {
                    continue;
                }
                final Properties properties = new Properties();
                try (final InputStream in = jar.getInputStream(entry)) {
                    properties.load(in);
                }
                versionByArtifact.put(properties.getProperty("artifactId"), properties.getProperty("version"));
            }
        } catch (final IOException e) {
            throw new IllegalStateException("Could not read " + path, e);
        }
    }
}
