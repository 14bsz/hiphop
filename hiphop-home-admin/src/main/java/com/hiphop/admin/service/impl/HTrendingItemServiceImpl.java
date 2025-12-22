package com.hiphop.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiphop.admin.entity.HTrendingItem;
import com.hiphop.admin.mapper.HTrendingItemMapper;
import com.hiphop.admin.service.HTrendingItemService;
import org.springframework.stereotype.Service;

@Service
public class HTrendingItemServiceImpl extends ServiceImpl<HTrendingItemMapper, HTrendingItem> implements HTrendingItemService {
    @Override
    public boolean updateStatus(Long id, Integer status) {
        return lambdaUpdate().set(HTrendingItem::getStatus, status).eq(HTrendingItem::getId, id).update();
    }
}




