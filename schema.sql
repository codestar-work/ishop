create database ishop default charset='utf8';
create user ishop@'%' identified by 'iShop2017';
grant all on ishop.* to ishop@'%';

use ishop;

create table shop (
	name   varchar(255),
	phone  varchar(255)
);

create table member (
	code      serial, -- identity or auto_increment
	name      varchar(255) unique not null,
	password  varchar(255) not null,
	full_name varchar(255),
	email     varchar(255)
);

create table category (
	code      serial,
	name      varchar(255)
);

create table product (
	code      serial,
	name      varchar(255),
	detail    varchar(2047),
	photo     varchar(255),
	price     double,
	category  bigint
);


-- Test Data
insert into shop(name,phone)
values('iCoffee', '022142000');

insert into member(name, password, full_name, email)
values('markz', sha2('mark123', 512),
	'Mark Zuckerberg', 'mark@fb.com');

insert into category(name) values('Coffee');
insert into category(name) values('Cookie');
insert into category(name) values('Cake');

insert into product(name, price, category)
values('Latte', 80, 1);

insert into product(name, price, category)
select 'Latte', 80, code
from category where name = 'Coffee';

insert into product(name, price, category)
select 'Mocha', 90, code
from category where name = 'Coffee';

insert into product(name, price, category)
select 'Americano', 70, code
from category where name = 'Coffee';
