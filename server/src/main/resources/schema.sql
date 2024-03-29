DROP TABLE IF EXISTS USERS, REQUESTS, ITEMS, BOOKINGS, COMMENTS CASCADE;

CREATE TABLE IF NOT EXISTS USERS
(
    ID    bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    EMAIL varchar(100) UNIQUE NOT NULL,
    NAME  varchar(50)         NOT NULL
);

CREATE TABLE IF NOT EXISTS REQUESTS
(
    ID           bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    DESCRIPTION  varchar(250)                not null,
    REQUESTER_ID bigint                      not null REFERENCES USERS (ID) on delete cascade,
    CREATED      timestamp without time zone not null
);

CREATE TABLE IF NOT EXISTS ITEMS
(
    ID           bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME         varchar(100) not null,
    DESCRIPTION  varchar(500) not null,
    IS_AVAILABLE boolean      not null,
    OWNER_ID     bigint REFERENCES USERS (ID) on delete cascade,
    REQUEST_ID   bigint REFERENCES REQUESTS (ID) on delete cascade
);

CREATE TABLE IF NOT EXISTS BOOKINGS
(
    ID         bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    START_DATE timestamp without time zone not null,
    END_DATE   timestamp without time zone not null,
    ITEM_ID    bigint                      not null REFERENCES ITEMS (ID) on delete cascade,
    BOOKER_ID  bigint                      not null REFERENCES USERS (ID) on delete cascade,
    STATUS     varchar                     not null
);


CREATE TABLE IF NOT EXISTS COMMENTS
(
    ID        bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    TEXT      varchar(500)                not null,
    ITEM_ID   bigint                      not null REFERENCES ITEMS (ID) on delete cascade,
    AUTHOR_ID bigint                      not null REFERENCES USERS (ID) on delete cascade,
    CREATED   timestamp without time zone not null
);
