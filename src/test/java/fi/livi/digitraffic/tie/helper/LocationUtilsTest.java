package fi.livi.digitraffic.tie.helper;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.util.Assert;

import fi.livi.digitraffic.tie.AbstractTest;

public class LocationUtilsTest extends AbstractTest {

    @Test
    public void sameVersion() {
        Assert.equals(0, LocationUtils.compareTypesOrVersions("1.2.3", "1.2.3"));
    }

    @Test
    public void firstVersionGreater() {
        Assert.equals(1, LocationUtils.compareTypesOrVersions("1.2.4", "1.2.3"));
        Assert.equals(1, LocationUtils.compareTypesOrVersions("1.3.3", "1.2.3"));
        Assert.equals(1, LocationUtils.compareTypesOrVersions("2.2.3", "1.2.3"));
        Assert.equals(1, LocationUtils.compareTypesOrVersions("10.2.3", "1.2.3"));
    }

    @Test
    public void secondVersionGreater() {
        Assert.equals(-1, LocationUtils.compareTypesOrVersions("1.2.3", "1.2.4"));
        Assert.equals(-1, LocationUtils.compareTypesOrVersions("1.2.3", "1.3.3"));
        Assert.equals(-1, LocationUtils.compareTypesOrVersions("1.2.3", "2.2.3"));
        Assert.equals(-1, LocationUtils.compareTypesOrVersions("1.2.3", "10.2.3"));
    }


    @Test
    public void same() {
        Assert.equals(0, LocationUtils.compareTypesOrVersions("A1.2.3", "A1.2.3"));
    }

    @Test
    public void firstTypeAlphaGreater() {
        Assert.equals(1, LocationUtils.compareTypesOrVersions("B1.2.3", "A1.2.3"));
    }

    @Test
    public void secondTypeAlphaGreater() {
        Assert.equals(-1, LocationUtils.compareTypesOrVersions("B1.2.3", "C1.2.3"));
    }

    @Test
    public void firstTypeNumberGreater1() {
        Assert.equals(1, LocationUtils.compareTypesOrVersions("A2.2.3", "A1.2.3"));
        Assert.equals(1, LocationUtils.compareTypesOrVersions("A1.3.3", "A1.2.3"));
        Assert.equals(1, LocationUtils.compareTypesOrVersions("A1.2.4", "A1.2.3"));
    }

    @Test
    public void secondTypeNumberGreater1() {
        Assert.equals(-1, LocationUtils.compareTypesOrVersions("A1.2.3", "A2.2.3"));
        Assert.equals(-1, LocationUtils.compareTypesOrVersions("A1.2.3", "A1.3.3"));
        Assert.equals(-1, LocationUtils.compareTypesOrVersions("A1.2.3", "A1.2.4"));
    }
}
