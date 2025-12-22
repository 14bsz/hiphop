package com.hiphop.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiphop.admin.entity.HNewsItem;
import com.hiphop.admin.mapper.HNewsItemMapper;
import com.hiphop.admin.service.HNewsItemService;
import org.springframework.stereotype.Service;

@Service
public class HNewsItemServiceImpl extends ServiceImpl<HNewsItemMapper, HNewsItem> implements HNewsItemService {
    @Override
    public boolean updateStatus(Long id, Integer status) {
        return lambdaUpdate()
                .set(HNewsItem::getStatus, status)
                .eq(HNewsItem::getId, id)
                .update();
    }
}
