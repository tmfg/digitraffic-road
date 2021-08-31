package fi.livi.digitraffic.tie.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v1.datex2.Datex2;

@Repository
public interface WazeDatex2Repository extends JpaRepository<Datex2, Long> {

    String FIND_ALL_ACTIVE =
        "with situation as (\n" +
        "    select distinct on (ds.situation_id)\n" +
        "        ds.situation_id\n" +
        "      , ds.id\n" +
        "      , ds.datex2_id\n" +
        "    from datex2_situation ds\n" +
        "    order by ds.situation_id desc, ds.id desc\n" +
        ")\n" +
        "select d2.*\n" +
        "from datex2 d2\n" +
        "join situation s on s.datex2_id = d2.id\n" +
        "where d2.situation_type = 'TRAFFIC_ANNOUNCEMENT'\n" +
        "  and d2.traffic_announcement_type = 'ACCIDENT_REPORT'\n" +
        "  and d2.json_message is not null\n" +
        "  and exists(\n" +
        "        select null\n" +
        "        from datex2_situation_record dsr\n" +
        "        where dsr.datex2_situation_id = s.id\n" +
        "          and dsr.effective_end_time > current_timestamp\n" +
        "    )\n" +
        "order by d2.publication_time, d2.id";

    @Query(value = FIND_ALL_ACTIVE, nativeQuery = true)
    List<Datex2> findAllActive();

}