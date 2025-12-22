package com.hiphop.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hiphop.admin.entity.HContentItem;
import com.hiphop.admin.service.HContentItemService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/home/items")
public class HContentItemController {
    @Autowired
    private HContentItemService contentItemService;
    @GetMapping
    public List<HContentItem> list(@RequestParam(value = "sectionId", required = false) Long sectionId, @RequestParam(value = "type", required = false) String type, @RequestParam(value = "status", required = false) Integer status) {
        LambdaQueryWrapper<HContentItem> qw = new LambdaQueryWrapper<>();
        if (sectionId != null) qw.eq(HContentItem::getSectionId, sectionId);
        if (type != null && !type.isEmpty()) qw.eq(HContentItem::getType, type);
        if (status != null) qw.eq(HContentItem::getStatus, status);
        Date now = new Date();
        qw.and(w -> w.isNull(HContentItem::getStartTime).or().le(HContentItem::getStartTime, now));
        qw.and(w -> w.isNull(HContentItem::getEndTime).or().ge(HContentItem::getEndTime, now));
        qw.orderByAsc(HContentItem::getSortNo);
        return contentItemService.list(qw);
    }
    @PostMapping
    public boolean create(@RequestBody HContentItem item) {
        return contentItemService.save(item);
    }
    @PutMapping("/{id}")
    public boolean update(@PathVariable("id") Long id, @RequestBody HContentItem item) {
        item.setId(id);
        return contentItemService.updateById(item);
    }
    @PatchMapping("/{id}/status")
    public boolean updateStatus(@PathVariable("id") Long id, @RequestParam("status") @NotNull Integer status) {
        return contentItemService.updateStatus(id, status);
    }
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable("id") Long id) {
        return contentItemService.removeById(id);
    }
}
