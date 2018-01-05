DROP TABLE IF EXISTS `company_details_report`;
CREATE TABLE `company_details_report` (
  `company_details_report_id` varchar(45) NOT NULL,
  `company_id` int(11) unsigned DEFAULT NULL,
  `company` varchar(450) DEFAULT NULL,
  `user_count` int(11) DEFAULT NULL,
  `verified_user_count` int(11) DEFAULT NULL,
  `verified_percent` decimal(10,2) DEFAULT NULL,
  `region_count` int(11) DEFAULT NULL,
  `branch_count` int(11) DEFAULT NULL,
  `completion_rate` decimal(10,2) DEFAULT NULL,
  `verified_gmb` int(11) DEFAULT NULL,
  `missing_gmb` int(11) DEFAULT NULL,
  `mismatch_count` int(11) DEFAULT NULL,
  `missing_photo_count` int(11) DEFAULT NULL,
  `missing_url_count` int(11) DEFAULT NULL,
  `facebook_connection_count` int(11) DEFAULT NULL,
  `twitter_connection_count` int(11) DEFAULT NULL,
  `linkedin_connection_count` int(11) DEFAULT NULL,
  PRIMARY KEY (`company_details_report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;