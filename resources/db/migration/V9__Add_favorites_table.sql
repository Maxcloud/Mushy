CREATE TABLE `favorites` (
  `accountid` int(11) NOT NULL,
  `sn` int(11) NOT NULL,
  KEY `accountid` (`accountid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;