package fi.livi.digitraffic.tie.dao.data;

import fi.livi.digitraffic.tie.model.data.DataIncoming;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataIncomingRepository extends JpaRepository<DataIncoming, Long> {
    List<DataIncoming> findByMessageId(final String messageId);

    @Query("select data from DataIncoming data where data.status = 'NEW' and data.source='JMS'")
    List<DataIncoming> findAllUnhandled();
}
