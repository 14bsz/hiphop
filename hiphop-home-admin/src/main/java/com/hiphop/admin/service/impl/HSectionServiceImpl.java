package com.hiphop.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiphop.admin.entity.HSection;
import com.hiphop.admin.mapper.HSectionMapper;
import com.hiphop.admin.service.HSectionService;
import org.springframework.stereotype.Service;

@Service
public class HSectionServiceImpl extends ServiceImpl<HSectionMapper, HSection> implements HSectionService {
    @Override
    public boolean updateStatus(Long id, Integer status) {
        return lambdaUpdate().set(HSection::getStatus, status).eq(HSection::getId, id).update();
    }
}
