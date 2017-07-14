drop database if exists user_management_db;
create database if not exists user_management_db;

use user_management_db;

create table user (
	id integer primary key auto_increment,
    username varchar(50) not null unique,
    password varchar(100) not null,
    enabled boolean not null
);
create table authority (
    id integer primary key auto_increment,
    name varchar(50) not null
);
create table user_authority (
	id integer primary key auto_increment,
    user_id integer,
    authority_id integer,
    constraint fk_user_authority_user foreign key(user_id) references user(id),
    constraint fk_user_authority_authority foreign key(authority_id) references authority(id)
);
create unique index ix_user_authority on user_authority (user_id, authority_id);
