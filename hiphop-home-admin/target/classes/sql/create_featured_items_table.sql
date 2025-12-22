-- 创建首页展位表
CREATE TABLE IF NOT EXISTS `h_featured_items` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `position` VARCHAR(20) NOT NULL COMMENT '展位位置: center(中间大图), left_top(左侧上方), left_bottom(左侧下方)',
  `title` VARCHAR(255) NOT NULL COMMENT '标题',
  `img_url` VARCHAR(500) NOT NULL COMMENT '封面图片URL',
  `link_url` VARCHAR(500) NOT NULL COMMENT '原文/原视频链接',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_position_status` (`position`, `status`),
  KEY `idx_sort_no` (`sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页展位表';











