-- 修复展位表以支持热门趋势功能
-- 执行顺序：
-- 1. 先修改 position 字段类型（从 ENUM 改为 VARCHAR，如果已经是 VARCHAR 则忽略错误）
-- 2. 再添加 tag 和 views 字段（如果已存在则忽略错误）

-- ============================================
-- 第一步：修改 position 字段类型
-- ============================================
-- 如果 position 字段是 ENUM 类型，需要先修改为 VARCHAR 以支持 trending_1 到 trending_5
-- 如果已经是 VARCHAR 类型，执行此语句不会报错，只是更新注释
ALTER TABLE `h_featured_items` 
MODIFY COLUMN `position` VARCHAR(20) NOT NULL COMMENT '展位位置: center(中间大图), left_top(左侧上方), left_bottom(左侧下方), trending_1到trending_5(热门趋势1-5)';

-- ============================================
-- 第二步：添加 tag 字段（如果不存在）
-- ============================================
-- 检查字段是否存在，如果不存在则添加
SET @dbname = DATABASE();
SET @tablename = 'h_featured_items';
SET @columnname = 'tag';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1', -- 字段已存在，不执行任何操作
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` VARCHAR(50) DEFAULT NULL COMMENT ''分类标签: NEWS, FEATURES, BEEF, MUSIC等（用于热门趋势）'' AFTER `link_url`')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- ============================================
-- 第三步：添加 views 字段（如果不存在）
-- ============================================
SET @columnname = 'views';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1', -- 字段已存在，不执行任何操作
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` VARCHAR(20) DEFAULT NULL COMMENT ''浏览量（显示用，如: 2.2K, 61.4K，用于热门趋势）'' AFTER `tag`')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- ============================================
-- 完成！
-- ============================================
-- 现在可以正常使用热门趋势功能了



