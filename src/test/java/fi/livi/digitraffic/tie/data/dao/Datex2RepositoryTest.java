package fi.livi.digitraffic.tie.data.dao;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.base.MetadataIntegrationTest;
import fi.livi.digitraffic.tie.data.model.Datex2;

@Transactional
public class Datex2RepositoryTest extends MetadataIntegrationTest {

    @Autowired
    private Datex2Repository datex2Repository;

    @Test
    public void testDelete() {
        List<Datex2> all = datex2Repository.findAll();
        int before = all.size();
        Assert.assertTrue(before > 0);
        for (Datex2 datex2 : all) {
            if (!datex2.getSituations().isEmpty()) {
                datex2Repository.delete(datex2);
                break;
            }
        }
        all = datex2Repository.findAll();
        Assert.assertTrue(all.size() == before-1);
    }
}