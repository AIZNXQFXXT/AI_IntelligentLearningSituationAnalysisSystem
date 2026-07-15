package com.campus.backend.converter;

import com.campus.backend.entity.SysDict;
import com.campus.common.dto.SysDictDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SysDictConverter {
    public SysDict toEntity(SysDictDTO dto) {
        SysDict entity = new SysDict();
        entity.setId(dto.getId());
        entity.setTypeCode(dto.getTypeCode());
        entity.setItemCode(dto.getItemCode());
        entity.setItemValue(dto.getItemValue());
        entity.setSortOrder(dto.getSortOrder());
        entity.setStatus(dto.getStatus());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    public SysDictDTO toDTO(SysDict entity) {
        SysDictDTO dto = new SysDictDTO();
        dto.setId(entity.getId());
        dto.setTypeCode(entity.getTypeCode());
        dto.setItemCode(entity.getItemCode());
        dto.setItemValue(entity.getItemValue());
        dto.setSortOrder(entity.getSortOrder());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
