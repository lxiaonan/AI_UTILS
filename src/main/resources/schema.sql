CREATE DATABASE IF NOT EXISTS ai_sql_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_sql_platform;

CREATE TABLE IF NOT EXISTS `app_user` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `password_hash` varchar(100) NOT NULL COMMENT '密码Hash(BCrypt)',
    `ai_use_count` bigint(20) NOT NULL DEFAULT 0 COMMENT 'AI 使用次数',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `sql_optimize_record` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
    `user_sql` text NOT NULL COMMENT '用户输入的待优化SQL',
    `db_type` varchar(50) NOT NULL COMMENT '数据库类型 (如 mysql, clickhouse, pg)',
    `ai_model` varchar(50) NOT NULL COMMENT '使用的 AI 模型',
    `score` int(11) DEFAULT NULL COMMENT 'AI 综合评分',
    `ai_analysis` text COMMENT 'AI 返回的完整分析结果',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SQL智能优化记录表';

CREATE TABLE IF NOT EXISTS `voice_generate_record` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
    `user_text` text NOT NULL COMMENT '用户输入的文本',
    `ai_model` varchar(50) NOT NULL COMMENT '使用的语音模型',
    `audio_url` text COMMENT '生成的音频文件URL',
    `raw_response` text COMMENT 'API返回的原始JSON数据',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='语音生成历史记录表';

CREATE TABLE IF NOT EXISTS `image_generate_record` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
    `user_prompt` text NOT NULL COMMENT '用户输入的生成提示词或Payload',
    `ai_model` varchar(50) NOT NULL COMMENT '使用的图像模型',
    `image_urls` text COMMENT '生成的图片URL(多个以逗号分隔)',
    `raw_response` text COMMENT 'API返回的原始JSON数据',
    `status` varchar(20) DEFAULT 'SUCCESS' COMMENT '生成状态: SUCCESS/FAILED',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图像生成历史记录表';

CREATE TABLE IF NOT EXISTS `camera_generate_record` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
    `original_image_url` varchar(500) NOT NULL COMMENT '上传的原始图片URL',
    `ai_model` varchar(50) NOT NULL COMMENT '使用的图像模型',
    `azimuth` int NOT NULL COMMENT '方位角',
    `elevation` int NOT NULL COMMENT '俯仰角',
    `distance` decimal(3,1) NOT NULL COMMENT '距离缩放',
    `prompt_used` text COMMENT '最终使用的提示词',
    `generated_image_url` text COMMENT '生成的3D视角图片URL',
    `raw_response` text COMMENT 'API返回的原始JSON数据',
    `status` varchar(20) DEFAULT 'SUCCESS' COMMENT '生成状态: SUCCESS/FAILED',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='3D镜头生成历史记录表';

SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sql_optimize_record' AND COLUMN_NAME = 'user_id'
);
SET @stmt := IF(@col_exists = 0,
    'ALTER TABLE `sql_optimize_record` ADD COLUMN `user_id` bigint(20) DEFAULT NULL COMMENT ''用户ID''',
    'SELECT 1'
);
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'voice_generate_record' AND COLUMN_NAME = 'user_id'
);
SET @stmt := IF(@col_exists = 0,
    'ALTER TABLE `voice_generate_record` ADD COLUMN `user_id` bigint(20) DEFAULT NULL COMMENT ''用户ID''',
    'SELECT 1'
);
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'voice_generate_record' AND COLUMN_NAME = 'language_type'
);
SET @stmt := IF(@col_exists = 0,
    'ALTER TABLE `voice_generate_record` ADD COLUMN `language_type` varchar(50) DEFAULT NULL COMMENT ''Language type'' AFTER `ai_model`',
    'SELECT 1'
);
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'voice_generate_record' AND COLUMN_NAME = 'voice_code'
);
SET @stmt := IF(@col_exists = 0,
    'ALTER TABLE `voice_generate_record` ADD COLUMN `voice_code` varchar(100) DEFAULT NULL COMMENT ''Voice code'' AFTER `language_type`',
    'SELECT 1'
);
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'image_generate_record' AND COLUMN_NAME = 'user_id'
);
SET @stmt := IF(@col_exists = 0,
    'ALTER TABLE `image_generate_record` ADD COLUMN `user_id` bigint(20) DEFAULT NULL COMMENT ''用户ID''',
    'SELECT 1'
);
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'camera_generate_record' AND COLUMN_NAME = 'user_id'
);
SET @stmt := IF(@col_exists = 0,
    'ALTER TABLE `camera_generate_record` ADD COLUMN `user_id` bigint(20) DEFAULT NULL COMMENT ''用户ID''',
    'SELECT 1'
);
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sql_optimize_record' AND INDEX_NAME = 'idx_sql_user_time'
);
SET @stmt := IF(@idx_exists = 0,
    'CREATE INDEX `idx_sql_user_time` ON `sql_optimize_record` (`user_id`, `create_time`)',
    'SELECT 1'
);
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'voice_generate_record' AND INDEX_NAME = 'idx_voice_user_time'
);
SET @stmt := IF(@idx_exists = 0,
    'CREATE INDEX `idx_voice_user_time` ON `voice_generate_record` (`user_id`, `create_time`)',
    'SELECT 1'
);
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'image_generate_record' AND INDEX_NAME = 'idx_image_user_time'
);
SET @stmt := IF(@idx_exists = 0,
    'CREATE INDEX `idx_image_user_time` ON `image_generate_record` (`user_id`, `create_time`)',
    'SELECT 1'
);
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'camera_generate_record' AND INDEX_NAME = 'idx_camera_user_time'
);
SET @stmt := IF(@idx_exists = 0,
    'CREATE INDEX `idx_camera_user_time` ON `camera_generate_record` (`user_id`, `create_time`)',
    'SELECT 1'
);
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `video_generate_record` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` bigint(20) DEFAULT NULL COMMENT 'User ID',
    `user_prompt` text NOT NULL COMMENT 'Video generation payload',
    `ai_model` varchar(50) NOT NULL COMMENT 'Video model',
    `task_id` varchar(100) DEFAULT NULL COMMENT 'Async task ID',
    `task_status` varchar(40) DEFAULT 'PENDING' COMMENT 'Task status',
    `video_url` text COMMENT 'Generated video URL',
    `raw_response` text COMMENT 'Raw API response',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Video generation history';

SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'video_generate_record' AND COLUMN_NAME = 'user_id'
);
SET @stmt := IF(@col_exists = 0,
    'ALTER TABLE `video_generate_record` ADD COLUMN `user_id` bigint(20) DEFAULT NULL COMMENT ''User ID''',
    'SELECT 1'
);
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'video_generate_record' AND COLUMN_NAME = 'task_id'
);
SET @stmt := IF(@col_exists = 0,
    'ALTER TABLE `video_generate_record` ADD COLUMN `task_id` varchar(100) DEFAULT NULL COMMENT ''Task ID'' AFTER `ai_model`',
    'SELECT 1'
);
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'video_generate_record' AND COLUMN_NAME = 'task_status'
);
SET @stmt := IF(@col_exists = 0,
    'ALTER TABLE `video_generate_record` ADD COLUMN `task_status` varchar(40) DEFAULT ''PENDING'' COMMENT ''Task status'' AFTER `task_id`',
    'SELECT 1'
);
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'video_generate_record' AND COLUMN_NAME = 'video_url'
);
SET @stmt := IF(@col_exists = 0,
    'ALTER TABLE `video_generate_record` ADD COLUMN `video_url` text COMMENT ''Video URL'' AFTER `task_status`',
    'SELECT 1'
);
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'video_generate_record' AND INDEX_NAME = 'idx_video_user_time'
);
SET @stmt := IF(@idx_exists = 0,
    'CREATE INDEX `idx_video_user_time` ON `video_generate_record` (`user_id`, `create_time`)',
    'SELECT 1'
);
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `marketing_generate_record` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` bigint(20) DEFAULT NULL COMMENT 'User ID',
    `record_type` varchar(40) NOT NULL COMMENT 'marketing_copy / slogan',
    `product_name` varchar(255) NOT NULL COMMENT 'Product name',
    `targets` varchar(255) DEFAULT NULL COMMENT 'Platforms or generate types',
    `ai_model` varchar(80) NOT NULL COMMENT 'AI model',
    `input_payload` mediumtext COMMENT 'Input payload JSON',
    `result_payload` mediumtext COMMENT 'Result payload JSON',
    `status` varchar(20) DEFAULT 'SUCCESS' COMMENT 'SUCCESS / FAILED',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Marketing generation history';

SET @idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'marketing_generate_record' AND INDEX_NAME = 'idx_marketing_user_time'
);
SET @stmt := IF(@idx_exists = 0,
    'CREATE INDEX `idx_marketing_user_time` ON `marketing_generate_record` (`user_id`, `create_time`)',
    'SELECT 1'
);
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
