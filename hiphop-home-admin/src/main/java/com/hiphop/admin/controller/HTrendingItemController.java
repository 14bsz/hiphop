package com.hiphop.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hiphop.admin.entity.HTrendingItem;
import com.hiphop.admin.service.HTrendingItemService;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/home/trending")
public class HTrendingItemController {
    private static final Logger logger = LoggerFactory.getLogger(HTrendingItemController.class);
    
    @Autowired
    private HTrendingItemService trendingItemService;
    
    @GetMapping
    public List<HTrendingItem> list(@RequestParam(value = "position", required = false) Integer position, 
                                    @RequestParam(value = "status", required = false) Integer status) {
        LambdaQueryWrapper<HTrendingItem> qw = new LambdaQueryWrapper<>();
        if (position != null) {
            qw.eq(HTrendingItem::getPosition, position);
        }
        if (status != null) {
            qw.eq(HTrendingItem::getStatus, status);
        }
        qw.orderByAsc(HTrendingItem::getSortNo).orderByAsc(HTrendingItem::getPosition);
        return trendingItemService.list(qw);
    }
    
    @PostMapping
    public boolean create(@RequestBody HTrendingItem item) {
        // 验证位置范围
        if (item.getPosition() == null || item.getPosition() < 1 || item.getPosition() > 5) {
            throw new RuntimeException("位置必须在1-5之间");
        }
        
        // 如果状态为启用，检查该位置是否已有启用的记录
        if (item.getStatus() == null || item.getStatus() == 1) {
            long count = trendingItemService.count(
                new LambdaQueryWrapper<HTrendingItem>()
                    .eq(HTrendingItem::getPosition, item.getPosition())
                    .eq(HTrendingItem::getStatus, 1)
            );
            if (count > 0) {
                throw new RuntimeException("位置" + item.getPosition() + "已有启用的热门趋势，请先禁用或删除");
            }
        }
        
        // 如果未设置排序号，使用位置作为默认排序
        if (item.getSortNo() == null) {
            item.setSortNo(item.getPosition());
        }
        // 默认标签
        if (item.getTag() == null || item.getTag().isEmpty()) {
            item.setTag("NEWS");
        }
        // 默认浏览量
        if (item.getViews() == null || item.getViews().isEmpty()) {
            item.setViews("0");
        }
        // 默认状态
        if (item.getStatus() == null) {
            item.setStatus(1);
        }
        return trendingItemService.save(item);
    }

    @PutMapping("/{id}")
    public boolean update(@PathVariable("id") Long id, @RequestBody HTrendingItem item) {
        logger.info("更新热门趋势数据: id={}, position={}, title={}, status={}, sortNo={}, imgUrl={}, linkUrl={}", 
            id, item.getPosition(), item.getTitle(), item.getStatus(), item.getSortNo(), 
            item.getImgUrl(), item.getLinkUrl());
        
        try {
            // 如果更新了位置，验证位置范围
            if (item.getPosition() != null) {
                if (item.getPosition() < 1 || item.getPosition() > 5) {
                    throw new RuntimeException("位置必须在1-5之间");
                }
            }
            
            // 如果状态为启用且位置有变化，检查新位置是否已有启用的记录
            if ((item.getStatus() == null || item.getStatus() == 1) && item.getPosition() != null) {
                long count = trendingItemService.count(
                    new LambdaQueryWrapper<HTrendingItem>()
                        .eq(HTrendingItem::getPosition, item.getPosition())
                        .eq(HTrendingItem::getStatus, 1)
                        .ne(HTrendingItem::getId, id) // 排除当前记录
                );
                if (count > 0) {
                    throw new RuntimeException("位置" + item.getPosition() + "已有启用的热门趋势，请先禁用或删除");
                }
            }
        
            var update = trendingItemService.lambdaUpdate()
                .eq(HTrendingItem::getId, id);
            
            if (item.getPosition() != null) {
                update.set(HTrendingItem::getPosition, item.getPosition());
            }
            if (item.getTitle() != null) {
                update.set(HTrendingItem::getTitle, item.getTitle());
            }
            if (item.getImgUrl() != null) {
                update.set(HTrendingItem::getImgUrl, item.getImgUrl());
            }
            if (item.getLinkUrl() != null) {
                update.set(HTrendingItem::getLinkUrl, item.getLinkUrl());
            }
            if (item.getTag() != null) {
                update.set(HTrendingItem::getTag, item.getTag());
            }
            if (item.getViews() != null) {
                update.set(HTrendingItem::getViews, item.getViews());
            }
            if (item.getStatus() != null) {
                update.set(HTrendingItem::getStatus, item.getStatus());
            }
            if (item.getSortNo() != null) {
                update.set(HTrendingItem::getSortNo, item.getSortNo());
            }
            
            boolean result = update.update();
            logger.info("更新结果: {}, id={}", result, id);
            
            if (!result) {
                HTrendingItem existing = trendingItemService.getById(id);
                if (existing == null) {
                    logger.error("更新失败：记录不存在，id={}", id);
                } else {
                    logger.error("更新失败：记录存在但更新返回false，id={}", id);
                }
            }
            
            return result;
        } catch (Exception e) {
            logger.error("更新热门趋势数据异常，id={}", id, e);
            throw e;
        }
    }
    
    @PatchMapping("/{id}/status")
    public boolean updateStatus(@PathVariable("id") Long id, @RequestParam("status") @NotNull Integer status) {
        return trendingItemService.updateStatus(id, status);
    }
    
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable("id") Long id) {
        return trendingItemService.removeById(id);
    }
}




