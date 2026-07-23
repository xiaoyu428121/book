package com.bookcycle.backend.interceptor;

import com.bookcycle.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 处理 CORS 预检请求 - 放行，让 Spring 的 CORS 处理
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        
        String token = request.getHeader("Authorization");
        System.out.println("[JWT拦截器] 请求: " + request.getMethod() + " " + request.getRequestURI());
        System.out.println("[JWT拦截器] Token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        if (token == null || token.isEmpty()) {
            System.out.println("[JWT拦截器] Token为空");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\",\"data\":null}");
            return false;
        }
        
        if (!jwtUtil.validateToken(token)) {
            System.out.println("[JWT拦截器] Token无效或已过期");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"token无效或已过期\",\"data\":null}");
            return false;
        }
        
        Integer userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);
        
        request.setAttribute("currentUserId", userId);
        request.setAttribute("currentUsername", username);
        request.setAttribute("currentUserRole", role);
        
        System.out.println("[JWT拦截器] 用户ID: " + userId + ", 用户名: " + username);
        return true;
    }
}
