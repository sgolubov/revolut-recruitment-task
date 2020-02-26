-- accounts

insert into ACCOUNT (id, name, balance)
values (998, 'Holden Caulfield', 3500);
insert into ACCOUNT (id, name, balance)
values (999, 'John Doe', 4500);

-- transactions

insert into ACCOUNT_TRANSACTION (id, from_acc, to_acc, amount, type)
values (1995, 998, 998, 1000, 'TOP_UP');
insert into ACCOUNT_TRANSACTION (id, from_acc, to_acc, amount, type)
values (1996, 999, 999, 7000, 'TOP_UP');
insert into ACCOUNT_TRANSACTION (id, from_acc, to_acc, amount, type)
values (1997, 998, 999, 500, 'TRANSFER');
insert into ACCOUNT_TRANSACTION (id, from_acc, to_acc, amount, type)
values (1998, 999, 998, 3000, 'TRANSFER');
