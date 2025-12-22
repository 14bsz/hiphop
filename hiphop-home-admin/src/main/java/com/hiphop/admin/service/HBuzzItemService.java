package com.hiphop.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hiphop.admin.entity.HBuzzItem;

public interface HBuzzItemService extends IService<HBuzzItem> {
    boolean updateStatus(Long id, Integer status);
}
