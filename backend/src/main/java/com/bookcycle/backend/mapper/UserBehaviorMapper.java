package com.bookcycle.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookcycle.backend.entity.UserBehavior;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserBehaviorMapper extends BaseMapper<UserBehavior> {
}