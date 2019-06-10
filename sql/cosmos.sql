--
-- Table structure for table `applications`
--

DROP TABLE IF EXISTS `applications`;
CREATE TABLE `applications` (
  `name` varchar(100) COLLATE utf8_bin NOT NULL,
  `queue` varchar(100) COLLATE utf8_bin NOT NULL,
  `user` varchar(15) COLLATE utf8_bin NOT NULL,
  `priority` int(11) NOT NULL,
  `cores` int(11) NOT NULL,
  `mems` int(11) NOT NULL,
  `repository` varchar(500) COLLATE utf8_bin NOT NULL,
  `tag` varchar(100) COLLATE utf8_bin NOT NULL,
  `params` varchar(1000) COLLATE utf8_bin NOT NULL,
  `cron` varchar(20) COLLATE utf8_bin NOT NULL,
  `timeout` int(11) NOT NULL,
  UNIQUE KEY `name_queue_unique_index` (`name`,`queue`),
  UNIQUE KEY `queue_name_unique_index` (`queue`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Table structure for table `dependencies`
--

DROP TABLE IF EXISTS `dependencies`;
CREATE TABLE `dependencies` (
  `name` varchar(100) COLLATE utf8_bin NOT NULL,
  `queue` varchar(100) COLLATE utf8_bin NOT NULL,
  `depend_name` varchar(100) COLLATE utf8_bin NOT NULL,
  `depend_queue` varchar(100) COLLATE utf8_bin NOT NULL,
  `from_seconds` int(11) NOT NULL,
  `to_seconds` int(11) NOT NULL,
  UNIQUE KEY `name_queue_depend_name_depend_queue_unique_index` (`name`,`queue`,`depend_name`,`depend_queue`),
  KEY `depend_name_depend_queue_index` (`depend_name`,`depend_queue`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Table structure for table `message_queue`
--

DROP TABLE IF EXISTS `message_queue`;
CREATE TABLE `message_queue` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `queue` varchar(100) COLLATE utf8_bin NOT NULL,
  `priority` int(11) NOT NULL,
  `msgcontent` varchar(1000) COLLATE utf8_bin NOT NULL,
  `msgtimestamp` datetime NOT NULL,
  `consumer` varchar(100) COLLATE utf8_bin NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Table structure for table `records`
--

DROP TABLE IF EXISTS `records`;
CREATE TABLE `records` (
  `name` varchar(100) COLLATE utf8_bin NOT NULL,
  `queue` varchar(100) COLLATE utf8_bin NOT NULL,
  `host` varchar(100) COLLATE utf8_bin NOT NULL,
  `scheduletime` datetime NOT NULL,
  `updatetime` datetime NOT NULL,
  `state` int(11) NOT NULL,
  UNIQUE KEY `name_queue_scheduletime` (`name`,`queue`,`scheduletime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
