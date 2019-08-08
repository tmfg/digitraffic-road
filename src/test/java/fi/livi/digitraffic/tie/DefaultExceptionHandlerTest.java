package fi.livi.digitraffic.tie;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import javax.validation.ConstraintViolationException;

import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.ResourceAccessException;

import fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration;
import fi.livi.digitraffic.tie.data.controller.DataController;
import fi.livi.digitraffic.tie.data.dto.tms.TmsRootDataObjectDto;
import fi.livi.digitraffic.tie.data.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.data.service.TmsDataService;
import fi.livi.digitraffic.tie.service.BadRequestException;

@TestPropertySource(properties = {
    "road.datasource.hikari.maximum-pool-size=1",
})
public class DefaultExceptionHandlerTest extends AbstractRestWebTest {
    @MockBean
    private TmsDataService tmsDataService;

    private ResultActions performQuery() throws Exception {
        return mockMvc.perform(get(RoadWebApplicationConfiguration.API_V1_BASE_PATH +
            RoadWebApplicationConfiguration.API_DATA_PART_PATH +
            DataController.TMS_DATA_PATH + "/1"));
    }

    @Test
    public void ok() throws Exception {
        when(tmsDataService.findPublishableTmsData(anyLong())).thenReturn(new TmsRootDataObjectDto(ZonedDateTime.now()));

        performQuery()
            .andExpect(status().isOk());

    }

    private <T extends Throwable> void testException(final Class<T> clazz, final int statuscode) throws Exception {
        when(tmsDataService.findPublishableTmsData(anyLong())).thenThrow(mock(clazz));

        performQuery()
            .andExpect(status().is(statuscode));

    }

    @Test
    public void notFoundException() throws Exception {
        testException(ObjectNotFoundException.class, 404);
    }

    @Test
    public void badRequestException() throws Exception {
        testException(BadRequestException.class, 400);
    }

    @Test
    public void resourceAccessException() throws Exception {
        testException(ResourceAccessException.class, 500);
    }

    @Test
    public void illegalArgumentException() throws Exception {
        testException(IllegalArgumentException.class, 400);
    }

    @Test
    public void constraintViolationException() throws Exception {
        testException(ConstraintViolationException.class, 400);
    }

    @Test
    public void httpMessageNotWritableException() throws Exception {
        testException(HttpMessageNotWritableException.class, 500);
    }

    @Test
    public void httpMessageNotReadableException() throws Exception {
        testException(HttpMessageNotReadableException.class, 400);
    }

}
