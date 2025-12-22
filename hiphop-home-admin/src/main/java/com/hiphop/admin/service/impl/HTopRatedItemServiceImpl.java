package com.hiphop.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiphop.admin.entity.HTopRatedItem;
import com.hiphop.admin.mapper.HTopRatedItemMapper;
import com.hiphop.admin.service.HTopRatedItemService;
import org.springframework.stereotype.Service;

@Service
public class HTopRatedItemServiceImpl extends ServiceImpl<HTopRatedItemMapper, HTopRatedItem> implements HTopRatedItemService {

    @Override
    public boolean updateStatus(Long id, Integer status) {
        return lambdaUpdate()
                .set(HTopRatedItem::getStatus, status)
                .eq(HTopRatedItem::getId, id)
                .update();
    }
}










