package com.bookcycle.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookcycle.backend.common.Result;
import com.bookcycle.backend.entity.Order;
import com.bookcycle.backend.entity.User;
import com.bookcycle.backend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/users")
    public Result<IPage<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<User> userPage = adminService.getUserList(page, size);
        return Result.success(userPage);
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(
            @PathVariable Integer id,
            @RequestParam Integer status) {
        try {
            adminService.updateUserStatus(id, status);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * ✅ 审核书籍列表：BLOB转String
     */
    @GetMapping("/books/review")
    public Result<IPage<Map<String, Object>>> getBookReviewList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        String sql = "SELECT * FROM book WHERE status = 0 ORDER BY create_time DESC";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        
        // ✅ BLOB转String
        for (Map<String, Object> row : list) {
            convertBlobToString(row, "images");
            convertBlobToString(row, "description");
        }
        
        Page<Map<String, Object>> pageResult = new Page<>(page, size);
        pageResult.setRecords(list);
        pageResult.setTotal(list.size());
        
        return Result.success(pageResult);
    }

    @PutMapping("/books/{id}/approve")
    public Result<Void> approveBook(@PathVariable Integer id) {
        try {
            adminService.approveBook(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PutMapping("/books/{id}/reject")
    public Result<Void> rejectBook(@PathVariable Integer id) {
        try {
            adminService.rejectBook(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/orders")
    public Result<IPage<Order>> getOrderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<Order> orderPage = adminService.getOrderList(page, size);
        return Result.success(orderPage);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = adminService.getStatistics();
        return Result.success(stats);
    }

    /**
     * BLOB转String工具方法
     */
    private void convertBlobToString(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value instanceof byte[]) {
            row.put(key, new String((byte[]) value, StandardCharsets.UTF_8));
        }
    }
}