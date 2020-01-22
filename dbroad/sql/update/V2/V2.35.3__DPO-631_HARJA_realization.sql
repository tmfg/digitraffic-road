CREATE SEQUENCE SEQ_V2_REALIZATION;

CREATE TABLE IF NOT EXISTS V2_REALIZATION
(
    id                      BIGINT NOT NULL PRIMARY KEY,
    job_id                  BIGINT NOT NULL, -- harja urakka
    sending_system          TEXT,
    sending_time            TIMESTAMP(0) WITH TIME ZONE,
    message_id              INTEGER,
    realization_data_id     BIGINT NOT NULL
        REFERENCES V2_REALIZATION_DATA(id),
    created                 TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX V2_REALIZATION_DATA_FK ON V2_REALIZATION
USING BTREE (id ASC);

-- CREATE TABLE IF NOT EXISTS WORK_MACHINE_REALIZATION_TASK
-- (
--     realization_id          BIGINT NOT NULL,
--     task_harja_id           BIGINT NOT NULL
--         REFERENCES V2_WORK_MACHINE_TASK(HARJA_ID) ON DELETE CASCADE,
--     created                 TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
--     PRIMARY KEY (realization_id, task_harja_id)
-- );
--
-- CREATE INDEX WORK_MACHINE_REALIZATION_TASKS_REALIZATION_FK ON WORK_MACHINE_REALIZATION_TASK
--     USING BTREE (task_harja_id ASC);


CREATE TABLE V2_REALIZATION_POINT (
    realization_id              BIGINT REFERENCES V2_REALIZATION(id) NOT NULL,
    order_number                INTEGER,
    point                       geometry(pointz, 4326), -- 4326 = WGS84
    time                        TIMESTAMP(0) WITH TIME ZONE,
    PRIMARY KEY(realization_id, order_number),
    CONSTRAINT V2_REALIZATION_POINT_UNIQUE_FK_I UNIQUE(realization_id, order_number)
);

CREATE TABLE V2_REALIZATION_POINT_TASK (
    realization_point_realization_id            BIGINT NOT NULL,
    realization_point_order_number              INTEGER NOT NULL,
    task_harja_id                               BIGINT NOT NULL
        REFERENCES V2_REALIZATION_TASK(harja_id) ON DELETE CASCADE,
    PRIMARY KEY(realization_point_realization_id, realization_point_order_number, task_harja_id),
    FOREIGN KEY (realization_point_realization_id, realization_point_order_number)
       REFERENCES V2_REALIZATION_POINT (realization_id, order_number)
);