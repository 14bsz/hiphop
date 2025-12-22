# 数据库初始化说明

## 执行SQL脚本

请在MySQL数据库中执行以下SQL脚本来创建展位表：

```bash
# 方式1: 使用MySQL命令行
mysql -u root -p hiphop < src/main/resources/sql/create_featured_items_table.sql

# 方式2: 在MySQL客户端中执行
# 连接到hiphop数据库后，复制并执行 create_featured_items_table.sql 中的SQL语句
```

## 表结构说明

`h_featured_items` 表用于控制首页的三个展位位置：

- **center**: 中间大图（topStory）
- **left_top**: 左侧上方小图（leftNews[0]）
- **left_bottom**: 左侧下方小图（leftNews[1]）

## 字段说明

- `id`: 主键ID
- `position`: 展位位置（center/left_top/left_bottom）
- `title`: 标题
- `img_url`: 封面图片URL
- `link_url`: 原文/原视频链接
- `status`: 状态（1-启用，0-禁用）
- `sort_no`: 排序号
- `created_at`: 创建时间
- `updated_at`: 更新时间











