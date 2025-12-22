package com.hiphop.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiphop.admin.entity.HBuzzItem;
import com.hiphop.admin.mapper.HBuzzItemMapper;
import com.hiphop.admin.service.HBuzzItemService;
import org.springframework.stereotype.Service;

@Service
public class HBuzzItemServiceImpl extends ServiceImpl<HBuzzItemMapper, HBuzzItem> implements HBuzzItemService {
    @Override
    public boolean updateStatus(Long id, Integer status) {
        return lambdaUpdate().set(HBuzzItem::getStatus, status).eq(HBuzzItem::getId, id).update();
    }
}
