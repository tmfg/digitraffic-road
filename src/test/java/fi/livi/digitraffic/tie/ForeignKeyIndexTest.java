package fi.livi.digitraffic.tie;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import fi.livi.digitraffic.tie.base.MetadataIntegrationTest;

public class ForeignKeyIndexTest extends MetadataIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String IGNORED_CONSTRAINT_NAMES_REGEX =
//            ".*ABC.*" +
//            "|CDE" +
            "";

    @Test
    public void testForeignKeysHaveIndex() {
        String sql =
                "select c.table_name\n" +
                "     , c.constraint_name\n" +
                "     , c.cols\n" +
                "from (\n" +
                "  select a.table_name\n" +
                "       , a.constraint_name\n" +
                "       , listagg(b.column_name, ' ' ) \n" +
                "          within group (order by column_name) cols\n" +
                "      from user_constraints a, user_cons_columns b\n" +
                "     where a.constraint_name = b.constraint_name\n" +
                "       and a.constraint_type = 'R'\n" +
                "  group by a.table_name, a.constraint_name\n" +
                " ) c\n" +
                " left outer join\n" +
                " (\n" +
                "  select table_name\n" +
                "       , index_name\n" +
                "       , cr\n" +
                "       , listagg(column_name, ' ' ) \n" +
                "          within group (order by column_name) cols\n" +
                "    from (\n" +
                "        select table_name\n" +
                "             , index_name\n" +
                "             , column_position\n" +
                "             , column_name\n" +
                "             , connect_by_root(column_name) cr\n" +
                "          from user_ind_columns\n" +
                "       connect by prior column_position-1 = column_position\n" +
                "              and prior index_name = index_name\n" +
                "         )\n" +
                "    group by table_name, index_name, cr\n" +
                ") i on c.cols = i.cols and c.table_name = i.table_name\n" +
                "\n" +
                "where i.index_name is null\n" +
                "order by table_name, cols";

        List<Map<String, Object>> foreignKeysWithoutIndex =
                jdbcTemplate.queryForList(sql)
                        .stream()
                        .filter(fk -> !fk.get("CONSTRAINT_NAME").toString().matches(IGNORED_CONSTRAINT_NAMES_REGEX))
                        .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();

        for (Map<String, Object> stringObjectMap : foreignKeysWithoutIndex) {
            sb.append("CREATE INDEX ");
            sb.append(StringUtils.substring(stringObjectMap.get("CONSTRAINT_NAME").toString(), 0, 28));
            sb.append("_I ON ");
            sb.append(stringObjectMap.get("TABLE_NAME"));
            sb.append(" (");
            sb.append(StringUtils.replace(stringObjectMap.get("COLS").toString(), " ", ", "));
            sb.append(") TABLESPACE STP_IDX; -- ");
            sb.append(stringObjectMap);
            sb.append("\n");
        }
        Assert.assertTrue(
                "Found foreign key(s) without index. Add to the ignore list or create indexes. Something like: \n" + sb.toString(),
                foreignKeysWithoutIndex.isEmpty());
    }
}
