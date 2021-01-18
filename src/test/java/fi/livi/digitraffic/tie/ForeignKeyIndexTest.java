package fi.livi.digitraffic.tie;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class ForeignKeyIndexTest extends AbstractServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String IGNORED_CONSTRAINT_NAMES_REGEX =
//            ".*ABC.*" +
//            "|CDE" +
            // No fk-index to save space and have better performance for insert and updates
            "maintenance_tracking_data_tracking_tracking_id_fkey";

    @Test
    public void testForeignKeysHaveIndex() {
        final String sql =
            "with constraints as (\n" +
                "  select a.table_name\n" +
                "       , a.constraint_name\n" +
                "       , string_agg(b.column_name, ' ' order by b.ordinal_position) cols\n" +
                "      from information_schema.table_constraints a, information_schema.key_column_usage b\n" +
                "     where a.constraint_name = b.constraint_name\n" +
                "       and a.constraint_type = 'FOREIGN KEY'\n" +
                "  group by a.table_name, a.constraint_name\n" +
                " ), indexes as (\n" +
                "select\n" +
                "    t.relname as table_name,\n" +
                "    i.relname as index_name,\n" +
                "    array_to_string(array_agg(a.attname order by a.attnum), ' ') as cols\n" +
                "from\n" +
                "    pg_class t,\n" +
                "    pg_class i,\n" +
                "    pg_index ix,\n" +
                "    pg_attribute a\n" +
                "where\n" +
                "    t.oid = ix.indrelid\n" +
                "    and i.oid = ix.indexrelid\n" +
                "    and a.attrelid = i.oid\n" +
                "    and t.relkind = 'r'\n" +
                "group by\n" + "    t.relname,\n" + "    i.relname\n" +
                "order by\n" + "    t.relname,\n" + "    i.relname\n" +
                ")\n" +
                "select * from constraints\n" +
                "where not exists(select * from indexes where indexes.cols like '' || constraints.cols || '%');\n";


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
        Assert.assertTrue(
                "Found foreign key(s) without index. Add to the ignore list or create indexes. Something like: \n\n" + sb.toString(),
                foreignKeysWithoutIndex.isEmpty());
    }
}
