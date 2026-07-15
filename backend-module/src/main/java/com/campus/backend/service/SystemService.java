package com.campus.backend.service;

import com.campus.backend.entity.SysConfig;
import com.campus.backend.entity.SysDict;
import com.campus.common.dto.SysConfigDTO;
import com.campus.common.dto.SysDictDTO;
import java.util.List;

public interface SystemService {
    List<SysConfig> listConfigs(String key);
    void saveConfigs(List<SysConfigDTO> dtos);
    List<SysDict> listDicts(String typeCode, Integer status);
    SysDict createDict(SysDictDTO dto);
    SysDict updateDict(Long id, SysDictDTO dto);
}
