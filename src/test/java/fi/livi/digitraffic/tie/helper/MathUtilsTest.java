package fi.livi.digitraffic.tie.helper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import fi.livi.digitraffic.tie.AbstractTest;

public class MathUtilsTest extends AbstractTest {

    @Test
    public void floorToHalf() {
        Assertions.assertEquals(0, MathUtils.floorToHalf(0.0));
        Assertions.assertEquals(0.0, MathUtils.floorToHalf(0.1));
        Assertions.assertEquals(0.0, MathUtils.floorToHalf(0.499999999999));
        Assertions.assertEquals(0.5, MathUtils.floorToHalf(0.5));
        Assertions.assertEquals(0.5, MathUtils.floorToHalf(0.500000000001));
        Assertions.assertEquals(0.5, MathUtils.floorToHalf(0.999999999999));
        Assertions.assertEquals(1.0, MathUtils.floorToHalf(1.0));
        Assertions.assertEquals(1.0, MathUtils.floorToHalf(1.000000000001));
        Assertions.assertEquals(1.0, MathUtils.floorToHalf(1.499999999999));
        Assertions.assertEquals(1.5, MathUtils.floorToHalf(1.5));
        Assertions.assertEquals(1.5, MathUtils.floorToHalf(1.500000000001));
    }

    @Test
    public void ceilToHalf() {
        Assertions.assertEquals(0, MathUtils.ceilToHalf(0.0));
        Assertions.assertEquals(0.5, MathUtils.ceilToHalf(0.1));
        Assertions.assertEquals(0.5, MathUtils.ceilToHalf(0.499999999999));
        Assertions.assertEquals(0.5, MathUtils.ceilToHalf(0.5));
        Assertions.assertEquals(1.0, MathUtils.ceilToHalf(0.500000000001));
        Assertions.assertEquals(1.0, MathUtils.ceilToHalf(0.999999999999));
        Assertions.assertEquals(1.0, MathUtils.ceilToHalf(1.0));
        Assertions.assertEquals(1.5, MathUtils.ceilToHalf(1.000000000001));
        Assertions.assertEquals(1.5, MathUtils.ceilToHalf(1.499999999999));
        Assertions.assertEquals(1.5, MathUtils.ceilToHalf(1.5));
        Assertions.assertEquals(2.0, MathUtils.ceilToHalf(1.500000000001));
    }

}
