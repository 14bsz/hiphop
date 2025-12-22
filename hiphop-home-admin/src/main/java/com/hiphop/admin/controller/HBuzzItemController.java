package com.hiphop.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hiphop.admin.entity.HBuzzItem;
import com.hiphop.admin.service.HBuzzItemService;
import com.hiphop.admin.service.CrawlerService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/home/buzz")
public class HBuzzItemController {
    @Autowired
    private HBuzzItemService buzzItemService;
    
    @Autowired
    private CrawlerService crawlerService;
    
    @GetMapping
    public List<HBuzzItem> list(@RequestParam(value = "sectionId", required = false) Long sectionId, @RequestParam(value = "featured", required = false) Integer featured, @RequestParam(value = "status", required = false) Integer status) {
        LambdaQueryWrapper<HBuzzItem> qw = new LambdaQueryWrapper<>();
        if (sectionId != null) qw.eq(HBuzzItem::getSectionId, sectionId);
        if (featured != null) qw.eq(HBuzzItem::getFeatured, featured);
        if (status != null) qw.eq(HBuzzItem::getStatus, status);
        Date now = new Date();
        qw.and(w -> w.isNull(HBuzzItem::getStartTime).or().le(HBuzzItem::getStartTime, now));
        qw.and(w -> w.isNull(HBuzzItem::getEndTime).or().ge(HBuzzItem::getEndTime, now));
        qw.orderByAsc(HBuzzItem::getSortNo);
        return buzzItemService.list(qw);
    }
    
    @PostMapping
    public boolean create(@RequestBody HBuzzItem item) {
        return buzzItemService.save(item);
    }
    
    @PutMapping("/{id}")
    public boolean update(@PathVariable("id") Long id, @RequestBody HBuzzItem item) {
        item.setId(id);
        return buzzItemService.updateById(item);
    }
    
    @PatchMapping("/{id}/status")
    public boolean updateStatus(@PathVariable("id") Long id, @RequestParam("status") @NotNull Integer status) {
        return buzzItemService.updateStatus(id, status);
    }
    
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable("id") Long id) {
        return buzzItemService.removeById(id);
    }
    
    /**
     * 爬取内容接口：根据URL或BV号自动获取标题和封面
     */
    @PostMapping("/fetch")
    public Map<String, String> fetchContent(@RequestParam("url") String url) {
        try {
            return crawlerService.fetchContent(url);
        } catch (Exception e) {
            throw new RuntimeException("爬取失败: " + e.getMessage(), e);
        }
    }
}
