package com.hiphop.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiphop.admin.entity.HDailyRecommend;
import com.hiphop.admin.service.HDailyRecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/home/daily")
@CrossOrigin
public class HDailyRecommendController {

    @Autowired
    private HDailyRecommendService dailyRecommendService;

    /**
     * 分页查询每日推荐列表
     */
    @GetMapping
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status) {
        
        LambdaQueryWrapper<HDailyRecommend> queryWrapper = new LambdaQueryWrapper<>();
        
        if (title != null && !title.trim().isEmpty()) {
            queryWrapper.like(HDailyRecommend::getTitle, title.trim());
        }
        if (status != null) {
            queryWrapper.eq(HDailyRecommend::getStatus, status);
        }
        
        queryWrapper.orderByDesc(HDailyRecommend::getRecommendDate);
        queryWrapper.orderByAsc(HDailyRecommend::getSortNo);
        
        Page<HDailyRecommend> pageParam = new Page<>(page, size);
        IPage<HDailyRecommend> pageResult = dailyRecommendService.page(pageParam, queryWrapper);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        
        return result;
    }

    /**
     * 获取最新的推荐列表（用于前台展示）
     */
    @GetMapping("/latest")
    public List<HDailyRecommend> getLatest(@RequestParam(defaultValue = "10") int limit) {
        return dailyRecommendService.getLatestRecommend(limit);
    }

    /**
     * 获取指定日期的推荐
     */
    @GetMapping("/date/{date}")
    public List<HDailyRecommend> getByDate(@PathVariable String date) {
        return dailyRecommendService.getRecommendByDate(date);
    }

    /**
     * 新增推荐
     */
    @PostMapping
    public Map<String, Object> create(@RequestBody HDailyRecommend recommend) {
        if (recommend.getRecommendDate() == null) {
            recommend.setRecommendDate(new Date());
        }
        if (recommend.getStatus() == null) {
            recommend.setStatus(1);
        }
        if (recommend.getSortNo() == null) {
            recommend.setSortNo(0);
        }
        if (recommend.getBadge() == null || recommend.getBadge().isEmpty()) {
            recommend.setBadge("热度爆表");
        }
        
        dailyRecommendService.save(recommend);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "创建成功");
        result.put("data", recommend);
        return result;
    }

    /**
     * 更新推荐
     */
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody HDailyRecommend recommend) {
        recommend.setId(id);
        dailyRecommendService.updateById(recommend);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "更新成功");
        return result;
    }

    /**
     * 删除推荐
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        dailyRecommendService.removeById(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "删除成功");
        return result;
    }

    /**
     * 更新状态
     */
    @PatchMapping("/{id}/status")
    public Map<String, Object> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        HDailyRecommend recommend = new HDailyRecommend();
        recommend.setId(id);
        recommend.setStatus(status);
        dailyRecommendService.updateById(recommend);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "状态更新成功");
        return result;
    }

    /**
     * 手动触发抓取每日推荐
     */
    @PostMapping("/fetch")
    public Map<String, Object> fetchDaily(
            @RequestParam(defaultValue = "5205824122") String playlistId,
            @RequestParam(defaultValue = "10") int count) {
        
        int fetchedCount = dailyRecommendService.fetchDailyRecommend(playlistId, count);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", fetchedCount > 0);
        result.put("message", fetchedCount > 0 ? "抓取成功" : "抓取失败");
        result.put("count", fetchedCount);
        return result;
    }

    /**
     * 获取详情
     */
    @GetMapping("/{id}")
    public HDailyRecommend getById(@PathVariable Long id) {
        return dailyRecommendService.getById(id);
    }
}
