package com.hiphop.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hiphop.admin.entity.HNewsItem;

public interface HNewsItemService extends IService<HNewsItem> {
    boolean updateStatus(Long id, Integer status);
}
