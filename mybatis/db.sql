drop table user;

CREATE TABLE `user` (
  `id` int(32) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) NOT NULL,
  `age` int(32) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;