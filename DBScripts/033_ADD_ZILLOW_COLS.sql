ALTER TABLE `ss_user`.`COMPANY` 
ADD COLUMN `IS_ZILLOW_CONNECTED` INT(1) NOT NULL DEFAULT 0 COMMENT '' AFTER `SETTINGS_SET_STATUS`,
ADD COLUMN `ZILLOW_REVIEW_COUNT` INT(1) NOT NULL DEFAULT 0 COMMENT '' AFTER `IS_ZILLOW_CONNECTED`,
ADD COLUMN `ZILLOW_AVERAGE_SCORE` DOUBLE NULL DEFAULT 0.0 COMMENT '' AFTER `ZILLOW_REVIEW_COUNT`;

ALTER TABLE `ss_user`.`BRANCH` 
ADD COLUMN `IS_ZILLOW_CONNECTED` INT(1) NOT NULL DEFAULT 0 COMMENT '' AFTER `SETTINGS_SET_STATUS`,
ADD COLUMN `ZILLOW_REVIEW_COUNT` INT(1) NOT NULL DEFAULT 0 COMMENT '' AFTER `IS_ZILLOW_CONNECTED`,
ADD COLUMN `ZILLOW_AVERAGE_SCORE` DOUBLE NULL DEFAULT 0.0 COMMENT '' AFTER `ZILLOW_REVIEW_COUNT`;

ALTER TABLE `ss_user`.`REGION` 
ADD COLUMN `IS_ZILLOW_CONNECTED` INT(1) NOT NULL DEFAULT 0 COMMENT '' AFTER `SETTINGS_SET_STATUS`,
ADD COLUMN `ZILLOW_REVIEW_COUNT` INT(1) NOT NULL DEFAULT 0 COMMENT '' AFTER `IS_ZILLOW_CONNECTED`,
ADD COLUMN `ZILLOW_AVERAGE_SCORE` DOUBLE NULL DEFAULT 0.0 COMMENT '' AFTER `ZILLOW_REVIEW_COUNT`;

ALTER TABLE `ss_user`.`USERS` 
ADD COLUMN `IS_ZILLOW_CONNECTED` INT(1) NOT NULL DEFAULT 0 COMMENT '' AFTER `SUPER_ADMIN`,
ADD COLUMN `ZILLOW_REVIEW_COUNT` INT(1) NOT NULL DEFAULT 0 COMMENT '' AFTER `IS_ZILLOW_CONNECTED`,
ADD COLUMN `ZILLOW_AVERAGE_SCORE` DOUBLE NULL DEFAULT 0.0 COMMENT '' AFTER `ZILLOW_REVIEW_COUNT`;


