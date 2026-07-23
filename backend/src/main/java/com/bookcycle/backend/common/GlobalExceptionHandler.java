package com.bookcycle.backend.common;

import com.bookcycle.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired(required = false)
    private JwtUtil jwtUtil;

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<String> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpServletRequest request) {
        logger.error("接口不存在: {} {}", request.getMethod(), request.getRequestURI());
        return Result.error(404, "接口不存在: " + request.getMethod() + " " + request.getRequestURI());
    }

    // 认证和授权异常
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<String> handleUnauthorizedException(UnauthorizedException ex, HttpServletRequest request) {
        logger.warn("未授权访问: {} {} - {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return Result.error(401, ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<String> handleForbiddenException(ForbiddenException ex, HttpServletRequest request) {
        logger.warn("禁止访问: {} {} - {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return Result.error(403, ex.getMessage());
    }

    // 业务异常
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        logger.warn("业务异常: {} {} - {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return Result.error(400, ex.getMessage());
    }

    // 参数验证异常
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        logger.warn("参数错误: {} {} - {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return Result.error(400, "参数错误: " + ex.getMessage());
    }

    // 空指针异常（通常是程序错误）
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleNullPointerException(NullPointerException ex, HttpServletRequest request) {
        logger.error("空指针异常 [程序错误]: {} {} - {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return Result.error(500, "程序内部错误，请联系管理员");
    }

    // 数据库异常
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleDataAccessException(org.springframework.dao.DataAccessException ex, HttpServletRequest request) {
        logger.error("数据库异常: {} {} - {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return Result.error(500, "数据库操作失败，请稍后重试");
    }

    // JWT 相关异常
    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<String> handleJwtException(JwtException ex, HttpServletRequest request) {
        logger.warn("JWT异常: {} {} - {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return Result.error(401, ex.getMessage());
    }

    // 所有其他未捕获的异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleException(Exception ex, HttpServletRequest request) {
        logger.error("系统异常: {} {} - {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return Result.error(500, "系统错误，请稍后重试。如果问题持续存在，请联系管理员");
    }

    // 自定义异常：未授权
    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }

    // 自定义异常：禁止访问
    public static class ForbiddenException extends RuntimeException {
        public ForbiddenException(String message) {
            super(message);
        }
    }

    // 自定义异常：业务异常
    public static class BusinessException extends RuntimeException {
        public BusinessException(String message) {
            super(message);
        }
    }

    // 自定义异常：JWT 异常
    public static class JwtException extends RuntimeException {
        public JwtException(String message) {
            super(message);
        }
    }
}
