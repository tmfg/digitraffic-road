-- The Datex II message type used to be written into data_incoming as the Java constant name
-- DATEX_2, while the schema value and the data_datex2_situation_message column hold DATEX2. The
-- constant is now named after the schema value, so rows that are still waiting to be processed
-- would no longer be understood. Rewrite them.
--
-- Only NEW rows are touched: PROCESSED rows have already been read, and FAILED rows are kept as
-- received so the content that failed can be inspected as it arrived.
update data_incoming
set data = replace(data, '"type":"DATEX_2"', '"type":"DATEX2"')
where status = 'NEW'
  and strpos(data, '"type":"DATEX_2"') > 0;
