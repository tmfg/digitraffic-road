package fi.livi.digitraffic.tie.data.dao;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.data.model.Datex2;
import fi.livi.digitraffic.tie.data.model.Datex2MessageType;

public class Datex2RepositoryTest extends AbstractTest {

    @Autowired
    private Datex2Repository datex2Repository;

    @Before
    public void insertDatex() {
        final Datex2 datex2 = new Datex2();

        datex2.setImportTime(ZonedDateTime.now());
        datex2.setMessageType(Datex2MessageType.ROADWORK);
        datex2.setMessage("Message of high importance");

        datex2Repository.save(datex2);
    }

    @Test
    public void testDelete() {
        final List<Datex2> all = datex2Repository.findAll();
        assertCollectionSize(1, all);

        datex2Repository.delete(all.get(0));

        final List<Datex2> after = datex2Repository.findAll();
        assertCollectionSize(0, after);
    }
}
