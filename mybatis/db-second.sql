use mybatis_second;

DROP TABLE IF EXISTS `address`;

CREATE TABLE `address` (
  `id` int(32) NOT NULL AUTO_INCREMENT,
  `city` varchar(32) NOT NULL,
  `street` varchar(32) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;