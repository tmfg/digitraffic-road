package fi.livi.digitraffic.tie.dao;

import javax.persistence.EntityManager;

import fi.livi.digitraffic.tie.service.StaticDataStatusServiceImpl;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class StaticDataStatusDAO {
    private final EntityManager entityManager;

    @Autowired
    public StaticDataStatusDAO(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void updateStaticDataStatus(StaticDataStatusServiceImpl.StaticStatusType type, final boolean updateStaticDataStatus) {
        final Session session = entityManager.unwrap(Session.class);

        if(updateStaticDataStatus || session.isDirty()) {
            session.createSQLQuery("update static_data_status set " + type.getUpdateField() + " = current_date").executeUpdate();
        }
    }
}
