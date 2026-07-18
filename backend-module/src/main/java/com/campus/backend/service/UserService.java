package com.campus.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.entity.User;
import com.campus.common.dto.UserDTO;

public interface UserService {
    IPage<User> pageList(int page, int size, String role);
    User create(UserDTO dto);
    void updateStatus(Long id, Integer status);
    void resetPassword(Long id);
    void delete(Long id);
}
