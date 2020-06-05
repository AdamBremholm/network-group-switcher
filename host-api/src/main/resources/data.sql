INSERT INTO alias (name) VALUES ('usa');
INSERT INTO alias (name) VALUES ('sweden');
INSERT INTO alias (name) VALUES ('uk');
INSERT INTO alias (name) VALUES ('russia');

insert into host (id, address, alias_id, name) values (null, '192.168.1.101', 1, 'desktop');
insert into host (id, address, alias_id, name) values (null, '192.168.1.102', 1, 'laptop');
insert into host (id, address, alias_id, name) values (null, '192.168.1.103', 2, 'tablet');
insert into host (id, address, alias_id, name) values (null, '192.168.1.104', 2, 'phone');
insert into host (id, address, alias_id, name) values (null, '192.168.1.105', 3, 'chromecast');
insert into host (id, address, alias_id, name) values (null, '192.168.1.106', 4, 'switch');
insert into host (id, address, alias_id, name) values (null, '192.168.1.107', 4, 'raspberry-pi');

