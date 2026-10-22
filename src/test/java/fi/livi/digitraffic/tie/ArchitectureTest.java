package fi.livi.digitraffic.tie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

import fi.livi.digitraffic.common.annotation.NotTransactionalServiceMethod;

/**
 * <a href="https://www.archunit.org/userguide/html/000_Index.html">ArchUnit User Guide</a>
 */
public class ArchitectureTest extends AbstractTest {
    private static final Logger log = LoggerFactory.getLogger(ArchitectureTest.class);

    private static final List<Path> SOURCE_ROOTS = List.of(Path.of("src/main/java"), Path.of("src/test/java"));
    private static final Pattern IMPORT_PATTERN = Pattern.compile("^\\s*import\\s+(?:static\\s+)?([\\w.]+)\\s*;");

    private final JavaClasses importedClasses;

    public ArchitectureTest() {
        final StopWatch timer = StopWatch.createStarted();
        importedClasses = new ClassFileImporter()
            .withImportOption(location ->
                location.contains("/target/classes/") &&
                location.contains("fi/livi/digitraffic") &&
                // Skip generated classes
                !location.contains("fi/livi/digitraffic/tie/external"))
            .importPath("target/classes/fi/livi/digitraffic/tie/");

        log.info("Reading classes took {} s", timer.getDuration().toMillis()/1000.0);
    }

    @Test
    public void publicServiceMethodMustBeTransactional() {
        ArchRuleDefinition
            .methods()
            .that()
            .arePublic()
            .and()
            .areNotStatic()
            .and()
            .areDeclaredInClassesThat()
            .areAnnotatedWith(Service.class)
            .and()
            // Skip test classes
            .areDeclaredInClassesThat()
            .haveSimpleNameNotContaining("Test")
            .and()
            .areNotAnnotatedWith(NotTransactionalServiceMethod.class)
            .should()
            .beAnnotatedWith(Transactional.class)
            .check(importedClasses);
    }

    @Test
    public void classesAnnotatedWithComponentShouldNotHaveServiceInName() {
        ArchRuleDefinition
            .classes()
            .that()
            .areAnnotatedWith(Component.class)
            .should()
            .haveSimpleNameNotContaining("Service")
            .check(importedClasses);
    }

    @Test
    public void classesAnnotatedWithConfigurationNameShouldEndWithConfiguration() {
        ArchRuleDefinition
            .classes()
            .that()
            .areAnnotatedWith(Configuration.class)
            .should()
            .haveSimpleNameEndingWith("Configuration")
            .check(importedClasses);
    }

    /**
     * Unused imports cannot be detected from bytecode, because the compiler discards them, so this
     * rule scans the Java sources directly.
     *
     * <p>An import is reported only when its simple name appears nowhere in the file's code or
     * javadoc. Javadoc is searched because {@code @link} references are a legitimate use of an
     * import, while string literals, character literals, text blocks and ordinary comments are
     * excluded, because a class name mentioned there does not use the import. Wildcard imports are
     * skipped, since their usage cannot be resolved from the source alone.
     *
     * <p>The exclusion is done before the imports are collected, so that an import declaration
     * written inside a text block or a comment is not mistaken for a real one.
     */
    @Test
    public void noUnusedImports() throws IOException {
        final List<String> violations = new ArrayList<>();
        for (final Path root : SOURCE_ROOTS) {
            try (final Stream<Path> paths = Files.walk(root)) {
                for (final Path file : paths.filter(p -> p.toString().endsWith(".java")).sorted().toList()) {
                    violations.addAll(findUnusedImports(file));
                }
            }
        }
        assertTrue(violations.isEmpty(),
                () -> String.format("Found %d unused import(s), remove them:%n%s",
                        violations.size(), String.join(System.lineSeparator(), violations)));
    }

    private static List<String> findUnusedImports(final Path file) throws IOException {
        return findUnusedImports(file.toString(), Files.readString(file));
    }

    static List<String> findUnusedImports(final String fileName, final String source) {
        final String[] lines = codeAndJavadocOf(source).split("\n", -1);

        final List<String> importedNames = new ArrayList<>();
        final List<Integer> importLineNumbers = new ArrayList<>();
        final StringBuilder body = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            final Matcher matcher = IMPORT_PATTERN.matcher(lines[i]);
            if (matcher.find()) {
                // Usage of a wildcard import cannot be resolved from the source
                if (!matcher.group(1).endsWith(".*")) {
                    importedNames.add(matcher.group(1));
                    importLineNumbers.add(i + 1);
                }
            } else {
                body.append(lines[i]).append('\n');
            }
        }
        if (importedNames.isEmpty()) {
            return List.of();
        }

        final String searchable = body.toString();
        final List<String> unused = new ArrayList<>();
        for (int i = 0; i < importedNames.size(); i++) {
            final String imported = importedNames.get(i);
            final String simpleName = imported.substring(imported.lastIndexOf('.') + 1);
            if (!Pattern.compile("\\b" + Pattern.quote(simpleName) + "\\b").matcher(searchable).find()) {
                unused.add(String.format("%s:%d: %s", fileName, importLineNumbers.get(i), imported));
            }
        }
        return unused;
    }

    /**
     * Returns the source with string literals, character literals, text blocks, line comments and
     * non-javadoc block comments blanked out, keeping code and javadoc. Line breaks are kept and
     * every removed character is replaced by a space, so positions in the result match the source.
     *
     * <p>A plain regular expression is not enough here: a string such as
     * {@code "classpath:/lotju/datex2/*.xml"} contains a sequence that opens a block comment, which
     * would make a naive strip swallow all the code that follows it.
     */
    private static String codeAndJavadocOf(final String source) {
        final StringBuilder out = new StringBuilder(source.length());
        int i = 0;
        while (i < source.length()) {
            if (source.startsWith("\"\"\"", i)) {
                final int end = endOfTextBlock(source, i);
                blank(out, source, i, end);
                i = end;
            } else if (source.charAt(i) == '"' || source.charAt(i) == '\'') {
                final int end = skipLiteral(source, i);
                blank(out, source, i, end);
                i = end;
            } else if (source.startsWith("//", i)) {
                final int end = source.indexOf('\n', i);
                final int to = end < 0 ? source.length() : end;
                blank(out, source, i, to);
                i = to;
            } else if (source.startsWith("/**", i)) {
                final int end = source.indexOf("*/", i + 3);
                final int to = end < 0 ? source.length() : end + 2;
                out.append(source, i, to);
                i = to;
            } else if (source.startsWith("/*", i)) {
                final int end = source.indexOf("*/", i + 2);
                final int to = end < 0 ? source.length() : end + 2;
                blank(out, source, i, to);
                i = to;
            } else {
                out.append(source.charAt(i));
                i++;
            }
        }
        return out.toString();
    }

    private static void blank(final StringBuilder out, final String source, final int from, final int to) {
        for (int i = from; i < to; i++) {
            out.append(source.charAt(i) == '\n' ? '\n' : ' ');
        }
    }

    /**
     * Returns the position after the closing delimiter of the text block that starts at
     * {@code start}. An escaped character is skipped, because a text block may contain
     * {@code \"""} without that ending it.
     */
    private static int endOfTextBlock(final String source, final int start) {
        int i = start + 3;
        while (i < source.length()) {
            if (source.charAt(i) == '\\') {
                i += 2;
            } else if (source.startsWith("\"\"\"", i)) {
                return i + 3;
            } else {
                i++;
            }
        }
        return source.length();
    }

    private static int skipLiteral(final String source, final int start) {
        final char quote = source.charAt(start);
        int i = start + 1;
        while (i < source.length()) {
            final char c = source.charAt(i);
            if (c == '\\') {
                i += 2;
            } else if (c == quote) {
                return i + 1;
            } else if (c == '\n') {
                return i;
            } else {
                i++;
            }
        }
        return i;
    }

    @Test
    public void unusedImportIsReportedWithItsLineNumber() {
        final String source = """
                package fi.livi.digitraffic.tie;

                import java.util.List;
                import java.util.Set;

                public class Example {
                    private Set<String> values;
                }
                """;
        assertEquals(List.of("Example.java:3: java.util.List"), findUnusedImports("Example.java", source));
    }

    @Test
    public void nameUsedOnlyInJavadocKeepsTheImport() {
        final String source = """
                package fi.livi.digitraffic.tie;

                import java.util.List;

                /**
                 * See {@link List}.
                 */
                public class Example {
                }
                """;
        assertEquals(List.of(), findUnusedImports("Example.java", source));
    }

    @Test
    public void nameUsedOnlyInAStringLiteralDoesNotKeepTheImport() {
        final String source = """
                package fi.livi.digitraffic.tie;

                import java.util.List;

                public class Example {
                    private final String name = "List";
                }
                """;
        assertEquals(List.of("Example.java:3: java.util.List"), findUnusedImports("Example.java", source));
    }

    @Test
    public void importWrittenInsideATextBlockIsNotAnImport() {
        final String source = """
                package fi.livi.digitraffic.tie;

                import java.util.Set;

                public class Example {
                    private final String example = ""\"
                            import java.util.List;
                            ""\";
                    private Set<String> values;
                }
                """;
        assertEquals(List.of(), findUnusedImports("Example.java", source));
    }

    @Test
    public void importWrittenInsideABlockCommentIsNotAnImport() {
        final String source = """
                package fi.livi.digitraffic.tie;

                import java.util.Set;

                /*
                import java.util.List;
                */
                public class Example {
                    private Set<String> values;
                }
                """;
        assertEquals(List.of(), findUnusedImports("Example.java", source));
    }

    @Test
    public void escapedDelimiterDoesNotEndATextBlock() {
        final String source = """
                package fi.livi.digitraffic.tie;

                import java.util.Set;

                public class Example {
                    private final String quotes = ""\"
                            \\""\" import java.util.List;
                            ""\";
                    private Set<String> values;
                }
                """;
        assertEquals(List.of(), findUnusedImports("Example.java", source));
    }
}
