package fi.livi.digitraffic.tie.data.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.data.dto.daydata.HistoryRootDataObjectDto;

public class DayDataServiceTest extends AbstractTest {

    private long days = 0;
    private final LocalDate date = LocalDate.of(2015, 8, 25);
    private final LocalDate yesterday = LocalDate.now().minusDays(1);

    @Autowired
    private DayDataService dayDataService;

    @Before
    public void alterEndTimeStamp() {
        days = ChronoUnit.DAYS.between(date, LocalDate.now()) - 1;

        jdbcTemplate.update("update journeytime_median set end_timestamp = end_timestamp + ?", days);
    }

    @After
    public void restoreEndTimestamp() {
        jdbcTemplate.update("update journeytime_median set end_timestamp = end_timestamp - ?", days);
    }

    @Test
    public void testLinkHistoryYearMonth() {
        HistoryRootDataObjectDto linkHistoryYearMonth = dayDataService.listHistoryData(4, yesterday.getYear(), yesterday.getMonthValue());
        Assert.assertTrue(!linkHistoryYearMonth.getLinks().isEmpty());
        Assert.assertTrue(linkHistoryYearMonth.getLinks().get(0).getMeasuredTime() != null);
        Assert.assertTrue(!linkHistoryYearMonth.getLinks().get(0).getLinkMeasurements().isEmpty());
        Assert.assertTrue(linkHistoryYearMonth.getLinks().get(0).getLinkMeasurements().get(0).getMeasuredTime() != null);
    }

}
