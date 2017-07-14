use user_management_db;

delete from user_authority;
delete from user;
delete from authority;

insert into user (username, password, enabled) values ('admin', '$argon2i$v=19$m=65536,t=2,p=1$8buuloxG+y4CecxZjlKhPw$UDAi/srpZrmgCOny30uhGcMnBJYgBQFECvGB9H/GA44', true);
insert into authority (name) values ('ROLE_USER');
insert into user_authority (user_id, authority_id) values ((select id from user where username = 'admin'), (select id from authority where name = 'ROLE_USER'));
