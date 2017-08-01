DROP TABLE IF EXISTS `survey_transaction_report_branch`;
CREATE TABLE `survey_transaction_report_branch` (
  `survey_transaction_report_branch_id` varchar(36) NOT NULL,
  `month` int(11) DEFAULT NULL,
  `year` int(11) DEFAULT NULL,
  `user_name` varchar(45) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `nmls` varchar(45) DEFAULT NULL,
  `license_id` varchar(45) DEFAULT NULL,
  `company_name` varchar(45) DEFAULT NULL,
  `company_id` int(11) DEFAULT NULL,
  `region_name` text,
  `branch_name` text,
  `branch_id` int(11) DEFAULT NULL,
  `total_reviews` int(11) DEFAULT NULL,
  `total_zillow_reviews` int(11) DEFAULT NULL,
  `total_3rd_party_reviews` int(11) DEFAULT NULL,
  `total_verified_customer_reviews` int(11) DEFAULT NULL,
  `total_unverified_customer_reviews` int(11) DEFAULT NULL,
  `total_social_survey_reviews` int(11) DEFAULT NULL,
  `total_abusive_reviews` int(11) DEFAULT NULL,
  `total_retake_reviews` int(11) DEFAULT NULL,
  `total_retake_completed` int(11) DEFAULT NULL,
  `transaction_received_by_source` int(11) DEFAULT NULL,
  `transaction_sent` int(11) DEFAULT NULL,
  `transaction_unprocessable` int(11) DEFAULT NULL,
  `transaction_clicked` int(11) DEFAULT NULL,
  `transaction_completed_` int(11) DEFAULT NULL,
  `transaction_partially_completed` int(11) DEFAULT NULL,
  `transaction_unopened` int(11) DEFAULT NULL,
  `transaction_duplicates` int(11) DEFAULT NULL,
  `transaction_mismatched` int(11) DEFAULT NULL,
  `transaction_unassigned` int(11) DEFAULT NULL,
  PRIMARY KEY (`survey_transaction_report_branch_id`),
  UNIQUE KEY `userIdMonthYearBranch` (`month`,`year`,`user_id`,`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
