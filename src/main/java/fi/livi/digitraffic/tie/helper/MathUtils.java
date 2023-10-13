package fi.livi.digitraffic.tie.helper;

public class MathUtils {

    public static double floorToHalf(double number) {
        return Math.floor(number * 2.0) / 2.0;
    }

    public static double ceilToHalf(double number) {
        return Math.ceil(number * 2.0) / 2.0;
    }
}
