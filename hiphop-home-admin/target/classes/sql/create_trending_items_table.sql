-- 创建热门趋势表
CREATE TABLE IF NOT EXISTS `h_trending_items` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `position` INT NOT NULL COMMENT '位置: 1-5 (热门趋势1到5)',
  `title` VARCHAR(255) NOT NULL COMMENT '标题',
  `img_url` VARCHAR(500) NOT NULL COMMENT '封面图片URL',
  `link_url` VARCHAR(500) NOT NULL COMMENT '原文/原视频链接',
  `tag` VARCHAR(50) DEFAULT 'NEWS' COMMENT '分类标签: NEWS, FEATURES, BEEF, MUSIC等',
  `views` VARCHAR(20) DEFAULT '0' COMMENT '浏览量（显示用，如: 2.2K, 61.4K）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_position_status` (`position`, `status`),
  KEY `idx_status_sort` (`status`, `sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页热门趋势表';




