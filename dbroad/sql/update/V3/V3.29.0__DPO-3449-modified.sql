alter table data_datex2_situation_message add column if not exists created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
alter table data_datex2_situation_message add column if not exists modified_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
