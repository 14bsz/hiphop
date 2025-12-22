package com.hiphop.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hiphop.admin.entity.HTopRatedItem;
import com.hiphop.admin.service.CrawlerService;
import com.hiphop.admin.service.HTopRatedItemService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 高分音乐榜接口
 * 对应前台 /api/home/top-rated，用于首页「高分音乐榜」模块和后台管理增删改查。
 */
@RestController
@RequestMapping("/api/home/top-rated")
public class HTopRatedItemController {

    @Autowired
    private HTopRatedItemService topRatedItemService;

    @Autowired
    private CrawlerService crawlerService;

    /**
     * 列表查询
     */
    @GetMapping
    public List<HTopRatedItem> list(@RequestParam(value = "title", required = false) String title,
                                    @RequestParam(value = "status", required = false) Integer status,
                                    @RequestParam(value = "type", required = false) Integer type) {
        LambdaQueryWrapper<HTopRatedItem> qw = new LambdaQueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            qw.like(HTopRatedItem::getTitle, title);
        }
        if (status != null) {
            qw.eq(HTopRatedItem::getStatus, status);
        }
        if (type != null) {
            qw.eq(HTopRatedItem::getType, type);
        }
        // 默认按分享数（favoriteCount）从高到低排序，其次按 sortNo 从小到大兜底
        qw.orderByDesc(HTopRatedItem::getFavoriteCount)
          .orderByAsc(HTopRatedItem::getSortNo);
        return topRatedItemService.list(qw);
    }

    /**
     * 新增
     */
    @PostMapping
    public boolean create(@RequestBody HTopRatedItem item) {
        // 默认类型：单曲
        if (item.getType() == null) {
            item.setType(1);
        }
        // 限制每种类型最多只能有 6 条记录
        long count = topRatedItemService.count(
                new LambdaQueryWrapper<HTopRatedItem>()
                        .eq(HTopRatedItem::getType, item.getType())
        );
        if (count >= 6) {
            throw new RuntimeException("高分音乐榜每种类型最多只能添加 6 条记录");
        }
        // 默认按已有数量顺序追加，排名从 1 开始
        if (item.getSortNo() == null || item.getSortNo() < 1) {
            item.setSortNo((int) count + 1);
        }
        return topRatedItemService.save(item);
    }

    /**
     * 更新
     */
    @PutMapping("/{id}")
    public boolean update(@PathVariable("id") Long id, @RequestBody HTopRatedItem item) {
        item.setId(id);
        return topRatedItemService.updateById(item);
    }

    /**
     * 更新状态
     */
    @PatchMapping("/{id}/status")
    public boolean updateStatus(@PathVariable("id") Long id,
                                @RequestParam("status") @NotNull Integer status) {
        return topRatedItemService.updateStatus(id, status);
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable("id") Long id) {
        return topRatedItemService.removeById(id);
    }

    /**
     * 网易云歌曲搜索
     */
    @GetMapping("/netease/search")
    public List<Map<String, Object>> searchNetease(@RequestParam("keywords") String keywords,
                                                   @RequestParam(value = "limit", required = false) Integer limit) {
        try {
            return crawlerService.searchNeteaseSongs(keywords, limit);
        } catch (Exception e) {
            throw new RuntimeException("搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 网易云专辑搜索
     */
    @GetMapping("/netease/search-album")
    public List<Map<String, Object>> searchNeteaseAlbum(@RequestParam("keywords") String keywords,
                                                        @RequestParam(value = "limit", required = false) Integer limit) {
        try {
            return crawlerService.searchNeteaseAlbums(keywords, limit);
        } catch (Exception e) {
            throw new RuntimeException("搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量导入网易云歌曲
     */
    @PostMapping("/netease/import")
    public boolean importNeteaseSongs(@RequestBody List<HTopRatedItem> songs) {
        if (songs == null || songs.isEmpty()) {
            return true;
        }
        // 默认类型：单曲
        Integer type = songs.get(0).getType();
        if (type == null) {
            type = 1;
        }
        long count = topRatedItemService.count(
                new LambdaQueryWrapper<HTopRatedItem>()
                        .eq(HTopRatedItem::getType, type)
        );
        int remain = 6 - (int) count;
        if (remain <= 0) {
            throw new RuntimeException("高分音乐榜该类型最多只能添加 6 条记录，当前已满");
        }
        if (songs.size() > remain) {
            throw new RuntimeException("最多还能再添加 " + remain + " 条记录");
        }
        // 排名从 1 开始，后续递增
        int baseSort = (int) count + 1;
        for (int i = 0; i < songs.size(); i++) {
            HTopRatedItem item = songs.get(i);
            item.setId(null);
            if (item.getType() == null) {
                item.setType(type);
            }
            if (item.getRating() == null) {
                item.setRating(4.0);
            }
            if (item.getSortNo() == null || item.getSortNo() < 1) {
                item.setSortNo(baseSort + i);
            }
            if (item.getStatus() == null) {
                item.setStatus(1);
            }
        }
        return topRatedItemService.saveBatch(songs);
    }
}




