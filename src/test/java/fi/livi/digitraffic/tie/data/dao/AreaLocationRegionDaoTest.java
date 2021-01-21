package fi.livi.digitraffic.tie.data.dao;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;

import fi.livi.digitraffic.tie.AbstractJpaTest;
import fi.livi.digitraffic.tie.dao.v3.AreaLocationRegionRepository;
import fi.livi.digitraffic.tie.helper.AssertHelper;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.geojson.LineString;
import fi.livi.digitraffic.tie.model.v3.trafficannouncement.geojson.AreaLocationRegion;

@Import({ JacksonAutoConfiguration.class })
public class AreaLocationRegionDaoTest extends AbstractJpaTest {
    private static final Logger log = LoggerFactory.getLogger(AreaLocationRegionDaoTest.class);

    @Autowired
    private AreaLocationRegionRepository areaLocationRegionRepository;

    @Test
    public void savedAndLoadedEquals() {

        final List<List<Double>> coordinates = Arrays
            .asList(Arrays.asList(28.954077, 69.027261), Arrays.asList(28.839614, 69.080281), Arrays.asList(28.827573, 69.090771),
                    Arrays.asList(28.826953, 69.106842), Arrays.asList(28.84411, 69.181256), Arrays.asList(28.852171, 69.203529));
        final LineString lineString = new LineString(coordinates);
        areaLocationRegionRepository.deleteAll();

        final AreaLocationRegion src = new AreaLocationRegion(1, "municipality",
            DateHelper.getZonedDateTimeNowWithoutMillisAtUtc().minusHours(1).toInstant(), lineString,
            DateHelper.getZonedDateTimeNowWithoutMillisAtUtc().toInstant());
        areaLocationRegionRepository.save(src);
        entityManager.flush();
        entityManager.clear();

        final List<AreaLocationRegion> result = areaLocationRegionRepository.findAll();
        AssertHelper.assertCollectionSize(1, result);
        final AreaLocationRegion tgt = result.get(0);

        assertEquals(src.getGeometry().getCoordinates(), tgt.getGeometry().getCoordinates());
        assertEquals(src.getGeometry().getType(), tgt.getGeometry().getType());
        assertEquals(src.getEffectiveDate(), tgt.getEffectiveDate());
        assertEquals(src.getId(), tgt.getId());
        assertEquals(src.getLocationCode(), tgt.getLocationCode());
        assertEquals(src.getVersionDate(), tgt.getVersionDate());
        assertEquals(src.getType(), tgt.getType());
    }



}
