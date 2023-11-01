package fi.livi.digitraffic.tie.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtils {

    public static double floorToHalf(double number) {
        return Math.floor(number * 2.0) / 2.0;
    }

    public static double ceilToHalf(double number) {
        return Math.ceil(number * 2.0) / 2.0;
    }

    /**
     * Rounds no nearest half using given scale
     * @param number to round
     * @param scale scale to round to 
     * @return rounded value
     * 
     * @see java.math.BigDecimal#setScale(int, RoundingMode) 
     */
    public static double roundToScale(final double number, final int scale) {
        return BigDecimal.valueOf(number).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

}
