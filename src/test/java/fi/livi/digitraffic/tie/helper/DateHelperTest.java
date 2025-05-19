package fi.livi.digitraffic.tie.helper;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fi.livi.digitraffic.tie.AbstractTest;

public class DateHelperTest extends AbstractTest {

    private final static String ISO_DATE_TIME_WITH_Z_REGEX_PATTERN =      "([0-9]{4})-(1[0-2]|0[1-9])-([0-3][0-9])T([0-2][0-9]):([0-5][0-9]):([0-5][0-9])(\\.(\\d{3}|\\d{6}))?Z";
    private final static String ISO_DATE_TIME_WITH_OFFSET_REGEX_PATTERN = "([0-9]{4})-(1[0-2]|0[1-9])-([0-3][0-9])T([0-2][0-9]):([0-6][0-9])(:([0-6][0-9])){0,1}(\\.[0-9]{0,6})?[+|-].*";

    public static final Matcher<String> ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER = Matchers.matchesRegex(ISO_DATE_TIME_WITH_Z_REGEX_PATTERN);
    public static final Matcher<String> ISO_DATE_TIME_WITH_Z_OFFSET_CONTAINS_MATCHER = Matchers.matchesRegex("[\\s\\S]*" +
        ISO_DATE_TIME_WITH_Z_REGEX_PATTERN + "[\\s\\S]*");
    public static final Matcher<String> NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER = Matchers.not(Matchers.matchesRegex(
        ISO_DATE_TIME_WITH_OFFSET_REGEX_PATTERN));
    public static final Matcher<String> NO_ISO_DATE_TIME_WITH_OFFSET_CONTAINS_MATCHER = Matchers.not(Matchers.matchesRegex("[\\s\\S]*" +
        ISO_DATE_TIME_WITH_OFFSET_REGEX_PATTERN + "[\\s\\S]*"));
    public static final Matcher<String> ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_MATCHER =
        Matchers.allOf(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER, NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER);
    public static final Matcher<String> ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_MATCHER =
        Matchers.allOf(ISO_DATE_TIME_WITH_Z_OFFSET_CONTAINS_MATCHER, NO_ISO_DATE_TIME_WITH_OFFSET_CONTAINS_MATCHER);
    public static final ResultMatcher ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER =
        MockMvcResultMatchers.content().string(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_MATCHER);

    /**
     * This is test of test. It checks that ISO_DATE_TIME regex patterns works.
     */
    @Test
    public void isoDateTimeRegexpMatcherMatches() {
        final String DATE_TIME = "2022-01-02T10:31:21";
        final String DATE_TIME_MILLIS = "2022-01-02T10:31:21.123";
        final String DATE_TIME_MIKROS = "2022-01-02T10:31:21.123456";
        final String ZONE_Z = "Z";
        final String ZONE_OFFSET = "+01:00";
        final String RANDOM = RandomStringUtils.random(3) + "\n" + RandomStringUtils.random(3);

        // No extra befor or after allowed
        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME + ZONE_Z));
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(RANDOM + DATE_TIME + ZONE_Z));
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME + ZONE_Z + RANDOM));

        // Digits
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME + ".1" + ZONE_Z)); // 1-digit millis not allowed
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME + ".12" + ZONE_Z)); // 2-digit millis not allowed
        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME + ".123" + ZONE_Z)); // 3-digit millis ok
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME + "123" + ZONE_Z)); // 3-digit millis without dot not allowed
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + "4" + ZONE_Z)); // 4-digit micros not allowed
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + "45" + ZONE_Z)); // 5-digit micros not allowed
        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + "456" + ZONE_Z)); // 6-digit micros ok
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + "4567" + ZONE_Z)); // 7-digit micros not allowed
        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + ZONE_Z));
        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME_MIKROS + ZONE_Z));
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME + ZONE_OFFSET));
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + ZONE_OFFSET));
        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME_MIKROS + ZONE_Z));
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME_MIKROS + ZONE_OFFSET));

        // All digits should "match" iso with offset -> make no-matcher to not match as ok (offset checker can be looser to match)
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER.matches(DATE_TIME + ".1" + ZONE_OFFSET));
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER.matches(DATE_TIME + ".12" + ZONE_OFFSET));
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER.matches(DATE_TIME + ".123" + ZONE_OFFSET));
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + "4" + ZONE_OFFSET));
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + "45" + ZONE_OFFSET));
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + "456" + ZONE_OFFSET));

        Assertions.assertTrue(NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER.matches(DATE_TIME + ZONE_Z));
        Assertions.assertTrue(NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + ZONE_Z));
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER.matches(DATE_TIME + "+"));
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER.matches(DATE_TIME + ZONE_OFFSET));
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + ZONE_OFFSET));

        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_MATCHER.matches(DATE_TIME + ZONE_Z));
        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + ZONE_Z));
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_MATCHER.matches(DATE_TIME + ZONE_OFFSET));
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + ZONE_OFFSET));


        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME + ZONE_Z + RANDOM));
        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_OFFSET_CONTAINS_MATCHER.matches(RANDOM + "\n" + DATE_TIME + ZONE_Z + "\n" + RANDOM));
        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME_MILLIS + ZONE_Z + RANDOM));
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME + ZONE_OFFSET + RANDOM));
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME_MILLIS + ZONE_OFFSET + RANDOM));

        Assertions.assertTrue(NO_ISO_DATE_TIME_WITH_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME + ZONE_Z + RANDOM));
        Assertions.assertTrue(NO_ISO_DATE_TIME_WITH_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME_MILLIS + ZONE_Z + RANDOM));
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME + "+" + RANDOM));
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME + ZONE_OFFSET + RANDOM));
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME_MILLIS + ZONE_OFFSET + RANDOM));

        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME + ZONE_Z + RANDOM));
        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME_MILLIS + ZONE_Z + RANDOM));
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME + ZONE_OFFSET + RANDOM));
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME_MILLIS + ZONE_OFFSET + RANDOM));
    }
}
