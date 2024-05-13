package fi.livi.digitraffic.tie;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class ForeignKeyIndexTest extends AbstractJpaTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String IGNORED_CONSTRAINT_NAMES_REGEX =
//            ".*ABC.*" +
//            "|CDE" +
            // No fk-index to save space and have better performance for insert and updates
            "maintenance_tracking_data_tracking_tracking_id_fkey";

    @Test
    public void testForeignKeysHaveIndex() {
        final String sql = """
            with constraints as (
                select a.table_name
                     , a.constraint_name
                     , string_agg(b.column_name, ' ' order by b.ordinal_position) cols
                from information_schema.table_constraints a, information_schema.key_column_usage b
                where a.constraint_name = b.constraint_name
                  and a.constraint_type = 'FOREIGN KEY'
                group by a.table_name, a.constraint_name
            ), indexes as (
                select t.relname as table_name
                     , i.relname as index_name
                     , array_to_string(array_agg(a.attname order by a.attnum), ' ') as cols
                from pg_class t
                   , pg_class i
                   , pg_index ix
                   , pg_attribute a
                where t.oid = ix.indrelid
                  and i.oid = ix.indexrelid
                  and a.attrelid = i.oid
                  and t.relkind = 'r'
                group by t.relname 
                       , i.relname
                order by t.relname
                       , i.relname
            )
            select * from constraints
            where not exists(select * from indexes where indexes.cols like '' || constraints.cols || '%')""";


        final List<Map<String, Object>> foreignKeysWithoutIndex =
                jdbcTemplate.queryForList(sql)
                        .stream()
                        .filter(fk -> !fk.get("CONSTRAINT_NAME").toString().matches(IGNORED_CONSTRAINT_NAMES_REGEX))
                        .collect(Collectors.toList());

        final StringBuilder sb = new StringBuilder();

        for (final Map<String, Object> stringObjectMap : foreignKeysWithoutIndex) {
            sb.append("CREATE INDEX ");
            sb.append(stringObjectMap.get("CONSTRAINT_NAME").toString().toUpperCase());
            sb.append("_I ON ");
            sb.append(stringObjectMap.get("TABLE_NAME").toString().toUpperCase());
            sb.append(" USING BTREE (");
            sb.append(StringUtils.replace(stringObjectMap.get("COLS").toString(), " ", ", "));
            sb.append("); -- ");
            sb.append(stringObjectMap);
            sb.append("\n");
        }

        assertTrue(foreignKeysWithoutIndex.isEmpty(),
                "Found foreign key(s) without index. Add to the ignore list or create indexes. Something like: \n\n" + sb.toString());
    }
}
