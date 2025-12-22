package com.hiphop.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hiphop.admin.entity.HSection;

public interface HSectionService extends IService<HSection> {
    boolean updateStatus(Long id, Integer status);
}
