ALTER TABLE `system_users`
  ADD COLUMN `failed_attempt_count` int NOT NULL DEFAULT 0,
  ADD COLUMN `locked_until` datetime(6) DEFAULT NULL;
