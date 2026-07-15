package com.campus.backend.controller;

import com.campus.backend.entity.SysConfig;
import com.campus.backend.entity.SysDict;
import com.campus.backend.service.SystemService;
import com.campus.backend.util.SecurityHelper;
import com.campus.common.dto.SysConfigDTO;
import com.campus.common.dto.SysDictDTO;
import com.campus.common.validator.Create;
import com.campus.common.validator.Update;
import com.campus.common.vo.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system")
@AllArgsConstructor
public class SystemController {
    private final SystemService systemService;

    @GetMapping("/configs")
    public ApiResponse<List<SysConfig>> listConfigs(@RequestParam(required = false) String key, HttpServletRequest request) {
        SecurityHelper.requireAdmin(request);
        return ApiResponse.success(systemService.listConfigs(key));
    }

    @PutMapping("/configs")
    public ApiResponse<Void> saveConfigs(@RequestBody List<SysConfigDTO> dtos, HttpServletRequest request) {
        SecurityHelper.requireAdmin(request);
        systemService.saveConfigs(dtos);
        return ApiResponse.success();
    }

    @GetMapping("/dicts")
    public ApiResponse<List<SysDict>> listDicts(@RequestParam(required = false) String typeCode, @RequestParam(required = false) Integer status, HttpServletRequest request) {
        SecurityHelper.requireAdmin(request);
        return ApiResponse.success(systemService.listDicts(typeCode, status));
    }

    @PostMapping("/dicts")
    public ApiResponse<SysDict> createDict(@Validated(Create.class) @RequestBody SysDictDTO dto, HttpServletRequest request) {
        SecurityHelper.requireAdmin(request);
        return ApiResponse.success(systemService.createDict(dto));
    }

    @PutMapping("/dicts/{id}")
    public ApiResponse<SysDict> updateDict(@PathVariable Long id, @Validated(Update.class) @RequestBody SysDictDTO dto, HttpServletRequest request) {
        SecurityHelper.requireAdmin(request);
        dto.setId(id);
        return ApiResponse.success(systemService.updateDict(id, dto));
    }
}
