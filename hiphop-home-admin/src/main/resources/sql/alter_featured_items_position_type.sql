-- 修改展位表的 position 字段类型，支持热门趋势位置（trending_1 到 trending_5）
-- 如果当前是 ENUM 类型，需要先修改为 VARCHAR
ALTER TABLE `h_featured_items` 
MODIFY COLUMN `position` VARCHAR(20) NOT NULL COMMENT '展位位置: center(中间大图), left_top(左侧上方), left_bottom(左侧下方), trending_1到trending_5(热门趋势1-5)';



