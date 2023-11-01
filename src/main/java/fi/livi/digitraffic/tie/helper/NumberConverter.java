package fi.livi.digitraffic.tie.helper;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.google.protobuf.ByteString;

import fi.ely.lotju.tiesaa.proto.TiesaaProtos;

public class NumberConverter {

    private NumberConverter() {}

    public static double convertAnturiValueToDouble(final TiesaaProtos.TiesaaMittatieto.Anturi.BDecimal arvo) {
        return convertAnturiValueToBigDecimal(arvo).doubleValue();
    }

    public static BigDecimal convertAnturiValueToBigDecimal(final TiesaaProtos.TiesaaMittatieto.Anturi.BDecimal arvo) {
        return new BigDecimal(new BigInteger(arvo.getBigintValue().getValue().toByteArray()), arvo.getScale());
    }

    public static TiesaaProtos.TiesaaMittatieto.Anturi.BDecimal convertDoubleValueToBDecimal(final double value) {
        final BigDecimal tmpValue = BigDecimal.valueOf(value);

        final TiesaaProtos.TiesaaMittatieto.Anturi.BDecimal.BInteger.Builder biBuilder = TiesaaProtos.TiesaaMittatieto.Anturi.BDecimal.BInteger.newBuilder();
        biBuilder.setValue(ByteString.copyFrom(tmpValue.unscaledValue().toByteArray()));

        final TiesaaProtos.TiesaaMittatieto.Anturi.BDecimal.Builder bdBuilder = TiesaaProtos.TiesaaMittatieto.Anturi.BDecimal.newBuilder();
        bdBuilder.setScale(tmpValue.scale());
        bdBuilder.setBigintValue(biBuilder.build());

        return bdBuilder.build();
    }
}
