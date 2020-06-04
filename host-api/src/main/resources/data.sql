INSERT INTO alias (name) VALUES ('usa');
INSERT INTO alias (name) VALUES ('sweden');
INSERT INTO alias (name) VALUES ('uk');
INSERT INTO alias (name) VALUES ('russia');

insert into host (id, address, alias_id, name) values (null, '192.168.1.101', 1, 'desktop')

INSERT INTO alias_hosts (alias_id, host_id) VALUES (1, 'USER');