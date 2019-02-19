package fi.livi.digitraffic.tie.data.dao;

import java.math.BigDecimal;
import java.sql.Timestamp;

import fi.ely.lotju.lam.proto.LAMRealtimeProtos;
import fi.ely.lotju.tiesaa.proto.TiesaaProtos;
import fi.livi.digitraffic.tie.helper.NumberConverter;
import fi.livi.digitraffic.tie.helper.TimestampCache;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

public class SensorValueUpdateParameterDto {

    private final BigDecimal value;
    private final Timestamp measured;
    private final long roadSationId;
    private final long sensorLotjuId;
    private final String stationType;
    private final Timestamp timeWindowStart;
    private final Timestamp timeWindowEnd;

    public SensorValueUpdateParameterDto(final LAMRealtimeProtos.Lam lam, final LAMRealtimeProtos.Lam.Anturi anturi,
                                         long roadSationId, TimestampCache timestampCache) {
        this.value = BigDecimal.valueOf(anturi.getArvo());
        this.measured = timestampCache.get(lam.getAika());
        this.roadSationId = roadSationId;
        this.sensorLotjuId = anturi.getLaskennallinenAnturiId();
        this.stationType = RoadStationType.TMS_STATION.name();
        this.timeWindowStart = anturi.hasAikaikkunaAlku() ? timestampCache.get(anturi.getAikaikkunaAlku()) : null;
        this.timeWindowEnd = anturi.hasAikaikkunaLoppu() ? timestampCache.get(anturi.getAikaikkunaLoppu()) : null;
    }

    public SensorValueUpdateParameterDto(final TiesaaProtos.TiesaaMittatieto tiesaa, final TiesaaProtos.TiesaaMittatieto.Anturi anturi,
                                         final Long roadSationId, final TimestampCache timestampCache) {
        this.value = NumberConverter.convertAnturiValueToBigDecimal(anturi.getArvo());
        this.measured = timestampCache.get(tiesaa.getAika());
        this.roadSationId = roadSationId;
        this.sensorLotjuId = anturi.getLaskennallinenAnturiId();
        this.stationType = RoadStationType.WEATHER_STATION.name();
        this.timeWindowStart = null;
        this.timeWindowEnd = null;
    }

    public BigDecimal getValue() {
        return value;
    }

    public Timestamp getMeasured() {
        return measured;
    }

    public long getRoadSationId() {
        return roadSationId;
    }

    public long getSensorLotjuId() {
        return sensorLotjuId;
    }

    public String getStationType() {
        return stationType;
    }

    public Timestamp getTimeWindowStart() {
        return timeWindowStart;
    }

    public Timestamp getTimeWindowEnd() {
        return timeWindowEnd;
    }
}
