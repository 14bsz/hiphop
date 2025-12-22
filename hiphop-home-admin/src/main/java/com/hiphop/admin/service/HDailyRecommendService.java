package com.hiphop.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hiphop.admin.entity.HDailyRecommend;

import java.util.List;

public interface HDailyRecommendService extends IService<HDailyRecommend> {
    
    /**
     * 从网易云歌单抓取每日推荐
     * @param playlistId 歌单ID
     * @param count 抓取数量
     * @return 抓取成功的数量
     */
    int fetchDailyRecommend(String playlistId, int count);
    
    /**
     * 获取指定日期的推荐列表
     * @param date 日期 (格式: yyyy-MM-dd)
     * @return 推荐列表
     */
    List<HDailyRecommend> getRecommendByDate(String date);
    
    /**
     * 获取最新的推荐列表
     * @param limit 数量
     * @return 推荐列表
     */
    List<HDailyRecommend> getLatestRecommend(int limit);
}
