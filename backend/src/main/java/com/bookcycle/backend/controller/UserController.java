package com.bookcycle.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bookcycle.backend.common.Result;
import com.bookcycle.backend.dto.*;
import com.bookcycle.backend.entity.Address;
import com.bookcycle.backend.entity.Order;
import com.bookcycle.backend.entity.User;
import com.bookcycle.backend.mapper.OrderMapper;
import com.bookcycle.backend.service.AddressService;
import com.bookcycle.backend.service.UserService;
import com.bookcycle.backend.util.UserContext;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AddressService addressService;
    
    @Autowired
    private OrderMapper orderMapper;

    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterDTO dto) {
        try {
            User user = userService.register(dto);
            return Result.success("注册成功", user);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<User> login(@Valid @RequestBody LoginDTO dto) {
        try {
            User user = userService.login(dto);
            return Result.success("登录成功", user);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/profile")
    public Result<User> getProfile() {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        User user = userService.getProfile(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 修复：真实交易统计逻辑（解决所有编译错误）
        // 1. 统计【卖出】数据（排除已取消订单 status=4）
        QueryWrapper<Order> sellQuery = new QueryWrapper<>();
        sellQuery.eq("seller_id", userId)
                 .ne("status", 4);
        // 修复：selectCount返回Long，转Integer
        Long sellCountLong = orderMapper.selectCount(sellQuery);
        Integer sellCount = sellCountLong != null ? sellCountLong.intValue() : 0;
        // 修复：使用自定义的selectSumPrice方法
        BigDecimal sellIncome = orderMapper.selectSumPrice(sellQuery);

        // 2. 统计【买入】数据（排除已取消订单 status=4）
        QueryWrapper<Order> buyQuery = new QueryWrapper<>();
        buyQuery.eq("buyer_id", userId)
                .ne("status", 4);
        // 修复：selectCount返回Long，转Integer
        Long buyCountLong = orderMapper.selectCount(buyQuery);
        Integer buyCount = buyCountLong != null ? buyCountLong.intValue() : 0;
        // 修复：使用自定义的selectSumPrice方法
        BigDecimal buyExpense = orderMapper.selectSumPrice(buyQuery);

        // 3. 设置统计数据（空值自动处理为0）
        user.setSellCount(sellCount);
        user.setSellIncome(sellIncome != null ? sellIncome : BigDecimal.ZERO);
        user.setBuyCount(buyCount);
        user.setBuyExpense(buyExpense != null ? buyExpense : BigDecimal.ZERO);

        return Result.success(user);
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileDTO dto) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        try {
            userService.updateProfile(userId, dto);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordDTO dto) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        try {
            userService.updatePassword(userId, dto);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/address")
    public Result<List<Address>> getAddressList() {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        List<Address> addresses = addressService.getAddressList(userId);
        return Result.success(addresses);
    }

    @PostMapping("/address")
    public Result<Void> addAddress(@Valid @RequestBody AddressDTO dto) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        try {
            addressService.addAddress(userId, dto);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PutMapping("/address")
    public Result<Void> updateAddress(@Valid @RequestBody AddressDTO dto) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        try {
            addressService.updateAddress(userId, dto);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @DeleteMapping("/address/{id}")
    public Result<Void> deleteAddress(@PathVariable Integer id) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        try {
            addressService.deleteAddress(userId, id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PutMapping("/address/{id}/default")
    public Result<Void> setDefaultAddress(@PathVariable Integer id) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        try {
            addressService.setDefaultAddress(userId, id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}