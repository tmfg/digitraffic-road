package fi.livi.digitraffic.tie.metadata.dao;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.service.DataStatusService.StaticStatusType;

/**
 * Used by old daemon. Should be removed after old daemon is eol.
 */
@Deprecated
@Repository
public class StaticDataStatusDao {
    private final EntityManager entityManager;

    @Autowired
    public StaticDataStatusDao(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void updateStaticDataStatus(final StaticStatusType type, final boolean updateStaticDataStatus) {
        final Session session = entityManager.unwrap(Session.class);

        if(updateStaticDataStatus || session.isDirty()) {
            session.createSQLQuery("update static_data_status set " + type.getUpdateField() + " = current_timestamp").executeUpdate();
        }
    }
}
