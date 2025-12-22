-- 为展位表添加分类标签和浏览量字段（用于热门趋势）
-- 注意：如果 position 字段是 ENUM 类型，需要先执行 alter_featured_items_position_type.sql 修改字段类型

-- 第一步：修改 position 字段类型（如果当前是 ENUM 类型，需要先执行此步骤）
-- ALTER TABLE `h_featured_items` 
-- MODIFY COLUMN `position` VARCHAR(20) NOT NULL COMMENT '展位位置: center(中间大图), left_top(左侧上方), left_bottom(左侧下方), trending_1到trending_5(热门趋势1-5)';

-- 第二步：添加 tag 和 views 字段
-- 如果字段已存在，会报错，可以忽略或手动检查
ALTER TABLE `h_featured_items` 
ADD COLUMN `tag` VARCHAR(50) DEFAULT NULL COMMENT '分类标签: NEWS, FEATURES, BEEF, MUSIC等（用于热门趋势）' AFTER `link_url`,
ADD COLUMN `views` VARCHAR(20) DEFAULT NULL COMMENT '浏览量（显示用，如: 2.2K, 61.4K，用于热门趋势）' AFTER `tag`;




