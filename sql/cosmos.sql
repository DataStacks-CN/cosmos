DROP TABLE IF EXISTS `applications`;
CREATE TABLE `applications` (
  `name` varchar(100) COLLATE utf8_bin NOT NULL,
  `queue` varchar(100) COLLATE utf8_bin NOT NULL,
  `user` varchar(15) COLLATE utf8_bin NOT NULL,
  `priority` int(11) NOT NULL DEFAULT '0',
  `cores` int(11) NOT NULL DEFAULT '1',
  `mems` int(11) NOT NULL DEFAULT '1024',
  `repository` varchar(500) COLLATE utf8_bin NOT NULL,
  `tag` varchar(100) COLLATE utf8_bin NOT NULL,
  `params` varchar(1000) COLLATE utf8_bin NOT NULL,
  `cron` varchar(20) COLLATE utf8_bin NOT NULL,
  `timeout` int(11) NOT NULL DEFAULT '3600',
  UNIQUE KEY `name_queue_unique_index` (`name`,`queue`),
  UNIQUE KEY `queue_name_unique_index` (`queue`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `message_queue`;
CREATE TABLE `message_queue` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `queue` varchar(100) COLLATE utf8_bin NOT NULL,
  `priority` int(11) NOT NULL DEFAULT '0',
  `msgcontent` varchar(1000) COLLATE utf8_bin NOT NULL,
  `msgtimestamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `consumer` varchar(100) COLLATE utf8_bin NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=320 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `records`;
CREATE TABLE `records` (
  `name` varchar(100) COLLATE utf8_bin NOT NULL,
  `queue` varchar(100) COLLATE utf8_bin NOT NULL,
  `host` varchar(100) COLLATE utf8_bin NOT NULL,
  `scheduletime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updatetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `state` int(11) NOT NULL DEFAULT '-1',
  UNIQUE KEY `name_queue_scheduletime` (`name`,`scheduletime`,`queue`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `dependencies`;
CREATE TABLE `dependencies` (
  `name` varchar(100) COLLATE utf8_bin NOT NULL,
  `queue` varchar(100) COLLATE utf8_bin NOT NULL,
  `depend_name` varchar(100) COLLATE utf8_bin NOT NULL,
  `depend_queue` varchar(100) COLLATE utf8_bin NOT NULL,
  `from_seconds` int(11) NOT NULL DEFAULT '0',
  `to_seconds` int(11) NOT NULL DEFAULT '0',
  UNIQUE KEY `name_queue_depend_name_depend_queue_unique_index` (`name`,`queue`,`depend_name`,`depend_queue`),
  KEY `depend_name_depend_queue_index` (`depend_name`,`depend_queue`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;