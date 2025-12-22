package com.hiphop.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiphop.admin.entity.HFeaturedItem;
import com.hiphop.admin.mapper.HFeaturedItemMapper;
import com.hiphop.admin.service.HFeaturedItemService;
import org.springframework.stereotype.Service;

@Service
public class HFeaturedItemServiceImpl extends ServiceImpl<HFeaturedItemMapper, HFeaturedItem> implements HFeaturedItemService {
    @Override
    public boolean updateStatus(Long id, Integer status) {
        return lambdaUpdate().set(HFeaturedItem::getStatus, status).eq(HFeaturedItem::getId, id).update();
    }
}











