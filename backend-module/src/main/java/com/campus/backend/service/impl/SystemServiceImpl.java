package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.backend.converter.SysConfigConverter;
import com.campus.backend.converter.SysDictConverter;
import com.campus.backend.entity.SysConfig;
import com.campus.backend.entity.SysDict;
import com.campus.backend.mapper.SysConfigMapper;
import com.campus.backend.mapper.SysDictMapper;
import com.campus.backend.service.SystemService;
import com.campus.common.dto.SysConfigDTO;
import com.campus.common.dto.SysDictDTO;
import com.campus.common.enums.ErrorCode;
import com.campus.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class SystemServiceImpl implements SystemService {
    private final SysConfigConverter configConverter;
    private final SysConfigMapper configMapper;
    private final SysDictConverter dictConverter;
    private final SysDictMapper dictMapper;

    @Override
    public List<SysConfig> listConfigs(String key) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        if (key != null && !key.isEmpty()) {
            wrapper.eq(SysConfig::getConfigKey, key);
        }
        return configMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void saveConfigs(List<SysConfigDTO> dtos) {
        for (SysConfigDTO dto : dtos) {
            LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysConfig::getConfigKey, dto.getConfigKey());
            SysConfig existing = configMapper.selectOne(wrapper);
            if (existing != null) {
                configConverter.toEntity(dto);
                existing.setConfigValue(dto.getConfigValue());
                existing.setDescription(dto.getDescription());
                existing.setUpdatedAt(LocalDateTime.now());
                configMapper.updateById(existing);
            } else {
                SysConfig entity = configConverter.toEntity(dto);
                entity.setConfigKey(dto.getConfigKey());
                entity.setConfigValue(dto.getConfigValue());
                entity.setDescription(dto.getDescription());
                entity.setCreatedAt(LocalDateTime.now());
                entity.setUpdatedAt(LocalDateTime.now());
                configMapper.insert(entity);
            }
        }
    }

    @Override
    public List<SysDict> listDicts(String typeCode, Integer status) {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        if (typeCode != null && !typeCode.isEmpty()) {
            wrapper.eq(SysDict::getTypeCode, typeCode);
        }
        if (status != null) {
            wrapper.eq(SysDict::getStatus, status);
        }
        wrapper.orderByAsc(SysDict::getSortOrder);
        return dictMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public SysDict createDict(SysDictDTO dto) {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDict::getTypeCode, dto.getTypeCode());
        wrapper.eq(SysDict::getItemCode, dto.getItemCode());
        if (dictMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
        SysDict entity = dictConverter.toEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        dictMapper.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public SysDict updateDict(Long id, SysDictDTO dto) {
        SysDict entity = dictMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        entity.setItemValue(dto.getItemValue());
        entity.setSortOrder(dto.getSortOrder());
        entity.setStatus(dto.getStatus());
        entity.setUpdatedAt(LocalDateTime.now());
        dictMapper.updateById(entity);
        return entity;
    }
}
