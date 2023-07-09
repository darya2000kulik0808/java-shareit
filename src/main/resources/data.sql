delete
from USERS;
delete
from BOOKINGS;
delete
from COMMENTS;
delete
from ITEMS;
delete
from REQUESTS;

alter table USERS
    alter COLUMN ID RESTART with 1;
alter table ITEMS
    alter COLUMN ID RESTART with 1;
alter table BOOKINGS
    alter COLUMN ID RESTART with 1;
alter table COMMENTS
    alter COLUMN ID RESTART with 1;
alter table REQUESTS
    alter COLUMN ID RESTART with 1;