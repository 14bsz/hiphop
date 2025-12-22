-- 创建高分音乐榜表
CREATE TABLE IF NOT EXISTS `h_top_rated_items` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(255) NOT NULL COMMENT '歌曲标题',
  `artist` VARCHAR(255) DEFAULT NULL COMMENT '艺人 / 歌手',
  `cover` VARCHAR(500) NOT NULL COMMENT '封面图片URL',
  `link_url` VARCHAR(500) NOT NULL COMMENT '原歌曲链接（外链）',
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型: 1-单曲, 2-专辑',
  `rating` DECIMAL(4,2) DEFAULT 0 COMMENT '评分 (0-5)',
  `favorite_count` BIGINT DEFAULT 0 COMMENT '收藏数（来自网易云）',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序号（越小越靠前）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`, `sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页高分音乐榜表';




