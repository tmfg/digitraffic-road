update camera_preset
set pic_last_modified_db = pic_last_modified
where pic_last_modified_db is null
  AND pic_last_modified is not null;
