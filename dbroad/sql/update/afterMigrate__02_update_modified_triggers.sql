CREATE OR REPLACE PROCEDURE create_modified_trigger_on_all_tables()
  LANGUAGE PLPGSQL
AS
$$
DECLARE
  _sql VARCHAR;
BEGIN
  FOR _sql IN
    SELECT 'DROP TRIGGER IF EXISTS ' || table_name || '_modified_t on ' || table_name || '; ' ||
           'CREATE TRIGGER ' || table_name || '_modified_t BEFORE UPDATE ON ' || table_name ||
           ' FOR EACH ROW EXECUTE PROCEDURE update_modified_column();' AS drop_and_create_trigger_query
    FROM information_schema.columns
    WHERE table_schema = 'public'
      AND column_name = 'modified'
    LOOP
      EXECUTE _sql;
    END LOOP;
END;
$$;

-- create before update trigger on all tables
call create_modified_trigger_on_all_tables();