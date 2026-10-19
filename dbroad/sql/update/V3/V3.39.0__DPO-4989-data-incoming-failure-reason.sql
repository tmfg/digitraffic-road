-- Store why processing of an incoming message failed, so the broken row can be found
-- and understood without digging through application logs.
alter table data_incoming add column failure_reason text;

comment on column data_incoming.failure_reason is
    'Reason the message could not be processed. Null while status is NEW or PROCESSED.';
