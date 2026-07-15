package com.campus.backend.util;

import com.campus.common.enums.ErrorCode;
import com.campus.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;

public class SecurityHelper {
    public static void requireAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) throw new BusinessException(ErrorCode.FORBIDDEN);
    }
}
