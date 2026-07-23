package com.bookcycle.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bookcycle.backend.dto.LoginDTO;
import com.bookcycle.backend.dto.RegisterDTO;
import com.bookcycle.backend.dto.UpdatePasswordDTO;
import com.bookcycle.backend.dto.UpdateProfileDTO;
import com.bookcycle.backend.entity.User;

public interface UserService {
    User register(RegisterDTO dto);
    User login(LoginDTO dto);
    User getProfile(Integer userId);
    void updateProfile(Integer userId, UpdateProfileDTO dto);
    void updatePassword(Integer userId, UpdatePasswordDTO dto);
    IPage<User> getUserList(Integer page, Integer size);
    void updateUserStatus(Integer userId, Integer status);
}
