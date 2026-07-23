package com.bookcycle.backend.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class UserContext {

    public static Integer getCurrentUserId() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            Object userId = request.getAttribute("currentUserId");
            if (userId != null) {
                return (Integer) userId;
            }
        }
        return null;
    }

    public static String getCurrentUsername() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            Object username = request.getAttribute("currentUsername");
            if (username != null) {
                return (String) username;
            }
        }
        return null;
    }

    public static String getCurrentUserRole() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            Object role = request.getAttribute("currentUserRole");
            if (role != null) {
                return (String) role;
            }
        }
        return null;
    }

    public static boolean isAdmin() {
        return "admin".equals(getCurrentUserRole());
    }

    private static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getRequest();
        }
        return null;
    }
}
