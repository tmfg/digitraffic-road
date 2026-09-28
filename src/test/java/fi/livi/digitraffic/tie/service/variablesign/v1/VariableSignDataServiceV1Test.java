package fi.livi.digitraffic.tie.service.variablesign.v1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fi.livi.digitraffic.tie.dao.variablesign.v1.DeviceDataRepositoryV1;
import fi.livi.digitraffic.tie.dto.variablesigns.v1.TrafficSignHistoryV1;

@ExtendWith(MockitoExtension.class)
class VariableSignDataServiceV1Test {

    @Mock
    private DeviceDataRepositoryV1 deviceDataRepositoryV1;

    @Mock
    private TestDataFilteringService testDataFilteringService;

    private VariableSignDataServiceV1 service;

    @Captor
    private ArgumentCaptor<Instant> startCaptor;

    @BeforeEach
    void setUp() {
        service = new VariableSignDataServiceV1(null, deviceDataRepositoryV1, null, testDataFilteringService);
    }

    @Test
    void listVariableSignHistoryWithoutEffectiveDateUsesPastSevenDays() {
        final String deviceId = "device-1";
        final List<TrafficSignHistoryV1> history = List.of(org.mockito.Mockito.mock(TrafficSignHistoryV1.class));
        final Instant before = Instant.now();

        when(testDataFilteringService.filter(eq(deviceId), eq(history))).thenReturn(history);
        when(deviceDataRepositoryV1.getDeviceDataByDeviceIdAndEffectDateGreaterThanEqualOrderByEffectDateDesc(eq(deviceId), any(Instant.class)))
            .thenReturn(history);

        final List<TrafficSignHistoryV1> result = service.listVariableSignHistory(deviceId, null);
        final Instant after = Instant.now();

        assertEquals(history, result);

        verify(deviceDataRepositoryV1).getDeviceDataByDeviceIdAndEffectDateGreaterThanEqualOrderByEffectDateDesc(eq(deviceId), startCaptor.capture());
        verify(deviceDataRepositoryV1, never()).getDeviceDataByDeviceIdAndEffectDateBetweenOrderByEffectDateDesc(anyString(), any(Instant.class), any(Instant.class));

        final Instant start = startCaptor.getValue();
        assertFalse(start.isBefore(before.minus(Duration.ofDays(7))), "start should be at most 7 days before the call");
        assertFalse(start.isAfter(after.minus(Duration.ofDays(7))), "start should be computed during the call");
    }

    @Test
    void listVariableSignHistoryWithEffectiveDateUsesDayWindow() {
        final String deviceId = "device-1";
        final Instant effectiveInstant = Instant.parse("2026-09-25T12:34:56Z");
        final Date effectiveDate = Date.from(effectiveInstant);
        final Instant expectedEnd = effectiveInstant.plus(Duration.ofDays(1));
        final List<TrafficSignHistoryV1> history = List.of(org.mockito.Mockito.mock(TrafficSignHistoryV1.class));

        when(testDataFilteringService.filter(eq(deviceId), eq(history))).thenReturn(history);
        when(deviceDataRepositoryV1.getDeviceDataByDeviceIdAndEffectDateBetweenOrderByEffectDateDesc(eq(deviceId), eq(effectiveInstant), eq(expectedEnd)))
            .thenReturn(history);

        final List<TrafficSignHistoryV1> result = service.listVariableSignHistory(deviceId, effectiveDate);

        assertEquals(history, result);

        verify(deviceDataRepositoryV1).getDeviceDataByDeviceIdAndEffectDateBetweenOrderByEffectDateDesc(eq(deviceId), eq(effectiveInstant), eq(expectedEnd));
        verify(deviceDataRepositoryV1, never()).getDeviceDataByDeviceIdAndEffectDateGreaterThanEqualOrderByEffectDateDesc(anyString(), any(Instant.class));
    }
}




