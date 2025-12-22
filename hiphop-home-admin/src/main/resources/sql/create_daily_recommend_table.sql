-- 每日推荐表
CREATE TABLE IF NOT EXISTS h_daily_recommend (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL COMMENT '歌曲名称',
    artist VARCHAR(255) NOT NULL COMMENT '歌手名称',
    cover VARCHAR(500) COMMENT '封面图片URL',
    link_url VARCHAR(500) COMMENT '歌曲链接',
    badge VARCHAR(100) DEFAULT '热度爆表' COMMENT '标签（如：热度爆表）',
    recommend_date DATE NOT NULL COMMENT '推荐日期',
    sort_no INT DEFAULT 0 COMMENT '排序号',
    status INT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_recommend_date (recommend_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日推荐表';
