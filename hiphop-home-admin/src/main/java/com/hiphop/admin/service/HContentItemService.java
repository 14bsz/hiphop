package com.hiphop.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hiphop.admin.entity.HContentItem;

public interface HContentItemService extends IService<HContentItem> {
    boolean updateStatus(Long id, Integer status);
}
