package com.campus.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.entity.User;
import com.campus.backend.service.UserService;
import com.campus.common.dto.UserDTO;
import com.campus.common.validator.Create;
import com.campus.common.vo.ApiResponse;
import com.campus.common.vo.PageResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<PageResult<User>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role) {
        IPage<User> result = userService.pageList(page, size, role);
        return ApiResponse.success(PageResult.of(
                result.getRecords(), result.getTotal(), page, size));
    }

    @PostMapping
    public ApiResponse<User> create(@Validated(Create.class) @RequestBody UserDTO dto) {
        return ApiResponse.success(userService.create(dto));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return ApiResponse.success();
    }
}
