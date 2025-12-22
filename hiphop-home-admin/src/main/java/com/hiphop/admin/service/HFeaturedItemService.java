package com.hiphop.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hiphop.admin.entity.HFeaturedItem;

public interface HFeaturedItemService extends IService<HFeaturedItem> {
    boolean updateStatus(Long id, Integer status);
}











