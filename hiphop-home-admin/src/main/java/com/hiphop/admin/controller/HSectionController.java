package com.hiphop.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hiphop.admin.entity.HSection;
import com.hiphop.admin.service.HSectionService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/home/sections")
public class HSectionController {
    @Autowired
    private HSectionService sectionService;
    @GetMapping
    public List<HSection> list(@RequestParam(value = "status", required = false) Integer status, @RequestParam(value = "visible", required = false) Integer visible) {
        LambdaQueryWrapper<HSection> qw = new LambdaQueryWrapper<>();
        if (status != null) qw.eq(HSection::getStatus, status);
        if (visible != null) qw.eq(HSection::getVisible, visible);
        qw.orderByAsc(HSection::getSortNo);
        return sectionService.list(qw);
    }
    @PostMapping
    public boolean create(@RequestBody HSection section) {
        return sectionService.save(section);
    }
    @PutMapping("/{id}")
    public boolean update(@PathVariable("id") Long id, @RequestBody HSection section) {
        section.setId(id);
        return sectionService.updateById(section);
    }
    @PatchMapping("/{id}/status")
    public boolean updateStatus(@PathVariable("id") Long id, @RequestParam("status") @NotNull Integer status) {
        return sectionService.updateStatus(id, status);
    }
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable("id") Long id) {
        return sectionService.removeById(id);
    }
}
