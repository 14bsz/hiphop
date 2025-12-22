package com.hiphop.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hiphop.admin.entity.HTrendingItem;

public interface HTrendingItemService extends IService<HTrendingItem> {
    boolean updateStatus(Long id, Integer status);
}




