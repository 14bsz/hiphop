package com.hiphop.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiphop.admin.entity.HContentItem;
import com.hiphop.admin.mapper.HContentItemMapper;
import com.hiphop.admin.service.HContentItemService;
import org.springframework.stereotype.Service;

@Service
public class HContentItemServiceImpl extends ServiceImpl<HContentItemMapper, HContentItem> implements HContentItemService {
    @Override
    public boolean updateStatus(Long id, Integer status) {
        return lambdaUpdate().set(HContentItem::getStatus, status).eq(HContentItem::getId, id).update();
    }
}
