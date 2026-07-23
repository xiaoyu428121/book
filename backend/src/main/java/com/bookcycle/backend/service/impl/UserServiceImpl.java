package com.bookcycle.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bookcycle.backend.dto.LoginDTO;
import com.bookcycle.backend.dto.RegisterDTO;
import com.bookcycle.backend.dto.UpdatePasswordDTO;
import com.bookcycle.backend.dto.UpdateProfileDTO;
import com.bookcycle.backend.entity.User;
import com.bookcycle.backend.mapper.UserMapper;
import com.bookcycle.backend.service.UserService;
import com.bookcycle.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public User register(RegisterDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setNickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname() : dto.getUsername());
        user.setPhone(dto.getPhone());
        user.setRole("user");
        user.setBalance(BigDecimal.ZERO);
        user.setStatus(1);

        userMapper.insert(user);
        return user;
    }

    @Override
    public User login(LoginDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new RuntimeException("用户名不存在");
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        if (!dto.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        user.setToken(token);
        user.setPassword(null);

        return user;
    }

    @Override
    public User getProfile(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    @Override
    public void updateProfile(Integer userId, UpdateProfileDTO dto) {
        System.out.println("===== 更新用户信息 =====");
        System.out.println("用户ID: " + userId);
        System.out.println("新昵称: " + dto.getNickname());
        System.out.println("新手机号: " + dto.getPhone());

        User user = new User();
        user.setId(userId);
        user.setNickname(dto.getNickname());
        user.setAvatar(dto.getAvatar());
        user.setPhone(dto.getPhone());
        int count = userMapper.updateById(user);

        System.out.println("更新结果: " + count + " 行受影响");
        System.out.println("========================");
    }

    @Override
    public void updatePassword(Integer userId, UpdatePasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!dto.getOldPassword().equals(user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(dto.getNewPassword());
        userMapper.updateById(updateUser);
    }

    @Override
    public IPage<User> getUserList(Integer page, Integer size) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        // 改动处：改为按ID升序
        wrapper.orderByAsc(User::getId);
        IPage<User> result = userMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(user -> user.setPassword(null));
        return result;
    }

    @Override
    public void updateUserStatus(Integer userId, Integer status) {
        User user = new User();
        user.setId(userId);
        user.setStatus(status);
        userMapper.updateById(user);
    }
}