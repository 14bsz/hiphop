package com.hiphop.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hiphop.admin.entity.HFeaturedItem;
import com.hiphop.admin.service.HFeaturedItemService;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/home/featured")
public class HFeaturedItemController {
    private static final Logger logger = LoggerFactory.getLogger(HFeaturedItemController.class);
    
    @Autowired
    private HFeaturedItemService featuredItemService;
    
    @GetMapping
    public List<HFeaturedItem> list(@RequestParam(value = "position", required = false) String position, @RequestParam(value = "status", required = false) Integer status) {
        LambdaQueryWrapper<HFeaturedItem> qw = new LambdaQueryWrapper<>();
        if (position != null && !position.isEmpty()) {
            qw.eq(HFeaturedItem::getPosition, position);
        }
        if (status != null) {
            qw.eq(HFeaturedItem::getStatus, status);
        }
        qw.orderByAsc(HFeaturedItem::getSortNo);
        return featuredItemService.list(qw);
    }
    
    @PostMapping
    public boolean create(@RequestBody HFeaturedItem item) {
        return featuredItemService.save(item);
    }

    @PutMapping("/{id}")
    public boolean update(@PathVariable("id") Long id, @RequestBody HFeaturedItem item) {
        logger.info("更新展位数据: id={}, position={}, title={}, status={}, sortNo={}, imgUrl={}, linkUrl={}, tag={}, views={}", 
            id, item.getPosition(), item.getTitle(), item.getStatus(), item.getSortNo(), 
            item.getImgUrl(), item.getLinkUrl(), item.getTag(), item.getViews());
        
        try {
            // 构建更新条件，使用链式调用一次性设置所有字段
            var update = featuredItemService.lambdaUpdate()
                .eq(HFeaturedItem::getId, id);
            
            // 设置所有字段（即使为 null 也要设置，确保能更新）
            if (item.getPosition() != null) {
                update.set(HFeaturedItem::getPosition, item.getPosition());
            }
            if (item.getTitle() != null) {
                update.set(HFeaturedItem::getTitle, item.getTitle());
            }
            if (item.getImgUrl() != null) {
                update.set(HFeaturedItem::getImgUrl, item.getImgUrl());
            }
            if (item.getLinkUrl() != null) {
                update.set(HFeaturedItem::getLinkUrl, item.getLinkUrl());
            }
            // 更新 tag 字段（允许为空字符串，用于清除标签）
            if (item.getTag() != null) {
                update.set(HFeaturedItem::getTag, item.getTag());
            }
            // 更新 views 字段（允许为空字符串，用于清除浏览量）
            if (item.getViews() != null) {
                update.set(HFeaturedItem::getViews, item.getViews());
            }
            if (item.getStatus() != null) {
                update.set(HFeaturedItem::getStatus, item.getStatus());
            }
            if (item.getSortNo() != null) {
                update.set(HFeaturedItem::getSortNo, item.getSortNo());
            }
            
            // 执行更新
            boolean result = update.update();
            logger.info("更新结果: {}, id={}", result, id);
            
            // 如果更新失败，查询记录是否存在
            if (!result) {
                HFeaturedItem existing = featuredItemService.getById(id);
                if (existing == null) {
                    logger.error("更新失败：记录不存在，id={}", id);
                } else {
                    logger.error("更新失败：记录存在但更新返回false，id={}, 当前数据: position={}, title={}, status={}", 
                        id, existing.getPosition(), existing.getTitle(), existing.getStatus());
                }
            }
            
            return result;
        } catch (Exception e) {
            logger.error("更新展位数据异常，id={}", id, e);
            throw e;
        }
    }
    
    @PatchMapping("/{id}/status")
    public boolean updateStatus(@PathVariable("id") Long id, @RequestParam("status") @NotNull Integer status) {
        return featuredItemService.updateStatus(id, status);
    }
    
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable("id") Long id) {
        return featuredItemService.removeById(id);
    }
}

