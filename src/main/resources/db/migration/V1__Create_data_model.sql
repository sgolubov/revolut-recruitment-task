create sequence ACCOUNT_SEQ;

create table ACCOUNT
(
    ID              int primary key,
    NAME            varchar(100) not null,
    BALANCE         decimal   default 0,
    LATEST_ACTIVITY timestamp default systimestamp,
    CREATED_DATE    timestamp default systimestamp
);

create sequence ACCOUNT_TRANSACTION_SEQ;

create table ACCOUNT_TRANSACTION
(
    ID             int primary key,
    FROM_ACC       int         not null references ACCOUNT (ID),
    TO_ACC         int         not null references ACCOUNT (ID),
    AMOUNT         decimal     not null,
    TYPE           varchar(10) not null,
    EXECUTION_DATE timestamp default systimestamp
);
