package com.campus.backend.util;

import com.campus.common.enums.ErrorCode;
import com.campus.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;

public class SecurityHelper {
    public static void requireAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) throw new BusinessException(ErrorCode.FORBIDDEN);
    }

    public static void requireAnyRole(HttpServletRequest request, String... roles) {
        String role = (String) request.getAttribute("role");
        if (role == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
        for (String r : roles) {
            if (r.equals(role)) return;
        }
        throw new BusinessException(ErrorCode.FORBIDDEN);
    }
}
