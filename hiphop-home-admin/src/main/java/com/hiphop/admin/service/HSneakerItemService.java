package com.hiphop.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hiphop.admin.entity.HSneakerItem;

public interface HSneakerItemService extends IService<HSneakerItem> {
    boolean updateStatus(Long id, Integer status);
}
