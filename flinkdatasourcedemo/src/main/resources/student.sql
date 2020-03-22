DROP DATABASE IF EXISTS flinkdemo;
CREATE DATABASE IF NOT EXISTS flinkdemo;
USE flinkdemo;

SELECT 'CREATING DATABASE STRUCTURE' as 'INFO';

DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(25) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

INSERT INTO `student` VALUES ('1', 'student01'), ('2', 'student02'), ('3', 'student03'), ('4', 'student04'), ('5', 'student05'), ('6', 'student06');
COMMIT;