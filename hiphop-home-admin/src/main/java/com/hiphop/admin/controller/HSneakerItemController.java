package com.hiphop.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hiphop.admin.entity.HSneakerItem;
import com.hiphop.admin.service.CrawlerService;
import com.hiphop.admin.service.HSneakerItemService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 球鞋资讯管理接口
 */
@RestController
@RequestMapping("/api/home/sneakers")
public class HSneakerItemController {

    @Autowired
    private HSneakerItemService sneakerItemService;

    @Autowired
    private CrawlerService crawlerService;

    /**
     * 列表查询
     */
    @GetMapping
    public List<HSneakerItem> list(@RequestParam(value = "title", required = false) String title,
                                   @RequestParam(value = "status", required = false) Integer status) {
        LambdaQueryWrapper<HSneakerItem> qw = new LambdaQueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            qw.like(HSneakerItem::getTitle, title);
        }
        if (status != null) {
            qw.eq(HSneakerItem::getStatus, status);
        }
        qw.orderByDesc(HSneakerItem::getCreatedAt)
          .orderByAsc(HSneakerItem::getSortNo);
        return sneakerItemService.list(qw);
    }

    /**
     * 新增
     */
    @PostMapping
    public boolean create(@RequestBody HSneakerItem item) {
        if (item.getStatus() == null) {
            item.setStatus(1);
        }
        if (item.getSortNo() == null) {
            item.setSortNo(0);
        }
        Date now = new Date();
        if (item.getCreatedAt() == null) {
            item.setCreatedAt(now);
        }
        item.setUpdatedAt(now);
        return sneakerItemService.save(item);
    }

    /**
     * 更新
     */
    @PutMapping("/{id}")
    public boolean update(@PathVariable("id") Long id, @RequestBody HSneakerItem item) {
        item.setId(id);
        item.setUpdatedAt(new Date());
        return sneakerItemService.updateById(item);
    }

    /**
     * 更新状态
     */
    @PatchMapping("/{id}/status")
    public boolean updateStatus(@PathVariable("id") Long id, @RequestParam("status") @NotNull Integer status) {
        return sneakerItemService.updateStatus(id, status);
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable("id") Long id) {
        return sneakerItemService.removeById(id);
    }

    /**
     * 自动爬取内容：支持得物等球鞋详情页
     */
    @PostMapping("/fetch")
    public Map<String, String> fetchContent(@RequestParam("url") String url) throws IOException, InterruptedException {
        return crawlerService.fetchContent(url);
    }
}
