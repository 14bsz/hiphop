package com.hiphop.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hiphop.admin.entity.HNewsItem;
import com.hiphop.admin.service.HNewsItemService;
import com.hiphop.admin.service.CrawlerService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 最新资讯管理接口
 */
@RestController
@RequestMapping("/api/home/news")
public class HNewsItemController {

    @Autowired
    private HNewsItemService newsItemService;

    @Autowired
    private CrawlerService crawlerService;

    /**
     * 列表查询
     */
    @GetMapping
    public List<HNewsItem> list(@RequestParam(value = "title", required = false) String title,
                                @RequestParam(value = "status", required = false) Integer status) {
        LambdaQueryWrapper<HNewsItem> qw = new LambdaQueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            qw.like(HNewsItem::getTitle, title);
        }
        if (status != null) {
            qw.eq(HNewsItem::getStatus, status);
        }
        qw.orderByDesc(HNewsItem::getCreatedAt)
          .orderByAsc(HNewsItem::getSortNo);
        return newsItemService.list(qw);
    }

    /**
     * 新增
     */
    @PostMapping
    public boolean create(@RequestBody HNewsItem item) {
        if (item.getStatus() == null) {
            item.setStatus(1);
        }
        if (item.getSortNo() == null) {
            item.setSortNo(0);
        }
        if (item.getCreatedAt() == null) {
            item.setCreatedAt(new Date());
        }
        item.setUpdatedAt(new Date());
        return newsItemService.save(item);
    }

    /**
     * 更新
     */
    @PutMapping("/{id}")
    public boolean update(@PathVariable("id") Long id, @RequestBody HNewsItem item) {
        item.setId(id);
        item.setUpdatedAt(new Date());
        return newsItemService.updateById(item);
    }

    /**
     * 更新状态
     */
    @PatchMapping("/{id}/status")
    public boolean updateStatus(@PathVariable("id") Long id, @RequestParam("status") @NotNull Integer status) {
        return newsItemService.updateStatus(id, status);
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable("id") Long id) {
        return newsItemService.removeById(id);
    }

    /**
     * 自动爬取内容：根据URL或BV号自动获取标题和封面
     */
    @PostMapping("/fetch")
    public Map<String, String> fetchContent(@RequestParam("url") String url) {
        try {
            return crawlerService.fetchContent(url);
        } catch (Exception e) {
            throw new RuntimeException("爬取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 快速收藏：根据当前页面 URL 一键抓取并保存为最新资讯
     * 设计给浏览器书签/脚本使用，支持直接 GET 访问
     */
    @GetMapping("/quick-add")
    public HNewsItem quickAdd(@RequestParam("url") String url) {
        try {
            Map<String, String> data = crawlerService.fetchContent(url);
            HNewsItem item = new HNewsItem();
            item.setTitle(data.getOrDefault("title", "无标题"));
            item.setImgUrl(data.getOrDefault("coverImage", ""));
            item.setLinkUrl(data.getOrDefault("sourceUrl", url));
            // 简单根据链接判断来源
            String source = null;
            String lower = url.toLowerCase();
            if (lower.contains("mp.weixin.qq.com") || lower.contains("weixin.qq.com")) {
                source = "公众号";
            } else if (lower.contains("bilibili.com") || lower.contains("b23.tv") || lower.startsWith("bv")) {
                source = "B站";
            }
            item.setSource(source);
            item.setCategory(null);
            item.setSortNo(0);
            item.setStatus(1);
            Date now = new Date();
            item.setCreatedAt(now);
            item.setUpdatedAt(now);
            newsItemService.save(item);
            return item;
        } catch (Exception e) {
            throw new RuntimeException("快速收藏失败: " + e.getMessage(), e);
        }
    }
}
