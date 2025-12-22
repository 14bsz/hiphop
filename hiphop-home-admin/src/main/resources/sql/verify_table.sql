-- 验证表是否存在
USE HP_hiphop;

-- 检查表是否存在
SHOW TABLES LIKE 'h_featured_items';

-- 查看表结构
DESC h_featured_items;

-- 如果表不存在，执行以下创建语句
CREATE TABLE IF NOT EXISTS `h_featured_items` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `position` enum('center','left_top','left_bottom') NOT NULL COMMENT '展位位置',
  `title` varchar(255) NOT NULL COMMENT '标题',
  `img_url` varchar(500) NOT NULL COMMENT '封面图片URL',
  `link_url` varchar(500) NOT NULL COMMENT '原文/原视频链接',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `sort_no` int NOT NULL DEFAULT '0' COMMENT '排序号，越小越靠前',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_position_status_sort` (`position`,`status`,`sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='首页展位控制表';











