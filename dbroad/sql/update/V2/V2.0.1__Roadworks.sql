alter table datex2 add message_type character VARYING (32);

update datex2 set message_type = 'TRAFFIC_DISORDER';

alter table datex2 alter message_type set not null;

create index datex2_type_import_i on datex2(message_type, import_date);
create index dsr_search_i on datex2_situation_record(datex2_situation_id, validy_status, overall_end_time);

--

alter table datex2_situation drop constraint SITUATION_DATEX2_FK;
alter table datex2_situation add CONSTRAINT SITUATION_DATEX2_FK FOREIGN KEY (DATEX2_ID) REFERENCES DATEX2(ID) ON DELETE CASCADE;

alter table datex2_situation_record drop constraint SITUATION_RECORD_SITUATION_FK;
alter table datex2_situation_record add CONSTRAINT SITUATION_RECORD_SITUATION_FK FOREIGN KEY (DATEX2_SITUATION_ID) REFERENCES DATEX2_SITUATION(ID) ON DELETE CASCADE;

alter table situation_record_comment_i18n drop constraint SITUATION_RECORD_COMMENT_FK;
alter table situation_record_comment_i18n add CONSTRAINT SITUATION_RECORD_COMMENT_FK FOREIGN KEY (DATEX2_SITUATION_RECORD_ID)  REFERENCES DATEX2_SITUATION_RECORD (ID)  ON DELETE CASCADE;
