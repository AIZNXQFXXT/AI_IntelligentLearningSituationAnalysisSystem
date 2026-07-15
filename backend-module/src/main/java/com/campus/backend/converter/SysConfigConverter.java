package com.campus.backend.converter;

import com.campus.backend.entity.SysConfig;
import com.campus.common.dto.SysConfigDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SysConfigConverter {
    public SysConfig toEntity(SysConfigDTO dto) {
        SysConfig entity = new SysConfig();
        entity.setConfigKey(dto.getConfigKey());
        entity.setConfigValue(dto.getConfigValue());
        entity.setDescription(dto.getDescription());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    public SysConfigDTO toDTO(SysConfig entity) {
        SysConfigDTO dto = new SysConfigDTO();
        dto.setConfigKey(entity.getConfigKey());
        dto.setConfigValue(entity.getConfigValue());
        dto.setDescription(entity.getDescription());
        return dto;
    }
}
