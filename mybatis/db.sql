DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` int(32) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) NOT NULL,
  `age` int(32) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `log`;

CREATE TABLE `log` (
  `id` int(32) NOT NULL AUTO_INCREMENT,
  `user_id` int(32),
  `action` varchar(255) NOT NULL,
  `create_time` datetime not null,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

INSERT INTO mybatis.user (id, name, age) VALUES (3, 'tom', 11);
INSERT INTO mybatis.log (id, user_id, action, create_time) VALUES (3, 3, 'read book', '2020-08-07 08:18:16');

CREATE TABLE `address` (
  `id` int(32) NOT NULL AUTO_INCREMENT,
  `city` varchar(32) NOT NULL,
  `street` varchar(32) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;