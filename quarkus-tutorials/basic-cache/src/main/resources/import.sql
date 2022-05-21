INSERT INTO city(id, name) VALUES (1, 'BeiJing');
INSERT INTO city(id, name) VALUES (2, 'ShangHai');
INSERT INTO city(id, name) VALUES (3, 'GuangZhou');

INSERT INTO country(id, name) VALUES (1, 'China');
INSERT INTO country_city(country_id, cities_id) VALUES (1, 1);
INSERT INTO country_city(country_id, cities_id) VALUES (1, 2);
INSERT INTO country_city(country_id, cities_id) VALUES (1, 3);
