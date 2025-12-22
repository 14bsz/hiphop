package com.hiphop.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiphop.admin.entity.HSneakerItem;
import com.hiphop.admin.mapper.HSneakerItemMapper;
import com.hiphop.admin.service.HSneakerItemService;
import org.springframework.stereotype.Service;

@Service
public class HSneakerItemServiceImpl extends ServiceImpl<HSneakerItemMapper, HSneakerItem> implements HSneakerItemService {
    @Override
    public boolean updateStatus(Long id, Integer status) {
        return lambdaUpdate()
                .set(HSneakerItem::getStatus, status)
                .eq(HSneakerItem::getId, id)
                .update();
    }
}
