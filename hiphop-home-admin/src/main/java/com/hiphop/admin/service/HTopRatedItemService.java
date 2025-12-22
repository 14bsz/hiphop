package com.hiphop.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hiphop.admin.entity.HTopRatedItem;

public interface HTopRatedItemService extends IService<HTopRatedItem> {

    /**
     * 更新状态
     */
    boolean updateStatus(Long id, Integer status);
}










